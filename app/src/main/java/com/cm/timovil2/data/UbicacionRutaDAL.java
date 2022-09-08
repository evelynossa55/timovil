package com.cm.timovil2.data;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.UbicacionRutaDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;


import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 9/02/17.
 */
public class UbicacionRutaDAL extends DAL {


    private final Context context;

    public UbicacionRutaDAL(Context context) {
        super(context, DAL.TablaUbicacionRuta);
        this.context = context;
    }

    public void Insertar(UbicacionRutaDTO ubicacion) {

        ContentValues values = new ContentValues();
        values.put("Comentario", ubicacion.Comentario);
        values.put("CodigoRuta", ubicacion.CodigoRuta);
        values.put("Fecha", ubicacion.Fecha);
        values.put("Latitud", ubicacion.Latitud);
        values.put("Longitud", ubicacion.Longitud);
        values.put("GpsActivo", ubicacion.GpsActivo?1:0);
        values.put("Sincronizada", ubicacion.Sincronizada?1:0);
        super.insertar(values);

    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    public void eliminarUbicacion(int idUbicacionRuta) {
        String update = "DELETE FROM "
                + TablaUbicacionRuta
                + " WHERE " + "IdUbicacionRuta = " + idUbicacionRuta;
        super.executeQuery(update);
    }

    public void eliminarUbicacionesSincronizadas() {
        String update = "DELETE FROM "
                + TablaUbicacionRuta
                + " WHERE " + "Sincronizada = 1" ;
        super.executeQuery(update);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<UbicacionRutaDTO> ObtenerListado() {

        ArrayList<UbicacionRutaDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener("IdUbicacionRuta ASC", null, null);

        if (cursor.moveToFirst()) {
            do {
                UbicacionRutaDTO dto = new UbicacionRutaDTO();
                dto.IdUbicacionRuta = cursor.getInt(0);
                dto.CodigoRuta = cursor.getString(1);
                dto.Fecha = cursor.getString(2);
                dto.Latitud = cursor.getString(3);
                dto.Longitud = cursor.getString(4);
                dto.GpsActivo = cursor.getInt(5) == 1;
                dto.Comentario = cursor.getString(6);
                dto.Sincronizada = cursor.getInt(7) == 1;
                lista.add(dto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    private Cursor obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<UbicacionRutaDTO> obtenerListadoPendientes() {
        ArrayList<UbicacionRutaDTO> lista = new ArrayList<>();
        String filtro = "Sincronizada = ?";
        String[] parametros = {"0"};

        Cursor cursor;
        cursor = this.obtener(null, filtro, parametros);
        if (cursor.moveToFirst()) {
            do {
                UbicacionRutaDTO f = new UbicacionRutaDTO();
                f.IdUbicacionRuta = cursor.getInt(0);
                f.CodigoRuta = cursor.getString(1);
                f.Fecha = cursor.getString(2);
                f.Latitud = cursor.getString(3);
                f.Longitud = cursor.getString(4);
                f.GpsActivo = cursor.getInt(5) == 1;
                f.Comentario = cursor.getString(6);
                f.Sincronizada = cursor.getInt(7) == 1;
                lista.add(f);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public String sincronizarUbicacion(UbicacionRutaDTO ubicacion) throws Exception {
        ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
        String respuesta = "";
        String imei = getImei();
        if(imei == null){
            imei="1111111";
        }
        else {imei=imei;}

        if (resolucionDTO != null) {
            if (!ubicacion.Sincronizada) {

                if (!Utilities.isNetworkReachable(context)
                        || !Utilities.isNetworkConnected(context)) {
                    throw new Exception(App.ERROR_CONECTIVIDAD);
                }

                NetWorkHelper netWorkHelper = new NetWorkHelper();
                Log.i("LocationService", "pendientes : Ruta --> " +
                                ubicacion.CodigoRuta + " Cliente -->" + resolucionDTO.IdCliente + " Lat -->"
                        + ubicacion.Latitud + " Long -->"
                        + ubicacion.Longitud + " Fecha -->" + ubicacion.Fecha + " Imei -->" + imei
                        + " GpsActivo -->" + ubicacion.GpsActivo + " Comentario -->" + ubicacion.Comentario);

                if(ubicacion.Latitud == null || TextUtils.isEmpty(ubicacion.Latitud))ubicacion.Latitud = "-";
                if(ubicacion.Latitud == null || TextUtils.isEmpty(ubicacion.Longitud))ubicacion.Longitud = "-";
                String peticion = SincroHelper.getRegistroUbicacionURL(
                    ubicacion.CodigoRuta,
                    resolucionDTO.IdCliente,
                    ubicacion.Latitud,
                    ubicacion.Longitud,
                    ubicacion.Fecha,
                    imei,
                    ubicacion.GpsActivo,
                    ubicacion.Comentario
                );

                String result = netWorkHelper.writeService(peticion);
                respuesta = SincroHelper.procesarOkJson(result);

                if (respuesta.equals("OK")) {
                    // Aquí debo actualizar el estado de la remision
                    actualizarEstadoDescarga(ubicacion.IdUbicacionRuta, true);
                }else if(respuesta.equals(Utilities.IMEI_ERROR)){
                    App.guardarConfiguracionEstadoAplicacion("B", context);
                }

            }
        }
        return respuesta;
    }

    private String getImei(){
        String imei = null;
        try{
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {

                TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (manager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        imei = manager.getImei();
                    } else {
                        imei = manager.getDeviceId();
                    }
                }
            }
        }catch(Exception e){
            imei = null;
        }
        return imei;
    }

    private String actualizarEstadoDescarga(int idUbicacionRuta,
                                           boolean sincronizada) throws Exception {
        try {
            String update = "UPDATE "
                    + TablaUbicacionRuta
                    + " SET Sincronizada=" + (sincronizada?"1":"0")
                    + " WHERE IdUbicacionRuta=" + idUbicacionRuta;
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:"
                    + e.getMessage());
        }
    }

    @Override
    void setColumns() {
        columnas = new String[] { "IdUbicacionRuta", "CodigoRuta",
                "Fecha", "Latitud", "Longitud", "GpsActivo",
                "Comentario", "Sincronizada" };
    }
}

