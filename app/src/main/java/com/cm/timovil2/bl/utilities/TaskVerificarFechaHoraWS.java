package com.cm.timovil2.bl.utilities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 20/10/17.
 */

public class TaskVerificarFechaHoraWS extends AsyncTask<String, String, String> {

    private String message = "";
    private String errorProceso;
    private String fechaActual = "";
    private final ActivityBase contexto;

    public TaskVerificarFechaHoraWS(ActivityBase contexto) {
        this.contexto = contexto;
        errorProceso = "";
    }

    /**
     * * Hace una comparación de las configuraciones de fecha y hora en el dispositivo movil
     * contra las mismas configuraciones en el servidor. Si se presenta incorcondancia
     * el sistema muestra un mensaje de alerta al usuario.
     * @return true si las fechas concuerdan, o false si hay incorcondancia.
     * @throws Exception Excepción general
     */
    private boolean validar_fecha_hora_servidor() throws Exception{

        //boolean sw = true;
        NetWorkHelper netWorkHelper = new NetWorkHelper();
        String url = SincroHelper.getFechaHoraServerURL();
        String respuesta = netWorkHelper.readService(url);

        if(respuesta != null && !respuesta.equals("")){
            String[] fechaServer = SincroHelper.procesarJsonFechaHora(respuesta);
            if(fechaServer != null && fechaServer.length == 5) {

                int intYearServer = Integer.parseInt(fechaServer[0]);
                int intMonthServer = Integer.parseInt(fechaServer[1]);
                int intDayServer = Integer.parseInt(fechaServer[2]);
                int intHourServer = Integer.parseInt(fechaServer[3]);
                int intMinuteServer = Integer.parseInt(fechaServer[4]);

                String fecha_server = (intYearServer) + ":" + (intMonthServer) + ":" + intDayServer
                        + ":" + (intHourServer) + ":" + (intMinuteServer);
                App.guardarFechaDescargaDatos(fecha_server, contexto);

                fechaActual = ((intDayServer < 10) ? "0" + intDayServer : intDayServer)
                        + "/" + ((intMonthServer < 10) ? "0" + intMonthServer : intMonthServer)
                        + "/" + intYearServer
                        + " - " + ((intHourServer < 10) ? "0" + intHourServer : intHourServer)
                        + ":" + ((intMinuteServer < 10) ? "0" + intMinuteServer : intMinuteServer);

                Calendar fechaDispositivo = GregorianCalendar.getInstance();
                int intYearLocal = fechaDispositivo.get(GregorianCalendar.YEAR);
                int intMonthLocal = (fechaDispositivo.get(GregorianCalendar.MONTH) + 1);
                int intDayLocal = fechaDispositivo.get(GregorianCalendar.DAY_OF_MONTH);
                int intHourLocal = fechaDispositivo.get(GregorianCalendar.HOUR_OF_DAY);
                int intMinuteLocal = fechaDispositivo.get(GregorianCalendar.MINUTE);

                if (intYearLocal != intYearServer) {
                    return false;
                } else if (intMonthLocal != intMonthServer) {
                    return false;
                } else if (intDayLocal != intDayServer) {
                    return false;
                } else if (intHourLocal != intHourServer) {
                    int diferencia = (intHourServer - intHourLocal);
                    if ( diferencia > 1 || diferencia < -1 ) return false;
                } else {

                    int diferenciaHora = (intHourServer - intHourLocal);
                    int diferenciaMinutos;

                    if(diferenciaHora == 0){
                        diferenciaMinutos = intMinuteServer - intMinuteLocal;
                    }else{
                        if(diferenciaHora == 1){//El servidor está una hora arriba
                            diferenciaMinutos = (intMinuteServer + (60 - intMinuteLocal));
                        }else{//El servidor está una hora abajo
                            diferenciaMinutos = (intMinuteLocal + (60 - intMinuteServer));
                        }
                    }

                    if (diferenciaMinutos > 5 || diferenciaMinutos < -5) return false;
                }


            }
        }
        return true;
    }

    @Override
    protected String doInBackground(String... params) {
        try {

            ResolucionDAL r = new ResolucionDAL(contexto);
            ResolucionDTO rdto = r.ObtenerResolucion();
            if(!validar_fecha_hora_servidor() && !rdto.IdCliente.equals(Utilities.ID_FR)){
                message = "Por favor corrija la fecha y hora de su dispositivo móvil. La fecha y hora actual es: "
                        + fechaActual;
            }

        } catch (IOException e) {
            errorProceso = "Error IO: " + e.getMessage();
        } catch (XmlPullParserException e) {
            errorProceso = "Error XML: " + e.getMessage();
        } catch (Exception e) {
            errorProceso = "Excepción general: " + e.getMessage();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (errorProceso.equals("") && !message.equals("")) {
            terminarActividad(message);
        }
    }

    private void terminarActividad(String message){
        AlertDialog.Builder d = new AlertDialog.Builder(contexto);
        d.setTitle("Error");
        d.setMessage(message);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                contexto.finish();
            }
        });
        d.show();
    }
}