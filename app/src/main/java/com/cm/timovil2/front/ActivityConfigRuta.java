package com.cm.timovil2.front;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.ControlImeiService;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.ControlImeiDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;
import com.cm.timovil2.sincro.SincroResolucion;

public class ActivityConfigRuta extends ActivityBase {

    private TextView lblNumeroResolucion;
    private TextView lblFacturaInicial;
    private TextView lblFacturaFinal;
    private TextView lblPrefijoFacturacion;
    private TextView lblFechaResolucion;
    private TextView lblSiguienteFactura;
    private TextView lblCodigoRuta;
    private TextView lblNombreRuta;
    private TextView lblInstallationId;
    private ActivityBase context;
    private Button config_button;
    private Button wsp_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_ruta_v2);
        setControls();
        cargarResolucion();
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        App.actualActivity = this;
        context = App.actualActivity;
        config_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CargaResolucionWS(context).execute();
            }
        });
        wsp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, lblInstallationId.getText().toString());
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ActivityConfigRuta.this, "Whatsapp no está instalado", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            if (!Utilities.isNetworkConnected(this) && !Utilities.isNetworkReachable(this)) {
                App.MostrarToast(this, "Verifique la conexión");
            }
        } catch (Exception e) {
            logCaughtException(e);
            App.MostrarToast(this, "Error comprobando la conectividad a datos: " + e.getMessage());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void setControls() {
        lblNumeroResolucion = findViewById(R.id.lblNumeroResolucion);
        lblFechaResolucion = findViewById(R.id.lblFechaResolucion);
        lblPrefijoFacturacion = findViewById(R.id.lblPrefijo);
        lblFacturaInicial = findViewById(R.id.lblFacturaInicial);
        lblFacturaFinal = findViewById(R.id.lblFacturaFinal);
        lblSiguienteFactura = findViewById(R.id.lblSiguienteFactura);
        lblCodigoRuta = findViewById(R.id.lblCodigoRuta);
        lblNombreRuta = findViewById(R.id.lblNombreRuta);
        lblInstallationId = findViewById(R.id.lblInstallationId);
        config_button = findViewById(R.id.config_button);
        wsp_button = findViewById(R.id.wsp_button);
    }

    private void cargarResolucion() {
        try {
            lblInstallationId.setText(getInstallationId());
            if (resolucion != null) {
                lblNumeroResolucion.setText(resolucion.Resolucion);
                lblFechaResolucion.setText(resolucion.FechaResolucion);
                lblPrefijoFacturacion.setText(resolucion.PrefijoFacturacion);
                lblFacturaInicial.setText(resolucion.FacturaInicial);
                lblFacturaFinal.setText(resolucion.FacturaFinal);
                lblSiguienteFactura.setText(String.valueOf(resolucion.SiguienteFactura));
                lblCodigoRuta.setText(resolucion.CodigoRuta);
                lblNombreRuta.setText(resolucion.NombreRuta);
            }
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityConfigRuta.this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this,
                    ActivityLogin.class));
        }
        return true;
    }

    private class CargaResolucionWS extends AsyncTask<Void, Integer, String> {

        String error = null;

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Configurando la ruta...");
        }

        ActivityBase contexto;

        private CargaResolucionWS(ActivityBase contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                if (Utilities.isNetworkReachable(contexto) && Utilities.isNetworkConnected(contexto)) {

                    if(contexto != null){
                        String installationId = contexto.getInstallationId();
                        String error;

                        if(installationId == null || installationId.trim().equals("")){
                            error = "Error: El id de instalación no está disponible";
                            Log.d("ActivityConfigRuta", error);
                            throw new Exception(error);
                        }else {
                            String respuesta;
                            NetWorkHelper netWorkHelper = new NetWorkHelper();
                            String peticion = SincroHelper.getConfigRutaImeiURL(installationId);
                            respuesta = netWorkHelper.readService(peticion);

                            ControlImeiDTO controlImeiDTO = SincroHelper.procesarImeiJsonObject(respuesta);

                            if( controlImeiDTO != null
                                    && controlImeiDTO.IdCliente != null
                                    && controlImeiDTO.CodigoRuta != null &&
                                    !controlImeiDTO.CodigoRuta.equals("")){

                                SincroResolucion sincroResolucion = SincroResolucion.getInstance();
                                sincroResolucion.setControlImei(controlImeiDTO);
                                sincroResolucion.download(contexto);
                                resolucion = sincroResolucion.getResolucionActualDTO();

                                App.guardarConfiguracionEstadoAplicacion("OK", contexto);

                                Log.d("ControlImeiService", "Se ha re-iniciado el servicio...");
                                Intent ImeiIntent = new Intent(ActivityConfigRuta.this, ControlImeiService.class);
                                startService(ImeiIntent);

                            }else{
                                error = "Error validando el Id de Instalación";
                                Log.d("ActivityConfigRuta", error);
                                throw new Exception(error);
                            }
                        }
                    }else {
                        error = "Error verificando el Id de Instalación: contexto = null";
                        Log.d("ActivityConfigRuta", error);
                        throw new Exception(error);
                    }

                }else{
                    error = "Por favor verifique su conexión a Internet";
                    Log.d("ActivityConfigRuta", error);
                    throw new Exception(error);
                }
            } catch (Exception e) {
                logCaughtException(e);
                error = e.getMessage();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();
            if (error != null) {
                makeErrorDialog(error, ActivityConfigRuta.this);
                App.guardarConfiguracionEstadoAplicacion("R", contexto);
            } else {
                makeDialog("Configuración cargada exitosamente.", ActivityConfigRuta.this);
                cargarResolucion();
            }
        }
    }
}
