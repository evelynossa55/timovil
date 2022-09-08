package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.backup.RemisionBackUp;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.TaskVerificarFechaHoraWS;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.FormaPagoDAL;
import com.cm.timovil2.data.PedidoCallcenterDAL;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.FormaPagoDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.wsentities.MDetUltRem;
import com.cm.timovil2.dto.wsentities.MUltRem;
import com.cm.timovil2.proxy.ProxyProducto;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;

public class ActivityCrearRemision extends ActivityBase implements View.OnClickListener {

    private LayoutInflater vi;
    private ViewGroup parent;
    private TextView lblDatosCliente;
    private TextView lblFecha;
    private TextView txtFormasPago;
    private RemisionDAL remisionDAL;
    private RemisionDTO remision;
    private ClienteDTO cliente;
    private TextView lblResumen;
    private Spinner cboFormasDePago;
    private EditText txtNumeroOrden;
    private boolean isPedidoCallcenter;

    private final int INGRESAR_DETALLE = 1;
    private final int CONFIGURAR_GPS = 3;
    private final int ULTIMA_REMISION = 4;
    private final int NO_VENTA = 6;
    private final int AUTOMATIC_TIME = 7;

    private LinearLayout linearLayoutDetalle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remision_activity);
        App.actualActivity = this;
        remisionDAL = new RemisionDAL(this);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        try {

            reloadResolucion();
            if (resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                    || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
                verificar_fecha_facturacion_con_ultima_remision();
            }

            setControls();
            iniciarRemision();
            cargarDetalle();
            obtenerUbicacion();

        } catch (Exception e) {

            logCaughtException(e);
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("Error TiMovil");
            d.setMessage(e.getMessage());
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    finish();
                }
            });
            d.show();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYSalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        } else {
            checkGPS();
            checkTimeAutomatic();
        }
    }

    @Override
    protected void setControls() {
        parent = findViewById(R.id.layout_container);
        linearLayoutDetalle = findViewById(R.id.linearlayoutDetalle);
        lblDatosCliente = findViewById(R.id.lblDatosCliente);
        lblFecha = findViewById(R.id.lblFecha);
        lblResumen = findViewById(R.id.lblResumen);
        lblDatosCliente.setOnClickListener(this);
        txtNumeroOrden = findViewById(R.id.etNroPedido);
        cboFormasDePago = findViewById(R.id.cboFormaDePago);
        txtFormasPago = findViewById(R.id.txtFormasPago);

        ImageButton mnuIngresarDetalle = findViewById(R.id.mnuIngresarDetalle);
        ImageButton mnuImprimir = findViewById(R.id.mnuImprimir);
        ImageButton mnuIngresarComentario = findViewById(R.id.mnuIngresarComentario);
        ImageButton mnuVerCartera = findViewById(R.id.mnuVerCartera);
        ImageButton mnuNoVenta = findViewById(R.id.mnuNoVenta);
        ImageButton mnuVerUltimaRemision = findViewById(R.id.mnuVerUltimaRemision);

        mnuIngresarDetalle.setOnClickListener(this);
        mnuImprimir.setOnClickListener(this);
        mnuIngresarComentario.setOnClickListener(this);
        mnuVerCartera.setOnClickListener(this);
        mnuNoVenta.setOnClickListener(this);
        mnuVerUltimaRemision.setOnClickListener(this);

    }

    private void iniciarRemision() throws Exception {
        try {

            if (resolucion.CodigoRuta == null || resolucion.SiguienteRemision <= 0) {
                throw new Exception("Error en el número de remisión. Vuelva a ingresar al programa");
            }

            App.EfectivoPagado = 0;
            App.ComentarioFactura = "";
            String numeroRemision;
            numeroRemision = resolucion.CodigoRuta + "-" + resolucion.SiguienteRemision;
            setTitle("REMISIÓN #" + numeroRemision);

            RemisionDTO remisionExiste = remisionDAL.obtenerPorNumeroFac(numeroRemision);
            if (remisionExiste != null
                    && remisionExiste.NumeroRemision != null
                    && remisionExiste.NumeroRemision.equals(numeroRemision)) {
                mostrarErrorYSalir("Error número de remisión: La remisión " + numeroRemision + " ya existe. Por favor realice la carga de datos para actualizar la numeración.");
                return;
            }

            if (numeroRemision.equals("-0") || numeroRemision.equals("null-0")) {
                throw new Exception("Error número de remisión: -0 (Ingrese de nuevo a la aplicación)");
            }

            remision = new RemisionDTO();
            remision.NumeroRemision = numeroRemision;
            remision.CodigoRuta = resolucion.CodigoRuta;
            remision.NombreRuta = resolucion.NombreRuta;

            Intent intent = getIntent();
            if (intent.getExtras() != null
                    && intent.getExtras().getString("codigoBodega") != null) {

                remision.CodigoBodega = intent.getExtras().getString("codigoBodega");

            }

            cargarDatosCliente();
            cargarFormasDePago();

            isPedidoCallcenter = intent.getBooleanExtra("isPedidoCallcenter", false);
            boolean codigoBarrasLeido = intent.getBooleanExtra("codigoBarrasLeido", false);

            remision.IsPedidoCallcenter = isPedidoCallcenter;
            remision.CreadaConCodigoBarras = codigoBarrasLeido;

            if (isPedidoCallcenter
                    && App.pedido_actual.IdCliente == remision.IdCliente) {
                remision.IdPedido = App.pedido_actual.IdPedido;
                remision.IdCaso = App.pedido_actual.IdCaso;
            } else {
                remision.IdPedido = 0;
                remision.IdCaso = 0;
                App.pedido_actual = null;
                remision.IsPedidoCallcenter = false;
                limpiarDetalle();
            }

            setLblFecha(DateTime.now());

        } catch (Exception e) {
            throw new Exception("Error iniciando la remisión: " + e.getMessage());
        }
    }

    private void cargarFormasDePago() {

        try {

            cboFormasDePago.setAdapter(null);
            ArrayList<FormaPagoDTO> listFormaPago = new FormaPagoDAL(this).ObtenerListado();
            int selectedPosition = 0;
            boolean credito = cliente.Credito;
            if (credito && listFormaPago.size() >= 2) {
                selectedPosition = 1;
            }

            ArrayAdapter<FormaPagoDTO> adaptadorCombo = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, listFormaPago);
            adaptadorCombo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cboFormasDePago.setAdapter(adaptadorCombo);
            cboFormasDePago.setSelection(selectedPosition);

            if (resolucion.IdCliente.equals(Utilities.ID_MATERIALES_Y_HERRAMIENTAS)) {

                //MATERIALES Y HERRAMIENTAS,
                txtFormasPago.setVisibility(View.VISIBLE);
                cboFormasDePago.setVisibility(View.VISIBLE);


            } else {

                txtFormasPago.setVisibility(View.GONE);
                cboFormasDePago.setVisibility(View.GONE);

            }

        } catch (Exception e) {
            logCaughtException(e);
            makeLToast("Error cargando las formas de pago: " + e.getMessage());
        }

    }

    private void mostrarErrorYSalir(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityCrearRemision.this);
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

    private void cargarDatosCliente() {
        try {

            int _id = getIntent().getIntExtra("_id", 0);
            cliente = new ClienteDAL(this).ObtenerClientePorId(_id);
            String sb = ("(") + (cliente.Identificacion) + (") -") + (cliente.RazonSocial) + (" - ")
                    + ((!cliente.NombreComercial.equals("") ? cliente.NombreComercial + (" - ") : ""))
                    + (cliente.Direccion) + (" - ")
                    + (cliente.Telefono1);
            lblDatosCliente.setText(sb);
            remision.IdCliente = cliente.IdCliente;
            remision.IdentificacionCliente = cliente.Identificacion;
            remision.RazonSocialCliente = cliente.RazonSocial;
            remision.DireccionCliente = cliente.Direccion;
            remision.TelefonoCliente = cliente.Telefono1;
            remision.Negocio = cliente.NombreComercial;

        } catch (Exception e) {
            logCaughtException(e);
            makeLToast("Error cargando los datos del cliente: " + e.getMessage());
        }
    }

    private void iniciarValoresFactura() {
        remision.Subtotal = 0;
        remision.Descuento = 0;
        remision.Iva = 0;
        remision.Total = 0;
        remision.Devolucion = 0;
        remision.Rotacion = 0;
        remision.ValorDevolucion = 0;
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

    private void cargarDetalle() {

        try {
            iniciarValoresFactura();
            ArrayList<DetalleFacturaDTO> detalle = new ArrayList<>();

            for (DetalleFacturaDTO d : App.DetalleFacturacion) {
                if (d.Cantidad != 0 || d.Devolucion != 0 || d.Rotacion != 0) {
                    detalle.add(d);
                    if (resolucion.IdCliente.equals(Utilities.ID_FR)) {
                        remision.Subtotal += (d.Subtotal + d.Iva);
                        remision.Iva += 0;
                        remision.Descuento += d.Descuento;
                        remision.Devolucion += d.Devolucion;
                        remision.Rotacion += d.Rotacion;
                        remision.ValorDevolucion += d.ValorDevolucion;
                        remision.Ipoconsumo += d.ValorIpoConsumo;
                    } else {
                        remision.Subtotal += d.Subtotal;
                        remision.Iva += d.Iva;
                        remision.Descuento += d.Descuento;
                        remision.Devolucion += d.Devolucion;
                        remision.Rotacion += d.Rotacion;
                        remision.ValorDevolucion += d.ValorDevolucion;
                        remision.Ipoconsumo += d.ValorIpoConsumo;
                    }
                }
            }

            remision.Total =
                    remision.Subtotal +
                            remision.Iva +
                            remision.Ipoconsumo -
                            remision.Descuento;

            if (resolucion != null && resolucion.DevolucionAfectaRemision) {
                remision.Total = remision.Total - remision.ValorDevolucion;
            }

            ArrayList<DetalleRemisionDTO> detalleRemision = getDetalleRemision(detalle);
            cargarDetalle(detalleRemision);
            lblResumen.setText(remision.getResumenValores());
            remision.DetalleRemision = detalleRemision;

            double valorTotalIvaDevolucion = 0;
            for (DetalleRemisionDTO detalleRemisionDTO : remision.DetalleRemision) {
                valorTotalIvaDevolucion += detalleRemisionDTO.IvaDevolucion;
            }

            //------RETE FUENTE--------------
            float PorcentajeRetefuente;
            if (cliente != null && cliente.ReteFuente && ((remision.Subtotal - remision.Descuento) > resolucion.TopeRetefuente)) {
                PorcentajeRetefuente = resolucion.PorcentajeRetefuente;
            } else {
                PorcentajeRetefuente = 0;
            }

            if (PorcentajeRetefuente > 0) {
                remision.ValorRetefuente = (remision.Subtotal - remision.Descuento) * (PorcentajeRetefuente / 100);
                if (resolucion.DevolucionAfectaRemision) {
                    remision.RetefuenteDevolucion = (remision.ValorDevolucion - valorTotalIvaDevolucion) * (PorcentajeRetefuente / 100);
                } else {
                    remision.RetefuenteDevolucion = 0;
                }
            } else {
                remision.ValorRetefuente = 0;
            }

            //------RETE IVA--------------
            float PorcentajeReteIva;
            if (cliente != null && cliente.ReteIva && ((remision.Subtotal - remision.Descuento) > resolucion.TopeReteIva)) {
                PorcentajeReteIva = resolucion.PorcentajeReteIva;
            } else {
                PorcentajeReteIva = 0;
            }

            if (PorcentajeReteIva > 0) {
                remision.ValorReteIva = remision.Iva * (PorcentajeReteIva / 100);
                if (remision.ValorDevolucion > 0 && valorTotalIvaDevolucion > 0) {
                    remision.ValorReteIvaDevolucion = valorTotalIvaDevolucion * (PorcentajeReteIva / 100);
                } else {
                    remision.ValorReteIvaDevolucion = 0;
                }
            } else {
                remision.ValorReteIva = 0;
            }

            remision.ValorDevolucion = Utilities.dosDecimalesDouble(remision.ValorDevolucion, 2);
            remision.Iva = Utilities.dosDecimalesDouble(remision.Iva, 2);
            remision.Total = Utilities.dosDecimalesDouble(remision.Total, 2);

            mostrarDetalleFactura(detalle);
            lblResumen.setText(remision.getResumenValores());
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog("Error cargando el detalle: " + e.getMessage(), ActivityCrearRemision.this);

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


    private ArrayList<DetalleRemisionDTO> getDetalleRemision(ArrayList<DetalleFacturaDTO> detalle) {

        ArrayList<DetalleRemisionDTO> result = new ArrayList<>();

        if (detalle != null && detalle.size() > 0) {

            for (DetalleFacturaDTO d : detalle) {

                DetalleRemisionDTO aux = new DetalleRemisionDTO();
                aux.PorcentajeIva = d.PorcentajeIva;
                aux.FechaCreacion = new Date().getTime();
                aux.Total = Utilities.dosDecimalesDouble(d.Total, 2);
                aux.NumeroRemision = remision.NumeroRemision;
                aux.IdProducto = d.IdProducto;
                aux.NombreProducto = d.Nombre;
                aux.Cantidad = d.Cantidad;
                aux.Devolucion = d.Devolucion;
                aux.Rotacion = d.Rotacion;
                aux.ValorUnitario = Utilities.dosDecimalesDouble(d.ValorUnitario, 2);
                aux.Subtotal = Utilities.dosDecimalesDouble(d.Subtotal, 2);
                aux.Iva = Utilities.dosDecimalesDouble(d.Iva, 2);
                aux.Codigo = d.Codigo;
                aux.Descuento = Utilities.dosDecimalesDouble(d.Descuento, 2);
                aux.ValorDevolucion = Utilities.dosDecimalesDouble(d.ValorDevolucion, 2);
                aux.Ipoconsumo = d.ValorIpoConsumo;
                aux.IvaDevolucion = Utilities.dosDecimalesDouble(d.IvaDevolucion, 2);

                if (d.DescuentoAdicional > 0) {
                    aux.PorcentajeDescuento = d.DescuentoAdicional;
                } else {
                    aux.PorcentajeDescuento = d.PorcentajeDescuento;
                }

                if (resolucion.IdCliente.equals(Utilities.ID_FR)) {
                    aux.ValorUnitario = d.ValorUnitario + (d.ValorUnitario * d.PorcentajeIva / 100);
                    aux.Subtotal = d.Subtotal + d.Iva;
                    aux.Iva = 0;
                }

                result.add(aux);
            }
        }
        return result;
    }

    private void cargarDetalle(ArrayList<DetalleRemisionDTO> detalles) {
        if (detalles != null) {
            TextView txtDetalle;
            linearLayoutDetalle.removeAllViews();
            if (detalles.isEmpty()) {
                txtDetalle = new TextView(this);
                txtDetalle.setText("------");
                linearLayoutDetalle.addView(txtDetalle);
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#CCCCCC"));
            } else {
                linearLayoutDetalle.setBackgroundColor(Color.parseColor("#FFFFFF"));
                View view;
                for (DetalleRemisionDTO detalle : detalles) {
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

    private void pasarAlDetalle() {
        if (remision.Guardada) {
            makeDialog("La remisión ya está guardada y por lo tanto no puede modificarse.", ActivityCrearRemision.this);
            return;
        }

        Intent i = new Intent(ActivityCrearRemision.this, ActivityDetalleFacturacion.class);
        i.putExtra("isRemision", true);
        i.putExtra("exentoIva", cliente.ExentoIva);
        startActivityForResult(i, INGRESAR_DETALLE);
    }

    private void pasarAlComentario() {
        if (remision.Guardada) {
            makeDialog("La remisión ya está guardada y por lo tanto no puede modificarse.", ActivityCrearRemision.this);
            return;
        }

        Intent i = new Intent(ActivityCrearRemision.this, ActivityComentarioFactura.class);
        startActivity(i);
    }

    public void onClick(View arg) {
        int id = arg.getId();

        switch (id) {
            case R.id.mnuImprimir:
                imprimir();
                break;
            case R.id.mnuIngresarDetalle:
                pasarAlDetalle();
                break;
            case R.id.mnuNoVenta:
                if (cliente == null) break;
                if (remision.Guardada) {
                    makeDialog("La remisión ya está guardada y por lo tanto no puede modificarse.", ActivityCrearRemision.this);
                } else {
                    Intent intent = new Intent(ActivityCrearRemision.this, ActivityNoVenta.class);
                    intent.putExtra("IdCliente", remision.IdCliente);
                    intent.putExtra("IsPedidoCallcenter", isPedidoCallcenter);
                    startActivityForResult(intent, NO_VENTA);
                }
                break;
            case R.id.mnuIngresarComentario:
                pasarAlComentario();
                break;
            case R.id.mnuVerCartera:
                if (cliente == null) break;
                String mensaje = cliente.CarteraPendiente.replace('{', '\n');
                String mensaje2 = cliente.Remisiones.replace('{', '\n');
                String mensaje3 = cliente.ValorVentasMes.replace('{', '\n');
                String mensaje4 = Utilities.FormatoMoneda(resolucion.ValorVentaMensual);
                Intent intent = new Intent(this, ActivityCartera.class);
                intent.putExtra("cartera", mensaje);
                intent.putExtra("remisiones", mensaje2);
                intent.putExtra("ventasMes", mensaje3);
                intent.putExtra("ventasMesRuta", mensaje4);
                startActivity(intent);
                break;

            case R.id.lblDatosCliente:
                pasarAlDetalle();
                break;

            case R.id.mnuVerUltimaRemision:

                if (remision.Guardada) {
                    makeDialog("La factura ya está guardada y por lo tanto no puede modificarse.",
                            ActivityCrearRemision.this);
                } else if (cliente != null) {
                    new ActivityCrearRemision.ObtenerUltimaRemisionTask().execute();
                }

                break;

        }


    }

    /**
     * Creado por juan sebastian Arenas Borja
     * */
    class ObtenerUltimaRemisionTask extends AsyncTask<Boolean, String, String> {

        MUltRem ultimaRemision;
        //MUltFac ultimaRemision;

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(context, "Obteniendo la última remisión del cliente");
        }


        @Override
        protected String doInBackground(Boolean... params) {

            String error = null;
            try {
                //obtener el id del cliente de timovil
                String idClienteTM = resolucion.IdCliente;
                String idCliente = String.valueOf(cliente.IdCliente);
                publishProgress("Obteniendo última remisión del cliente", "Conectando con el servidor...");
                NetWorkHelper netWorkHelper = new NetWorkHelper();
                String jsonRespuesta = netWorkHelper.readService(SincroHelper.getUltimaRemisionURL(idClienteTM, idCliente));
                ultimaRemision = SincroHelper.procesarJsonRemision(jsonRespuesta);

                publishProgress("Remisión descargada", "...");
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

            if (result == null && ultimaRemision != null) {
                App.ultimaRemision = ultimaRemision;
                pasarAultimaRemision();
            } else if (result != null && !result.equals("")) {
                makeErrorDialog(result, ActivityCrearRemision.this);
            }
        }
    }

    private void pasarAultimaRemision() {

        if (remision.Guardada) {
            makeDialog("La Remision ya está guardada y por lo tanto no puede modificarse.", ActivityCrearRemision.this);
            return;
        }

        Intent i = new Intent(ActivityCrearRemision.this, ActivityUltimaRemision.class);
        i.putExtra("exentoIva", cliente.ExentoIva);
        startActivityForResult(i, ULTIMA_REMISION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INGRESAR_DETALLE) {
            cargarDetalle();
        } else if (requestCode == CONFIGURAR_GPS) {
            if (!isGpsActive()) {
                makeLToast("Debe habilitar el GPS para crear remisiones");
                finish();
            }
            obtenerUbicacion();

        } else if (requestCode == ULTIMA_REMISION) {
            if (resultCode == RESULT_OK) {
                makeLToast("Se han agregado productos del pedido anterior del cliente");
            }
            cargarDetalle();
        }
        else if (requestCode == NO_VENTA) {

            if (resultCode == RESULT_OK) {

                if (isPedidoCallcenter && (remision.IdPedido > 0 || remision.IdCaso > 0)) {
                    new PedidoCallcenterDAL(this).eliminarPedido(remision.IdPedido, remision.IdCaso);
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

    private void guardar() {
        try {
            // 1ro: Guardo
            if (!remision.Guardada) {

                if (cboFormasDePago != null) {
                    FormaPagoDTO fp = (FormaPagoDTO) cboFormasDePago.getSelectedItem();
                    remision.FormaPago = (fp != null ? fp.Codigo : null);
                }

                ArrayList<DetalleFacturaDTO> detalle = getDetalleAGuardar();
                remision.DetalleRemision = getDetalleRemision(detalle);
                remision.Comentario = App.ComentarioFactura;

                remision.Latitud = App.obtenerConfiguracion_latitudActual(this);
                remision.Longitud = App.obtenerConfiguracion_longitudActual(this);

                if (cliente.Credito
                        && (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                        || resolucion.IdCliente.equals(Utilities.ID_POLAR)
                        || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE))) {

                    if (txtNumeroOrden.getText() == null
                            || txtNumeroOrden.getText().toString().trim().equals("")) {
                        txtNumeroOrden.setError("Ingrese el número de pedido");
                        return;
                    } else {
                        remision.NumeroPedido = txtNumeroOrden.getText().toString();
                    }

                } else {
                    remision.NumeroPedido = ((txtNumeroOrden.getText() != null) ?
                            txtNumeroOrden.getText().toString() : "");
                }

                if (isPedidoCallcenter
                        && (App.pedido_actual == null
                        || App.pedido_actual.IdCliente != remision.IdCliente)
                ) {
                    isPedidoCallcenter = false;
                    remision.IsPedidoCallcenter = false;
                    remision.IdCaso = 0;
                    remision.IdPedido = 0;
                }
                if (!isPedidoCallcenter) {
                    remision.IsPedidoCallcenter = false;
                    remision.IdCaso = 0;
                    remision.IdPedido = 0;
                }

                DateTime fecha = DateTime.now();
                //fecha = fecha.year().withMinimumValue();
                //fecha = fecha.monthOfYear().withMinimumValue();
                //fecha = fecha.dayOfMonth().withMinimumValue();

                String fecha_ultima_remision = App.obtenerFechaUltimaRemision(this);
                if (fecha_ultima_remision != null
                        && !fecha_ultima_remision.equals("")
                        && !fecha_ultima_remision.equals("NULL")) {

                    String[] fecha_hora = fecha_ultima_remision.split(":");

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

                App.guardarFechaUltimaRemision(fecha_movil, this);
                remision.Fecha = fecha.getMillis();
                setLblFecha(fecha);

                try {
                    remisionDAL.insertar(remision);
                    remision.Guardada = true;
                } catch (Exception ex) {
                    makeErrorDialog(ex.toString(), ActivityCrearRemision.this);
                    remision.Guardada = false;
                }

                if (isPedidoCallcenter && (remision.IdPedido > 0 || remision.IdCaso > 0)) {
                    new PedidoCallcenterDAL(this).eliminarPedido(remision.IdPedido, remision.IdCaso);
                    App.pedido_actual = null;
                }

                //Actualizamos el estado del cliente [Atendido = 1]
                if (cliente != null) {
                    new ClienteDAL(this).AtenderCliente(cliente);
                }
            }

        } catch (Exception ex) {
            logCaughtException(ex);
            makeErrorDialog(ex.toString(), ActivityCrearRemision.this);
        }
    }

    private void sincronizar() {
        try {
            // 2do: Descargo en otro hilo
            if (!remision.Sincronizada) {
                new Thread(new Runnable() {
                    public void run() {
                        try {

                            remision.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_FACTURACION;
                            String respuesta = remisionDAL.sincronizarRemision(remision);

                            if (respuesta.equals("Sincronizando")) {
                                respuesta = "La remisión ya se estaba sincronizando, por favor intenta nuevamente";
                            }

                            if (respuesta.equals("OK")) {

                                remision.Sincronizada = true;
                                App.pedido_actual = null;

                                makeNotification("TiMovil", remision.NumeroRemision + " descargada.", false);

                                if (App.obtenerConfiguracionSincronizarInventario(context)) {
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
                                            ProductoDAL dal = new ProductoDAL(context);
                                            for (ProductoDTO p : l) {
                                                dal.actualizarInventario(p.IdProducto, p.StockInicial, p.CodigoBodega);
                                            }
                                        }

                                    } catch (Exception e) {
                                        logCaughtException(e);
                                        Log.d("ActivityCrearRemision", e.toString());
                                    }
                                }

                            } else {

                                if (respuesta.equals(Utilities.IMEI_ERROR)) {

                                    try {
                                        remisionDAL.eliminar();
                                        new ResolucionDAL(App.actualActivity).DecrementarSiguienteRemision();
                                        App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                                        makeNotification("TiMovil - error", "configure nuevamente la ruta", true);
                                    } catch (Exception ex) {
                                        makeNotification("TiMovil - error", respuesta, true);
                                    }

                                    finish();

                                } else {
                                    makeNotification("TiMovil - error", respuesta, true);
                                }
                            }


                        } catch (Exception e) {

                            logCaughtException(e);

                            App.SincronizandoRemisionNumero.remove(remision.NumeroRemision);
                            App.SincronizandoRemision = App.SincronizandoRemisionNumero.size() > 0;

                            String respuesta = e.getMessage();
                            if (respuesta.equals(Utilities.IMEI_ERROR)) {
                                try {
                                    remisionDAL.eliminar();
                                    new ResolucionDAL(App.actualActivity).DecrementarSiguienteRemision();
                                    App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                                    makeNotification("TiMovil - error", "configure nuevamente la ruta", true);
                                } catch (Exception ex) {
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
            App.SincronizandoRemisionNumero.remove(remision.NumeroRemision);
            App.SincronizandoRemision = App.SincronizandoRemisionNumero.size() > 0;
            makeErrorDialog(ex.toString(), ActivityCrearRemision.this);
        }
    }

    private void imprimir() {

        try {

            progressBar = new CustomProgressBar();
            String progressMessage = "Generando remisión " + remision.NumeroRemision;
            progressBar.show(context, progressMessage);

            if (!checkGPS() && resolucion.ReportarUbicacionGPS) {
                return;
            }

            guardar();

            if (remision.Guardada) {
                reloadResolucion();
                sincronizar();

                if (BackUpJsonRemisionesThread.getState() == Thread.State.NEW) {
                    BackUpJsonRemisionesThread.start();
                }

                // 3do: Imprimo
                if (App.obtenerConfiguracion_imprimir(this)) {

                    int numeroCopias;
                    if (remision.Impresa) {
                        numeroCopias = 1;
                    } else {
                        numeroCopias = App.obtenerPreferencias_NroCopias(this);
                    }

                    Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, numeroCopias);
                    printer.print(remision);

                }
            } else {
                makeDialog("La remisión no se pudo guardar", ActivityCrearRemision.this);
            }

        } catch (SQLiteConstraintException e) {
            logCaughtException(e);
            if (!remision.Guardada) {
                try {
                    remision.DetalleRemision = getDetalleRemision(App.DetalleFacturacion);
                } catch (Exception ex) {
                    logCaughtException(e);
                    makeErrorDialog(e.getMessage(), ActivityCrearRemision.this);
                }
            }

            makeErrorDialog("El número de remisión ya existe, por favor cargue nuevamente los datos", ActivityCrearRemision.this);

        } catch (Exception e) {
            logCaughtException(e);
            if (!remision.Guardada) {
                try {
                    remision.DetalleRemision = getDetalleRemision(App.DetalleFacturacion);
                } catch (Exception ex) {
                    logCaughtException(e);
                    makeErrorDialog(e.getMessage(), ActivityCrearRemision.this);
                }
            }
            makeErrorDialog(e.getMessage(), ActivityCrearRemision.this);
        }

        progressBar.getDialog().dismiss();

    }

    public void onBackPressed() {
        if (!remision.Guardada) {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            d.setTitle("TiMovil");
            d.setMessage("Está seguro que desea salir sin guardar la remisión?");
            d.setCancelable(true);
            d.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                }
            });
            d.show();
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    private boolean checkGPS() {
        if (!isGpsActive() && resolucion.ReportarUbicacionGPS) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Servicios de ubicación inactivos");
            builder.setMessage("Por favor habilite los servicios de ubicación y GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, CONFIGURAR_GPS);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
            return false;
        } else {
            obtenerUbicacion();
            return true;
        }
    }

    private void checkTimeAutomatic() {
        if (!isTimeAutomatic(this) || !isTimeZoneAutomatic(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Zona horaria manual");
            builder.setMessage("Debe configurar su celular con la fecha y hora automática proporcionada por la red");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface,
                                    int i) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS),
                            AUTOMATIC_TIME);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    private void verificar_fecha_facturacion_con_ultima_remision() throws Exception {
        String fecha_ultima_remision = App.obtenerFechaUltimaRemision(this);

        if (fecha_ultima_remision != null
                && !fecha_ultima_remision.equals("")
                && !fecha_ultima_remision.equals("NULL")) {

            String[] fecha_hora = fecha_ultima_remision.split(":");

            if (fecha_hora.length == 5) {

                int intYearUltima = Integer.parseInt(fecha_hora[0]);
                int intMonthUltima = Integer.parseInt(fecha_hora[1]);
                int intDayUltima = Integer.parseInt(fecha_hora[2]);
                int intHourUltima = Integer.parseInt(fecha_hora[3]);
                int intMinuteUltima = Integer.parseInt(fecha_hora[4]);

                DateTime start =
                        new DateTime(intYearUltima, intMonthUltima,
                                intDayUltima, intHourUltima, intMinuteUltima);

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

    Thread BackUpJsonRemisionesThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                new RemisionBackUp(context).makeBackUp();
            } catch (Exception ex) {
                logCaughtException(ex);
            }
        }
    });
}
