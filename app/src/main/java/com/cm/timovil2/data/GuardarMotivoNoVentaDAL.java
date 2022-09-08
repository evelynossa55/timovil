package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 31/01/17.
 */
public class GuardarMotivoNoVentaDAL extends DAL {


    private final Context context;

    public GuardarMotivoNoVentaDAL(Context context) {
        super(context, DAL.TablaGuardarMotivoNoVenta);
        this.context = context;
    }

    public void Insertar(GuardarMotivoNoVentaDTO pedido) {
        ContentValues values = new ContentValues();
        //values.put("IdMotivoNoVenta", pedido.IdMotivoNoVenta);
        values.put("CodigoRuta", pedido.CodigoRuta);
        values.put("Descripcion", pedido.Descripcion);
        values.put("esOtroMotivo", pedido.esOtroMotivo ? 1 : 0);
        values.put("Fecha", pedido.Fecha);
        values.put("Fecha_long", pedido.Fecha_long);
        values.put("IdCliente", pedido.IdCliente);
        values.put("IdClienteTimovil", pedido.IdClienteTimovil);
        values.put("IdMotivo", pedido.IdMotivo);
        values.put("Latitud", pedido.Latitud);
        values.put("Longitud", pedido.Longitud);
        values.put("Motivo", pedido.Motivo);
        values.put("Sincronizada", pedido.Sincronizada);

        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    public String eliminar(GuardarMotivoNoVentaDTO dto) throws Exception {
        try {
            String update = "DELETE FROM " + TablaGuardarMotivoNoVenta
                    + " WHERE IdMotivoNoVenta=" + dto.IdMotivoNoVenta;
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error eliminando la No venta:" + e.getMessage());
        }
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<GuardarMotivoNoVentaDTO> obtenerListadoPendientes() {
        ArrayList<GuardarMotivoNoVentaDTO> lista = new ArrayList<>();
        String filtro = "Sincronizada = ?";
        String[] parametros = {"0"};

        Cursor cursor;
        cursor = this.Obtener(null, filtro, parametros);
        if (cursor.moveToFirst()) {
            do {

                GuardarMotivoNoVentaDTO dto = new GuardarMotivoNoVentaDTO();
                dto.IdMotivoNoVenta = cursor.getInt(0);
                dto.IdMotivo = cursor.getInt(1);
                dto.IdCliente = cursor.getInt(2);
                dto.CodigoRuta = cursor.getString(3);
                dto.Descripcion = cursor.getString(4);
                dto.Fecha = cursor.getString(5);
                dto.Fecha_long = cursor.getLong(6);
                dto.Latitud = cursor.getString(7);
                dto.Longitud = cursor.getString(8);
                dto.IdClienteTimovil = cursor.getString(9);
                dto.esOtroMotivo = cursor.getInt(10) == 1;
                dto.Motivo = cursor.getString(11);
                dto.Sincronizada = cursor.getInt(12) == 1;
                lista.add(dto);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ArrayList<GuardarMotivoNoVentaDTO> ObtenerListado(int idMotivo) {
        ArrayList<GuardarMotivoNoVentaDTO> lista = new ArrayList<>();
        Cursor cursor;
        String filtro = "IdMotivo=?";
        cursor = this.Obtener("IdMotivo ASC", filtro, new String[]{String.valueOf(idMotivo)});
        if (cursor.moveToFirst()) {
            do {
                GuardarMotivoNoVentaDTO dto = new GuardarMotivoNoVentaDTO();
                dto.IdMotivoNoVenta = cursor.getInt(0);
                dto.IdMotivo = cursor.getInt(1);
                dto.IdCliente = cursor.getInt(2);
                dto.CodigoRuta = cursor.getString(3);
                dto.Descripcion = cursor.getString(4);
                dto.Fecha = cursor.getString(5);
                dto.Fecha_long = cursor.getLong(6);
                dto.Latitud = cursor.getString(7);
                dto.Longitud = cursor.getString(8);
                dto.IdClienteTimovil = cursor.getString(9);
                dto.esOtroMotivo = cursor.getInt(10) == 1;
                dto.Motivo = cursor.getString(11);
                dto.Sincronizada = cursor.getInt(12) == 1;
                lista.add(dto);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public GuardarMotivoNoVentaDTO ObtenerMotivoNoVentaDto(int idMotivoNoVenta) {
        GuardarMotivoNoVentaDTO dto = null;
        Cursor cursor;
        String filtro = "IdMotivoNoVenta=?";
        cursor = this.Obtener(null, filtro, new String[]{String.valueOf(idMotivoNoVenta)});
        if (cursor.moveToFirst()) {
            dto = new GuardarMotivoNoVentaDTO();
            dto.IdMotivoNoVenta = cursor.getInt(0);
            dto.IdMotivo = cursor.getInt(1);
            dto.IdCliente = cursor.getInt(2);
            dto.CodigoRuta = cursor.getString(3);
            dto.Descripcion = cursor.getString(4);
            dto.Fecha = cursor.getString(5);
            dto.Fecha_long = cursor.getLong(6);
            dto.Latitud = cursor.getString(7);
            dto.Longitud = cursor.getString(8);
            dto.IdClienteTimovil = cursor.getString(9);
            dto.esOtroMotivo = cursor.getInt(10) == 1;
            dto.Motivo = cursor.getString(11);
            dto.Sincronizada = cursor.getInt(12) == 1;
        }
        cursor.close();
        return dto;
    }

    public ArrayList<GuardarMotivoNoVentaDTO> ObtenerListado() {
        ArrayList<GuardarMotivoNoVentaDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener("Fecha_long DESC", null, null);
        if (cursor.moveToFirst()) {
            do {
                GuardarMotivoNoVentaDTO dto = new GuardarMotivoNoVentaDTO();
                dto.IdMotivoNoVenta = cursor.getInt(0);
                dto.IdMotivo = cursor.getInt(1);
                dto.IdCliente = cursor.getInt(2);
                dto.CodigoRuta = cursor.getString(3);
                dto.Descripcion = cursor.getString(4);
                dto.Fecha = cursor.getString(5);
                dto.Fecha_long = cursor.getLong(6);
                dto.Latitud = cursor.getString(7);
                dto.Longitud = cursor.getString(8);
                dto.IdClienteTimovil = cursor.getString(9);
                dto.esOtroMotivo = cursor.getInt(10) == 1;
                dto.Motivo = cursor.getString(11);
                dto.Sincronizada = cursor.getInt(12) == 1;
                lista.add(dto);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public String descargarPendientes() throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<GuardarMotivoNoVentaDTO> lista = obtenerListadoPendientes();
            for (GuardarMotivoNoVentaDTO m : lista) {

                String respuesta;
                try {
                    m.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_SINCRO;
                    respuesta = sincronizarPendiente(m);

                    if (respuesta.equals("Sincronizando")) {
                        respuesta = "La No Venta ya se estaba sincronizando, por favor intenta nuevamente";
                    }
                } catch (Exception e) {
                    App.SincronizandoNoVentaIdMotivoNoVenta.remove(Integer.valueOf(m.IdMotivoNoVenta));
                    App.SincronizandoNoVenta = App.SincronizandoNoVentaIdMotivoNoVenta.size() > 0;
                    respuesta = e.toString();
                }

                sb.append(m.Motivo).append(": ").append(respuesta).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public String sincronizarPendiente(GuardarMotivoNoVentaDTO pendiente) throws Exception {

        if (!Utilities.isNetworkReachable(context)
                || !Utilities.isNetworkConnected(context)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        if (App.SincronizandoNoVenta &&
                App.SincronizandoNoVentaIdMotivoNoVenta.contains(pendiente.IdMotivoNoVenta)) {
            return "Sincronizando";
        }

        Log.i("sincronizarAbono", "Sincronizando " + pendiente.IdMotivoNoVenta);
        App.SincronizandoNoVenta = true;
        App.SincronizandoNoVentaIdMotivoNoVenta.add(pendiente.IdMotivoNoVenta);

        JSONObject jsonNoVenta = new JSONObject();
        jsonNoVenta.put("IdC", pendiente.IdCliente);
        jsonNoVenta.put("IdM", pendiente.IdMotivo);
        jsonNoVenta.put("CR", pendiente.CodigoRuta);
        jsonNoVenta.put("Des", pendiente.Descripcion);
        jsonNoVenta.put("F", pendiente.Fecha);
        jsonNoVenta.put("La", pendiente.Latitud);
        jsonNoVenta.put("Lo", pendiente.Longitud);
        jsonNoVenta.put("IdCT", pendiente.IdClienteTimovil);
        jsonNoVenta.put("O", pendiente.esOtroMotivo);
        jsonNoVenta.put("ODes", pendiente.Motivo);
        jsonNoVenta.put("EnvDes", pendiente.EnviadaDesde);

        NetWorkHelper netWorkHelper = new NetWorkHelper();
        String jsonRespuesta = netWorkHelper.writeService(jsonNoVenta, SincroHelper.getIngresarNoVentaURL());
        jsonRespuesta = SincroHelper.procesarOkJson(jsonRespuesta);

        if (jsonRespuesta.equals("OK")) {
            // Aquí debo actualizar el estado del Motivo
            actualizarEstadoDescarga(pendiente.IdMotivoNoVenta);
        }

        App.SincronizandoNoVentaIdMotivoNoVenta.remove(Integer.valueOf(pendiente.IdMotivoNoVenta));
        App.SincronizandoNoVenta = App.SincronizandoNoVentaIdMotivoNoVenta.size() > 0;
        return jsonRespuesta;
    }

    private String actualizarEstadoDescarga(int idMotivoNoVEnta) throws Exception {
        try {

            String update = "update " + TablaGuardarMotivoNoVenta
                    + " set Sincronizada = 1 where IdMotivoNoVenta = " + idMotivoNoVEnta;
            executeQuery(update);
            return "OK";

        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:"
                    + e.getMessage());
        }

    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdMotivoNoVenta", "IdMotivo", "IdCliente", "CodigoRuta",
                "Descripcion", "Fecha", "Fecha_long", "Latitud", "Longitud",
                "IdClienteTimovil", "esOtroMotivo", "Motivo", "Sincronizada"};
    }
}
