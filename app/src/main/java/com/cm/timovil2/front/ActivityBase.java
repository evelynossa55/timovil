package com.cm.timovil2.front;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.cm.timovil2.R;
import com.cm.timovil2.backup.AbonoBackUp;
import com.cm.timovil2.backup.FacturaBackUp;
import com.cm.timovil2.backup.NoVentaBackUp;
import com.cm.timovil2.backup.NoVentaPedidoBackUp;
import com.cm.timovil2.backup.NotaCreditoFacturaPorDevolucionBackUp;
import com.cm.timovil2.backup.RemisionBackUp;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.app.Installation;
import com.cm.timovil2.bl.app.Seguridad;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.data.UbicacionRutaDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.UbicacionRutaDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.fabric.sdk.android.Fabric;

public abstract class ActivityBase extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    final ActivityBase context = this;
    protected static CustomProgressBar progressBar;
    public static ResolucionDTO resolucion;

    protected abstract void setControls();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        loadResolucion();
        logUserToCrashLytics();
        requestAllPermissions();
        obtenerUbicacion();
    }

    void loadResolucion() {
        if (resolucion == null) {
            resolucion = new ResolucionDAL(this).ObtenerResolucion();
        }
    }

    void reloadResolucion() {
        resolucion = new ResolucionDAL(this).ObtenerResolucion();
    }

    public void configurarApp() {
        Intent i = new Intent(this, ActivityConfigRuta.class);
        startActivity(i);
    }

    public void actualizarApp() {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityBase.this);
        d.setTitle("Actualizar TiMovil");
        d.setIcon(R.drawable.icon_launcher);
        d.setMessage("Si es la primera vez que instala TiMovil desde la Play Store, por favor desinstale su versión actual. Posteriormente diríjase a la Play Store y descargue la aplicación, buscándola con su nombre original \"TiMovil\".");
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                String url = NetWorkHelper.getApkUrl(ActivityBase.this);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        d.show();
    }

    public void makeLToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    void makeDialog(String titulo, String mensaje, Context c) {
        Builder d = new Builder(c);
        d.setTitle(titulo);
        d.setMessage(mensaje);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            d.setIcon(getResources().getDrawable(R.drawable.icon_launcher, getTheme()));
        } else {
            d.setIcon(getResources().getDrawable(R.drawable.icon_launcher));
        }
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    void makeDialog(String mensaje, Context c) {
        makeDialog("TiMovil", mensaje, c);
    }

    public void makeErrorDialog(String mensaje, Context c) {
        makeDialog("Error - TiMovil", mensaje, c);
    }

    public void makeNotification(String titulo, String mensaje, boolean error) {

        // notifyID allows you to update the notification later on.
        int notifyID = 4;
        String CHANNEL_ID = "timovil_not_0";
        String CHANNEL_NAME = "timovil";

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                                error ? android.R.drawable.stat_notify_error :
                                        R.drawable.icon_launcher)
                        .setContentTitle(titulo)
                        .setContentText(mensaje)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setTicker("TiMovil")
                        .setSound(alarmSound);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            if (mNotificationManager != null)
                mNotificationManager.createNotificationChannel(mChannel);
        }

        if (mNotificationManager != null)
            mNotificationManager.notify(notifyID, mBuilder.build());

    }

    public String getInstallationId() {
        String id;
        try {
            id = Installation.id(context);
        } catch (Exception e) {
            id = null;
        }
        return id;
    }

    //-----------REQUEST PERMISSIONS------

    private void requestImeiPermission() {

        Log.i(Seguridad.TAG, "Requesting permission for reading IMEI");

        if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityBase.this,
                Manifest.permission.READ_PHONE_STATE)) {
            //Display message with explanation and a button to trigger the request
            Builder d = new Builder(ActivityBase.this);
            d.setTitle("Solicitud de permiso");
            d.setMessage("TiMovil solicita permiso para realizar configuración de la ruta satisfactoriamente");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher, getTheme()));
            } else {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher));
            }
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    ActivityCompat.requestPermissions(
                            ActivityBase.this,
                            Seguridad.PERMISSIONS_IMEI,
                            Seguridad.IMEI_REQUEST);
                }
            });
            d.show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    Seguridad.PERMISSIONS_IMEI,
                    Seguridad.IMEI_REQUEST);
        }
    }

    private void requestLocationPermission() {

        Log.i(Seguridad.TAG, "Requesting permission for Locations");

        if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityBase.this,
                Manifest.permission.READ_PHONE_STATE)) {
            //Display message with explanation and a button to trigger the request
            AlertDialog.Builder d = new AlertDialog.Builder(ActivityBase.this);
            d.setTitle("Solicitud de permiso");
            d.setMessage("TiMovil solicita permiso para acceder a su ubicacón");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher, getTheme()));
            } else {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher));
            }
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    ActivityCompat.requestPermissions(
                            ActivityBase.this,
                            Seguridad.PERMISSIONS_LOCATION,
                            Seguridad.LOCATION_REQUEST);
                }
            });
            d.show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    Seguridad.PERMISSIONS_LOCATION,
                    Seguridad.LOCATION_REQUEST);
        }
    }

    public void requestReadExternalStoragePermission() {

        Log.i(Seguridad.TAG, "Requesting permission for read sd");

        if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityBase.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //Display message with explanation and a button to trigger the request
            AlertDialog.Builder d = new AlertDialog.Builder(ActivityBase.this);
            d.setTitle("Solicitud de permiso");
            d.setMessage("TiMovil solicita permiso para acceder a su tarjeta SD");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher, getTheme()));
            } else {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher));
            }
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    ActivityCompat.requestPermissions(
                            ActivityBase.this,
                            Seguridad.PERMISSIONS_READ_EXTERNAL_STORAGE,
                            Seguridad.READ_EXTERNAL_STORAGE_REQUEST);
                }
            });
            d.show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    Seguridad.PERMISSIONS_READ_EXTERNAL_STORAGE,
                    Seguridad.READ_EXTERNAL_STORAGE_REQUEST);
        }
    }

    public void requestWriteExternalStoragePermission() {

        Log.i(Seguridad.TAG, "Requesting permission for read sd");

        if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityBase.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Display message with explanation and a button to trigger the request
            AlertDialog.Builder d = new AlertDialog.Builder(ActivityBase.this);
            d.setTitle("Solicitud de permiso");
            d.setMessage("TiMovil solicita permiso para acceder a su tarjeta SD");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher, getTheme()));
            } else {
                d.setIcon(getResources().getDrawable(R.drawable.icon_launcher));
            }
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    ActivityCompat.requestPermissions(
                            ActivityBase.this,
                            Seguridad.PERMISSIONS_WRITE_EXTERNAL_STORAGE,
                            Seguridad.WRITE_EXTERNAL_STORAGE_REQUEST);
                }
            });
            d.show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    Seguridad.PERMISSIONS_WRITE_EXTERNAL_STORAGE,
                    Seguridad.WRITE_EXTERNAL_STORAGE_REQUEST);
        }
    }

    public void requestAllPermissions() {
        if (!hasPermissions(Seguridad.PERMISSIONS_ALL)) {
            int PERMISSIONS_ALL = 100;
            ActivityCompat.requestPermissions(this, Seguridad.PERMISSIONS_ALL, PERMISSIONS_ALL);
        }
    }

    private boolean hasPermissions(String[] permissions) {
        boolean granted = false;
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            granted = permissionCheck == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                break;
            }
        }
        return granted;
    }

    //LOCATION LISTENER
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location ubicacion_actual = null;
    private LocationManager manager;

    private void guardarUbicacion() {
        try {
            if (ubicacion_actual != null) {
                String latitud = String.valueOf(ubicacion_actual.getLatitude());
                String longitud = String.valueOf(ubicacion_actual.getLongitude());
                App.guardarConfiguracion_latitudActual(getApplicationContext(), latitud);
                App.guardarConfiguracion_longitudActual(getApplicationContext(), longitud);

                //Validar tiempo transcurrido desde el último registro
                Calendar fechaDispositivo = GregorianCalendar.getInstance();
                fechaDispositivo.setTime(new Date());
                long ultimoRegistroUbicacion = App.obtenerConfiguracion_ultimoRegistroUbicacion(App.actualActivity);

                long milisegundosActuales = fechaDispositivo.getTimeInMillis();
                long diff = milisegundosActuales - ultimoRegistroUbicacion;
                long diffMinutes = diff / (60 * 1000);

                boolean swRegistrar = false;
                int periodoReporteUbicacion = App.obtenerConfiguracion_periodoReporteUbicacion(App.actualActivity);

                if (ultimoRegistroUbicacion <= 0) {
                    swRegistrar = true;
                } else {
                    if (diffMinutes >= periodoReporteUbicacion) {
                        swRegistrar = true;
                    }
                }
                if (swRegistrar==true) {
                    App.guardarConfiguracion_ultimoRegistroUbicacion(this, milisegundosActuales);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                requestLocationPermission();

            } else {

                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        ubicacion_actual = location;
        // Crear un hilo para enviar la ubicacion al servicio cada cambio del Activity
        // Esta porcion de codigo se puede mejorar
        new Thread(new Runnable(){
            @Override
            public void run() {
//                registrarUbicacion(location.getLatitude(), location.getLongitude());
            }
        }).start();
        guardarUbicacion();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        obtenerUbicacion();
    }

    @Override
    public void onConnectionSuspended(int i) {
        obtenerUbicacion();
    }

    void obtenerUbicacion() {
        try {
            if (googleApiClient == null || !googleApiClient.isConnected()) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();

                //ong period = App.obtenerConfiguracion_periodoReporteUbicacion(getApplicationContext()) * 60000;
                long period = 10000;
                locationRequest = new LocationRequest();
                locationRequest.setInterval(period);
                locationRequest.setFastestInterval(period / 2);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                googleApiClient.connect();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    boolean isGpsActive() {
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_provider = (manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        boolean network_provider = (manager != null && manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        return (gps_provider || network_provider);
    }

    boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return Settings.System.getInt(c.getContentResolver(), Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    boolean isTimeZoneAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) == 1;
        } else {
            return Settings.System.getInt(c.getContentResolver(), Settings.System.AUTO_TIME_ZONE, 0) == 1;
        }
    }

    boolean verificarVersionAplicacion() {
        String version_number = App.obtenerConfiguracion_VersionAplicacion(this);
        String last_version_number = App.obtenerConfiguracion_LastVersionAplicacion(this);

        if (!version_number.equals("NULL") && !last_version_number.equals("NULL")) {
            try {
                int int_version_number = Integer.parseInt(version_number);
                int int_last_version_number = Integer.parseInt(last_version_number);

                return (int_last_version_number > int_version_number);

            } catch (Exception e) {
                e.printStackTrace();
                makeErrorDialog("Error comprobando la versión de la aplicación", this);
                return false;
            }
        } else {
            return false;
        }
    }

    boolean isFirstRun() {
        String version_actual_app = getResources().getString(R.string.version_number);
        String version_actual_guardada = App.obtenerConfiguracion_VersionAplicacion(this);

        if (!version_actual_guardada.equals("NULL") && !TextUtils.isEmpty(version_actual_app)) {

            try {
                int int_version_actual_guardada = Integer.parseInt(version_actual_guardada);
                int int_version_actual_app = Integer.parseInt(version_actual_app);

                return (int_version_actual_app > int_version_actual_guardada);

            } catch (Exception e) {
                e.printStackTrace();
                makeErrorDialog("Error comprobando la versión de la aplicación", this);
                return false;
            }

        } else {
            return false;
        }
    }

    void startBackUpThread() {
        new Thread(new Runnable() {
            public void run() {
                startBackUp();
            }
        }).start();
    }

    public void startBackUp() {
        if (Utilities.isNetworkConnected(context)) {
            new FacturaBackUp(context).syncBackup();
            new RemisionBackUp(context).syncBackup();
            new AbonoBackUp(context).syncBackup();
            new NoVentaBackUp(context).syncBackup();
            new NoVentaPedidoBackUp(context).syncBackup();
            new NotaCreditoFacturaPorDevolucionBackUp(context).syncBackup();
        }
    }

    void registrarUbicacion(double latitude, double longitude) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            UbicacionRutaDTO ubicacionRutaDTO = new UbicacionRutaDTO();
            ubicacionRutaDTO.CodigoRuta = resolucion.CodigoRuta;
            ubicacionRutaDTO.Latitud = String.valueOf(latitude);
            ubicacionRutaDTO.Longitud = String.valueOf(longitude);
            ubicacionRutaDTO.Fecha = Utilities.FechaHoraAnsi(new Date());
            ubicacionRutaDTO.GpsActivo = isGpsActive();
            ubicacionRutaDTO.Comentario = "Comentario";
            ubicacionRutaDTO.Sincronizada = false;

            new UbicacionRutaDAL(this).sincronizarUbicacion(ubicacionRutaDTO);
            Log.e("Registro ubicacion", "Ubicacion registrada");
        } catch (Exception e) {
            Log.e("Registro ubicacion", "Error registrando " + e.getMessage());
        }
    }

    //CRASHLYTICS

    /**
     * Log Exception to CrashLytics
     *
     * @param ex to be logged*/

    void logCaughtException(Exception ex) {
        Crashlytics.logException(ex);
    }

    private void logUserToCrashLytics() {
        if(resolucion == null) return;
        String userId = "Ruta {0} - {1}";
        Crashlytics.setUserIdentifier(String.format(userId, resolucion.CodigoRuta, resolucion.IdCliente));
    }
}