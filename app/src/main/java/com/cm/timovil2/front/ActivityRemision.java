package com.cm.timovil2.front;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
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
import com.cm.timovil2.data.FormaPagoDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.FormaPagoDTO;
import com.cm.timovil2.dto.RemisionDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ActivityRemision extends ActivityBase implements MenuItem.OnMenuItemClickListener {

    private TextView tv_datos_cliente;
    private TextView tv_resumen;
    private TextView tv_NroPedido;
    private LinearLayout linearlayoutDetalleRemision;
    private LayoutInflater vi;

    private RemisionDTO remisionDTO;
    private String _Id_remision;

    //-----------------------
    private TextView lblFechaRemision;
    private TextView lblDatoFormaPago;
    private TextView lblDatoComentario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remision);
        setControls();
        App.actualActivity = this;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            _Id_remision = bundle.getString("_Id");
        }
        if (_Id_remision != null) {
            remisionDTO = new RemisionDAL(this).obtenerPorID(new String[]{_Id_remision});
            if (remisionDTO != null) {
                cargarDetalle(remisionDTO.DetalleRemision);
                cargarDatosRemision();
            } else {
                makeDialog("No se pudo cargar la remisión, por favor ingrese nuevamente [Remisión = null]", ActivityRemision.this);
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
        tv_datos_cliente = findViewById(R.id.lblDatosCliente);
        tv_resumen = findViewById(R.id.lblResumen);
        tv_NroPedido = findViewById(R.id.tvNroPedido);
        linearlayoutDetalleRemision = findViewById(R.id.linearlayoutDetalle);
        vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //--------------------------
        lblFechaRemision = findViewById(R.id.lblFechaRemision);
        lblDatoFormaPago = findViewById(R.id.lblDatoFormaPago);
        lblDatoComentario = findViewById(R.id.lblDatoComentario);
    }

    public boolean onMenuItemClick(MenuItem item) {
        imprimir();
        return false;
    }

    private void imprimir() {
        try {
            if (App.obtenerConfiguracion_imprimir(this)) {
                if (remisionDTO != null) {

                    Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                    printer.print(remisionDTO);
                } else {
                    makeErrorDialog("No es posible imprimir la remisión [Remisión = null]", ActivityRemision.this);
                }
            } else {
                makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityRemision.this);
            }
        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), ActivityRemision.this);
        }
    }

    private void cargarDetalle(ArrayList<DetalleRemisionDTO> detalles) {
        if (detalles != null) {
            TextView txtDetalle;
            linearlayoutDetalleRemision.removeAllViews();
            if (detalles.isEmpty()) {
                txtDetalle = new TextView(this);
                txtDetalle.setText("------");
                linearlayoutDetalleRemision.addView(txtDetalle);
                linearlayoutDetalleRemision.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearlayoutDetalleRemision.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (DetalleRemisionDTO detalle : detalles) {
                    if (vi != null) {
                        view = vi.inflate(R.layout.lista_item, linearlayoutDetalleRemision, false);
                        if (view != null) {
                            txtDetalle = view.findViewById(R.id.layout_lista_item);
                            if (lblFechaRemision != null) {
                                txtDetalle.setText(detalle.getResumeDetallado(this));
                            } else {
                                txtDetalle.setText(detalle.getResume(this));
                            }

                            linearlayoutDetalleRemision.addView(view);
                        }
                    }
                }
            }
        }
    }

    private void cargarDatosRemision() {
        if (remisionDTO != null) {
            try {
                tv_resumen.setText(remisionDTO.getResumenValores());
                tv_NroPedido.setText(!remisionDTO.NumeroPedido.equals("") ? remisionDTO.NumeroPedido : "Sin número de pedido");

                ClienteDTO cliente = new ClienteDAL(this).ObtenerClientePorIdCliente(String.valueOf(remisionDTO.IdCliente));
                String sb = (cliente.RazonSocial) + (" - ")
                        + ((!cliente.NombreComercial.equals("") && cliente.NombreComercial != null ?
                        cliente.NombreComercial + (" - ") : ""))
                        + (cliente.Direccion) + (" - ")
                        + (cliente.Telefono1);
                tv_datos_cliente.setText(sb);

                setTitle("Remisión #" + remisionDTO.NumeroRemision);

                ActionBar ab = getSupportActionBar();
                if (ab != null) {
                    ab.setDisplayHomeAsUpEnabled(true);
                }

                //-----------------------------------------------------
                if (lblFechaRemision != null) {
                    String fechaRemision = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US)
                            .format(new Date(remisionDTO.Fecha))
                            + (remisionDTO.Anulada ? "; ANULADA" : "")
                            + (remisionDTO.PendienteAnulacion || !remisionDTO.Sincronizada ? "; PENDIENTE POR DESCARGAR" : "");
                    lblFechaRemision.setText(fechaRemision);
                }

                if (remisionDTO.Comentario != null && !TextUtils.isEmpty(remisionDTO.Comentario)
                        && lblDatoComentario != null) {
                    lblDatoComentario.setText(remisionDTO.Comentario);
                }

                if (remisionDTO.ComentarioAnulacion != null && !TextUtils.isEmpty(remisionDTO.ComentarioAnulacion)
                        && lblDatoComentario != null) {
                    CharSequence comentario_aux = (TextUtils.isEmpty(lblDatoComentario.getText())
                            || lblDatoComentario.getText().equals("Sin comentario") ? "" : lblDatoComentario.getText());
                    String datoComentario = comentario_aux + " Motivo anulación: " + remisionDTO.ComentarioAnulacion;
                    lblDatoComentario.setText(datoComentario);
                }

                if (lblDatoFormaPago != null && remisionDTO.FormaPago != null
                        && !TextUtils.isEmpty(remisionDTO.FormaPago)) {
                    FormaPagoDTO formaPagoDTO = new FormaPagoDAL(this).obtenerPorCodigo(remisionDTO.FormaPago);
                    lblDatoFormaPago.setText(formaPagoDTO.Nombre);
                } else if (lblDatoFormaPago != null) {
                    lblDatoFormaPago.setText(getResources().getString(R.string.no_aplica));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
