package com.cm.timovil2.front;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 15/03/18.
 */

public class ActivityNotaCredito extends ActivityBase implements MenuItem.OnMenuItemClickListener {

    private TextView tv_datos_cliente;
    private TextView tv_resumen;
    private TextView tv_resumen_nota;
    private TextView tv_numero_nota;
    private TextView tv_numero_factura;
    private LinearLayout linearlayoutDetalleFactura;
    private LinearLayout linearlayoutDetalleNotaCredito;
    private LayoutInflater vi;
    private FacturaDTO facturaDTO;
    private NotaCreditoFacturaDTO notaCreditoFacturaDTO;
    private String _Id_nota;
    private LinearLayout layout_factura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota_credito);
        setControls();
        App.actualActivity = this;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            _Id_nota = bundle.getString("_Id_nota");
        }
        if (_Id_nota != null) {

            notaCreditoFacturaDTO = new NotaCreditoFacturaDAL(this).obtenerPorID(_Id_nota);
            facturaDTO = new FacturaDAL(this).obtenerPorNumeroFac(notaCreditoFacturaDTO.NumeroFactura);

            if (facturaDTO != null) {
                cargarDatosNotaCredito();
                cargarDatosFactura();
                cargarDetalle(facturaDTO.DetalleFactura);
                cargarDetalleNota(notaCreditoFacturaDTO.DetalleNotaCreditoFactura);
            } else {
                makeDialog("No se pudo cargar la nota crédito, no se eencontró la factura asociada", ActivityNotaCredito.this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_resumen_diario, menu);
        MenuItem item = menu.findItem(R.id.mnuImprimirResumen);
        if (item != null) {
            item.setOnMenuItemClickListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void setControls() {
        tv_datos_cliente = findViewById(R.id.tv_datos_cliente);
        tv_resumen = findViewById(R.id.tv_resumen);
        tv_resumen_nota = findViewById(R.id.tv_resumen_nota);
        tv_numero_nota = findViewById(R.id.tv_numero_nota);
        tv_numero_factura = findViewById(R.id.tv_numero_factura);
        linearlayoutDetalleFactura = findViewById(R.id.linearlayoutDetalleFactura);
        linearlayoutDetalleNotaCredito = findViewById(R.id.linearlayoutDetalleNotaCredito);
        layout_factura = findViewById(R.id.layout_factura);
        vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean onMenuItemClick(MenuItem item) {
        imprimir();
        return false;
    }

    private void imprimir() {
        try {
            if (App.obtenerConfiguracion_imprimir(this)) {
                if (notaCreditoFacturaDTO != null) {

                    Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                    printer.print(notaCreditoFacturaDTO);

                } else {
                    makeErrorDialog("No es posible imprimir la nota crédito [Nota Crédito Factura = null]", ActivityNotaCredito.this);
                }
            } else {
                makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityNotaCredito.this);
            }
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityNotaCredito.this);
        }
    }

    private void cargarDetalleNota(ArrayList<DetalleNotaCreditoFacturaDTO> detalles) {
        if (detalles != null) {
            TextView txtDetalle;
            linearlayoutDetalleNotaCredito.removeAllViews();
            if (detalles.isEmpty()) {
                txtDetalle = new TextView(this);
                txtDetalle.setText("------");
                linearlayoutDetalleNotaCredito.addView(txtDetalle);
                linearlayoutDetalleNotaCredito.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearlayoutDetalleNotaCredito.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (DetalleNotaCreditoFacturaDTO detalle : detalles) {
                    if (vi != null) {
                        view = vi.inflate(R.layout.lista_item, layout_factura, false);
                        if (view != null) {
                            txtDetalle = view.findViewById(R.id.layout_lista_item);
                            txtDetalle.setText(detalle.getResume());
                            linearlayoutDetalleNotaCredito.addView(view);
                        }
                    }
                }
            }
        }
    }

    private void cargarDetalle(ArrayList<DetalleFacturaDTO> detalles) {
        if (detalles != null) {
            TextView txtDetalle;
            linearlayoutDetalleFactura.removeAllViews();
            if (detalles.isEmpty()) {
                txtDetalle = new TextView(this);
                txtDetalle.setText("------");
                linearlayoutDetalleFactura.addView(txtDetalle);
                linearlayoutDetalleFactura.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearlayoutDetalleFactura.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (DetalleFacturaDTO detalle : detalles) {
                    if (vi != null) {
                        view = vi.inflate(R.layout.lista_item, layout_factura, false);
                        if (view != null) {
                            txtDetalle = view.findViewById(R.id.layout_lista_item);
                            txtDetalle.setText(detalle.getResume(this));
                            linearlayoutDetalleFactura.addView(view);
                        }
                    }
                }
            }
        }
    }

    private void cargarDatosFactura() {
        if (facturaDTO != null) {
            try {
                tv_resumen.setText(facturaDTO.getResumenValores());

                ClienteDTO cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(String.valueOf(facturaDTO.IdCliente));
                String sb = (cliente.RazonSocial) + (" - ")
                        + ((!cliente.NombreComercial.equals("") && cliente.NombreComercial != null ?
                        cliente.NombreComercial + (" - ") : ""))
                        + (cliente.Direccion) + (" - ")
                        + (cliente.Telefono1);
                tv_datos_cliente.setText(sb);

                setTitle("Factura #" + facturaDTO.NumeroFactura);

                ActionBar ab = getSupportActionBar();
                if (ab != null) {
                    ab.setDisplayHomeAsUpEnabled(true);
                }

            } catch (Exception e) {
                logCaughtException(e);
            }
        }
    }

    private void cargarDatosNotaCredito() {
        if (notaCreditoFacturaDTO != null && facturaDTO != null) {
            StringBuilder sbMensaje = new StringBuilder();
            sbMensaje.append("Subtotal Nota   :").append(String.valueOf((int) notaCreditoFacturaDTO.Subtotal));
            sbMensaje.append("\nDescuento Nota  :").append(String.valueOf((int) notaCreditoFacturaDTO.Descuento));
            sbMensaje.append("\nIva Total Nota  :").append(String.valueOf((int) notaCreditoFacturaDTO.Iva));
            sbMensaje.append("\nTotal Factura     :").append(String.valueOf((int) facturaDTO.Total));
            sbMensaje.append("\nTotal Nota      :").append(String.valueOf((int) notaCreditoFacturaDTO.Valor));
            sbMensaje.append("\nTotal A Pagar   :").append(String.valueOf((int) (facturaDTO.Total - notaCreditoFacturaDTO.Valor)));

            tv_resumen_nota.setText(sbMensaje);
            tv_numero_factura.setText(notaCreditoFacturaDTO.NumeroFactura);
            tv_numero_nota.setText(notaCreditoFacturaDTO.NumeroDocumento);
        }
    }
}
