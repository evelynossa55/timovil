package com.cm.timovil2.vehiculos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.R;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 19/01/2015.
 */
public class IdentificarEmpleadoActivity extends ActivityBase {

    private EditText txtIdentificacion;
    private EditText txtPlaca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identificar_empleado_vehiculo);
        setControls();
    }

    @Override
    protected void setControls() {
        txtIdentificacion = findViewById(R.id.txtCedulaEmpleado);
        txtPlaca = findViewById(R.id.txtPlacaVehiculo);
        Button btnAceptar = findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarDatos();
            }
        });
    }

    private void enviarDatos() {

        if (txtIdentificacion.getText() == null ||
            txtIdentificacion.getText().toString().trim().equals("")) {
            txtIdentificacion.setError("Campo requerido");
            return;
        }

        if (txtPlaca.getText() == null ||
            txtPlaca.getText().toString().trim().equals("")) {
            txtPlaca.setError("Campo requerido");
            return;
        }

        Intent intent = new Intent(this, GastosVehiculoActivity.class);
        intent.putExtra(IDENT_EMPLEADO, txtIdentificacion.getText().toString());
        intent.putExtra(PLACA_VEHICULO, txtPlaca.getText().toString());
        startActivity(intent);
    }

    public static final String IDENT_EMPLEADO = "co.com.timovil.vehiculos.IDENT_EMPLEADO";
    public static final String PLACA_VEHICULO = "co.com.timovil.vehiculos.PLACA_VEHICULO";
}
