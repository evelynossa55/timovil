package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleResumenDiarioDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;

import org.joda.time.DateTime;

import java.util.ArrayList;

class DetalleFacturaDAL extends DAL {

    private final Context contexto;

    DetalleFacturaDAL(Context context) {
        super(context, DAL.TablaDetalleFactura);
        contexto = context;
    }

    public void insertar(DetalleFacturaDTO f, String codigoBodega) {

        ContentValues values = new ContentValues();

        values.put("NumeroFactura", f.NumeroFactura);
        values.put("IdProducto", f.IdProducto);
        values.put("Codigo", f.Codigo);
        values.put("Nombre", f.Nombre);
        values.put("Cantidad", f.Cantidad);
        values.put("Devolucion", f.Devolucion);
        values.put("Rotacion", f.Rotacion);
        values.put("ValorUnitario", f.ValorUnitario);
        values.put("Subtotal", f.Subtotal);
        values.put("Descuento", f.Descuento);
        if(f.DescuentoAdicional > 0){
            values.put("PorcentajeDescuento", f.DescuentoAdicional);
        }else {
            values.put("PorcentajeDescuento", f.PorcentajeDescuento);
        }
        values.put("Iva", f.Iva);
        values.put("PorcentajeIva", f.PorcentajeIva);
        values.put("Total", f.Total);
        values.put("IpoConsumo", f.ValorIpoConsumo);
        values.put("ValorDevolucion", f.ValorDevolucion);

        long id = super.insertar(values);
        ProductoDAL prod = new ProductoDAL(contexto);
        if (id >= 0) {
            //Actualizar el inventario
            ResolucionDTO resolucionDTO = new ResolucionDAL(contexto).ObtenerResolucion();
            if(resolucionDTO.ManejarInventario){
                prod.actualizarMovimientosProducto(
                        f.IdProducto,
                        f.Cantidad,
                        resolucionDTO.DevolucionRestaInventario ? f.Devolucion: 0,
                        resolucionDTO.RotacionRestaInventario ? f.Rotacion : 0,
                        codigoBodega);
            }
        }
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<DetalleFacturaDTO> ObtenerListado(String numeroFactura) {
        ArrayList<DetalleFacturaDTO> lista = new ArrayList<>();
        Cursor cursor;
        String[] parametros = {numeroFactura};
        cursor = this.Obtener(null, "NumeroFactura = ?", parametros);
        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }


    private DetalleFacturaDTO getFromCursor(Cursor cursor) {
        DetalleFacturaDTO c = new DetalleFacturaDTO();
        c._Id = Integer.parseInt(cursor.getString(0));

        c.NumeroFactura = cursor.getString(1);
        c.IdProducto = Integer.parseInt(cursor.getString(2));
        c.Codigo = cursor.getString(3);
        c.Nombre = cursor.getString(4);
        c.Cantidad = cursor.getInt(5);
        c.Devolucion = cursor.getInt(6);
        c.Rotacion = cursor.getInt(7);
        c.ValorUnitario = Float.parseFloat(cursor.getString(8));
        c.Subtotal = Float.parseFloat(cursor.getString(9));
        c.Descuento = Float.parseFloat(cursor.getString(10));
        c.PorcentajeDescuento = Float.parseFloat(cursor.getString(11));
        c.Iva = Float.parseFloat(cursor.getString(12));
        c.PorcentajeIva = Float.parseFloat(cursor.getString(13));
        c.Total = Float.parseFloat(cursor.getString(14));
        c.IpoConsumo = cursor.getFloat(15);
        c.ValorDevolucion = cursor.getFloat(16);
        return c;
    }

    @Override
    void setColumns() {
        columnas = new String[]{"_id", "NumeroFactura", "IdProducto",
                "Codigo", "Nombre", "Cantidad", "Devolucion", "Rotacion",
                "ValorUnitario", "Subtotal", "Descuento",
                "PorcentajeDescuento", "Iva", "PorcentajeIva", "Total",
                "IpoConsumo, ValorDevolucion"};
    }

    DetalleResumenDiarioDTO obtenerResumenProducto(ProductoDTO productoDTO, DateTime fechaDesde,
             DateTime fechaHasta)
    throws Exception{
        String select = "SELECT "
                + TablaDetalleFactura + ".Cantidad, "
                + TablaDetalleFactura + ".Devolucion, "
                + TablaDetalleFactura + ".Rotacion, "
                + TablaDetalleFactura + ".Total, "
                + TablaFactura + ".FormaPago, "
                + TablaFactura + ".FechaHora "
                + "FROM " + TablaFactura + ", " + TablaDetalleFactura
                + " WHERE " + TablaFactura + ".Anulada = 0 AND "
                + TablaFactura + ".NumeroFactura = " + TablaDetalleFactura + ".NumeroFactura AND "
                + TablaDetalleFactura + ".IdProducto = " + productoDTO.IdProducto + " AND "
                + TablaFactura + ".codigoBodega = '" + productoDTO.CodigoBodega + "'";

        Cursor cursor = obtener(select, null);
        return obtenerResumenDesdeCursor(cursor, productoDTO, fechaDesde, fechaHasta);
    }

    private  DetalleResumenDiarioDTO obtenerResumenDesdeCursor
            (Cursor cursor, ProductoDTO productoDTO,
             DateTime fechaDesde, DateTime fechaHasta)
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
                inv = resolucionDTO.ManejarInventario;
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

                dto.CodigoBodega = productoDTO.CodigoBodega;
                dto.Inventario = inv;
                dto.IdProducto = productoDTO.IdProducto;
                dto.Nombre = productoDTO.Nombre;
                dto.StockInicial = productoDTO.StockInicial;
                dto.StockActual -= dto.Cantidad;

                if (dr) {
                    dto.StockActual -= dto.Devolucion;
                }

                if (rr) {
                    dto.StockActual -= dto.Rotacion;
                }

            }

            return dto;

        }catch (Exception e){
            throw new Exception("Error generando el resumen: " + e.getMessage());
        }
    }
}
