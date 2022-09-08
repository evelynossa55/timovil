package com.cm.timovil2.rest;

import android.content.Context;

import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class NetWorkHelper {

    public final static String DIRECCION_SERVICIO = "http://168.62.104.173";
    //public final static String DIRECCION_SERVICIO = "http://ws.tiendamovil.com.co/api/";
    //public final static String DIRECCION_SERVICIO = "http://192.168.1.72/ServicioVentas";

    //Producción:
    private final static String DIRECCION_REST = DIRECCION_SERVICIO + "/api/";
    private final static String DIRECCION_APK = "http://cliente.tiendamovil.com.co/timovil2.apk";
    private static String direccion_ws_rest;
    public NetWorkHelper() {
        direccion_ws_rest = DIRECCION_REST;
    }

    public String readService(String peticion) throws Exception {

        String respuesta;
        URL url;
        HttpURLConnection conn = null;

        try {
            url = new URL(direccion_ws_rest + peticion);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/json");
            conn.setReadTimeout(300000);
            conn.setConnectTimeout(10000);
            conn.connect();
            int statusCode = conn.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Código de respuesta desde el servidor: " + statusCode);
            } else {
                InputStreamReader isr = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                respuesta = br.readLine();
                isr.close();
                br.close();
            }
        } catch (SocketTimeoutException se) {
            throw new Exception("No se ha podido establecer conexión con el servidor (NetWorkHelper)");
        } catch (Exception e) {
            closeConnection(conn);
            throw new Exception("ERROR: " + e.getMessage());
        } finally {
            closeConnection(conn);
        }
        return respuesta;
    }

    public String writeService(JSONObject jsonObject, String peticion) throws Exception {
        URL url;
        HttpURLConnection connection = null;
        StringBuilder mensajeSalida = new StringBuilder();
        try {

            url = new URL(direccion_ws_rest + peticion);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "Application/json");

            OutputStream os = connection.getOutputStream();
            String input = jsonObject.toString();
            os.write(input.getBytes());
            os.flush();
            connection.connect();

            int statusCode = connection.getResponseCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                mensajeSalida.append("Error: ").append(mensajeSalida).append(" (HTTP ")
                        .append(statusCode).append(")");
            } else {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(isr);

                String line = br.readLine();
                while (line != null) {
                    mensajeSalida.append(line);
                    line = br.readLine();
                }

                isr.close();
                br.close();
            }

        } catch (SocketTimeoutException se) {
            throw new Exception("No se ha podido establecer conexión con el servidor (NetWorkHelper)");
        } catch (Exception e) {
            closeConnection(connection);
            throw new Exception("ERROR: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }

        return mensajeSalida.toString();
    }

    public String writeService(String peticion) throws Exception {
        URL url;
        HttpURLConnection connection = null;
        StringBuilder mensajeSalida = new StringBuilder();
        try {

            url = new URL(direccion_ws_rest + peticion);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "Application/json");

            connection.connect();

            int statusCode = connection.getResponseCode();

            if (statusCode != HttpURLConnection.HTTP_OK) {
                mensajeSalida.append("Error: ").append(mensajeSalida).append(" (HTTP ")
                        .append(statusCode).append(")");
            } else {
                InputStreamReader isr = new InputStreamReader(connection.getInputStream());
                BufferedReader br = new BufferedReader(isr);

                String line = br.readLine();
                while (line != null) {
                    mensajeSalida.append(line);
                    line = br.readLine();
                }

                isr.close();
                br.close();
            }

        } catch (SocketTimeoutException se) {
            throw new Exception("No se ha podido establecer conexión con el servidor (NetWorkHelper)");
        } catch (Exception e) {
            closeConnection(connection);
            throw new Exception("ERROR: " + e.getMessage());
        } finally {
            closeConnection(connection);
        }

        return mensajeSalida.toString();
    }

    private static void closeConnection(HttpURLConnection conexion) {
        if (conexion != null) {
            try {
                conexion.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getApkUrl(Context context){
        try {
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            if(resolucionDTO != null &&
                    (resolucionDTO.IdCliente.equals(Utilities.ID_IGLU)
                    || resolucionDTO.IdCliente.equals(Utilities.ID_ALMIRANTE)
                    || resolucionDTO.IdCliente.equals(Utilities.ID_POLAR))){
                return DIRECCION_APK;
            }else{
                return "market://details?id=" + context.getPackageName();
            }
        }catch (Exception e){
            return DIRECCION_APK;
        }
    }

}
