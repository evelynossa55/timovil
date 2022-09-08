package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.cm.timovil2.bl.utilities.Images;
import com.cm.timovil2.dto.GestionComercialDTO;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 24/08/2015.
 */
public class GestionComercialDAL extends DAL{

    @Override
    void setColumns() {
        columnas = new String[]{"IdGestion","IdCliente","FechaHora","Comentario","CodigoRuta",
                "Latitud","Longitud","Contacto","TelContacto","Estado", "Sincronizada", "EncodedImages"};
    }

    public GestionComercialDAL(Context context){
        super(context, TablaGestionComercial);
    }

    public long Insertar(GestionComercialDTO gestion){
        ContentValues contentValues = new ContentValues();
        contentValues.put("IdCliente", gestion.IdCliente);
        contentValues.put("FechaHora", gestion.FechaHora);
        contentValues.put("Comentario", gestion.Comentario);
        contentValues.put("CodigoRuta", gestion.CodigoRuta);
        contentValues.put("Latitud", gestion.Latitud);
        contentValues.put("Longitud", gestion.Longitud);
        contentValues.put("Contacto", gestion.Contacto);
        contentValues.put("TelContacto", gestion.TelContacto);
        contentValues.put("Estado", gestion.Estado);
        contentValues.put("Sincronizada", gestion.Sincronizada ? 1: 0);
        contentValues.put("EncodedImages", gestion.EncodedImages);
        gestion.IdGestion = insertar(contentValues);
        return gestion.IdGestion;
    }

    public JSONObject getJson(GestionComercialDTO dto, String idClienteTimo, String imei){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("IdC", dto.IdCliente);
            jsonObject.put("F", dto.FechaHora);
            jsonObject.put("Com", dto.Comentario);
            jsonObject.put("CR", dto.CodigoRuta);
            jsonObject.put("La", dto.Latitud);
            jsonObject.put("Lo", dto.Longitud);
            jsonObject.put("Con", dto.Contacto);
            jsonObject.put("TelCon", dto.TelContacto);
            jsonObject.put("E", dto.Estado);
            jsonObject.put("IdCT", idClienteTimo);
            jsonObject.put("Imei", imei);
            jsonObject.put("IdG", dto.IdGestion);

            if(dto.EncodedImages != null && !dto.EncodedImages.isEmpty()) {

                String[] paths = dto.EncodedImages.split("\\|");
                StringBuilder strbuilder = new StringBuilder();

                for (String path: paths) {
                    if(TextUtils.isEmpty(path)) continue;
                    strbuilder.append(Images.imageFileToBase64String(path, 100));
                    strbuilder.append("|");
                }

                jsonObject.put("EncIma", strbuilder.toString());
            }else{
                jsonObject.put("EncIma", "");
            }

            return jsonObject;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void eliminarTodo(){
        deleteImages();
        deleteAll();
    }


    private void deleteImages() {
        ArrayList<GestionComercialDTO> gestiones = getLista();
        for(GestionComercialDTO g: gestiones){
            if(g.EncodedImages != null && !TextUtils.isEmpty(g.EncodedImages)){
                String[] files = g.EncodedImages.split("\\|");
                for(String file : files){
                    File fdelete = new File(file);
                    if (fdelete.exists()) {
                        fdelete.delete();
                    }
                }
            }
        }
    }

    public ArrayList<GestionComercialDTO> getLista(){
        ArrayList<GestionComercialDTO> lista = new ArrayList<>();
        Cursor cursor = obtener(columnas, "IdGestion ASC", null, null);
        if(cursor != null && cursor.moveToFirst()){
            do{
                lista.add(getFromCursor(cursor));
            }while (cursor.moveToNext());
        }
        return lista;
    }

    public ArrayList<GestionComercialDTO> getListaPendientes(){
        ArrayList<GestionComercialDTO> lista = new ArrayList<>();
        Cursor cursor = obtener(columnas, "IdGestion ASC", "Sincronizada!=?", new String[]{"1"});
        if(cursor != null && cursor.moveToFirst()){
            do{
                lista.add(getFromCursor(cursor));
            }while (cursor.moveToNext());
        }
        return lista;
    }

    public GestionComercialDTO getGestionComercial(long idGestion){
        GestionComercialDTO g = null;
        Cursor cursor = obtener(columnas, null, "IdGestion==?",
                new String[]{String.valueOf(idGestion)});
        if(cursor != null && cursor.moveToFirst()){
                g = (getFromCursor(cursor));
        }
        return g;
    }


    public void cambiarEstadoSincronizacion(long idGestion, boolean estado) throws Exception{
        try {
            String update = "update "
                    + TablaGestionComercial
                    + " set Sincronizada = "+(estado?1:0)+" where IdGestion = " + idGestion;
            executeQuery(update);
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de sincronización:" + e.getMessage());
        }
    }

    private GestionComercialDTO getFromCursor(Cursor cursor){
        GestionComercialDTO gestionComercialDTO = new GestionComercialDTO();
        gestionComercialDTO.IdGestion = cursor.getLong(0);
        gestionComercialDTO.IdCliente = cursor.getInt(1);
        gestionComercialDTO.FechaHora = cursor.getString(2);
        gestionComercialDTO.Comentario = cursor.getString(3);
        gestionComercialDTO.CodigoRuta = cursor.getString(4);
        gestionComercialDTO.Latitud = cursor.getString(5);
        gestionComercialDTO.Longitud = cursor.getString(6);
        gestionComercialDTO.Contacto = cursor.getString(7);
        gestionComercialDTO.TelContacto = cursor.getString(8);
        gestionComercialDTO.Estado = cursor.getString(9);
        gestionComercialDTO.Sincronizada = cursor.getInt(10) == 1;
        gestionComercialDTO.EncodedImages = cursor.getString(11);
        return gestionComercialDTO;
    }
}
