package com.cm.timovil2.front;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cm.timovil2.R;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;


public class ActivitySeleccionarBodega extends Activity {

    private String _idCliente;
    private int idBodegaSelected;
    private SparseArray<String> bodegas_aux;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.seleccionar_bodega);
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        _idCliente = extras.getString("_idCliente");
        setControls();
    }

    private void setControls() {
        try {
            Button btnAceptar;
            RadioGroup grupoBodegas;
            String bodegas[];
            btnAceptar = findViewById(R.id.btnAceptarBodega);
            grupoBodegas = findViewById(R.id.grupoBodegas);
            final ResolucionDTO resolucionDTO = new ResolucionDAL(this).ObtenerResolucion();
            if (resolucionDTO != null) {
                int i = 0;
                bodegas = resolucionDTO.Bodegas.split("-");
                bodegas_aux = new SparseArray<>();
                for (String bodega : bodegas) {
                    String detalle[] = bodega.split(":");
                    bodegas_aux.put(i+1, detalle[0]);
                    RadioButton radioBodega = new RadioButton(this);
                    radioBodega.setId(i+1);
                    String lbl = detalle[1] + " (" + detalle[0] + ")";
                    radioBodega.setText(lbl);
                    grupoBodegas.addView(radioBodega);
                    i++;
                }
                grupoBodegas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        idBodegaSelected = checkedId;
                    }
                });
                grupoBodegas.check(i);
                btnAceptar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("codigoBodega", bodegas_aux.get(idBodegaSelected));
                        intent.putExtra("_idCliente", _idCliente);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
