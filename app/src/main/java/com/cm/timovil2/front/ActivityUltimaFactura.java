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
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.wsentities.MDetUltFac;
import com.cm.timovil2.dto.wsentities.MUltFac;

public class ActivityUltimaFactura extends ActivityBase implements MenuItem.OnMenuItemClickListener {

    private TextView tv_numero_factura;
    private TextView tv_fecha_venta;
    private LinearLayout linearLayoutDetalle;
    private MUltFac ultimaFactura;
    private boolean exentoIva;
    private LayoutInflater vi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ultima_factura_activity);
        setControls();
        App.actualActivity = this;
        vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cargarDatosCliente();
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void setControls() {
        ultimaFactura = App.ultimaFactura;
        exentoIva = getIntent().getBooleanExtra("exentoIva", false);
        tv_numero_factura = findViewById(R.id.tv_numero_factura);
        tv_fecha_venta = findViewById(R.id.tv_fecha_venta);
        linearLayoutDetalle = findViewById(R.id.linearlayoutDetalleUlt);
    }

    private void cargarDatosCliente() {
        if (ultimaFactura != null) {
            if (tv_numero_factura != null && ultimaFactura.NumeroFactura != null) {
                tv_numero_factura.setText(ultimaFactura.NumeroFactura);
            }
            if (tv_fecha_venta != null && ultimaFactura.Fecha != null) {
                tv_fecha_venta.setText(ultimaFactura.Fecha);
            }
            if (linearLayoutDetalle != null && ultimaFactura.Detalle != null) {
                cargarDetalleUltimaFactura();
            }
        }
    }

    private void cargarDetalleUltimaFactura() {
        if (ultimaFactura.Detalle != null) {
            TextView txtDetalle;
            linearLayoutDetalle.removeAllViews();
            if (ultimaFactura.Detalle.isEmpty()) {
                txtDetalle = new TextView(this);
                txtDetalle.setText("------");
                linearLayoutDetalle.addView(txtDetalle);
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (MDetUltFac detalle : ultimaFactura.Detalle) {
                    if (vi != null) {
                        view = vi.inflate(R.layout.lista_item, linearLayoutDetalle, false);
                        if (view != null) {
                            txtDetalle = view.findViewById(R.id.layout_lista_item);
                            txtDetalle.setText(detalle.obtenerResumen(this));
                            linearLayoutDetalle.addView(view);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ultima_factura, menu);
        MenuItem item = menu.findItem(R.id.mnuUsarPedido);
        if (item != null) {
            item.setOnMenuItemClickListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        boolean cantidadNoIgual=false; //No es la cantidad anterior debido al stock actual
        for (MDetUltFac detUltFac : ultimaFactura.Detalle) {
            DetalleFacturaDTO detalle = buscarDetalle(detUltFac.IdProd, detUltFac.CodigoProducto);
            if (detalle != null) {
                if(asignarValores(detalle, detUltFac)){
                    cantidadNoIgual = true;
                }
            }
        }

        if(cantidadNoIgual){
            makeDialog("Alerta", "Algunos productos del pedido anterior poseen una " +
                    "cantidad mayor al stock actual, se añadira el total del stock para esos productos"
            , ActivityUltimaFactura.this);
        }

        setResult(RESULT_OK);
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return true;
    }

    private DetalleFacturaDTO buscarDetalle(int idProd, String Codigo) {
        DetalleFacturaDTO dto = null;
        for (DetalleFacturaDTO detalle : App.DetalleFacturacion) {
            if (detalle.IdProducto == idProd && detalle.Codigo.equals(Codigo)) {
                dto = detalle;
                break;
            }
        }
        return dto;
    }

    private boolean asignarValores(DetalleFacturaDTO detalle, MDetUltFac mDetUltFac) {
        detalle.Cantidad = mDetUltFac.Cant;
        detalle.Devolucion = 0;
        detalle.Rotacion = 0;
        boolean stockOverFlowed = false; //La cantidad del pedido anterior sobrepasa el stock actual?

        try {
            if (detalle.Cantidad > detalle.StockDisponible
                    && resolucion.ManejarInventario) {
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
            makeErrorDialog(e.getMessage(), ActivityUltimaFactura.this);
        }
        return stockOverFlowed;
    }
}
