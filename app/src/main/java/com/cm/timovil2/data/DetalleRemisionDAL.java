package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.DetalleResumenDiarioDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class DetalleRemisionDAL extends DAL{

    private final Context contexto;

    DetalleRemisionDAL(Context context) {
        super(context, DAL.TablaDetalleRemision);
        contexto = context;
    }

    public void insertar(DetalleRemisionDTO f, String codigoBodega) {

        ContentValues values = new ContentValues();
        values.put("NumeroRemision", f.NumeroRemision);
        values.put("IdProducto", f.IdProducto);
        values.put("NombreProducto", f.NombreProducto);
        values.put("Cantidad", f.Cantidad);
        values.put("ValorUnitario", f.ValorUnitario);
        values.put("Subtotal", f.Subtotal);
        values.put("Total", f.Total);
        values.put("Iva", f.Iva);
        values.put("FechaCreacion", f.FechaCreacion);
        values.put("PorcentajeIva", f.PorcentajeIva);
        values.put("Codigo", f.Codigo);
        values.put("Descuento", f.Descuento);
        values.put("PorcentajeDescuento", f.PorcentajeDescuento);
        values.put("Devolucion", f.Devolucion);
        values.put("Rotacion", f.Rotacion);
        values.put("ValorDevolucion", f.ValorDevolucion);
        values.put("Ipoconsumo", f.Ipoconsumo);
        values.put("IvaDevolucion", f.IvaDevolucion);

        long id = super.insertar(values);
        ProductoDAL prod = new ProductoDAL(contexto);
        if (id >= 0) {
            //Actualizar el inventario
            try{
                ResolucionDTO resolucion =  new ResolucionDAL(contexto).ObtenerResolucion();
                if(resolucion.ManejarInventarioRemisiones){
                    prod.actualizarMovimientosProducto(
                            f.IdProducto,
                            f.Cantidad,
                            resolucion.DevolucionRestaInventario ? f.Devolucion : 0,
                            resolucion.RotacionRestaInventario ? f.Rotacion : 0,
                            codigoBodega);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<DetalleRemisionDTO> ObtenerListado(String numeroRemision) {
        ArrayList<DetalleRemisionDTO> lista = new ArrayList<>();
        Cursor cursor;
        String[] parametros = {numeroRemision};
        cursor = this.Obtener(null, "NumeroRemision = ?", parametros);
        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    private DetalleRemisionDTO getFromCursor(Cursor cursor) {
        DetalleRemisionDTO c = new DetalleRemisionDTO();
        c.IdDetalle = Integer.parseInt(cursor.getString(0));
        c.NumeroRemision = cursor.getString(1);
        c.IdProducto = Integer.parseInt(cursor.getString(2));
        c.NombreProducto = cursor.getString(3);
        c.Cantidad = Integer.parseInt(cursor.getString(4));
        c.ValorUnitario = cursor.getDouble(5);
        c.Subtotal = cursor.getDouble(6);
        c.Total = cursor.getDouble(7);
        c.Iva = Float.parseFloat(cursor.getString(8));
        c.FechaCreacion = Long.parseLong(cursor.getString(9));
        c.PorcentajeIva = Float.parseFloat(cursor.getString(10));
        c.Codigo = cursor.getString(11);
        c.Descuento = cursor.getFloat(12);
        c.PorcentajeDescuento = cursor.getFloat(13);
        c.Devolucion = cursor.getInt(14);
        c.Rotacion = cursor.getInt(15);
        c.ValorDevolucion = cursor.getFloat(16);
        c.Ipoconsumo = cursor.getFloat(17);
        c.IvaDevolucion = cursor.getFloat(18);
        return c;
    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdDetalle", "NumeroRemision", "IdProducto",
                "NombreProducto", "Cantidad", "ValorUnitario", "Subtotal", "Total",
                "Iva", "FechaCreacion", "PorcentajeIva", "Codigo", "Descuento",
                "PorcentajeDescuento", "Devolucion", "Rotacion", "ValorDevolucion",
                "Ipoconsumo", "IvaDevolucion"};
    }

    DetalleResumenDiarioDTO obtenerResumenProducto
            (ProductoDTO productoDTO, DateTime fechaDesde, DateTime fechaHasta)
            throws Exception{
        String select = "SELECT "
                + TablaDetalleRemision + ".Cantidad, "
                + TablaRemision + ".Devolucion, "
                + TablaRemision + ".Rotacion, "
                + TablaDetalleRemision + ".Total, "
                + TablaRemision + ".FormaPago, "
                + TablaRemision + ".Fecha "
                + "FROM " + TablaRemision + ", " + TablaDetalleRemision
                + " WHERE " + TablaRemision + ".Anulada = 0 AND "
                + TablaRemision + ".NumeroRemision = " + TablaDetalleRemision + ".NumeroRemision AND "
                + TablaDetalleRemision + ".IdProducto = " + productoDTO.IdProducto + " AND "
                + TablaRemision + ".codigoBodega = '" + productoDTO.CodigoBodega + "'";


        Cursor cursor = obtener(select, null);

        return obtenerResumenDesdeCursor(cursor, productoDTO, fechaDesde, fechaHasta);
    }

    private DetalleResumenDiarioDTO obtenerResumenDesdeCursor
            (Cursor cursor, ProductoDTO productoDTO, DateTime fechaDesde, DateTime fechaHasta)

            throws Exception{

        try{
            DetalleResumenDiarioDTO dto = new DetalleResumenDiarioDTO();
            ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();
            boolean dr = true;
            boolean rr = true;
            boolean inv = true;

            if (resolucionDTO.EsDatoValido()) {
                dr = resolucionDTO.DevolucionRestaInventario;
                rr = resolucionDTO.RotacionRestaInventario;
                inv = resolucionDTO.ManejarInventarioRemisiones;
            }

            if(cursor != null && cursor.moveToFirst()){

                do {

                    long _fechaResumen = cursor.getLong(5);
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
                        dto.Devolucion = dto.Devolucion + cursor.getInt(1);
                        dto.Rotacion = dto.Rotacion + cursor.getInt(2);
                        dto.Total = dto.Total + cursor.getDouble(3);
                        dto.FormaPago = cursor.getString(4);

                        if (dto.FormaPago.equals("000")) {
                            dto.CantidadContado = dto.CantidadContado + cursor.getInt(0);
                        } else {
                            dto.CantidadCredito = dto.CantidadCredito + cursor.getInt(0);
                        }
                    }
                } while (cursor.moveToNext());
            }

            dto.Inventario = inv;
            dto.IdProducto = productoDTO.IdProducto;
            dto.StockInicial = productoDTO.StockInicial;
            dto.Nombre = productoDTO.Nombre;
            dto.CodigoBodega = productoDTO.CodigoBodega;
            dto.StockActual -= dto.Cantidad;

            if (dr) {
                dto.StockActual -= dto.Devolucion;
            }

            if (rr) {
                dto.StockActual -= dto.Rotacion;
            }

            return dto;

        }catch (Exception e){
            throw new Exception("Error generando el resumen: " + e.getMessage());
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }
}
