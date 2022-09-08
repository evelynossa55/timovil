package com.cm.timovil2.front;

/*
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 12/06/17.
 */

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
import com.cm.timovil2.bl.adapters.AdapterNoVentasPorFecha;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemFacturaHeader;
import com.cm.timovil2.bl.utilities.ItemNoVentaDetail;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ActivityNoVentas extends ActivityBase implements MenuItem.OnMenuItemClickListener{

    private ListView lsvNoVentas;

    private GuardarMotivoNoVentaPedidoDAL noVentaPedidoDal;
    private GuardarMotivoNoVentaDAL noVentaDal;

    private ArrayList<GuardarMotivoNoVentaDTO> listadoNoVenta;
    private ArrayList<GuardarMotivoNoVentaPedidoDTO> listadoNoVentaPedido;
    private ArrayList<Item> listadoItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_ventas);
        App.isFacturasActivityVisible = false;
        App.actualActivity = this;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        noVentaPedidoDal = new GuardarMotivoNoVentaPedidoDAL(this);
        noVentaDal = new GuardarMotivoNoVentaDAL(this);
        listadoNoVenta = new ArrayList<>();
        listadoNoVentaPedido = new ArrayList<>();

        setControls();
        addFooter();

        lsvNoVentas.requestFocus();
        registerForContextMenu(lsvNoVentas);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.isFacturasActivityVisible = false;
        App.actualActivity = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.isFacturasActivityVisible = false;
        App.actualActivity = null;
    }

    private void mostrarErrorYsalir(String mensaje){
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityNoVentas.this);
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
        }else{
            cargarNoVentas();
        }
    }

    @Override
    protected void setControls() {
        lsvNoVentas = findViewById(R.id.lsvNoVentas);
    }

    private void addFooter() {
        Button backToTop = (Button) getLayoutInflater().inflate(R.layout.list_footer, lsvNoVentas, false);
        if (backToTop != null) {
            backToTop.setCompoundDrawablesWithIntrinsicBounds(getResources()
                    .getDrawable(android.R.drawable.ic_menu_upload), null, null, null);
            backToTop.setBackgroundColor(getResources().getColor(R.color.black));
            backToTop.setTextColor(getResources().getColor(R.color.white));
            lsvNoVentas.addFooterView(backToTop, null, true);
            backToTop.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    lsvNoVentas.setSelection(0);
                }
            });
        }
    }

    private ArrayList<Item> cargarListadoEncabezados(ArrayList<GuardarMotivoNoVentaDTO> noVentas,
                                                     ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPedido){
        ArrayList<Item> result = new ArrayList<>();
        ArrayList<String> dates_aux = new ArrayList<>();
        SimpleDateFormat dt = new SimpleDateFormat("yyyyyMMdd", Locale.US);
        ItemFacturaHeader fh;
        ItemNoVentaDetail rd;

        for(GuardarMotivoNoVentaDTO f: noVentas){

            Date fecha = new Date(f.Fecha_long);
            String format = dt.format(fecha);
            if(!dates_aux.contains(format)){
                dates_aux.add(format);
                fh = new ItemFacturaHeader();
                fh.setFechaFactura(f.Fecha_long);
                result.add(fh);

                //Añadir facturas
                for(GuardarMotivoNoVentaDTO fa: noVentas){
                    if(format.equals(dt.format(new Date(fa.Fecha_long)))){
                        try {
                            ClienteDTO cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(String.valueOf(fa.IdCliente));
                            rd = new ItemNoVentaDetail();
                            rd.setPedido(false);
                            rd.setMotivoNvDTO(fa);
                            rd.setCliente(cliente);
                            result.add(rd);

                        }catch (Exception e){
                            makeErrorDialog("Error cargando las no ventas: " + e.getMessage(), ActivityNoVentas.this);
                        }
                    }
                }
            }
        }

        for(GuardarMotivoNoVentaPedidoDTO f: noVentasPedido){

            Date fecha = new Date(f.Fecha);
            String format = dt.format(fecha);
            if(!dates_aux.contains(format)){
                dates_aux.add(format);
                fh = new ItemFacturaHeader();
                fh.setFechaFactura(f.Fecha);
                result.add(fh);

                //Añadir facturas
                for(GuardarMotivoNoVentaPedidoDTO fa: noVentasPedido){
                    if(format.equals(dt.format(new Date(fa.Fecha)))){
                        ClienteDTO cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(String.valueOf(fa.IdCliente));
                        rd = new ItemNoVentaDetail();
                        rd.setPedido(true);
                        rd.setMotivoNvpDTO(fa);
                        rd.setCliente(cliente);
                        result.add(rd);
                    }
                }
            }
        }

        return result;
    }

    private void cargarNoVentas() {
        try {
            listadoNoVenta.clear();
            listadoNoVentaPedido.clear();
            listadoNoVenta = noVentaDal.ObtenerListado();
            listadoNoVentaPedido = noVentaPedidoDal.ObtenerListado();
            listadoItems = cargarListadoEncabezados(listadoNoVenta, listadoNoVentaPedido);

            AdapterNoVentasPorFecha adaptadorPorFechas = new AdapterNoVentasPorFecha(this, listadoItems);
            lsvNoVentas.setAdapter(adaptadorPorFechas);
            setTitle("Registros (" + String.valueOf(listadoNoVenta.size() + listadoNoVentaPedido.size()) + ")");
        } catch (Exception e) {
            makeErrorDialog("Error cargando las no ventas: " + e.getMessage(), ActivityNoVentas.this);
        }
    }

    public boolean onMenuItemClick(MenuItem arg0) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_remisiones, menu);
        return true;
    }

    private void mostrarResumen() {
        Intent intent = new Intent(this, ActivityResumenDiario.class);
        intent.putExtra("resumen", App.ResumenFacturacion.toString());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuEliminarRemisiones:
                eliminarNoVentas();
                break;
            case R.id.mnuDescargarRemisionesPendientes:
                try {
                    int pendientes = noVentaDal.obtenerListadoPendientes().size() +
                            noVentaPedidoDal.obtenerListadoPendientes().size();
                    if (pendientes > 0) {
                        new DescargaNoVentas(this).execute("");
                    } else {
                        makeDialog("No hay registros pendientes por descargar.", ActivityNoVentas.this);
                    }
                } catch (Exception e) {
                    makeDialog("Error descarga", e.getMessage(), ActivityNoVentas.this);
                }
                break;
            case R.id.mnuResumenFacturas:
                mostrarResumen();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return false;
    }

    private void eliminarNoVentas() {
        String mensaje = "";
        int noVentasPendientes = noVentaDal.obtenerListadoPendientes().size();
        int noVentasPedidoPendientes = noVentaPedidoDal.obtenerListadoPendientes().size();
        final int pendientes = noVentasPendientes + noVentasPedidoPendientes;
        if (pendientes > 0) {
            mensaje = "Tiene "
                    + String.valueOf(pendientes)
                    + " registros de No venta pendientes por descargar. ";
        }

        mensaje += "¿Está seguro que desea eliminar todos los registros de No venta?";

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Eliminar no ventas");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                try {
                    App.remision_para_eliminar = null;
                    if (pendientes > 0) {
                        confirmarPasswordAdmin();
                    } else {
                        eliminarNoVenta(null,null);
                    }
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityNoVentas.this);
                }
            }
        });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    private void eliminarNoVenta(GuardarMotivoNoVentaDTO gmnv, GuardarMotivoNoVentaPedidoDTO gmnvp) {
        try {
            if(gmnv != null){
                noVentaDal.eliminar(gmnv);
            }else if(gmnvp != null){
                noVentaPedidoDal.eliminar(gmnvp);
            }else{
                noVentaDal.Eliminar();
                noVentaPedidoDal.Eliminar();
            }
            cargarNoVentas();
            new AbonoFacturaDAL(this).contarAbonosPorRangoFechas(DateTime.now(), DateTime.now());
        }catch (Exception e){
            makeErrorDialog("Error , " + e.getMessage(), ActivityNoVentas.this);
        }
    }

    private void showNoTienePermisos() {
        String mensaje = "No posee los permisos para eliminar registros";
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
                eliminarNoVenta(App.mnv_para_eliminar, App.mnvp_para_eliminar);
            }

            if (resultCode == RESULT_CANCELED) {
                showNoTienePermisos();
            }

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lsvNoVentas) {
            menu.setHeaderTitle("Opciones no venta");
            menu.add(Menu.NONE, 4, 4, "Eliminar");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int seleccion = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (info != null) {

            final Item it = listadoItems.get(info.position);
            if (it.isSection()) {
                return true;
            }

            GuardarMotivoNoVentaDTO gmnv = ((ItemNoVentaDetail)it).getMotivoNvDTO();
            GuardarMotivoNoVentaPedidoDTO gmnvp = ((ItemNoVentaDetail)it).getMotivoNvpDTO();
            switch (seleccion) {
                case 4: // Eliminar
                    eliminarUnaRemision(gmnv, gmnvp);
                    break;
            }
        }
        return true;
    }

    private void eliminarUnaRemision(final GuardarMotivoNoVentaDTO noVentaDTO,
                             final GuardarMotivoNoVentaPedidoDTO noVentaPedidoDTO) {
        try {


            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("Eliminar no venta");
            d.setMessage("Está seguro que desea eliminar la no venta? ");
            d.setCancelable(false);
            d.setPositiveButton("Aceptar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            try {
                                App.mnv_para_eliminar = noVentaDTO;
                                App.mnvp_para_eliminar = noVentaPedidoDTO;
                                confirmarPasswordAdmin();
                            } catch (Exception e) {
                                makeErrorDialog(e.getMessage(), ActivityNoVentas.this);
                            }
                        }
                    });
            d.setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                        }
                    });
            d.show();

        } catch (Exception e) {
            makeDialog("TiMovil", "Error eliminado la no venta: " + e.getMessage(), ActivityNoVentas.this);
        }
    }

    /**
     * =======================================================================
     */

    private class DescargaNoVentas extends AsyncTask<String, String, String> {
        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Enviando registros de no venta");
        }

        ActivityBase contexto;

        DescargaNoVentas(ActivityBase contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder respuesta = new StringBuilder();
            try {

                ArrayList<GuardarMotivoNoVentaPedidoDTO> listadoNoVentaPedidoPendientes = new
                        GuardarMotivoNoVentaPedidoDAL(getApplicationContext()).obtenerListadoPendientes();

                ArrayList<GuardarMotivoNoVentaDTO> listadoNoVentaPendientes = new
                        GuardarMotivoNoVentaDAL(getApplicationContext()).obtenerListadoPendientes();

                int pendientes = listadoNoVentaPedidoPendientes.size() + listadoNoVentaPendientes.size();

                if (pendientes == 0) {
                    respuesta.append("No hay datos pendientes para descargar.");
                } else {
                    if (pendientes > 0) {

                        if (listadoNoVentaPedidoPendientes.size() > 0) {
                            respuesta.append("\nNo venta: ");
                            respuesta.append(new GuardarMotivoNoVentaPedidoDAL(this.contexto).descargarPendientes());
                        }

                        if (listadoNoVentaPendientes.size() > 0) {
                            respuesta.append("\nNo venta, pedidos: ");
                            respuesta.append(new GuardarMotivoNoVentaDAL(this.contexto).descargarPendientes());
                        }

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
                makeErrorDialog(msgError, ActivityNoVentas.this);
            } else if (!TextUtils.isEmpty(result)) {
                makeDialog(result, ActivityNoVentas.this);
            }
            cargarNoVentas();
        }
    }

    private final static int CONFIRMAR_PASS_ADMIN = 10111;
}
