package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.MotivoNoVentaDTO;

import java.util.ArrayList;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 04/08/2015.
 */
public class MotivoNoVentaDAL extends DAL{

    Context context;
    public MotivoNoVentaDAL(Context context){
        super(context, TablaMotivoNoVenta);
    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdMotivo", "Descripcion"};
    }

    private long Insertar(MotivoNoVentaDTO motivoNoVentaDTO){
        ContentValues contentValues = new ContentValues();
        contentValues.put("IdMotivo", motivoNoVentaDTO.IdMotivo);
        contentValues.put("Descripcion", motivoNoVentaDTO.Descripcion);
        return insertar(contentValues);
    }

    public void Insertar(ArrayList<MotivoNoVentaDTO> motivos){
        for(MotivoNoVentaDTO m : motivos){
            Insertar(m);
        }
    }

    public void eliminarTodo(){
        deleteAll();
    }

    public MotivoNoVentaDTO getById(int IdMotivo){
        MotivoNoVentaDTO motivoNoVentaDTO = null;
        Cursor cursor = obtener(columnas, null,
                "IdMotivo = ?", new String[]{String.valueOf(IdMotivo)});
        if(cursor.moveToFirst()){
            motivoNoVentaDTO = getFromCursor(cursor);
        }
        return motivoNoVentaDTO;
    }

    public ArrayList<MotivoNoVentaDTO> getLista(){
        ArrayList<MotivoNoVentaDTO> lista = new ArrayList<>();
        Cursor cursor = obtener(columnas, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            do{
                lista.add(getFromCursor(cursor));
            }while (cursor.moveToNext());
        }
        return lista;
    }

    private MotivoNoVentaDTO getFromCursor(Cursor cursor){
        MotivoNoVentaDTO motivoNoVentaDTO = new MotivoNoVentaDTO();
        motivoNoVentaDTO.IdMotivo = cursor.getLong(0);
        motivoNoVentaDTO.Descripcion = cursor.getString(1);
        return motivoNoVentaDTO;
    }
}
