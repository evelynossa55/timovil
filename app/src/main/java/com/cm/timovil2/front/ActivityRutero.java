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
import com.cm.timovil2.bl.utilities.zebra_datawedge.DWScanner;
import com.cm.timovil2.bl.utilities.zebra_datawedge.OnDataWedgeScannerDecodedData;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.PedidoCallcenterDAL;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetallePedidoCallcenterDTO;
import com.cm.timovil2.dto.PedidoCallcenterDTO;
import com.cm.timovil2.sincro.SincroCliente;
import com.cm.timovil2.sincro.SincroFormaPago;
import com.cm.timovil2.sincro.SincroListaPrecios;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class ActivityRutero extends ActivityBase{

    //-----------------------------
    Camera camera;
    Camera.Parameters parameters;
    boolean isFlash = false;
    boolean isOn = false;

    private AdapterClientes adaptador;
    private EditText txtFiltro;
    private TextView lblTitulo;
    private RecyclerView recyclerRutero;

    //para el filtro de clientes
    private ClienteDAL clienteDal;
    private ArrayList<ClienteDTO> listadoClientes;
    private ClienteDTO _cliente;
    private int dia;

    //----------------------------------
    private ArrayAdapter<CharSequence> diasAdapter;

    private boolean codigoBarrasLeido = false;
    private String errorProceso;
    private final static int SELCCIONAR_BODEGA_REQ = 1000;
    private final static int PASAR_A_VENTA = 1500;

    //----------------------------------
    private DWScanner dataWedge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_rutero);
            App.actualActivity = this;
            prepareScreen();
            setControls();
            diasAdapter = ArrayAdapter.createFromResource(this,
                    R.array.array_dias,
                    R.layout.actionbar_spinner_dropdown);
            clienteDal = new ClienteDAL(this);
            CargarClientes();
            _cliente = null;
        }catch (Exception e){
            logCaughtException(e);
        }
    }

    private boolean facturacionPOS(){
        boolean sw;
        try{

            sw = ( resolucion.PrefijoFacturacionPOS != null &&
                    !TextUtils.isEmpty(resolucion.PrefijoFacturacionPOS) &&
                    resolucion.IdResolucionPOS > 0 && resolucion.SiguienteFacturaPOS > 0 &&
                    resolucion.FacturaFinalPOS != null && !TextUtils.isEmpty(resolucion.FacturaFinalPOS) &&
                    resolucion.FacturaInicialPOS != null && !TextUtils.isEmpty(resolucion.FacturaInicialPOS) &&
                    resolucion.FechaResolucionPOS != null && !TextUtils.isEmpty(resolucion.FechaResolucionPOS) &&
                    resolucion.ResolucionPOS != null && !TextUtils.isEmpty(resolucion.ResolucionPOS)
            );
        }catch (Exception e){
            logCaughtException(e);
            sw = false;
            makeErrorDialog(e.getMessage(), this);
        }
        return sw;
    }

    private void mostrarErrorYsalir(){
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
        d.setTitle("Error");
        d.setMessage("La aplicación se encuentra bloqueada, " +
                "por favor configure nuevamente la ruta");
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                finish();
            }
        });
        d.show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!App.validarEstadoAplicacion(this))
            mostrarErrorYsalir();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(dataWedge != null) dataWedge.stopScanner();
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
            if(actionBar != null){
                actionBar.setSelectedNavigationItem(this.dia);
            }

            this.dia = dia;
            String titulo = "Rutero " + App.Dia(dia);
            lblTitulo.setText(titulo);
            listadoClientes = clienteDal.ObtenerListado(dia);
            setTitle(listadoClientes);

            loadAdapterData(listadoClientes);

        } catch (Exception e) {
            logCaughtException(e);
            App.MostrarToast(this, "Error cargando el rutero: " + e.getMessage());
        }
    }

    private void loadAdapterData(ArrayList<ClienteDTO> clientes){
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
            createListNavigationModeOnActionBar(diasAdapter);
        } catch (Exception ex) {
            logCaughtException(ex);
        }
    }

    private void filtrarClientes() {
        try {
            if (txtFiltro != null && txtFiltro.getText() != null) {
                ArrayList<ClienteDTO> lFiltro = clienteDal.filtrarClientes(
                        listadoClientes, txtFiltro.getText().toString(), dia);
                if(lFiltro == null) lFiltro = new ArrayList<>();
                loadAdapterData(lFiltro);
                setTitle(lFiltro);
            }
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog("Error cargando el rutero: " + e.getMessage(), ActivityRutero.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuActualizar:
                new CargaRuteroWS(ActivityRutero.this).execute("");
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.mnuFacturaPOS:
                pasar_a_facturacion_POS();
                break;
            case R.id.mnuFacturaReadBarCode:
                leer_codigo_barras_cliente();
                break;
            default:
                break;
        }
        return true;
    }

    public void clientSelected(int position){
        try {

            _cliente = adaptador.getClient(position);
            if(_cliente.ObligatorioCodigoBarra){
                leer_codigo_barras_cliente();
            }else{
                vender_a_cliente(_cliente);
            }
        } catch (Exception e) {
            logCaughtException(e);
            App.MostrarToast(this, e.getMessage());
        }
    }

    private void vender_a_cliente(final ClienteDTO cliente){

        String tipoRuta = App.obtenerConfiguracion_tipoRuta(this);

        App.pedido_actual = null;
        if(resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)){

            PedidoCallcenterDAL pedido_dal = new PedidoCallcenterDAL(this);
            PedidoCallcenterDTO pedido = pedido_dal.ObtenerPorCliente(cliente.IdCliente);

            if(pedido != null){

                App.pedido_actual = pedido;
                AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
                d.setTitle("Pedido");
                d.setMessage("El cliente tiene un pedido pendiente, éste se cargará en la pantalla");
                d.setCancelable(false);
                d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        String[] bodegas = resolucion.Bodegas.split("-");//1:MEDELLIN-2:BOGOTA
                        String codigoBodegaAux = bodegas[0].split(":")[0];
                        if (bodegas.length > 1) {
                            Intent intent = new Intent(ActivityRutero.this, ActivitySeleccionarBodega.class);
                            intent.putExtra("_idCliente", String.valueOf(cliente._Id));
                            startActivityForResult(intent, SELCCIONAR_BODEGA_REQ);
                        } else {
                            if(cliente.Remision){
                                pasar_a_remision_pedido(cliente, codigoBodegaAux);
                            }else{
                                pasar_a_facturacion_pedido(cliente, codigoBodegaAux);
                            }
                        }
                    }
                });
                d.show();
                return;
            }
        }

        int cantidadFacturasClientePorDia = App.obtenerConfiguracion_CantidadFacturasClientePorDia(this);
        int vecesAtendido = cliente.VecesAtendido;

        if(cantidadFacturasClientePorDia > 0
                && vecesAtendido >= cantidadFacturasClientePorDia){
            AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
            d.setTitle("Error");
            d.setMessage("De momento no es posible atender a un cliente más de "
                    + cantidadFacturasClientePorDia + " vez(veces)");
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                }
            });
            d.show();
            return;
        }

        if (tipoRuta.equals("Vendedor")) {
            String[] bodegas = resolucion.Bodegas.split("-");//1:MEDELLIN-2:BOGOTA
            String codigoBodegaAux = bodegas[0].split(":")[0];
            if (bodegas.length > 1) {
                Intent intent = new Intent(this, ActivitySeleccionarBodega.class);
                intent.putExtra("_idCliente", String.valueOf(cliente._Id));
                startActivityForResult(intent, SELCCIONAR_BODEGA_REQ);
            } else {
                if(cliente.Remision){
                    pasar_a_remision(cliente, codigoBodegaAux);
                }else{
                    pasar_a_facturacion(cliente, codigoBodegaAux);
                }
            }
        }else if(tipoRuta.equals("Asesor comercial")){
            pasar_a_gestion_comercial(cliente);
        }
    }

    private void pasar_a_facturacion_POS() {
        try {

            if(!facturacionPOS()) {
                makeErrorDialog("No está habilitado para crear facturas POS", this);
                return;
            }

            String[] bodegas = resolucion.Bodegas.split("-");//1:MEDELLIN-2:BOGOTA
            String codigoBodega = bodegas[0].split(":")[0];

            if (bodegas.length > 1) {
                Intent intent = new Intent(this, ActivitySeleccionarBodega.class);
                intent.putExtra("_idCliente", resolucion.IdCliente);
                startActivityForResult(intent, SELCCIONAR_BODEGA_REQ);
            } else {
                ClienteDTO cliente = new ClienteDTO();
                cliente.IdCliente = -1;
                cliente.IdListaPrecios = -1;
                App.DetalleFacturacion = new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);
                start_facturacion_intent(Integer.parseInt(resolucion.IdCliente), codigoBodega, true, false);
            }

        } catch (Exception ex) {
            logCaughtException(ex);
            makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
        }
    }

    private void pasar_a_facturacion(ClienteDTO cliente, String codigoBodega) {
        if (cliente != null && codigoBodega != null && !codigoBodega.equals("")) {
            try {
                App.DetalleFacturacion =
                        new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);
                start_facturacion_intent(cliente._Id, codigoBodega, false, false);
            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
            }
        }
    }

    private void pasar_a_facturacion_pedido(final ClienteDTO cliente, final String codigoBodega) {
        if (cliente != null && codigoBodega != null && !codigoBodega.equals("")) {
            try {
                App.DetalleFacturacion =
                        new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);
                setDetalle(cliente.ExentoIva);

                if(cantidadNoIgual && resolucion.ManejarInventario){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
                    d.setTitle("Error");
                    d.setMessage("Algunos productos del pedido poseen una " +
                            "cantidad mayor al stock actual, se añadira el total del stock para esos productos");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            start_facturacion_intent(cliente._Id, codigoBodega, false, true);
                        }
                    });
                    d.show();

                }if(ProductoNoSeEncontro){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
                    d.setTitle("Error");
                    d.setMessage("No se encontraron algunos productos y por lo tanto no saldrán en el pedido");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            start_facturacion_intent(cliente._Id, codigoBodega, false, true);
                        }
                    });
                    d.show();

                }else{
                    start_facturacion_intent(cliente._Id, codigoBodega, false, true);
                }

            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
            }
        }
    }

    private void pasar_a_gestion_comercial(ClienteDTO cliente) {
        if (cliente != null ) {
            try {
                Intent i = new Intent(this, ActivityGestionComercial.class);
                i.putExtra("IdCliente", cliente.IdCliente);
                startActivity(i);
            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
            }
        }
    }

    private void pasar_a_remision(ClienteDTO cliente, String codigoBodega) {
        if (cliente != null && codigoBodega != null && !codigoBodega.equals("")) {
            try {
                App.DetalleFacturacion =
                        new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);
                start_remision_intent(cliente._Id, codigoBodega, false);
            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
            }
        }
    }

    private void pasar_a_remision_pedido(final ClienteDTO cliente, final String codigoBodega) {
        if (cliente != null && codigoBodega != null && !codigoBodega.equals("")) {
            try {
                App.DetalleFacturacion =
                        new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);

                setDetalle(cliente.ExentoIva);

                if(cantidadNoIgual && resolucion.ManejarInventarioRemisiones){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
                    d.setTitle("Error");
                    d.setMessage("Algunos productos del pedido poseen una " +
                            "cantidad mayor al stock actual, se añadira el total del stock para esos productos");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            start_remision_intent(cliente._Id, codigoBodega, true);
                        }
                    });
                    d.show();

                }if(ProductoNoSeEncontro){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityRutero.this);
                    d.setTitle("Error");
                    d.setMessage("No se encontraron algunos productos y por lo tanto no saldrán en el pedido");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            start_remision_intent(cliente._Id, codigoBodega, true);
                        }
                    });
                    d.show();

                }else{
                    start_remision_intent(cliente._Id, codigoBodega, true);
                }
            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
            }
        }
    }

    private void leer_codigo_barras_cliente(){
        String scannerType = App.obtenerPreferencias_ScannerType(this);
        switch (scannerType){
            case DWScanner.CAMERA_TYPE:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
                break;
            case DWScanner.ZEBRAESCANER_TYPE:
                dataWedge = new DWScanner(this);
                dataWedge.setDecodedDataListener(new OnDataWedgeScannerDecodedData() {
                    @Override
                    public void onDecodedData(String decodedData) {
                        if(!TextUtils.isEmpty(decodedData)){
                            dataWedge.stopScanner();
                            txtFiltro.setText("");
                            Context context = getApplicationContext();
                            String genericError = "No se pudo encontrar el cliente, por favor verifica que el cliente exista en tu rutero";
                            String[] decodedDataArray = decodedData.split("\\.");
                            String idCliente = decodedDataArray[0];
                            ClienteDTO cliente = new ClienteDAL(context).ObtenerClientePorIdCliente(idCliente);
                            if(cliente != null && cliente.IdCliente == Integer.parseInt(idCliente)){
                                codigoBarrasLeido = true;
                                vender_a_cliente(cliente);
                            }else{
                                makeErrorDialog(genericError, ActivityRutero.this);
                            }
                        }
                    }
                });

                dataWedge.createProfile();
                dataWedge.setDecoders();
                break;
        }
    }

    private void start_facturacion_intent(int idCliente,
                                          String codigoBodega,
                                          boolean pos,
                                          boolean isPedidoCallCenter){
        Intent i = new Intent(this, ActivityFacturacion.class);
        i.putExtra("_id", idCliente);
        i.putExtra("codigoBodega", codigoBodega);
        i.putExtra("pos", pos);
        i.putExtra("isPedidoCallcenter", isPedidoCallCenter);
        i.putExtra("codigoBarrasLeido", codigoBarrasLeido);
        startActivityForResult(i, PASAR_A_VENTA);
    }

    private void start_remision_intent(int idCliente,
                                       String codigoBodega,
                                       boolean isPedidoCallCenter){
        Intent i = new Intent(this, ActivityCrearRemision.class);
        i.putExtra("_id", idCliente);
        i.putExtra("codigoBodega", codigoBodega);
        i.putExtra("isPedidoCallcenter", isPedidoCallCenter);
        i.putExtra("codigoBarrasLeido", codigoBarrasLeido);
        startActivityForResult(i, PASAR_A_VENTA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == SELCCIONAR_BODEGA_REQ) {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    assert extras != null;
                    String codigoBodegaAux = extras.getString("codigoBodega");
                    String _idCliente = extras.getString("_idCliente");

                    if(resolucion.IdCliente.equals(String.valueOf(_idCliente))){

                        ClienteDTO cliente = new ClienteDTO();
                        cliente.IdCliente = -1;
                        cliente.IdListaPrecios = -1;
                        App.DetalleFacturacion = new ProductoDAL(this)
                                .obtenerListadoInicialFacturacion(cliente, codigoBodegaAux);
                        Intent i = new Intent(this, ActivityFacturacion.class);
                        i.putExtra("_id", resolucion.IdCliente);
                        i.putExtra("codigoBodega", codigoBodegaAux);
                        i.putExtra("pos", true);
                        startActivity(i);

                    }else{

                        ClienteDTO clienteDTO = new ClienteDAL(this).
                                ObtenerClientePorId(Integer.parseInt(_idCliente));

                        if(App.pedido_actual != null){
                            if(clienteDTO.Remision){
                                pasar_a_remision_pedido(clienteDTO, codigoBodegaAux);
                            }else{
                                pasar_a_facturacion_pedido(clienteDTO, codigoBodegaAux);
                            }
                        }else
                        if(clienteDTO.Remision){
                            pasar_a_remision(clienteDTO, codigoBodegaAux);
                        }else{
                            pasar_a_facturacion(clienteDTO, codigoBodegaAux);
                        }

                    }
                }
            } else if(requestCode == PASAR_A_VENTA) {
                if(resultCode == RESULT_OK) CargarClientes(dia);
            }else{
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(this, "Scaner cancelado", Toast.LENGTH_LONG).show();
                    } else {
                        String genericError = "No se pudo encontrar el cliente, por favor verifica que el cliente exista en tu rutero";
                        try{
                            //Formar= IdCliente.IdClienteTiMovil
                            String decodedData = result.getContents();
                            if(!TextUtils.isEmpty(decodedData)){
                                String[] decodedDataArray = decodedData.split("\\.");
                                String idCliente = decodedDataArray[0];
                                ClienteDTO cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(idCliente);
                                if(cliente != null && cliente.IdCliente == Integer.parseInt(idCliente)){
                                    codigoBarrasLeido = true;
                                    vender_a_cliente(cliente);
                                }else{
                                    makeErrorDialog(genericError, this);
                                }
                            }
                            Toast.makeText(this, "Scaneado: " + result.getContents(), Toast.LENGTH_LONG).show();
                        }catch (Exception e){
                            makeErrorDialog(genericError, this);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logCaughtException(ex);
            makeErrorDialog("ERROR: " + ex.getMessage(), ActivityRutero.this);
        }
    }

    private void setTitle(ArrayList<ClienteDTO> lFiltro) {
        setTitle("(" + lFiltro.size() + ")");
    }

    private class CargaRuteroWS extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(ActivityRutero.this, "Estamos actualizando sus clientes");
        }

        ActivityBase contexto;

        private CargaRuteroWS(ActivityBase contexto) {
            this.contexto = contexto;
            errorProceso = "";
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                //Descargar clientes
                SincroCliente sincroCliente = SincroCliente.getInstance();
                sincroCliente.download(contexto);

                //Descargar formas de pago
                SincroFormaPago sincroFormaPago = SincroFormaPago.getInstance();
                sincroFormaPago.download(contexto);

                //Descargar Listas de precios
                SincroListaPrecios sincroListaPrecios = SincroListaPrecios.getInstance();
                sincroListaPrecios.download(contexto);

            } catch (IOException e) {
                logCaughtException(e);
                errorProceso = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                logCaughtException(e);
                errorProceso = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                logCaughtException(e);
                errorProceso = "JSONException: " + e.getMessage();
            } catch (Exception e) {
                logCaughtException(e);
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

    //------CALCULO DEL PEDIDO-------------
    private boolean cantidadNoIgual=false;
    private boolean ProductoNoSeEncontro=false;
    private void setDetalle(boolean exentoIva){

        if(App.pedido_actual == null || App.pedido_actual.Detalle == null) return;

        //No es la cantidad anterior debido al stock actual
        for (DetallePedidoCallcenterDTO d : App.pedido_actual.Detalle) {
            DetalleFacturaDTO detalle = buscarDetalle(d.IdProducto);
            if (detalle != null) {
                if(asignarValores(detalle, d.Cantidad, exentoIva)){
                    cantidadNoIgual = true;
                }
            }else{
                ProductoNoSeEncontro = true;
            }
        }

    }

    private DetalleFacturaDTO buscarDetalle(int idProd) {
        DetalleFacturaDTO dto = null;
        for (DetalleFacturaDTO detalle : App.DetalleFacturacion) {
            if (detalle.IdProducto == idProd) {
                dto = detalle;
                break;
            }
        }
        return dto;
    }

    private boolean asignarValores(DetalleFacturaDTO detalle, int cantidad, boolean exentoIva) {
        detalle.Cantidad = cantidad;
        boolean stockOverFlowed = false; //La cantidad del pedido anterior sobrepasa el stock actual?
        boolean manejarInventario = _cliente !=  null && _cliente.Remision
                ? resolucion.ManejarInventarioRemisiones : resolucion.ManejarInventario;
        try {

            if (detalle.Cantidad > detalle.StockDisponible && manejarInventario) {
                stockOverFlowed = true;
                detalle.Cantidad = detalle.StockDisponible;
            }
            int cantidadVenta = detalle.Cantidad;
            if (detalle.ValorUnitario > 0 && cantidadVenta > 0) {
                detalle.Subtotal = detalle.ValorUnitario * cantidadVenta;// detalle.Cantidad;
                detalle.Descuento = detalle.Subtotal
                        * (detalle.PorcentajeDescuento / 100);

                if(!exentoIva){
                    detalle.Iva = (detalle.Subtotal - detalle.Descuento)
                            * (detalle.PorcentajeIva / 100);
                }else{
                    detalle.Iva = 0;
                }

                detalle.Total = detalle.Subtotal - detalle.Descuento
                        + (detalle.IpoConsumo * detalle.Cantidad)
                        + detalle.Iva;
            } else {
                detalle.Subtotal = 0;
                detalle.Descuento = 0;
                detalle.Iva = 0;
                detalle.Total = 0;
                detalle.Cantidad = 0;
                detalle.Devolucion = 0;
                detalle.Rotacion = 0;
            }
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityRutero.this);
        }
        return stockOverFlowed;
    }
}