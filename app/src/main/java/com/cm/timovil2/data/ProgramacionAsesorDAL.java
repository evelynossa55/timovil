package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.AsesorProgramacionDetalleDTO;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 28/06/17.
 */
public class ProgramacionAsesorDAL extends DAL{

    Context context;
    public ProgramacionAsesorDAL(Context context){
        super(context, TablaProgramacionAsesor);
    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdAsesorProgramacionDetalle",
                "IdAsesorProgramacion", "Fecha", "Dia"};
    }

    private long Insertar(AsesorProgramacionDetalleDTO asesorProgramacionDetalleDTO){
        ContentValues contentValues = new ContentValues();
        contentValues.put("IdAsesorProgramacionDetalle", asesorProgramacionDetalleDTO.IdAsesorProgramacionDetalle);
        contentValues.put("IdAsesorProgramacion", asesorProgramacionDetalleDTO.IdAsesorProgramacion);
        contentValues.put("Fecha", asesorProgramacionDetalleDTO.Fecha);
        contentValues.put("IdCliente", asesorProgramacionDetalleDTO.IdCliente);
        contentValues.put("Dia", asesorProgramacionDetalleDTO.Dia);
        return insertar(contentValues);
    }

    public void Insertar(ArrayList<AsesorProgramacionDetalleDTO> progs){
        for(AsesorProgramacionDetalleDTO m : progs){
            Insertar(m);
        }
    }

    public void eliminarTodo(){
        deleteAll();
    }

    public AsesorProgramacionDetalleDTO getByIdCliente(int IdCliente){
        AsesorProgramacionDetalleDTO programacionDetalleDTO = null;
        Cursor cursor = obtener(columnas, null,
                "IdCliente = ?", new String[]{String.valueOf(IdCliente)});
        if(cursor.moveToFirst()){
            programacionDetalleDTO = getFromCursor(cursor);
        }
        return programacionDetalleDTO;
    }

    public ArrayList<AsesorProgramacionDetalleDTO> getLista(){
        ArrayList<AsesorProgramacionDetalleDTO> lista = new ArrayList<>();
        Cursor cursor = obtener(columnas, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            do{
                lista.add(getFromCursor(cursor));
            }while (cursor.moveToNext());
        }
        return lista;
    }

    private AsesorProgramacionDetalleDTO getFromCursor(Cursor cursor){
        AsesorProgramacionDetalleDTO asesorProgramacionDetalleDTO = new AsesorProgramacionDetalleDTO();
        asesorProgramacionDetalleDTO.IdAsesorProgramacionDetalle = cursor.getInt(0);
        asesorProgramacionDetalleDTO.IdAsesorProgramacion = cursor.getInt(1);
        asesorProgramacionDetalleDTO.IdCliente = cursor.getInt(2);
        asesorProgramacionDetalleDTO.Fecha = cursor.getString(3);
        asesorProgramacionDetalleDTO.Dia = cursor.getInt(4);
        return asesorProgramacionDetalleDTO;
    }
}