package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.ResultadoGestionCasoCallcenterDTO;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 27/01/17.
 */
public class ResultadoGestionCasoCallcenterDAL extends DAL {


    public ResultadoGestionCasoCallcenterDAL(Context context) {
        super(context, DAL.TablaResultadoGestionCasoCallcenter);
    }

    public void Insertar(ResultadoGestionCasoCallcenterDTO resultado) {

        ContentValues values = new ContentValues();
        values.put("IdResultadoGestion", resultado.IdResultadoGestion);
        values.put("Nombre", resultado.Nombre);

        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<ResultadoGestionCasoCallcenterDTO> ObtenerListado() {
        ArrayList<ResultadoGestionCasoCallcenterDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener("IdResultadoGestion ASC", null, null);
        if (cursor.moveToFirst()) {
            do {
                ResultadoGestionCasoCallcenterDTO dto = new ResultadoGestionCasoCallcenterDTO();
                dto.IdResultadoGestion = cursor.getInt(0);
                dto.Nombre = cursor.getString(1);
                lista.add(dto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ResultadoGestionCasoCallcenterDTO Obtener(int idResultadoGestion) {
        ResultadoGestionCasoCallcenterDTO dto = null;
        Cursor cursor;
        String filtro = "IdResultadoGestion = ?";
        cursor = this.Obtener("IdResultadoGestion ASC", filtro, new String[]{String.valueOf(idResultadoGestion)});
        if (cursor.moveToFirst()) {
            dto = new ResultadoGestionCasoCallcenterDTO();
            dto.IdResultadoGestion = cursor.getInt(0);
            dto.Nombre = cursor.getString(1);
        }
        cursor.close();
        return dto;
    }

    @Override
    void setColumns() {
        columnas = new String[] { "IdResultadoGestion", "Nombre" };
    }

}
