package com.cm.timovil2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleResumenDiarioDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FacturaDAL extends DAL {

    private final ActivityBase contexto;
    private final StringBuilder detalleToPrint;
    private final StringBuilder detalleRemisionToPrint;
    private int cantdadRemisiones = 0;
    private int cantdadFacturasCreditos = 0;
    private int cantdadFacturasContado = 0;
    private int cantdadRemisionesCreditos = 0;
    private int cantdadRemisionesContado = 0;
    private ArrayList<ProductoDTO> productos;

    public FacturaDAL(ActivityBase context) {
        super(context, DAL.TablaFactura);
        contexto = context;
        detalleToPrint = new StringBuilder();
        detalleRemisionToPrint = new StringBuilder();
    }

    private void validarDatosInsercion(FacturaDTO f) throws Exception {

        if (f == null) {
            throw new Exception("La factura no ha sido inicializada.");
        }

        if (f.DetalleFactura == null || f.DetalleFactura.size() == 0) {
            throw new Exception("La factura debe contener al menos un producto.");
        }

        if (f.Total == 0
                && f.Devolucion == 0) {
            throw new Exception("El valor de la factura debe ser diferente a cero.");
        }
    }

    public void insertar(FacturaDTO f, boolean remision) throws Exception {

        validarDatosInsercion(f);
        ContentValues values = new ContentValues();
        values.put("NumeroFactura", f.NumeroFactura);
        values.put("FechaHora", f.FechaHora);
        values.put("FormaPago", f.FormaPago);
        values.put("IdCliente", f.IdCliente);
        values.put("Identificacion", f.Identificacion);
        values.put("RazonSocial", f.RazonSocial);
        values.put("Negocio", f.Negocio);
        values.put("Direccion", f.Direccion);
        values.put("Telefono", f.Telefono);
        values.put("Subtotal", f.Subtotal);
        values.put("Descuento", f.Descuento);
        values.put("Retefuente", f.Retefuente);
        values.put("PorcentajeRetefuente", f.PorcentajeRetefuente);
        values.put("Iva", f.Iva);
        values.put("Ica", f.Ica);
        values.put("Total", f.Total);
        values.put("Sincronizada", f.Sincronizada ? 1 : 0);
        values.put("Anulada", f.Anulada ? 1 : 0);
        values.put("PendienteAnulacion", f.PendienteAnulacion ? 1 : 0);
        values.put("Efectivo", f.EfectivoPagado);
        values.put("Devolucion", f.Devolucion);
        values.put("Latitud", f.Latitud);
        values.put("Longitud", f.Longitud);
        values.put("CREE", f.CREE);
        values.put("PorcentajeCREE", f.PorcentajeCREE);
        values.put("Comentario", f.Comentario);
        values.put("IpoConsumo", f.IpoConsumo);
        values.put("TipoDocumento", f.TipoDocumento);
        values.put("codigoBodega", f.CodigoBodega);
        values.put("NumeroPedido", f.NumeroPedido);
        values.put("ComentarioAnulacion", f.ComentarioAnulacion);
        values.put("IdEmpleadoEntregador", f.IdEmpleadoEntregador);
        values.put("IdResolucion", f.IdResolucion);
        values.put("Remision", remision ? 1 : 0);
        values.put("Revisada", f.Revisada ? 1 : 0);
        values.put("IsPedidoCallcenter", f.IsPedidoCallcenter ? 1 : 0);
        values.put("FechaHoraVencimiento", f.FechaHoraVencimiento);
        values.put("IdPedido", f.IdPedido);
        values.put("IdCaso", f.IdCaso);
        values.put("ValorDevolucion", f.ValorDevolucion);
        values.put("ReteIva", f.ReteIva);
        values.put("FacturaPos", f.FacturaPos ? 1 : 0);
        values.put("Cantidad", f.Cantidad);
        values.put("Rotacion", f.Rotacion);
        values.put("QRInputValue", f.QRInputValue);
        values.put("Cufe", f.Cufe);
        values.put("DistanciaDelNegocio", f.DistanciaCodBarras);
        values.put("CreadaConCodigoBarras", f.CreadaConCodigoBarras ? 1 : 0);

        super.insertar(values);
        if (remision) {
            new ResolucionDAL(contexto).IncrementarSiguienteRemision();
        } else {
            new ResolucionDAL(contexto).IncrementarSiguienteFactura(f.FacturaPos);
        }

        // Ahora, voy a guardar el detalle
        DetalleFacturaDAL detalleDal = new DetalleFacturaDAL(contexto);
        for (DetalleFacturaDTO detalle : f.DetalleFactura) {
            detalle.NumeroFactura = f.NumeroFactura;
            detalleDal.insertar(detalle, f.CodigoBodega);
        }
    }

    public int eliminar() {
        //Detalle
        new DetalleFacturaDAL(contexto).Eliminar();
        return super.eliminar(null);
    }

    private Cursor obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    private Cursor obtenerPorFecha() {
        String orderBy = "FechaHora ASC";
        return super.obtener(columnas, orderBy, null, null);
    }

    public FacturaDTO obtenerPorNumeroFac(String numeroFactura) {
        FacturaDTO facturaDTO = null;
        String orderBy = "FechaHora ASC";
        String filtro = "NumeroFactura = ?";
        String params[] = new String[]{numeroFactura};
        Cursor cursor = super.obtener(columnas, orderBy, filtro, params);
        if (cursor != null && cursor.moveToFirst()) {
            facturaDTO = getFromCursor(cursor);
            if (facturaDTO != null && !facturaDTO.NumeroFactura.equals("")) {
                facturaDTO.DetalleFactura = new DetalleFacturaDAL(contexto).ObtenerListado(facturaDTO.NumeroFactura);
            }
        }
        return facturaDTO;
    }

    public FacturaDTO obtenerPorID(String[] _Id) {
        FacturaDTO facturaDTO = null;
        String filtro = "_id=?";
        Cursor cursor = super.obtener(columnas, null, filtro, _Id);
        if (cursor != null && cursor.moveToFirst()) {
            facturaDTO = getFromCursor(cursor);
            if (facturaDTO != null && !facturaDTO.NumeroFactura.equals("")) {
                facturaDTO.DetalleFactura = new DetalleFacturaDAL(contexto).ObtenerListado(facturaDTO.NumeroFactura);
            }
        }
        return facturaDTO;
    }

    public synchronized FacturaDTO ObtenerPorIdCaso(int idCaso){
        FacturaDTO facturaDTO = null;
        String filtro = "IdCaso=?";
        Cursor cursor = super.obtener(columnas, null, filtro, new String[]{String.valueOf(idCaso)});
        if (cursor != null && cursor.moveToFirst()) {
            facturaDTO = getFromCursor(cursor);
        }
        return facturaDTO;
    }

    public String descargarFacturas() throws Exception {

        if (!Utilities.isNetworkReachable(contexto)
                || !Utilities.isNetworkConnected(contexto)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        try {

            StringBuilder sb = new StringBuilder();
            ArrayList<FacturaDTO> lista = obtenerListadoPendientes();
            int descargasExitosas = 0;
            int descargasErroneas = 0;
            StringBuilder sbExitosas = new StringBuilder();
            StringBuilder sbErroneas = new StringBuilder();

            for (FacturaDTO f : lista) {
                String respuesta;
                try {
                    f.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_SINCRO;
                    respuesta = sincronizarFactura(f);

                    if (respuesta.equals("Sincronizando")) {
                        respuesta = "La factura ya se estaba sincronizando, por favor intenta nuevamente";
                    }
                } catch (Exception e) {
                    App.SincronizandoFacturaNumero.remove(f.NumeroFactura);
                    App.SincronizandoFactura = App.SincronizandoFacturaNumero.size() > 0;
                    respuesta = e.toString();
                }

                if (respuesta.equals("OK")) {
                    descargasExitosas++;
                    sbExitosas.append(f.NumeroFactura).append(", ");
                } else {
                    descargasErroneas++;
                    sbErroneas.append(f.NumeroFactura).append(": ").append(respuesta).append("\n");
                }
            }

            sb.append(descargasExitosas).append(" facturas descargadas:\n")
                    .append(sbExitosas.toString()).append("\n");

            if (descargasErroneas > 0) {
                sb.append(descargasErroneas)
                        .append(" facturas no se pudieron descargar:\n")
                        .append(sbErroneas.toString()).append("\n");
            }

            return sb.toString();

        } catch (Exception e) {
            throw new Exception("Error descargando las facturas: " + e.getMessage());
        }
    }

    public int obtenerCantidadFacturasPendientes() {
        int cantidad = 0;
        String query = "SELECT COUNT(1) AS Cantidad " +
                "FROM Factura " +
                "WHERE Sincronizada = 0 OR PendienteAnulacion = 0 OR Revisada = 0";
        Cursor cursor;
        cursor = this.obtener(query);
        if (cursor != null && cursor.moveToFirst()) {
            cantidad = cursor.getInt(0);
        }

        try {
            if (cursor != null) cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cantidad;
    }

    public synchronized ArrayList<FacturaDTO> obtenerListadoPendientes() {
        ArrayList<FacturaDTO> lista = new ArrayList<>();
        String filtro = "Sincronizada = ? or PendienteAnulacion = ?";
        String[] parametros = {"0", "1"};

        Cursor cursor;
        cursor = this.obtener(null, filtro, parametros);
        DetalleFacturaDAL df = new DetalleFacturaDAL(contexto);
        if (cursor.moveToFirst()) {
            do {
                FacturaDTO f = getFromCursor(cursor);
                f.DetalleFactura = df.ObtenerListado(f.NumeroFactura);
                lista.add(f);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public synchronized ArrayList<FacturaDTO> obtenerListado(boolean cargarDetalle, DateTime fechaDesde, DateTime fechaHasta) throws Exception {

        ArrayList<FacturaDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.obtener("FechaHora DESC", null, null);
        if (cursor.isClosed()) return lista;

        DetalleFacturaDAL df = new DetalleFacturaDAL(contexto);

        if (cursor.moveToFirst()) {
            do {
                FacturaDTO f = getFromCursor(cursor);
                lista.add(f);

                long _fechaResumen = f.FechaHora;
                DateTime fechaResumen = new DateTime(_fechaResumen);

                boolean sameDay =
                        (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                boolean isBetween = (fechaResumen.isAfter(fechaDesde)
                        && fechaResumen.isBefore(fechaHasta));

                if (sameDay || isBetween) {

                    if (f.Anulada) {
                        App.ResumenFacturacion.anuladas += 1;
                        if (f.PendienteAnulacion || !f.Sincronizada) {
                            App.ResumenFacturacion.pendientesSincronizacion += 1;
                        }
                    } else {
                        App.ResumenFacturacion.subtotal += f.Subtotal;
                        App.ResumenFacturacion.descuento += f.Descuento;
                        App.ResumenFacturacion.iva += f.Iva;
                        App.ResumenFacturacion.retefuente += f.Retefuente;
                        App.ResumenFacturacion.reteiva += f.ReteIva;
                        App.ResumenFacturacion.total += f.Total;
                        App.ResumenFacturacion.ipoConsumo += f.IpoConsumo;
                        App.ResumenFacturacion.ValorDevolucion += f.ValorDevolucion;

                        if (f.FormaPago.equals("000")) {
                            App.ResumenFacturacion.contado += f.Total;
                        } else {
                            App.ResumenFacturacion.credito += f.Total;
                        }

                        if (f.PendienteAnulacion || !f.Sincronizada) {
                            App.ResumenFacturacion.pendientesSincronizacion += 1;
                        }
                    }
                }

                // COMPLETAR EL RESUMEN con débitos
                if (cargarDetalle) {
                    f.DetalleFactura = df.ObtenerListado(f.NumeroFactura);
                    if (!f.Anulada && (isBetween || sameDay)) {
                        for (DetalleFacturaDTO detalle : f.DetalleFactura) {
                            //cantidad
                            App.ResumenFacturacion.cantidad += detalle.Cantidad;
                            //devolucion
                            App.ResumenFacturacion.devoluciones += detalle.Devolucion;
                            //rotacion
                            App.ResumenFacturacion.rotaciones += detalle.Rotacion;
                        }
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
        //débito
        new AbonoFacturaDAL(this.contexto).contarAbonosPorRangoFechas(fechaDesde, fechaHasta);
        return lista;
    }

    public synchronized ArrayList<FacturaDTO> obtenerListadoFiltro(boolean cargarDetalle, DateTime fechaDesde, DateTime fechaHasta,String Parametro) throws Exception {

        ArrayList<FacturaDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.obtener("FechaHora DESC", null, null);
        if (cursor.isClosed()) return lista;

        DetalleFacturaDAL df = new DetalleFacturaDAL(contexto);

        if (cursor.moveToFirst()) {
            do {
                FacturaDTO f = getFromCursor(cursor);
                if(f.RazonSocial.toUpperCase().contains(Parametro.toUpperCase())||f.NumeroFactura.toUpperCase().contains(Parametro.toUpperCase())){

                lista.add(f);

                long _fechaResumen = f.FechaHora;
                DateTime fechaResumen = new DateTime(_fechaResumen);

                boolean sameDay =
                        (fechaResumen.toLocalDate().toString().equals(fechaDesde.toLocalDate().toString()))
                                || (fechaResumen.toLocalDate().toString().equals(fechaHasta.toLocalDate().toString()));

                boolean isBetween = (fechaResumen.isAfter(fechaDesde)
                        && fechaResumen.isBefore(fechaHasta));

                if (sameDay || isBetween) {

                    if (f.Anulada) {
                        App.ResumenFacturacion.anuladas += 1;
                        if (f.PendienteAnulacion || !f.Sincronizada) {
                            App.ResumenFacturacion.pendientesSincronizacion += 1;
                        }
                    } else {
                        App.ResumenFacturacion.subtotal += f.Subtotal;
                        App.ResumenFacturacion.descuento += f.Descuento;
                        App.ResumenFacturacion.iva += f.Iva;
                        App.ResumenFacturacion.retefuente += f.Retefuente;
                        App.ResumenFacturacion.reteiva += f.ReteIva;
                        App.ResumenFacturacion.total += f.Total;
                        App.ResumenFacturacion.ipoConsumo += f.IpoConsumo;
                        App.ResumenFacturacion.ValorDevolucion += f.ValorDevolucion;

                        if (f.FormaPago.equals("000")) {
                            App.ResumenFacturacion.contado += f.Total;
                        } else {
                            App.ResumenFacturacion.credito += f.Total;
                        }

                        if (f.PendienteAnulacion || !f.Sincronizada) {
                            App.ResumenFacturacion.pendientesSincronizacion += 1;
                        }
                    }
                }

                // COMPLETAR EL RESUMEN con débitos
                if (cargarDetalle) {
                    f.DetalleFactura = df.ObtenerListado(f.NumeroFactura);
                    if (!f.Anulada && (isBetween || sameDay)) {
                        for (DetalleFacturaDTO detalle : f.DetalleFactura) {
                            //cantidad
                            App.ResumenFacturacion.cantidad += detalle.Cantidad;
                            //devolucion
                            App.ResumenFacturacion.devoluciones += detalle.Devolucion;
                            //rotacion
                            App.ResumenFacturacion.rotaciones += detalle.Rotacion;
                        }
                    }
                }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        //débito
        new AbonoFacturaDAL(this.contexto).contarAbonosPorRangoFechas(fechaDesde, fechaHasta);
        return lista;
    }


    public String obtenerDetalleResumenFacturacion2(DateTime fechaDesde, DateTime fechaHasta) throws Exception {

        try {

            StringBuilder detalle = new StringBuilder();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat formatter2 = new DecimalFormat("$###,###.##");

            String linea = "\n-------------------------------\n";
            ResolucionDTO resolucion = new ResolucionDAL(contexto).ObtenerResolucion();
            DetalleFacturaDAL detalleFacturaDAL = new DetalleFacturaDAL(contexto);
            ProductoDAL productoDAL = new ProductoDAL(contexto);

            productos = productoDAL.obtenerListadoFacturado();
            ArrayList<String> productos_aux = new ArrayList<>();

            ArrayList<DetalleResumenDiarioDTO> detalleResumenProductos = new ArrayList<>();

            if (productos != null && productos.size() > 0) {
                for (ProductoDTO productoDTO : productos) {

                    String key = productoDTO.IdProducto + productoDTO.CodigoBodega;
                    if (!productos_aux.contains(key)) {
                        productos_aux.add(key);
                    } else {
                        continue;
                    }

                    DetalleResumenDiarioDTO resumenProducto = detalleFacturaDAL.
                            obtenerResumenProducto(productoDTO,
                                    fechaDesde, fechaHasta);

                    if (resumenProducto.Cantidad != 0
                            || resumenProducto.Devolucion != 0
                            || resumenProducto.Rotacion != 0) {
                        detalleResumenProductos.add(resumenProducto);
                    }
                }
            }

            if (detalleResumenProductos.size() > 0) {

                detalleToPrint.append(" Producto \r\n Cant  |  Devol  |  Rot  |  Val\r\n");

                for (DetalleResumenDiarioDTO dto : detalleResumenProductos) {
                    detalle.append(dto.Nombre)
                            .append("\nCantidad: ").append(dto.Cantidad);
                    detalle.append("\nCantidad Cred: ").append(dto.CantidadCredito)
                            .append("\nCantidad Cont: ").append(dto.CantidadContado);
                    detalle.append("\nDevoluciones: ").append(dto.Devolucion)
                            .append("\nRotaciones: ").append(dto.Rotacion)
                            .append((dto.Inventario ? "\nStock inicial: " + dto.StockInicial : ""))
                            .append("\nValor: ").append(formatter.format(dto.Total)).append(linea);

                    detalleToPrint.append(" ").append(dto.Nombre)
                            .append((dto.Inventario ? " [" + dto.StockInicial + "]" : ""))
                            .append("\r\n")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Cantidad), 5))
                            .append("|")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Devolucion), 5))
                            .append("|")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Rotacion), 5))
                            .append("|")
                            .append(formatter2.format(dto.Total)).append("\r\n");

                    if (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                            || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                            || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                            || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {

                        detalleToPrint
                                .append(" Cantidad Cred: ").append(dto.CantidadCredito)
                                .append("\r\n")
                                .append(" Cantidad Cont: ").append(dto.CantidadContado)
                                .append("\r\n");
                    }
                    cantdadFacturasCreditos += dto.CantidadCredito;
                    cantdadFacturasContado += dto.CantidadContado;
                }
            } else {
                return ("No se ha facturado ningún producto");
            }
            return detalle.toString();
        } catch (Exception e) {
            throw new Exception("Error generando el resumen: " + e.getMessage());
        }
    }

    public String obtenerDetalleResumenFacturacionToPrint() {
        return detalleToPrint.toString();
    }

    public int obtenerCantidadFacturasCredito() {
        return cantdadFacturasCreditos;
    }

    public int obtenerCantidadFacturasContado() {
        return cantdadFacturasContado;
    }

    public int obtenerCantidadRemisionesCredito() {
        return cantdadRemisionesCreditos;
    }

    public int obtenerCantidadRemisionesContado() {
        return cantdadRemisionesContado;
    }

    public int obtenerCantidadRemisiones() {
        return cantdadRemisiones;
    }

    public String obtenerDetalleResumenRemision2
            (DateTime fechaDesde, DateTime fechaHasta) throws Exception {
        try {

            StringBuilder detalle = new StringBuilder();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat formatter2 = new DecimalFormat("$###,###.##");

            String linea = "\r\n-------------------------------\r\n";

            DetalleRemisionDAL detalleRemisionDAL = new DetalleRemisionDAL(contexto);
            ProductoDAL productoDAL = new ProductoDAL(contexto);

            productos = productoDAL.obtenerListadoRemisionado();
            ArrayList<String> productos_aux = new ArrayList<>();

            ArrayList<DetalleResumenDiarioDTO> detalleResumenProductos = new ArrayList<>();
            if (productos != null && productos.size() > 0) {
                for (ProductoDTO productoDTO : productos) {

                    String key = productoDTO.IdProducto + productoDTO.CodigoBodega;
                    if (!productos_aux.contains(key)) {
                        productos_aux.add(key);
                    } else {
                        continue;
                    }

                    DetalleResumenDiarioDTO resumenProducto = detalleRemisionDAL.
                            obtenerResumenProducto(productoDTO, fechaDesde, fechaHasta);

                    if (resumenProducto.Cantidad != 0
                            || resumenProducto.Devolucion != 0
                            || resumenProducto.Rotacion != 0) {
                        detalleResumenProductos.add(resumenProducto);
                    }
                }
            }

            if (detalleResumenProductos.size() > 0) {

                detalleRemisionToPrint.append(" Producto \r\n Cant  |  Devol  |  Rot  |  Val\r\n");

                for (DetalleResumenDiarioDTO dto : detalleResumenProductos) {
                    detalle.append(dto.Nombre)
                            .append("\nCantidad: ").append(dto.Cantidad);
                    detalle.append("\nCantidad Cred: ").append(dto.CantidadCredito)
                            .append("\nCantidad Cont: ").append(dto.CantidadContado);
                    detalle.append("\nDevoluciones: ").append(dto.Devolucion)
                            .append("\nRotaciones: ").append(dto.Rotacion)
                            .append((dto.Inventario ? "\nStock inicial: " + dto.StockInicial : ""))
                            .append("\nValor: ").append(formatter.format(dto.Total))
                            .append(linea);

                    detalleRemisionToPrint.append(" ").append(dto.Nombre)
                            .append((dto.Inventario ? " [" + dto.StockInicial + "]" : ""))
                            .append("\r\n")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Cantidad), 5))
                            .append("|")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Devolucion), 5))
                            .append("|")
                            .append(Utilities.completarEspacios(String.valueOf(dto.Rotacion), 5))
                            .append("|")
                            .append(formatter2.format(dto.Total)).append("\r\n");

                    cantdadRemisiones += dto.Cantidad;
                    cantdadRemisionesCreditos += dto.CantidadCredito;
                    cantdadRemisionesContado += dto.CantidadContado;
                }
            } else {
                detalleRemisionToPrint.append("");
                return ("No se ha realizado ninguna remisión");
            }

            return detalle.toString();
        } catch (Exception e) {
            throw new Exception("Error generando el resumen: " + e.getMessage());
        }
    }

    public String obtenerDetalleResumenRemisionToPrint() {
        return detalleRemisionToPrint.toString();
    }

    public String obtenerPrimeraYultimaFactura
            (DateTime fechaDesde, DateTime fechaHasta) {

        StringBuilder result = new StringBuilder();
        Cursor cursor = obtenerPorFecha();
        ArrayList<FacturaDTO> facturasHoy = null;

        if (cursor != null && cursor.moveToFirst()) {
            facturasHoy = new ArrayList<>();
            do {
                FacturaDTO factura = getFromCursor(cursor);
                DateTime fechaFactura = new DateTime(factura.FechaHora);

                boolean sameDay =
                        (fechaFactura.toLocalDate().toString()
                                .equals(fechaDesde.toLocalDate().toString())
                        ) ||
                                (fechaFactura.toLocalDate().toString()
                                        .equals(fechaHasta.toLocalDate().toString())
                                );

                boolean between =
                        (fechaFactura.isAfter(fechaDesde)) &&
                                (fechaFactura.isBefore(fechaHasta));

                if (sameDay || between) {
                    facturasHoy.add(factura);
                }

            } while (cursor.moveToNext());
        }

        Date date;
        if (facturasHoy != null && facturasHoy.size() > 0) {
            FacturaDTO primeraFac = facturasHoy.get(0);
            date = new Date(primeraFac.FechaHora);
            result.append(" PRIMERA FACTURA: ")
                    .append(primeraFac.NumeroFactura).append("\r\n El ")
                    .append(Utilities.FechaDetallada(date))
                    .append("\r\n");
            FacturaDTO ultimaFac = facturasHoy.get(facturasHoy.size() - 1);
            date = new Date(ultimaFac.FechaHora);
            result.append(" ULTIMA FACTURA: ")
                    .append(ultimaFac.NumeroFactura).append("\r\n El ")
                    .append(Utilities.FechaDetallada(date));
        }

        return result.toString();
    }

    private FacturaDTO getFromCursor(Cursor cursor) {
        FacturaDTO c = new FacturaDTO();
        c._Id = Integer.parseInt(cursor.getString(0));
        c.NumeroFactura = cursor.getString(1);
        c.FechaHora = Long.parseLong(cursor.getString(2));
        c.FormaPago = cursor.getString(3);
        c.IdCliente = Integer.parseInt(cursor.getString(4));
        c.Identificacion = cursor.getString(5);
        c.RazonSocial = cursor.getString(6);
        c.Negocio = cursor.getString(7);
        c.Direccion = cursor.getString(8);
        c.Telefono = cursor.getString(9);
        c.Subtotal = Float.parseFloat(cursor.getString(10));
        c.Descuento = Float.parseFloat(cursor.getString(11));
        c.Retefuente = Float.parseFloat(cursor.getString(12));
        c.PorcentajeRetefuente = Float.parseFloat(cursor.getString(13));
        c.Iva = Float.parseFloat(cursor.getString(14));
        c.Ica = Float.parseFloat(cursor.getString(15));
        c.Total = Float.parseFloat(cursor.getString(16));
        c.Sincronizada = cursor.getInt(17) == 1;
        c.Anulada = cursor.getInt(18) == 1;
        c.PendienteAnulacion = cursor.getInt(19) == 1;
        c.EfectivoPagado = cursor.getFloat(20);
        c.Devolucion = cursor.getInt(21);
        c.Latitud = cursor.getString(22);
        c.Longitud = cursor.getString(23);
        c.CREE = cursor.getDouble(24);
        c.PorcentajeCREE = cursor.getDouble(25);
        c.Comentario = cursor.getString(26);
        c.IpoConsumo = cursor.getFloat(27);
        c.TipoDocumento = cursor.getString(28);
        c.CodigoBodega = cursor.getString(29);
        c.NumeroPedido = cursor.getString(30);
        c.ComentarioAnulacion = cursor.getString(31);
        c.IdEmpleadoEntregador = cursor.getInt(32);
        c.IdResolucion = cursor.getInt(33);
        c.Remision = cursor.getInt(34) == 1;
        c.Revisada = cursor.getInt(35) == 1;
        c.IsPedidoCallcenter = cursor.getInt(36) == 1;
        c.FechaHoraVencimiento = Long.parseLong(cursor.getString(37));
        c.IdPedido = cursor.getInt(38);
        c.IdCaso = cursor.getInt(39);
        c.ValorDevolucion = cursor.getFloat(40);
        c.ReteIva = cursor.getFloat(41);
        c.FacturaPos = cursor.getInt(42) == 1;
        c.Cantidad = cursor.getInt(43);
        c.Rotacion = cursor.getInt(44);
        c.QRInputValue = cursor.getString(45);
        c.Cufe = cursor.getString(46);
        c.DistanciaCodBarras = cursor.getString(47);
        c.CreadaConCodigoBarras = cursor.getInt(48) == 1;
        return c;
    }

    @Override
    void setColumns() {
        columnas = new String[]{"_id", "NumeroFactura", "FechaHora",
                "FormaPago", "IdCliente", "Identificacion", "RazonSocial",
                "Negocio", "Direccion", "Telefono", "Subtotal", "Descuento",
                "Retefuente", "PorcentajeRetefuente", "Iva", "Ica", "Total",
                "Sincronizada", "Anulada", "PendienteAnulacion",
                "Efectivo", "Devolucion", "Latitud", "Longitud",
                "CREE", "PorcentajeCREE", "Comentario", "IpoConsumo", "TipoDocumento",
                "codigoBodega", "NumeroPedido", "ComentarioAnulacion", "IdEmpleadoEntregador",
                "IdResolucion", "Remision", "Revisada", "IsPedidoCallcenter", "FechaHoraVencimiento",
                "IdPedido", "IdCaso", "ValorDevolucion", "ReteIva", "FacturaPos", "Cantidad",
                "Rotacion", "QRInputValue", "Cufe", "DistanciaDelNegocio", "CreadaConCodigoBarras"};
    }

    public synchronized String sincronizarFactura(FacturaDTO factura) throws Exception {

        if (!Utilities.isNetworkReachable(contexto) || !Utilities.isNetworkConnected(contexto)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        if (App.SincronizandoFactura && App.SincronizandoFacturaNumero.contains(factura.NumeroFactura)) {
            return "Sincronizando";
        }

        Log.i("sincronizarFactura", "Sincronizando " + factura.NumeroFactura);
        App.SincronizandoFactura = true;
        App.SincronizandoFacturaNumero.add(factura.NumeroFactura);

        ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();
        String respuesta = "";

        if (resolucionDTO != null) {
            String installationId = contexto.getInstallationId();

            if (!factura.Sincronizada) {

                JSONObject jsonFactura = new JSONObject();
                jsonFactura.put("Num", factura.NumeroFactura);
                jsonFactura.put("CRuta", resolucionDTO.CodigoRuta);
                jsonFactura.put("IdCli", factura.IdCliente);
                jsonFactura.put("PRet", factura.PorcentajeRetefuente);
                jsonFactura.put("PIca", 0);
                //jsonFactura.put("Fecha", Utilities.FechaHoraAnsi(new Date(factura.FechaHora)));
                jsonFactura.put("Fecha", Utilities.FechaHoraAnsiJoda(new DateTime(factura.FechaHora)));
                jsonFactura.put("FP", factura.FormaPago);
                jsonFactura.put("Anul", factura.Anulada);
                jsonFactura.put("IdRes", factura.IdResolucion);
                jsonFactura.put("Efect", factura.EfectivoPagado);
                jsonFactura.put("Devol", factura.Devolucion);
                jsonFactura.put("Latitud", factura.Latitud);
                jsonFactura.put("Longitud", factura.Longitud);
                jsonFactura.put("Comentario", factura.Comentario);
                jsonFactura.put("TipoDoc", factura.TipoDocumento);
                jsonFactura.put("CodigoBodega", factura.CodigoBodega);
                jsonFactura.put("NroPedido", factura.NumeroPedido);
                jsonFactura.put("ComentAnu", factura.ComentarioAnulacion);
                jsonFactura.put("IdEmpEntre", factura.IdEmpleadoEntregador);
                jsonFactura.put("CantDet", factura.DetalleFactura.size());
                jsonFactura.put("IdClieTim", resolucionDTO.IdCliente);
                jsonFactura.put("Imei", installationId);
                jsonFactura.put("Pos", factura.FacturaPos);
                jsonFactura.put("ValDev", factura.ValorDevolucion);
                jsonFactura.put("ReteIva", factura.ReteIva);
                jsonFactura.put("EnviadaDesde", factura.EnviadaDesde);
                jsonFactura.put("Cantidad", factura.Cantidad);
                jsonFactura.put("Rotacion", factura.Rotacion);
                jsonFactura.put("Cccb", factura.CreadaConCodigoBarras);
                //jsonFactura.put("QRInputValue", factura.QRInputValue);
                //jsonFactura.put("Cufe", factura.Cufe);
                //jsonFactura.put("DistanciaDelNegocio", factura.DistanciaCodBarras);

                if (factura.IdPedido > 0) {
                    jsonFactura.put("Pedido", factura.IdPedido);
                }

                JSONArray array = new JSONArray();
                for (DetalleFacturaDTO oDetalle : factura.DetalleFactura) {
                    JSONObject jsonDetalleFactura = new JSONObject();
                    jsonDetalleFactura.put("IdProd", oDetalle.IdProducto);
                    jsonDetalleFactura.put("Cant", oDetalle.Cantidad);
                    jsonDetalleFactura.put("Dev", oDetalle.Devolucion);
                    jsonDetalleFactura.put("Rot", oDetalle.Rotacion);
                    jsonDetalleFactura.put("VUnit", oDetalle.ValorUnitario);
                    jsonDetalleFactura.put("PDesc", oDetalle.PorcentajeDescuento);
                    jsonDetalleFactura.put("PIva", oDetalle.PorcentajeIva);
                    jsonDetalleFactura.put("PDescA", oDetalle.DescuentoAdicional);
                    jsonDetalleFactura.put("ValDev", oDetalle.ValorDevolucion);
                    array.put(jsonDetalleFactura);
                }

                jsonFactura.put("Detalle", array);
                NetWorkHelper r = new NetWorkHelper();

                Log.d("JSON:", jsonFactura.toString());

                respuesta = r.writeService(jsonFactura, SincroHelper.getFactura_EnviarURL());
                Log.d("ERROR JSON", respuesta);
                respuesta = SincroHelper.procesarRemisionJson(respuesta);
                switch (respuesta) {
                    case "OK":
                        // Aquí debo actualizar el estado de la factura
                        actualizarEstadoDescarga(factura.NumeroFactura, true, false);

                        if (factura.IsPedidoCallcenter) {

                            JSONObject jsonConfirmacion = new JSONObject();
                            jsonConfirmacion.put("IdClienteTiMovil", resolucionDTO.IdCliente);
                            jsonConfirmacion.put("CodigoRuta", resolucionDTO.CodigoRuta);
                            jsonConfirmacion.put("IdMotivoNegativo", 0);
                            jsonConfirmacion.put("Comentario", "");
                            jsonConfirmacion.put("IdCaso", factura.IdCaso);
                            jsonConfirmacion.put("EsFactura", true);
                            jsonConfirmacion.put("NumeroDocumento", factura.NumeroFactura);

                            String respuesta_confirmar_pedido = r.writeService(jsonConfirmacion, SincroHelper.CONFIRMAR_PEDIDO);
                            respuesta_confirmar_pedido = SincroHelper.procesarOkJson(respuesta_confirmar_pedido);

                            if (!respuesta_confirmar_pedido.equals("OK")) {
                                throw new Exception(respuesta_confirmar_pedido);
                            } else {
                                new PedidoCallcenterDAL(contexto).eliminarPedido(factura.IdPedido, factura.IdCaso);
                            }
                        }
                        break;
                    case Utilities.IMEI_ERROR:
                        App.guardarConfiguracionEstadoAplicacion("B", contexto);
                        break;
                    default:
                        respuesta = "Respuesta inválida desde el servidor, por favor intente nuevamente";
                }

            } else  // cuando esta pendiente la anulación
                if (factura.PendienteAnulacion) {

                    if (!Utilities.isNetworkReachable(contexto) ||
                            !Utilities.isNetworkConnected(contexto)) {
                        throw new Exception(App.ERROR_CONECTIVIDAD);
                    }

                    NetWorkHelper netWorkHelper = new NetWorkHelper();
                    String url = SincroHelper.getAnularFacturaURL(factura.NumeroFactura,
                                    factura.ComentarioAnulacion,
                                    resolucionDTO.CodigoRuta,
                                    resolucionDTO.IdCliente, installationId);

                    respuesta = netWorkHelper.readService(url);
                    respuesta = SincroHelper.procesarOkJson(respuesta);

                    if (respuesta.equalsIgnoreCase("OK")) {
                        actualizarEstadoAnulacion(factura.NumeroFactura);
                    } else if (respuesta.equals(Utilities.IMEI_ERROR)) {
                        App.guardarConfiguracionEstadoAplicacion("B", contexto);
                    }

                }
        }

        App.SincronizandoFacturaNumero.remove(factura.NumeroFactura);
        App.SincronizandoFactura = App.SincronizandoFacturaNumero.size() > 0;
        return respuesta;
    }

    public String sincronizarDetalleFactura(FacturaDTO factura) throws Exception {
        ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();
        String respuesta = "";
        if (resolucionDTO != null) {

            if (!Utilities.isNetworkReachable(contexto) || !Utilities.isNetworkConnected(contexto)) {
                throw new Exception(App.ERROR_CONECTIVIDAD);
            }

            JSONObject jsonFactura = new JSONObject();
            jsonFactura.put("Num", factura.NumeroFactura);
            jsonFactura.put("CRuta", resolucionDTO.CodigoRuta);
            jsonFactura.put("IdCli", factura.IdCliente);
            jsonFactura.put("PRet", factura.PorcentajeRetefuente);
            jsonFactura.put("PIca", 0);
            jsonFactura.put("Fecha", Utilities.FechaHoraAnsi(new Date(factura.FechaHora)));
            jsonFactura.put("FP", factura.FormaPago);
            jsonFactura.put("Anul", factura.Anulada);
            jsonFactura.put("IdRes", factura.IdResolucion);
            jsonFactura.put("Efect", factura.EfectivoPagado);
            jsonFactura.put("Devol", factura.Devolucion);
            jsonFactura.put("Latitud", factura.Latitud);
            jsonFactura.put("Longitud", factura.Longitud);
            jsonFactura.put("Comentario", factura.Comentario);
            jsonFactura.put("TipoDoc", factura.TipoDocumento);
            jsonFactura.put("CodigoBodega", factura.CodigoBodega);
            jsonFactura.put("NroPedido", factura.NumeroPedido);
            jsonFactura.put("ComentAnu", factura.ComentarioAnulacion);
            jsonFactura.put("IdEmpEntre", factura.IdEmpleadoEntregador);
            jsonFactura.put("CantDet", factura.DetalleFactura.size());
            jsonFactura.put("IdClieTim", resolucionDTO.IdCliente);
            jsonFactura.put("ValDev", factura.ValorDevolucion);
            jsonFactura.put("ReteIva", factura.ReteIva);

            JSONArray array = new JSONArray();
            for (DetalleFacturaDTO oDetalle : factura.DetalleFactura) {
                JSONObject jsonDetalleFactura = new JSONObject();
                jsonDetalleFactura.put("IdProd", oDetalle.IdProducto);
                jsonDetalleFactura.put("Cant", oDetalle.Cantidad);
                jsonDetalleFactura.put("Dev", oDetalle.Devolucion);
                jsonDetalleFactura.put("Rot", oDetalle.Rotacion);
                jsonDetalleFactura.put("VUnit", oDetalle.ValorUnitario);
                jsonDetalleFactura.put("PDesc", oDetalle.PorcentajeDescuento);
                jsonDetalleFactura.put("PIva", oDetalle.PorcentajeIva);
                jsonDetalleFactura.put("PDescA", oDetalle.DescuentoAdicional);
                jsonDetalleFactura.put("ValDev", oDetalle.ValorDevolucion);
                array.put(jsonDetalleFactura);
            }

            jsonFactura.put("Detalle", array);
            NetWorkHelper r = new NetWorkHelper();
            respuesta = r.writeService(jsonFactura, SincroHelper.getFactura_EnviarDetalleURL());
            respuesta = SincroHelper.procesarRemisionJson(respuesta);
        }
        return respuesta;
    }

    public void actualizarEstadoDescarga(String numeroFactura, boolean sincronizada,
                                           boolean pendienteAnulacion) throws Exception {
        try {
            String update = "UPDATE "
                    + TablaFactura
                    + " SET Sincronizada=" + (sincronizada ? "1" : "0")
                    + ", PendienteAnulacion=" + (pendienteAnulacion ? "1" : "0")
                    + " WHERE NumeroFactura='" + numeroFactura + "'";
            executeQuery(update);
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:"
                    + e.getMessage());
        }
    }

    private void actualizarEstadoAnulacion(String numeroFactura) throws Exception {
        try {
            String update = "update "
                    + TablaFactura
                    + " set PendienteAnulacion=0 where NumeroFactura='"
                    + numeroFactura
                    + "'";
            executeQuery(update);
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de la anulación:" + e.getMessage());
        }
    }

    public String desanularFactura(String numeroFactura) throws Exception {
        try {

            Cursor cursor = obtener(columnas, null, "NumeroFactura=?", new String[]{numeroFactura});

            FacturaDTO facturaDTO = null;
            if (cursor.moveToFirst()) {
                facturaDTO = getFromCursor(cursor);
            }

            if (facturaDTO != null) {

                DetalleFacturaDAL df = new DetalleFacturaDAL(contexto);
                facturaDTO.DetalleFactura = df.ObtenerListado(numeroFactura);

                float sumSubtotal = 0;
                float sumDescuento = 0;
                float sumIva = 0;
                float sumIpoConsumo = 0;

                for (DetalleFacturaDTO detalle : facturaDTO.DetalleFactura) {
                    sumSubtotal += detalle.Subtotal;
                    sumDescuento *= detalle.Descuento;
                    sumIva += detalle.Iva;
                    sumIpoConsumo += detalle.IpoConsumo;
                }

                float Retefuente;
                if (facturaDTO.PorcentajeRetefuente > 0) {
                    Retefuente = (sumSubtotal - sumDescuento)
                            * (facturaDTO.PorcentajeRetefuente / 100);
                } else {
                    Retefuente = 0;
                }

                float Total = sumSubtotal
                        - sumDescuento
                        - Retefuente
                        - facturaDTO.ReteIva
                        + sumIva
                        + sumIpoConsumo;

                String update = "update "
                        + TablaFactura
                        + " set Anulada=0, PendienteAnulacion=0, "
                        + " Total = " + Total + ", Descuento = " + sumDescuento + ", Iva = " + sumIva
                        + ", Retefuente = " + Retefuente + ", Subtotal = " + sumSubtotal + ", "
                        + " IpoConsumo = " + sumIpoConsumo + ", ComentarioAnulacion = '"
                        + facturaDTO.ComentarioAnulacion.replace("\"", "")
                        + "' where NumeroFactura='" + facturaDTO.NumeroFactura + "'";

                executeQuery(update);
                return "OK";

            } else {
                return "La factura no existe";
            }

        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de la anulación:" + e.getMessage());
        }
    }

    public String anular(FacturaDTO facturaDTO) throws Exception {
        try {
            String update = "update "
                    + TablaFactura
                    + " set Anulada=1, Subtotal = 0, Total = 0,"
                    + " Descuento = 0, Iva = 0, Retefuente = 0, ReteIva = 0, "
                    + " ValorDevolucion = 0, ComentarioAnulacion = '" + facturaDTO.ComentarioAnulacion.replace("\"", "")
                    + "' where NumeroFactura='" + facturaDTO.NumeroFactura + "'";

            executeQuery(update);

            String updateDetalle = "update "
                    + TablaDetalleFactura
                    + " set Subtotal = 0, Total = 0, Descuento = 0, "
                    + " Iva = 0, IpoConsumo = 0, ValorDevolucion = 0"
                    + " where NumeroFactura='" + facturaDTO.NumeroFactura + "'";

            executeQuery(updateDetalle);

            // Ahora me toca actualizar todos los productos
            DetalleFacturaDAL df = new DetalleFacturaDAL(contexto);
            ProductoDAL pDal = new ProductoDAL(contexto);
            ArrayList<DetalleFacturaDTO> detalle = df.ObtenerListado(facturaDTO.NumeroFactura);
            ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();

            if(resolucionDTO.ManejarInventario){
                for (DetalleFacturaDTO oDetalle : detalle) {
                    pDal.actualizarMovimientosProducto(
                            oDetalle.IdProducto,
                            -oDetalle.Cantidad,
                            resolucionDTO.DevolucionRestaInventario ? -oDetalle.Devolucion : 0,
                            resolucionDTO.RotacionRestaInventario ? -oDetalle.Rotacion : 0,
                            facturaDTO.CodigoBodega);
                }
            }

            facturaDTO.PendienteAnulacion = true;
            facturaDTO.Anulada = true;
            facturaDTO.Subtotal = 0;
            facturaDTO.Total = 0;
            facturaDTO.Descuento = 0;
            facturaDTO.ValorDevolucion = 0;
            facturaDTO.Iva = 0;
            facturaDTO.Retefuente = 0;
            facturaDTO.IpoConsumo = 0;

            return "OK";
        } catch (Exception e) {
            throw new Exception("Error anulando la factura:" + e.getMessage());
        }
    }

    public void revisar(FacturaDTO facturaDTO) throws Exception {
        try {

            String update = "update "
                    + TablaFactura
                    + " set Revisada = 1"
                    + " where NumeroFactura='" + facturaDTO.NumeroFactura + "'";
            executeQuery(update);
            facturaDTO.Revisada = true;

        } catch (Exception e) {
            throw new Exception("Error revisando la factura:" + e.getMessage());
        }
    }

    public String obtenerNumeroFacturaFR(String numeroFactura){
        String[] numeroFacturaArray = numeroFactura.split("-");
        String numeroFacturaResult = numeroFactura;
        switch (ActivityBase.resolucion.CodigoRuta){
            case "01":
                numeroFacturaResult = "A7" + "-" + numeroFacturaArray[1];
                break;
            case "02":
                numeroFacturaResult = "A4" + "-" + numeroFacturaArray[1];
                break;
            case "05":
                numeroFacturaResult = "A6" + "-" + numeroFacturaArray[1];
                break;
        }

        return numeroFacturaResult;
    }
}