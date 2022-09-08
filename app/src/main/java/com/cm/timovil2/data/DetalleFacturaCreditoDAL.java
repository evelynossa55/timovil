package com.cm.timovil2.data;


import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.wsentities.MDetalleFactura;

public class DetalleFacturaCreditoDAL extends DAL {

	public DetalleFacturaCreditoDAL(Context context) {
		super(context, DAL.TablaDetalleFacturaCredito);
	}

	public long insertar(MDetalleFactura f) {

		ContentValues values = new ContentValues();

		values.put("IdFactura", f.IdFactura);
		values.put("IdProducto", f.IdProducto);
		values.put("Codigo", f.Codigo);
		values.put("Nombre", f.Nombre);
		values.put("Cantidad", f.Cantidad);
		values.put("Devolucion", f.Devolucion);
		values.put("Rotacion", f.Rotacion);
		values.put("Subtotal", f.Subtotal);
		values.put("Descuento", f.Descuento);
		values.put("Iva", f.Iva);
		values.put("Total", f.Total);

		return super.insertar(values);
	}

	public int Eliminar() {
		return super.eliminar(null);
	}

	private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
		return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
	}

	public ArrayList<MDetalleFactura> ObtenerListado(String idFactura) {
		ArrayList<MDetalleFactura> lista = new ArrayList<>();
		Cursor cursor;
		String[] parametros = { idFactura };
		cursor = this.Obtener(null, "IdFactura = ?", parametros);
		if (cursor.moveToFirst()) {
			do {
				lista.add(getFromCursor(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return lista;
	}

	private MDetalleFactura getFromCursor(Cursor cursor) {
		MDetalleFactura c = new MDetalleFactura();
		c._Id = Integer.parseInt(cursor.getString(0));

		c.IdFactura = cursor.getString(1);
		c.IdProducto = cursor.getInt(2);
		c.Codigo = cursor.getString(3);
		c.Nombre = cursor.getString(4);
		c.Cantidad = cursor.getInt(5);
		c.Devolucion = cursor.getInt(6);
		c.Rotacion = cursor.getInt(7);
		c.Subtotal = cursor.getFloat(8);
		c.Descuento = cursor.getFloat(9);
		c.Iva = cursor.getFloat(10);
		c.Total = cursor.getFloat(11);

		return c;
	}

	@Override
	void setColumns() {		
		columnas = new String[] { 
				"_id",
				"IdFactura",
				"IdProducto",
				"Codigo", 
				"Nombre",
				"Cantidad",
				"Devolucion",
				"Rotacion",
				"Subtotal",
				"Descuento", 
				"Iva",
				"Total",
				};
	}
}
