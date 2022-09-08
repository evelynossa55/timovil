package com.cm.timovil2.front;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cm.timovil2.R;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class ActivityConfirmarPass extends Activity {

    private EditText txtPass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.confirmar_pass_admin);
        setControls();
    }

    private void setControls() {
        try {
            Button btnAceptar = findViewById(R.id.btnAceptarConfirmacion);
            txtPass = findViewById(R.id.txtPass);
            final ResolucionDTO resolucionDTO = new ResolucionDAL(this).ObtenerResolucion();
            if (resolucionDTO != null) {
                btnAceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String pass = txtPass.getText().toString();
                        if (resolucionDTO.ClaveAdmin.equals(pass)) {
                            setResult(RESULT_OK);
                        } else {
                            setResult(RESULT_CANCELED);
                        }
                        finish();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}