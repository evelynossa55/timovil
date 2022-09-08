package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterCreditos;
import com.cm.timovil2.bl.adapters.AdapterFacturasPorFecha;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.FacturasDescargadasCallback;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemFacturaDetail;
import com.cm.timovil2.bl.utilities.ItemFacturaHeader;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.bl.utilities.ServiceMonitorFacturacion;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.wsentities.MFactCredito;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ActivityFacturas extends ActivityBase implements OnMenuItemClickListener, FacturasDescargadasCallback {

    private RecyclerView recyclerFacturas;
    private FacturaDAL facturaDal;
    private NotaCreditoFacturaDAL notaCreditoFacturaDal;
    private EditText textoBuscar;
    private ArrayList<FacturaDTO> listadoFacturas;
    private ArrayList<Item> listadoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facturas);
        App.isFacturasActivityVisible = true;
        App.actualActivity = this;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        facturaDal = new FacturaDAL(this);
        notaCreditoFacturaDal = new NotaCreditoFacturaDAL(this);
        listadoFacturas = new ArrayList<>();
        setControls();

        recyclerFacturas.requestFocus();
        registerForContextMenu(recyclerFacturas);
    }

    private void mostrarErrorYsalir(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityFacturas.this);
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
    public void actualizarFacturas() {
        cargarFacturas();
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

    @Override
    protected void onResume() {
        super.onResume();
        App.isFacturasActivityVisible = true;
        App.actualActivity = this;
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYsalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        } else {
            cargarFacturas();
        }
    }

    @Override
    protected void setControls() {
        recyclerFacturas = findViewById(R.id.recycler_facturas);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerFacturas.setLayoutManager(mLayoutManager);
        textoBuscar=findViewById(R.id.txtBuscar_filtros);

        textoBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cargarFacturasFiltros();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private ArrayList<Item> cargarListadoEncabezados(ArrayList<FacturaDTO> facturas) {

        ArrayList<Item> result = new ArrayList<>();
        ArrayList<String> dates_aux = new ArrayList<>();
        SimpleDateFormat dt = new SimpleDateFormat("yyyyyMMdd", Utilities.getLocale());
        ItemFacturaHeader fh;
        ItemFacturaDetail fd;

        for (FacturaDTO f : facturas) {

            Date fecha = new Date(f.FechaHora);
            String format = dt.format(fecha);
            if (!dates_aux.contains(format)) {
                dates_aux.add(format);
                fh = new ItemFacturaHeader();
                fh.setFechaFactura(f.FechaHora);
                result.add(fh);

                //Añadir facturas
                for (FacturaDTO fa : facturas) {
                    if (format.equals(dt.format(new Date(fa.FechaHora)))) {
                        fd = new ItemFacturaDetail();
                        fd.setFacturaDTO(fa);
                        result.add(fd);
                    }
                }
            }
        }
        return result;
    }

    private void cargarFacturas() {
        try {
            DateTime now = DateTime.now();
            listadoFacturas.clear();
            listadoFacturas = facturaDal.obtenerListado(true, now, now);
            listadoItems = cargarListadoEncabezados(listadoFacturas);

            AdapterFacturasPorFecha adaptadorPorFechas = new AdapterFacturasPorFecha(listadoItems);
            adaptadorPorFechas.setOnClickListener(new OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //Back to top
                    recyclerFacturas.smoothScrollToPosition(0);
                }
            });

            recyclerFacturas.setAdapter(adaptadorPorFechas);

            setTitle("Facturas (" + String.valueOf(listadoFacturas.size()) + ")");

        } catch (Exception e) {
            makeErrorDialog("Error cargando las facturas: " + e.getMessage(), ActivityFacturas.this);
        }
    }
    private void cargarFacturasFiltros() {
        try {
            DateTime now = DateTime.now();
            listadoFacturas.clear();
            listadoFacturas = facturaDal.obtenerListadoFiltro(true, now, now,textoBuscar.getText().toString());
            listadoItems = cargarListadoEncabezados(listadoFacturas);

            AdapterFacturasPorFecha adaptadorPorFechas = new AdapterFacturasPorFecha(listadoItems);
            adaptadorPorFechas.setOnClickListener(new OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //Back to top
                    recyclerFacturas.smoothScrollToPosition(0);
                }
            });
            recyclerFacturas.setAdapter(adaptadorPorFechas);
            setTitle("Facturas (" + String.valueOf(listadoFacturas.size()) + ")");

        } catch (Exception e) {
            }
    }

    public boolean onMenuItemClick(MenuItem arg) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_facturas, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            if (resolucion.IdCliente.equals(Utilities.ID_NATIPAN)) {
                menu.getItem(2).setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuVerificarFacturas:
                new VerificarIntegridadFacturas().execute();
                break;
            case R.id.mnuEliminarFacturas:
                eliminarFacturas();
                break;
            case R.id.mnuResumenFacturas:
                mostrarResumen();
                break;
            case R.id.mnuDescargarFacturasPendientes:
                try {
                    int facturasPendientes = new FacturaDAL(this).obtenerListadoPendientes().size();
                    int remisionesPendientes = new RemisionDAL(this).obtenerListadoPendientes().size();
                    int noVentaPedidos = new GuardarMotivoNoVentaPedidoDAL(getApplicationContext())
                            .obtenerListadoPendientes().size();
                    int noVenta = new GuardarMotivoNoVentaDAL(getApplicationContext())
                            .obtenerListadoPendientes().size();

                    final int pendientes = facturasPendientes + remisionesPendientes
                            + noVentaPedidos + noVenta;

                    if (pendientes > 0) {
                        new DescargaFacturas(this).execute("");
                    } else {
                        makeDialog("No hay facturas pendientes por descargar.", ActivityFacturas.this);
                        cargarFacturas();
                    }
                } catch (Exception e) {
                    makeDialog("Error descarga", e.getMessage(), ActivityFacturas.this);
                }
                break;
            case R.id.mnuImprimirTodas:
                if (App.obtenerConfiguracion_imprimir(this)) {

                    try {
                        DateTime now = DateTime.now();
                        listadoFacturas.clear();
                        listadoFacturas = facturaDal.obtenerListado(true, now, now);

                        Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                        printer.printFacturas(listadoFacturas);

                    } catch (Exception e) {
                        makeDialog("Error al imprimir", e.getMessage(), ActivityFacturas.this);
                    }

                } else {
                    makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityFacturas.this);
                }
                break;
           /* case R.id.mnuCerrarDia:
                cerrarDia();
                break;*/
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return false;
    }
