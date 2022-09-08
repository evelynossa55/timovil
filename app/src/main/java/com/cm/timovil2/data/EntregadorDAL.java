package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.EntregadorDTO;

import java.util.ArrayList;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 29/04/2015.
 */
public class EntregadorDAL extends DAL{

    public EntregadorDAL(Context context) {
        super(context, DAL.TablaEntregador);
    }

    private void Insertar(EntregadorDTO dto) {
        ContentValues values = new ContentValues();
        values.put("IdEmpleado", dto.IdEmpleado);
        values.put("NombreCompleto", dto.NombreCompleto);
        super.insertar(values);
    }

    public void Insertar(ArrayList<EntregadorDTO> entregadores){
        if(entregadores != null && entregadores.size() > 0){
            for (EntregadorDTO dto: entregadores){
                Insertar(dto);
            }
        }
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<EntregadorDTO> Obtener(){
        ArrayList<EntregadorDTO> lista = new ArrayList<>();
        Cursor cursor = Obtener(null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                EntregadorDTO e = getFromCursor(cursor);
                lista.add(e);
            } while (cursor.moveToNext());
        }
        if(cursor != null) {cursor.close();}
        return lista;
    }

    public EntregadorDTO obtenerPorIdEmpleado(String[] Id) {
        EntregadorDTO entregadorDTO = null;
        String filtro = "IdEmpleado=?";
        Cursor cursor = super.obtener(columnas, null, filtro, Id);
        if (cursor != null && cursor.moveToFirst()) {
            entregadorDTO = getFromCursor(cursor);
        }
        return entregadorDTO;
    }

    private EntregadorDTO getFromCursor(Cursor cursor) {
        EntregadorDTO e = new EntregadorDTO();
        e.IdEntregador = Integer.parseInt(cursor.getString(0));
        e.IdEmpleado = Integer.parseInt(cursor.getString(1));
        e.NombreCompleto = cursor.getString(2);
        return e;
    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdEntregador", "IdEmpleado", "NombreCompleto"};
    }
}
