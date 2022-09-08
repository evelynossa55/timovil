package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterRemisionesPorFecha;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemFacturaHeader;
import com.cm.timovil2.bl.utilities.ItemRemisionDetail;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.RemisionDTO;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ActivityRemisiones extends ActivityBase implements MenuItem.OnMenuItemClickListener {

    private RecyclerView recyclerRemisiones;

    private RemisionDAL remisionDal;
    private EditText textoBuscar;
    private ArrayList<RemisionDTO> listadoRemisiones;
    private ArrayList<Item> listadoItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remisiones);
        App.isFacturasActivityVisible = false;
        App.actualActivity = this;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        remisionDal = new RemisionDAL(this);
        listadoRemisiones = new ArrayList<>();

        setControls();

        recyclerRemisiones.requestFocus();
        registerForContextMenu(recyclerRemisiones);
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

    private void mostrarErrorYsalir(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityRemisiones.this);
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
        } else {
            cargarRemisiones();
        }
    }

    @Override
    protected void setControls() {
        recyclerRemisiones = findViewById(R.id.recycler_remisiones);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerRemisiones.setLayoutManager(mLayoutManager);

        textoBuscar=findViewById(R.id.txtBuscar_filtros);

        textoBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cargarRemisionesFiltros();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }



    private ArrayList<Item> cargarListadoEncabezados(ArrayList<RemisionDTO> remisiones) {
        ArrayList<Item> result = new ArrayList<>();
        ArrayList<String> dates_aux = new ArrayList<>();
        SimpleDateFormat dt = new SimpleDateFormat("yyyyyMMdd", Utilities.getLocale());
        ItemFacturaHeader fh;
        ItemRemisionDetail rd;
        for (RemisionDTO f : remisiones) {

            Date fecha = new Date(f.Fecha);
            String format = dt.format(fecha);
            if (!dates_aux.contains(format)) {
                dates_aux.add(format);
                fh = new ItemFacturaHeader();
                fh.setFechaFactura(f.Fecha);
                result.add(fh);

                //Añadir facturas
                for (RemisionDTO fa : remisiones) {
                    if (format.equals(dt.format(new Date(fa.Fecha)))) {
                        rd = new ItemRemisionDetail();
                        rd.setRemisionDTO(fa);
                        result.add(rd);
                    }
                }
            }
        }
        return result;
    }

    private void cargarRemisiones() {
        try {
            DateTime now = DateTime.now();
            listadoRemisiones.clear();
            listadoRemisiones = remisionDal.obtenerListado(true, now, now);
            listadoItems = cargarListadoEncabezados(listadoRemisiones);

            AdapterRemisionesPorFecha adaptadorPorFechas = new AdapterRemisionesPorFecha(listadoItems);
            adaptadorPorFechas.setOnClickListener(new OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //Back to top
                    recyclerRemisiones.smoothScrollToPosition(0);
                }
            });
            recyclerRemisiones.setAdapter(adaptadorPorFechas);
            setTitle("Remisiones (" + String.valueOf(listadoRemisiones.size()) + ")");
        } catch (Exception e) {
            makeErrorDialog("Error cargando las remisiones: " + e.getMessage(), ActivityRemisiones.this);
        }
    }
    private void cargarRemisionesFiltros() {
        try {
            DateTime now = DateTime.now();
            listadoRemisiones.clear();
            listadoRemisiones = remisionDal.obtenerListadoFiltros(true, now, now,textoBuscar.getText().toString());
            listadoItems = cargarListadoEncabezados(listadoRemisiones);

            AdapterRemisionesPorFecha adaptadorPorFechas = new AdapterRemisionesPorFecha(listadoItems);
            adaptadorPorFechas.setOnClickListener(new OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //Back to top
                    recyclerRemisiones.smoothScrollToPosition(0);
                }
            });
            recyclerRemisiones.setAdapter(adaptadorPorFechas);
            setTitle("Remisiones (" + String.valueOf(listadoRemisiones.size()) + ")");
        } catch (Exception e) {
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
        // generar resumen
        try {
            DateTime now = DateTime.now();
            App.ResumenFacturacion.Iniciar();
            new FacturaDAL(ActivityRemisiones.this).obtenerListado(true, now, now);
            remisionDal.obtenerListado(true, now, now);
        } catch (Exception e) {
            Log.d("ResumenesActivity", e.toString());
        }

        Intent intent = new Intent(this, ActivityResumenDiario.class);
        intent.putExtra("resumen", App.ResumenFacturacion.toString());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuEliminarRemisiones:
                eliminarRemisiones();
                break;
            case R.id.mnuDescargarRemisionesPendientes:
                try {
                    if (remisionDal.obtenerListadoPendientes().size() > 0) {
                        new DescargaRemisiones(this).execute("");
                    } else {
                        makeDialog("No hay remisiones pendientes por descargar.", ActivityRemisiones.this);
                        cargarRemisiones();
                    }
                } catch (Exception e) {
                    makeDialog("Error descarga", e.getMessage(), ActivityRemisiones.this);
                }
                break;
            case R.id.mnuResumenFacturas:
                mostrarResumen();
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.mnuImprimirTodas:
                if (App.obtenerConfiguracion_imprimir(this)) {

                    try {
                        DateTime now = DateTime.now();
                        listadoRemisiones.clear();
                        listadoRemisiones = remisionDal.obtenerListado(true, now, now);

                        Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                        printer.printRemisiones(listadoRemisiones);

                    } catch (Exception e) {
                        makeDialog("Error al imprimir", e.getMessage(), ActivityRemisiones.this);
                    }

                } else {
                    makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityRemisiones.this);
                }
                break;
        }
        return false;
    }

    private void eliminarRemisiones() {
        String mensaje = "";
        int facturasPendientes = App.ResumenFacturacion.pendientesSincronizacion;
        int remisionesPendientes = new RemisionDAL(this).obtenerListadoPendientes().size();
        final int pendientes = facturasPendientes + remisionesPendientes;
        if (facturasPendientes > 0) {
            mensaje = "Tiene "
                    + String.valueOf(facturasPendientes)
                    + " facturas pendientes por descargar. ";
        }
        if (remisionesPendientes > 0) {
            if (facturasPendientes > 0) {
                mensaje += " Además de "
                        + String.valueOf(remisionesPendientes)
                        + " remisiones pendientes por descargar. ";
            } else {
                mensaje = "Tiene "
                        + String.valueOf(remisionesPendientes)
                        + " remisiones pendientes por descargar. ";
            }
        }
        mensaje += "Está seguro que desea eliminar todas las facturas y/o remisiones?";

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Eliminar facturas/remisiones");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                try {
                    App.remision_para_eliminar = null;
                    if (pendientes > 0) {
                        confirmarPasswordAdmin();
                    } else {
                        eliminarRemision(null);
                    }
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityRemisiones.this);
                }
            }
        });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    private void eliminarRemision(RemisionDTO rem) {
        try {

            if (resolucion.IdCliente.equals(Utilities.ID_FOGON_PAISA)) {
                makeDialog("No posee permisos para eliminar remisiones.", ActivityRemisiones.this);
                return;
            }

            RemisionDAL dal = new RemisionDAL(ActivityRemisiones.this);
            if (rem == null) {
                new FacturaDAL(this).eliminar();
                dal.eliminar();
                new NotaCreditoFacturaDAL(this).eliminar();
            } else {
                dal.eliminar(rem);
            }
            cargarRemisiones();
            new AbonoFacturaDAL(this).contarAbonosPorRangoFechas(DateTime.now(), DateTime.now());
        } catch (Exception e) {
            makeErrorDialog("Error , " + e.getMessage(), ActivityRemisiones.this);
        }
    }

    private void showNoTienePermisos() {
        String mensaje = "No posee los permisos para eliminar facturas";
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
                eliminarRemision(App.remision_para_eliminar);
            }
            if (resultCode == RESULT_CANCELED) {
                showNoTienePermisos();
            }
        } else if (requestCode == INGRESAR_COMENTARIO_REMISION) {
            if (resultCode == RESULT_OK) {
                if (App.ComentarioFactura != null && !App.ComentarioFactura.equals("")
                        && App.aux_remisionDTO != null) {
                    App.aux_remisionDTO.ComentarioAnulacion = App.ComentarioFactura;
                    App.ComentarioFactura = null;
                    new AnularRemisionTask().execute(App.aux_remisionDTO);
                } else {
                    makeErrorDialog("Debe ingresar un comentario a la remisión para poder anularla.", ActivityRemisiones.this);
                }
            } else {
                makeErrorDialog("Debe ingresar un comentario a la remisión para poder anularla.", ActivityRemisiones.this);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int seleccion = item.getItemId();
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //if (info != null) {
        AdapterRemisionesPorFecha adaptador = (AdapterRemisionesPorFecha) recyclerRemisiones.getAdapter();
        if (adaptador != null) {
            int position = adaptador.getPosition();
            final Item it = listadoItems.get(position);
            if (it.isSection()) {
                return true;
            }

            RemisionDTO rem = ((ItemRemisionDetail) it).getRemisionDTO();
            switch (seleccion) {
                case 1: // Abrir
                    int idRemision = rem.IdAuto;
                    Intent intent = new Intent(this, ActivityRemision.class);
                    intent.putExtra("_Id", String.valueOf(idRemision));
                    startActivity(intent);
                    break;
                case 2: // Anular
                    anularRemision(rem);
                    break;
                case 3: //Imprimir
                    if (App.obtenerConfiguracion_imprimir(this)) {
                        imprimirRemision(rem);
                    } else {
                        makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityRemisiones.this);
                    }
                    break;
                case 4: // Eliminar
                    eliminarUnaRemision(rem);
                    break;
            }
        }
        //}
        return true;
    }

    private void eliminarUnaRemision(final RemisionDTO remision) {
        try {

            //if (!remision.Sincronizada) {
            //  makeDialog("Debe sincronizar la remisión antes de eliminarla.", ActivityRemisiones.this);
            //return;
            //}

            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("Eliminar remisión");
            d.setMessage("Está seguro que desea eliminar la remisión? " + (!remision.Sincronizada ?
                    "La remisión no se encuentra sincronizada y se perderà" : ""));
            d.setCancelable(false);
            d.setPositiveButton("Aceptar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            try {
                                App.remision_para_eliminar = remision;
                                confirmarPasswordAdmin();
                            } catch (Exception e) {
                                makeErrorDialog(e.getMessage(), ActivityRemisiones.this);
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
            makeDialog("TiMovil", "Error eliminado la remisión: " + e.getMessage(), ActivityRemisiones.this);
        }
    }

    private void anularRemision(final RemisionDTO remision) {
        try {

            if (resolucion.IdCliente.equals(Utilities.ID_FOGON_PAISA)
                    || resolucion.IdCliente.equals(Utilities.ID_COMESCOL)) {
                makeDialog("No posee permisos para anular remisiones.", ActivityRemisiones.this);
                return;
            }

            if (remision.Anulada) {
                makeDialog("La remisión ya se encuentra anulada.", ActivityRemisiones.this);
                return;
            }
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("Anular remisión");
            d.setMessage("Está seguro que desea anular la remisión?");
            d.setCancelable(false);
            d.setPositiveButton("Aceptar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            try {

                                App.aux_remisionDTO = remision;
                                Intent i = new Intent(ActivityRemisiones.this, ActivityComentarioFactura.class);
                                i.putExtra("estado", "anulacion");
                                startActivityForResult(i, INGRESAR_COMENTARIO_REMISION);

                            } catch (Exception e) {
                                makeErrorDialog(e.getMessage(), ActivityRemisiones.this);
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
            makeDialog("TiMovil", "Error anulando la remisión: " + e.getMessage(), ActivityRemisiones.this);
        }
    }

    private void imprimirRemision(RemisionDTO remision) {
        try {

            Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
            printer.print(remision);

        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), ActivityRemisiones.this);
        }
    }

    /**
     * =======================================================================
     */
    private class AnularRemisionTask extends AsyncTask<RemisionDTO, String, String> {

        RemisionDTO remision;

        @Override
        protected String doInBackground(RemisionDTO... params) {
            String respuesta;
            remision = params[0];
            try {
                remisionDal.anular(remision);
                respuesta = remisionDal.sincronizarRemision(remision);

                if (respuesta.equals("Sincronizando")) {

                    respuesta = "La remisión ya se estaba sincronizando, por favor intenta nuevamente";

                } else {
                    remision = null;
                }

            } catch (Exception e) {
                respuesta = (e.getMessage());
            }
            return respuesta;
        }

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Anulando la remisión");
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.getDialog().dismiss();

            if (result.equals("OK")) {
                makeDialog(" Remisión anulada correctamente.", ActivityRemisiones.this);
            } else {
                if (result.equals(Utilities.IMEI_ERROR)) {
                    App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                    mostrarErrorYsalir("La aplicación se encuentra " +
                            "bloqueada, por favor configure nuevamente la ruta");
                } else {
                    makeErrorDialog("No se pudo descargar el cambio: " + result, ActivityRemisiones.this);
                }
            }
            cargarRemisiones();
        }
    }

    private class DescargaRemisiones extends AsyncTask<String, String, String> {
        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Enviando remisiones");
        }

        ActivityBase contexto;

        private DescargaRemisiones(ActivityBase contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder respuesta = new StringBuilder();
            try {

                FacturaDAL facturaDal = new FacturaDAL(contexto);
                AbonoFacturaDAL abonoFacturaDal = new AbonoFacturaDAL(contexto);
                GuardarMotivoNoVentaPedidoDAL guardarMotivoNoVentaPedidoDal = new GuardarMotivoNoVentaPedidoDAL(contexto);
                GuardarMotivoNoVentaDAL guardarMotivoNoVentaDal = new GuardarMotivoNoVentaDAL(contexto);
                RemisionDAL remisionDal = new RemisionDAL(contexto);
                NotaCreditoFacturaDAL notaCreditoFacturaDal = new NotaCreditoFacturaDAL(contexto);

                int listadoPendientes = facturaDal.obtenerListadoPendientes().size();
                int listadoAbonoPendientes = abonoFacturaDal.obtenerListadoPendientes().size();
                int listadoNoVentaPedidoPendientes = guardarMotivoNoVentaPedidoDal.obtenerListadoPendientes().size();
                int listadoNoVentaPendientes = guardarMotivoNoVentaDal.obtenerListadoPendientes().size();
                int remPendientes = remisionDal.obtenerListadoPendientes().size();
                int notasCreditoPendientes = notaCreditoFacturaDal.obtenerListadoPendientes().size();

                int totalPendientes =
                        listadoPendientes + listadoAbonoPendientes + listadoNoVentaPedidoPendientes
                                + listadoNoVentaPendientes + remPendientes + notasCreditoPendientes;

                if (totalPendientes > 0) {
                    if (listadoPendientes > 0) {
                        respuesta.append("\nFacturas: ");
                        respuesta.append(facturaDal.descargarFacturas()).append("\n");
                    }
                    if (listadoAbonoPendientes > 0) {
                        respuesta.append("\nCréditos: ");
                        respuesta.append(abonoFacturaDal.descargarAbonos());
                    }
                    if (listadoNoVentaPendientes > 0) {
                        respuesta.append("\nNo venta: ");
                        respuesta.append(guardarMotivoNoVentaDal.descargarPendientes());
                    }
                    if (listadoNoVentaPedidoPendientes > 0) {
                        respuesta.append("\nNo venta, pedidos: ");
                        respuesta.append(guardarMotivoNoVentaPedidoDal.descargarPendientes());
                    }
                    if (remPendientes > 0) {
                        respuesta.append(remisionDal.descargarRemisiones());
                    }
                    if (notasCreditoPendientes > 0) {
                        respuesta.append("\nNotas Crédito: ");
                        respuesta.append(notaCreditoFacturaDal.descargarPendientes());
                    }
                } else {
                    respuesta.append("No hay remisiones pendientes para descargar.\n");
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
                makeErrorDialog(msgError, ActivityRemisiones.this);
            } else if (!TextUtils.isEmpty(result)) {
                makeDialog(result, ActivityRemisiones.this);
            }
            cargarRemisiones();
        }
    }

    private final static int CONFIRMAR_PASS_ADMIN = 10111;
    private final static int INGRESAR_COMENTARIO_REMISION = 10101;
}