/** Deprecated, la opcion de cerrar dia ya no se utiliza
    private void cerrarDia() {
        //1ro: Verifico que se hayan descargado todas las facturas:
        try {
            FacturaDAL facturaDAL = new FacturaDAL(this);
            int cantidadFacturas = facturaDAL.obtenerCantidadFacturasPendientes();
            makeLToast(String.valueOf(cantidadFacturas));
        } catch (Exception ex) {
            makeErrorDialog("Error consultando las facturas pendientes: " + ex.getMessage(), getApplicationContext());
        }
    }
 **/
    private void mostrarResumen() {
        Intent intent = new Intent(this, ActivityResumenDiario.class);
        startActivity(intent);
    }

    private void eliminarFacturas() {
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

        Builder d = new Builder(this);
        d.setTitle("Eliminar facturas/remisiones");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                try {
                    if (pendientes > 0) {
                        confirmarPasswordAdmin();
                    } else {
                        eliminarFactura();
                    }
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityFacturas.this);
                }
            }
        });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    private void eliminarFactura() {

        if (resolucion.IdCliente.equals(Utilities.ID_FOGON_PAISA)) {
            makeDialog("No posee permisos para eliminar facturas.", ActivityFacturas.this);
            return;
        }

        new FacturaDAL(this).eliminar();
        new RemisionDAL(this).eliminar();
        new NotaCreditoFacturaDAL(this).eliminar();
        cargarFacturas();
        try {
            new AbonoFacturaDAL(this).contarAbonosPorRangoFechas(DateTime.now(), DateTime.now());
        } catch (Exception e) {
            makeErrorDialog("Error haciendo el conteo de abonos, " + e.getMessage(), ActivityFacturas.this);
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
                eliminarFactura();
            }
            if (resultCode == RESULT_CANCELED) {
                showNoTienePermisos();
            }
        } else if (requestCode == INGRESAR_COMENTARIO_FACTURA) {
            if (resultCode == RESULT_OK) {
                if (App.ComentarioFactura != null
                        && !App.ComentarioFactura.equals("")
                        && App.aux_facturaDTO != null) {
                    App.aux_facturaDTO.ComentarioAnulacion = App.ComentarioFactura;
                    App.ComentarioFactura = null;
                    new AnularFacturaTask().execute(App.aux_facturaDTO);
                } else {
                    makeErrorDialog("Debe ingresar un comentario a la factura para poder anularla.", ActivityFacturas.this);
                }
            } else {
                makeErrorDialog("Debe ingresar un comentario a la factura para poder anularla.", ActivityFacturas.this);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int seleccion = item.getItemId();

        //final AdapterView.AdapterContextMenuInfo info =
          //      (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        //if (info != null) {
        AdapterFacturasPorFecha adapterFacturasPorFecha = (AdapterFacturasPorFecha)recyclerFacturas.getAdapter();
        if(adapterFacturasPorFecha != null) {

            int position = adapterFacturasPorFecha.getPosition();
            final Item it = listadoItems.get(position);

            if (it.isSection()) {
                return true;
            }
            final FacturaDTO fac = ((ItemFacturaDetail) it).getFacturaDTO();

            switch (seleccion) {
                case 1: // Abrir
                    int _id_factura = fac._Id;
                    Intent intent = new Intent(this, ActivityFactura.class);
                    intent.putExtra("_Id", String.valueOf(_id_factura));
                    startActivity(intent);
                    break;
                case 2: // Anular
                    anularFactura(fac);
                    break;
                case 3: //Imprimir
                    if (App.obtenerConfiguracion_imprimir(this)) {
                        imprimirFactura(fac);
                    } else {
                        makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityFacturas.this);
                    }
                    break;
                case 4:
                    final ActivityBase context = this;
                    Builder d = new Builder(this);
                    d.setTitle("Cambiar estado de factura");
                    d.setMessage("Hacer esta factura pendiente de descarga?");
                    d.setCancelable(false);
                    d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                            try {
                                new FacturaDAL(context).actualizarEstadoDescarga(fac.NumeroFactura,
                                        false, fac.PendienteAnulacion);
                                cargarFacturas();
                            } catch (Exception e) {
                                makeErrorDialog(e.getMessage(), context);
                            }
                        }
                    });
                    d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo1, int id) {
                        }
                    });
                    d.show();
                    break;
            }
        }
        //}
        return true;
    }

    private void anularFactura(final FacturaDTO factura) {

        try {
            if (resolucion.IdCliente.equals(Utilities.ID_FOGON_PAISA)
                    || resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || resolucion.IdCliente.equals(Utilities.ID_VEGA)
                    || resolucion.IdCliente.equals(Utilities.ID_COMESCOL)) {

                makeDialog("No posee permisos para anular facturas.", ActivityFacturas.this);
                return;
            }

            Calendar fechaDispositivo = GregorianCalendar.getInstance();
            int intYearLocal = fechaDispositivo.get(GregorianCalendar.YEAR);
            int intMonthLocal = (fechaDispositivo.get(GregorianCalendar.MONTH) + 1);
            int intDayLocal = fechaDispositivo.get(GregorianCalendar.DAY_OF_MONTH);
            int intHourLocal = fechaDispositivo.get(GregorianCalendar.HOUR_OF_DAY);
            int intMinuteLocal = fechaDispositivo.get(GregorianCalendar.MINUTE);

            fechaDispositivo.setTime(new Date(factura.FechaHora));
            int intYearFactura = fechaDispositivo.get(GregorianCalendar.YEAR);
            int intMonthFactura = (fechaDispositivo.get(GregorianCalendar.MONTH) + 1);
            int intDayFactura = fechaDispositivo.get(GregorianCalendar.DAY_OF_MONTH);
            int intHourFactura = fechaDispositivo.get(GregorianCalendar.HOUR_OF_DAY);
            int intMinuteFactura = fechaDispositivo.get(GregorianCalendar.MINUTE);

            boolean tiempoParaAnular;
            if (intYearFactura != intYearLocal) {
                tiempoParaAnular = false;
            } else if (intMonthLocal != intMonthFactura) {
                tiempoParaAnular = false;
            } else if (intDayLocal != intDayFactura) {
                tiempoParaAnular = false;
            } else if (intHourLocal != intHourFactura
                    && ((intHourLocal - intHourFactura) > 1
                    || (intHourLocal - intHourFactura) < -1)) {
                tiempoParaAnular = false;
            } else {

                int diferenciaHora = (intHourLocal - intHourFactura);
                int diferenciaMinutos;
                if (diferenciaHora == 0) {
                    diferenciaMinutos = intMinuteLocal - intMinuteFactura;
                } else {

                    if (diferenciaHora == 1) {
                        diferenciaMinutos = (intMinuteLocal + (60 - intMinuteFactura));
                    } else {
                        diferenciaMinutos = (intMinuteFactura + (60 - intMinuteLocal));
                    }
                }

                tiempoParaAnular = !(diferenciaMinutos > 30 || diferenciaMinutos < -30);
            }

            if (!tiempoParaAnular &&
                    (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || resolucion.IdCliente.equals(Utilities.ID_VEGA)
                    || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL))) {
                makeDialog("Ya ha pasado más de media hora desde que se creó esta factura, y " +
                        "por lo tanto no puede ser anulada", ActivityFacturas.this);
                return;
            }

            if (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                    || resolucion.IdCliente.equals(Utilities.ID_POLAR)) {

                String rutas[] = {"30", "31", "34", "35", "41", "42", "43", "44", "46", "50", "52", "53", "54", "56", "57", "60", "67", "68", "70", "72", "73", "74", "75", "76", "77", "78", "80", "81", "82", "83", "85", "86", "87", "89", "90", "91", "92", "93"};

                boolean found = false;
                for (String element : rutas) {
                    if (resolucion.CodigoRuta.equals(element)) {
                        found = true;
                        break;
                    }
                }

                if ((found && factura.FormaPago.equals("000"))
                        || resolucion.IdCliente.equals(Utilities.ID_POLAR)) {
                    makeDialog("No posee permisos para anular facturas de Contado.", ActivityFacturas.this);
                    return;
                }
            }

            if (factura.Anulada) {
                makeDialog("La factura ya se encuentra anulada.", ActivityFacturas.this);
                return;
            }

            Builder d = new Builder(this);
            d.setTitle("Anular factura");
            d.setMessage("Está seguro que desea anular la factura?");
            d.setCancelable(false);
            d.setPositiveButton("Aceptar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogo, int id) {
                            try {
                                App.aux_facturaDTO = factura;
                                Intent i = new Intent(ActivityFacturas.this, ActivityComentarioFactura.class);
                                i.putExtra("estado", "anulacion");
                                startActivityForResult(i, INGRESAR_COMENTARIO_FACTURA);
                            } catch (Exception e) {
                                makeErrorDialog(e.getMessage(), ActivityFacturas.this);
                            }
                        }
                    });
            d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                }
            });
            d.show();

        } catch (Exception e) {
            makeDialog("TiMovil", "Error anulando la factura: " + e.getMessage(), ActivityFacturas.this);
        }
    }

    private void imprimirFactura(FacturaDTO factura) {
        int i =0;

        try {
            Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);

            if ((resolucion.IdCliente.equals(Utilities.ID_FR) && factura.Remision)
                    || (resolucion.IdCliente.equals(Utilities.ID_JAF) &&
                    resolucion.CodigoRuta.equals("50"))) {
                printer.printFacturaComoRemision(factura);
            } else {
                if(i==0){
                    i=1;
                    NotaCreditoFacturaDTO nota = new NotaCreditoFacturaDAL(this).obtenerPorNumeroFac(factura.NumeroFactura);
                    if (nota != null) factura.notaCreditoFactura = nota;
                    printer.print(factura);
                    i=0;
                }else{
                    makeDialog("Por favor espere a prosesar las facturas...", ActivityFacturas.this);
                    App.guardarConfiguracionEstadoAplicacion("R", context);
                }
            }
        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), ActivityFacturas.this);
        }

    }

    /**
     * =======================================================================
     */
    private class AnularFacturaTask extends AsyncTask<FacturaDTO, String, String> {
        FacturaDTO factura;

        @Override
        protected String doInBackground(FacturaDTO... params) {
            String respuesta;
            factura = params[0];
            try {
                String respuestaAnualada = facturaDal.anular(factura);

                if (!TextUtils.isEmpty(respuestaAnualada) && respuestaAnualada.equals("OK")) {
                    notaCreditoFacturaDal.anular(factura.NumeroFactura);
                }

                respuesta = facturaDal.sincronizarFactura(factura);

                if (respuesta.equals("Sincronizando")) {
                    respuesta = "La factura ya se estaba sincronizando, por favor intenta nuevamente";
                } else {
                    new ClienteDAL(getApplicationContext()).restarVecesAntendido(factura.IdCliente);
                    factura = null;
                }
            } catch (Exception e) {
                respuesta = (e.getMessage());
            }
            return respuesta;
        }

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Anulando la factura");
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.getDialog().dismiss();

            if (result.equals("OK")) {
                makeDialog(" Factura anulada correctamente.", ActivityFacturas.this);
            } else {
                if (result.equals(Utilities.IMEI_ERROR)) {
                    App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                    mostrarErrorYsalir("La aplicación se encuentra " +
                            "bloqueada, por favor configure nuevamente la ruta");
                } else {
                    makeErrorDialog("No se pudo descargar el cambio: " + result, ActivityFacturas.this);
                }
            }
            cargarFacturas();
        }
    }

    private class DescargaFacturas extends AsyncTask<String, String, String> {

        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Enviando facturas");
        }

        ActivityBase contexto;

        private DescargaFacturas(ActivityBase contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {
            StringBuilder respuesta = new StringBuilder();
            try {

                new ServiceMonitorFacturacion().verificar(true);

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
                        publishProgress("Descargando facturas pendientes", "...");
                        respuesta.append("\nFacturas: ");
                        respuesta.append(facturaDal.descargarFacturas()).append("\n");
                    }
                    if (listadoAbonoPendientes > 0) {
                        publishProgress("Descargando abonos pendientes", "...");
                        respuesta.append("\nCréditos: ");
                        respuesta.append(abonoFacturaDal.descargarAbonos());
                    }
                    if (listadoNoVentaPendientes > 0) {
                        publishProgress("Descargando registros de no venta", "...");
                        respuesta.append("\nNo venta: ");
                        respuesta.append(guardarMotivoNoVentaDal.descargarPendientes());
                    }
                    if (listadoNoVentaPedidoPendientes > 0) {
                        publishProgress("Descargando registros de no venta para pedidos", "...");
                        respuesta.append("\nNo venta, pedidos: ");
                        respuesta.append(guardarMotivoNoVentaPedidoDal.descargarPendientes());
                    }
                    if (remPendientes > 0) {
                        publishProgress("Remisiones pendientes", "Conectando con el servidor...");
                        publishProgress("Descargando remisiones pendientes", "...");
                        respuesta.append(remisionDal.descargarRemisiones());
                    }
                    if (notasCreditoPendientes > 0) {
                        publishProgress("Notas crédito pendientes", "Conectando con el servidor...");
                        respuesta.append("\nNotas Crédito: ");
                        respuesta.append(notaCreditoFacturaDal.descargarPendientes());
                    }
                } else {
                    respuesta.append("No hay facturas pendientes para descargar.\n");
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
                msgError = e.getMessage();
            }
            return respuesta.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (!msgError.equals("")) {
                makeErrorDialog(msgError, ActivityFacturas.this);
            } else if (!TextUtils.isEmpty(result)) {
                makeDialog(result, ActivityFacturas.this);
            }
            cargarFacturas();
        }
    }

    private final Context context = this;
    private class VerificarIntegridadFacturas extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Verificando todas las facturas");
        }

        private VerificarIntegridadFacturas() {
        }

        @Override
        protected String doInBackground(String... params) {
            new ServiceMonitorFacturacion().verificar(true);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            makeDialog("Se han revisado todas las facturas", ActivityFacturas.this);
            cargarFacturas();
        }
    }

    private final static int CONFIRMAR_PASS_ADMIN = 1000;
    private final static int INGRESAR_COMENTARIO_FACTURA = 1001;
}