package com.cm.timovil2.bl.app;


import android.Manifest;
import android.content.Context;

import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

public class Seguridad {


    public static final String TAG = "TiMovilSecurity";


    public static final int IMEI_REQUEST = 0;
    public static final int LOCATION_REQUEST = 1;
    public static final int READ_EXTERNAL_STORAGE_REQUEST = 2;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST = 3;
    public static final String[] PERMISSIONS_IMEI = {Manifest.permission.READ_PHONE_STATE};
    public static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] PERMISSIONS_READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] PERMISSIONS_WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] PERMISSIONS_ALL = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };


    public enum TiposUsuario {Admin, Vendedor}

    public static ResolucionDTO ValidarUsuario(String clave, Context context) {
        try {
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            if (resolucionDTO != null && resolucionDTO.EsDatoValido()) {
                if (resolucionDTO.ClaveAdmin.equals(clave)) { //Es el administrador
                    resolucionDTO.TipoUsuario = TiposUsuario.Admin;
                    return resolucionDTO;
                } else if (resolucionDTO.ClaveVendedor.equals(clave)) {
                    resolucionDTO.TipoUsuario = TiposUsuario.Vendedor;
                    return resolucionDTO;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
