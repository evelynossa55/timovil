package com.cm.timovil2.front;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterMenuVendedor;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.CustomProgressBar;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.pref.Configuraciones;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;
import com.cm.timovil2.vehiculos.IdentificarEmpleadoActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class ActivityMenuVendedor extends ActivityBase implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<String> values;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NestedScrollView nestedScrollView;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_vendedor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setControls();

    }


    @Override
    protected void setControls() {
        values = new ArrayList<>();
        setContentView(R.layout.activity_menu_vendedor);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);
        nestedScrollView=findViewById(R.id.nsv);


        TextView toolbar_titulo = findViewById(R.id.tituloToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        toolbar_titulo.setText(resolucion.NombreRuta + " - " + resolucion.CodigoRuta);


        //establecer el evento onclick al navigationView
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        RecyclerView recyclerMenuVendedor = findViewById(R.id.recycler_menu_vendedor);
        ImageView LogoVendedor = findViewById(R.id.iv_logo_timovil);
        //RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        recyclerMenuVendedor.setLayoutManager(mLayoutManager);

        recyclerMenuVendedor.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide));
        LogoVendedor.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide));
        try {
            String tipoRuta = App.obtenerConfiguracion_tipoRuta(this);
            if (tipoRuta.equals("Asesor comercial")) {
                values.add("Rutero");
                values.add("Cargar datos");
                values.add("Informe de gestión");
            } else {
                values.add("Cargar datos");
                values.add("Rutero");
                if (!resolucion.IdCliente.equals(Utilities.ID_DOBLEVIA)) {
                    values.add("Créditos");
                    values.add("Facturas");
                    values.add("Remisiones");
                    values.add("Notas Crédito");
                    values.add("No ventas");
                    values.add("Inventario");
                    values.add("Gastos vehículo");
                    values.add("Pedidos logística");
                    values.add("Ventas mes");

                } else {
                    values.add("Facturas");
                    values.add("Notas Crédito");
                    values.add("No ventas");
                    values.add("Inventario");
                    values.add("Gastos vehículo");
                    values.add("Pedidos logística");
                    values.add("Ventas mes");
                }



/*
                if(!resolucion.IdCliente.equals(Utilities.ID_DOBLEVIA)) {
                    //values.add("Impresora");
                    values.add("Gastos vehículo");
                    //values.add("Ajustes");
                }*/

            }

        } catch (Exception e) {
            logCaughtException(e);
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

        AdapterMenuVendedor adapter = new AdapterMenuVendedor(values, this);
        adapter.setOnClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                abrirOpcionMenu(position);
            }
        });

        recyclerMenuVendedor.setAdapter(adapter);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        drawerLayout.closeDrawer(GravityCompat.START);
        Intent i = null;
        if (menuItem.getItemId() == R.id.impresoras) {
            i = new Intent(this, ActivityConfigImpresora.class);
        }
        if (menuItem.getItemId() == R.id.salir) {
            i = new Intent(this, ActivityLogin.class);
            Toast.makeText(getApplicationContext(), "Sesión cerrada", Toast.LENGTH_LONG).show();
        }
        if (menuItem.getItemId() == R.id.ajustes) {
            i = new Intent(this, Configuraciones.class);

        }
        startActivity(i);
        return false;
    }

    private void abrirOpcionMenu(int position) {

        if (App.validarEstadoAplicacion(this)) {
            Intent i = null;
            String item = values.get(position);

            switch (item) {
                case "Impresora":
                    i = new Intent(this, ActivityConfigImpresora.class);
                    break;
                case "Rutero":

                    try {
                        reloadResolucion();
                        if (resolucion.DiaCerrado) {
                            Toast.makeText(getApplicationContext(), "Día cerrado, consultar con el administrador.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        logCaughtException(e);
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    String tipoRuta = App.obtenerConfiguracion_tipoRuta(this);

                    if (!tipoRuta.equals("Vendedor")) {
                        i = new Intent(this, ActivityRuteroAsesor.class);
                    } else {
                        i = new Intent(this, ActivityRutero.class);
                    }

                    break;
                case "Facturas":
                    i = new Intent(this, ActivityFacturas.class);
                    break;
                case "Inventario":
                    i = new Intent(this, ActivityInventario.class);
                    break;
                case "Cargar datos":
                    i = new Intent(this, ActivityCargaDatos.class);
                    break;
                case "Informe de gestión":
                    i = new Intent(this, ActivityGestionesComerciales.class);
                    break;
                case "Créditos":
                    i = new Intent(this, ActivityCreditos.class);
                    break;
                case "Ajustes":
                    i = new Intent(this, Configuraciones.class);
                    break;
                case "Gastos vehículo":
                    i = new Intent(this, IdentificarEmpleadoActivity.class);
                    break;
                case "Remisiones":
                    i = new Intent(this, ActivityRemisiones.class);
                    break;
                case "Ventas mes":
                    String mensaje = "";
                    try {
                        mensaje = resolucion.getCumplimientoMeta();
                    } catch (Exception ex) {
                        logCaughtException(ex);
                        mostrarDialogo("Error", ex.toString());
                    }
                    mostrarDialogo("Cumplimiento ventas", mensaje);
                    break;
                case "Pedidos logística":
                    i = new Intent(this, ActivityPedidos.class);
                    break;
                case "Enviar pedidos":
                    if (!Utilities.isNetworkConnected(this) && !Utilities.isNetworkReachable(this)) {
                        mostrarDialogo("Error", "Por favor verifique su conexión a Internet");
                    } else if (!tieneFacturasPendientes()) {
                        new EnviarPedidos().execute();
                    }
                    break;
                case "No ventas":
                    i = new Intent(this, ActivityNoVentas.class);
                    break;
                case "Notas Crédito":
                    i = new Intent(this, ActivityNotasCredito.class);
                    break;
            }

            if (i != null)
                startActivity(i);
        }
    }

    private void mostrarDialogo(String titulo, String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(titulo);
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    private boolean tieneFacturasPendientes() {
        FacturaDAL facturaDAL = new FacturaDAL(App.actualActivity);
        ArrayList<FacturaDTO> listaPendiente = facturaDAL.obtenerListadoPendientes();
        RemisionDAL remisionDAL = new RemisionDAL(App.actualActivity);
        ArrayList<RemisionDTO> listaPendiente2 = remisionDAL.obtenerListadoPendientes();

        if (listaPendiente.size() > 0) {
            int cantidadPendiente = listaPendiente.size();
            StringBuilder sb = new StringBuilder();
            for (FacturaDTO factura : listaPendiente) {
                sb.append(factura.NumeroFactura).append(", ");
            }

            String mensaje = ("Tiene " + cantidadPendiente +
                    (cantidadPendiente > 1 ? (" facturas (" + sb.toString() + ") pendientes ") :
                            (" factura (" + sb.toString() + ") pendiente ")) + "por descargar." +
                    "\nProceda a descargar las facturas y remisiones pendientes antes de enviar los pedidos");
            mostrarDialogo("Error", mensaje);
            return true;
        } else {
            if (listaPendiente2.size() > 0) {
                int cantidadPendiente = listaPendiente2.size();
                StringBuilder sb = new StringBuilder();
                for (RemisionDTO remision : listaPendiente2) {
                    sb.append(remision.NumeroRemision).append(", ");
                }
                String mensaje = ("Tiene " + cantidadPendiente +
                        (cantidadPendiente > 1 ? (" remisiones (" + sb.toString() + ") pendientes ") :
                                (" remisión (" + sb.toString() + ") pendiente ")) + "por descargar." +
                        "\nProceda a descargar las facturas y remisiones pendientes antes de enviar los pedidos");
                mostrarDialogo("Error", mensaje);
                return true;
            } else {
                return false;
            }
        }
    }


    private class EnviarPedidos extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressBar = new CustomProgressBar();
            progressBar.show(getApplicationContext(), "Enviando pedidos realizados");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.getDialog().dismiss();

            if (s != null && !TextUtils.isEmpty(s)) {
                if (s.equals(Utilities.IMEI_ERROR)) {
                    try {
                        App.guardarConfiguracionEstadoAplicacion("B", getApplicationContext());
                        mostrarDialogo(s, "configure nuevamente la ruta");
                    } catch (Exception ex) {
                        logCaughtException(ex);
                        mostrarDialogo(s, ex.getMessage());
                    }
                } else {
                    mostrarDialogo("Error", s);
                }
            } else {
                mostrarDialogo("TiMovil", "Se han enviado correctamente los pedidos del día");
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String error = null;
            try {
                NetWorkHelper netWorkHelper = new NetWorkHelper();
                if (resolucion != null) {
                    String installationId = App.actualActivity.getInstallationId();
                    String url = SincroHelper.getEnviarPedidosURL(resolucion.CodigoRuta, resolucion.IdCliente, installationId);
                    String jsonRespuesta = netWorkHelper.readService(url);
                    jsonRespuesta = SincroHelper.procesarOkJson(jsonRespuesta);
                    if (!jsonRespuesta.equals("OK")) {
                        error = jsonRespuesta;
                    }
                } else {
                    error = "Debe configurar nuevamente la ruta";
                }
            } catch (Exception e) {
                logCaughtException(e);
                error = e.getMessage();
            }
            return error;
        }
    }


}