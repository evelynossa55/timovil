package com.cm.timovil2.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class RemisionDAL extends DAL {

    private final ActivityBase contexto;

    public RemisionDAL(ActivityBase context) {
        super(context, DAL.TablaRemision);
        contexto = context;
    }

    private void validarDatosInsercion(RemisionDTO f) throws Exception {

        if (f == null) {
            throw new Exception("La remisión no ha sido inicializada.");
        }

        if (f.DetalleRemision == null || f.DetalleRemision.size() == 0) {
            throw new Exception("La remisión debe contener al menos un producto.");
        }

        boolean permitirRemisionesConValoreACero =
                App.obtenerConfiguracion_PermitirRemisionesConValorCero(contexto);

        if (f.Total == 0
                && f.Devolucion == 0
                && f.Rotacion == 0
                && !permitirRemisionesConValoreACero) {
            throw new Exception("El valor de la remisión debe ser diferente a cero.");
        }
    }

    public void insertar(RemisionDTO f) throws Exception {

        validarDatosInsercion(f);

        ContentValues values = new ContentValues();
        values.put("NumeroRemision", f.NumeroRemision);
        values.put("Fecha", f.Fecha);
        values.put("IdCliente", f.IdCliente);
        values.put("CodigoRuta", f.CodigoRuta);
        values.put("NombreRuta", f.NombreRuta);
        values.put("Subtotal", f.Subtotal);
        values.put("Iva", f.Iva);
        values.put("Total", f.Total);
        values.put("RazonSocialCliente", f.RazonSocialCliente);
        values.put("IdentificacionCliente", f.IdentificacionCliente);
        values.put("TelefonoCliente", f.TelefonoCliente);
        values.put("DireccionCliente", f.DireccionCliente);
        values.put("Anulada", (f.Anulada ? 1 : 0));
        values.put("FechaCreacion", f.FechaCreacion);
        values.put("Latitud", f.Latitud);
        values.put("Longitud", f.Longitud);
        values.put("PendienteAnulacion", f.PendienteAnulacion ? 1 : 0);
        values.put("Comentario", f.Comentario);
        values.put("Sincronizada", f.Sincronizada);
        values.put("codigoBodega", f.CodigoBodega);
        values.put("NumeroPedido", f.NumeroPedido);
        values.put("ComentarioAnulacion", f.ComentarioAnulacion);
        values.put("Descuento", f.Descuento);
        values.put("FormaPago", f.FormaPago);
        values.put("IsPedidoCallcenter", f.IsPedidoCallcenter);
        values.put("Devolucion", f.Devolucion);
        values.put("Rotacion", f.Rotacion);
        values.put("IdPedido", f.IdPedido);
        values.put("IdCaso", f.IdCaso);
        values.put("ValorDevolucion", f.ValorDevolucion);
        values.put("Ipoconsumo", f.Ipoconsumo);
        values.put("Negocio", f.Negocio);
        values.put("ValorRetefuente", f.ValorRetefuente);
        values.put("RetefuenteDevolucion", f.RetefuenteDevolucion);
        values.put("ValorReteIvaDevolucion", f.ValorReteIvaDevolucion);
        values.put("ValorReteIva", f.ValorReteIva);

        super.insertar(values);

        // Ahora, voy a actualizar la resolución
        new ResolucionDAL(contexto).IncrementarSiguienteRemision();

        // Ahora, voy a guardar el detalle
        DetalleRemisionDAL detalleDal = new DetalleRemisionDAL(contexto);
        for (DetalleRemisionDTO detalle : f.DetalleRemision) {
            detalle.NumeroRemision = f.NumeroRemision;
            detalleDal.insertar(detalle, f.CodigoBodega);
        }
    }

    public int eliminar() {
        new DetalleRemisionDAL(contexto).Eliminar();
        return super.eliminar(null);
    }

    private Cursor obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    private Cursor obtenerPorFecha() {
        String orderBy = "Fecha ASC";
        return super.obtener(columnas, orderBy, null, null);
    }

    private Cursor obtenerPorFechaDesc() {
        String orderBy = "Fecha DESC";
        return super.obtener(columnas, orderBy, null, null);
    }

    public RemisionDTO obtenerPorID(String[] _Id) {
        RemisionDTO remisionDTO = null;
        String filtro = "IdAuto = ?";
        Cursor cursor = super.obtener(columnas, null, filtro, _Id);
        if (cursor != null && cursor.moveToFirst()) {
            remisionDTO = getFromCursor(cursor);
            if (remisionDTO != null && !remisionDTO.NumeroRemision.equals("")) {
                remisionDTO.DetalleRemision = new DetalleRemisionDAL(contexto).ObtenerListado(remisionDTO.NumeroRemision);
            }
        }
        return remisionDTO;
    }


    public RemisionDTO obtenerPorNumeroFac(String numeroRemision) {

        String orderBy = "Fecha ASC";
        String filtro = "NumeroRemision = ?";
        String[] params = new String[]{numeroRemision};
        Cursor cursor = super.obtener(columnas, orderBy, filtro, params);
        if (cursor.moveToFirst()) return getFromCursor(cursor);
        else return null;
    }

    public String descargarRemisiones() throws Exception {

        if (!Utilities.isNetworkReachable(contexto) || !Utilities.isNetworkConnected(contexto)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        StringBuilder sb = new StringBuilder();
        ArrayList<RemisionDTO> lista = obtenerListadoPendientes();
        int descargasExitosas = 0;
        int descargasErroneas = 0;
        StringBuilder sbExitosas = new StringBuilder();
        StringBuilder sbErroneas = new StringBuilder();
        for (RemisionDTO r : lista) {
            try {
                r.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_SINCRO;
                String respuesta = sincronizarRemision(r);

                if (respuesta.equals("Sincronizando")) {
                    respuesta = "La remisión ya se estaba sincronizando, por favor intenta nuevamente";
                }

                if (respuesta.equals("OK")) {
                    descargasExitosas++;
                    sbExitosas.append(r.NumeroRemision).append(", ");
                } else {
                    descargasErroneas++;
                    sbErroneas.append(r.NumeroRemision).append(": ")
                            .append(respuesta).append("\n");
                }
            } catch (Exception e) {

                App.SincronizandoRemisionNumero.remove(r.NumeroRemision);
                App.SincronizandoRemision = App.SincronizandoRemisionNumero.size() > 0;

                descargasErroneas++;
                sbErroneas.append(r.NumeroRemision).append(": ")
                        .append(e.getMessage()).append("\n");
            }
        }
        sb.append(descargasExitosas).append(" remisiones descargadas:\n")
                .append(sbExitosas.toString()).append("\n");
        if (descargasErroneas > 0) {
            sb.append(descargasErroneas)
                    .append(" remisiones no se pudieron descargar:\n")
                    .append(sbErroneas.toString());
        }
        return sb.toString();
    }

    public synchronized ArrayList<RemisionDTO> obtenerListadoPendientes() {
        ArrayList<RemisionDTO> lista = new ArrayList<>();
        String filtro = "Sincronizada = ? or PendienteAnulacion = ?";
        String[] parametros = {"0", "1"};

        Cursor cursor;
        cursor = this.obtener(null, filtro, parametros);
        DetalleRemisionDAL df = new DetalleRemisionDAL(contexto);
        if (cursor.moveToFirst()) {
            do {
                RemisionDTO f = getFromCursor(cursor);
                f.DetalleRemision = df.ObtenerListado(f.NumeroRemision);
                lista.add(f);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public synchronized ArrayList<RemisionDTO> obtenerListado(boolean cargarDetalle, DateTime fechaDesde, DateTime fechaHasta) {

        ArrayList<RemisionDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.obtenerPorFechaDesc();
        DetalleRemisionDAL df = new DetalleRemisionDAL(contexto);

        if (cursor.moveToFirst()) {
            do {
                RemisionDTO f = getFromCursor(cursor);

                lista.add(f);

                long _fechaResumen = f.Fecha;
                DateTime fechaResumen = new DateTime(_fechaResumen);

                boolean sameDay =
                        (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                boolean isBetween = (fechaResumen.isAfter(fechaDesde) &&
                        fechaResumen.isBefore(fechaHasta));

                if (sameDay || isBetween) {

                    if (f.Anulada) {
                        App.ResumenRemisiones.anuladas += 1;
                        if (f.PendienteAnulacion || !f.Sincronizada) {
                            App.ResumenRemisiones.pendientesSincronizacion += 1;
                        }
                    } else {
                        App.ResumenRemisiones.subtotal += f.Subtotal;
                        App.ResumenRemisiones.descuento += f.Descuento;
                        App.ResumenRemisiones.iva += f.Iva;
                        //App.ResumenFacturacion.retefuente += f.Retefuente;
                        App.ResumenRemisiones.ValorDevolucion += f.ValorDevolucion;
                        App.ResumenRemisiones.ipoConsumo += f.Ipoconsumo;
                        App.ResumenRemisiones.total += f.Total;

                        if (f.FormaPago != null) {
                            if (f.FormaPago.equals("000")) {
                                App.ResumenRemisiones.contado += f.Total;
                            } else {
                                App.ResumenRemisiones.credito += f.Total;
                            }
                        }

                        if (f.PendienteAnulacion || !f.Sincronizada) {
                            App.ResumenRemisiones.pendientesSincronizacion += 1;
                        }
                    }
                }

                if (cargarDetalle) {
                    f.DetalleRemision = df.ObtenerListado(f.NumeroRemision);
                    if (!f.Anulada && (isBetween || sameDay)) {
                        for (DetalleRemisionDTO detalle : f.DetalleRemision) {
                            //cantidad
                            App.ResumenRemisiones.cantidad += detalle.Cantidad;
                            //devolucion
                            App.ResumenRemisiones.devoluciones += detalle.Devolucion;
                            //rotacion
                            App.ResumenRemisiones.rotaciones += detalle.Rotacion;

                        }
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public synchronized ArrayList<RemisionDTO> obtenerListadoFiltros(boolean cargarDetalle, DateTime fechaDesde, DateTime fechaHasta,String Parametro) {

        ArrayList<RemisionDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.obtenerPorFechaDesc();
        DetalleRemisionDAL df = new DetalleRemisionDAL(contexto);

        if (cursor.moveToFirst()) {
            do {
                RemisionDTO f = getFromCursor(cursor);

                if(f.CodigoBodega.toUpperCase().contains(Parametro.toUpperCase())||
                        f.CodigoRuta.toUpperCase().contains(Parametro.toUpperCase())||
                        f.NombreRuta.toUpperCase().contains(Parametro.toUpperCase())||
                        f.NumeroPedido.toUpperCase().contains(Parametro.toUpperCase())||
                        f.NumeroRemision.toUpperCase().contains(Parametro.toUpperCase())||
                        f.IdentificacionCliente.toUpperCase().contains(Parametro.toUpperCase())||
                        f.DireccionCliente.toUpperCase().contains(Parametro.toUpperCase())||
                        f.RazonSocialCliente.toUpperCase().contains(Parametro.toUpperCase())){
                    lista.add(f);

                    long _fechaResumen = f.Fecha;
                    DateTime fechaResumen = new DateTime(_fechaResumen);

                    boolean sameDay =
                            (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                    || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                    boolean isBetween = (fechaResumen.isAfter(fechaDesde) &&
                            fechaResumen.isBefore(fechaHasta));

                    if (sameDay || isBetween) {

                        if (f.Anulada) {
                            App.ResumenRemisiones.anuladas += 1;
                            if (f.PendienteAnulacion || !f.Sincronizada) {
                                App.ResumenRemisiones.pendientesSincronizacion += 1;
                            }
                        } else {
                            App.ResumenRemisiones.subtotal += f.Subtotal;
                            App.ResumenRemisiones.descuento += f.Descuento;
                            App.ResumenRemisiones.iva += f.Iva;
                            //App.ResumenFacturacion.retefuente += f.Retefuente;
                            App.ResumenRemisiones.ValorDevolucion += f.ValorDevolucion;
                            App.ResumenRemisiones.ipoConsumo += f.Ipoconsumo;
                            App.ResumenRemisiones.total += f.Total;

                            if (f.FormaPago != null) {
                                if (f.FormaPago.equals("000")) {
                                    App.ResumenRemisiones.contado += f.Total;
                                } else {
                                    App.ResumenRemisiones.credito += f.Total;
                                }
                            }

                            if (f.PendienteAnulacion || !f.Sincronizada) {
                                App.ResumenRemisiones.pendientesSincronizacion += 1;
                            }
                        }
                    }

                    if (cargarDetalle) {
                        f.DetalleRemision = df.ObtenerListado(f.NumeroRemision);
                        if (!f.Anulada && (isBetween || sameDay)) {
                            for (DetalleRemisionDTO detalle : f.DetalleRemision) {
                                //cantidad
                                App.ResumenRemisiones.cantidad += detalle.Cantidad;
                                //devolucion
                                App.ResumenRemisiones.devoluciones += detalle.Devolucion;
                                //rotacion
                                App.ResumenRemisiones.rotaciones += detalle.Rotacion;

                            }
                        }
                    }
                }


            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }


    private RemisionDTO getFromCursor(Cursor cursor) {
        RemisionDTO c = new RemisionDTO();
        c.IdAuto = cursor.getInt(0);
        c.NumeroRemision = cursor.getString(1);
        c.Fecha = Long.parseLong(cursor.getString(2));
        c.IdCliente = cursor.getInt(3);
        c.CodigoRuta = cursor.getString(4);
        c.NombreRuta = cursor.getString(5);
        c.Subtotal = cursor.getDouble(6);
        c.Iva = cursor.getDouble(7);
        c.Total = cursor.getDouble(8);
        c.RazonSocialCliente = cursor.getString(9);
        c.IdentificacionCliente = cursor.getString(10);
        c.TelefonoCliente = cursor.getString(11);
        c.DireccionCliente = cursor.getString(12);
        c.Anulada = (cursor.getInt(13) == 1);
        c.FechaCreacion = Long.parseLong(cursor.getString(14));
        c.Latitud = cursor.getString(15);
        c.Longitud = cursor.getString(16);
        c.Comentario = cursor.getString(17);
        c.Sincronizada = (cursor.getInt(18) == 1);
        c.PendienteAnulacion = (cursor.getInt(19) == 1);
        c.CodigoBodega = (cursor.getString(20));
        c.NumeroPedido = cursor.getString(21);
        c.ComentarioAnulacion = cursor.getString(22);
        c.Descuento = cursor.getFloat(23);
        c.FormaPago = cursor.getString(24);
        c.IsPedidoCallcenter = cursor.getInt(25) == 1;
        c.Devolucion = cursor.getInt(26);
        c.Rotacion = cursor.getInt(27);
        c.IdPedido = cursor.getInt(28);
        c.IdCaso = cursor.getInt(29);
        c.ValorDevolucion = cursor.getFloat(30);
        c.Ipoconsumo = cursor.getFloat(31);
        c.Negocio = cursor.getString(32);
        c.ValorRetefuente = cursor.getFloat(33);
        c.RetefuenteDevolucion = cursor.getFloat(34);
        c.ValorReteIvaDevolucion = cursor.getFloat(35);
        c.ValorReteIva = cursor.getFloat(36);
        return c;
    }

    @Override
    void setColumns() {
        columnas = new String[]{
                "IdAuto", "NumeroRemision", "Fecha",
                "IdCliente", "CodigoRuta", "NombreRuta",
                "Subtotal", "Iva", "Total", "RazonSocialCliente", "IdentificacionCliente",
                "TelefonoCliente", "DireccionCliente", "Anulada", "FechaCreacion", "Latitud",
                "Longitud", "Comentario", "Sincronizada", "PendienteAnulacion", "CodigoBodega",
                "NumeroPedido", "ComentarioAnulacion", "Descuento", "FormaPago", "IsPedidoCallcenter",
                "Devolucion", "Rotacion", "IdPedido", "IdCaso", "ValorDevolucion", "Ipoconsumo",
                "Negocio", "ValorRetefuente", "RetefuenteDevolucion", "ValorReteIvaDevolucion", "ValorReteIva"};
    }

    public synchronized String sincronizarRemision(RemisionDTO remision)
            throws Exception {

        if (!Utilities.isNetworkReachable(contexto)
                || !Utilities.isNetworkConnected(contexto)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        if (App.SincronizandoRemision &&
                App.SincronizandoRemisionNumero.contains(remision.NumeroRemision)) {
            return "Sincronizando";
        }

        Log.i("sincronizarRemision", "Sincronizando " + remision.NumeroRemision);
        App.SincronizandoRemision = true;
        App.SincronizandoRemisionNumero.add(remision.NumeroRemision);

        ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();
        String respuesta = "";
        String installationId = contexto.getInstallationId();

        if (resolucionDTO != null) {
            if (!remision.Sincronizada) {

                JSONObject jsonRemision = new JSONObject();
                jsonRemision.put("IdAuto", remision.IdAuto);
                jsonRemision.put("NroRem", remision.NumeroRemision);
                jsonRemision.put("Fecha", Utilities.FechaHoraAnsiJoda(new DateTime(remision.Fecha)));
                jsonRemision.put("IdCli", remision.IdCliente);
                jsonRemision.put("CodRu", remision.CodigoRuta);
                jsonRemision.put("NomRu", remision.NombreRuta);
                jsonRemision.put("Subt", remision.Subtotal);
                jsonRemision.put("Iva", remision.Iva);
                jsonRemision.put("Total", remision.Total);
                jsonRemision.put("RazSoc", remision.RazonSocialCliente);
                jsonRemision.put("IdentCli", remision.IdentificacionCliente);
                jsonRemision.put("TelCli", remision.TelefonoCliente);
                jsonRemision.put("DirCli", remision.DireccionCliente);
                jsonRemision.put("Anulada", remision.Anulada);
                jsonRemision.put("FechaC", remision.FechaCreacion);
                jsonRemision.put("Lat", remision.Latitud);
                jsonRemision.put("Lon", remision.Longitud);
                jsonRemision.put("Coment", remision.Comentario);
                jsonRemision.put("IdClienteTiMo", resolucionDTO.IdCliente);
                jsonRemision.put("NumPed", remision.NumeroPedido);
                jsonRemision.put("ComentAnu", remision.ComentarioAnulacion);
                jsonRemision.put("Imei", installationId);
                jsonRemision.put("Desc", remision.Descuento);
                jsonRemision.put("FPago", remision.FormaPago);
                jsonRemision.put("CodBod", remision.CodigoBodega);

                if (remision.IdPedido > 0) {
                    jsonRemision.put("Pedido", remision.IdPedido);
                }

                jsonRemision.put("Devolucion", remision.Devolucion);
                jsonRemision.put("Rotacion", remision.Rotacion);
                jsonRemision.put("ValDev", remision.ValorDevolucion);
                jsonRemision.put("Ipoc", remision.Ipoconsumo);
                jsonRemision.put("EnviadaDesde", remision.EnviadaDesde);
                jsonRemision.put("Cccb", remision.CreadaConCodigoBarras);
                jsonRemision.put("ValorRetefuente", remision.ValorRetefuente);
                jsonRemision.put("RetefuenteDevolucion", remision.RetefuenteDevolucion);
                jsonRemision.put("ValorReteIvaDevolucion", remision.ValorReteIvaDevolucion);
                jsonRemision.put("ValorReteIva", remision.ValorReteIva);

                JSONArray array = new JSONArray();
                for (DetalleRemisionDTO d : remision.DetalleRemision) {
                    JSONObject jsonDetalleRemision = new JSONObject();
                    jsonDetalleRemision.put("IdDet", d.IdDetalle);
                    jsonDetalleRemision.put("NroRem", d.NumeroRemision);
                    jsonDetalleRemision.put("IdProd", d.IdProducto);
                    jsonDetalleRemision.put("NomProd", d.NombreProducto);
                    jsonDetalleRemision.put("Cant", d.Cantidad);
                    jsonDetalleRemision.put("ValUnit", d.ValorUnitario);
                    jsonDetalleRemision.put("Subt", d.Subtotal);
                    jsonDetalleRemision.put("Total", d.Total);
                    jsonDetalleRemision.put("Iva", d.Iva);
                    jsonDetalleRemision.put("FechaC", d.FechaCreacion);
                    jsonDetalleRemision.put("PorIva", d.PorcentajeIva);
                    jsonDetalleRemision.put("Desc", d.Descuento);
                    jsonDetalleRemision.put("PorDesc", d.PorcentajeDescuento);
                    jsonDetalleRemision.put("Devol", d.Devolucion);
                    jsonDetalleRemision.put("Rotacion", d.Rotacion);
                    jsonDetalleRemision.put("ValDev", d.ValorDevolucion);
                    jsonDetalleRemision.put("Ipoc", d.Ipoconsumo);
                    jsonDetalleRemision.put("IvaDevolucion", d.IvaDevolucion);
                    array.put(jsonDetalleRemision);
                }

                jsonRemision.put("DetRem", array);
                NetWorkHelper r = new NetWorkHelper();
                respuesta = r.writeService(jsonRemision, SincroHelper.getRemision_EnviarURL());
                respuesta = SincroHelper.procesarRemisionJson(respuesta);
                if (respuesta.equals("OK")) {
                    // Aquí debo actualizar el estado de la remision
                    actualizarEstadoDescarga(remision.NumeroRemision);

                    if (remision.IsPedidoCallcenter) {

                        JSONObject jsonConfirmacion = new JSONObject();
                        jsonConfirmacion.put("IdClienteTiMovil", resolucionDTO.IdCliente);
                        jsonConfirmacion.put("CodigoRuta", resolucionDTO.CodigoRuta);
                        jsonConfirmacion.put("IdMotivoNegativo", 0);
                        jsonConfirmacion.put("Comentario", "");
                        jsonConfirmacion.put("IdCaso", remision.IdCaso);
                        jsonConfirmacion.put("EsFactura", false);
                        jsonConfirmacion.put("NumeroDocumento", remision.NumeroRemision);

                        String respuesta_confirmar_pedido = r.writeService(jsonConfirmacion, SincroHelper.CONFIRMAR_PEDIDO);
                        respuesta_confirmar_pedido = SincroHelper.procesarOkJson(respuesta_confirmar_pedido);

                        if (!respuesta_confirmar_pedido.equals("OK")) {
                            throw new Exception(respuesta_confirmar_pedido);
                        } else {
                            new PedidoCallcenterDAL(contexto).eliminarPedido(remision.IdPedido, remision.IdCaso);
                        }
                    }

                } else if (respuesta.equals(Utilities.IMEI_ERROR)) {
                    App.guardarConfiguracionEstadoAplicacion("B", contexto);
                }

            } else  // cuando esta pendiente la anulación
                if (remision.PendienteAnulacion) {

                    if (!Utilities.isNetworkReachable(contexto)
                            || !Utilities.isNetworkConnected(contexto)) {
                        throw new Exception(App.ERROR_CONECTIVIDAD);
                    }

                    String peticion = SincroHelper.getAnularRemisionURL(remision.NumeroRemision,
                            remision.ComentarioAnulacion, resolucionDTO.CodigoRuta,
                            resolucionDTO.IdCliente, installationId);
                    respuesta = new NetWorkHelper().readService(peticion);
                    respuesta = SincroHelper.procesarRemisionJson(respuesta);
                    if (respuesta.equalsIgnoreCase("OK")) {
                        actualizarEstadoAnulacion(remision.NumeroRemision);
                    } else if (respuesta.equals(Utilities.IMEI_ERROR)) {
                        App.guardarConfiguracionEstadoAplicacion("B", contexto);
                    }

                }
        }

        App.SincronizandoRemisionNumero.remove(remision.NumeroRemision);
        App.SincronizandoRemision = App.SincronizandoRemisionNumero.size() > 0;

        return respuesta;
    }

    private String actualizarEstadoDescarga(String numeroRemision)
            throws Exception {
        try {
            String update = "UPDATE "
                    + TablaRemision
                    + " SET Sincronizada=1, PendienteAnulacion=0 WHERE NumeroRemision='"
                    + numeroRemision
                    + "'";
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:"
                    + e.getMessage());
        }
    }

    private String actualizarEstadoAnulacion(String numeroRemision) throws Exception {
        try {
            String update = "UPDATE "
                    + TablaRemision
                    + " SET PendienteAnulacion=0 where NumeroRemision='"
                    + numeroRemision
                    + "'";
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de la anulación:" + e.getMessage());
        }
    }

    public String eliminar(RemisionDTO remisionDTO) throws Exception {
        try {
            String update = "DELETE FROM " + TablaRemision
                    + " WHERE NumeroRemision=" + "'" + remisionDTO.NumeroRemision + "'";
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error eliminando la remisión:" + e.getMessage());
        }
    }

    public String anular(RemisionDTO remisionDTO) throws Exception {
        try {
            String update = "UPDATE " + TablaRemision
                    + " SET Anulada=1, PendienteAnulacion=1, Subtotal = 0, "
                    + " Total = 0, Iva = 0, ComentarioAnulacion = '" + remisionDTO.ComentarioAnulacion.replace("\"", "")
                    + "' WHERE NumeroRemision=" + "'" + remisionDTO.NumeroRemision + "'";
            executeQuery(update);

            // Ahora me toca actualizar todos los productos
            DetalleRemisionDAL df = new DetalleRemisionDAL(contexto);
            ProductoDAL pDal = new ProductoDAL(contexto);
            ArrayList<DetalleRemisionDTO> detalle = df.ObtenerListado(remisionDTO.NumeroRemision);
            ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();

            if(resolucionDTO.ManejarInventarioRemisiones){
                for (DetalleRemisionDTO oDetalle : detalle) {
                    pDal.actualizarMovimientosProducto(
                            oDetalle.IdProducto,
                            -oDetalle.Cantidad,
                            resolucionDTO.DevolucionRestaInventario ? -oDetalle.Devolucion : 0,
                            resolucionDTO.RotacionRestaInventario ? -oDetalle.Rotacion : 0,
                            remisionDTO.CodigoBodega
                    );
                }
            }

            remisionDTO.PendienteAnulacion = true;
            remisionDTO.Anulada = true;
            remisionDTO.Total = 0;
            remisionDTO.Iva = 0;
            remisionDTO.Subtotal = 0;
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error anulando la remisión:" + e.getMessage());
        }
    }

    public String obtenerPrimeraYultimaRemision
            (DateTime fechaDesde, DateTime fechaHasta) {

        StringBuilder result = new StringBuilder();
        Cursor cursor = obtenerPorFecha();
        ArrayList<RemisionDTO> remisionesHoy = null;

        if (cursor != null && cursor.moveToFirst()) {
            remisionesHoy = new ArrayList<>();
            do {
                RemisionDTO remision = getFromCursor(cursor);
                DateTime fechaRemision = new DateTime(remision.Fecha);

                boolean sameDay =
                        (fechaRemision.toLocalDate().toString()
                                .equals(fechaDesde.toLocalDate().toString())
                        ) ||
                                (fechaRemision.toLocalDate().toString()
                                        .equals(fechaHasta.toLocalDate().toString())
                                );

                boolean between =
                        (fechaRemision.isAfter(fechaDesde)) &&
                                (fechaRemision.isBefore(fechaHasta));

                if (sameDay || between) {
                    remisionesHoy.add(remision);
                }

            } while (cursor.moveToNext());
        }

        Date date;
        if (remisionesHoy != null && remisionesHoy.size() > 0) {
            RemisionDTO primeraRem = remisionesHoy.get(0);
            date = new Date(primeraRem.Fecha);
            result.append(" PRIMERA REMISION: ")
                    .append(primeraRem.NumeroRemision).append("\r\n El ")
                    .append(Utilities.FechaDetallada(date))
                    .append("\r\n");
            RemisionDTO ultimaRem = remisionesHoy.get(remisionesHoy.size() - 1);
            date = new Date(ultimaRem.Fecha);
            result.append(" ULTIMA REMISION: ")
                    .append(ultimaRem.NumeroRemision).append("\r\n El ")
                    .append(Utilities.FechaDetallada(date));
        }

        return result.toString();
    }

}