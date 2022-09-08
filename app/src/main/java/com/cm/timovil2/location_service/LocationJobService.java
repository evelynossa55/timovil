package com.cm.timovil2.location_service;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.data.UbicacionRutaDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.UbicacionRutaDTO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = LocationJobService.class.getSimpleName();
    private LocationCallback locationCallback;
    private GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    boolean jobCancelled = false;

    private long JOB_MAXIMUM_ALLOWED_TIME_RUNNING;

    private long MINUTES;
    private Location previousBestLocation = null;
    private ResolucionDTO resolucionDTO;
    private JobParameters jobParameters;

    //Called by the Android System when it's time to run thw Job
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Location Job Service started");
        this.jobParameters = jobParameters;
        setUpLocationClientIfNeeded();
        locationRequest = LocationRequest.create();
        //Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        MINUTES = App.obtenerConfiguracion_periodoReporteUbicacion(getApplicationContext()) * 60000;
        //MINUTES = 60000; //One minute
        locationRequest.setInterval(MINUTES);
        locationRequest.setFastestInterval(5000);
        resolucionDTO = new ResolucionDAL(getApplicationContext()).ObtenerResolucion();
        //setUpJobTimeToFinish();
        setUpLocationCallBack();
        return true;
    }

    private void setUpJobTimeToFinish(){
        long job_started_at = new Date().getTime();
        JOB_MAXIMUM_ALLOWED_TIME_RUNNING = job_started_at + (MINUTES * 2);
    }

    private void setUpLocationCallBack(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null) return;
                Location location = locationResult.getLastLocation();
                if(location != null){
                    if(resolucionDTO == null || jobCancelled) return;
                    if(validateTimeNewLocation(location) && validateDateNewLocation()){
                        Log.d(TAG, "Location changed:");
                        guardarUbicacion(location, "", true);
                        previousBestLocation = location;
                    }
                }
            }
        };
    }

    private boolean validateTimeNewLocation(Location loc){
        boolean tiempo_valido = true;
        if(previousBestLocation != null){
            long timeDelta = loc.getTime() - previousBestLocation.getTime();
            tiempo_valido = !(timeDelta < MINUTES);
            Log.d(TAG, "Delta --> " + timeDelta);
        }

        return tiempo_valido;
    }

    private boolean validateDateNewLocation(){
        Calendar c = Calendar.getInstance();
        int dia_semana = c.get(Calendar.DAY_OF_WEEK);
        int hora = c.get(Calendar.HOUR_OF_DAY);

        Log.d(TAG, "Dia-->" + dia_semana);
        Log.d(TAG, "Hora-->" + hora);
        int hora_inicial=0;
        int hora_final=24;

        if(resolucionDTO.IdCliente.equals(Utilities.ID_DEFRUTA)){
            hora_inicial = 8;
            hora_final = 17;
        }

        boolean entreSemana = (dia_semana >= Calendar.MONDAY && dia_semana <= Calendar.SATURDAY);
        boolean entreHorasDeTrabajo = (hora >= hora_inicial && hora <= hora_final);

        return entreSemana && entreHorasDeTrabajo;
    }

    //Called if the job was cancelled before being finished
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before being completed");
        jobCancelled = true;
        jobFinished(jobParameters, true);
        return true;
    }

    private void setUpLocationClientIfNeeded(){
        if(googleApiClient == null)
            buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient(){
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        this.googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request location updates called");
            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, locationCallback, null /*Looper*/);
        }else{
            Log.d(TAG, "ACCESS_FINE_LOCATION Permission denied");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Location connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Location connection failed");
    }

    private class RegistrarUbicacion extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Context contexto = getApplicationContext();

                UbicacionRutaDAL ubicacionRutaDAL = new UbicacionRutaDAL(contexto);

                ArrayList<UbicacionRutaDTO> listadoPendientes
                        = ubicacionRutaDAL.obtenerListadoPendientes();

                Log.d(TAG, "pendientes : " + listadoPendientes.size());

                for (UbicacionRutaDTO ubicacion: listadoPendientes){
                    String result = ubicacionRutaDAL.sincronizarUbicacion(ubicacion);
                    Log.d(TAG, "<< " + result + " >>");
                }

                ubicacionRutaDAL.eliminarUbicacionesSincronizadas();

                long actual_time = new Date().getTime();
                if(actual_time > JOB_MAXIMUM_ALLOWED_TIME_RUNNING){
                    Log.d(TAG, "Location Job Service finished!");
                    jobFinished(jobParameters, true /* Needs re schedule*/);
                }

            } catch (Exception e) {
                String respuesta = e.getMessage();
                Log.d(TAG, "Exception: " + respuesta);
            }
            return null;
        }
    }

    private void guardarUbicacion(Location loc, String comentario, boolean gpsActive){
        try{

            if(resolucionDTO == null) return;

            String latitud = String.valueOf(loc!=null?loc.getLatitude():"-");
            String longitud = String.valueOf(loc!=null?loc.getLongitude():"-");

            UbicacionRutaDTO ubicacion = new UbicacionRutaDTO();
            ubicacion.Comentario = comentario;
            ubicacion.GpsActivo = gpsActive;
            ubicacion.Sincronizada = false;
            ubicacion.CodigoRuta = resolucionDTO.CodigoRuta;
            ubicacion.Latitud = latitud;
            ubicacion.Longitud= longitud;
            ubicacion.Fecha = Utilities.FechaHoraAnsi(new Date());

            if(!latitud.equals("-") && !longitud.equals("-")){
                Calendar fechaDispositivo = GregorianCalendar.getInstance();
                fechaDispositivo.setTime(new Date());
                long milisegundosActuales = fechaDispositivo.getTimeInMillis();
                App.guardarConfiguracion_ultimoRegistroUbicacion(getApplicationContext(), milisegundosActuales);
                App.guardarConfiguracion_latitudActual(getApplicationContext(), latitud);
                App.guardarConfiguracion_longitudActual(getApplicationContext(), longitud);
            }

            new UbicacionRutaDAL(getApplicationContext()).Insertar(ubicacion);
            Log.d(TAG, "Ubicación guardada" );

            new RegistrarUbicacion().execute();

        }catch (Exception e){
            guardarUbicacion(null, e.toString(), false);
            Log.i(TAG, "Exception: " + e.toString());
        }
    }
}
