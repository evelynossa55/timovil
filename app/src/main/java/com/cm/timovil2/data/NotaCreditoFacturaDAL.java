package com.cm.timovil2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.DetalleResumenNotaCreditoFacturaPorDevolucionDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/03/18.
 */

public class NotaCreditoFacturaDAL extends DAL{

    private final ActivityBase contexto;
    private StringBuilder detalleNotaCreditoPorDevolucionToPrint;
    private int cantdadNotas = 0;

    public NotaCreditoFacturaDAL(ActivityBase context) {
        super(context, DAL.TablaNotaCreditoFactura);
        contexto = context;
        detalleNotaCreditoPorDevolucionToPrint = new StringBuilder();
    }

    public NotaCreditoFacturaDTO obtenerPorID(String Id) {
        NotaCreditoFacturaDTO notaDto = null;
        String filtro = "IdNotaCreditoFactura=?";
        Cursor cursor = super.obtener(columnas, null, filtro, new String[]{Id});
        if (cursor != null && cursor.moveToFirst()) {
            notaDto = getFromCursor(cursor);
            if (notaDto != null && !notaDto.NumeroFactura.equals("")) {
                notaDto.DetalleNotaCreditoFactura = new DetalleNotaCreditoFacturaDAL(contexto)
                        .ObtenerListado(notaDto.NumeroDocumento);
            }
        }
        return notaDto;
    }

    public void insertar(NotaCreditoFacturaDTO nc) {

        ContentValues values = new ContentValues();
        values.put("NumeroFactura", nc.NumeroFactura);
        values.put("Fecha", nc.Fecha);
        values.put("NumeroDocumento", nc.NumeroDocumento);
        values.put("Subtotal", nc.Subtotal);
        values.put("Descuento", nc.Descuento);
        values.put("Ipoconsumo", nc.Ipoconsumo);
        values.put("Iva5", nc.Iva5);
        values.put("Iva19", nc.Iva19);
        values.put("Iva", nc.Iva);
        values.put("Valor", nc.Valor);
        values.put("Sincronizada", false);
        values.put("CodigoBodega", nc.CodigoBodega);
        values.put("Anulada", nc.Anulada);
        values.put("QRInputValue", nc.QRInputValue);
        values.put("Cufe", nc.Cufe);

        long id = super.insertar(values);
        if (id > 0) {
            new ResolucionDAL(contexto).IncrementarSiguienteNotaCredito();
        }

        // Ahora, voy a guardar el detalle
        DetalleNotaCreditoFacturaDAL detalleDal = new DetalleNotaCreditoFacturaDAL(contexto);
        for (DetalleNotaCreditoFacturaDTO detalle : nc.DetalleNotaCreditoFactura) {
            detalleDal.insertar(detalle);
        }
    }

    public int eliminar() {
        //Detalle
        new DetalleNotaCreditoFacturaDAL(contexto).Eliminar();
        return super.eliminar(null);
    }

