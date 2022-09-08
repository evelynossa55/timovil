package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cm.timovil2.dto.wsentities.MDetalleListaPrecios;
import java.util.ArrayList;

public class DetalleListaPreciosDAL extends DAL{


    public DetalleListaPreciosDAL(Context context) {
        super(context, DAL.TablaDetalleListaPrecios);
    }

    private void Insertar(MDetalleListaPrecios detalle) {
        ContentValues values = new ContentValues();
        values.put("IdDetalleListaPrecios", detalle.IdDetalleListaPrecios);
        values.put("IdListaPrecios", detalle.IdListaPrecios);
        values.put("IdProducto", detalle.IdProducto);
        values.put("Precio", detalle.Precio);
        values.put("NombreListaPrecios", detalle.NombreListaPrecios);
        super.insertar(values);
    }

    public void Insert(ArrayList<MDetalleListaPrecios> lista) throws Exception{
        if(lista != null && lista.size() > 0){
            for(MDetalleListaPrecios m: lista){
                Insertar(m);
            }
        }else{
            throw new Exception("No se ha especificado la lista de precios a ingresar o ésta se encuentra vacía");
        }
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<MDetalleListaPrecios> ObtenerListado() {
        ArrayList<MDetalleListaPrecios> lista = new ArrayList<>();
        Cursor cursor = this.Obtener(null, null, null);
        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ArrayList<MDetalleListaPrecios> ObtenerListado(int IdListaPrecios) {
        ArrayList<MDetalleListaPrecios> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        if(IdListaPrecios != -1){
            cursor = db.rawQuery("SELECT * FROM " + DAL.TablaDetalleListaPrecios
                    + " WHERE IdListaPrecios = ?", new String[]{String.valueOf(IdListaPrecios)});
        }else{
            cursor = db.rawQuery("SELECT * FROM " + DAL.TablaDetalleListaPrecios
                    + " ORDER BY IdProducto", null);
        }

        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    private MDetalleListaPrecios getFromCursor(Cursor cursor) {
        MDetalleListaPrecios c = new MDetalleListaPrecios();
        c.IdDetalleListaPrecios = cursor.getInt(0);
        c.IdListaPrecios = cursor.getInt(1);
        c.IdProducto = cursor.getInt(2);
        c.Precio = cursor.getInt(3);
        c.NombreListaPrecios = cursor.getString(4);
        return c;
    }

    @Override
    void setColumns() {
        columnas = new String[] { "IdDetalleListaPrecios", "IdListaPrecios", "IdProducto", "Precio", "NombreListaPrecios"};
    }

}
