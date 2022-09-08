package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterInventario;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.wsentities.MDetalleListaPrecios;

import java.util.ArrayList;

public class ActivityInventario extends ActivityBase implements OnMenuItemClickListener {

    private RecyclerView recyclerInventario;
    private ArrayList<ProductoDTO> listado;
    private ArrayList<ProductoDTO> listadoCompleto;
    private ArrayList<MDetalleListaPrecios> listasPrecios;
    private EditText txtFiltro;
    private Context context;
    private int idListaPrecios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventario);
        App.actualActivity = this;
        setTitle("Resumen inventario");
        context = this;
        setControls();
        cargarListado();
    }

    @Override
    protected void setControls() {

        recyclerInventario = findViewById(R.id.recycler_inventario);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerInventario.setLayoutManager(mLayoutManager);

        txtFiltro = findViewById(R.id.txtFiltroInventario);
        if (txtFiltro != null) {
            txtFiltro.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btn_search, 0, 0, 0);
            txtFiltro.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    filtrarInventario();
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }
    }

    private void cargarListado() {

        try {

            listasPrecios = new ProductoDAL(this).obtenerListasPrecios();
            listadoCompleto = new ProductoDAL(this).obtenerListadoCompleto();

            ArrayList<Integer> idsLista = new ArrayList<>();
            ArrayList<MDetalleListaPrecios> selected = new ArrayList<>();
            for (MDetalleListaPrecios m: listasPrecios){
                if(!idsLista.contains(m.IdListaPrecios))
                {
                    idsLista.add(m.IdListaPrecios);
                    selected.add(m);
                }
            }

            listasPrecios = selected;

            ArrayAdapter<MDetalleListaPrecios> adapter = new ArrayAdapter<>(this, R.layout.actionbar_spinner_dropdown, listasPrecios);
            createListNavigationModeOnActionBar(adapter);

            if(listasPrecios.size()>0){
                cargarProductos(idListaPrecios = listasPrecios.get(0).IdListaPrecios);
            }

        } catch (Exception e) {

            makeErrorDialog("Error cargando el inventario.", ActivityInventario.this);

        }
    }

    private void createListNavigationModeOnActionBar(ArrayAdapter<MDetalleListaPrecios> adapter) {

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ab.setListNavigationCallbacks(adapter,
                    new ActionBar.OnNavigationListener() {
                        @Override
                        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                            MDetalleListaPrecios dlp = listasPrecios.get(itemPosition);
                            idListaPrecios = dlp.IdListaPrecios;
                            cargarProductos(idListaPrecios);
                            txtFiltro.setText("");
                            return false;
                        }
                    });
            ab.setSelectedNavigationItem(0);
        }
    }

    private void cargarProductos(int idListaPrecios){

        if(idListaPrecios > 0){
            listado = new ProductoDAL(context).obtenerProductosPorListaPrecios(idListaPrecios, listadoCompleto);
        }else{
            listado = new ArrayList<>();
        }

        loadProductos(listado);

    }

    private void loadProductos(ArrayList<ProductoDTO> listado){
        RecyclerView.Adapter mAdapter = new AdapterInventario(this, listado);
        recyclerInventario.setAdapter(mAdapter);
    }

    private void mostrarErrorYsalir(String mensaje){
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityInventario.this);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.validarEstadoAplicacion(this)) {
            mostrarErrorYsalir("La aplicación se encuentra bloqueada, " +
                    "por favor configure nuevamente la ruta");
        }else {
            cargarProductos(idListaPrecios);
            txtFiltro.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inventario, menu);
        MenuItem item = menu.findItem(R.id.mnuImprimirInventario);
        if (item != null) {
            item.setOnMenuItemClickListener(this);
        }
        return true;
    }

    public boolean onMenuItemClick(MenuItem arg) {
        if(arg.getItemId() == R.id.mnuImprimirInventario){
            imprimir();
        }
        return true;
    }


    private void imprimir() {
        try {
            if(App.obtenerConfiguracion_imprimir(this)){
                Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 1);
                printer.printInventario();
            }else{
                makeErrorDialog(getResources().getString(R.string.lbl_no_imprimir), ActivityInventario.this);
            }
        } catch (Exception e) {
            makeErrorDialog("Error imprimiendo el inventario: " + e.getMessage(), ActivityInventario.this);
        }
    }

    private void filtrarInventario() {
        try {
            if (txtFiltro != null && txtFiltro.getText() != null) {
                ProductoDAL dal = new ProductoDAL(this);

                ArrayList<ProductoDTO> lFiltro = dal.filtrarProductos2(
                        listado, txtFiltro.getText().toString());

                loadProductos(lFiltro);
            }
        } catch (Exception e) {
            makeErrorDialog("Error cargando el rutero: " + e.getMessage(), ActivityInventario.this);
        }
    }

    // ==================================================================================================//
}