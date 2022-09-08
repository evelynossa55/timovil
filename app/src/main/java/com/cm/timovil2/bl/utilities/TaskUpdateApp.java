package com.cm.timovil2.bl.utilities;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 29/12/2015.
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.wsentities.MAbono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TaskUpdateApp extends AsyncTask<String, String, File> {

    private ActivityBase context;
    private String errorActualizacion = null;
    private CustomProgressBar progressBar;

    public TaskUpdateApp() {
        this.context = App.actualActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar = new CustomProgressBar();
        progressBar.show(context, "TiMovil se está actualizando");
    }

    @Override
    protected File doInBackground(String... args) {
        try {

            if (!Utilities.isNetworkReachable(context) || !Utilities.isNetworkConnected(context)) {
                errorActualizacion = "Por favor verifique su conexión a Internet";
                return null;
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                context.requestReadExternalStoragePermission();
                return null;
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                context.requestWriteExternalStoragePermission();
                return null;
            }

            //Descargamos pendientes
            FacturaDAL fDal = new FacturaDAL(context);
            AbonoFacturaDAL afDal = new AbonoFacturaDAL(context);
            RemisionDAL rDal = new RemisionDAL(context);
            NotaCreditoFacturaDAL ndal = new NotaCreditoFacturaDAL(context);

            fDal.descargarFacturas();
            afDal.descargarAbonos();
            rDal.descargarRemisiones();
            ndal.descargarPendientes();

            ArrayList<FacturaDTO> listaF = fDal.obtenerListadoPendientes();
            ArrayList<MAbono> listaAF = afDal.obtenerListadoPendientes();
            ArrayList<RemisionDTO> listaR = rDal.obtenerListadoPendientes();
            ArrayList<NotaCreditoFacturaDTO> listaN = ndal.obtenerListadoPendientes();

            //Verificamos pendientes
            int pendientes = (listaF.size() + listaAF.size() + listaR.size() + listaN.size());
            if (pendientes > 0) {
                int _listaF = listaF.size();
                int _listaAF = listaAF.size();
                int _listaR = listaR.size();
                int _listaN = listaN.size();

                errorActualizacion = ("\nTiene ")
                        + (_listaF > 0 ? (_listaF + " factura(s)") : "")
                        + (_listaF > 0 && _listaAF > 0 ? ", " : "")
                        + (_listaAF > 0 ? (_listaAF + " abonos(s)") : "")
                        + ((_listaF > 0 || _listaAF > 0) && _listaR > 0 ? " y " : "")
                        + (_listaR > 0 ? (_listaR + " remisiones(s)") : "")
                        + (_listaN > 0 ? (_listaN + " notas(s)") : "")
                        + (" pendiente(s) por descargar");
                return null;
            }

            URL url = new URL(args[0]);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();

            errorActualizacion = "";
            c.connect();

            if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String respuesta = "Error HTTP " + c.getResponseCode()
                        + " " + c.getResponseMessage();
                Log.d("Descarga archivo", respuesta);
                errorActualizacion = respuesta;
                return null;
            }

            InputStream is = c.getInputStream();

            File file = Environment.getExternalStorageDirectory();
            File outputFile = new File(file, "tmp939281.apk");
            if (outputFile.exists()) {
                outputFile.delete();
            }

            FileOutputStream fos = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();

            return outputFile;


        } catch (Exception e) {
            Log.d("ACTUALIZACION", e.getMessage());
            errorActualizacion = e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        progressBar.getDialog().dismiss();

        final File _file = file;

        if (TextUtils.isEmpty(errorActualizacion) && _file != null) {
            context = App.actualActivity;
            AlertDialog.Builder d = new AlertDialog.Builder(context);
            d.setTitle("Actualizar TiMovil");
            d.setMessage("La descarga ha finalizado, por favor da clic en \"Aceptar\" para instalarla");
            d.setCancelable(false);
            d.setPositiveButton("Aceptar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Uri apkURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.cm.timovil2.fileprovider", _file);
                                    Intent intent_newest = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                    intent_newest.setData(apkURI);
                                    intent_newest.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent_newest);
                                } else {
                                    Uri apkUri = Uri.fromFile(_file);
                                    Intent intent_olders = new Intent(Intent.ACTION_VIEW);
                                    intent_olders.setDataAndType(apkUri, "application/vnd.android.package-archive");
                                    intent_olders.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent_olders);
                                }

                                new TaskUpdateDateApp().execute();

                            } catch (Exception e) {
                                context.makeErrorDialog(e.getMessage(), context);
                            }
                        }
                    });
            d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            d.show();
        } else {
            context.makeLToast("Error descargando la actualización: " + errorActualizacion);
        }
    }
}

