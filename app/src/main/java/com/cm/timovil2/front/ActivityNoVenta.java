package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.cm.timovil2.R;
import com.cm.timovil2.backup.NoVentaBackUp;
import com.cm.timovil2.backup.NoVentaPedidoBackUp;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.data.MotivoNoVentaDAL;
import com.cm.timovil2.data.ResultadoGestionCasoCallcenterDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.dto.MotivoNoVentaDTO;
import com.cm.timovil2.dto.ResultadoGestionCasoCallcenterDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 04/08/2015.
 */
public class ActivityNoVenta extends ActivityBase {

    private Spinner spinnerMotivos;
    private EditText editTextOtroMotivo;
    private EditText editTextDescripcion;
    private int idCliente;
    private boolean isPedidoCallcenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_venta);
        setControls();

        Intent intent = getIntent();
        idCliente = intent.getIntExtra("IdCliente", -1);
        isPedidoCallcenter = intent.getBooleanExtra("IsPedidoCallcenter", false);
        App.actualActivity = this;

        if (idCliente <= 0) {
            mostratMensajeYSalir("No se especificó el cliente", RESULT_CANCELED);
        } else if(!isPedidoCallcenter){

            //Si es un pedido si permitimos ingresar la No Venta, apesar de que ya haya sido atendido

            try {
                ClienteDAL clienteDAL = new ClienteDAL(this);
                ClienteDTO clienteDTO = clienteDAL.ObtenerClientePorIdCliente(String.valueOf(idCliente));
                Calendar calendar = new GregorianCalendar();
                String fecha =
                        (calendar.get(Calendar.DAY_OF_MONTH)) + "/"
                                + (calendar.get(Calendar.MONTH) + 1) + "/"
                                + (calendar.get(Calendar.YEAR));
                if (clienteDTO.Atendido.equals(fecha)) {
                    mostratMensajeYSalir("El cliente ya ha sido atendido el día de hoy", RESULT_CANCELED);
                }
            } catch (Exception e) {
                logCaughtException(e);
                mostratMensajeYSalir("Error con el cliente: " + e.getMessage(), RESULT_CANCELED);
            }
        }

        if(isPedidoCallcenter){
            editTextOtroMotivo.setEnabled(false);
            fillSpinnerMotivosPedido(getMotivosPedido());
        }else{
            fillSpinnerMotivos(getMotivos());
        }

    }

    private void mostratMensajeYSalir(String mensaje, final int result) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("No venta");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                setResult(result);
                finish();
            }
        });
        d.show();
    }

    @Override
    protected void setControls() {
        spinnerMotivos = findViewById(R.id.spinner_motivos);
        editTextDescripcion = findViewById(R.id.edit_text_descripcion);
        editTextOtroMotivo = findViewById(R.id.edit_text_otro_motivo);
        Button btnGuardarNoVenta = findViewById(R.id.btn_guardar_no_venta);
        btnGuardarNoVenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isPedidoCallcenter){
                    guardarNoVentaPedido();
                }else {
                    guardarNoVenta();
                }
            }
        });

        obtenerUbicacion();

        editTextOtroMotivo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String otro = editTextOtroMotivo.getText().toString();
                if (TextUtils.isEmpty(otro)) {
                    fillSpinnerMotivos(getMotivos());
                } else {
                    fillSpinnerMotivos(new ArrayList<MotivoNoVentaDTO>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void fillSpinnerMotivos(ArrayList<MotivoNoVentaDTO> list) {
        ArrayAdapter<MotivoNoVentaDTO> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, list);
        spinnerMotivos.setAdapter(adapter);
    }

    private void fillSpinnerMotivosPedido(ArrayList<ResultadoGestionCasoCallcenterDTO> list) {
        ArrayAdapter<ResultadoGestionCasoCallcenterDTO> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, list);
        spinnerMotivos.setAdapter(adapter);
    }

    private ArrayList<MotivoNoVentaDTO> getMotivos() {
        MotivoNoVentaDAL motivoNoVentaDAL = new MotivoNoVentaDAL(this);
        return motivoNoVentaDAL.getLista();
    }

    private ArrayList<ResultadoGestionCasoCallcenterDTO> getMotivosPedido() {
        ResultadoGestionCasoCallcenterDAL motivoNoVentaDAL = new ResultadoGestionCasoCallcenterDAL(this);
        return motivoNoVentaDAL.ObtenerListado();
    }

    private void guardarNoVentaPedido(){
        try {
            ResultadoGestionCasoCallcenterDTO r =
                    (ResultadoGestionCasoCallcenterDTO) spinnerMotivos.getSelectedItem();

            if(TextUtils.isEmpty(editTextDescripcion.getText())){
                editTextDescripcion.setError("Debe ingresar un comentario");
                return;
            }

            new SyncNoVentaPedidoCallcenter(this)
                    .execute(resolucion.IdCliente,
                            resolucion.CodigoRuta,
                            String.valueOf(r.IdResultadoGestion),
                            editTextDescripcion.getText().toString());

        }catch (Exception e){
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), this);
        }
    }

    private void guardarNoVenta() {

        try {

            Object object_motivo = spinnerMotivos.getSelectedItem();
            Object object_otro_motivo = editTextOtroMotivo.getText();
            Object object_descripcion = editTextDescripcion.getText();

            String motivo;
            long idMotivo = 0;
            String descripcion;
            int idCliente = 0;

            boolean is_otro_motivo = false;
            if (object_motivo == null) {
                if (object_otro_motivo == null
                        || TextUtils.isEmpty(object_otro_motivo.toString())) {
                    makeDialog("Debe seleccionar el motivo de la no venta", this);
                    return;
                } else {
                    motivo = object_otro_motivo.toString();
                    is_otro_motivo = true;
                }
            } else {
                motivo = ((MotivoNoVentaDTO) object_motivo).Descripcion;
                idMotivo = ((MotivoNoVentaDTO) object_motivo).IdMotivo;
            }

            if (is_otro_motivo
                    && (object_descripcion == null
                    || TextUtils.isEmpty(object_descripcion.toString()))) {
                editTextDescripcion.setError("Debe ingresar la descripción");
                makeDialog("Debe ingresar la descripción", this);
                return;
            } else {
                descripcion = object_descripcion.toString();
            }

            if (this.idCliente <= 0) {
                mostratMensajeYSalir("No se especificó el cliente", RESULT_CANCELED);
            } else {
                idCliente = this.idCliente;
            }

            String latitud = App.obtenerConfiguracion_latitudActual(this);
            String longitud = App.obtenerConfiguracion_longitudActual(this);
            Date date = new Date();
            String fecha = Utilities.FechaHoraAnsi(date);
            String codigoRuta = resolucion.CodigoRuta;
            String idClienteTimo = resolucion.IdCliente;

            //Crear json
            JSONObject jsonNoVenta = new JSONObject();
            jsonNoVenta.put("IdC", idCliente);
            jsonNoVenta.put("IdM", idMotivo);
            jsonNoVenta.put("CR", codigoRuta);
            jsonNoVenta.put("Des", descripcion);
            jsonNoVenta.put("F", fecha);
            jsonNoVenta.put("Fl", date.getTime());
            jsonNoVenta.put("La", latitud);
            jsonNoVenta.put("Lo", longitud);
            jsonNoVenta.put("IdCT", idClienteTimo);
            jsonNoVenta.put("O", is_otro_motivo);
            jsonNoVenta.put("ODes", motivo);
            jsonNoVenta.put("EnvDes", Utilities.FACTURA_ENVIADA_DESDE_FACTURACION);

            new SyncNoVenta(this).execute(jsonNoVenta);

        } catch (Exception e) {
            logCaughtException(e);
            makeDialog("Error guardando la no venta: " + e.toString(), this);
        }
    }

    private class SyncNoVenta extends AsyncTask<JSONObject, String, String> {

        Context contexto;

        private SyncNoVenta(Context contexto) {
            this.contexto = contexto;
        }

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Enviando la no venta");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.getDialog().dismiss();

            if (s != null && !s.equals("")) {
                makeErrorDialog(s, ActivityNoVenta.this);
            } else {
                mostratMensajeYSalir("Registro de no venta creado con éxito", RESULT_OK);
                backUpNoVentas();
            }
        }

        @Override
        protected String doInBackground(JSONObject... params) {
            String error = null;
            try {

                String jsonRespuesta = null;

                if(Utilities.isNetworkReachable(contexto)
                        && Utilities.isNetworkConnected(contexto)){

                    NetWorkHelper netWorkHelper = new NetWorkHelper();
                    jsonRespuesta = netWorkHelper.writeService(params[0], SincroHelper.getIngresarNoVentaURL());
                    jsonRespuesta = SincroHelper.procesarOkJson(jsonRespuesta);
                    if (!jsonRespuesta.equals("OK")) {
                        error = jsonRespuesta;
                    }
                }

                GuardarMotivoNoVentaDTO noVenta = new GuardarMotivoNoVentaDTO();
                noVenta.IdCliente = params[0].getInt("IdC");
                noVenta.IdMotivo = params[0].getInt("IdM");
                noVenta.CodigoRuta = params[0].getString("CR");
                noVenta.Descripcion = params[0].getString("Des");
                noVenta.Fecha = params[0].getString("F");
                noVenta.Fecha_long = params[0].getLong("Fl");
                noVenta.Latitud = params[0].getString("La");
                noVenta.Longitud = params[0].getString("Lo");
                noVenta.IdClienteTimovil = params[0].getString("IdCT");
                noVenta.esOtroMotivo = params[0].getBoolean("O");
                noVenta.Motivo = params[0].getString("ODes");
                noVenta.Sincronizada = (jsonRespuesta != null && jsonRespuesta.equals("OK"));

                guardarNoVentaSqlite(noVenta);

            } catch (Exception e) {
                logCaughtException(e);
                error = e.getMessage();
            }
            return error;
        }
    }

    private class SyncNoVentaPedidoCallcenter extends AsyncTask<String, String, String> {

        Context contexto;

        private SyncNoVentaPedidoCallcenter(Context contexto) {
            this.contexto = contexto;
        }

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Enviando la no venta");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.getDialog().dismiss();

            if (s != null && !s.equals("")) {
                makeErrorDialog(s, ActivityNoVenta.this);
            } else {
                mostratMensajeYSalir("Registro de no venta creado con éxito", RESULT_OK);
                backUpNoVentasPedidos();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String error = null;
            try {

                String respuesta_confirmar_pedido = null;

                if(Utilities.isNetworkReachable(contexto)
                        && Utilities.isNetworkConnected(contexto)){

                    NetWorkHelper netWorkHelper = new NetWorkHelper();
                    JSONObject jsonConfirmacion = new JSONObject();
                    jsonConfirmacion.put("IdClienteTiMovil", params[0]);
                    jsonConfirmacion.put("CodigoRuta", params[1]);
                    jsonConfirmacion.put("IdMotivoNegativo", Integer.valueOf(params[2]));
                    jsonConfirmacion.put("Comentario", params[3]);
                    jsonConfirmacion.put("IdCaso", App.pedido_actual.IdCaso);
                    jsonConfirmacion.put("EsFactura", true);
                    jsonConfirmacion.put("NumeroDocumento", "");
                    jsonConfirmacion.put("EnviadaDesde", Utilities.FACTURA_ENVIADA_DESDE_FACTURACION);

                    respuesta_confirmar_pedido = netWorkHelper.writeService(jsonConfirmacion, SincroHelper.CONFIRMAR_PEDIDO);
                    respuesta_confirmar_pedido = SincroHelper.procesarOkJson(respuesta_confirmar_pedido);

                    if(!respuesta_confirmar_pedido.equals("OK")){
                        error = respuesta_confirmar_pedido;
                    }
                }

                GuardarMotivoNoVentaPedidoDTO noVenta = new GuardarMotivoNoVentaPedidoDTO();
                noVenta.IdClienteTimovil = params[0];
                noVenta.IdCaso = App.pedido_actual.IdCaso;
                noVenta.CodigoRuta = params[1];
                noVenta.IdResultadoGestion = Integer.valueOf(params[2]);
                noVenta.Descripcion = params[3];
                noVenta.Sincronizada =
                        (respuesta_confirmar_pedido != null &&
                                respuesta_confirmar_pedido.equals("OK"));
                noVenta.Fecha = new Date().getTime();
                noVenta.IdCliente = String.valueOf(idCliente);

                guardarNoVentaPedidoSqlite(noVenta);

            } catch (Exception e) {
                logCaughtException(e);
                error = e.getMessage();
            }
            return error;
        }
    }

    private void guardarNoVentaSqlite(GuardarMotivoNoVentaDTO noVenta){
        GuardarMotivoNoVentaDAL dal = new GuardarMotivoNoVentaDAL(this);
        dal.Insertar(noVenta);
    }

    private void guardarNoVentaPedidoSqlite(GuardarMotivoNoVentaPedidoDTO noVentaPedido){
        GuardarMotivoNoVentaPedidoDAL dal = new GuardarMotivoNoVentaPedidoDAL(this);
        dal.Insertar(noVentaPedido);
    }

    private void backUpNoVentas(){
        if (BackUpJsonNoVentasThread.getState() == Thread.State.NEW) {
            BackUpJsonNoVentasThread.start();
        }
    }

    private void backUpNoVentasPedidos(){
        if (BackUpJsonNoVentasPedidoThread.getState() == Thread.State.NEW) {
            BackUpJsonNoVentasPedidoThread.start();
        }
    }

    Thread BackUpJsonNoVentasThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                new NoVentaBackUp(context).makeBackUp();

            } catch (Exception ex) {
                logCaughtException(ex);
            }
        }
    });

    Thread BackUpJsonNoVentasPedidoThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                new NoVentaPedidoBackUp(context).makeBackUp();

            } catch (Exception ex) {
                logCaughtException(ex);
            }
        }
    });
}
