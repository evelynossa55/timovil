package com.cm.timovil2.front;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.printer_v2.Printer;
import com.cm.timovil2.bl.printers.printer_v2.PrinterFactory;
import com.cm.timovil2.data.ResolucionDAL;

import java.util.ArrayList;
import java.util.Set;

public class ActivityConfigImpresora extends ActivityBase implements OnClickListener {

    private ArrayList<BluetoothDevice> dispositivos = null;
    private Dispositivo dispositivoActual = null;
    private ListView lsvDispositivos;
    private BluetoothAdapter btAdapter;

    // Cuando se abre la activity, se lista la impresora que esta guardada
    private boolean impresoraGuardada = true;

    private boolean estadoBuscando;

    private View statusView;
    private View configView;
    private TextView lblProgresoBusqueda;
    private Spinner spTiposImpresora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracion_impresora);
        if (App.obtenerConfiguracion_imprimir(this)) {
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
            setControls();
        } else {
            mostrarErrorYsalir(getResources().getString(R.string.lbl_no_imprimir));
        }
    }

    @Override
    protected void setControls() {
        App.actualActivity = this;
        estadoBuscando = false;
        dispositivos = new ArrayList<>();
        dispositivoActual = null;
        lsvDispositivos = findViewById(R.id.lsvDispositivos);
        spTiposImpresora = findViewById(R.id.spTipoImpresora);
        listarImpresoraGuardada();

        lsvDispositivos.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                if (impresoraGuardada) { // Solo estoy listando la impresora guardada previamente
                    dispositivoActual = new Dispositivo(resolucion.NombreImpresora, resolucion.MACImpresora);
                } else if (dispositivos.size() > 0 && position <= dispositivos.size()) {
                    dispositivoActual = new Dispositivo(
                            dispositivos.get(position).getName(),
                            dispositivos.get(position).getAddress()
                    );
                }
            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        statusView = findViewById(R.id.search_status);
        configView = findViewById(R.id.config_impresora_form);
        lblProgresoBusqueda = findViewById(R.id.lblDetalleProgreso);
        Button btnCancelarBusqueda = findViewById(R.id.btnCancelarBusqueda);
        btnCancelarBusqueda.setOnClickListener(this);
    }

    private void mostrarErrorYsalir(String mensaje) {
        AlertDialog.Builder d = new AlertDialog.Builder(ActivityConfigImpresora.this);
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
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btnCancelarBusqueda) {
            buscarDispositivos();
        }
    }

    private static final int REQUEST_ENABLE_BT = 127;

    private void buscarDispositivos() {
        try {
            lblProgresoBusqueda.setText("");
            if (estadoBuscando) {// Voy a cancelar la busqueda
                if (btAdapter.isDiscovering()) {// Voy a detener la busquda
                    if (btAdapter.cancelDiscovery()) {
                        Log.i("ConfigImpresora", "Cancel Discovery call");
                    }
                }
                cerrarConexiones();
                estadoInicial();
                estadoBuscando = false;
                return;
            }
            if (btAdapter == null) {
                makeErrorDialog("Usted no tiene bluetooth!!!", ActivityConfigImpresora.this);
                return;
            }

            showProgress(true);
            dispositivos.clear();
            boolean enable = true;
            if (!btAdapter.isEnabled()) {
                enable = btAdapter.enable();
            }
            if (enable) {
                IntentFilter filtro = new IntentFilter(
                        BluetoothDevice.ACTION_FOUND);
                filtro.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(mReceiver, filtro);
                if (btAdapter.startDiscovery()) {
                    impresoraGuardada = false;
                    dispositivoActual = null;
                    estadoBusqueda();
                    estadoBuscando = true;
                }
            } else {
                estadoInicial();
                estadoBuscando = false;
            }
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog(e.getMessage(), ActivityConfigImpresora.this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                makeErrorDialog("No se pudo habilitar el Bluetooth", ActivityConfigImpresora.this);
            } else {
                estadoBuscando = false;
                buscarDispositivos();
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    dispositivos.add(device);
                    String progresoBusqueda = device.getName() + " " + device.getAddress() + " [" + dispositivos.size() + "]";
                    lblProgresoBusqueda.setText(progresoBusqueda);
                    mostrarDispositivos();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                cerrarConexiones();
                estadoInicial();
            }
        }
    };

    private void estadoBusqueda() {
        showProgress(true);
    }

    private void estadoInicial() {
        showProgress(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cerrarConexiones();
    }

    private void cerrarConexiones() {
        try {
            btAdapter.cancelDiscovery();
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            logCaughtException(e);
            e.printStackTrace();
        }
    }

    private void listarImpresoraGuardada() {
        try {

            ArrayList<String> l = new ArrayList<>();
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    String deviceMajorClass
                            = getBTMajorDeviceClass(device
                            .getBluetoothClass()
                            .getMajorDeviceClass());
                    l.add(deviceName + ": " + deviceMajorClass + "\n" + deviceAddress);
                }
            }

            if (!TextUtils.isEmpty(resolucion.MACImpresora) && !TextUtils.isEmpty(resolucion.NombreImpresora)) {
                l.add(resolucion.NombreImpresora + ": CONFIGURADA" + "\n" + resolucion.MACImpresora);
            } else {
                impresoraGuardada = false;
            }

            llenarListView(l);

        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog("Error listando los dispositivos: " + e.getMessage(), ActivityConfigImpresora.this);
        }
    }

    private void llenarListView(ArrayList<String> l) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice,
                android.R.id.text1, l);
        lsvDispositivos.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lsvDispositivos.setAdapter(adapter);
    }

    private void mostrarDispositivos() {
        try {
            ArrayList<String> l = new ArrayList<>();
            for (BluetoothDevice device : dispositivos) {
                String deviceMajorClass
                        = getBTMajorDeviceClass(device
                        .getBluetoothClass()
                        .getMajorDeviceClass());
                l.add(device.getName() + ": " + deviceMajorClass + "\n" + device.getAddress());
            }
            llenarListView(l);
        } catch (Exception e) {
            logCaughtException(e);
            makeErrorDialog("Error listando los dispositivos: " + e.getMessage(), ActivityConfigImpresora.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config_impresora, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuBuscarImpresora) {
            buscarDispositivos();
        } else if (item.getItemId() == R.id.mnuGuardarImpresora) {
            try {
                if (dispositivoActual != null) {
                    resolucion.MACImpresora = dispositivoActual.direccion;
                    resolucion.NombreImpresora = dispositivoActual.nombre;
                    Object objTipoImpresora = spTiposImpresora.getSelectedItem();
                    resolucion.TipoImpresora = (objTipoImpresora != null ? objTipoImpresora.toString() : "GENERICA");

                    App.guardarConfiguracionImpresora(resolucion, this);
                    new ResolucionDAL(this).Insertar(resolucion);

                    Printer printer = PrinterFactory.getPrinter(resolucion.MACImpresora, this, 0);
                    printer.printConfig();

                }
            } catch (Exception e) {
                logCaughtException(e);
                makeErrorDialog(e.getMessage(), ActivityConfigImpresora.this);
            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        statusView.setVisibility(View.VISIBLE);
        statusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        statusView.setVisibility(show ? View.VISIBLE
                                : View.GONE);
                    }
                });

        configView.setVisibility(View.VISIBLE);
        configView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        configView.setVisibility(show ? View.GONE
                                : View.VISIBLE);
                    }
                });

    }

    private class Dispositivo {
        Dispositivo(String nombre, String direccion) {
            this.nombre = nombre;
            this.direccion = direccion;
        }

        final String nombre;
        final String direccion;
    }

    private String getBTMajorDeviceClass(int major) {
        switch (major) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO";
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTADOR";
            case BluetoothClass.Device.Major.HEALTH:
                return "SALUD";
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGENES";
            case BluetoothClass.Device.Major.MISC:
                return "MISCELANEO";
            case BluetoothClass.Device.Major.NETWORKING:
                return "INTERNET";
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIFERICO";
            case BluetoothClass.Device.Major.PHONE:
                return "TELEFONO";
            case BluetoothClass.Device.Major.TOY:
                return "JUGUETE";
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "SIN CATEGORIA";
            case BluetoothClass.Device.Major.WEARABLE:
                return "AUDIO_VIDEO";
            default:
                return "DESCONOCIDO";
        }
    }
}