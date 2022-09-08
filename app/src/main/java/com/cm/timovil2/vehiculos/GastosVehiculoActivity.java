package com.cm.timovil2.vehiculos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ConceptoMantenimientoDTO;
import com.cm.timovil2.dto.EmpleadoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.VehiculoDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class GastosVehiculoActivity extends ActivityBase {

    private ResolucionDTO resolucionDTO;
    private Spinner cboConceptos;
    private EditText txtKilometraje;
    private EditText txtValor;
    private EditText txtDescripcion;
    //---------------------------------
    private String cedulaEmpleado;
    private String placaVehiculo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gastos_vehiculo);
        App.actualActivity = this;
        Intent intent = getIntent();
        cedulaEmpleado = intent.getStringExtra(IdentificarEmpleadoActivity.IDENT_EMPLEADO);
        placaVehiculo = intent.getStringExtra(IdentificarEmpleadoActivity.PLACA_VEHICULO);
        setControls();
    }

    @Override
    protected void onResume() {
        if (Utilities.isNetworkReachable(this) && Utilities.isNetworkConnected(this)) {
            ingresar();
        } else {
            terminarProcesoMovimiento("Para generar movimientos de gastos de vehículo debe poseer conexión a Internet");
        }
        super.onResume();
    }

    @Override
    protected void setControls() {
        try {
            cboConceptos = findViewById(R.id.cboConceptos);
            txtKilometraje = findViewById(R.id.txtKilometraje);
            txtValor = findViewById(R.id.txtValor);
            txtDescripcion = findViewById(R.id.txtDescripcion);
            Button btnEnviar = findViewById(R.id.btnEnviar);
            resolucionDTO = new ResolucionDAL(this).ObtenerResolucion();
            btnEnviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enviarGastoVehiculo();
                }
            });

        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), GastosVehiculoActivity.this);
        }
    }

    private void cargarConceptos() {
        if (cboConceptos != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            ArrayAdapter<ConceptoMantenimientoDTO> adaptadorCombo = new ArrayAdapter<>(
                    this, R.layout.spinner_text, App.conceptoMantenimiento);
            cboConceptos.setAdapter(adaptadorCombo);

            cboConceptos.setSelection(ObtenerIndexSinNovedadItem());
        }
    }

    /**
     * @return Index del item 'Sin novedad'
     */
    private int ObtenerIndexSinNovedadItem() {
        int index = 0;
        ArrayList<ConceptoMantenimientoDTO> c = App.conceptoMantenimiento;
        for (int i = 0; i < c.size(); i++) {
            if (c.get(i).Descripcion.equals("Sin novedad")) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void enviarGastoVehiculo() {
        try {
            int concepto, empleado, vehiculo, kilometraje = 0, proximoMantenimiento;
            String descripcion = "", nombreVendedor, idCliente;
            float valor = 0;
            boolean sinNovedad;

            ConceptoMantenimientoDTO conceptoMto = getSelectedConcepto();
            proximoMantenimiento = conceptoMto.DuracionMantenimiento;
            sinNovedad = conceptoMto.Descripcion.equals("Sin novedad");
            concepto = Integer.parseInt(conceptoMto.IdConceptoMantenimiento);
            empleado = Integer.parseInt(App.empleado.IdEmpleado);
            vehiculo = App.vehiculo.IdVehiculo;
            nombreVendedor = encode(App.empleado.Nombres + App.empleado.Apellidos);
            idCliente = encode(resolucionDTO.IdCliente);

            String valueAux;
            if (!sinNovedad && txtKilometraje != null && txtKilometraje.getText() != null) {
                valueAux = txtKilometraje.getText().toString();
                kilometraje = (valueAux.equals("") ? 0 : Integer.parseInt(valueAux));
                if (kilometraje <= 0) {
                    txtKilometraje.setError("Requerido");
                    txtKilometraje.requestFocus();
                    return;
                }
            }

            if (!sinNovedad && txtValor != null && txtValor.getText() != null) {
                valueAux = txtValor.getText().toString();
                valor = valueAux.equals("") ? 0 : Integer.parseInt(valueAux);
                if (valor <= 0) {
                    txtValor.setError("Requerido");
                    txtValor.requestFocus();
                    return;
                }
            }

            if (!sinNovedad && txtDescripcion != null && txtDescripcion.getText() != null) {
                descripcion = encode(txtDescripcion.getText().toString());
            }

            String gastoURL = SincroHelper.getGastoURL(concepto, empleado, vehiculo, valor,
                    descripcion, kilometraje, proximoMantenimiento, nombreVendedor, idCliente,
                    sinNovedad);

            new generarMovimiento(this).execute(gastoURL);

        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), GastosVehiculoActivity.this);
        }
    }

    private ConceptoMantenimientoDTO getSelectedConcepto() {
        ConceptoMantenimientoDTO concepto;
        if (cboConceptos != null && cboConceptos.getSelectedItem() != null) {
            concepto = (ConceptoMantenimientoDTO) cboConceptos.getSelectedItem();
        } else {
            concepto = new ConceptoMantenimientoDTO();
            concepto.IdConceptoMantenimiento = "0";
        }
        return concepto;
    }

    private String encode(String parameter) {
        try {
            if (parameter == null || parameter.equals("")) {
                return "---";
            }
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return parameter.replace(" ", "_");
        }
    }

    private class generarMovimiento extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Generando el movimiento");
        }

        Context contexto;

        private generarMovimiento(Context contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {
            String respuesta;
            try {
                String gastoURL = params[0];

                NetWorkHelper netWorkHelper = new NetWorkHelper();
                respuesta = netWorkHelper.writeService(gastoURL);
                boolean sw = SincroHelper.procesarJsonMantenimiento(respuesta);

                if (sw) {
                    respuesta = "OK";
                } else {
                    respuesta = "NO";
                }

            } catch (Exception ex) {
                respuesta = ex.getMessage();
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            switch (result) {

                case "OK":
                    terminarProcesoMovimiento("El movimiento se ha generado satisfactoriamente");
                    break;
                case "NO":
                    terminarProcesoMovimiento("No se ha generado el movimiento satisfactoriamente, por favor intente más tarde");
                    break;
                default:
                    terminarProcesoMovimiento(result);
                    break;
            }
        }
    }

    private void terminarProcesoMovimiento(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(getResources().getString(R.string.app_name));
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                onBackPressed();
            }
        });
        d.show();
    }

    private void ingresar() {

        if (resolucionDTO == null || resolucionDTO.CodigoRuta == null || resolucionDTO.CodigoRuta.equals("")) {
            terminarProcesoMovimiento("Por favor configure TiMovil nuevamente");
            return;
        }

        if (cedulaEmpleado == null || cedulaEmpleado.trim().equals("")
                || placaVehiculo == null || placaVehiculo.trim().equals("")) {
            terminarProcesoMovimiento("Por favor ingrese su número de cédula y la " +
                    "matrícula de su vehículo");
            return;
        }

        new IdentificarEmpleado(this).execute(cedulaEmpleado, placaVehiculo);
    }

    private class IdentificarEmpleado extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(contexto, "Estamos validando su identificación");
        }

        Context contexto;

        private IdentificarEmpleado(Context contexto) {
            this.contexto = contexto;
        }

        @Override
        protected String doInBackground(String... params) {
            String respuesta;
            try {
                String identificacion = params[0];
                String placa = params[1];

                EmpleadoDTO empleado;
                VehiculoDTO vehiculo;
                ArrayList<ConceptoMantenimientoDTO> conceptos;
                String idCliente = resolucionDTO.IdCliente;
                NetWorkHelper netWorkHelper = new NetWorkHelper();

                String jsonVehiculo = netWorkHelper.readService(SincroHelper.getVehiculoURL(placa, idCliente));
                if (!jsonVehiculo.equals("")) {
                    vehiculo = SincroHelper.procesarJsonVehiculo(jsonVehiculo);
                    App.vehiculo = vehiculo;
                } else {
                    throw new Exception("No se ha encontrado el vehículo del empleado, por favor ingrese nuevamente");
                }

                String jsonEmpleado = netWorkHelper.readService(SincroHelper.getEmpleadoURL(identificacion, idCliente));
                if (!jsonEmpleado.equals("")) {
                    empleado = SincroHelper.procesarJsonEmpleado(jsonEmpleado);
                    if (empleado.Identificacion.equals(identificacion)) {
                        App.empleado = empleado;
                    } else {
                        throw new Exception("Error identificando el empleado, por favor intente nuevamente");
                    }
                } else {
                    throw new Exception("El empleado con identificación [" + identificacion + "] no se encuentra registrado");
                }

                String jsonConceptos = netWorkHelper.readService(SincroHelper.getConceptoMantenimientoURL(idCliente));
                if (!jsonConceptos.equals("")) {
                    conceptos = SincroHelper.procesarJsonConceptos(jsonConceptos);
                    if(conceptos != null && !conceptos.isEmpty()){
                        App.conceptoMantenimiento = conceptos;
                   }
                } else {
                    throw new Exception("No se han encontrado los conceptos de mantenimiento, intente nuevamente");
               }

                respuesta = "OK";

            } catch (Exception e) {
                respuesta = e.getMessage();
            }
            return respuesta;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.getDialog().dismiss();

            if (result.equals("OK")) {
                cargarConceptos();
            } else {
                terminarProcesoMovimiento(result);
            }
        }
    }
}
