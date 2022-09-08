package com.cm.timovil2.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MAbono;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;
import org.json.JSONObject;

public class AbonoFacturaDAL extends DAL {

    private final ActivityBase context;

    public AbonoFacturaDAL(ActivityBase context) {
        super(context, DAL.TablaAbonoFactura);
        this.context = context;
    }

    public String insertar(MAbono abono) throws Exception {
        float saldo = obtenerSaldoFactura(abono.NumeroFactura);
        if (saldo >= abono.Valor) {
            ContentValues values = new ContentValues();

            values.put("IdFactura", abono.IdFactura);
            values.put("NumeroFactura", abono.NumeroFactura);
            values.put("Fecha", abono.Fecha);
            values.put("Valor", abono.Valor);
            values.put("Saldo", abono.Saldo);
            values.put("Identificador", abono.Identificador);
            values.put("Sincronizado", abono.Sincronizado ? 1 : 0);
            values.put("DiaCreacion", abono.DiaCreacion);
            values.put("IdCuentaCaja", abono.IdCuentaCaja);
            values.put("FechaCreacion", abono.FechaCreacion);

            abono._Id = (int) super.insertar(values);
            super.executeQuery(
                    "update " + TablaFacturaCredito
                            + " set Saldo=" + abono.Saldo +
                            " where Numerofactura=" + "'" + abono.NumeroFactura + "'");
            return "OK";
        } else {
            throw new Exception("El valor del abono no puede ser mayor al saldo pendiente: " + saldo);
        }
    }

    public int eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    private MAbono getFromCursor(Cursor cursor) {
        MAbono abono = new MAbono();
        abono._Id = Integer.parseInt(cursor.getString(0));

        abono.IdFactura = cursor.getString(1);
        abono.NumeroFactura = cursor.getString(2);
        abono.Fecha = cursor.getString(3);
        abono.Valor = cursor.getFloat(4);
        abono.Saldo = cursor.getFloat(5);
        abono.Identificador = cursor.getString(6);
        abono.Sincronizado = cursor.getInt(7) == 1;
        abono.DiaCreacion = cursor.getString(8);
        abono.IdCuentaCaja = cursor.getInt(9);
        abono.FechaCreacion = cursor.getLong(10);
        return abono;
    }

