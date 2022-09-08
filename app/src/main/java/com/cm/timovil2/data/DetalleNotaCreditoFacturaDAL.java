package com.cm.timovil2.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.DetalleResumenNotaCreditoFacturaPorDevolucionDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.front.ActivityBase;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/03/18.
 */

class DetalleNotaCreditoFacturaDAL extends DAL{

    ActivityBase context;

    DetalleNotaCreditoFacturaDAL(ActivityBase context){
        super(context, DAL.TablaDetalleNotaCreditoFactura);
        this.context = context;
    }

    public long insertar(DetalleNotaCreditoFacturaDTO nc) {

        ContentValues values = new ContentValues();
        values.put("NumeroDocumento", nc.NumeroDocumento);
        values.put("Cantidad", nc.Cantidad);
        values.put("IdProducto", nc.IdProducto);
        values.put("Subtotal", nc.Subtotal);
        values.put("Descuento", nc.Descuento);
        values.put("Ipoconsumo", nc.Ipoconsumo);
        values.put("Iva5", nc.Iva5);
        values.put("Iva19", nc.Iva19);
        values.put("Iva", nc.Iva);
        values.put("Valor", nc.Valor);
        values.put("Codigo", nc.Codigo);
        values.put("Nombre", nc.Nombre);

        return super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<DetalleNotaCreditoFacturaDTO> ObtenerListado(String numeroDocumento) {
        ArrayList<DetalleNotaCreditoFacturaDTO> lista = new ArrayList<>();
        Cursor cursor;
        String[] parametros = {numeroDocumento};
        cursor = this.Obtener(null, "NumeroDocumento = ?", parametros);
        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    DetalleResumenNotaCreditoFacturaPorDevolucionDTO obtenerResumenProducto
            (ProductoDTO productoDTO, DateTime fechaDesde, DateTime fechaHasta)
            throws Exception{
        String select = "SELECT "
                + TablaDetalleNotaCreditoFactura + ".Cantidad, "
                + TablaDetalleNotaCreditoFactura + ".Subtotal, "
                + TablaDetalleNotaCreditoFactura + ".Descuento, "
                + TablaDetalleNotaCreditoFactura + ".Iva5, "
                + TablaDetalleNotaCreditoFactura + ".Iva19, "
                + TablaDetalleNotaCreditoFactura + ".Iva, "
                + TablaDetalleNotaCreditoFactura + ".Valor, "
                + TablaNotaCreditoFactura + ".Fecha "
                + "FROM " + TablaNotaCreditoFactura + ", " + TablaDetalleNotaCreditoFactura
                + " WHERE "
                + TablaNotaCreditoFactura + ".NumeroDocumento = " + TablaDetalleNotaCreditoFactura + ".NumeroDocumento AND "
                + TablaDetalleNotaCreditoFactura + ".IdProducto = " + productoDTO.IdProducto + " AND "
                + TablaNotaCreditoFactura + ".CodigoBodega = '" + productoDTO.CodigoBodega + "' AND "
                + TablaNotaCreditoFactura + ".Anulada = 0";

        Cursor cursor = obtener(select, null);
        return obtenerResumenDesdeCursor(cursor, productoDTO, fechaDesde, fechaHasta);
    }

    private DetalleResumenNotaCreditoFacturaPorDevolucionDTO obtenerResumenDesdeCursor
            (Cursor cursor, ProductoDTO productoDTO,
             DateTime fechaDesde, DateTime fechaHasta)
            throws Exception{

        try{

            DetalleResumenNotaCreditoFacturaPorDevolucionDTO dto = new DetalleResumenNotaCreditoFacturaPorDevolucionDTO();
            //ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();

            //boolean inv = true;

            //if (resolucionDTO.EsDatoValido()) {
                //inv = resolucionDTO.ManejarInventario;
            //}

            if(cursor != null && cursor.moveToFirst()){

                do {

                    long _fechaResumen = cursor.getLong(7);
                    DateTime fechaResumen = new DateTime(_fechaResumen);

                    boolean sameDay =
                            (fechaResumen.toLocalDate().toString()
                                    .equals(fechaDesde.toLocalDate().toString())
                            ) ||
                                    (fechaResumen.toLocalDate().toString()
                                            .equals(fechaHasta.toLocalDate().toString())
                                    );

                    boolean between =
                            (fechaResumen.isAfter(fechaDesde)) &&
                                    (fechaResumen.isBefore(fechaHasta));

                    if (sameDay || between) {
                        dto.Cantidad = dto.Cantidad + cursor.getInt(0);
                        dto.Subtotal = dto.Subtotal + cursor.getDouble(1);
                        dto.Descuento = dto.Descuento + cursor.getDouble(2);
                        dto.Iva5 = dto.Iva5 + cursor.getDouble(3);
                        dto.Iva19 = dto.Iva19 + cursor.getDouble(4);
                        dto.Iva = dto.Iva + cursor.getDouble(5);
                        dto.Valor = dto.Valor + cursor.getDouble(6);
                    }

                } while (cursor.moveToNext());

                dto.CodigoBodega = productoDTO.CodigoBodega;
                //dto.Inventario = inv;
                dto.IdProducto = productoDTO.IdProducto;
                dto.Nombre = productoDTO.Nombre;
                dto.StockInicial = productoDTO.StockInicial;
                dto.StockActual -= dto.Cantidad;

            }

            return dto;

        }catch (Exception e){
            throw new Exception("Error generando el resumen: " + e.getMessage());
        }
    }

    private DetalleNotaCreditoFacturaDTO getFromCursor(Cursor cursor) {
        DetalleNotaCreditoFacturaDTO c = new DetalleNotaCreditoFacturaDTO();
        c.IdDetalleNotaCreditoFactura = cursor.getInt(0);
        c.NumeroDocumento = cursor.getString(1);
        c.IdProducto = cursor.getInt(2);
        c.Cantidad = cursor.getInt(3);
        c.Subtotal = cursor.getFloat(4);
        c.Descuento = cursor.getFloat(5);
        c.Ipoconsumo = cursor.getFloat(6);
        c.Iva5 = cursor.getFloat(7);
        c.Iva19 = cursor.getFloat(8);
        c.Iva = cursor.getFloat(9);
        c.Valor = cursor.getFloat(10);
        c.Codigo = cursor.getString(11);
        c.Nombre = cursor.getString(12);
        return c;
    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdDetalleNotaCreditoFactura",
                "NumeroDocumento",
                "IdProducto",
                "Cantidad",
                "Subtotal",
                "Descuento",
                "Ipoconsumo",
                "Iva5",
                "Iva19",
                "Iva",
                "Valor",
                "Codigo",
                "Nombre"};
    }

}
