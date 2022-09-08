package com.cm.timovil2.front;

import java.util.ArrayList;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.dto.DetalleFacturaDTO;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ActivityFiltroProducto extends ActivityBase {

    private ListView lsvFiltro;
    private EditText txtFiltroProductos;
    private ProductoDAL productoDal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filtro_productos);
        App.actualActivity = this;
        setControls();
        productoDal = new ProductoDAL(this);
        cargarProductos();
    }

    @Override
    protected void setControls() {
        txtFiltroProductos = findViewById(R.id.txtFiltroProductos);
        lsvFiltro = findViewById(R.id.lsvFiltroProductos);

        txtFiltroProductos.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,int count) {
                cargarProductos();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            public void afterTextChanged(Editable s) {}
        });

        lsvFiltro.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                DetalleFacturaDTO dto = (DetalleFacturaDTO) lsvFiltro.getItemAtPosition(position);
                try {
                    if (dto != null) {
                        Intent resultData = new Intent();
                        resultData.putExtra("idProducto", dto.IdProducto);
                        setResult(Activity.RESULT_OK, resultData);
                        finish();
                    }
                } catch (Exception e) {
                    makeErrorDialog(e.getMessage(), ActivityFiltroProducto.this);
                }
            }
        });
    }

    private void cargarProductos() {
        try {
            Editable strfiltro = txtFiltroProductos.getText();
            if (strfiltro != null) {

                ArrayList<DetalleFacturaDTO> filtro =
                        productoDal.filtrarProductos(App.DetalleFacturacion, strfiltro.toString());

                ArrayAdapter<DetalleFacturaDTO> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, filtro);

                lsvFiltro.setAdapter(adapter);
            }
        } catch (Exception e) {
            makeErrorDialog(e.getMessage(), ActivityFiltroProducto.this);
        }
    }

}
