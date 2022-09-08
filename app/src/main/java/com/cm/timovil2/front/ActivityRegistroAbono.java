package com.cm.timovil2.front;

import java.util.ArrayList;
import java.util.Date;

import com.cm.timovil2.R;
import com.cm.timovil2.backup.AbonoBackUp;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.CuentaCajaDAL;
import com.cm.timovil2.data.FacturaCreditoDAL;
import com.cm.timovil2.dto.CuentaCajaDTO;
import com.cm.timovil2.dto.wsentities.MAbono;
import com.cm.timovil2.dto.wsentities.MFactCredito;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ActivityRegistroAbono extends ActivityBase {

    private TextView lblResumen;
    private EditText txtValorAbono;
    private MFactCredito factura;
    private AbonoFacturaDAL bl;
    private MAbono abono;
    private Spinner spinner_cuentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_abono_activity);
        App.actualActivity = this;
        bl = new AbonoFacturaDAL(this);
        setControls();
        cargarDatos();
        cargarCuentas();
    }

    @Override
    protected void setControls() {
        lblResumen = findViewById(R.id.lblResumenFactura);
        txtValorAbono = findViewById(R.id.txtValorAbono);
        spinner_cuentas = findViewById(R.id.spinner_cuentas);
    }

    private void cargarDatos() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            int _id = b.getInt("_Id");
            FacturaCreditoDAL facturaDal = new FacturaCreditoDAL(this);
            try {
                factura = facturaDal.ObtenerFacturaPorId(_id);
                lblResumen.setText(factura.toString());
            } catch (Exception e) {
                logCaughtException(e);
                makeErrorDialog(e.getMessage(), ActivityRegistroAbono.this);
            }
        }
    }

    private void cargarCuentas(){
        try{
            ArrayList<CuentaCajaDTO> cuentas = new CuentaCajaDAL(this).Obtener();
            ArrayAdapter<CuentaCajaDTO> spinnerArrayAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cuentas); //selected item will look like a spinner set from XML
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_cuentas.setAdapter(spinnerArrayAdapter);
        }catch (Exception e){
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityRegistroAbono.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_registro_abono_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuRegistrarAbono:
                registrarAbono();
                break;
            default:
                break;
        }
        return true;
    }

    private void registrarAbono() {
        if (txtValorAbono.getText() != null && txtValorAbono.getText().toString().equals("")) {
            makeDialog("Abonos", "Ingrese el valor del abono", ActivityRegistroAbono.this);
        } else {
            try {
                CuentaCajaDTO cuenta = (CuentaCajaDTO) spinner_cuentas.getSelectedItem();
                abono = new MAbono();
                abono.IdFactura = factura.IdFactura;
                abono.NumeroFactura = factura.NumeroFactura;
                abono.Fecha = Utilities.FechaHoraAnsi(new Date());
                abono.Valor = Float.parseFloat(txtValorAbono.getText().toString());
                abono.IdCuentaCaja = cuenta.IdCuentaCaja;
                abono.Saldo = factura.Saldo - abono.Valor;
                Date now = new Date();
                abono.DiaCreacion = Utilities.FechaAnsi(now);
                abono.FechaCreacion = now.getTime();
                abono.EnviadoDesde = Utilities.FACTURA_ENVIADA_DESDE_FACTURACION;

                bl = new AbonoFacturaDAL(this);
                bl.insertar(abono);
                txtValorAbono.setEnabled(false);

                if (!abono.Sincronizado) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                String respuesta = bl.sincronizarAbono(abono);

                                if (respuesta.equals("Sincronizando")) {
                                    respuesta = "El abono ya se estaba sincronizando, por favor intenta nuevamente";
                                }

                                if (respuesta.equals("OK")) {
                                    abono.Sincronizado = true;
                                    mostrarMensajeYsalir("TiMovil", "Abono creado correctamente");
                                }else{
                                    mostrarMensajeYsalir("Abono", respuesta);
                                }

                            } catch (Exception e) {
                                //logCaughtException(e);
                                App.SincronizandoAbonoFacturaId.remove(Integer.valueOf(abono._Id));
                                App.SincronizandoAbonoFactura = App.SincronizandoAbonoFacturaId.size() > 0;
                                mostrarMensajeYsalir("Error", "TiMovil - error descargando abono: " + e.getMessage());
                            }
                        }
                    }).start();
                }

                if (BackUpJsonAbonosThread.getState() == Thread.State.NEW) {
                    BackUpJsonAbonosThread.start();
                }

            } catch (Exception e) {
                logCaughtException(e);
                makeDialog("Error abono", e.getMessage(), ActivityRegistroAbono.this);
            }
        }
    }

    private void mostrarMensajeYsalir(final String titulo, final String mensaje) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder d = new AlertDialog.Builder(ActivityRegistroAbono.this);
                d.setTitle(titulo);
                d.setMessage(mensaje);
                d.setCancelable(false);
                d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo, int id) {
                        finish();
                    }
                });
                d.show();
            }
        });
    }

    Thread BackUpJsonAbonosThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                new AbonoBackUp(context).makeBackUp();
            } catch (Exception ex) {
                logCaughtException(ex);
            }
        }
    });
}