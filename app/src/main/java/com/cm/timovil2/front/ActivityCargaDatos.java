package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;

import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.TaskUpdateApp;
import com.cm.timovil2.bl.utilities.TaskVerificarFechaHoraWS;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;
import com.cm.timovil2.sincro.SincroCliente;
import com.cm.timovil2.sincro.SincroCredito;
import com.cm.timovil2.sincro.SincroCuentaCaja;
import com.cm.timovil2.sincro.SincroEntregador;
import com.cm.timovil2.sincro.SincroFormaPago;
import com.cm.timovil2.sincro.SincroListaPrecios;
import com.cm.timovil2.sincro.SincroMotivoNoVenta;
import com.cm.timovil2.sincro.SincroProducto;
import com.cm.timovil2.sincro.SincroResolucion;

import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ActivityCargaDatos extends ActivityBase implements View.OnClickListener {

    private String errorProceso = "";
    private int cantProductos;
    private int cantClientes;
    private int cantEntregadores;
    private int cantMotivosNoVenta;
    private int cantCuentasCaja;
    private int cantFormasPago;
    private int cantPrecios;
    private int cantCreditos;

    private ArrayList<FacturaDTO> listaPendiente;
    private ArrayList<RemisionDTO> listaPendiente2;
    private ArrayList<GuardarMotivoNoVentaPedidoDTO> listadoNoVentaPedidoPendientes;
    private ArrayList<GuardarMotivoNoVentaDTO> listadoNoVentaPendientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carga_datos);
        setControls();
        initCantidadesDescarga();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        App.actualActivity = this;

        FacturaDAL facturaDAL = new FacturaDAL(this);
        listaPendiente = facturaDAL.obtenerListadoPendientes();

        RemisionDAL remisionDAL = new RemisionDAL(this);
        listaPendiente2 = remisionDAL.obtenerListadoPendientes();

        GuardarMotivoNoVentaPedidoDAL gmnvpDal = new GuardarMotivoNoVentaPedidoDAL(this);
        listadoNoVentaPedidoPendientes = gmnvpDal.obtenerListadoPendientes();

        GuardarMotivoNoVentaDAL gmnv = new GuardarMotivoNoVentaDAL(this);
        listadoNoVentaPendientes = gmnv.obtenerListadoPendientes();

        tieneFacturasPendientes();

        if (Utilities.isNetworkConnected(this) && Utilities.isNetworkReachable(this)) {
            new TaskVerificarFechaHoraWS(this).execute();
        }
    }

    private void initCantidadesDescarga(){
        cantProductos = 0;
        cantClientes = 0;
        cantEntregadores = 0;
        cantMotivosNoVenta = 0;
        cantCuentasCaja = 0;
        cantFormasPago = 0;
        cantPrecios = 0;
        cantCreditos = 0;
    }

    @Override
    protected void setControls() {
        TextView estado_online;
        ImageView ic_estado_online;
        estado_online = findViewById(R.id.tv_estado_online);
        ic_estado_online = findViewById(R.id.iv_estado_online);
        ic_estado_online.setAdjustViewBounds(true);

        if (!Utilities.isNetworkReachable(this) || !Utilities.isNetworkConnected(this)) {
            String con = " Sin conexión ";
            estado_online.setText(con);
            ic_estado_online.setImageResource(android.R.drawable.presence_offline);
        } else {
            String con = " Conectado ";
            estado_online.setText(con);
            ic_estado_online.setImageResource(android.R.drawable.presence_online);
        }

        Button btnMiMetaMensual = findViewById(R.id.btn_mi_meta_mensual);
        btnMiMetaMensual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float valorMetaMensual = App.obtenerConfiguracionValorVentasMensual(ActivityCargaDatos.this);
                if (valorMetaMensual > 0) {
                    mostrarProgresoMetaMensual();
                } else {
                    makeDialog("No tiene asignada una meta mensual", ActivityCargaDatos.this);
                }
            }
        });

        Button btnCargarTodo = findViewById(R.id.btnCargarTodo);
        Button btnCargarClientes = findViewById(R.id.btnCargarClientes);
        Button btnCargarProductos = findViewById(R.id.btnCargarProductos);

        if (resolucion.IdCliente.equals(Utilities.ID_VEGA)) {
            btnCargarClientes.setVisibility(View.GONE);
            btnCargarProductos.setVisibility(View.GONE);
            btnMiMetaMensual.setVisibility(View.GONE);
        }

        btnCargarTodo.setOnClickListener(this);
        btnCargarProductos.setOnClickListener(this);
        btnCargarClientes.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorDescargaDatos("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        } else {
            setControls();
        }
    }

    private void mostrarErrorDescargaDatos(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityCargaDatos.this);
        d.setTitle("No es posible cargar datos");
        d.setIcon(R.drawable.icon_launcher);
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                finish();
            }
        });
        d.show();
    }

    private void mostrarResultadoCargaDatos(String mensaje, final boolean updateApp){
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityCargaDatos.this);
        d.setTitle("TiMovil");
        d.setMessage(Html.fromHtml(mensaje));
        d.setIcon(R.drawable.icon_launcher);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        try {
                            if (updateApp) {
                                taskUpdate();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        d.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        d.show();
    }

    private void mostrarProgresoMetaMensual() {
        try {
            StringBuilder message = new StringBuilder();
            //Consultar valor ventas mensual
            double valorMetaMensual = App.obtenerConfiguracionValorVentasMensual(this);
            if (valorMetaMensual <= 0) {
                return;
            } else {
                DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
                message.append("META MENSUAL: $").append(decimalFormat.format(valorMetaMensual)).append("\n");

                double valorVentasMesRuta = resolucion.ValorVentaMensual;
                //Consultar ventas del mes de la ruta
                message.append("VENTAS POR: $").append(decimalFormat.format(valorVentasMesRuta)).append("\n");

                //Calcular porcentaje
                long porcentajeMeta = 0;
                if (valorVentasMesRuta > 0) {
                    porcentajeMeta = Math.round((valorVentasMesRuta * 100) / valorMetaMensual);
                }
                message.append("HAS ALCANZADO EL: %").append(porcentajeMeta);
            }

            //Mostrar porcentaje en caja de dialogo
            AlertDialog.Builder d = new AlertDialog.Builder(ActivityCargaDatos.this);
            d.setTitle("Meta mensual");
            d.setMessage(message);
            d.setCancelable(false);
            d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                }
            });
            d.show();
        } catch (Exception ex) {
            logCaughtException(ex);
            makeErrorDialog("Error: " + ex.getMessage(), this);
        }
    }

    private boolean tieneFacturasPendientes() {
        if (listaPendiente.size() > 0) {
            int cantidadPendiente = listaPendiente.size();
            StringBuilder sb = new StringBuilder();
            for (FacturaDTO factura : listaPendiente) {
                sb.append(factura.NumeroFactura).append(", ");
            }

            String mensaje = ("Tiene " + cantidadPendiente +
                    (cantidadPendiente > 1 ? (" facturas (" + sb.toString() + ") pendientes ") :
                            (" factura (" + sb.toString() + ") pendiente ")) + "por descargar.");
            mostrarErrorDescargaDatos(mensaje);
            return true;
        } else {
            if (listaPendiente2.size() > 0) {
                int cantidadPendiente = listaPendiente2.size();
                StringBuilder sb = new StringBuilder();
                for (RemisionDTO remision : listaPendiente2) {
                    sb.append(remision.NumeroRemision).append(", ");
                }
                String mensaje = ("Tiene " + cantidadPendiente +
                        (cantidadPendiente > 1 ? (" remisiones (" + sb.toString() + ") pendientes ")
                                : (" remisión (" + sb.toString() + ") pendiente ")) + "por descargar.");
                mostrarErrorDescargaDatos(mensaje);
                return true;
            } else {
                if (listadoNoVentaPendientes.size() > 0) {
                    int cantidadPendiente = listadoNoVentaPendientes.size();
                    StringBuilder sb = new StringBuilder();
                    for (GuardarMotivoNoVentaDTO n : listadoNoVentaPendientes) {
                        sb.append(n.Motivo).append(", ");
                    }
                    String mensaje = ("Tiene " + cantidadPendiente +
                            (cantidadPendiente > 1 ? (" registros (" + sb.toString() + ") pendientes de no venta")
                                    : (" registro (" + sb.toString() + ") pendiente de no venta")) + " por descargar.");
                    mostrarErrorDescargaDatos(mensaje);
                    return true;
                } else if (listadoNoVentaPedidoPendientes.size() > 0) {
                    int cantidadPendiente = listadoNoVentaPedidoPendientes.size();
                    StringBuilder sb = new StringBuilder();
                    for (GuardarMotivoNoVentaPedidoDTO n : listadoNoVentaPedidoPendientes) {
                        sb.append("Caso: ").append(n.IdCaso).append(", ");
                    }
                    String mensaje = ("Tiene " + cantidadPendiente +
                            (cantidadPendiente > 1 ? (" registros (" + sb.toString() + ") pendientes de no venta (Pedidos)")
                                    : (" registro (" + sb.toString() + ") pendiente de no venta (Pedidos)")) + " por descargar.");
                    mostrarErrorDescargaDatos(mensaje);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() != android.R.id.home && tieneFacturasPendientes()) {
            return true;
        }

        if (item.getItemId() != android.R.id.home && !Utilities.isNetworkReachable(this)) {
            makeErrorDialog(App.ERROR_CONECTIVIDAD, this);
            return false;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        if (!Utilities.isNetworkConnected(getApplicationContext())) {
            makeErrorDialog("En el momento no posee conexión a Internet, por favor intente nuevamente", this);
        }

        switch (view.getId()) {
            case R.id.btnCargarTodo:
                new CargaTodoWS(this).execute("");
                break;
            case R.id.btnCargarClientes:
                new CargaRuteroWS(this).execute("");
                break;
            case R.id.btnCargarProductos:
                new CargaProductosWS(this).execute("");
                break;
        }
    }

    private class CargaProductosWS extends AsyncTask<String, String, String> {

        String msgError = "";

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Estamos actualizando sus productos");
        }

        ActivityCargaDatos contexto;
        final StringBuilder message = new StringBuilder();

        private CargaProductosWS(ActivityCargaDatos contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                startBackUp();

                //Descargar resolución
                SincroResolucion.getInstance().download(contexto);

                //Descargar productos
                SincroProducto sincroProducto = SincroProducto.getInstance();
                sincroProducto.download(contexto);
                cantProductos = sincroProducto.getCantidadCargada();

                //Descargar entregadores
                SincroEntregador sincroEntregador = SincroEntregador.getInstance();
                sincroEntregador.download(contexto);
                cantEntregadores = sincroEntregador.getCantidadCargada();

                //Motivos de no venta
                SincroMotivoNoVenta sincroMotivoNoVenta = SincroMotivoNoVenta.getInstance();
                sincroMotivoNoVenta.download(contexto);
                cantMotivosNoVenta = sincroMotivoNoVenta.getCantidadCargada();

                enviar_datos_aplicacion();
            } catch (IOException e) {
                logCaughtException(e);
                msgError = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                logCaughtException(e);
                msgError = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                logCaughtException(e);
                msgError = "Error JSONException: " + e.getMessage();
            } catch (Exception e) {
                logCaughtException(e);
                msgError = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            reloadResolucion();

            if (!msgError.equals("")) {
                makeErrorDialog(msgError, ActivityCargaDatos.this);
            } else {

                final boolean updateApp;
                if (updateApp = verificarVersionAplicacion()) {
                    message.append("Su versión de TiMovil se encuentra DESACTUALIZADA, de clic ")
                            .append("en \"Aceptar\" para actualizar la aplicación a la última versión disponible.\n\n");
                }

                message.append("<b>Se han cargado:</b><br>")
                        .append("<b>"+cantProductos+"</b>").append(" productos.<br>")
                        .append("<b>"+cantEntregadores+"</b>").append(((cantEntregadores > 1 || cantEntregadores == 0) ?
                        " entregadores" : " entregador")).append("<br>")
                        .append("<b>"+cantMotivosNoVenta+"</b>").append(((cantMotivosNoVenta > 1 || cantMotivosNoVenta == 0) ?
                        " motivos de no venta" : " motivo de no venta"));

                mostrarProgresoMetaMensual();
                mostrarResultadoCargaDatos(message.toString(), updateApp);
            }
        }
    }

    private class CargaRuteroWS extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Estamos actualizando sus clientes");
        }

        ActivityCargaDatos contexto;
        final StringBuilder message = new StringBuilder();

        private CargaRuteroWS(ActivityCargaDatos contexto) {
            this.contexto = contexto;
            errorProceso = "";
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                startBackUp();

                //Descargar clientes
                SincroCliente sincroCliente = SincroCliente.getInstance();
                sincroCliente.download(contexto);
                cantClientes = sincroCliente.getCantidadCargada();

                //Descargar formas de pago
                SincroFormaPago sincroFormaPago = SincroFormaPago.getInstance();
                sincroFormaPago.download(contexto);
                cantFormasPago = sincroFormaPago.getCantidadCargada();

                //Descargar Listas de precios
                SincroListaPrecios sincroListaPrecios = SincroListaPrecios.getInstance();
                sincroListaPrecios.download(contexto);
                cantPrecios = sincroListaPrecios.getCantidadCargada();

                //Descargar Entregadores
                SincroEntregador sincroEntregador = SincroEntregador.getInstance();
                sincroEntregador.download(contexto);
                cantEntregadores = sincroEntregador.getCantidadCargada();

                enviar_datos_aplicacion();

            } catch (IOException e) {
                logCaughtException(e);
                errorProceso = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                logCaughtException(e);
                errorProceso = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                logCaughtException(e);
                errorProceso = "JSONException: " + e.getMessage();
            } catch (Exception e) {
                logCaughtException(e);
                String error = e.getMessage();
                if (error.equals(Utilities.IMEI_ERROR)) {
                    App.guardarConfiguracionEstadoAplicacion("B", contexto);
                    errorProceso = "Excepción general: Debe configurar nuevamente la aplicación TiMovil";
                } else {
                    errorProceso = "Excepción general: " + e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            reloadResolucion();

            if (!errorProceso.equals("")) {
                makeErrorDialog(errorProceso, ActivityCargaDatos.this);
            } else {

                final boolean updateApp;
                if (updateApp = verificarVersionAplicacion()) {
                    message.append("Su versión de TiMovil se encuentra DESACTUALIZADA, de clic ")
                            .append("en \"Aceptar\" para actualizar la aplicación a la última versión disponible.\n\n");
                }

                message.append("<b>Se han cargado:</b><br>")
                        .append("<b>"+cantClientes+"</b>").append(" clientes.<br>")
                        .append("<b>"+cantFormasPago+"</b>").append(" formas de pago.<br>")
                        .append("<b>"+cantPrecios+"</b>").append(" precios.<br>")
                        .append("<b>"+cantEntregadores+"</b>").append(((cantEntregadores > 1 || cantEntregadores == 0) ?
                        " entregadores" : " entregador"));

                mostrarResultadoCargaDatos(message.toString(), updateApp);
            }
        }
    }

    //----------Cargar rutero, productos y créditos--------------
    private class CargaTodoWS extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Estamos actualizando toda su información");
        }

        ActivityCargaDatos contexto;
        final StringBuilder message = new StringBuilder();

        private CargaTodoWS(ActivityCargaDatos contexto) {
            this.contexto = contexto;
            errorProceso = "";
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                startBackUp();

                //Descargar resolución
                SincroResolucion.getInstance().download(contexto);

                //Descargar productos
                SincroProducto sincroProducto = SincroProducto.getInstance();
                sincroProducto.download(contexto);
                cantProductos = sincroProducto.getCantidadCargada();

                //Descargar clientes
                SincroCliente sincroCliente = SincroCliente.getInstance();
                sincroCliente.download(contexto);
                cantClientes = sincroCliente.getCantidadCargada();

                //Descargar formas de pago
                SincroFormaPago sincroFormaPago = SincroFormaPago.getInstance();
                sincroFormaPago.download(contexto);
                cantFormasPago = sincroFormaPago.getCantidadCargada();

                //Descargar listas de precios
                SincroListaPrecios sincroListaPrecios = SincroListaPrecios.getInstance();
                sincroListaPrecios.download(contexto);
                cantPrecios = sincroListaPrecios.getCantidadCargada();

                //Descargar créditos
                SincroCredito sincroCredito = SincroCredito.getInstance();
                sincroCredito.download(contexto);
                cantCreditos = sincroCredito.getCantidadCargada();

                //Descargar cuentas de caja
                SincroCuentaCaja sincroCuentaCaja = SincroCuentaCaja.getInstance();
                sincroCuentaCaja.download(contexto);
                cantCuentasCaja = sincroCuentaCaja.getCantidadCargada();

                //Descargar entregadores
                SincroEntregador sincroEntregador = SincroEntregador.getInstance();
                sincroEntregador.download(contexto);
                cantEntregadores = sincroEntregador.getCantidadCargada();

                //Descargar motivos de no venta
                SincroMotivoNoVenta sincroMotivoNoVenta = SincroMotivoNoVenta.getInstance();
                sincroMotivoNoVenta.download(contexto);
                cantMotivosNoVenta = sincroMotivoNoVenta.getCantidadCargada();

                enviar_datos_aplicacion();
            } catch (IOException e) {
                logCaughtException(e);
                errorProceso = "Error IO: " + e.getMessage();
            } catch (XmlPullParserException e) {
                logCaughtException(e);
                errorProceso = "Error XML: " + e.getMessage();
            } catch (JSONException e) {
                logCaughtException(e);
                errorProceso = "JSONException: " + e.getMessage();
            } catch (Exception e) {
                logCaughtException(e);
                String error = e.getMessage();
                if (error != null && error.equals(Utilities.IMEI_ERROR)) {
                    App.guardarConfiguracionEstadoAplicacion("B", contexto);
                    errorProceso = "Excepción general: Debe configurar nuevamente la aplicación TiMovil";
                } else {
                    errorProceso = "Excepción general: " + e.toString();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            reloadResolucion();

            if (!errorProceso.equals("")) {
                makeErrorDialog(errorProceso, ActivityCargaDatos.this);
            } else {

                final boolean updateApp;
                if (updateApp = verificarVersionAplicacion()) {
                    message.append("Su versión de TiMovil se encuentra DESACTUALIZADA, de clic ")
                            .append("en \"Aceptar\" para actualizar la aplicación a la última versión disponible.\n\n");
                }

                message.append("<b>Se han cargado:</b><br>").append("<b>"+cantClientes+"</b>").append(" clientes <br>")
                        .append("<b>"+cantFormasPago+"</b>").append(" formas de pago <br>")
                        .append("<b>"+cantProductos+"</b>").append(" productos <br>")
                        .append("<b>"+cantCreditos+"</b>").append(" créditos <br>")
                        .append("<b>"+cantPrecios+"</b>").append(" precios <br>")
                        .append("<b>"+cantEntregadores+"</b>").append(((cantEntregadores > 1 || cantEntregadores == 0) ?
                        " entregadores" : " entregador")).append("<br>")
                        .append("<b>"+cantMotivosNoVenta+"</b>").append(((cantMotivosNoVenta > 1 || cantMotivosNoVenta == 0) ?
                        " motivos de no venta" : " motivo de no venta")).append("<br>")
                        .append("<b>"+cantCuentasCaja+"</b>").append(((cantCuentasCaja > 1 || cantCuentasCaja == 0) ?
                        " cuentas" : " cuenta<br>"));

                mostrarProgresoMetaMensual();
                mostrarResultadoCargaDatos(message.toString(), updateApp);
            }
        }
    }

    private void taskUpdate() {
        if (resolucion != null &&
                (resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                || resolucion.IdCliente.equals(Utilities.ID_POLAR))) {

            TaskUpdateApp taskUpdateApp = new TaskUpdateApp();
            taskUpdateApp.execute(NetWorkHelper.getApkUrl(this));

        } else {
            actualizarApp();
        }
    }

    /**
     * Envía el número de la versión de la app que el vendedor tiene actualmente instalada en
     * su dispositivo movil.
     *
     * @throws Exception general Exception
     */
    private void enviar_datos_aplicacion() throws Exception {

        String version_number = App.obtenerConfiguracion_VersionAplicacion(this);
        String s = "Android info";
        s += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        s += "\n OS API Level: " + android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.SDK_INT + ")";
        s += "\n Device: " + android.os.Build.DEVICE;
        s += "\n Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";

        if (version_number != null && !version_number.equals("NULL")) {
            if (resolucion == null) return;
            NetWorkHelper netWorkHelper = new NetWorkHelper();

            String url = SincroHelper.getEnviarVersionURL(Integer.parseInt(version_number),
                    resolucion.IdCliente, resolucion.CodigoRuta);
            String respuesta = netWorkHelper.writeService(url);
            respuesta = SincroHelper.procesarOkJson(respuesta);

            String url_info_android = SincroHelper.getEnviarInfoAndroidURL(s,
                    resolucion.IdCliente, resolucion.CodigoRuta);
            String respuesta_info = netWorkHelper.writeService(url_info_android);
            respuesta_info = SincroHelper.procesarOkJson(respuesta_info);

            if (!respuesta.equals("OK")) {
                throw new Exception("No se ha podido enviar la versión actual instalada de la aplicación" +
                        ", por favor comuniquese con el administrador o encargado.");
            }

            if (!respuesta_info.equals("OK")) {
                throw new Exception("No se ha podido enviar la información del sistema" +
                        ", por favor comuniquese con el administrador o encargado.");
            }
        }
    }

}