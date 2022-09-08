package com.cm.timovil2.front;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.ResumenVentasImpresionDTO;

import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class ActivityResumenDiario extends ActivityBase
        implements MenuItem.OnMenuItemClickListener, View.OnClickListener {

    private TextView lblDetalleResumenDiario;
    private TextView lblDetalleRemision;
    private TextView lblResumenDiario;
    private TextView lblResumenRemisionesDiario;
    private TextView lblResumenRemision;
    private TextView lblResumenCantidades;
    private TextView lblDetalleNotaCreditoPorDevolucion;
    private TextView lblResumenNotasCreditoFacturaPorDevolucion;

    private static TextView lblFechaDesde;
    private static TextView lblFechaHasta;
    private static DateTime fechaDesde;
    private static DateTime fechaHasta;
    private Button btn_ver_resumen;

    private ResumenVentasImpresionDTO resumenVentasImpresion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resumen_diario_activity);
        App.actualActivity = this;
        resumenVentasImpresion = new ResumenVentasImpresionDTO();
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        setControls();

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mes = month + 1;

        fechaDesde = new DateTime(year, mes, day, 0, 0);
        fechaHasta = new DateTime(year, mes, day, 23, 59);

        obtenerDetalleResumen(fechaDesde, fechaHasta);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_resumen_diario, menu);
        MenuItem item = menu.findItem(R.id.mnuImprimirResumen);
        if (item != null) {
            item.setOnMenuItemClickListener(this);
        }
        return true;
    }

    public boolean onMenuItemClick(MenuItem item) {
        if(App.obtenerConfiguracion_imprimir(this)){
            imprimir();
        }else{
            makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityResumenDiario.this);
        }
        return false;
    }

    private void mostrarResumen(){

        if (resumenVentasImpresion.detalleResumen != null && lblDetalleResumenDiario != null) {
            lblDetalleResumenDiario.setText(resumenVentasImpresion.detalleResumen);
        }

        if (resumenVentasImpresion.resumen != null && lblResumenDiario != null) {
            lblResumenDiario.setText(resumenVentasImpresion.resumen);
        }

        if (resumenVentasImpresion.resumenRemisiones != null && lblResumenRemisionesDiario != null) {
            lblResumenRemisionesDiario.setText(resumenVentasImpresion.resumenRemisiones);
        }

        if (resumenVentasImpresion.detalleRemision != null && lblDetalleRemision != null) {
            lblDetalleRemision.setText(resumenVentasImpresion.detalleRemision);
        }

        if(resumenVentasImpresion.detalleResumenNotasCreditoPorDevolucion != null &&
                lblDetalleNotaCreditoPorDevolucion != null){
            lblDetalleNotaCreditoPorDevolucion.setText(resumenVentasImpresion.detalleResumenNotasCreditoPorDevolucion);
        }

        if (lblResumenNotasCreditoFacturaPorDevolucion != null &&
                resumenVentasImpresion.resumenNotasCreditoPorDevolucion != null){
            lblResumenNotasCreditoFacturaPorDevolucion.setText(resumenVentasImpresion.resumenNotasCreditoPorDevolucion);
        }

        if (resumenVentasImpresion.resumenRemision != null && lblResumenRemision != null) {
            lblResumenRemision.setText(resumenVentasImpresion.resumenRemision);
        }

        if (resumenVentasImpresion.resumenCantidades != null && lblResumenCantidades != null) {
            lblResumenCantidades.setText(resumenVentasImpresion.resumenCantidades);
        }
    }

    @Override
    protected void setControls() {

        lblDetalleResumenDiario = findViewById(R.id.lblDetalleResumenDiario);
        lblDetalleRemision = findViewById(R.id.lblDetalleRemision);
        lblResumenDiario = findViewById(R.id.lblResumenDiario);
        lblResumenRemisionesDiario = findViewById(R.id.lblResumenRemisionesDiario);
        lblResumenRemision = findViewById(R.id.lblResumenRemision);
        lblResumenCantidades = findViewById(R.id.lblResumenCantidades);
        lblDetalleNotaCreditoPorDevolucion = findViewById(R.id.lblDetalleNotaCreditoPorDevolucion);
        lblResumenNotasCreditoFacturaPorDevolucion = findViewById(R.id.lblResumenNotasCreditoFacturaPorDevolucion);

        lblFechaDesde = findViewById(R.id.lblFechaDesde);
        lblFechaDesde.setOnClickListener(this);

        lblFechaHasta = findViewById(R.id.lblFechaHasta);
        lblFechaHasta.setOnClickListener(this);

        DateTime now = DateTime.now();
        StringBuilder fecha = new StringBuilder();
        int day = now.getDayOfMonth();
        int mes = now.getMonthOfYear();
        int year = now.getYear();

        fecha.append((day < 10 ? ("0" + day) : String.valueOf(day)))
                .append("/").append(mes < 10 ? ("0" + mes) : String.valueOf(mes))
                .append("/").append(year);

        lblFechaDesde.setText(fecha.toString());
        lblFechaHasta.setText(fecha.toString());

        btn_ver_resumen = findViewById(R.id.btn_ver_resumen);
        btn_ver_resumen.setOnClickListener(this);
    }

    private void obtenerDetalleResumen(DateTime fechaDesde, DateTime fechaHasta) {
        try {
            FacturaDAL facturaDAL = new FacturaDAL(this);
            RemisionDAL remisionDAL = new RemisionDAL(this);
            NotaCreditoFacturaDAL ncfdal = new NotaCreditoFacturaDAL(this);

            // generar resumen
            try{
                App.ResumenFacturacion.Iniciar();
                App.ResumenRemisiones.Iniciar();
                App.ResumenNotasCreditoPorDevolucion.Iniciar();

                facturaDAL.obtenerListado(true, fechaDesde, fechaHasta);
                remisionDAL.obtenerListado(true, fechaDesde, fechaHasta);
                ncfdal.obtenerListado(true, fechaDesde, fechaHasta);

                App.ResumenFacturacion.CrearNotaCreditoPorDevolucion = App.obtenerConfiguracion_CrearNotaCreditoPorDevolucion(this);
                resumenVentasImpresion.resumen = App.ResumenFacturacion.toString();
                resumenVentasImpresion.resumenRemisiones = App.ResumenRemisiones.toString();
                resumenVentasImpresion.resumenNotasCreditoPorDevolucion = App.ResumenNotasCreditoPorDevolucion.toString();

            }catch (Exception e){
                logCaughtException(e);
                Log.d("ResumenesActivity", e.toString());
            }

            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

            resumenVentasImpresion.detalleResumen = facturaDAL.obtenerDetalleResumenFacturacion2(fechaDesde, fechaHasta);
            resumenVentasImpresion.detalleResumenToPrint = facturaDAL.obtenerDetalleResumenFacturacionToPrint();
            resumenVentasImpresion.detallePrimeraYultimaFactura = facturaDAL.obtenerPrimeraYultimaFactura(fechaDesde, fechaHasta);
            //-----------------------------------------------------------------------
            resumenVentasImpresion.detalleRemision = facturaDAL.obtenerDetalleResumenRemision2(fechaDesde, fechaHasta);
            resumenVentasImpresion.detalleRemisionToPrint = facturaDAL.obtenerDetalleResumenRemisionToPrint();
            resumenVentasImpresion.detallePrimeraYultimaRemision = remisionDAL.obtenerPrimeraYultimaRemision(fechaDesde, fechaHasta);
            //-----------------------------------------------------------------------
            resumenVentasImpresion.detalleResumenNotasCreditoPorDevolucion = ncfdal.obtenerDetalleResumenNotaCreditoPorDevolucion(fechaDesde, fechaHasta);
            resumenVentasImpresion.detalleResumenNotasCreditoPorDevolucionToPrint = ncfdal.obtenerDetalleResumenNotaCreditoPorDevolucionToPrint();
            resumenVentasImpresion.detallePrimeraYultimaNotaCreditoPorDevolucion = ncfdal.obtenerPrimeraYultimaNotaCreditoPorDevolucion(fechaDesde, fechaHasta);

            double totalRemisiones = App.ResumenRemisiones.total;
            double totalVentas = App.ResumenFacturacion.total;
            double totalNotasCreditoPorDevolucion = App.ResumenNotasCreditoPorDevolucion.Valor;

            resumenVentasImpresion.resumenRemision =
                      " Total remisiones: "+ formatter.format(totalRemisiones) + "\r\n"
                    + " Total facturas  : " + formatter.format(totalVentas) + "\r\n"
                    + " Total notas dev : " + formatter.format(totalNotasCreditoPorDevolucion) + "\r\n"
                    + " TOTAL general   : " + formatter.format(totalVentas + totalRemisiones - totalNotasCreditoPorDevolucion);

            //--------------------------------------------------------------
            if(resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                    || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {

                int cantidadRemisiones = facturaDAL.obtenerCantidadRemisiones();
                int cantidadFacturasCreditos = facturaDAL.obtenerCantidadFacturasCredito();
                int cantidadFacturasContado = facturaDAL.obtenerCantidadFacturasContado();
                int cantidadRemisionesCreditos = facturaDAL.obtenerCantidadRemisionesCredito();
                int cantidadRemisionesContado = facturaDAL.obtenerCantidadRemisionesContado();
                int cantidadNotasCreditoPorDevolucion = ncfdal.obtenerCantidadNotasCreditoPorDevolucion();

                resumenVentasImpresion.resumenCantidades =
                        " Cant. FACTURAS: " + (cantidadFacturasContado + cantidadFacturasCreditos) +
                         "\r\n Cant. Contado Facturas: " + cantidadFacturasContado +
                         "\r\n Cant. Crédito Facturas: " + cantidadFacturasCreditos +
                         "\r\n Cant. REMISIONES: " + cantidadRemisiones +
                         "\r\n Cant. Contado Remisiones: " + cantidadRemisionesContado +
                         "\r\n Cant. Crédito Remisiones: " + cantidadRemisionesCreditos +
                         "\r\n Cant. NOTAS CREDITO POR DEVOLUCION: " + cantidadNotasCreditoPorDevolucion +
                         "\r\n Total unidades: " + (cantidadFacturasContado + cantidadFacturasCreditos + cantidadRemisiones);
            }

            btn_ver_resumen.setEnabled(true);
            mostrarResumen();

        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityResumenDiario.this);
        }
    }

    private void imprimir() {
        try {

            if (resolucion != null) {
                Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                printer.print(resumenVentasImpresion);
            }

        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityResumenDiario.this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.lblFechaDesde:
                Bundle bundleDesde = new Bundle();
                bundleDesde.putString("fecha", "lblFechaDesde");
                mostrarDatePicker(bundleDesde);
                break;
            case R.id.lblFechaHasta:
                Bundle bundleHasta = new Bundle();
                bundleHasta.putString("fecha", "lblFechaHasta");
                mostrarDatePicker(bundleHasta);
                break;
            case R.id.btn_ver_resumen:
                btn_ver_resumen.setEnabled(false);
                obtenerDetalleResumen(fechaDesde, fechaHasta);
                break;
        }
    }

    private void mostrarDatePicker(Bundle bundle) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private String lblFechaActual;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            Bundle bundle = getArguments();
            if (bundle != null) {
                lblFechaActual = bundle.getString("fecha", "lblFechaDesde");
            }
            //Create a new instance of DatePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            c.add(Calendar.DAY_OF_MONTH, -7);
            dialog.getDatePicker().setMinDate(c.getTimeInMillis());

            final Calendar c2 = Calendar.getInstance();
            c2.add(Calendar.DAY_OF_MONTH, 1);
            dialog.getDatePicker().setMaxDate(c2.getTimeInMillis());

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            StringBuilder fecha = new StringBuilder();
            int mes = (month + 1);

            fecha.append((day < 10 ? ("0" + day) : String.valueOf(day)))
                    .append("/").append(mes < 10 ? ("0" + mes) : String.valueOf(mes))
                    .append("/").append(String.valueOf(year));

            if (lblFechaActual.equals("lblFechaDesde")) {
                lblFechaDesde.setText(fecha.toString());
                fechaDesde = new DateTime(year, mes, day, 0, 0);
            }

            if (lblFechaActual.equals("lblFechaHasta")) {
                lblFechaHasta.setText(fecha.toString());
                fechaHasta = new DateTime(year, mes, day, 23, 59);
            }

            if(fechaDesde != null && fechaHasta != null
                    && !fechaDesde.isBefore(fechaHasta)){
                Toast.makeText(getActivity(), "La fecha 'hasta' debe ser mayor a la fecha 'desde'",
                        Toast.LENGTH_LONG).show();
                lblFechaHasta.setText("");
            }
        }
    }

}
