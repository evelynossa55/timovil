package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterCreditos;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.FacturaCreditoDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.wsentities.MAbono;
import com.cm.timovil2.dto.wsentities.MFactCredito;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class ActivityCreditos extends ActivityBase implements OnMenuItemClickListener {

    private RecyclerView recyclerFacturas;
    private FacturaCreditoDAL facturaDal;
    private ArrayList<MFactCredito> listadoFacturas;
    private EditText textoBuscar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facturas);

        try{
            facturaDal = new FacturaCreditoDAL(this);
            listadoFacturas = facturaDal.obtenerListadoConSaldo(true);
        }catch (Exception e){
            makeErrorDialog("Error cargando los créditos, "  + e.getMessage(), ActivityCreditos.this);
        }

        setControls();
        App.actualActivity = this;
        cargarCreditos();

        recyclerFacturas.requestFocus();
        registerForContextMenu(recyclerFacturas);
    }

    private void mostrarErrorYsalir(String mensaje){
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityCreditos.this);
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

    private boolean puedeManejarRecaudoCredito(){
        try{
            boolean manejarRecaudoCredito = App.obtenerConfiguracion_ManejarRecaudoCredito(this);
            if(!manejarRecaudoCredito){
                facturaDal.eliminar();
            }
            return manejarRecaudoCredito;
        }catch (Exception e){
            makeErrorDialog("Error cargando los créditos, "  + e.getMessage(), ActivityCreditos.this);
            return false;
        }
    }

    @Override
    protected void onResume() {
        try{
            super.onResume();
            if (!App.validarEstadoAplicacion(this)) {
                mostrarErrorYsalir("La aplicación se encuentra bloqueada, " +
                        "por favor configure nuevamente la ruta");
            }else {

                if(puedeManejarRecaudoCredito())
                {
                    listadoFacturas = facturaDal.obtenerListadoConSaldo(true);
                    cargarCreditos();
                }else{
                    mostrarErrorYsalir("No tiene permitido manejar recaudo de créditos");
                }
            }
        }catch (Exception e){
            makeErrorDialog("Error cargando los créditos, "  + e.getMessage(), ActivityCreditos.this);
        }
    }

    @Override
    protected void setControls() {
        recyclerFacturas = findViewById(R.id.recycler_facturas);
        textoBuscar=findViewById(R.id.txtBuscar_filtros);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerFacturas.setLayoutManager(mLayoutManager);
        textoBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cargarCreditosFiltros();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });


    }
    private void cargarCreditosFiltros() {
        try {
            ArrayList<MFactCredito> lFiltro = facturaDal.obtenerListadoConFiltros(true,textoBuscar.getText().toString());

                AdapterCreditos adaptador = new AdapterCreditos(this, lFiltro);
                adaptador.setOnClickListener(new OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //Back to top
                        recyclerFacturas.smoothScrollToPosition(0);
                    }
                });
                recyclerFacturas.setAdapter(adaptador);
                setTitle("Créditos (" + String.valueOf(lFiltro.size()) + ")");



        } catch (Exception e) {
        }
    }
    private void cargarCreditos() {
        try {
            ArrayList<MFactCredito> lFiltro = facturaDal.obtenerListadoConSaldo(true);
            AdapterCreditos adaptador = new AdapterCreditos(this, lFiltro);
            adaptador.setOnClickListener(new OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //Back to top
                    recyclerFacturas.smoothScrollToPosition(0);
                }
            });
            recyclerFacturas.setAdapter(adaptador);
            setTitle("Créditos (" + String.valueOf(lFiltro.size()) + ")");
        } catch (Exception e) {
            makeErrorDialog("Error cargando los créditos: " + e.getMessage(), ActivityCreditos.this);
        }
    }

    public boolean onMenuItemClick(MenuItem arg) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_creditos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuEliminarCreditos:
                eliminarCreditos();
                break;
            case R.id.mnuDescargarCreditosPendientes:
                new DescargaFacturas(this).execute("");
                break;
            default:
                break;
        }
        return false;
    }

    private void eliminarCreditos() {
        String mensaje = "";
        int size = new AbonoFacturaDAL(this).obtenerListadoPendientes().size();
        if (size > 0) {
            mensaje = "Tiene "
                    + String.valueOf(size)
                    + " abonos pendientes por descargar. ";
        }
        mensaje += "Esté seguro que desea eliminar todos los créditos?";
        Builder d = new Builder(this);
        d.setTitle("Eliminar créditos");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                try {
                    new FacturaCreditoDAL(ActivityCreditos.this).eliminar();
                    new AbonoFacturaDAL(ActivityCreditos.this).eliminar();
                    cargarCreditos();
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityCreditos.this);
                }
            }
        });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int seleccion = item.getItemId();
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //if (info != null) {
        AdapterCreditos adapterCreditos = (AdapterCreditos) recyclerFacturas.getAdapter();
        if(adapterCreditos != null) {
            int position = adapterCreditos.getPosition();
            switch (seleccion) {
                case 1: // Registrar pago
                    Intent i = new Intent(this, ActivityRegistroAbono.class);
                    i.putExtra("_Id", listadoFacturas.get(position)._Id);
                    startActivity(i);
                    break;
                case 2:
                    if (App.obtenerConfiguracion_imprimir(this)) {
                        try {
                            MFactCredito factCredito = listadoFacturas.get(position);
                            Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                            printer.print(factCredito);

                        } catch (Exception e) {
                            makeErrorDialog("Error imprimiendo el credito", ActivityCreditos.this);
                        }
                    } else {
                        makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityCreditos.this);
                    }
                    break;
            }
        }
        //}
        return true;
    }

    private class DescargaFacturas extends AsyncTask<String, String, String> {
        String msgError = "";

        FacturaDAL facturaDal;

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Descargando facturas");
        }

        ActivityBase contexto;

        private DescargaFacturas(ActivityBase contexto) {
            this.contexto = contexto;
            facturaDal = new FacturaDAL(this.contexto);
        }

        @Override
        protected String doInBackground(String... params) {
            String respuesta = "";
            try {
                publishProgress("Datos pendientes",
                        "Conectando con el servidor...");
                ArrayList<FacturaDTO> listadoPendientes = new FacturaDAL(contexto).obtenerListadoPendientes();
                ArrayList<MAbono> listadoAbonoPendientes = new AbonoFacturaDAL(contexto).obtenerListadoPendientes();
                int pendientes = listadoPendientes.size()
                        + listadoAbonoPendientes.size();
                if (pendientes == 0) {
                    respuesta = "No hay datos pendientes para descargar.";
                } else {
                    if (listadoPendientes.size() > 0) {
                        publishProgress("Descargando facturas pendientes", "...");
                        respuesta = facturaDal.descargarFacturas();
                    }
                    if (listadoAbonoPendientes.size() > 0) {
                        publishProgress("Descargando abonos pendientes", "...");
                        respuesta += "\nCréditos: ";
                        respuesta += (new AbonoFacturaDAL(this.contexto).descargarAbonos());
                    }
                }
            } catch (IOException e) {
                publishProgress("Error IO", e.getMessage());
                msgError = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                publishProgress("Error XML", e.getMessage());
                msgError = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                publishProgress("JSONException", e.getMessage());
                msgError = "Error JSONException: " + e.getMessage();
            } catch (Exception e) {
                publishProgress("Exception", e.getMessage());
                msgError = "Exception: " + e.getMessage();
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (!msgError.equals("")) {
                makeErrorDialog(msgError, ActivityCreditos.this);
            } else {
                if (!TextUtils.isEmpty(result)) {
                    makeDialog(result, ActivityCreditos.this);
                }
                cargarCreditos();
            }
        }
    }
}