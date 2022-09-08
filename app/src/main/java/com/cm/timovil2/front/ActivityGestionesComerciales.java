package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterGestionesComerciales;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.GestionComercialDAL;
import com.cm.timovil2.dto.GestionComercialDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 24/08/2015.
 */
public class ActivityGestionesComerciales extends ActivityBase implements MenuItem.OnMenuItemClickListener, AdapterView.OnItemClickListener {

    private ListView list_gestiones;

    private GestionComercialDAL gestionComercialDAL;

    private ArrayList<GestionComercialDTO> listadoGestiones;

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
        gestionComercialDAL = new GestionComercialDAL(this);
        listadoGestiones = new ArrayList<>();
        setControls();
        addFooter();

        list_gestiones.requestFocus();
        registerForContextMenu(list_gestiones);
    }

    private void mostrarErrorYSalir(String mensaje){
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityGestionesComerciales.this);
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
        }else{
            cargarGestiones();
        }
    }

    @Override
    protected void setControls() {
        list_gestiones = findViewById(R.id.list_gestiones);
        list_gestiones.setOnItemClickListener(this);
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

    private void cargarGestiones() {
        try {
            listadoGestiones.clear();
            listadoGestiones = gestionComercialDAL.getLista();
            AdapterGestionesComerciales adaptador = new AdapterGestionesComerciales(this, listadoGestiones);
            list_gestiones.setAdapter(adaptador);
            setTitle("Registros (" + String.valueOf(listadoGestiones.size()) + ")");
        } catch (Exception e) {
            makeErrorDialog("Error cargando las gestiones: " + e.getMessage(), ActivityGestionesComerciales.this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        try {

            long _idGestion = listadoGestiones.get(position).IdGestion;
            Intent intent = new Intent(this, ActivityVerGestionComercial.class);
            intent.putExtra("IdGestion", _idGestion);
            startActivity(intent);

        } catch (Exception e) {
            App.MostrarToast(this, e.getMessage());
        }

    }

    public boolean onMenuItemClick(MenuItem arg0) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gestiones_comerciales, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuEliminarFacturas:
                eliminarGestiones();
                break;
            case R.id.mnuDescargarFacturasPendientes:
                try {
                    ArrayList<GestionComercialDTO> registros_pendientes = gestionComercialDAL.getListaPendientes();
                    final int pendientes = registros_pendientes.size();
                    if (pendientes > 0) {
                        new DescargaGestiones(this).execute("");
                    } else {
                        makeDialog("No hay registros pendientes por descargar.", ActivityGestionesComerciales.this);
                    }
                } catch (Exception e) {
                    makeDialog("Error descarga", e.getMessage(), ActivityGestionesComerciales.this);
                }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return false;
    }

    private void eliminarGestiones() {
        String mensaje = "";
        ArrayList<GestionComercialDTO> registros_pendientes = gestionComercialDAL.getListaPendientes();
        final int pendientes = registros_pendientes.size();

        if (pendientes > 0) {
            mensaje = "Tiene "
                    + String.valueOf(pendientes)
                    + " registros pendientes por descargar. ";
        }

        mensaje += "Está seguro que desea eliminar todos las registros de gestión comercial?";

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Eliminar gestiones");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                try {
                    if (pendientes > 0) {
                        confirmarPasswordAdmin();
                    } else {
                        eliminarTodasGestiones();
                    }
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityGestionesComerciales.this);
                }
            }
        });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    private void eliminarTodasGestiones() {
        try {
            gestionComercialDAL.eliminarTodo();
            cargarGestiones();
        } catch (Exception e) {
            makeErrorDialog("Error haciendo el conteo de abonos, " + e.getMessage(),ActivityGestionesComerciales.this);
        }
    }

    private void showNoTienePermisos() {
        String mensaje = "No posee los permisos para eliminar los registros de gestión comercial";
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void confirmarPasswordAdmin() {
        Intent intent = new Intent(this, ActivityConfirmarPass.class);
        startActivityForResult(intent, CONFIRMAR_PASS_ADMIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIRMAR_PASS_ADMIN) {
            if (resultCode == RESULT_OK) {
                eliminarTodasGestiones();
            }
            if (resultCode == RESULT_CANCELED) {
                showNoTienePermisos();
            }
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

    private class DescargaGestiones extends AsyncTask<String, String, String> {

        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Enviando las gestiones comerciales");
        }

        ActivityBase contexto;

        private DescargaGestiones(ActivityBase contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder respuesta = new StringBuilder();
            try {
                ArrayList<GestionComercialDTO> listadoPendientes = gestionComercialDAL.getListaPendientes();
                int pendientes = listadoPendientes.size();

                if (pendientes == 0) {
                    respuesta.append("No hay registros pendientes para descargar.\n");
                } else {

                    if (!Utilities.isNetworkReachable(contexto) ||
                            !Utilities.isNetworkConnected(contexto)) {
                        throw new Exception(App.ERROR_CONECTIVIDAD);
                    }

                    if(resolucion != null){

                        String idClienteTiMo = resolucion.IdCliente;
                        String installationId = getInstallationId();

                        NetWorkHelper netWorkHelper = new NetWorkHelper();
                        String url = SincroHelper.INGRESAR_GESTION_COMERCIAL;

                        int cantidad_exitosa = 0;

                        for(GestionComercialDTO g: listadoPendientes){
                            JSONObject jsonGestion = gestionComercialDAL.getJson(g, idClienteTiMo, installationId);
                            String jsonRespuesta = netWorkHelper.writeService(jsonGestion, url);

                            jsonRespuesta = SincroHelper.procesarOkJson(jsonRespuesta);

                            if(jsonRespuesta.equals("OK")){
                                cantidad_exitosa++;
                                respuesta.append(g.Contacto).append(" [").append("OK]").append("\n");
                                gestionComercialDAL.cambiarEstadoSincronizacion(g.IdGestion, true);
                            }else{
                                respuesta.append(g.Contacto).append(" [").append("ERROR]").append("\n");
                            }
                        }

                        respuesta.append("\nSe han descargado ").append(cantidad_exitosa).append(" registros")
                                .append(" satisfactoriamente, ").append(" de un total de ")
                                .append(pendientes).append(" registros totales.");
                    }
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
            return respuesta.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (!msgError.equals("")) {
                makeErrorDialog(msgError, ActivityGestionesComerciales.this);
            } else if (!TextUtils.isEmpty(result)) {
                makeDialog(result, ActivityGestionesComerciales.this);
            }
            cargarGestiones();
        }
    }

    private final static int CONFIRMAR_PASS_ADMIN = 2000;
}