    private Cursor obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public synchronized ArrayList<NotaCreditoFacturaDTO> obtenerListado(boolean cargarDetalle, DateTime fechaDesde, DateTime fechaHasta) {

        ArrayList<NotaCreditoFacturaDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.obtener("Fecha DESC", null, null);
        if (cursor.isClosed()) return lista;

        DetalleNotaCreditoFacturaDAL df = new DetalleNotaCreditoFacturaDAL(contexto);

        if (cursor.moveToFirst()) {
            do {
                NotaCreditoFacturaDTO f = getFromCursor(cursor);
                lista.add(f);

                long _fechaResumen = f.Fecha;
                DateTime fechaResumen = new DateTime(_fechaResumen);

                boolean sameDay =
                        (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                boolean isBetween = (fechaResumen.isAfter(fechaDesde) &&
                        fechaResumen.isBefore(fechaHasta));

                if(sameDay || isBetween){
                    if(!f.Anulada) {
                        App.ResumenNotasCreditoPorDevolucion.Subtotal += f.Subtotal;
                        App.ResumenNotasCreditoPorDevolucion.Descuento += f.Descuento;
                        App.ResumenNotasCreditoPorDevolucion.Iva += f.Iva;
                        App.ResumenNotasCreditoPorDevolucion.Iva5 += f.Iva5;
                        App.ResumenNotasCreditoPorDevolucion.Iva19 += f.Iva19;
                        App.ResumenNotasCreditoPorDevolucion.Ipoconsumo += f.Ipoconsumo;
                        App.ResumenNotasCreditoPorDevolucion.Valor += f.Valor;
                    }
                }

                if (cargarDetalle) {
                    f.DetalleNotaCreditoFactura = df.ObtenerListado(f.NumeroDocumento);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public synchronized ArrayList<NotaCreditoFacturaDTO> obtenerListadoFiltros(boolean cargarDetalle, DateTime fechaDesde, DateTime fechaHasta,String Parametro) {

        ArrayList<NotaCreditoFacturaDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.obtener("Fecha DESC", null, null);
        if (cursor.isClosed()) return lista;

        DetalleNotaCreditoFacturaDAL df = new DetalleNotaCreditoFacturaDAL(contexto);

        if (cursor.moveToFirst()) {
            do {
                NotaCreditoFacturaDTO f = getFromCursor(cursor);
                if(f.CodigoBodega.toUpperCase().contains(Parametro.toUpperCase())||
                        f.NumeroFactura.toUpperCase().contains(Parametro.toUpperCase())||
                        f.NumeroDocumento.toUpperCase().contains(Parametro.toUpperCase())||
                        f.Cufe.toUpperCase().contains(Parametro.toUpperCase())||
                        f.IdNotaCreditoFactura==(Integer.parseInt(Parametro))){
                    lista.add(f);

                    long _fechaResumen = f.Fecha;
                    DateTime fechaResumen = new DateTime(_fechaResumen);

                    boolean sameDay =
                            (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                    || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                    boolean isBetween = (fechaResumen.isAfter(fechaDesde) &&
                            fechaResumen.isBefore(fechaHasta));

                    if(sameDay || isBetween){
                        if(!f.Anulada) {
                            App.ResumenNotasCreditoPorDevolucion.Subtotal += f.Subtotal;
                            App.ResumenNotasCreditoPorDevolucion.Descuento += f.Descuento;
                            App.ResumenNotasCreditoPorDevolucion.Iva += f.Iva;
                            App.ResumenNotasCreditoPorDevolucion.Iva5 += f.Iva5;
                            App.ResumenNotasCreditoPorDevolucion.Iva19 += f.Iva19;
                            App.ResumenNotasCreditoPorDevolucion.Ipoconsumo += f.Ipoconsumo;
                            App.ResumenNotasCreditoPorDevolucion.Valor += f.Valor;
                        }
                    }

                    if (cargarDetalle) {
                        f.DetalleNotaCreditoFactura = df.ObtenerListado(f.NumeroDocumento);
                    }
                }


            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public synchronized ArrayList<NotaCreditoFacturaDTO> obtenerListadoPendientes() {
        ArrayList<NotaCreditoFacturaDTO> lista = new ArrayList<>();
        String filtro = "Sincronizada = ?";
        String[] parametros = {"0"};

        Cursor cursor = this.obtener(null, filtro, parametros);
        DetalleNotaCreditoFacturaDAL dn = new DetalleNotaCreditoFacturaDAL(contexto);
        if (cursor.moveToFirst()) {
            do {
                NotaCreditoFacturaDTO f = getFromCursor(cursor);
                f.DetalleNotaCreditoFactura = dn.ObtenerListado(f.NumeroDocumento);
                lista.add(f);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public NotaCreditoFacturaDTO obtenerPorNumeroFac(String numeroFactura) {

        String orderBy = "Fecha ASC";
        String filtro = "NumeroFactura = ?";
        String[] params = new String[]{numeroFactura};
        Cursor cursor = super.obtener(columnas, orderBy, filtro, params);
        if (cursor.moveToFirst()) {
            DetalleNotaCreditoFacturaDAL df = new DetalleNotaCreditoFacturaDAL(contexto);
            NotaCreditoFacturaDTO nota = getFromCursor(cursor);
            nota.DetalleNotaCreditoFactura = df.ObtenerListado(nota.NumeroDocumento);
            return nota;
        }
        else return null;
    }

    public NotaCreditoFacturaDTO obtenerPorNumeroDocumento(String numeroDocumento) {

        String orderBy = "Fecha ASC";
        String filtro = "NumeroDocumento = ?";
        String[] params = new String[]{numeroDocumento};
        Cursor cursor = super.obtener(columnas, orderBy, filtro, params);
        if (cursor.moveToFirst()) {
            DetalleNotaCreditoFacturaDAL df = new DetalleNotaCreditoFacturaDAL(contexto);
            NotaCreditoFacturaDTO nota = getFromCursor(cursor);
            nota.DetalleNotaCreditoFactura = df.ObtenerListado(nota.NumeroDocumento);
            return nota;
        }
        else return null;
    }

    public int obtenerCantidadNotasCreditoPorDevolucion() {
        return cantdadNotas;
    }

    private NotaCreditoFacturaDTO getFromCursor(Cursor cursor) {
        NotaCreditoFacturaDTO c = new NotaCreditoFacturaDTO();
        c.IdNotaCreditoFactura = cursor.getInt(0);
        c.NumeroDocumento = cursor.getString(1);
        c.NumeroFactura = cursor.getString(2);
        c.Fecha = cursor.getLong(3);
        c.Subtotal = cursor.getFloat(4);
        c.Descuento = cursor.getFloat(5);
        c.Ipoconsumo = cursor.getFloat(6);
        c.Iva5 = cursor.getFloat(7);
        c.Iva19 = cursor.getFloat(8);
        c.Iva = cursor.getFloat(9);
        c.Valor = cursor.getFloat(10);
        c.Sincronizada = cursor.getInt(11) == 1;
        c.CodigoBodega = cursor.getString(12);
        c.Anulada = cursor.getInt(13) == 1;
        c.QRInputValue = cursor.getString(14);
        c.Cufe = cursor.getString(15);
        return c;
    }

    public synchronized String sincronizarNotaCreditoFactura(NotaCreditoFacturaDTO nota) throws Exception {

        if (!Utilities.isNetworkReachable(contexto) || !Utilities.isNetworkConnected(contexto)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        if (App.SincronizandoNotaCredito && App.SincronizandoNotaCreditoNumero.contains(nota.NumeroDocumento)) {
            return "Sincronizando";
        }

        Log.i("sincronizarNotaCredito", "Sincronizando " + nota.NumeroDocumento);
        App.SincronizandoNotaCredito = true;
        App.SincronizandoNotaCreditoNumero.add(nota.NumeroDocumento);

        ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();
        String respuesta = "";

        if (resolucionDTO != null) {

            if (!nota.Sincronizada) {

                JSONObject jsonNota = nota.toJSON();
                jsonNota.put("IdCliTimo", resolucionDTO.IdCliente);
                jsonNota.put("CodigoRuta", resolucionDTO.CodigoRuta);

                NetWorkHelper r = new NetWorkHelper();
                respuesta = r.writeService(jsonNota, SincroHelper.getNotaCreditoDevolucion_EnviarURL());
                respuesta = SincroHelper.procesarRemisionJson(respuesta);

                if (respuesta.equals("OK")) {
                    // Aquí debo actualizar el estado de la remision
                    setSincronizedStatus(nota.NumeroDocumento);

                } else if (respuesta.equals(Utilities.IMEI_ERROR)) {
                    App.guardarConfiguracionEstadoAplicacion("B", contexto);
                }
            }
        }

        App.SincronizandoNotaCreditoNumero.remove(nota. NumeroDocumento);
        App.SincronizandoNotaCredito = App.SincronizandoNotaCreditoNumero.size() > 0;

        return respuesta;
    }

    private void setSincronizedStatus(String numeroDocumento) throws Exception {
        try {
            String update = "UPDATE "
                    + TablaNotaCreditoFactura
                    + " SET Sincronizada=1"
                    + " WHERE NumeroDocumento='" + numeroDocumento + "'";
            executeQuery(update);
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:" + e.getMessage());
        }
    }

    public String descargarPendientes() {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<NotaCreditoFacturaDTO> lista = obtenerListadoPendientes();
            for (NotaCreditoFacturaDTO n : lista) {
                try{
                    n.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_SINCRO;
                    String respuesta = sincronizarNotaCreditoFactura(n);
                    sb.append("Numero documento: ").append(n.NumeroDocumento).append(": ").append(respuesta).append("\n");
                }catch (Exception ex){
                    App.SincronizandoNotaCreditoNumero.remove(n.NumeroDocumento);
                    App.SincronizandoNotaCredito = App.SincronizandoNotaCreditoNumero.size() > 0;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String obtenerDetalleResumenNotaCreditoPorDevolucion
            (DateTime fechaDesde, DateTime fechaHasta) throws Exception {
        try {

            StringBuilder detalle = new StringBuilder();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat formatter2 = new DecimalFormat("$###,###.##");

            String linea = "\r\n-------------------------------\r\n";

            DetalleNotaCreditoFacturaDAL detalleNotaCreditoFacturaDAL = new DetalleNotaCreditoFacturaDAL(contexto);
            ProductoDAL productoDAL = new ProductoDAL(contexto);

            ArrayList<ProductoDTO> productos = productoDAL.obtenerListadoEnNotaCreditoPorDevolucion();
            ArrayList<String> productos_aux = new ArrayList<>();

            ArrayList<DetalleResumenNotaCreditoFacturaPorDevolucionDTO> detalleResumenProductos = new ArrayList<>();
            if (productos != null && productos.size() > 0) {
                for (ProductoDTO productoDTO : productos) {

                    String key = productoDTO.IdProducto + productoDTO.CodigoBodega;
                    if (!productos_aux.contains(key)) {
                        productos_aux.add(key);
                    } else {
                        continue;
                    }

                    DetalleResumenNotaCreditoFacturaPorDevolucionDTO resumenProducto =
                            detalleNotaCreditoFacturaDAL.
                            obtenerResumenProducto(productoDTO, fechaDesde, fechaHasta);

                    if (resumenProducto.Cantidad != 0) {
                        detalleResumenProductos.add(resumenProducto);
                    }
                }
            }

            if (detalleResumenProductos.size() > 0) {

                detalleNotaCreditoPorDevolucionToPrint.append(" Producto \r\n Cant  |  Iva  |  Val\r\n");

                for (DetalleResumenNotaCreditoFacturaPorDevolucionDTO dto : detalleResumenProductos) {
                    detalle.append(dto.Nombre)
                            .append("\nCantidad: ").append(dto.Cantidad)
                            .append("\nSubtotal: ").append(dto.Subtotal)
                            .append("\nDescuento: ").append(dto.Descuento)
                            .append("\nIva: ").append(formatter.format(dto.Iva))
                            .append("\nValor: ").append(formatter.format(dto.Valor))
                            .append(linea);

                    detalleNotaCreditoPorDevolucionToPrint.append(" ").append(dto.Nombre)
                            .append("\r\n")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Cantidad), 5))
                            .append("|")
                            .append(Utilities.completarEspacios(formatter2.format(dto.Iva), 5))
                            .append("|")
                            .append(Utilities.completarEspacios(formatter2.format(dto.Valor), 5));

                    cantdadNotas += dto.Cantidad;
                }
            } else {
                return ("No se ha realizado ninguna nota crédito por devolución");
            }

            return detalle.toString();
        } catch (Exception e) {
            throw new Exception("Error generando el resumen: " + e.getMessage());
        }
    }

    public String obtenerDetalleResumenNotaCreditoPorDevolucionToPrint() {
        return detalleNotaCreditoPorDevolucionToPrint.toString();
    }

    private Cursor obtenerPorFecha() {
        String orderBy = "Fecha ASC";
        return super.obtener(columnas, orderBy, null, null);
    }

    public String obtenerPrimeraYultimaNotaCreditoPorDevolucion
            (DateTime fechaDesde, DateTime fechaHasta) {

        StringBuilder result = new StringBuilder();
        Cursor cursor = obtenerPorFecha();
        ArrayList<NotaCreditoFacturaDTO> notasCreditoFacturaPorDevolucionHoy = null;

        if (cursor != null && cursor.moveToFirst()) {
            notasCreditoFacturaPorDevolucionHoy = new ArrayList<>();
            do {
                NotaCreditoFacturaDTO nota = getFromCursor(cursor);
                DateTime fechaNota = new DateTime(nota.Fecha);

                boolean sameDay =
                        (fechaNota.toLocalDate().toString()
                                .equals(fechaDesde.toLocalDate().toString())
                        ) ||
                                (fechaNota.toLocalDate().toString()
                                        .equals(fechaHasta.toLocalDate().toString())
                                );

                boolean between =
                        (fechaNota.isAfter(fechaDesde)) &&
                                (fechaNota.isBefore(fechaHasta));

                if (sameDay || between) {
                    notasCreditoFacturaPorDevolucionHoy.add(nota);
                }

            } while (cursor.moveToNext());
        }

        Date date;
        if (notasCreditoFacturaPorDevolucionHoy != null && notasCreditoFacturaPorDevolucionHoy.size() > 0) {
            NotaCreditoFacturaDTO primeraNota = notasCreditoFacturaPorDevolucionHoy.get(0);
            date = new Date(primeraNota.Fecha);
            result.append(" PRIMERA NOTA CREDITO POR DEVOLUCION: ")
                    .append(primeraNota.NumeroDocumento).append("\r\n El ")
                    .append(Utilities.FechaDetallada(date))
                    .append("\r\n");
            NotaCreditoFacturaDTO ultimaNota = notasCreditoFacturaPorDevolucionHoy.get(notasCreditoFacturaPorDevolucionHoy.size()-1);
            date = new Date(ultimaNota.Fecha);
            result.append(" ULTIMA NOTA CREDITO POR DEVOLUCION: ")
                    .append(ultimaNota.NumeroDocumento).append("\r\n El ")
                    .append(Utilities.FechaDetallada(date));
        }

        return result.toString();
    }

    public void anular(String numeroFactura) throws Exception{
        try {
            String update = "UPDATE " + TablaNotaCreditoFactura
                    + " SET Anulada=1, Subtotal = 0, Valor = 0,"
                    + " Descuento = 0, Ipoconsumo = 0, Iva = 0, Iva5 = 0, Iva19 = 0 " +
                    "WHERE NumeroFactura=" + "'" + numeroFactura + "'";
            executeQuery(update);

            //NOTA:
            //La devolución de los productos a la bodega se hace al anular la factura

        } catch (Exception e) {
            throw new Exception("Error anulando la nota crédito por devolución:" + e.getMessage());
        }
    }

    @Override
    void setColumns() {
        columnas = new String[]{
                "IdNotaCreditoFactura",
                "NumeroDocumento",
                "NumeroFactura",
                "Fecha",
                "Subtotal",
                "Descuento",
                "Ipoconsumo",
                "Iva5",
                "Iva19",
                "Iva",
                "Valor",
                "Sincronizada",
                "CodigoBodega",
                "Anulada",
                "QRInputValue",
                "Cufe"
        };
    }
}
