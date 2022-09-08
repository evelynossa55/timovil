package com.cm.timovil2.data;

import java.util.ArrayList;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DescuentoDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MDetalleListaPrecios;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProductoDAL extends DAL{

    //private final ISincroWeb llamador = null;
    private Context contexto;
    private ResolucionDTO resolucionDTO;

    public ProductoDAL(Context context) {
        super(context, DAL.TablaProducto);
        this.contexto = context;
        try {
            resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Insertar(ProductoDTO producto) {
        ContentValues values = new ContentValues();
        values.put("_id", producto.IdProducto);
        values.put("Codigo", producto.Codigo);
        values.put("Nombre", producto.Nombre);
        values.put("Precio1", producto.Precio1);
        values.put("Precio2", producto.Precio2);
        values.put("Precio3", producto.Precio3);
        values.put("PorcentajeIva", producto.PorcentajeIva);
        values.put("StockInicial", producto.StockInicial);
        values.put("Ventas", producto.Ventas);
        values.put("Devoluciones", producto.Devoluciones);
        values.put("Rotaciones", producto.Rotaciones);
        values.put("Precio4", producto.Precio4);
        values.put("Precio5", producto.Precio5);
        values.put("Precio6", producto.Precio6);
        values.put("Precio7", producto.Precio7);
        values.put("Precio8", producto.Precio8);
        values.put("Precio9", producto.Precio9);
        values.put("Precio10", producto.Precio10);
        values.put("Precio11", producto.Precio11);
        values.put("Precio12", producto.Precio12);
        values.put("IpoConsumo", producto.IpoConsumo);
        values.put("CodigoBodega", producto.CodigoBodega);

        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener() {
        return super.obtener(columnas, "Codigo ASC", null, null);
    }

    private ProductoDTO getFromCursor(Cursor cursor) {
        ProductoDTO prod = new ProductoDTO();
        prod.IdProducto = Integer.parseInt(cursor.getString(0));
        prod.Codigo = cursor.getString(1);
        prod.Nombre = cursor.getString(2);
        prod.Precio1 = Float.parseFloat(cursor.getString(3));
        prod.Precio2 = Float.parseFloat(cursor.getString(4));
        prod.Precio3 = Float.parseFloat(cursor.getString(5));
        prod.PorcentajeIva = Float.parseFloat(cursor.getString(6));
        prod.StockInicial = Integer.parseInt(cursor.getString(7));
        prod.Ventas = Integer.parseInt(cursor.getString(8));
        prod.Devoluciones = Integer.parseInt(cursor.getString(9));
        prod.Rotaciones = Integer.parseInt(cursor.getString(10));
        prod.Precio4 = Float.parseFloat(cursor.getString(11));
        prod.Precio5 = Float.parseFloat(cursor.getString(12));
        prod.Precio6 = Float.parseFloat(cursor.getString(13));
        prod.Precio7 = Float.parseFloat(cursor.getString(14));
        prod.Precio8 = Float.parseFloat(cursor.getString(15));
        prod.Precio9 = Float.parseFloat(cursor.getString(16));
        prod.Precio10 = Float.parseFloat(cursor.getString(17));
        prod.Precio11 = Float.parseFloat(cursor.getString(18));
        prod.Precio12 = Float.parseFloat(cursor.getString(19));
        prod.IpoConsumo = Float.parseFloat(cursor.getString(20));
        prod.CodigoBodega = cursor.getString(21);
        return prod;
    }

    private ArrayList<ProductoDTO> ObtenerListado(String codigoBodega) {
        ArrayList<ProductoDTO> lista = new ArrayList<>();
        String params[] = new String[]{codigoBodega};
        Cursor cursor = super.obtener(columnas, null, "CodigoBodega = ?", params);
        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return lista;
    }

    public ArrayList<ProductoDTO> ObtenerListado() {
        ArrayList<ProductoDTO> lista = new ArrayList<>();

        Cursor cursor = super.obtener(columnas, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return lista;
    }

    public ArrayList<ProductoDTO> obtenerListadoCompleto() {
        ArrayList<ProductoDTO> lista = new ArrayList<>();
        Cursor cursor = this.Obtener();
        if (cursor.moveToFirst()) {
            do {
                ProductoDTO p = getFromCursor(cursor);
                lista.add(p);
            } while (cursor.moveToNext());
        }
        return lista;
    }

    ArrayList<ProductoDTO> obtenerListadoEnNotaCreditoPorDevolucion() {
        ArrayList<ProductoDTO> lista = new ArrayList<>();
        String update = "SELECT "
                +TablaProducto+"._id, " + TablaProducto + ".Codigo, "+ TablaProducto+ ".Nombre, "
                +TablaProducto+".Precio1, "+TablaProducto+".Precio2, "+TablaProducto+ ".Precio3, "
                +TablaProducto+".PorcentajeIva, "+TablaProducto+ ".StockInicial, "
                +TablaProducto+".Ventas, "+TablaProducto+".Devoluciones, "
                +TablaProducto+".Rotaciones, "+TablaProducto+".Precio4, "+TablaProducto+".Precio5, "
                +TablaProducto+".Precio6, "+TablaProducto+ ".Precio7, "+TablaProducto+".Precio8, "
                +TablaProducto+".Precio9, "+TablaProducto+".Precio10, "+TablaProducto+".Precio11, "
                +TablaProducto+".Precio12, "+TablaProducto+".IpoConsumo, "+TablaProducto+".CodigoBodega, "
                +TablaDetalleNotaCreditoFactura+".IdProducto "
                + "FROM " + TablaProducto
                + ", " + TablaDetalleNotaCreditoFactura
                + " WHERE "
                + TablaProducto + "._id = "
                + TablaDetalleNotaCreditoFactura + ".IdProducto";

        Cursor c = obtener(update, null);
        if (c.moveToFirst()) {
            do {
                ProductoDTO p = getFromCursor(c);
                lista.add(p);
            } while (c.moveToNext());
        }
        return lista;
    }

    ArrayList<ProductoDTO> obtenerListadoRemisionado() {
        ArrayList<ProductoDTO> lista = new ArrayList<>();
        String update = "SELECT "
                +TablaProducto+"._id, " + TablaProducto + ".Codigo, "+ TablaProducto+ ".Nombre, "
                +TablaProducto+".Precio1, "+TablaProducto+".Precio2, "+TablaProducto+ ".Precio3, "
                +TablaProducto+".PorcentajeIva, "+TablaProducto+ ".StockInicial, "
                +TablaProducto+".Ventas, "+TablaProducto+".Devoluciones, "
                +TablaProducto+".Rotaciones, "+TablaProducto+".Precio4, "+TablaProducto+".Precio5, "
                +TablaProducto+".Precio6, "+TablaProducto+ ".Precio7, "+TablaProducto+".Precio8, "
                +TablaProducto+".Precio9, "+TablaProducto+".Precio10, "+TablaProducto+".Precio11, "
                +TablaProducto+".Precio12, "+TablaProducto+".IpoConsumo, "+TablaProducto+".CodigoBodega, "
                +TablaDetalleRemision+".IdProducto "
                + "FROM " + TablaProducto
                + ", " + TablaDetalleRemision
                + " WHERE "
                + TablaProducto + "._id = "
                + TablaDetalleRemision + ".IdProducto";

        Cursor c = obtener(update, null);
        if (c.moveToFirst()) {
            do {
                ProductoDTO p = getFromCursor(c);
                lista.add(p);
            } while (c.moveToNext());
        }
        return lista;
    }

     ArrayList<ProductoDTO> obtenerListadoFacturado() {
        ArrayList<ProductoDTO> lista = new ArrayList<>();
        String update = "SELECT "
                +TablaProducto+"._id, " + TablaProducto + ".Codigo, "+ TablaProducto+ ".Nombre, "
                +TablaProducto+".Precio1, "+TablaProducto+".Precio2, "+TablaProducto+ ".Precio3, "
                +TablaProducto+".PorcentajeIva, "+TablaProducto+ ".StockInicial, "
                +TablaProducto+".Ventas, "+TablaProducto+".Devoluciones, "
                +TablaProducto+".Rotaciones, "+TablaProducto+".Precio4, "+TablaProducto+".Precio5, "
                +TablaProducto+".Precio6, "+TablaProducto+ ".Precio7, "+TablaProducto+".Precio8, "
                +TablaProducto+".Precio9, "+TablaProducto+".Precio10, "+TablaProducto+".Precio11, "
                +TablaProducto+".Precio12, "+TablaProducto+".IpoConsumo, "+TablaProducto+".CodigoBodega, "
                +TablaDetalleFactura+".IdProducto "
                + "FROM " + TablaProducto
                + ", " + TablaDetalleFactura
                + " WHERE "
                + TablaProducto + "._id = "
                + TablaDetalleFactura + ".IdProducto";

        Cursor c = obtener(update, null);
        if (c.moveToFirst()) {
            do {
                ProductoDTO p = getFromCursor(c);
                lista.add(p);
            } while (c.moveToNext());
        }
        return lista;
    }

    public ArrayList<ProductoDTO> obtenerProductosPorListaPrecios(int idListaPrecios, ArrayList<ProductoDTO> listaProductos){
        ArrayList<ProductoDTO> productos = new ArrayList<>();

        ArrayList<MDetalleListaPrecios> listaPrecios = new DetalleListaPreciosDAL(contexto)
                .ObtenerListado(idListaPrecios);


        for(ProductoDTO p:listaProductos){
            for(MDetalleListaPrecios m:listaPrecios){
                if(p.IdProducto == m.IdProducto){
                    //&&  !idsProducto.contains(p.IdProducto)){
                    p.Precio1 = (float)m.Precio;
                    //idsProducto.add(p.IdProducto);
                    productos.add(p);
                }
            }
        }

        return productos;

    }

    public ArrayList<MDetalleListaPrecios> obtenerListasPrecios(){
        return new DetalleListaPreciosDAL(contexto).ObtenerListado();
    }

    /**
     * Obtiene el detalle inicial usado para realizar una factura. Obtiene el
     * listado de productos que tienen inventario.
     * @param cliente El cliente para buscar los descuentos asignados.
     * @return ArrayList<DetalleFacturaDTO>
     */
    public ArrayList<DetalleFacturaDTO> obtenerListadoInicialFacturacion(
            ClienteDTO cliente, String codigoBodega) throws Exception {

        ArrayList<MDetalleListaPrecios> listaPrecios = new DetalleListaPreciosDAL(contexto)
                .ObtenerListado(cliente.IdListaPrecios);

        if(listaPrecios == null || listaPrecios.size()<=0){
            throw new Exception("El cliente no tiene asociada una lista de precios");
        }

        ArrayList<ProductoDTO> listaProductos = ObtenerListado(codigoBodega);
        ArrayList<DetalleFacturaDTO> detalle = new ArrayList<>();

        ArrayList<DescuentoDTO> descuentos;

        if(cliente.IdCliente != -1){
            descuentos = new DescuentoDAL(this.contexto)
                    .ObtenerListado(cliente.IdCliente);
        }else{
            descuentos = new ArrayList<>();
        }

        boolean manejarInventario = cliente.Remision ? resolucionDTO.ManejarInventarioRemisiones : resolucionDTO.ManejarInventario;
        boolean permitirFacturarSinInventario =
                App.obtenerConfiguracion_PermitirFacturarSinInventario(this.contexto);

        for (ProductoDTO p : listaProductos) {

            int stock = p.getStock(resolucionDTO);
            if (resolucionDTO.IdCliente.equals(Utilities.ID_VEGA) || resolucionDTO.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
                if (manejarInventario && stock < 0 && !permitirFacturarSinInventario) {
                    continue;
                }
            } else {
                if (manejarInventario && stock <= 0 && !permitirFacturarSinInventario) {
                    continue;
                }
            }

            DetalleFacturaDTO d = new DetalleFacturaDTO();
            d.IdProducto = p.IdProducto;
            d.Codigo = p.Codigo;
            d.Nombre = p.Nombre;

            ArrayList<Float> precios = getValorUnitario(listaPrecios, p);
             if (precios == null || precios.size() <= 0){
               continue;
             }

            d.ValorUnitario = precios.get(0);
            d.PorcentajeIva = p.PorcentajeIva;
            d.StockDisponible = stock;
            d.PorcentajeDescuento = 0;
            d.Precio1 = precios.get(0);
            d.Precio2 = precios.size() >= 2 ? precios.get(1):p.Precio2;
            d.Precio3 = precios.size() >= 3 ? precios.get(2):p.Precio3;
            d.Precio4 = precios.size() >= 4 ? precios.get(3):p.Precio4;
            d.Precio5 = precios.size() >= 5 ? precios.get(4):p.Precio5;
            d.Precio6 = precios.size() >= 6 ? precios.get(5):p.Precio6;
            d.Precio7 = precios.size() >= 7 ? precios.get(6):p.Precio7;
            d.Precio8 = precios.size() >= 8 ? precios.get(7):p.Precio8;
            d.Precio9 = precios.size() >= 9 ? precios.get(8):p.Precio9;
            d.Precio10 = precios.size() >= 10 ? precios.get(9):p.Precio10;
            d.Precio11 = precios.size() >= 11 ? precios.get(10):p.Precio11;
            d.Precio12 = precios.size() >= 12 ? precios.get(12):p.Precio12;
            d.IpoConsumo = p.IpoConsumo;

            for (DescuentoDTO descuento : descuentos) {
                if (descuento.IdProducto == p.IdProducto) {
                    d.PorcentajeDescuento = descuento.Porcentaje;
                }
            }

            detalle.add(d);
        }

        if(detalle.size()<=0){
            throw new Exception("No hay productos disponibles para atender el cliente");
        }

        return detalle;
    }

    private ArrayList<Float> getValorUnitario(ArrayList<MDetalleListaPrecios> listaPrecios, ProductoDTO producto){
        ArrayList<Float> valores = new ArrayList<>();
        for(MDetalleListaPrecios m:listaPrecios){
            if(m.IdProducto == producto.IdProducto){
                if(!valores.contains((float)m.Precio))
                    valores.add((float)m.Precio);
            }
        }
        return valores;
    }

    /*
    public float getValorUnitario(int listaPrecio, ProductoDTO producto) {
        switch (listaPrecio) {
            case 1:
                return producto.Precio1;
            case 2:
                return producto.Precio2;
            case 3:
                return producto.Precio3;
            case 4:
                return producto.Precio4;
            case 5:
                return producto.Precio5;
            case 6:
                return producto.Precio6;
            case 7:
                return producto.Precio7;
            case 8:
                return producto.Precio8;
            case 9:
                return producto.Precio9;
            case 10:
                return producto.Precio10;
            case 11:
                return producto.Precio11;
            case 12:
                return producto.Precio12;
        }
        return producto.Precio1;
    }

    boolean EsProductoValido(int listaPrecio, ProductoDTO producto) {
        boolean respuesta = false;
        switch (listaPrecio) {
            case 0://El precio 0, le da acceso a toda la lista de precios.
                respuesta = true;
                break;
            case 1:
                respuesta = producto.Precio1 > 0;
                break;
            case 2:
                respuesta = producto.Precio2 > 0;
                break;
            case 3:
                respuesta = producto.Precio3 > 0;
                break;
            case 4:
                respuesta = producto.Precio4 > 0;
                break;
            case 5:
                respuesta = producto.Precio5 > 0;
                break;
            case 6:
                respuesta = producto.Precio6 > 0;
                break;
            case 7:
                respuesta = producto.Precio7 > 0;
                break;
            case 8:
                respuesta = producto.Precio8 > 0;
                break;
            case 9:
                respuesta = producto.Precio9 > 0;
                break;
            case 10:
                respuesta = producto.Precio10 > 0;
                break;
            case 11:
                respuesta = producto.Precio11 > 0;
                break;
            case 12:
                respuesta = producto.Precio12 > 0;
                break;
        }
        return respuesta;
    }
*/


	/*public void actualizarInventario(ArrayList<InventarioDTO> inventario) {
        SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			// 1ro: Actualizo el inventario:
			db.execSQL("UPDATE " + TablaProducto + " SET StockInicial = 0, Ventas = 0, Devoluciones = 0, Rotaciones = 0");
			// 2do: Recorro la lista y actualizo el inventario de cada producto
			String cadena;
			for (InventarioDTO dto : inventario) {
				db.execSQL("UPDATE " + TablaProducto + " SET StockInicial = "
						+ String.valueOf(dto.Cantidad) + " WHERE _id = "
						+ String.valueOf(dto.IdProducto));
			}
			db.close();
		} catch (Exception e) {
			try {
				if (db != null && db.isOpen()) {
					db.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}*/

    void actualizarMovimientosProducto(int idProducto, int cantidad,
                                              int devolucion, int rotacion,
                                              String codigoBodega) {
        SQLiteDatabase db = null;
        try {
            Cursor c = obtenerPorId(idProducto);
            if (c.moveToFirst()) {
                ProductoDTO p = getFromCursor(c);
                c.close();
                p.Ventas += cantidad;
                p.Devoluciones += devolucion;
                p.Rotaciones += rotacion;
                db = this.getWritableDatabase();
                if (db != null) {
                    db.execSQL("UPDATE " + TablaProducto + " SET "
                            + "Ventas = " + String.valueOf(p.Ventas)
                            + ", Devoluciones = " + String.valueOf(p.Devoluciones)
                            + ", Rotaciones = " + String.valueOf(p.Rotaciones)
                            + " WHERE _id = " + String.valueOf(idProducto)
                            + " AND CodigoBodega = '" + codigoBodega +"'");
                    db.close();
                }
            }
        } catch (Exception e) {
            try {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void actualizarInventario(int idProducto, int stock, String codigoBodega) {
        SQLiteDatabase db = null;
        try {
            Cursor c = obtenerPorId(idProducto);
            if (c.moveToFirst()) {
                ProductoDTO p = getFromCursor(c);
                db = this.getWritableDatabase();
                if (db != null) {
                    db.execSQL("UPDATE " + TablaProducto + " SET "
                            + ", StockInicial = " + String.valueOf(stock)
                            + " WHERE _id = " + String.valueOf(p.IdProducto)
                            + " AND CodigoBodega = '" + codigoBodega +"'");
                    db.close();
                }
            }
        } catch (Exception e) {
            try {
                if (db != null && db.isOpen()) {
                    db.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public ArrayList<DetalleFacturaDTO> filtrarProductos(
            ArrayList<DetalleFacturaDTO> listaOriginal, String filtro) {
        if (filtro != null && filtro.trim().length() > 0) {
            filtro = filtro.toUpperCase();
            ArrayList<DetalleFacturaDTO> nuevaLista = new ArrayList<>();
            for (DetalleFacturaDTO dto : listaOriginal) {
                if (dto.Codigo.toUpperCase().contains(filtro)
                        || dto.Nombre.toUpperCase().contains(filtro)) {
                    nuevaLista.add(dto);
                }
            }
            return nuevaLista;
        } else {
            return listaOriginal;
        }
    }

    public ArrayList<ProductoDTO> filtrarProductos2(
            ArrayList<ProductoDTO> listaOriginal, String filtro) {
        if (filtro != null && filtro.trim().length() > 0) {
            filtro = filtro.toUpperCase();
            ArrayList<ProductoDTO> nuevaLista = new ArrayList<>();
                for (ProductoDTO dto : listaOriginal) {
                    if ((dto.Codigo.toUpperCase().contains(filtro)
                            || dto.CodigoBodega.toUpperCase().contains(filtro)
                            || dto.Nombre.toUpperCase().contains(filtro))) {
                        nuevaLista.add(dto);
                    }
                }
            return nuevaLista;
        } else {
            return listaOriginal;
        }
    }

    @Override
    void setColumns() {
        columnas = new String[]{"_id", "Codigo", "Nombre", "Precio1",
                "Precio2", "Precio3", "PorcentajeIva", "StockInicial",
                "Ventas", "Devoluciones", "Rotaciones",
                "Precio4", "Precio5", "Precio6", "Precio7", "Precio8", "Precio9",
                "Precio10", "Precio11", "Precio12", "IpoConsumo", "CodigoBodega"
        };
    }
}