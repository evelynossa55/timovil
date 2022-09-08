package com.cm.timovil2.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.dto.CuentaCajaDTO;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 20/02/17.
 */
public class CuentaCajaDAL extends DAL{

    public CuentaCajaDAL(ActivityBase context) {
        super(context, DAL.TablaCuentaCaja);
    }

    private void Insertar(CuentaCajaDTO dto) {
        ContentValues values = new ContentValues();
        values.put("IdCuentaCaja", dto.IdCuentaCaja);
        values.put("Nombre", dto.Nombre);
        values.put("NumeroCuenta", dto.NumeroCuenta);
        super.insertar(values);
    }

    public void Insertar(ArrayList<CuentaCajaDTO> cuentas){
        super.deleteAll();
        if(cuentas != null && cuentas.size() > 0){
            for (CuentaCajaDTO dto: cuentas){
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

    public ArrayList<CuentaCajaDTO> Obtener(){
        ArrayList<CuentaCajaDTO> lista = new ArrayList<>();
        Cursor cursor = Obtener(null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                CuentaCajaDTO e = getFromCursor(cursor);
                lista.add(e);
            } while (cursor.moveToNext());
        }
        if(cursor != null) {cursor.close();}
        return lista;
    }


    private CuentaCajaDTO getFromCursor(Cursor cursor) {
        CuentaCajaDTO e = new CuentaCajaDTO();
        e.IdCuentaCaja = Integer.parseInt(cursor.getString(0));
        e.Nombre = cursor.getString(1);
        e.NumeroCuenta = cursor.getString(2);
        return e;
    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdCuentaCaja", "Nombre", "NumeroCuenta"};
    }

}
