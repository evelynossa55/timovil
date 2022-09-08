package com.cm.timovil2.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.FormaPagoDTO;

public class FormaPagoDAL extends DAL {

	public FormaPagoDAL(Context context) {
		super(context, DAL.TablaFormaDePago);
	}

	public void Insertar(FormaPagoDTO formaPago) {
		ContentValues values = new ContentValues();
		values.put("Codigo", formaPago.Codigo);
		values.put("Nombre", formaPago.Nombre);
		super.insertar(values);
	}

	public int Eliminar() {
		return super.eliminar(null);
	}

	private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
		return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
	}

	public ArrayList<FormaPagoDTO> ObtenerListado() {
		ArrayList<FormaPagoDTO> lista = new ArrayList<>();
		Cursor cursor;
		cursor = this.Obtener("Codigo ASC", null, null);
		if (cursor.moveToFirst()) {
			do {
				FormaPagoDTO dto = new FormaPagoDTO();
				dto.Codigo = cursor.getString(1);
				dto.Nombre = cursor.getString(2);
				lista.add(dto);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return lista;
	}

	public FormaPagoDTO obtenerPorCodigo(String codigo) {
		Cursor cursor;
		String[] parametros = { codigo };
		cursor = this.Obtener(null, "Codigo=?", parametros);
		FormaPagoDTO fp = new FormaPagoDTO();
		if (cursor.moveToFirst()) {
			fp.Codigo = cursor.getString(1);
			fp.Nombre = cursor.getString(2);
		}
		cursor.close();
		return fp;
	}

	@Override
	void setColumns() {
		columnas = new String[] { "_id", "Codigo", "Nombre" };
	}
}