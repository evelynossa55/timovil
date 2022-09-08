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
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.FormaPagoDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.FacturaDTO;

import java.util.ArrayList;

public class ActivityFactura extends ActivityBase implements MenuItem.OnMenuItemClickListener {

    private TextView tv_datos_cliente;
    private TextView tv_resumen;
    private TextView tv_forma_pago;
    private TextView tv_tipo_documento;
    private LinearLayout linearlayoutDetalleFactura;
    private LayoutInflater vi;
    private FacturaDTO facturaDTO;
    private String _Id_factura;
    private LinearLayout layout_factura;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factura_activity);
        setControls();
        App.actualActivity = this;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            _Id_factura = bundle.getString("_Id");
        }
        if (_Id_factura != null) {
            facturaDTO = new FacturaDAL(this).obtenerPorID(new String[]{_Id_factura});
            if (facturaDTO != null) {
                cargarDetalle(facturaDTO.DetalleFactura);
                cargarDatosFactura();
            } else {
                makeDialog("No se pudo cargar la factura, por favor ingrese nuevamente [Factura = null]", ActivityFactura.this);
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
        tv_forma_pago = findViewById(R.id.tv_forma_pago);
        tv_tipo_documento = findViewById(R.id.tv_tipo_documento);
        linearlayoutDetalleFactura = findViewById(R.id.linearlayoutDetalleFactura);
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
                if (facturaDTO != null) {

                    Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);

                    if ((resolucion.IdCliente.equals(Utilities.ID_FR) && facturaDTO.Remision)
                            || (resolucion.IdCliente.equals(Utilities.ID_JAF) &&
                            resolucion.CodigoRuta.equals("50"))) {
                        printer.printFacturaComoRemision(facturaDTO);
                    } else {
                        printer.print(facturaDTO);
                    }

                } else {
                    makeErrorDialog("No es posible imprimir la factura [Factura = null]", ActivityFactura.this);
                }
            } else {
                makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityFactura.this);
            }
        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), ActivityFactura.this);
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
                String codigo_forma_pago = facturaDTO.FormaPago;
                tv_forma_pago.setText(new FormaPagoDAL(this).obtenerPorCodigo(codigo_forma_pago).Nombre.toUpperCase());
                tv_tipo_documento.setText(facturaDTO.TipoDocumento.toUpperCase());

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
                e.printStackTrace();
            }
        }
    }

}
