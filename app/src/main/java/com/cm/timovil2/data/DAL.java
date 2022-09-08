package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

abstract class DAL extends SQLiteOpenHelper {

    private static final String DB_NAME = "timovil.db";
    private static final int DB_VERSION = 132;
    private final String TABLE_NAME;

    final static String TablaResolucion = "Resolucion";
    final static String TablaProducto = "Producto";
    final static String TablaCliente = "Cliente";
    final static String TablaDescuento = "Descuento";
    final static String TablaFactura = "Factura";
    final static String TablaDetalleFactura = "DetalleFactura";
    final static String TablaFormaDePago = "FormaPago";
    final static String TablaFacturaCredito = "FacturaCredito";
    final static String TablaDetalleFacturaCredito = "DetalleFacturaCredito";
    final static String TablaAbonoFactura = "AbonoFactura";
    final static String TablaDetalleListaPrecios = "DetalleListaPrecios";
    final static String TablaRemision = "Remision";
    final static String TablaDetalleRemision = "DetalleRemision";
    final static String TablaEntregador = "Entregador";
    final static String TablaMotivoNoVenta = "MotivoNoVenta";
    final static String TablaGestionComercial = "GestionComercial";
    final static String TablaPedidoCallcenter = "PedidoCallcenter";
    final static String TablaDetallePedidoCallcenter = "DetallePedidoCallcenter";
    final static String TablaResultadoGestionCasoCallcenter = "ResultadoGestionCasoCallcenter";
    final static String TablaGuardarMotivoNoVenta = "GuardarMotivoNoVenta";
    final static String TablaGuardarMotivoNoVentaPedido = "GuardarMotivoNoVentaPedido";
    final static String TablaUbicacionRuta = "UbicacionRuta";
    final static String TablaCuentaCaja = "CuentaCaja";
    final static String TablaProgramacionAsesor = "ProgramacionAsesor";
    final static String TablaNotaCreditoFactura = "NotaCreditoFactura";
    final static String TablaDetalleNotaCreditoFactura = "DetalleNotaCreditoFactura";

    String[] columnas;

    abstract void setColumns();

    DAL(Context context, String tableName) {
        super(context, DB_NAME, null, DB_VERSION);
        TABLE_NAME = tableName;
        setColumns();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            BDHelper.CrearBD(db);
        } catch (Exception e) {
            Log.d("DAL", "<<Error creando la BD>> : " + e.getMessage());
        }
    }

    void executeQuery(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null && db.isOpen()) {
            try{
                db.execSQL(query);
            }catch(Exception e) {
                Log.d("Error query: ", e.getMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            //if(oldVersion < 70) {
                onCreate(db);
            //} else{
              //  int versionActual = oldVersion;
                //while (versionActual < newVersion) {
                  //  actualizacionesBaseDatos(db, versionActual);
                   // versionActual++;
                //}
            //}
        } catch (Exception e) {
            Log.d("DAL", "<<Error actualizando la BD>> : " + e.getMessage());
        }
    }

    void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null && db.isOpen()) {
            db.delete(TABLE_NAME, null, null);
        }
    }

    int eliminar(String whereClause) {
        SQLiteDatabase db = this.getWritableDatabase();
        int registros = 0;
        if (db != null && db.isOpen()) {
            registros = db.delete(TABLE_NAME, whereClause == null ? "1" : null, null);
        }
        return registros;
    }

    protected int eliminar(String tabla, String whereClause) {
        SQLiteDatabase db = this.getWritableDatabase();
        int registros = 0;
        if (db != null && db.isOpen()) {
            registros = db.delete(tabla, whereClause == null ? "1" : null, null);
        }
        return registros;
    }

    long insertar(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;
        if (db != null && db.isOpen()) {
            id = db.insertOrThrow(TABLE_NAME, null, values);
        }
        return id;
    }

    Cursor obtener(String[] columnas, String orderBy, String filtro,
                   String[] parametrosFiltro) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null && db.isOpen()) {
            cursor = db.query(TABLE_NAME, columnas, filtro,
                    parametrosFiltro, null, null, orderBy);
        }
        return cursor;
    }

    Cursor obtenerPorId(int id){
        String[] args = {String.valueOf(id)};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null && db.isOpen()) {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", args);
        }
        return cursor;
    }

    Cursor obtener(String strSql, String[] argumentos) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        if (db != null && db.isOpen()) {
            cursor = db.rawQuery(strSql, argumentos);
        }
        return cursor;
    }

    Cursor obtener(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null && db.isOpen()) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

}