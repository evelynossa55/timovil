package com.cm.timovil2.front;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.app.Seguridad;
import com.cm.timovil2.bl.utilities.ControlImeiService;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.ServicePedidosCallcenter;
import com.cm.timovil2.bl.utilities.ServiceSincrofacturas;
import com.cm.timovil2.bl.utilities.TaskUpdateDateApp;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ControlImeiDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.location_service.AlarmReceiverTimovil;
import com.cm.timovil2.location_service.LocationJobService;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;
import com.cm.timovil2.sincro.SincroResolucion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ActivityLogin extends ActivityBase implements View.OnClickListener{

    private EditText mPasswordView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        App.actualActivity = this;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setControls();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleLocationJobService();
        }else{
            setLocationAlarm();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {

            if (resolucion == null ||
                    !resolucion.EsDatoValido() ||
                    !App.validarEstadoAplicacion(this)) {
                guardarConfiguracionImei();
            }

            comenzarServicios();

        }catch (Exception e){
            logCaughtException(e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("ActivityConfigRuta", e.getMessage());
        }
    }

    private void comenzarServicios() {

        Log.d("ServiceSincrofacturas", "Se ha iniciado el servicio...");
        Intent facturasIntent = new Intent(ActivityLogin.this, ServiceSincrofacturas.class);
        startService(facturasIntent);

        Log.d("ControlImeiService", "Se ha iniciado el servicio...");
        Intent ImeiIntent = new Intent(ActivityLogin.this, ControlImeiService.class);
        startService(ImeiIntent);


        if(resolucion != null &&
                (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                        || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                        || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                        || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                        || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_IGLU)
                )){

            Log.d("PedidosService", "Se ha iniciado el servicio...");
            Intent pedidosCallcenterIntent = new Intent(ActivityLogin.this, ServicePedidosCallcenter.class);
            startService(pedidosCallcenterIntent);

        }
    }

    @Override
    protected void setControls() {
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                                          KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.btnConfigurar).setOnClickListener(this);
        findViewById(R.id.btnActualzar).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int controlId = v.getId();
        switch (controlId){
            case R.id.sign_in_button:
                attemptLogin();
                break;
            case R.id.btnConfigurar:
                configurarApp();
                break;
            case R.id.btnActualzar:
                actualizarApp();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_configurar:
                configurarApp();
                break;
            case R.id.menu_actualizar_aplicacion:
                actualizarApp();
                break;
                /**
            case R.id.menu_cierredia:
                AlertDialog.Builder d = new AlertDialog.Builder(context);
                d.setTitle("Timovil");
                d.setMessage("¿Estas seguro de cerrar el dia?");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    d.setIcon(getResources().getDrawable(R.drawable.icon_launcher, getTheme()));
                } else {
                    d.setIcon(getResources().getDrawable(R.drawable.icon_launcher));
                }
                d.setCancelable(true);
                d.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        resolucion.DiaCerrado = true;
                        new ResolucionDAL(context).Insertar(resolucion);
                    }
                });
                d.show();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    private void pasarAconfigurarRuta() {
        Toast.makeText(this, "Configurar ruta", Toast.LENGTH_LONG).show();
        Intent i = new Intent(ActivityLogin.this, ActivityConfigRuta.class);
        startActivity(i);
    }

    /**
     * Recupera el Id del Dispositivo (IMEI) y consulta los datos del cliente y la ruta
     * asociados a este dispositivo. Si recupera los datos del cliente y la ruta correctamente
     * entonces descarga y guarda la resolución de facturación.
     */
    private void guardarConfiguracionImei() {
        if(Utilities.isNetworkReachable(this)
                && Utilities.isNetworkConnected(this)) {
            new CargaResolucionWS(this).execute();
        }
    }

    private void guardarVersionApp(){
        try {

            if(isFirstRun()){
                new TaskUpdateDateApp().execute();
            }

            App.guardarConfiguracion_VersionAplicacion(this, getResources().getString(R.string.version_number));

        } catch (Exception e) {
            logCaughtException(e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if (mPasswordView != null) {
            mPasswordView.setError(null);
            if (mPasswordView.getText() == null || mPasswordView.getText().toString().equals("")) {
                App.MostrarToast(this, "Por favor ingrese su clave.");
                return;
            }

            ResolucionDTO resolucion = Seguridad.
                    ValidarUsuario(mPasswordView.getText().toString().trim(), this);

            if (resolucion != null) {
                startBackUpThread();
                mPasswordView.setText("");
                switch (resolucion.TipoUsuario) {
                    case Admin:
                        Intent iv_ = new Intent(ActivityLogin.this, ActivityMenuVendedor.class);
                        startActivity(iv_);
                        App.MostrarToast(this, "Usuario administrador");
                        break;
                    case Vendedor:
                        Intent iv = new Intent(ActivityLogin.this, ActivityMenuVendedor.class);
                        startActivity(iv);
                        break;
                    default:
                        break;
                }
            } else {
                App.MostrarToast(this, "Clave inválida");
            }
        }
    }

    // Setup a recurring alarm every half hour
    private void setLocationAlarm() {

        try{
            if(resolucion == null || !resolucion.ReportarUbicacionGPS) return;

            //long period = App.obtenerConfiguracion_periodoReporteUbicacion(getApplicationContext()) * 60000;
            long period = 1000 * 60 * 15;
            // Construct an intent that will execute the AlarmReceiver
            Intent intent = new Intent(getApplicationContext(), AlarmReceiverTimovil.class);
            intent.setAction(AlarmReceiverTimovil.ACTION);

            // Create a PendingIntent to be triggered when the alarm goes off
            final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiverTimovil.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Setup periodic alarm every ...
            long firstMillis = System.currentTimeMillis(); // alarm is set right away
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
            // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
            if(alarm != null) alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, period, pIntent);

            Log.i("LocationService", "Se ha iniciado el servicio from Login...");

        }catch (Exception e){
            logCaughtException(e);
            Log.d("LocationService", e.toString());
        }

    }

    private final static int jobId = 2;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleLocationJobService(){
        ComponentName componentName = new ComponentName(this, LocationJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(jobId, componentName)
                .setPeriodic(5 * 60000) //5 minutes
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler!=null){
            int resultCode = jobScheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d("LocationJobService", "Location Job Service scheduled!");
            } else {
                Log.d("LocationJobService", "Location Job Service not scheduled");
            }
        }
    }

    //------------------------DESCARGA DE LA RESOLUCIÓN CON LA CONF ANTERIOR----------------
    private class CargaResolucionWS extends AsyncTask<Void, String, String> {

        ActivityBase contexto;

        private CargaResolucionWS(ActivityBase contexto) {
            this.contexto = contexto;
        }

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Cargando la configuración");
        }

        @Override
        protected String doInBackground(Void... params) {
            String respuesta;
            String error;
            try {
                if (Utilities.isNetworkReachable(contexto) &&
                        Utilities.isNetworkConnected(contexto)) {

                    if(contexto != null){
                        String installationId = contexto.getInstallationId();
                        if(installationId == null || installationId.trim().equals("")){
                            error = "Error: El Id de instalación no está disponible";
                            Log.d("ActivityLogin", error);
                            throw new Exception(error);
                        }else {
                            String res;
                            NetWorkHelper netWorkHelper = new NetWorkHelper();
                            String peticion = SincroHelper.getConfigRutaImeiURL(installationId);
                            res = netWorkHelper.readService(peticion);
                            ControlImeiDTO controlImeiDTO = SincroHelper.procesarImeiJsonObject(res);

                            if( controlImeiDTO != null
                                    && controlImeiDTO.IdCliente != null
                                    && controlImeiDTO.CodigoRuta != null
                                    && !TextUtils.isEmpty(controlImeiDTO.CodigoRuta)){

                                SincroResolucion sincroResolucion = SincroResolucion.getInstance();
                                sincroResolucion.setControlImei(controlImeiDTO);
                                sincroResolucion.download(contexto);
                                resolucion = sincroResolucion.getResolucionActualDTO();

                                App.guardarConfiguracionEstadoAplicacion("OK", contexto);
                                Log.d("ControlImeiService", "Se ha re-iniciado el servicio...");
                                Intent ImeiIntent = new Intent(ActivityLogin.this, ControlImeiService.class);
                                startService(ImeiIntent);
                                respuesta = "OK";

                            }else{
                                error = "Error validando el Id de Instalación";
                                Log.d("ActivityLogin", error);
                                throw new Exception(error);
                            }
                        }

                    }else {
                        error = "Error verificando el Id de Instalación: contexto = null";
                        Log.d("ActivityLogin", error);
                        throw new Exception(error);
                    }

                }else{
                    error = "Error: El Id de Instalación no está disponible";
                    Log.d("ActivityLogin", error);
                    throw new Exception(error);
                }

            } catch (IOException e) {
                logCaughtException(e);
                respuesta = e.getMessage();
                publishProgress("Error IO", e.getMessage());
            } catch (XmlPullParserException e) {
                logCaughtException(e);
                respuesta = e.getMessage();
                publishProgress("Error XML", e.getMessage());
            } catch (Exception e) {
                logCaughtException(e);
                respuesta = e.getMessage();
                publishProgress("Exception", e.getMessage());
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();
            if (result == null || !result.equals("OK")) {
                new ResolucionDAL(contexto).Eliminar();
                if(result == null || !result.equals("Error: El Id de Instalación no está disponible")){
                    pasarAconfigurarRuta();
                }
            }else{
                guardarVersionApp();
            }
        }
    }
}