    private ArrayList<MAbono> obtenerListadoPorRangoFechas(DateTime fechaDesde, DateTime fechaHasta) {
        ArrayList<MAbono> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener(null, null, null);

        if (cursor.moveToFirst()) {
            do {

                long _fechaResumen = cursor.getLong(10);
                DateTime fechaResumen = new DateTime(_fechaResumen);

                boolean sameDay =
                        (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                boolean between = (fechaResumen.isAfter(fechaDesde)) && (fechaResumen.isBefore(fechaHasta));

                if (sameDay || between) {
                    lista.add(getFromCursor(cursor));
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }


    public synchronized void contarAbonosPorRangoFechas(DateTime fechaDesde, DateTime fechaHasta) throws Exception {
        try {
            ArrayList<MAbono> abonos = obtenerListadoPorRangoFechas(fechaDesde, fechaHasta);
            App.ResumenFacturacion.debito = 0;
            App.ResumenFacturacion.abonosPendientes = 0;
            for (MAbono a : abonos) {
                App.ResumenFacturacion.debito += a.Valor;
                if (!a.Sincronizado) {
                    App.ResumenFacturacion.abonosPendientes++;
                }
            }
        } catch (Exception e) {
            throw new Exception("Error contando los abonos: \nDetalle " + e.toString());
        }
    }

    private Float obtenerSaldoFactura(String numeroFactura) {
        Float Saldo;
        Saldo = (float) 0;
        String filtro = "Select Saldo from FacturaCredito where NumeroFactura = ?";
        String[] parametros = {numeroFactura};

        Cursor cursor;
        cursor = this.obtener(filtro, parametros);

        if (cursor.moveToFirst()) {
            do {
                Saldo = cursor.getFloat(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return Saldo;
    }

    public boolean tieneAbonosPendientes(String idFactura) {
        boolean sw = false;
        String filtro = "SELECT * FROM " + TablaAbonoFactura + " WHERE IdFactura = ? AND Sincronizado = ?";
        String[] parametros = {idFactura, "0"};

        Cursor cursor;
        cursor = this.obtener(filtro, parametros);

        if (cursor != null && cursor.moveToFirst()) {
            sw = true;
            cursor.close();
        }
        return sw;
    }

    public String descargarAbonos() throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<MAbono> lista = obtenerListadoPendientes();
            for (MAbono f : lista) {

                String respuesta;
                try {
                    f.EnviadoDesde = Utilities.FACTURA_ENVIADA_DESDE_SINCRO;
                    respuesta = sincronizarAbono(f);

                    if (respuesta.equals("Sincronizando")) {
                        respuesta = "El abono ya se estaba sincronizando, por favor intenta nuevamente";
                    }
                } catch (Exception e) {
                    App.SincronizandoAbonoFacturaId.remove(Integer.valueOf(f._Id));
                    App.SincronizandoAbonoFactura = App.SincronizandoAbonoFacturaId.size() > 0;
                    respuesta = e.toString();
                }

                sb.append(f.NumeroFactura).append(": ").append(respuesta).append("\n");
            }
            contarAbonosPorRangoFechas(DateTime.now(), DateTime.now());
            return sb.toString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public synchronized String sincronizarAbono(MAbono abono) throws Exception {

        if (!Utilities.isNetworkReachable(context) || !Utilities.isNetworkConnected(context)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        if (App.SincronizandoAbonoFactura &&
                App.SincronizandoAbonoFacturaId.contains(abono._Id)) {
            return "Sincronizando";
        }

        Log.i("sincronizarAbonoFactura", "Sincronizando " + abono._Id + " NF: " + abono.NumeroFactura);
        App.SincronizandoAbonoFactura = true;
        App.SincronizandoAbonoFacturaId.add(abono._Id);

        ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
        NetWorkHelper netWorkHelper = new NetWorkHelper();

        String installationId = context.getInstallationId();
        JSONObject jsonAbono = new JSONObject();
        jsonAbono.put("Identificador", abono.Identificador);
        jsonAbono.put("IdFactura", abono.IdFactura);
        jsonAbono.put("IdCuentaCaja", abono.IdCuentaCaja);
        jsonAbono.put("IdClienteTiMovil", resolucionDTO.IdCliente);
        jsonAbono.put("NumeroFactura", abono.NumeroFactura);
        jsonAbono.put("CodigoRuta", resolucionDTO.CodigoRuta);
        jsonAbono.put("Fecha", abono.Fecha);
        jsonAbono.put("Valor", abono.Valor);
        jsonAbono.put("Saldo", abono.Saldo);
        jsonAbono.put("Imei", installationId);
        jsonAbono.put("EnviadoDesde", abono.EnviadoDesde);

        String respuesta = netWorkHelper.writeService(jsonAbono, SincroHelper.ABONO_FACTURA);
        respuesta = SincroHelper.procesarOkJson(respuesta);
        if (respuesta.equals("OK")) {
            // Aquí debo actualizar el estado del Abono
            actualizarEstadoDescarga(abono._Id);
        }

        App.SincronizandoAbonoFacturaId.remove(Integer.valueOf(abono._Id));
        App.SincronizandoAbonoFactura = App.SincronizandoAbonoFacturaId.size() > 0;

        return respuesta;
    }

    private String actualizarEstadoDescarga(int _idabono) throws Exception {
        try {

            String update = "update " + TablaAbonoFactura
                    + " set Sincronizado=1 where _id=" + _idabono;
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:"
                    + e.getMessage());
        }

    }

    public synchronized ArrayList<MAbono> obtenerListadoPendientes() {
        ArrayList<MAbono> lista = new ArrayList<>();
        String filtro = "Sincronizado = ?";
        String[] parametros = {"0"};

        Cursor cursor;
        cursor = this.Obtener(null, filtro, parametros);
        if (cursor.moveToFirst()) {
            do {
                MAbono a = getFromCursor(cursor);
                lista.add(a);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public synchronized MAbono obtenerAbono(int _id) {
        MAbono abono = null;
        String filtro = "_Id = ?";
        String[] parametros = {String.valueOf(_id)};

        Cursor cursor;
        cursor = this.Obtener(null, filtro, parametros);
        if (cursor.moveToFirst()) {
                abono = getFromCursor(cursor);
        }
        cursor.close();
        return abono;
    }

    public ArrayList<MAbono> obtenerListado() {
        ArrayList<MAbono> abonos = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener(null, null, null);
        if (cursor.moveToFirst()) {
            do {
                MAbono a = getFromCursor(cursor);
                abonos.add(a);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return abonos;
    }

    @Override
    void setColumns() {
        columnas = new String[]{
                "_id",
                "IdFactura",
                "NumeroFactura",
                "Fecha",
                "Valor",
                "Saldo",
                "Identificador",
                "Sincronizado",
                "DiaCreacion",
                "IdCuentaCaja",
                "FechaCreacion"
        };
    }
}