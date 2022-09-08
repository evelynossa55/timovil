package com.cm.timovil2.bl.utilities;

import android.os.AsyncTask;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 15/12/17.
 */

public class TaskUpdateDateApp extends AsyncTask<String, String, String> {
    private final ActivityBase context;

    public TaskUpdateDateApp() {
        this.context = App.actualActivity;
    }

    @Override
    protected String doInBackground(String... params) {
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucion = resolucionDAL.ObtenerResolucion();
        if(resolucion == null) return "No hay una configuración cargada actualmente";
        NetWorkHelper netWorkHelper = new NetWorkHelper();

        try{
            String url_info_android = SincroHelper.getEnviarFechaActualizacionURL(
                    resolucion.IdCliente, resolucion.CodigoRuta);
            String respuesta_info = netWorkHelper.writeService(url_info_android);
            respuesta_info = SincroHelper.procesarOkJson(respuesta_info);

            return respuesta_info;
        }catch (Exception ex){
            return ex.toString();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(!s.equals("OK")){
            context.makeErrorDialog(s, context);
        }
    }
}
