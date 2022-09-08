package com.cm.timovil2.front;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterProductos;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.bl.utilities.zebra_datawedge.DWScanner;
import com.cm.timovil2.bl.utilities.zebra_datawedge.OnDataWedgeScannerDecodedData;
import com.cm.timovil2.data.DetalleListaPreciosDAL;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.wsentities.MDetalleListaPrecios;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ActivityDetalleFacturacion extends ActivityBase {

    private Spinner cboProductos;
    private Spinner cboListaPrecios;
    private EditText txtCantidad;
    private EditText txtDevoluciones;
    private EditText txtRotaciones;
    private EditText txtDescuentoAdicional;

    private TextView lblValorUnitario;
    private TextView lblSubtotal;
    private TextView lblDescuento;
    private TextView lblDevolucion;
    private TextView lblIva;
    private TextView lblTotal;
    private TextView lblIpoConsumo;
    private boolean exentoIva;
    private boolean isRemision;

    //-----------------------------
    Camera camera;
    Camera.Parameters parameters;
    boolean isFlash = false;
    boolean isOn = false;

    private DetalleFacturaDTO detalle = null;

    //-----------------------------
    //----------------------------------
    private DWScanner dataWedge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingreso_detalle_factura);
        App.actualActivity = this;
        setControls();
        cargarProductos();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if(dataWedge != null) dataWedge.stopScanner();
    }

    @Override
    protected void setControls() {

        cboProductos = findViewById(R.id.cboProductos);
        cboListaPrecios = findViewById(R.id.cboListaPrecios);
        txtCantidad = findViewById(R.id.txtCantidad);
        txtDevoluciones = findViewById(R.id.txtDevoluciones);
        txtRotaciones = findViewById(R.id.txtRotaciones);
        txtDescuentoAdicional = findViewById(R.id.txtDescuentoAdicional);
        TextView textViewDevolucion = findViewById(R.id.textViewDevolucion);
        TextView textViewRotaciones = findViewById(R.id.textViewRotaciones);
        TextView textViewDescuentoAdicional = findViewById(R.id.textViewDescuentoAdicional);
        exentoIva = getIntent().getBooleanExtra("exentoIva", false);
        isRemision = getIntent().getBooleanExtra("isRemision", false);

        switch (resolucion.IdCliente) {
            case Utilities.ID_IGLU:
                txtDevoluciones.setVisibility(View.GONE);
                txtRotaciones.setVisibility(View.GONE);
                textViewDevolucion.setVisibility(View.GONE);
                textViewRotaciones.setVisibility(View.GONE);
                break;
            case Utilities.ID_VEGA:
            case Utilities.ID_NATIPAN:
                txtDevoluciones.setVisibility(View.VISIBLE);
                txtRotaciones.setVisibility(View.GONE);
                textViewDevolucion.setVisibility(View.VISIBLE);
                textViewRotaciones.setVisibility(View.GONE);
                break;
            default:
                txtDevoluciones.setVisibility(View.VISIBLE);
                txtRotaciones.setVisibility(View.VISIBLE);
                textViewDevolucion.setVisibility(View.VISIBLE);
                textViewRotaciones.setVisibility(View.VISIBLE);
                break;
        }

        lblValorUnitario = findViewById(R.id.lblValorUnitario);
        lblSubtotal = findViewById(R.id.lblSubtotal);
        lblDescuento = findViewById(R.id.lblDescuento);
        lblDevolucion = findViewById(R.id.lblDevolucion);
        lblIva = findViewById(R.id.lblIva);
        lblTotal = findViewById(R.id.lblTotal);
        lblIpoConsumo = findViewById(R.id.lblIpoConsumo);

        cboProductos.setOnItemSelectedListener( new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                detalle = (DetalleFacturaDTO) cboProductos.getSelectedItem();
                productoSeleccionado();
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        cboListaPrecios.setOnItemSelectedListener( new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                PrecioProducto precioProducto = (PrecioProducto) cboListaPrecios.getItemAtPosition(position);
                detalle.ValorUnitario = precioProducto.valorPrecio;
                asignarValores();
                ponerValores();
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        txtCantidad.addTextChangedListener(tw);
        txtDevoluciones.addTextChangedListener(tw);
        txtRotaciones.addTextChangedListener(tw);
        txtDescuentoAdicional.addTextChangedListener(tw);

        if (resolucion.IdCliente.equals(Utilities.ID_CESAR_GALLEGO)
                || resolucion.IdCliente.equals(Utilities.ID_FR)
                || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                || resolucion.IdCliente.equals(Utilities.ID_MATERIALES_Y_HERRAMIENTAS)
                || resolucion.IdCliente.equals(Utilities.ID_FERNANDO)
                || resolucion.IdCliente.equals(Utilities.ID_GUAYAZAN)
                || resolucion.IdCliente.equals(Utilities.ID_SARY)
                || resolucion.IdCliente.equals(Utilities.ID_DOBLEVIA)
        ) {

            txtDescuentoAdicional.setVisibility(View.VISIBLE);
            textViewDescuentoAdicional.setVisibility(View.VISIBLE);

        } else {
            txtDescuentoAdicional.setVisibility(View.GONE);
            textViewDescuentoAdicional.setVisibility(View.GONE);
        }

    }

    private final TextWatcher tw = new TextWatcher() {

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            asignarValores();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            asignarValores();
        }
    };

    private void listarPreciosProducto() {
        try {
            ArrayList<PrecioProducto> p = new ArrayList<>();
            PrecioProducto precioProducto;
            switch (resolucion.IdCliente) {
                case Utilities.ID_FERNANDO_CARDONA:
                    if (detalle.ValorUnitario > 0) {
                        precioProducto = new PrecioProducto();
                        precioProducto.valorPrecio = detalle.ValorUnitario;
                        p.add(precioProducto);
                    } else if (detalle.Precio1 > 0) {
                        precioProducto = new PrecioProducto();
                        precioProducto.valorPrecio = detalle.Precio1;
                        p.add(precioProducto);
                    }
                    if (detalle.Precio2 > 0) {
                        precioProducto = new PrecioProducto();
                        precioProducto.valorPrecio = detalle.Precio2;
                        p.add(precioProducto);
                    }
                    if (detalle.Precio3 > 0) {
                        precioProducto = new PrecioProducto();
                        precioProducto.valorPrecio = detalle.Precio3;
                        p.add(precioProducto);
                    }
                    break;
                case Utilities.ID_LETICIA:
                    ArrayList<MDetalleListaPrecios> listaPrecios =
                            new DetalleListaPreciosDAL(this).ObtenerListado();

                    if (detalle.ValorUnitario >= 0) {
                        precioProducto = new PrecioProducto();
                        precioProducto.valorPrecio = detalle.ValorUnitario;
                        p.add(precioProducto);
                    }

                    for (MDetalleListaPrecios d : listaPrecios) {
                        boolean esPrimerPrecio = detalle.ValorUnitario == d.Precio;
                        if (detalle.IdProducto == d.IdProducto && !esPrimerPrecio) {
                            precioProducto = new PrecioProducto();
                            precioProducto.valorPrecio = (float) d.Precio;
                            p.add(precioProducto);
                        }
                    }
                    break;

                case Utilities.ID_PRUEBAS_TIMOVIL:
                    ArrayList<MDetalleListaPrecios> listaPrecios2 =
                            new DetalleListaPreciosDAL(this).ObtenerListado();

                    if (detalle.ValorUnitario >= 0) {
                        precioProducto = new PrecioProducto();
                        precioProducto.valorPrecio = detalle.ValorUnitario;
                        p.add(precioProducto);
                    }

                    for (MDetalleListaPrecios d : listaPrecios2) {
                        boolean esPrimerPrecio = detalle.ValorUnitario == d.Precio;
                        if (detalle.IdProducto == d.IdProducto && !esPrimerPrecio) {
                            precioProducto = new PrecioProducto();
                            precioProducto.valorPrecio = (float) d.Precio;
                            p.add(precioProducto);
                        }
                    }
                    break;

                default:
                    precioProducto = new PrecioProducto();
                    precioProducto.valorPrecio = detalle.ValorUnitario;
                    p.add(precioProducto);
                    break;
            }


            ArrayAdapter<PrecioProducto> adaptadorCombo = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, p);
            adaptadorCombo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cboListaPrecios.setAdapter(adaptadorCombo);

        } catch (Exception e) {
            logCaughtException(e);
            makeLToast("Error listando los precios: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        cboProductos.setAdapter(null);
        AdapterProductos adaptadorCombo = new AdapterProductos(this, App.DetalleFacturacion);
        cboProductos.setAdapter(adaptadorCombo);
    }

    private void asignarValores() {

        if (detalle == null) {
            makeLToast("Seleccione un producto");
            return;
        }

        if (txtCantidad.getText() != null
                && !txtCantidad.getText().toString().equals("")) {
            detalle.Cantidad = Integer.valueOf(txtCantidad.getText().toString());
        } else {
            detalle.Cantidad = 0;
        }

        if (txtDevoluciones.getText() != null
                && !txtDevoluciones.getText().toString().equals("")) {
            detalle.Devolucion = Integer.valueOf(txtDevoluciones.getText().toString());
        } else {
            detalle.Devolucion = 0;
        }

        if (txtRotaciones.getText() != null
                && !txtRotaciones.getText().toString().equals("")) {
            detalle.Rotacion = Integer.valueOf(txtRotaciones.getText().toString());
        } else {
            detalle.Rotacion = 0;
        }

        if (txtDescuentoAdicional.getText() != null
                && !txtDescuentoAdicional.getText().toString().equals("")) {

            try {

                detalle.DescuentoAdicional =
                        Float.valueOf(txtDescuentoAdicional.getText().toString());

                if (detalle.DescuentoAdicional <= 0
                        || detalle.DescuentoAdicional > 58) {
                    makeLToast("El descuento debe ser mayor a cero y como máximo del 58%");
                    detalle.DescuentoAdicional = 0;
                    txtDescuentoAdicional.setText("");
                }

            } catch (NumberFormatException ex) {
                logCaughtException(ex);
                detalle.DescuentoAdicional = 0;
                txtDescuentoAdicional.setText("");
            }

        } else {
            detalle.DescuentoAdicional = 0;
        }

        int cTotal = detalle.Cantidad; //Cantidad total
        if (!resolucion.IdCliente.equals(Utilities.ID_VEGA)) {
            if (resolucion.DevolucionRestaInventario) {
                cTotal += detalle.Devolucion;
            }
        }
        if (resolucion.RotacionRestaInventario) {
            cTotal += detalle.Rotacion;
        }

        try {

            boolean permitirFacturarSinInventario = App.obtenerConfiguracion_PermitirFacturarSinInventario(this);
            boolean manejarInventario = isRemision ? resolucion.ManejarInventarioRemisiones : resolucion.ManejarInventario;

            if (cTotal > detalle.StockDisponible
                    && manejarInventario
                    && !permitirFacturarSinInventario) {

                makeDialog("La cantidad disponible es: " + detalle.StockDisponible, ActivityDetalleFacturacion.this);

                detalle.Cantidad = 0;
                detalle.Devolucion = 0;
                detalle.Rotacion = 0;
                txtCantidad.setText("");
                txtDevoluciones.setText("");
                txtRotaciones.setText("");
                txtDescuentoAdicional.setText("");

            } else {

                if (detalle.ValorUnitario > 0) {
                    boolean devolucionAfectaVenta = isRemision ? resolucion.DevolucionAfectaRemision : resolucion.DevolucionAfectaVenta;
                    detalle.Subtotal = detalle.ValorUnitario * detalle.Cantidad;
                    detalle.SubtotalDevolucion = detalle.ValorUnitario * detalle.Devolucion;
                    detalle.ValorIpoConsumo = detalle.IpoConsumo * detalle.Cantidad;

                    float descuento_devolucion;
                    if (detalle.DescuentoAdicional > 0) {
                        detalle.Descuento = detalle.Subtotal * (detalle.DescuentoAdicional / 100);
                        descuento_devolucion = detalle.SubtotalDevolucion * (detalle.DescuentoAdicional / 100);
                    } else {
                        detalle.Descuento = detalle.Subtotal * (detalle.PorcentajeDescuento / 100);
                        descuento_devolucion = detalle.SubtotalDevolucion * (detalle.PorcentajeDescuento / 100);
                    }

                    detalle.DescuentoDevolucion = descuento_devolucion;

                    if (!exentoIva) {
                        detalle.Iva = (detalle.Subtotal - detalle.Descuento) * (detalle.PorcentajeIva / 100);
                        float iva_devolucion = (detalle.SubtotalDevolucion - detalle.DescuentoDevolucion) * (detalle.PorcentajeIva / 100);
                        detalle.IvaDevolucion = iva_devolucion;

                        if (detalle.PorcentajeIva == 5f) {
                            detalle.Iva5Devolucion = iva_devolucion;
                        } else if (detalle.PorcentajeIva == 19f) {
                            detalle.Iva19Devolucion = iva_devolucion;
                        }

                    } else {
                        detalle.Iva = 0;
                        detalle.IvaDevolucion = 0;
                        detalle.Iva5Devolucion = 0;
                        detalle.Iva19Devolucion = 0;
                    }

                    detalle.IpoconsumoDevolucion = detalle.IpoConsumo * detalle.Devolucion;
                    detalle.ValorDevolucion =
                            detalle.SubtotalDevolucion -
                                    detalle.DescuentoDevolucion +
                                    detalle.IvaDevolucion +
                                    detalle.IpoconsumoDevolucion;

                    detalle.Total =
                            (detalle.Subtotal - detalle.Descuento)
                                    + detalle.ValorIpoConsumo
                                    + (detalle.Iva);

                    if (devolucionAfectaVenta) {
                        detalle.Total = detalle.Total - detalle.ValorDevolucion;
                    }

                } else {

                    detalle.Subtotal = 0;
                    detalle.Descuento = 0;
                    detalle.Iva = 0;
                    detalle.Total = 0;
                    //detalle.Cantidad = 0; //Dejar comentado, para permitir facturar productos sin precio
                    detalle.Devolucion = 0;
                    detalle.Rotacion = 0;
                }
            }

            mostrarResumen();

        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityDetalleFacturacion.this);
        }
    }

    private void ponerValores() {
        try {

            txtCantidad.removeTextChangedListener(tw);
            txtDevoluciones.removeTextChangedListener(tw);
            txtRotaciones.removeTextChangedListener(tw);
            txtDescuentoAdicional.removeTextChangedListener(tw);
            txtCantidad.setText("");
            txtDevoluciones.setText("");
            txtRotaciones.setText("");
            txtDescuentoAdicional.setText("");

            if (detalle.Cantidad > 0) {
                txtCantidad.setText(String.valueOf(detalle.Cantidad));
            }

            if (detalle.Devolucion > 0) {
                txtDevoluciones.setText(String.valueOf(detalle.Devolucion));
            }

            if (detalle.Rotacion > 0) {
                txtRotaciones.setText(String.valueOf(detalle.Rotacion));
            }

            if (detalle.DescuentoAdicional > 0) {
                txtDescuentoAdicional.setText(String.valueOf(detalle.DescuentoAdicional));
            }

            txtCantidad.addTextChangedListener(tw);
            txtDevoluciones.addTextChangedListener(tw);
            txtRotaciones.addTextChangedListener(tw);
            txtDescuentoAdicional.addTextChangedListener(tw);

            mostrarResumen();

        } catch (Exception e) {
            logCaughtException(e);
            makeLToast(e.getMessage());
        }
    }

    private void mostrarResumen() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        lblValorUnitario.setText(formatter.format(detalle.ValorUnitario));
        lblSubtotal.setText(formatter.format(detalle.Subtotal));
        lblDescuento.setText(formatter.format(detalle.Descuento));
        lblIva.setText(formatter.format(detalle.Iva));
        lblTotal.setText(formatter.format(detalle.Total));
        lblIpoConsumo.setText(formatter.format(detalle.ValorIpoConsumo));
        lblDevolucion.setText(formatter.format(detalle.ValorDevolucion));
    }

    private void productoSeleccionado() {
        //Colocar precios producto:
        listarPreciosProducto();
        txtCantidad.requestFocus();
        ponerValores();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_facturacion_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAceptar:
                finish();
                break;
            case R.id.menuBuscarProducto:
                Intent i = new Intent(ActivityDetalleFacturacion.this, ActivityFiltroProducto.class);
                startActivityForResult(i, BUSCAR_PRODUCTO);
                break;
            case R.id.mnuFacturaReadBarCode:
                leerCodigoBarrasProducto();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Flash();
        if (BUSCAR_PRODUCTO == requestCode) {
            if (data != null) {
                int idProducto = data.getIntExtra("idProducto", 0);
                if (idProducto != 0) { // Seleccionar el producto en la lista
                    for (int i = 0; i < App.DetalleFacturacion.size(); i++) {
                        DetalleFacturaDTO d = App.DetalleFacturacion.get(i);
                        if (d.IdProducto == idProducto) {
                            cboProductos.setSelection(i);
                            return;
                        }
                    }
                } else {
                    makeDialog("No llegó nada", ActivityDetalleFacturacion.this);
                }
            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Scaner cancelado", Toast.LENGTH_LONG).show();
                } else {
                    String genericError = "No se pudo encontrar el producto, por favor verifica que se encuentra en tu lista de precios";
                    try {
                        //Formar= IdProducto.IdClienteTiMovil
                        String decodedData = result.getContents();
                        if (!TextUtils.isEmpty(decodedData)) {
                            String[] decodedDataArray = decodedData.split("\\.");
                            int idProducto = Integer.parseInt(decodedDataArray[0]);
                            detalle = obtenerDetalleProducto(idProducto);
                            if (detalle != null) {
                                cboProductos.setSelection(detalle.Index, true);
                            } else {
                                makeErrorDialog(genericError, this);
                            }
                        }

                    } catch (Exception e) {
                        makeErrorDialog(genericError, this);
                    }
                    Toast.makeText(this, "Scaneado: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private DetalleFacturaDTO obtenerDetalleProducto(int idProducto) {
        DetalleFacturaDTO detalle = null;
        for (DetalleFacturaDTO d : App.DetalleFacturacion) {
            if (d.IdProducto == idProducto) {
                detalle = d;
                break;
            }
        }
        return detalle;
    }

    private class PrecioProducto {
        float valorPrecio;

        @Override
        public String toString() {
            NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
            return f.format(valorPrecio);
        }
    }

    private void leerCodigoBarrasProducto() {
        String scannerType = App.obtenerPreferencias_ScannerType(this);
        switch (scannerType) {
            case DWScanner.CAMERA_TYPE:
                Flash();
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
                break;
            case DWScanner.ZEBRAESCANER_TYPE:
                dataWedge = new DWScanner(this);
                dataWedge.setDecodedDataListener(new OnDataWedgeScannerDecodedData() {
                    @Override
                    public void onDecodedData(String decodedData) {
                        if (!TextUtils.isEmpty(decodedData)) {
                            dataWedge.stopScanner();
                            Context context = getApplicationContext();
                            String genericError = "No se pudo encontrar el producto, por favor verifica que se encuentra en tu lista de precios";
                            try {
                                String[] decodedDataArray = decodedData.split("\\.");
                                int idProducto = Integer.parseInt(decodedDataArray[0]);
                                detalle = obtenerDetalleProducto(idProducto);
                                if (detalle != null)
                                    cboProductos.setSelection(detalle.Index, true);
                                else
                                    makeErrorDialog(genericError, context);
                            } catch (Exception e) {
                                makeErrorDialog(genericError, ActivityDetalleFacturacion.this);
                            }
                        }
                    }
                });

                dataWedge.createProfile();
                dataWedge.setDecoders();
                break;
        }
    }

    private void Flash() {
        if (isFlash) {

            if (!isOn) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
                isOn = true;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
                isOn = false;

            }
        } else {
            Log.e("Error:", "No se pudo inciar el flash");
        }
    }

    private final static int BUSCAR_PRODUCTO = 1100;
}