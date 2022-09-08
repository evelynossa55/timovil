package com.cm.timovil2.front;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;

public class ActivityComentarioFactura extends Activity{

    private EditText txtComentario;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.comentario_factura);

        Bundle estado = getIntent().getExtras();
        setControls();
        if(estado!=null && estado.getString("estado", "facturacion").equals("anulacion")){
            txtComentario.setText("");
        }else{
            txtComentario.setText(App.ComentarioFactura);
        }
    }

    private void setControls() {
        Button btnAceptar = findViewById(R.id.btnAceptarComentario);
        txtComentario = findViewById(R.id.txtComentarioFactura);
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.ComentarioFactura = txtComentario.getText().toString();
                if(App.ComentarioFactura != null && !App.ComentarioFactura.trim().equals("")){
                    setResult(RESULT_OK);
                }else{
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
        });
    }
}
