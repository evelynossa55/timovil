package com.cm.timovil2.location_service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.data.UbicacionRutaDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.UbicacionRutaDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 18/01/17.
 */
public class LocationService extends Service{

    private long MINUTES;
    private LocationManager locationManager;
    private MyLocationListener listener;
    private Location previousBestLocation = null;
    private ResolucionDTO resolucionDTO;

    //Intent intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try{

            long period = App.obtenerConfiguracion_periodoReporteUbicacion(getApplicationContext()) * 60000;
            //long period = 1000 * 60 ;
            MINUTES = period;
            Log.i("LocationService", "onStartCommand: ");
            new ResolucionDAL(getApplicationContext()).ObtenerResolucion();
            if(locationManager == null || listener == null){
                Log.i("LocationService", "listener: period -->" + period);
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                listener = new MyLocationListener();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, period, 0, listener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, period, 0, listener);

                }else{
                    Log.i("LocationService", "Permiso denegado ACCESS_FINE_LOCATION");
                }
            }

            resolucionDTO = new ResolucionDAL(getApplicationContext()).ObtenerResolucion();

            if(!isGpsActive()){
                guardarUbicacion(null, "GPS desactivado", false);
                Log.i("LocationService", "Gps Inactivo ");
            }

        }catch (Exception e){
            guardarUbicacion(null, "Inicio servicio: " + e.toString(), isGpsActive());
            Log.i("LocationService", "Exception: " + e.toString());
        }

        return START_STICKY;
    }

    private boolean isGpsActive() {
        return (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*protected boolean isBetterLocation(Location location, Location currentBestLocation){

        if(currentBestLocation == null){
            //A new location is always better than no location
            return true;
        }

        //Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MINUTES;
        boolean isSignificantlyOlder = timeDelta < -MINUTES;
        boolean isNewer = timeDelta > 0;

        //If it's been more than two minutes since the current location, use the new location
        //because the user has likely moved
        if(isSignificantlyNewer){
            return true;

            //If the new location is more than two minutes older, it must be worse
        }else if(isSignificantlyOlder){
            return false;
        }

        //check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        //check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        //Determine location quality using a combination of timeliness and accuracy
        if(isMoreAccurate){
            return true;
        }else if(isNewer && !isLessAccurate){
            return true;
        }else if(isNewer && !isSignificantlyLessAccurate && isFromSameProvider){
            return true;
        }

        return false;
    }

    private boolean isSameProvider(String provider1, String provider2){
        if(provider1 == null){
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Stop service
        locationManager.removeUpdates(listener);
        Log.i("LocationService", "onDestroy");

        guardarUbicacion(null, "Servicio detenido", isGpsActive());

        try{
            Intent sincroReportIntent = new Intent();
            sincroReportIntent.putExtra("active", false);
            sincroReportIntent.setAction(LocationActiveReceiver.ACTION);
            sendBroadcast(sincroReportIntent);
        }catch (Exception e){
            Log.i("LocationService", "Exception: " + e.toString());
        }

    }

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(final Location loc) {

            try{

                if(resolucionDTO == null) return;

                Log.i("LocationService", "Location changed:");

                boolean tiempo_valido = true;
                if(previousBestLocation != null){
                    long timeDelta = loc.getTime() - previousBestLocation.getTime();
                    tiempo_valido = !(timeDelta < MINUTES);
                    Log.i("LocationService", "Delta --> " + timeDelta);
                }

                if(tiempo_valido){

                    Calendar c = Calendar.getInstance();
                    int dia_semana = c.get(Calendar.DAY_OF_WEEK);
                    int hora = c.get(Calendar.HOUR_OF_DAY);

                    Log.i("LocationService", "Dia-->" + dia_semana);
                    Log.i("LocationService", "Hora-->" + hora);
                    int hora_inicial=0;
                    int hora_final=24;

                    if(resolucionDTO.IdCliente.equals(Utilities.ID_DEFRUTA)){
                        hora_inicial = 8;
                        hora_final = 17;
                    }

                    if (dia_semana > 1 && dia_semana < 7) {//Entre semana
                        if ((hora >= hora_inicial && hora <= hora_final)) {
                            guardarUbicacion(loc, "", true);
                            previousBestLocation = loc;
                        }
                    }
                }

                try{
                    Intent sincroReportIntent = new Intent();
                    sincroReportIntent.putExtra("active", true);
                    sincroReportIntent.setAction(LocationActiveReceiver.ACTION);
                    sendBroadcast(sincroReportIntent);
                }catch (Exception e){
                    Log.i("LocationService", "Exception: " + e.toString());
                }


            }catch (Exception e){
                guardarUbicacion(null, e.toString(), isGpsActive());
                Log.i("LocationService", "Exception: " + e.toString());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

    }

    private class RegistrarUbicacion extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Context contexto = getApplicationContext();

                UbicacionRutaDAL ubicacionRutaDAL = new UbicacionRutaDAL(contexto);

                ArrayList<UbicacionRutaDTO> listadoPendientes
                        = ubicacionRutaDAL.obtenerListadoPendientes();

                Log.i("LocationService", "pendientes : " + listadoPendientes.size());

                for (UbicacionRutaDTO ubicacion: listadoPendientes){
                    String result = ubicacionRutaDAL.sincronizarUbicacion(ubicacion);
                    Log.i("LocationService", "<< " + result + " >>");
                }

                ubicacionRutaDAL.eliminarUbicacionesSincronizadas();

            } catch (Exception e) {
                String respuesta = e.getMessage();
                Log.i("LocationService", "Exception: " + respuesta);
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
            Log.i("LocationService", "Guardada Ubicación " );

            new RegistrarUbicacion().execute();

        }catch (Exception e){
            guardarUbicacion(null, e.toString(), isGpsActive());
            Log.i("LocationService", "Exception: " + e.toString());
        }
    }

}
