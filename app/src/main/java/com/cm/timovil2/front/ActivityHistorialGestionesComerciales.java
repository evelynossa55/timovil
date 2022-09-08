package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterGestionesComerciales;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.GestionComercialDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 28/06/17.
 */
public class ActivityHistorialGestionesComerciales extends ActivityBase implements AdapterView.OnItemClickListener {

    private ListView list_gestiones;

    private ArrayList<GestionComercialDTO> listadoGestiones;

    private int idCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gestiones_comerciales);
        App.isFacturasActivityVisible = false;
        App.actualActivity = this;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        listadoGestiones = new ArrayList<>();
        setControls();
        addFooter();

        list_gestiones.requestFocus();
        registerForContextMenu(list_gestiones);

        Intent intent = getIntent();
        idCliente = intent.getIntExtra("IdCliente", -1);
    }

    private void mostrarErrorYSalir(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityHistorialGestionesComerciales.this);
        d.setTitle("Error");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                finish();
            }
        });
        d.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.actualActivity = this;
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYSalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        } else {
            new CargarGestiones(this).execute();
        }
    }

    @Override
    protected void setControls() {
        list_gestiones = findViewById(R.id.list_gestiones);
        list_gestiones.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        try {

            App.vergestionComercialDTO = (GestionComercialDTO) (list_gestiones.getItemAtPosition(position));
            Intent intent = new Intent(this, ActivityVerGestionComercial.class);
            startActivity(intent);

        } catch (Exception e) {
            App.MostrarToast(this, e.getMessage());
        }

    }

    private void addFooter() {
        try {
            Button backToTop = (Button) getLayoutInflater().inflate(R.layout.list_footer, list_gestiones, false);
            if (backToTop != null) {
                backToTop.setCompoundDrawablesWithIntrinsicBounds(getResources()
                        .getDrawable(android.R.drawable.ic_menu_upload), null, null, null);
                backToTop.setBackgroundColor(getResources().getColor(R.color.black));
                backToTop.setTextColor(getResources().getColor(R.color.white));
                list_gestiones.addFooterView(backToTop, null, true);
                backToTop.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        list_gestiones.setSelection(0);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cargarGestiones(ArrayList<GestionComercialDTO> gestiones) {
        try {
            if(gestiones == null) gestiones = new ArrayList<>();
            listadoGestiones.clear();
            listadoGestiones = gestiones;
            AdapterGestionesComerciales adaptador = new AdapterGestionesComerciales(this, listadoGestiones);
            list_gestiones.setAdapter(adaptador);
            setTitle("Registros (" + String.valueOf(listadoGestiones.size()) + ")");
        } catch (Exception e) {
            makeErrorDialog("Error cargando las gestiones: " + e.getMessage(), ActivityHistorialGestionesComerciales.this);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_gestiones) {
            menu.setHeaderTitle("Opciones Gestión Comercial");
            menu.add(Menu.NONE, 1, 1, "Abrir");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int seleccion = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            switch (seleccion) {
                case 1: // Abrir
                    long _idGestion = listadoGestiones.get(info.position).IdGestion;
                    Intent intent = new Intent(this, ActivityVerGestionComercial.class);
                    intent.putExtra("IdGestion", _idGestion);
                    startActivity(intent);
                    break;
            }
        }
        return true;
    }

    private class CargarGestiones extends AsyncTask<String, String, String> {

        ArrayList<GestionComercialDTO> gestiones;
        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Descargando el historial de gestiones");
        }

        ActivityBase contexto;

        private CargarGestiones(ActivityBase contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                if (!Utilities.isNetworkReachable(contexto) ||
                        !Utilities.isNetworkConnected(contexto)) {
                    throw new Exception(App.ERROR_CONECTIVIDAD);
                }

                if (resolucion != null) {

                    NetWorkHelper netWorkHelper = new NetWorkHelper();
                    String url = SincroHelper.ObtenerHistorialGestionComercialURL(
                            resolucion.IdCliente, resolucion.CodigoRuta, idCliente);

                    String jsonRespuesta = netWorkHelper.readService(url);

                    gestiones =
                            SincroHelper.procesarJsonHistorialGestionComercial(jsonRespuesta);

                }


            } catch (IOException e) {
                msgError = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                msgError = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                msgError = "Error JSONException: " + e.getMessage();
            } catch (Exception e) {
                msgError = e.getMessage();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (!msgError.equals("")) {
                makeErrorDialog(msgError, ActivityHistorialGestionesComerciales.this);
            }
            cargarGestiones(gestiones);
        }
    }

}