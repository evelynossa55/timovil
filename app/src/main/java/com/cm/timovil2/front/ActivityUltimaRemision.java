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
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.wsentities.MDetUltFac;
import com.cm.timovil2.dto.wsentities.MDetUltRem;
import com.cm.timovil2.dto.wsentities.MUltFac;
import com.cm.timovil2.dto.wsentities.MUltRem;

import java.util.ArrayList;

/*Cambio realizado por Juan Sebastian Arenas Borja*/
public class ActivityUltimaRemision extends ActivityBase implements MenuItem.OnMenuItemClickListener {


    private TextView tv_numero_remision;
    private TextView tv_fecha_remision;
    private LinearLayout linearLayoutDetalle;
    private MUltRem ultimaRemision;
    private boolean exentoIva;
    private LayoutInflater vi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ultima_remision_activity);
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
        ultimaRemision = App.ultimaRemision;
        exentoIva = getIntent().getBooleanExtra("exentoIva", false);
        tv_numero_remision = findViewById(R.id.tv_numero_remision);
        tv_fecha_remision = findViewById(R.id.tv_fecha_remision);
        linearLayoutDetalle = findViewById(R.id.linearlayoutDetalleUltRemision);
    }

    private void cargarDatosCliente() {
        if (ultimaRemision != null) {
            if (tv_numero_remision != null && ultimaRemision.NumeroRemision != null) {
                tv_numero_remision.setText(ultimaRemision.NumeroRemision);
            }
            if (tv_fecha_remision != null && ultimaRemision.Fecha != null) {
                tv_fecha_remision.setText(ultimaRemision.Fecha);
            }
            if (linearLayoutDetalle != null && ultimaRemision.Detalle != null) {
                cargarDetalleUltimaRemision();
            }
        }
    }

    private void cargarDetalleUltimaRemision() {
        if (ultimaRemision.Detalle != null) {
            TextView txtDetalle;
            linearLayoutDetalle.removeAllViews();
            if (ultimaRemision.Detalle.isEmpty()) {
                txtDetalle = new TextView(this);
                txtDetalle.setText("------");
                linearLayoutDetalle.addView(txtDetalle);
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (MDetUltRem detalle : ultimaRemision.Detalle) {
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
        inflater.inflate(R.menu.menu_ultima_remision, menu);
        MenuItem item = menu.findItem(R.id.mnuUsarPedidoRemision);
        if (item != null) {
            item.setOnMenuItemClickListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        boolean cantidadNoIgual=false; //No es la cantidad anterior debido al stock actual
        for (MDetUltRem detUltRem : ultimaRemision.Detalle) {
            DetalleFacturaDTO detalle = buscarDetalle(detUltRem.IdProd, detUltRem.CodigoProducto);
            if (detalle != null) {
                if(asignarValores(detalle, detUltRem)){
                    cantidadNoIgual = true;
                }
            }
        }

        if(cantidadNoIgual){
            makeDialog("Alerta", "Algunos productos del pedido anterior poseen una " +
                            "cantidad mayor al stock actual, se añadira el total del stock para esos productos"
                    , ActivityUltimaRemision.this);
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

    private boolean asignarValores(DetalleFacturaDTO detalle, MDetUltRem mDetUltRem) {
        detalle.Cantidad = mDetUltRem.Cant;
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
            makeErrorDialog(e.getMessage(), ActivityUltimaRemision.this);
        }
        return stockOverFlowed;
    }

}
