package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterClientes;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.ProgramacionAsesorDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 28/06/17.
 */
public class ActivityRuteroAsesor extends ActivityBase {

    private AdapterClientes adaptador;
    private EditText txtFiltro;
    private TextView lblTitulo;
    private RecyclerView recyclerRutero;

    //para el filtro de clientes
    private ClienteDAL clienteDal;
    private ArrayList<ClienteDTO> listadoClientes;
    private int dia;
    private boolean codigoBarrasLeido = false;

    //----------------------------------
    private ArrayAdapter<CharSequence> adapter;

    private String errorProceso;

    //-----------------------------
    Camera camera;
    Camera.Parameters parameters;
    boolean isFlash = false;
    boolean isOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rutero);
            App.actualActivity = this;
            prepareScreen();
            setControls();
            adapter = ArrayAdapter.createFromResource(this, R.array.array_dias,
                    R.layout.actionbar_spinner_dropdown);
            clienteDal = new ClienteDAL(this);
            CargarClientes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarErrorYsalir() {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityRuteroAsesor.this);
        d.setTitle("Error");
        d.setMessage("La aplicación se encuentra bloqueada, por favor configure nuevamente la ruta");
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
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYsalir();
        }
    }

    private void createListNavigationModeOnActionBar(ArrayAdapter<CharSequence> adapter) {

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ab.setListNavigationCallbacks(adapter,
                    new ActionBar.OnNavigationListener() {
                        @Override
                        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                            dia = itemPosition;
                            CargarClientes(dia);
                            txtFiltro.setText("");
                            return false;
                        }
                    });
            ab.setSelectedNavigationItem(dia);
        }

    }

    @Override
    protected void setControls() {
        txtFiltro = findViewById(R.id.txtFiltro2);
        recyclerRutero = findViewById(R.id.recycler_rutero);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerRutero.setLayoutManager(mLayoutManager);

        if (txtFiltro != null) {
            txtFiltro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btn_search, 0, 0, 0);
            txtFiltro.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    filtrarClientes();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }

    private void prepareScreen() {
        ViewGroup view = findViewById(android.R.id.content);
        lblTitulo = (TextView) getLayoutInflater().inflate(R.layout.list_encabezado, view, false);
    }

    private void CargarClientes(int dia) {
        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSelectedNavigationItem(this.dia);
            }

            this.dia = dia;
            String titulo = "Rutero " + App.Dia(dia);
            lblTitulo.setText(titulo);
            listadoClientes = clienteDal.ObtenerListadoAsesor(dia);
            setTitle(listadoClientes);

            loadAdapterData(listadoClientes);

        } catch (Exception e) {
            App.MostrarToast(this, "Error cargando el rutero: " + e.getMessage());
        }
    }

    private void loadAdapterData(ArrayList<ClienteDTO> clientes) {
        adaptador = new AdapterClientes(this, clientes);
        adaptador.setOnClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                clientSelected(position);
            }
        });
        recyclerRutero.setAdapter(adaptador);
    }

    private void CargarClientes() {
        try {
            Calendar calendar = Calendar.getInstance();
            dia = (calendar.get(Calendar.DAY_OF_WEEK) - 1);
            createListNavigationModeOnActionBar(adapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void filtrarClientes() {
        try {
            if (txtFiltro != null && txtFiltro.getText() != null) {
                ArrayList<ClienteDTO> lFiltro = clienteDal.filtrarClientes(
                        listadoClientes, txtFiltro.getText().toString(), dia);
                if (lFiltro == null) lFiltro = new ArrayList<>();
                loadAdapterData(lFiltro);
                setTitle(lFiltro);
            }
        } catch (Exception e) {
            makeErrorDialog("Error cargando el rutero: " + e.getMessage(), ActivityRuteroAsesor.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar_rutero_asesor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuActualizar:
                new CargaRuteroWS(ActivityRuteroAsesor.this).execute();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.mnuFacturaReadBarCode:
                leer_codigo_barras_cliente();
                break;
            default:
                break;
        }
        return true;
    }

    private void leer_codigo_barras_cliente() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    public void clientSelected(int position) {
        try {
            ClienteDTO cliente = adaptador.getClient(position);
            pasar_a_gestion_comercial(cliente);
        } catch (Exception e) {
            App.MostrarToast(this, e.getMessage());
        }

    }

    private void pasar_a_gestion_comercial(ClienteDTO cliente) {
        if (cliente != null) {
            try {
                Intent i = new Intent(this, ActivityGestionComercial.class);
                i.putExtra("IdCliente", cliente.IdCliente);
                i.putExtra("codigoBarrasLeido", codigoBarrasLeido);
                startActivity(i);
            } catch (Exception ex) {
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRuteroAsesor.this);
            }
        }
    }

    private void setTitle(ArrayList<ClienteDTO> lFiltro) {
        setTitle("(" + lFiltro.size() + ")");
    }

    private class CargaRuteroWS extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Estamos actualizando sus clientes");
        }

        Context contexto;

        CargaRuteroWS(Context contexto) {
            this.contexto = contexto;
            errorProceso = "";
        }

        @Override
        protected String doInBackground(String... params) {
            NetWorkHelper netWorkHelper = new NetWorkHelper();
            try {
                String ruteroAsesor = netWorkHelper.readService(
                        SincroHelper.ObtenerRuteroAsesorURL(resolucion.IdCliente, resolucion.CodigoRuta));
                ArrayList<ClienteDTO> clientes = SincroHelper.procesarJsonAsesor(ruteroAsesor);

                ClienteDAL cdal = new ClienteDAL(contexto);
                ProgramacionAsesorDAL pdal = new ProgramacionAsesorDAL(contexto);

                cdal.Eliminar();
                pdal.eliminarTodo();

                for (int i = 0; i < clientes.size(); i++) {
                    ClienteDTO c = clientes.get(i);

                    cdal.Insertar(c);

                    if (c.programacionAsesor != null && c.programacionAsesor.size() > 0) {
                        pdal.Insertar(c.programacionAsesor);
                    }
                }

            } catch (IOException e) {
                errorProceso = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                errorProceso = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                errorProceso = "JSONException: " + e.getMessage();
            } catch (Exception e) {
                errorProceso = "Excepción general: " + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (!errorProceso.equals("")) {
                Utilities.makeDialog(this.contexto, errorProceso);
            } else {
                CargarClientes();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
           IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scaner cancelado", Toast.LENGTH_LONG).show();
            } else {
                String genericError = "No se pudo encontrar el cliente, por favor verifica que el cliente exista en tu rutero";
                try {
                    //Formar= IdCliente.IdClienteTiMovil
                    String decodedData = result.getContents();
                    if (!TextUtils.isEmpty(decodedData)) {
                        String[] decodedDataArray = decodedData.split("\\.");
                        String idCliente = decodedDataArray[0];
                        ClienteDTO cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(idCliente);
                        if (cliente != null && cliente.IdCliente == Integer.parseInt(idCliente)) {
                            codigoBarrasLeido = true;
                            pasar_a_gestion_comercial(cliente);
                        } else {
                            makeErrorDialog(genericError, this);
                        }
                    }
                    Toast.makeText(this, "Scaneado: " + result.getContents(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    makeErrorDialog(genericError, this);
                }
            }
        }
    }
}