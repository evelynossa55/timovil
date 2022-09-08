package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.adapters.AdapterPedidos;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.DetallePedidoCallcenterDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.PedidoCallcenterDAL;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResultadoGestionCasoCallcenterDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetallePedidoCallcenterDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.PedidoCallcenterDTO;
import com.cm.timovil2.dto.ResultadoGestionCasoCallcenterDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 20/01/17.
 */
public class ActivityPedidos extends ActivityBase implements AdapterView.OnItemClickListener {

    private ListView list_pedidos;

    private PedidoCallcenterDAL pedido_dal;

    private ArrayList<PedidoCallcenterDTO> listado_pedidos;
    private boolean esNotificacion;
    private ClienteDTO cliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            setContentView(R.layout.pedido_callcenter);

            App.actualActivity = this;

            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }

            pedido_dal = new PedidoCallcenterDAL(this);
            listado_pedidos = new ArrayList<>();

            setControls();
            addFooter();

            list_pedidos.requestFocus();
            list_pedidos.setOnItemClickListener(this);

            esNotificacion = getIntent().getBooleanExtra("esNotificacion", false);

        } catch (Exception e) {
            logCaughtException(e);
            mostrarErrorYsalir(e.getMessage());
        }
    }

    private final static int SELCCIONAR_BODEGA_REQ = 1001;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try {

            PedidoCallcenterDTO pedido = listado_pedidos.get(position);
            App.pedido_actual = pedido;

            //Abrir remision o facturacion
            cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(String.valueOf(pedido.IdCliente));

            if(cliente == null || cliente.IdCliente <= 0){

                AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
                d.setTitle("Error");
                d.setMessage("El pedido es actualmente para un cliente que no se encuentra en su " +
                        "rutero o lista de clientes asignada");
                d.setCancelable(false);
                d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                d.show();
                return;
            }

            int cantidadFacturasClientePorDia = App.obtenerConfiguracion_CantidadFacturasClientePorDia(this);
            int vecesAtendido = cliente.VecesAtendido;

            if (cantidadFacturasClientePorDia > 0 && vecesAtendido >= cantidadFacturasClientePorDia) {
                AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
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

            String[] bodegas = resolucion.Bodegas.split("-");//1:MEDELLIN-2:BOGOTA
            String codigoBodegaAux = bodegas[0].split(":")[0];
            if (bodegas.length > 1) {
                Intent intent = new Intent(this, ActivitySeleccionarBodega.class);
                intent.putExtra("_idCliente", String.valueOf(cliente._Id));
                startActivityForResult(intent, SELCCIONAR_BODEGA_REQ);
            } else {

                if (cliente.Remision) {
                    pasar_a_remision(cliente, codigoBodegaAux);
                } else {
                    pasar_a_facturacion(cliente, codigoBodegaAux);
                }

            }

        } catch (Exception e) {
            logCaughtException(e);
        }
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
                        if(clienteDTO.Remision){
                            pasar_a_remision(clienteDTO, codigoBodegaAux);
                        }else{
                            pasar_a_facturacion(clienteDTO, codigoBodegaAux);
                        }

                    }
                }
            }
        } catch (Exception ex) {
            logCaughtException(ex);
            makeErrorDialog("ERROR: " + ex.getMessage(), ActivityPedidos.this);
        }
    }

    private void pasar_a_facturacion(final ClienteDTO cliente, final String codigoBodega) {
        if (cliente != null && codigoBodega != null && !codigoBodega.equals("")) {
            try {
                App.DetalleFacturacion =
                        new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);

                setDetalle(cliente.ExentoIva);

                if(cantidadNoIgual && resolucion.ManejarInventario){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
                    d.setTitle("Error");
                    d.setMessage("Algunos productos del pedido poseen una " +
                            "cantidad mayor al stock actual, se añadira el total del stock para esos productos");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            Intent i = new Intent(context, ActivityFacturacion.class);
                            i.putExtra("_id", cliente._Id);
                            i.putExtra("codigoBodega", codigoBodega);
                            i.putExtra("isPedidoCallcenter", true);
                            startActivity(i);
                        }
                    });
                    d.show();

                }if(ProductoNoSeEncontro){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
                    d.setTitle("Error");
                    d.setMessage("No se encontraron algunos productos y por lo tanto no saldrán en el pedido");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            Intent i = new Intent(context, ActivityFacturacion.class);
                            i.putExtra("_id", cliente._Id);
                            i.putExtra("codigoBodega", codigoBodega);
                            i.putExtra("isPedidoCallcenter", true);
                            startActivity(i);
                        }
                    });
                    d.show();

                }else{
                    Intent i = new Intent(this, ActivityFacturacion.class);
                    i.putExtra("_id", cliente._Id);
                    i.putExtra("codigoBodega", codigoBodega);
                    i.putExtra("isPedidoCallcenter", true);
                    startActivity(i);
                }

            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityPedidos.this);
            }
        }
    }

    private void pasar_a_remision(final ClienteDTO cliente, final String codigoBodega) {
        if (cliente != null && codigoBodega != null && !codigoBodega.equals("")) {
            try {
                App.DetalleFacturacion =
                        new ProductoDAL(this).obtenerListadoInicialFacturacion(cliente, codigoBodega);

                setDetalle(cliente.ExentoIva);

                if(cantidadNoIgual && resolucion.ManejarInventarioRemisiones){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
                    d.setTitle("Error");
                    d.setMessage("Algunos productos del pedido poseen una " +
                            "cantidad mayor al stock actual, se añadirá el total del stock para esos productos");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            Intent i = new Intent(context, ActivityCrearRemision.class);
                            i.putExtra("_id", cliente._Id);
                            i.putExtra("codigoBodega", codigoBodega);
                            i.putExtra("isPedidoCallcenter", true);
                            startActivity(i);
                        }
                    });
                    d.show();

                }if(ProductoNoSeEncontro){

                    AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
                    d.setTitle("Error");
                    d.setMessage("No se encontraron algunos productos y por lo tanto no saldrán en el pedido");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            Intent i = new Intent(context, ActivityCrearRemision.class);
                            i.putExtra("_id", cliente._Id);
                            i.putExtra("codigoBodega", codigoBodega);
                            i.putExtra("isPedidoCallcenter", true);
                            startActivity(i);
                        }
                    });
                    d.show();

                }else{
                    Intent i = new Intent(this, ActivityCrearRemision.class);
                    i.putExtra("_id", cliente._Id);
                    i.putExtra("codigoBodega", codigoBodega);
                    i.putExtra("isPedidoCallcenter", true);
                    startActivity(i);
                }
            } catch (Exception ex) {
                logCaughtException(ex);
                makeErrorDialog("ERROR: " + ex.getMessage(), ActivityPedidos.this);
            }
        }
    }

    private void mostrarErrorYsalir(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityPedidos.this);
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
            mostrarErrorYsalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        }
        cargarPedidos();
    }

    private void ActualizarPedidos(){
        if(Utilities.isNetworkReachable(this) &&
                Utilities.isNetworkConnected(this)
                && !esNotificacion){
            new CargaPedidosTask().execute();
        }else{
            cargarPedidos();
        }
    }

    @Override
    protected void setControls() {
        list_pedidos = findViewById(R.id.list_pedidos);
    }

    private void addFooter() {
        try {
            Button backToTop = (Button) getLayoutInflater().inflate(R.layout.list_footer, list_pedidos, false);

            if (backToTop != null) {

                backToTop.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(android.R.drawable.ic_menu_upload), null, null, null);
                backToTop.setBackgroundColor(getResources().getColor(R.color.black));
                backToTop.setTextColor(getResources().getColor(R.color.white));
                list_pedidos.addFooterView(backToTop, null, true);
                backToTop.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        list_pedidos.setSelection(0);
                    }
                });

            }

        } catch (Exception ex) {
            logCaughtException(ex);
        }
    }

    private class CargaPedidosTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Actualizando los pedidos");
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                if(resolucion == null) return "Debe realizar la carga de datos nuevamente";

                ArrayList<PedidoCallcenterDTO> listadoPedidos;
                ArrayList<ResultadoGestionCasoCallcenterDTO> listadoMotivosNegativos;

                NetWorkHelper netWorkHelper = new NetWorkHelper();
                String request = SincroHelper.ObtenerPedidosCallcenterURL(resolucion.IdCliente, resolucion.CodigoRuta);
                String respuesta = netWorkHelper.readService(request);
                listadoPedidos = SincroHelper.procesarJsonPedidoCallcenter(respuesta);

                request = SincroHelper.ObtenerMotivosNegativosURL(resolucion.IdCliente, resolucion.CodigoRuta, context.getInstallationId());
                respuesta = netWorkHelper.readService(request);
                listadoMotivosNegativos = SincroHelper.procesarJsonResultadosGestionPedidosCallcenter(respuesta);

                new DetallePedidoCallcenterDAL(context).Eliminar();
                new PedidoCallcenterDAL(context).Eliminar();
                new ResultadoGestionCasoCallcenterDAL(context).Eliminar();

                FacturaDAL facturaDAL = new FacturaDAL(context);
                StringBuilder idCasos = new StringBuilder();
                if(listadoPedidos != null && listadoPedidos.size()>0){
                    for (PedidoCallcenterDTO p:listadoPedidos) {
                        FacturaDTO facturaDTO = facturaDAL.ObtenerPorIdCaso(p.IdCaso);
                        if(facturaDTO == null){
                            new PedidoCallcenterDAL(context).Insertar(p);
                            idCasos.append(p.IdCaso).append("|");
                        }
                    }
                }

                request = SincroHelper.ObtenerNotificaRecepcionPedidoURL(resolucion.IdCliente, idCasos.toString(), resolucion.CodigoRuta);
                respuesta = netWorkHelper.readService(request);
                String respuestaNotificar = SincroHelper.procesarOkJson(respuesta);
                Log.d("PedidosService", "Notificar recibidos: " + idCasos.toString() + " -->" + respuestaNotificar);

                if(listadoMotivosNegativos != null && listadoMotivosNegativos.size() > 0){
                    for (ResultadoGestionCasoCallcenterDTO r: listadoMotivosNegativos) {
                        new ResultadoGestionCasoCallcenterDAL(context).Insertar(r);
                    }
                }

                return "OK";

            } catch (Exception e) {
                logCaughtException(e);
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();
            cargarPedidos();
        }
    }

    private void cargarPedidos() {
        try {

            listado_pedidos.clear();
            listado_pedidos = pedido_dal.ObtenerListado();

            ClienteDAL clienteDAL = new ClienteDAL(this);

            ArrayList<ClienteDTO> clientes = clienteDAL.ObtenerListado();

            if(clientes == null || clientes.size() <= 0){
                mostrarErrorYsalir("Debe cargar los datos antes de realizar pedidos");
            }

            AdapterPedidos adaptador_pedidos = new AdapterPedidos(this, listado_pedidos);
            list_pedidos.setAdapter(adaptador_pedidos);

            setTitle("Pedidos (" + listado_pedidos.size() + ")");

        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog("Error cargando los pedidos: " + e.getMessage(), ActivityPedidos.this);
        }
    }

    private boolean cantidadNoIgual=false;
    private boolean ProductoNoSeEncontro=false;

    private void setDetalle(boolean exentoIva){

        if(App.pedido_actual == null || App.pedido_actual.Detalle == null) return;

         //No es la cantidad anterior debido al stock actual
        for (DetallePedidoCallcenterDTO d : App.pedido_actual.Detalle) {
            DetalleFacturaDTO detalle = buscarDetalle(d.IdProducto);
            if (detalle != null) {
                if(asignarValores(detalle, d.Cantidad, exentoIva, cliente.Remision)){
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

    private boolean asignarValores(DetalleFacturaDTO detalle, int cantidad, boolean exentoIva,
                                   boolean remision) {
        detalle.Cantidad = cantidad;
        detalle.Devolucion = 0;
        detalle.Rotacion = 0;
        boolean stockOverFlowed = false; //La cantidad del pedido anterior sobrepasa el stock actual?
        boolean manejar_inventario = remision ? resolucion.ManejarInventarioRemisiones : resolucion.ManejarInventario;
        try {

            if (detalle.Cantidad > detalle.StockDisponible
                    && manejar_inventario) {
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
            makeErrorDialog(e.getMessage(), ActivityPedidos.this);
        }
        return stockOverFlowed;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actualizar_pedidos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuActualizar:
                ActualizarPedidos();
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