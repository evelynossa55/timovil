package com.cm.timovil2.front;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.backup.FacturaBackUp;
import com.cm.timovil2.backup.NoVentaBackUp;
import com.cm.timovil2.backup.NoVentaPedidoBackUp;
import com.cm.timovil2.backup.NotaCreditoFacturaPorDevolucionBackUp;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.EFacturaUtilities;
import com.cm.timovil2.bl.utilities.TaskVerificarFechaHoraWS;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.EntregadorDAL;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.FormaPagoDAL;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.data.PedidoCallcenterDAL;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.EntregadorDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.FormaPagoDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MDetUltFac;
import com.cm.timovil2.dto.wsentities.MUltFac;
import com.cm.timovil2.dto.wsentities.MUltRem;
import com.cm.timovil2.proxy.ProxyProducto;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class ActivityFacturacion extends ActivityBase implements OnClickListener {

    private LayoutInflater vi;
    private ViewGroup parent;
    private TextView lblDatosCliente;
    private TextView lblFecha;
    private FacturaDTO factura;
    private NotaCreditoFacturaDTO notaCreditoFactura;
    private ClienteDTO cliente;
    private Spinner cboFormasDePago;
    private Spinner cboTiposDocumento;
    private Spinner cboEntregadores;
    private TextView lblResumen;
    private TextView txtNroPedido;
    private TextView txtFormasPago;
    private EditText etNroPedido;
    private TextView txtEntregador;
    private FacturaDAL facturaDAL;

    //requests codes
    private final int INGRESAR_DETALLE = 1;
    private final int CONFIGURAR_GPS = 3;
    private final int ULTIMA_FACTURA = 4;
    private final int NO_VENTA = 5;
    private final int AUTOMATIC_TIME = 6;

    private boolean remision;
    private boolean isPedidoCallcenter;
    private String numeroFactura;
    private LinearLayout linearLayoutDetalle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facturacion);
        App.actualActivity = this;
        facturaDAL = new FacturaDAL(this);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {

            reloadResolucion();
            if (resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                    || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || resolucion.IdCliente.equals(Utilities.ID_IGLU)
            //        || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
            ) {
                verificar_fecha_facturacion_con_ultima_factura();
            }

            setControls();
            iniciarFactura();
            cargarDetalleFactura();
            obtenerUbicacion();

        } catch (Exception e) {
            logCaughtException(e);
            mostrarErrorYSalir(e.toString(), ActivityFacturacion.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYSalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta", ActivityFacturacion.this);
        } else {
            checkGPS();
            //checkTimeAutomatic();
        }
    }

    @Override
    protected void setControls() {
        parent = findViewById(R.id.layout_container);
        linearLayoutDetalle = findViewById(R.id.linearlayoutDetalle);
        lblDatosCliente = findViewById(R.id.lblDatosCliente);
        lblFecha = findViewById(R.id.lblFecha);
        cboFormasDePago = findViewById(R.id.cboFormaDePago);
        cboTiposDocumento = findViewById(R.id.cboTipoDocumento);
        lblResumen = findViewById(R.id.lblResumen);
        txtNroPedido = findViewById(R.id.txtNroPedido);
        txtFormasPago = findViewById(R.id.txtFormasPago);
        etNroPedido = findViewById(R.id.etNroPedido);
        txtEntregador = findViewById(R.id.txtEntregador);
        cboEntregadores = findViewById(R.id.cboEntregadores);
        lblDatosCliente.setOnClickListener(this);

        ImageButton mnuIngresarDetalle = findViewById(R.id.mnuIngresarDetalle);
        ImageButton mnuImprimir = findViewById(R.id.mnuImprimir);
        ImageButton mnuIngresarComentario = findViewById(R.id.mnuIngresarComentario);
        ImageButton mnuVerCartera = findViewById(R.id.mnuVerCartera);
        ImageButton mnuVerUltimaFactura = findViewById(R.id.mnuVerUltimaFactura);
        ImageButton mnuNoVenta = findViewById(R.id.mnuNoVenta);

        mnuIngresarDetalle.setOnClickListener(this);
        mnuImprimir.setOnClickListener(this);
        mnuIngresarComentario.setOnClickListener(this);
        mnuVerCartera.setOnClickListener(this);
        mnuVerUltimaFactura.setOnClickListener(this);
        mnuNoVenta.setOnClickListener(this);
    }



    private void iniciarFactura() throws Exception {

        if (resolucion.CodigoRuta == null || resolucion.SiguienteFactura <= 0) {
            throw new Exception("Error en el número de factura. Vuelva a ingresar al programa");
        }

        if (resolucion.SiguienteFacturaPOS <= 0) {
            throw new Exception("Error en el número de factura POS. Por favor vuelva a cargar datos");
        }

        if (!resolucion.EsDatoValido() || resolucion.PrefijoFacturacion == null) {
            throw new Exception("Error en el número de factura. Vuelva a ingresar al programa");
        }

        factura = new FacturaDTO();
        Intent intent = getIntent();
        factura.FacturaPos = intent.getBooleanExtra("pos", false);
        if (factura.FacturaPos == false)
            factura.FacturaPos = esFacturacionPOSCliente();
        isPedidoCallcenter = intent.getBooleanExtra("isPedidoCallcenter", false);
        boolean codigoBarrasLeido = intent.getBooleanExtra("codigoBarrasLeido", false);
        verificar_resolucion(resolucion, factura.FacturaPos);

        App.EfectivoPagado = 0;
        App.ComentarioFactura = "";

        remision = false;
        if (intent.getExtras() != null) {
            if (intent.getExtras().getString("codigoBodega") != null) {
                factura.CodigoBodega = intent.getExtras().getString("codigoBodega");
            }
        }

        if (factura.FacturaPos) {
            numeroFactura = resolucion.PrefijoFacturacionPOS + "-" + resolucion.SiguienteFacturaPOS;
        } else {
            numeroFactura = resolucion.PrefijoFacturacion + "-" + resolucion.SiguienteFactura;
        }

        FacturaDTO facturaExiste = facturaDAL.obtenerPorNumeroFac(numeroFactura);
        if (facturaExiste != null
                && facturaExiste.NumeroFactura != null
                && facturaExiste.NumeroFactura.equals(numeroFactura)) {
            mostrarErrorYSalir("Debes corregir el siguiente numero de factura POS. " + numeroFactura, this);
            return;
        }

        setTitle("#" + numeroFactura);
        if (numeroFactura.equals("-0") || numeroFactura.equals("null-0")) {
            throw new Exception("Error número de factura: -0 (Ingrese de nuevo a la aplicación)");
        }


        factura.NumeroFactura = numeroFactura;

        if (factura.FacturaPos) {
            factura.IdResolucion = resolucion.IdResolucionPOS;
        } else {
            factura.IdResolucion = resolucion.IdResolucion;
        }

        cargarDatosCliente();
        cargarFormasDePago();
        cargarTiposDocumento();
        cargarEntregadores();

        factura.IsPedidoCallcenter = isPedidoCallcenter;
        factura.CreadaConCodigoBarras = codigoBarrasLeido;
        if (isPedidoCallcenter
                && App.pedido_actual.IdCliente == factura.IdCliente) {
            factura.IdPedido = App.pedido_actual.IdPedido;
            factura.IdCaso = App.pedido_actual.IdCaso;
        } else {
            factura.IdPedido = 0;
            factura.IdCaso = 0;
            App.pedido_actual = null;
            factura.IsPedidoCallcenter = false;
            limpiarDetalle();
        }

        setLblFecha(DateTime.now());
    }

    private boolean esFacturacionPOSCliente() {
        int id = getIntent().getIntExtra("_id", 0);
        boolean resultado = new ClienteDAL(this).ObtenerClientePorId(id).FacturacionPOSCliente;
        return resultado;
    }

    private void cargarDatosCliente() {
        try {
            String sb;
            if (factura.FacturaPos) {
                sb = "FACTURA POS";
                int id = getIntent().getIntExtra("_id", 0);
                cliente = new ClienteDAL(this).ObtenerClientePorId(id);
            } else {
                int id = getIntent().getIntExtra("_id", 0);
                cliente = new ClienteDAL(this).ObtenerClientePorId(id);
                sb = ("(")+(cliente.Identificacion)+(") - ")+(cliente.RazonSocial) + (" - ")
                        + ((!cliente.NombreComercial.equals("") ? cliente.NombreComercial + (" - ") : ""))
                        + (cliente.Direccion) + (" - ")
                        + (cliente.Telefono1);


                factura.IdCliente = cliente.IdCliente;
                factura.Identificacion = cliente.Identificacion;
                factura.RazonSocial = cliente.RazonSocial;
                factura.Negocio = cliente.NombreComercial;
                factura.Direccion = cliente.Direccion;
                factura.Telefono = cliente.Telefono1;
            }

            lblDatosCliente.setText(sb);
        } catch (Exception e) {
            logCaughtException(e);
            makeLToast("Error cargando los datos del cliente: " + e.getMessage());
        }
    }

    private void cargarFormasDePago() {
        try {
            cboFormasDePago.setAdapter(null);
            ArrayList<FormaPagoDTO> listFormaPago = new FormaPagoDAL(this).ObtenerListado();
            int selectedPosition = 0;
            boolean credito = cliente != null && cliente.Credito;
            boolean permitirCambiarFormaPago = App.obtenerConfiguracion_PermitirCambiarFormaDePago(this);
            boolean formaPagoFlexible = cliente != null && cliente.FormaPagoFlexible;

            if (credito && listFormaPago.size() >= 2) {
                if (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                        || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                        || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)) {
                    txtNroPedido.setVisibility(View.VISIBLE);
                    etNroPedido.setVisibility(View.VISIBLE);
                    txtFormasPago.setVisibility(View.GONE);
                    cboFormasDePago.setVisibility(View.GONE);
                    listFormaPago.remove(0);
                } else {
                    txtNroPedido.setVisibility(View.GONE);
                    etNroPedido.setVisibility(View.GONE);
                    txtFormasPago.setVisibility(View.VISIBLE);
                    cboFormasDePago.setVisibility(View.VISIBLE);
                    if (!permitirCambiarFormaPago && listFormaPago.size() >= 2 && !formaPagoFlexible) {
                        listFormaPago.remove(0);
                    }
                }
            } else {
                txtNroPedido.setVisibility(View.GONE);
                etNroPedido.setVisibility(View.GONE);
                txtFormasPago.setVisibility(View.VISIBLE);
                cboFormasDePago.setVisibility(View.VISIBLE);
                if (!permitirCambiarFormaPago && listFormaPago.size() >= 2 && !formaPagoFlexible) {
                    listFormaPago.remove(1);
                }
            }

            ArrayAdapter<FormaPagoDTO> adaptadorCombo = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, listFormaPago);
            adaptadorCombo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cboFormasDePago.setAdapter(adaptadorCombo);
            cboFormasDePago.setSelection(selectedPosition);
        } catch (Exception e) {
            logCaughtException(e);
            makeLToast("Error cargando las formas de pago: " + e.getMessage());
        }
    }

    private void cargarEntregadores() {

        ArrayList<EntregadorDTO> entregadores = new EntregadorDAL(this).Obtener();

        if (entregadores != null && entregadores.size() > 0) {

            txtEntregador.setVisibility(View.VISIBLE);
            cboEntregadores.setVisibility(View.VISIBLE);

            EntregadorDTO aux = new EntregadorDTO();
            aux.IdEmpleado = -1;
            aux.NombreCompleto = "-----";
            entregadores.add(0, aux);

            ArrayAdapter<EntregadorDTO> dataAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, entregadores);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cboEntregadores.setAdapter(dataAdapter);

        } else {
            txtEntregador.setVisibility(View.GONE);
            cboEntregadores.setVisibility(View.GONE);
        }
    }

    private void cargarTiposDocumento() {
        try {
            cboTiposDocumento.setAdapter(null);

            List<String> list = new ArrayList<>();
            list.add("FACT");

            if (resolucion.IdCliente.equals(Utilities.ID_DOBLEVIA)) {
                list.add("REM");
                list.add("SE");
                list.add("C.REM");
                list.add("DEV");
                list.add("DEV REM");
            }

            if (resolucion.IdCliente.equals(Utilities.ID_FR)) {
                list.add("REM");
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cboTiposDocumento.setAdapter(dataAdapter);

            cboTiposDocumento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (resolucion.IdCliente.equals(Utilities.ID_FR)) {
                        Object selected = cboTiposDocumento.getSelectedItem();
                        if (selected != null && !TextUtils.isEmpty(selected.toString()) && selected.toString().equals("REM")) {
                            numeroFactura = resolucion.CodigoRuta + "-" + resolucion.SiguienteRemision;
                            remision = true;
                        } else {
                            numeroFactura = resolucion.PrefijoFacturacion + "-" + resolucion.SiguienteFactura;
                            remision = false;
                        }
                    } else {

                        if (factura.FacturaPos) {
                            numeroFactura = resolucion.PrefijoFacturacionPOS + "-"
                                    + resolucion.SiguienteFacturaPOS;
                        } else {
                            numeroFactura = resolucion.PrefijoFacturacion + "-"
                                    + resolucion.SiguienteFactura;
                        }

                        remision = false;
                    }

                    factura.NumeroFactura = numeroFactura;
                    setTitle("#" + numeroFactura);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (Exception e) {
            logCaughtException(e);
            makeLToast("Error cargando los tipos de documentos: " + e.getMessage());
        }
    }

    private void iniciarValoresFactura() {
        factura.Subtotal = 0;
        factura.Descuento = 0;
        factura.Iva = 0;
        factura.IpoConsumo = 0;
        factura.ValorDevolucion = 0;
    }

    private void setLblFecha(DateTime date) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");
        // Printing the date
        lblFecha.setText(dtfOut.print(date));
    }

    private void limpiarDetalle() {
        for (DetalleFacturaDTO d : App.DetalleFacturacion) {
            d.Cantidad = 0;
            d.Devolucion = 0;
            d.Rotacion = 0;
        }
    }

    private void cargarDetalleFactura() {
        try {

            iniciarValoresFactura();
            ArrayList<DetalleFacturaDTO> detalle = new ArrayList<>();
            for (DetalleFacturaDTO d : App.DetalleFacturacion) {

                if (d.Cantidad != 0 || d.Devolucion != 0 || d.Rotacion != 0) {

                    if (d.DescuentoAdicional > 0) {
                        d.PorcentajeDescuento = d.DescuentoAdicional;
                    }

                    if (resolucion.IdCliente.equals(Utilities.ID_FR) && remision) {
                        factura.Subtotal += (d.Subtotal + d.Iva);
                        factura.Iva += 0;
                        factura.Descuento += d.Descuento;
                        factura.ValorDevolucion += Utilities.dosDecimalesDouble(d.ValorDevolucion, 2);
                        factura.IpoConsumo += d.ValorIpoConsumo;
                        factura.Devolucion += d.Devolucion;
                        factura.Rotacion += d.Rotacion;
                        factura.Cantidad += d.Cantidad;

                        d.ValorUnitario = d.ValorUnitario + (d.ValorUnitario * d.PorcentajeIva / 100);
                        d.Subtotal = Utilities.dosDecimalesDouble(d.Subtotal + d.Iva, 2);
                        d.Iva = 0;

                    } else {

                        factura.Subtotal += Utilities.dosDecimalesDouble(d.Subtotal, 2);
                        factura.Iva += d.Iva;
                        factura.Descuento += d.Descuento;
                        factura.ValorDevolucion += Utilities.dosDecimalesDouble(d.ValorDevolucion, 2);
                        factura.IpoConsumo += d.ValorIpoConsumo;
                        factura.Devolucion += d.Devolucion;
                        factura.Rotacion += d.Rotacion;
                        factura.Cantidad += d.Cantidad;
                    }

                    detalle.add(d);
                }
            }

            //------RETE FUENTE--------------
            if (cliente != null
                    && cliente.ReteFuente
                    && ((factura.Subtotal - factura.Descuento) > resolucion.TopeRetefuente)) {
                factura.PorcentajeRetefuente = resolucion.PorcentajeRetefuente;
            } else {
                factura.PorcentajeRetefuente = 0;
            }

            if (factura.PorcentajeRetefuente > 0) {
                factura.Retefuente = (factura.Subtotal - factura.Descuento)
                        * (factura.PorcentajeRetefuente / 100);
            } else {
                factura.Retefuente = 0;
            }
            //--------------------------------

            //-------RETE IVA-----------------
            float porcentajeReteIva = 0;
            float topeReteIva = App.obtenerConfiguracion_TopeReteIva(context);
            if (cliente != null
                    && cliente.ReteIva
                    && ((factura.Subtotal - factura.Descuento) > topeReteIva)) {
                porcentajeReteIva = App.obtenerConfiguracion_PorcentajeReteIva(context);
            }

            if (porcentajeReteIva > 0 && factura.Iva > 0) {
                factura.ReteIva = factura.Iva * (porcentajeReteIva / 100);
            } else {
                factura.ReteIva = 0;
            }
            //--------------------------------

            factura.Total = factura.Subtotal
                    - factura.Descuento
                    - factura.Retefuente
                    - factura.ReteIva
                    + factura.Iva
                    + factura.IpoConsumo;

            if (resolucion.DevolucionAfectaVenta) {
                factura.Total = factura.Total - factura.ValorDevolucion;
            }

            factura.ValorDevolucion = Utilities.dosDecimalesDouble(factura.ValorDevolucion, 2);
            factura.Retefuente = Utilities.dosDecimalesDouble(factura.Retefuente, 2);
            factura.Subtotal = Utilities.dosDecimalesDouble(factura.Subtotal, 2);
            factura.Total = Utilities.dosDecimalesDouble(factura.Total, 2);

            mostrarDetalleFactura(detalle);
            lblResumen.setText(factura.getResumenValores());

        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog("Error cargando el detalle: " + e.getMessage(), ActivityFacturacion.this);
        }
    }

    private void mostrarDetalleFactura(ArrayList<DetalleFacturaDTO> detalles) {
        if (detalles != null) {
            TextView txtDetalle;
            linearLayoutDetalle.removeAllViews();
            if (detalles.isEmpty()) {
                txtDetalle = new TextView(this);
                String sin_productos = "sin productos";
                txtDetalle.setText(sin_productos);
                linearLayoutDetalle.addView(txtDetalle);
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (DetalleFacturaDTO detalle : detalles) {
                    if (vi != null) {
                        view = vi.inflate(R.layout.lista_item, parent, false);
                        if (view != null) {
                            txtDetalle = view.findViewById(R.id.layout_lista_item);
                            txtDetalle.setText(detalle.getResume(this));
                            linearLayoutDetalle.addView(view);
                        }
                    }
                }
            }
        }
    }

    private void pasarAlDetalle(ClienteDTO client) {
        if (factura.Guardada) {
            makeDialog("La factura ya está guardada y por lo tanto no puede modificarse.", ActivityFacturacion.this);
            return;
        }

        Intent i = new Intent(ActivityFacturacion.this, ActivityDetalleFacturacion.class);
        i.putExtra("isRemision", false);
        i.putExtra("exentoIva", client != null && client.ExentoIva);
        startActivityForResult(i, INGRESAR_DETALLE);
    }



    private void pasarAlComentario() {

        if (factura.Guardada) {
            makeDialog("La factura ya está guardada y por lo tanto no puede modificarse.", ActivityFacturacion.this);
            return;
        }

        Intent i = new Intent(ActivityFacturacion.this, ActivityComentarioFactura.class);
        startActivity(i);
    }

    public void onClick(View arg) {

        int id = arg.getId();

        switch (id) {
            case R.id.mnuImprimir:
                imprimir();
                break;
            case R.id.mnuIngresarDetalle:
                pasarAlDetalle(cliente);
                break;
            case R.id.mnuNoVenta:
                if (cliente == null) break;
                if (factura.Guardada) {
                    makeDialog("La factura ya está guardada y por lo tanto no puede modificarse.",
                            ActivityFacturacion.this);
                } else {
                    Intent intent = new Intent(ActivityFacturacion.this, ActivityNoVenta.class);
                    intent.putExtra("IdCliente", factura.IdCliente);
                    intent.putExtra("IsPedidoCallcenter", isPedidoCallcenter);
                    startActivityForResult(intent, NO_VENTA);
                }
                break;
            case R.id.mnuIngresarComentario:
                pasarAlComentario();
                break;
            case R.id.mnuVerCartera:
                if (cliente == null) break;
                String mensaje = TextUtils.isEmpty(cliente.CarteraPendiente) ? "" : cliente.CarteraPendiente.replace('{', '\n');
                String mensaje2 = TextUtils.isEmpty(cliente.Remisiones) ? "" : cliente.Remisiones.replace('{', '\n');
                String mensaje3 = TextUtils.isEmpty(cliente.ValorVentasMes) ? "" : cliente.ValorVentasMes.replace('{', '\n');
                String mensaje4 = Utilities.FormatoMoneda(resolucion.ValorVentaMensual);
                Intent intent = new Intent(this, ActivityCartera.class);
                intent.putExtra("cartera", mensaje);
                intent.putExtra("remisiones", mensaje2);
                intent.putExtra("ventasMes", mensaje3);
                intent.putExtra("ventasMesRuta", mensaje4);
                startActivity(intent);
                break;
            case R.id.mnuVerUltimaFactura:
                if (factura.Guardada) {
                    makeDialog("La factura ya está guardada y por lo tanto no puede modificarse.",
                            ActivityFacturacion.this);
                } else if (cliente != null) {
                    new ObtenerUltimaFacturaTask().execute();
                }
                break;
            case R.id.lblDatosCliente:
                pasarAlDetalle(cliente);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INGRESAR_DETALLE) {
            cargarDetalleFactura();
        } else if (requestCode == CONFIGURAR_GPS) {
            if (!isGpsActive()) {
                makeLToast("Debe habilitar el GPS para facturar");
                finish();
            }
            obtenerUbicacion();
        } else if (requestCode == ULTIMA_FACTURA) {
            if (resultCode == RESULT_OK) {
                makeLToast("Se han agregado productos del pedido anterior del cliente");
            }
            cargarDetalleFactura();
        } else if (requestCode == NO_VENTA) {
            if (resultCode == RESULT_OK) {

                if (isPedidoCallcenter && (factura.IdPedido > 0 || factura.IdCaso > 0)) {
                    new PedidoCallcenterDAL(this).eliminarPedido(factura.IdPedido, factura.IdCaso);
                    App.pedido_actual = null;
                }

                finish();
            }
        } else if (requestCode == AUTOMATIC_TIME) {
            if (!isTimeAutomatic(this) || !isTimeZoneAutomatic(this)) {
               makeLToast("Debe configurar su celular con la hora automática proporcionada por la red");
              finish();
            }
        }
    }

    private ArrayList<DetalleFacturaDTO> getDetalleAGuardar() throws Exception {
        try {
            ArrayList<DetalleFacturaDTO> l = new ArrayList<>();
            for (DetalleFacturaDTO d : App.DetalleFacturacion) {
                if (d.Cantidad != 0 || d.Devolucion != 0 || d.Rotacion != 0) {
                    l.add(d);
                }
            }
            return l;
        } catch (Exception e) {
            throw new Exception("Error cargando el detalle a guardar: " + e.getMessage());
        }
    }

    private ArrayList<DetalleFacturaDTO> getDetalleConDevolucion() throws Exception {
        try {
            ArrayList<DetalleFacturaDTO> l = new ArrayList<>();
            for (DetalleFacturaDTO d : App.DetalleFacturacion) {
                if (d.Devolucion != 0) {
                    l.add(d);
                }
            }
            return l;
        } catch (Exception e) {
            throw new Exception("Error cargando el detalle con devolución: " + e.getMessage());
        }
    }

    private NotaCreditoFacturaDTO crearNotaCreditoFactura() throws Exception {

        ArrayList<DetalleFacturaDTO> detalleFacturaDevolucion = getDetalleConDevolucion();

        if (detalleFacturaDevolucion != null && detalleFacturaDevolucion.size() > 0) {

            resolucion.SiguienteNotaCredito =
                    resolucion.SiguienteNotaCredito == 0 ? 1 :
                    resolucion.SiguienteNotaCredito;

            String numeroDocumento = resolucion.CodigoRuta + "-" + resolucion.SiguienteNotaCredito;
            double valorTotalSubtotal = 0;
            double valorTotalDescuento = 0;
            double valorTotalIpoconsumo = 0;
            double valorTotalIva5 = 0;
            double valorTotalIva19 = 0;
            double valorTotalIva = 0;
            double valorTotalDevolucion = 0;

            ArrayList<DetalleNotaCreditoFacturaDTO> detalleNotaCreditoFactura = new ArrayList<>();
            for (DetalleFacturaDTO detalle : detalleFacturaDevolucion) {

                DetalleNotaCreditoFacturaDTO detalleNotaCreditoFacturaDTO = new DetalleNotaCreditoFacturaDTO();
                detalleNotaCreditoFacturaDTO.Cantidad = detalle.Devolucion;
                detalleNotaCreditoFacturaDTO.IdProducto = detalle.IdProducto;
                detalleNotaCreditoFacturaDTO.NumeroDocumento = numeroDocumento;
                detalleNotaCreditoFacturaDTO.Subtotal = Utilities.dosDecimalesDouble(detalle.SubtotalDevolucion, 2);
                detalleNotaCreditoFacturaDTO.Descuento = Utilities.dosDecimalesDouble(detalle.DescuentoDevolucion, 2);
                detalleNotaCreditoFacturaDTO.Ipoconsumo = detalle.IpoconsumoDevolucion;
                detalleNotaCreditoFacturaDTO.Iva5 = Utilities.dosDecimalesDouble(detalle.Iva5Devolucion, 2);
                detalleNotaCreditoFacturaDTO.Iva19 = Utilities.dosDecimalesDouble(detalle.Iva19Devolucion, 2);
                detalleNotaCreditoFacturaDTO.Iva = Utilities.dosDecimalesDouble(detalle.IvaDevolucion, 2);
                detalleNotaCreditoFacturaDTO.Valor = Utilities.dosDecimalesDouble(detalle.ValorDevolucion, 2);
                detalleNotaCreditoFacturaDTO.Codigo = detalle.Codigo;
                detalleNotaCreditoFacturaDTO.Nombre = detalle.Nombre;
                detalleNotaCreditoFactura.add(detalleNotaCreditoFacturaDTO);

                valorTotalSubtotal += detalle.SubtotalDevolucion;
                valorTotalDescuento += detalle.DescuentoDevolucion;
                valorTotalIpoconsumo += detalle.IpoconsumoDevolucion;
                valorTotalIva5 += detalle.Iva5Devolucion;
                valorTotalIva19 += detalle.Iva19Devolucion;
                valorTotalIva += detalle.IvaDevolucion;
                valorTotalDevolucion += detalle.ValorDevolucion;
            }

            NotaCreditoFacturaDTO notaCreditoFacturaDTO = new NotaCreditoFacturaDTO();
            notaCreditoFacturaDTO.NumeroDocumento = numeroDocumento;
            notaCreditoFacturaDTO.NumeroFactura = factura.NumeroFactura;
            notaCreditoFacturaDTO.Fecha = factura.FechaHora;
            notaCreditoFacturaDTO.Subtotal = Utilities.dosDecimalesDouble(valorTotalSubtotal, 2);
            notaCreditoFacturaDTO.Descuento = Utilities.dosDecimalesDouble(valorTotalDescuento, 2);
            notaCreditoFacturaDTO.Ipoconsumo = valorTotalIpoconsumo;
            notaCreditoFacturaDTO.Iva5 = Utilities.dosDecimalesDouble(valorTotalIva5, 2);
            notaCreditoFacturaDTO.Iva19 = Utilities.dosDecimalesDouble(valorTotalIva19, 2);
            notaCreditoFacturaDTO.Iva = Utilities.dosDecimalesDouble(valorTotalIva, 2);
            notaCreditoFacturaDTO.Valor = Utilities.dosDecimalesDouble(valorTotalDevolucion, 2);
            notaCreditoFacturaDTO.CodigoBodega = factura.CodigoBodega;
            notaCreditoFacturaDTO.DetalleNotaCreditoFactura = detalleNotaCreditoFactura;
            factura.Cufe = resolucion.EFactura ? EFacturaUtilities.getCufe(factura, cliente, resolucion) : "";
            notaCreditoFacturaDTO.Cufe = factura.Cufe;
            notaCreditoFacturaDTO.QRInputValue = resolucion.EFactura ? EFacturaUtilities.getQR(factura, cliente, resolucion) : "";

            return notaCreditoFacturaDTO;

        } else {
            return null;
        }
    }

    private void guardar() {
        try {
            // 1ro: Guardo
            if (!factura.Guardada) {

                FormaPagoDTO fp;
                fp = (FormaPagoDTO) cboFormasDePago.getSelectedItem();

                factura.FormaPago = fp != null ? fp.Codigo : "";
                factura.DetalleFactura = getDetalleAGuardar();

                factura.Comentario = App.ComentarioFactura;
                if (App.EfectivoPagado == 0) {
                    App.EfectivoPagado = factura.Total;
                }
                factura.EfectivoPagado = (float) App.EfectivoPagado;
                factura.Latitud = App.obtenerConfiguracion_latitudActual(this);
                factura.Longitud = App.obtenerConfiguracion_longitudActual(this);

                double distance = 0.0;

                try {
                    Location location = new Location("vendedor");
                    location.setLatitude(Double.parseDouble(factura.Latitud));  //latitud
                    location.setLongitude(Double.parseDouble(factura.Longitud));
                    //longitud
                    Location location2 = new Location("Codigo barras");
                    location2.setLatitude(Double.parseDouble(cliente.LatitudCodBarras));  //latitud
                    location2.setLongitude(Double.parseDouble(cliente.LongitudCodBarras)); //longitud

                    distance = (location.distanceTo(location2))/1e6;
                }catch (Exception e){
                    Log.d("Error:", "No se puede calcular la direccion");
                }

                distance = distance/1000;

                String distancia = Double.toString(distance);

                if(distancia.contains("E")){
                    String[]  spltDistance;
                    spltDistance = distancia.split("");

                    String[] spltEuler = null;

                    if(distancia.contains("E-")){
                      spltEuler = distancia.split("E-");
                    }else{
                       spltEuler = distancia.split("E");
                    }

                    String tmpDistance = "";
                    for(int i=0 ; i<=Integer.parseInt(spltEuler[1]); i++){
                        tmpDistance = tmpDistance + "0";
                    }

                    tmpDistance =  tmpDistance +"."+spltDistance[1];
                    distancia = tmpDistance;
                }

                String[] distanceDecimales = distancia.split("");

                String distanceFinal="";
                if(distanceDecimales.length > 3){
                    for(int i=1; i<4; i++){
                         if(i==2){
                            distanceFinal = distanceFinal + ".";
                         }

                         distanceFinal = distanceFinal + distanceDecimales[i];
                    }
                }

                factura.DistanciaCodBarras = distanceFinal;

                if (cboTiposDocumento.getSelectedItem() != null) {
                    factura.TipoDocumento = cboTiposDocumento.getSelectedItem().toString();
                }

                if (cliente != null &&
                        cliente.Credito &&
                        (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                                || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                                || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                        )) {

                    if (etNroPedido.getText() == null
                            || etNroPedido.getText().toString().trim().equals("")) {
                        etNroPedido.setError("Ingrese el número de pedido");
                        return;
                    } else {
                        factura.NumeroPedido = etNroPedido.getText().toString();
                    }

                } else {
                    factura.NumeroPedido = "";
                }

                if (cboEntregadores != null
                        && cboEntregadores.getVisibility() == View.VISIBLE) {
                    Object item = cboEntregadores.getSelectedItem();
                    if (item != null && ((EntregadorDTO) item).IdEmpleado > 0) {
                        factura.IdEmpleadoEntregador = ((EntregadorDTO) item).IdEmpleado;
                    }
                }

                int id = getIntent().getIntExtra("_id", 0);
                cliente = new ClienteDAL(this).ObtenerClientePorId(id);

                if (factura.FacturaPos) {
                    factura.IdCliente = -1;
                    if (cliente.FacturacionPOSCliente)
                        factura.IdCliente = cliente.IdCliente;
                    factura.Identificacion = cliente.Identificacion;
                    factura.RazonSocial = cliente.RazonSocial;
                    factura.Negocio = cliente.NombreComercial;
                    factura.Direccion = cliente.Direccion;
                    factura.Telefono = cliente.Telefono1;
                }

                if (isPedidoCallcenter
                        && (App.pedido_actual == null
                        || App.pedido_actual.IdCliente != factura.IdCliente)) {
                    isPedidoCallcenter = false;
                    factura.IsPedidoCallcenter = false;
                    factura.IdCaso = 0;
                    factura.IdPedido = 0;
                }

                if (!isPedidoCallcenter) {
                    factura.IsPedidoCallcenter = false;
                    factura.IdCaso = 0;
                    factura.IdPedido = 0;
                }

                DateTime fecha = DateTime.now();
                //fecha = fecha.year().withMinimumValue();
                //fecha = fecha.monthOfYear().withMinimumValue();
                //fecha = fecha.dayOfMonth().withMinimumValue();

                String fecha_ultima_factura = App.obtenerFechaUltimaFactura(this);
                if (fecha_ultima_factura != null
                        && !fecha_ultima_factura.equals("")
                        && !fecha_ultima_factura.equals("NULL")) {

                    String[] fecha_hora = fecha_ultima_factura.split(":");

                    if (fecha_hora.length == 5) {

                        int intYearUltima = Integer.parseInt(fecha_hora[0]);
                        int intMonthUltima = Integer.parseInt(fecha_hora[1]);
                        int intDayUltima = Integer.parseInt(fecha_hora[2]);
                        int intHourUltima = Integer.parseInt(fecha_hora[3]);
                        int intMinuteUltima = Integer.parseInt(fecha_hora[4]);

                        DateTime start =
                                new DateTime(intYearUltima, intMonthUltima,
                                        intDayUltima, intHourUltima, intMinuteUltima);

                        try {
                            boolean sw = start.isBefore(fecha);
                            if (!sw) {
                                fecha = start;
                            }
                        } catch (Exception e) {
                            logCaughtException(e);
                            fecha = start;
                        }
                    }
                }

                int year = fecha.getYear();
                int month = fecha.getMonthOfYear();
                int day = fecha.getDayOfMonth();
                int hora = fecha.getHourOfDay();
                int minuto = fecha.getMinuteOfHour();

                String fecha_movil = (year) + ":" + (month) + ":" + (day) + ":" + (hora) + ":" + (minuto);

                App.guardarFechaUltimaFactura(fecha_movil, this);
                factura.FechaHora = fecha.getMillis();
                setLblFecha(fecha);

                if (!factura.FormaPago.equals("") && !factura.FormaPago.equals("000") && cliente.Plazo > 0) {
                    DateTime fechaVencimiento = fecha.plusDays(cliente.Plazo);
                    factura.FechaHoraVencimiento = fechaVencimiento.getMillis();
                }

                boolean errorEnNotaCredito = false;
                if (App.obtenerConfiguracion_CrearNotaCreditoPorDevolucion(this)) {
                    try {
                        notaCreditoFactura = crearNotaCreditoFactura();
                        if (notaCreditoFactura != null) {
                            NotaCreditoFacturaDAL notaCreditoFacturaDAL = new NotaCreditoFacturaDAL(this);
                            notaCreditoFacturaDAL.insertar(notaCreditoFactura);
                        }
                    } catch (Exception e) {
                        logCaughtException(e);
                        errorEnNotaCredito = true;
                        makeErrorDialog("Error guardando la nota crédito, por favor intente nuevamente: "
                                + e.toString(), ActivityFacturacion.this);
                    }
                }

                if (!errorEnNotaCredito) { //Sino se ha guardado correctamente la nota crédito, no guardar la factura
                    try {
                        factura.Cufe = resolucion.EFactura ? EFacturaUtilities.getCufe(factura, cliente, resolucion) : "";
                        factura.QRInputValue = resolucion.EFactura ? EFacturaUtilities.getQR(factura, cliente, resolucion) : "";
                        facturaDAL.insertar(factura, remision);
                        factura.Guardada = true;
                    } catch (Exception e) {
                        logCaughtException(e);
                        makeErrorDialog("Error guardando la factura: " + e.getMessage(), ActivityFacturacion.this);
                        factura.Guardada = false;
                    }

                    if (isPedidoCallcenter && (factura.IdPedido > 0 || factura.IdCaso > 0)) {
                        new PedidoCallcenterDAL(this).eliminarPedido(factura.IdPedido, factura.IdCaso);
                        App.pedido_actual = null;
                    }

                    if (cliente != null) {
                        new ClienteDAL(this).AtenderCliente(cliente);
                    }
                }
            }

        } catch (Exception ex) {
            logCaughtException(ex);
            makeErrorDialog(ex.toString(), ActivityFacturacion.this);
        }
    }

    private void sincronizar() {
        try {
            // 2do: Descargo en otro hilo
            if (!factura.Sincronizada) {
                final Context c = ActivityFacturacion.this;
                new Thread(new Runnable() {
                    public void run() {
                        try {

                            factura.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_FACTURACION;
                            String respuesta = facturaDAL.sincronizarFactura(factura);

                            if (respuesta.equals("Sincronizando")) {
                                respuesta = "La factura ya se estaba sincronizando, por favor intenta nuevamente";
                            }

                            if (respuesta.equals("OK")) {

                                if (notaCreditoFactura != null && !notaCreditoFactura.Sincronizada) {
                                    notaCreditoFactura.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_FACTURACION;
                                    String respuestaNota = new NotaCreditoFacturaDAL(context)
                                            .sincronizarNotaCreditoFactura(notaCreditoFactura);
                                    if (respuestaNota.equals("OK")) {
                                        notaCreditoFactura.Sincronizada = true;
                                    }
                                }

                                factura.Sincronizada = true;
                                App.pedido_actual = null;

                                makeNotification("TiMovil", factura.NumeroFactura + " descargada.", false);

                                if (App.obtenerConfiguracionSincronizarInventario(c)) {
                                    try {
                                        ArrayList<ProductoDTO> l;
                                        if ((resolucion.ManejarInventario
                                                || resolucion.ManejarInventarioRemisiones)
                                                && resolucion.Bodegas.split("-").length > 1) {
                                            String[] bodegas = resolucion.Bodegas.split("-");
                                            l = new ArrayList<>();
                                            for (String bodega : bodegas) {
                                                String[] detalleBodega = bodega.split(":");
                                                NetWorkHelper netWorkHelper = new NetWorkHelper();
                                                String jsonRespuesta = netWorkHelper.readService
                                                        (SincroHelper.getInventarioBodega(resolucion.IdCliente, detalleBodega[0]));
                                                ArrayList<ProductoDTO> tmp = SincroHelper.procesarJsonInventario
                                                        (jsonRespuesta, detalleBodega[0]);

                                                if (tmp != null && !tmp.isEmpty()) {
                                                    l.addAll(tmp);
                                                }
                                            }
                                        } else {
                                            l = new ProxyProducto().CargarProductos(resolucion);
                                        }

                                        if (l != null && !l.isEmpty()) {
                                            ProductoDAL dal = new ProductoDAL(c);
                                            int cantidadCargada = 0;
                                            for (ProductoDTO p : l) {
                                                dal.actualizarInventario(p.IdProducto, p.StockInicial, p.CodigoBodega);
                                                cantidadCargada++;
                                            }
                                            if (cantidadCargada > 0) {
                                                makeNotification("TiMovil", "Se ha actualizado el inventario", false);
                                            }
                                        }
                                    } catch (Exception e) {
                                        logCaughtException(e);
                                        Log.d("ActivityFacturacion", e.toString());
                                    }
                                }

                            } else {
                                if (respuesta.equals(Utilities.IMEI_ERROR)) {
                                    try {
                                        facturaDAL.eliminar();
                                        new ResolucionDAL(App.actualActivity).DecrementarSiguienteFactura();
                                        App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                                        makeNotification("TiMovil - error", "configure nuevamente la ruta", true);
                                    } catch (Exception ex) {
                                        logCaughtException(ex);
                                        makeNotification("TiMovil - error", respuesta, true);
                                    }
                                    finish();
                                } else {
                                    makeNotification("TiMovil - error", respuesta, true);
                                }
                            }

                        } catch (Exception e) {
                            logCaughtException(e);
                            App.SincronizandoFacturaNumero.remove(factura.NumeroFactura);
                            App.SincronizandoFactura = App.SincronizandoFacturaNumero.size() > 0;

                            String respuesta = e.getMessage();
                            if (respuesta != null && respuesta.equals(Utilities.IMEI_ERROR)) {
                                try {
                                    facturaDAL.eliminar();
                                    new ResolucionDAL(App.actualActivity).DecrementarSiguienteFactura();
                                    App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                                    makeNotification("TiMovil - error", "configure nuevamente la ruta", true);
                                } catch (Exception ex) {
                                    logCaughtException(e);
                                    makeNotification("TiMovil - error", e.getMessage(), true);
                                }
                                finish();
                            } else {
                                makeNotification("TiMovil - error", e.getMessage(), true);
                            }
                        }

                    }
                }).start();
            }
        } catch (Exception ex) {
            logCaughtException(ex);
            App.SincronizandoFacturaNumero.remove(factura.NumeroFactura);
            App.SincronizandoFactura = App.SincronizandoFacturaNumero.size() > 0;
            makeErrorDialog(ex.toString(), ActivityFacturacion.this);
        }
    }

    private void imprimir() {

        try {

            progressBar = new CustomProgressBar();
            String progressMessage = "Generando factura " + factura.NumeroFactura;
            progressBar.show(context, progressMessage);

            if (!checkGPS() && resolucion.ReportarUbicacionGPS) {
                return;
            }

            guardar();
            if (factura.Guardada) {
                reloadResolucion();
                sincronizar();

                if (BackUpJsonFacturasYNotasCreditoThread.getState() == Thread.State.NEW) {
                    BackUpJsonFacturasYNotasCreditoThread.start();
                }

                // 3ro: Imprimo
                int numeroCopias;
                if (factura.Impresa) {
                    numeroCopias = 1;
                } else {
                    numeroCopias = App.obtenerPreferencias_NroCopias(this);
                }

                Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, numeroCopias);

                if ((resolucion.IdCliente.equals(Utilities.ID_FR) && remision)
                        || (resolucion.IdCliente.equals(Utilities.ID_JAF) && resolucion.CodigoRuta.equals("50"))) {
                    printer.printFacturaComoRemision(factura);
                } else if (App.obtenerConfiguracion_imprimir(this)) {
                    factura.notaCreditoFactura = notaCreditoFactura;
                    printer.print(factura);
                }
            }

        } catch (SQLiteConstraintException e) {
            logCaughtException(e);
            if (!factura.Guardada) {
                factura.DetalleFactura = App.DetalleFacturacion;
            }
            makeErrorDialog("El número de factura ya existe, por favor cargue nuevamente los datos",
                    ActivityFacturacion.this);

        } catch (Exception e) {
            logCaughtException(e);
            if (!factura.Guardada) {
                factura.DetalleFactura = App.DetalleFacturacion;
            }
            makeErrorDialog(e.getMessage(), ActivityFacturacion.this);
        }

        progressBar.getDialog().dismiss();
    }

    public void onBackPressed() {
        if (!factura.Guardada) {
            Builder d = new Builder(this);
            d.setTitle("TiMovil");
            d.setMessage("Está seguro que desea salir sin guardar la factura?");
            d.setCancelable(true);
            d.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}
            });
            d.show();
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    private boolean checkGPS() {
        boolean sw = true;
        if (!isGpsActive() &&
                resolucion.ReportarUbicacionGPS) {
            Builder builder = new Builder(this);
            builder.setTitle("Servicios de ubicación inactivos");
            builder.setMessage("Por favor habilite los servicios de ubicación y GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface,
                                    int i) {
                    Intent intent =
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, CONFIGURAR_GPS);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
            sw = false;
        }
        return sw;
    }

    private void checkTimeAutomatic() {

        if (!isTimeAutomatic(this) || !isTimeZoneAutomatic(this)) {
            Builder builder = new Builder(this);
            builder.setTitle("Zona horaria manual");
            builder.setMessage("Debe configurar su celular con la fecha y hora automática proporcionada por la red");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface,
                                    int i) {
                    startActivityForResult(
                            new Intent(android.provider.Settings.ACTION_DATE_SETTINGS),
                            AUTOMATIC_TIME);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    class ObtenerUltimaFacturaTask extends AsyncTask<Boolean, String, String> {

        MUltFac ultimaFactura;

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Obteniendo la última factura del cliente");
        }

        @Override
        protected String doInBackground(Boolean... params) {

            String error = null;
            try {

                String idClienteTM = resolucion.IdCliente;
                String idCliente = String.valueOf(cliente.IdCliente);
                publishProgress("Obteniendo última factura del cliente", "Conectando con el servidor...");
                NetWorkHelper netWorkHelper = new NetWorkHelper();
                String jsonRespuesta = netWorkHelper.readService(SincroHelper.getUltimaFacturaURL(idClienteTM, idCliente));
                 ultimaFactura = SincroHelper.procesarJsonFactura(jsonRespuesta);
                publishProgress("Factura descargada", "...");
            } catch (Exception e) {
                logCaughtException(e);
                error = e.getMessage();
            }
            return error;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (result == null && ultimaFactura != null) {
                App.ultimaFactura = ultimaFactura;
                pasarAultimaFactura();
            } else if (result != null && !result.equals("")) {
                makeErrorDialog(result, ActivityFacturacion.this);
            }
        }
    }

    private void pasarAultimaFactura() {

        if (factura.Guardada) {
            makeDialog("La factura ya está guardada y por lo tanto no puede modificarse.", ActivityFacturacion.this);
            return;
        }

        Intent i = new Intent(ActivityFacturacion.this, ActivityUltimaFactura.class);
        i.putExtra("exentoIva", cliente.ExentoIva);
        startActivityForResult(i, ULTIMA_FACTURA);

    }

    private void verificar_resolucion(ResolucionDTO resolucionDTO, boolean pos) throws Exception {

        if (resolucionDTO != null) {
            int siguienteFactura;
            String facturaFinal;
            int _facturaFinal;
            if (pos) {
                siguienteFactura = resolucionDTO.SiguienteFacturaPOS;
                facturaFinal = resolucionDTO.FacturaFinalPOS;
            } else {
                siguienteFactura = resolucionDTO.SiguienteFactura;
                facturaFinal = resolucionDTO.FacturaFinal;
            }

            try {
                _facturaFinal = Integer.parseInt(facturaFinal);
            } catch (Exception ex) {
                throw new Exception("Por favor cargue los datos nuevamente");
            }

            if (siguienteFactura > _facturaFinal) {
                throw new Exception("Su resolución de facturación se encuentra vencida");
            }

        } else {
            throw new Exception("Por favor cargue los datos nuevamente");
        }
    }

    private void verificar_fecha_facturacion_con_ultima_factura() throws Exception {
        String fecha_ultima_factura = App.obtenerFechaUltimaFactura(this);

        if (fecha_ultima_factura != null
                && !fecha_ultima_factura.equals("")
                && !fecha_ultima_factura.equals("NULL")) {

            String[] fecha_hora = fecha_ultima_factura.split(":");

            if (fecha_hora.length == 5) {

                int intYearUltima = Integer.parseInt(fecha_hora[0]);
                int intMonthUltima = Integer.parseInt(fecha_hora[1]);
                int intDayUltima = Integer.parseInt(fecha_hora[2]);
                int intHourUltima = Integer.parseInt(fecha_hora[3]);
                int intMinuteUltima = Integer.parseInt(fecha_hora[4]);

                DateTime start =
                        new DateTime(intYearUltima,
                                intMonthUltima,
                                intDayUltima,
                                intHourUltima,
                                intMinuteUltima);

                DateTime end = new DateTime();
                int year = end.year().get();
                int month = end.monthOfYear().get();
                int day = end.dayOfMonth().get();
                int hour = end.hourOfDay().get();

                String fecha_carga_datos = App.obtenerFechaDescargaDatos(this);
                if (fecha_carga_datos != null
                        && !fecha_carga_datos.equals("")
                        && !fecha_carga_datos.equals("NULL")) {

                    String[] fecha_hora_carga = fecha_carga_datos.split(":");
                    if (fecha_hora_carga.length == 5) {

                        int intYearCarga = Integer.parseInt(fecha_hora_carga[0]);
                        int intMonthCarga = Integer.parseInt(fecha_hora_carga[1]);
                        int intDayCarga = Integer.parseInt(fecha_hora_carga[2]);
                        int intHourCarga = Integer.parseInt(fecha_hora_carga[3]);

                        String toCompare1 = intYearCarga + "/" + intMonthCarga + "/" + intDayCarga + "/" + intHourCarga;
                        String toCompare2 = year + "/" + month + "/" + day + "/" + hour;

                        if (!toCompare1.equals(toCompare2) && hour == 0) {
                            throw new Exception("Debe realizar la Carga de Datos para poder facturar");
                        }

                    }
                }

                //Se supone end es la fecha actual del cel configurada correctamente
                boolean sw = start.isBefore(end);

                if (!sw) {
                    throw new Exception("Debe corregir la fecha de su dispositivo móvil");
                }

            }
        } else {
            if (Utilities.isNetworkConnected(this)
                    && Utilities.isNetworkReachable(this)) {
                new TaskVerificarFechaHoraWS(this).execute();
            }
        }
    }

    private void mostrarErrorYSalir(String mensaje, Context c) {
        Builder d = new Builder(c);
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

    Thread BackUpJsonFacturasYNotasCreditoThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                new FacturaBackUp(context).makeBackUp();
                new NotaCreditoFacturaPorDevolucionBackUp(context).makeBackUp();
                new NoVentaBackUp(context).makeBackUp();
                new NoVentaPedidoBackUp(context).makeBackUp();

            } catch (Exception e) {
                logCaughtException(e);
            }
        }
    });

}