package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.app.NavUtils;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterImageSlide;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CirclePageIndicator;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Images;
import com.cm.timovil2.bl.utilities.PageIndicator;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.GestionComercialDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.GestionComercialDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 20/08/2015.
 */
public class ActivityGestionComercial extends ActivityBase {

    private EditText editTextContacto;
    private EditText editTextTelefonoContacto;
    private EditText editTextComentario;
    private TextView textViewCliente;
    private int idCliente;
    private ClienteDTO clienteDTO;
    private String codigoRuta;
    private String idClienteTimo;

    //---
    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;
    private ViewPager mViewPager;
    private PageIndicator mIndicator;
    private ArrayList<Bitmap> images;
    private ArrayList<String> pathImages;
    private Runnable animateViewPager;
    private Handler handler;
    private boolean stopSliding = false;

    private String mCurrentPhotoPath;

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGPS();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_comercial);
        setControls();
        App.actualActivity = this;
        try {

            obtenerUbicacion();
            Intent intent = getIntent();
            idCliente = intent.getIntExtra("IdCliente", -1);

            if (idCliente > 0) {
                ClienteDAL clienteDAL = new ClienteDAL(this);
                clienteDTO = clienteDAL.ObtenerClientePorIdCliente(String.valueOf(idCliente));
                String cliente = clienteDTO.NombreComercial + " - " + clienteDTO.RazonSocial
                        + " - DIR: " + clienteDTO.Direccion + " - TEL: " + clienteDTO.Telefono1;
                textViewCliente.setText(cliente);
                codigoRuta = resolucion.CodigoRuta;
                idClienteTimo = resolucion.IdCliente;
                obtenerUbicacion();
            }

        } catch (Exception x) {
            makeErrorDialog(x.getMessage(), this);
        }
    }

    @Override
    protected void setControls() {
        editTextComentario = findViewById(R.id.edit_text_comentario);
        editTextTelefonoContacto = findViewById(R.id.edit_text_tel_contacto);
        editTextContacto = findViewById(R.id.edit_text_contacto);
        textViewCliente = findViewById(R.id.text_view_cliente);
        TextView textTomarFoto = findViewById(R.id.text_view_tomar_foto);
        Button btnGuardarGestionComercial = findViewById(R.id.btn_guardar_gestion_comercial);
        btnGuardarGestionComercial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarGestionComercial();
            }
        });
        textTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mViewPager = findViewById(R.id.view_pager);
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {

                    case MotionEvent.ACTION_CANCEL:
                        break;

                    case MotionEvent.ACTION_UP:
                        // calls when touch release on ViewPager
                        if (images != null && images.size() != 0) {
                            stopSliding = false;
                            runnable(images.size());
                            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY_USER_VIEW);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // calls when ViewPager touch
                        if (handler != null && !stopSliding) {
                            stopSliding = true;
                            handler.removeCallbacks(animateViewPager);
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void setPageAdapter() {
        ArrayList<Bitmap> bitmaps = new ArrayList<>(images);
        mostrarImagenes(bitmaps);
    }

    private void mostrarImagenes(ArrayList<Bitmap> imagenes) {
        if (images != null && images.size() > 0) {
            mViewPager.setAdapter(new AdapterImageSlide(this, imagenes));
            mIndicator.setViewPager(mViewPager);
            runnable(images.size());
            // Re-run callback
            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
        }
    }

    private void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!stopSliding) {
                    if (mViewPager.getCurrentItem() == size - 1) {
                        mViewPager.setCurrentItem(0);
                    } else {
                        mViewPager.setCurrentItem(
                                mViewPager.getCurrentItem() + 1, true);
                    }
                    handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                }
            }
        };
    }

    private boolean validarEntradas() {
        boolean sw = true;
        Editable contacto = editTextContacto.getText();
        Editable telefono_contacto = editTextTelefonoContacto.getText();
        Editable comentario = editTextComentario.getText();

        if (contacto == null || TextUtils.isEmpty(contacto.toString())) {
            editTextContacto.setError("Ingrese el contacto");
            sw = false;
        } else if (telefono_contacto == null || TextUtils.isEmpty(telefono_contacto.toString())) {
            editTextTelefonoContacto.setError("Ingrese el teléfono del contacto");
            sw = false;
        } else if (comentario == null || TextUtils.isEmpty(comentario.toString())) {
            editTextComentario.setError("Ingrese un comentario");
            sw = false;
        }
        return sw;
    }

    private final static int GESTION_COMERCIAL_CAMERA_REQUEST = 3453;

    //private Uri photoURI;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photo;

            try {
                // place where to store camera taken picture
                photo = Images.createTemporaryFile(context);
                mCurrentPhotoPath = photo.getAbsolutePath();

            } catch (Exception e) {
                makeLToast("Por favor verifica tu tarjeta SD, la imagen no se puede capturar");
                return;
            }

            Uri mImageUri;
            if (Build.VERSION.SDK_INT >= 24) {
                mImageUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.cm.timovil2.fileprovider", photo);
            } else {
                mImageUri = Uri.fromFile(photo);
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

            startActivityForResult(takePictureIntent, GESTION_COMERCIAL_CAMERA_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GESTION_COMERCIAL_CAMERA_REQUEST && resultCode == RESULT_OK) {

            try {
                Bitmap bitmap = null;
                if (Build.VERSION.SDK_INT >= 24) {
                    if(data != null){
                        Bundle extras = data.getExtras();
                        assert extras != null;
                        bitmap = (Bitmap) extras.get("data");
                    }else if(!TextUtils.isEmpty(mCurrentPhotoPath)){
                        File file = new File(mCurrentPhotoPath);
                        Uri uri = Uri.fromFile(file);
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    }
                } else {
                    File file = new File(mCurrentPhotoPath);
                    Uri uri = Uri.fromFile(file);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }

                if(bitmap != null){
                    if (pathImages == null) pathImages = new ArrayList<>();
                    pathImages.add(mCurrentPhotoPath);

                    if (images == null) images = new ArrayList<>();
                    images.add(bitmap);

                    setPageAdapter();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == CONFIGURAR_GPS) {
            if (isGpsActive()) {
                makeLToast("Debe habilitar el GPS para facturar");
                finish();
            }
            obtenerUbicacion();
        }

    }

    private void guardarGestionComercial() {
        try {

            if (idCliente <= 0 || codigoRuta == null || TextUtils.isEmpty(codigoRuta)) {
                makeErrorDialog("Error con el cliente, por favor ingrese nuevamente", this);
            }

            if (!validarEntradas()) {
                return;
            }

            GestionComercialDAL gestionComercialDAL = new GestionComercialDAL(this);
            Editable contacto = editTextContacto.getText();
            Editable telefono_contacto = editTextTelefonoContacto.getText();
            Editable comentario = editTextComentario.getText();
            String latitud = App.obtenerConfiguracion_latitudActual(this);
            String longitud = App.obtenerConfiguracion_longitudActual(this);
            String fechaHora = Utilities.FechaHoraAnsi(new Date());
            String estado = "A";
            String installationId = getInstallationId();

            //Guardar registro
            GestionComercialDTO gestionComercialDTO = new GestionComercialDTO();
            gestionComercialDTO.IdCliente = idCliente;
            gestionComercialDTO.FechaHora = fechaHora;
            gestionComercialDTO.Contacto = contacto.toString();
            gestionComercialDTO.Estado = estado;
            gestionComercialDTO.TelContacto = telefono_contacto.toString();
            gestionComercialDTO.Comentario = comentario.toString();
            gestionComercialDTO.Latitud = latitud;
            gestionComercialDTO.Longitud = longitud;
            gestionComercialDTO.CodigoRuta = codigoRuta;
            gestionComercialDTO.Sincronizada = false;
            StringBuilder builder = new StringBuilder();

            if (pathImages != null && pathImages.size() > 0) {
                for (String path : pathImages) {
                    builder.append(path).append("|");
                }
            }

            gestionComercialDTO.EncodedImages = builder.toString();
            long new_id = gestionComercialDAL.Insertar(gestionComercialDTO);

            if (new_id <= 0) {
                makeErrorDialog("Error creando el registro de gestión comercial", ActivityGestionComercial.this);
                return;
            } else {
                if (clienteDTO != null) {
                    new ClienteDAL(this).AtenderCliente(clienteDTO);
                }
            }

            //Sincronizar registro
            if (!Utilities.isNetworkReachable(this) ||
                    !Utilities.isNetworkConnected(this)) {
                AlertDialog.Builder d = new AlertDialog.Builder(this);
                d.setTitle("Gestión guardada");
                d.setMessage("El nuevo registro de gestión comercial se encuentra pendiente de sincronización");
                d.setCancelable(false);
                d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo, int id) {
                        finish();
                    }
                });
                d.show();
            } else {
                JSONObject jsonObject = gestionComercialDAL.getJson(gestionComercialDTO, idClienteTimo, installationId);
                new SyncGestionComercial(this).execute(jsonObject);
            }

        } catch (Exception e) {
            makeErrorDialog("Error guardando el registro de gestión: " + e.getMessage(), this);
        }
    }

    private final int CONFIGURAR_GPS = 34;

    private boolean checkGPS() {

        boolean sw = true;
        if (!isGpsActive() &&
                resolucion.ReportarUbicacionGPS) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Servicios de ubicación inactivos");
            builder.setMessage("Por favor habilite los servicios de ubicación y GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface,
                                    int i) {
                    Intent intent =
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, CONFIGURAR_GPS);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            sw = false;
        }
        return sw;
    }

    private class SyncGestionComercial extends AsyncTask<JSONObject, String, String> {

        private Context contexto;
        private long idGestion;

        private SyncGestionComercial(Context contexto) {
            this.contexto = contexto;
        }

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Enviando la gestión comercial");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.getDialog().dismiss();

            String message;
            String titulo = "Gestion Comercial";
            if (s != null && !TextUtils.isEmpty(s)) {
                if (s.equals(Utilities.IMEI_ERROR)) {
                    try {
                        App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                        message = "configure nuevamente la ruta";
                    } catch (Exception ex) {
                        message = s + ":" + ex.getMessage();
                    }
                } else {
                    message = s;
                }

                if(!isFinishing()){
                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityGestionComercial.this);
                    d.setTitle(titulo);
                    d.setMessage(message);
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                        }
                    });
                    d.show();
                }

            } else {
                message = "Registro de gestión comercial creado con éxito";
                try {
                    GestionComercialDAL gestionComercialDAL = new GestionComercialDAL(contexto);
                    gestionComercialDAL.cambiarEstadoSincronizacion(idGestion, true);
                } catch (Exception e) {
                    message = e.getMessage();
                }

                if(!isFinishing()){
                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityGestionComercial.this);
                    d.setTitle(titulo);
                    d.setMessage(message);
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            finish();
                        }
                    });
                    d.show();
                }
            }
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            String error = null;
            try {

                idGestion = params[0].getLong("IdG");
                publishProgress("Descargando registro", "Conectando con el servidor...");
                NetWorkHelper netWorkHelper = new NetWorkHelper();
                String jsonRespuesta = netWorkHelper.writeService(params[0],
                        SincroHelper.INGRESAR_GESTION_COMERCIAL);

                jsonRespuesta = SincroHelper.procesarOkJson(jsonRespuesta);
                if (!jsonRespuesta.equals("OK")) {
                    return jsonRespuesta;
                }

                publishProgress("Registro de gestión comercial creado con éxito", "...");

            } catch (Exception e) {
                error = e.getMessage();
            }
            return error;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crear_gestion_comercial, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuHistorialGestionComercial:
                Intent intent = new Intent(this, ActivityHistorialGestionesComerciales.class);
                intent.putExtra("IdCliente", idCliente);
                startActivity(intent);
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            default:
                break;
        }
        return true;
    }
}
