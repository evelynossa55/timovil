package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterNotasCreditoPorFecha;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemNotaCreditoDetail;
import com.cm.timovil2.bl.utilities.ItemNotaCreditoHeader;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 14/03/18.
 */

public class ActivityNotasCredito extends ActivityBase {

    private RecyclerView recyclerViewNotasCredito;
    private NotaCreditoFacturaDAL notaCreditoFacturaDAL;

    private ArrayList<NotaCreditoFacturaDTO> listadoNotas;
    private ArrayList<Item> listadoItems;
    private EditText textoBuscar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas_credito);
        App.isFacturasActivityVisible = false;
        App.actualActivity = this;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        notaCreditoFacturaDAL = new NotaCreditoFacturaDAL(this);
        listadoNotas = new ArrayList<>();
        setControls();
        recyclerViewNotasCredito.requestFocus();
        registerForContextMenu(recyclerViewNotasCredito);

    }

    @Override
    protected void onResume() {
        super.onResume();
        App.isFacturasActivityVisible = true;
        App.actualActivity = this;
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYsalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        } else {
            cargarNotas();
        }
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
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityNotasCredito.this);
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

    private void cargarNotas() {
        try {
            DateTime now = DateTime.now();
            listadoNotas.clear();
            listadoNotas = notaCreditoFacturaDAL.obtenerListado(true, now, now);
            listadoItems = cargarListadoEncabezados(listadoNotas);

            AdapterNotasCreditoPorFecha adaptadorPorFechas = new AdapterNotasCreditoPorFecha(listadoItems);
            recyclerViewNotasCredito.setAdapter(adaptadorPorFechas);

            setTitle("Notas (" + String.valueOf(listadoNotas.size()) + ")");
        } catch (Exception e) {
            makeErrorDialog("Error cargando las facturas: " + e.getMessage(), ActivityNotasCredito.this);
        }
    }

    private void cargarNotas2() {
        try {
            DateTime now = DateTime.now();
            listadoNotas.clear();
            listadoNotas = notaCreditoFacturaDAL.obtenerListadoFiltros(true, now, now,textoBuscar.getText().toString());
            listadoItems = cargarListadoEncabezados(listadoNotas);

            AdapterNotasCreditoPorFecha adaptadorPorFechas = new AdapterNotasCreditoPorFecha(listadoItems);
            recyclerViewNotasCredito.setAdapter(adaptadorPorFechas);

            setTitle("Notas (" + String.valueOf(listadoNotas.size()) + ")");
        } catch (Exception e) {
        }
    }
    private ArrayList<Item> cargarListadoEncabezados(ArrayList<NotaCreditoFacturaDTO> notas) {
        ArrayList<Item> result = new ArrayList<>();
        ArrayList<String> dates_aux = new ArrayList<>();
        SimpleDateFormat dt = new SimpleDateFormat("yyyyyMMdd", Locale.US);
        ItemNotaCreditoHeader nh;
        ItemNotaCreditoDetail nd;
        for (NotaCreditoFacturaDTO n : notas) {

            Date fecha = new Date(n.Fecha);
            String format = dt.format(fecha);
            if (!dates_aux.contains(format)) {
                dates_aux.add(format);
                nh = new ItemNotaCreditoHeader();
                nh.setFechaNota(n.Fecha);
                result.add(nh);

                //Añadir notas
                for (NotaCreditoFacturaDTO na : notas) {
                    if (format.equals(dt.format(new Date(na.Fecha)))) {
                        nd = new ItemNotaCreditoDetail();
                        nd.setNotaCreditoFacturaDTO(na);
                        result.add(nd);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void setControls() {
        recyclerViewNotasCredito = findViewById(R.id.recycler_notascredito);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerViewNotasCredito.setLayoutManager(mLayoutManager);
        textoBuscar=findViewById(R.id.txtBuscar_filtros);

        textoBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cargarNotas2();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notas_credito, menu);
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int seleccion = item.getItemId();

        AdapterNotasCreditoPorFecha adapterFacturasPorFecha = (AdapterNotasCreditoPorFecha)recyclerViewNotasCredito.getAdapter();
        if(adapterFacturasPorFecha != null) {

            int position = adapterFacturasPorFecha.getPosition();
            final Item it = listadoItems.get(position);

            if (it.isSection()) {
                return true;
            }
            final NotaCreditoFacturaDTO nota = ((ItemNotaCreditoDetail) it).getNotaCreditoFacturaDTO();

            switch (seleccion) {
                case 1: // Abrir
                    int _id_nota = nota.IdNotaCreditoFactura;
                    Intent intent = new Intent(this, ActivityNotaCredito.class);
                    intent.putExtra("_Id_nota", String.valueOf(_id_nota));
                    startActivity(intent);
                    break;
                case 3: //Imprimir
                    if (App.obtenerConfiguracion_imprimir(this)) {
                        imprimirNota(nota);
                    } else {
                        makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityNotasCredito.this);
                    }
                    break;
            }
        }
        //}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuEliminarFacturas:
                eliminarNotas();
                break;
            case R.id.mnuDescargarFacturasPendientes:
                try {
                    int facturasPendientes = new FacturaDAL(this).obtenerListadoPendientes().size();
                    int remisionesPendientes = new RemisionDAL(this).obtenerListadoPendientes().size();
                    int noVentaPedidos = new GuardarMotivoNoVentaPedidoDAL(this).obtenerListadoPendientes().size();
                    int noVenta = new GuardarMotivoNoVentaDAL(this).obtenerListadoPendientes().size();
                    int notasCredito = new NotaCreditoFacturaDAL(this).obtenerListadoPendientes().size();

                    final int pendientes = facturasPendientes + remisionesPendientes
                            + noVentaPedidos + noVenta + notasCredito;

                    if (pendientes > 0) {
                        new ActivityNotasCredito.DescargaNotas(this).execute("");
                    } else {
                        makeDialog("No hay notas crédito pendientes por descargar.", ActivityNotasCredito.this);
                        cargarNotas();
                    }
                } catch (Exception e) {
                    makeDialog("Error descarga", e.getMessage(), ActivityNotasCredito.this);
                }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return false;
    }

    private void eliminarNotas() {
        String mensaje = "";
        int facturasPendientes = App.ResumenFacturacion.pendientesSincronizacion;
        int remisionesPendientes = new RemisionDAL(this).obtenerListadoPendientes().size();
        int notasPendientes = new NotaCreditoFacturaDAL(this).obtenerListadoPendientes().size();
        final int pendientes = facturasPendientes + remisionesPendientes + notasPendientes;

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

        if (notasPendientes > 0) {
            if (facturasPendientes > 0 || remisionesPendientes > 0) {
                mensaje += " Y "
                        + String.valueOf(notasPendientes)
                        + " notas crédito por descargar. ";
            } else {
                mensaje = "Tiene "
                        + String.valueOf(notasPendientes)
                        + " notas crédito por descargar. ";
            }
        }

        mensaje += "Está seguro que desea eliminar todas las notas créditos, facturas y/o remisiones?";

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Eliminar notas crédito, facturas y remisiones");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                try {
                    if (pendientes > 0) {
                        confirmarPasswordAdmin();
                    } else {
                        eliminarNota();
                    }
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityNotasCredito.this);
                }
            }
        });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    private void confirmarPasswordAdmin() {
        Intent intent = new Intent(this, ActivityConfirmarPass.class);
        startActivityForResult(intent, CONFIRMAR_PASS_ADMIN);
    }

    private void eliminarNota() {
        new FacturaDAL(this).eliminar();
        new RemisionDAL(this).eliminar();
        new NotaCreditoFacturaDAL(this).eliminar();
        cargarNotas();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIRMAR_PASS_ADMIN) {
            if (resultCode == RESULT_OK) {
                eliminarNota();
            }
            if (resultCode == RESULT_CANCELED) {
                showNoTienePermisos();
            }
        }
    }

    private void showNoTienePermisos() {
        String mensaje = "No posee los permisos para eliminar notas créditos";
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void imprimirNota(NotaCreditoFacturaDTO nota) {

        try {
            Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
            printer.print(nota);

        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), ActivityNotasCredito.this);
        }
    }

    private class DescargaNotas extends AsyncTask<String, String, String> {

        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Enviando las notas crédito");
        }

        ActivityBase contexto;

        private DescargaNotas(ActivityBase contexto) {
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
                    respuesta.append("No hay notas crédito pendientes para descargar.\n");
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
                makeErrorDialog(msgError, ActivityNotasCredito.this);
            } else if (!TextUtils.isEmpty(result)) {
                makeDialog(result, ActivityNotasCredito.this);
            }
            cargarNotas();
        }
    }

    private final static int CONFIRMAR_PASS_ADMIN = 1001;
}
