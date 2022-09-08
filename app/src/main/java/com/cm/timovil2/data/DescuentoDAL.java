package com.cm.timovil2.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.DescuentoDTO;

public class DescuentoDAL extends DAL {

	public DescuentoDAL(Context context) {
		super(context, DAL.TablaDescuento);
	}

	public void Insertar(DescuentoDTO descuento) {
		ContentValues values = new ContentValues();
		values.put("IdCliente", descuento.IdCliente);
		values.put("IdProducto", descuento.IdProducto);
		values.put("Porcentaje", descuento.Porcentaje);
		super.insertar(values);
	}

	public int Eliminar() {
		return super.eliminar(null);
	}

	private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
		return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
	}

	public ArrayList<DescuentoDTO> ObtenerListado(int idCliente) {
		ArrayList<DescuentoDTO> lista = new ArrayList<>();
		Cursor cursor;
		cursor = this.Obtener(null, "IdCliente=?", new String[] {String.valueOf(idCliente)});
		if (cursor.moveToFirst()) {
			do {
				DescuentoDTO dto = new DescuentoDTO();
				dto.IdCliente = Integer.parseInt(cursor.getString(1));
				dto.IdProducto = Integer.parseInt(cursor.getString(2));
				dto.Porcentaje = cursor.getFloat(3);
				lista.add(dto);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return lista;
	}

	@Override
	void setColumns() {
		columnas = new String[]{ "_id", "IdCliente", "IdProducto", "Porcentaje" };
	}
}