package com.cm.timovil2.bl.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.FacturasDescargadasCallback;
import com.cm.timovil2.bl.utilities.FacturasDescargadasListener;

public class ReceiverSincrofacturas extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        FacturasDescargadasListener facturasDescargadasListener;
        boolean isFacturasActvityVisible = App.isFacturasActivityVisible;
        int cantidadDescargada = intent.getIntExtra("cantidadDescargada", 0);

        if (isFacturasActvityVisible && cantidadDescargada > 0) {
            if (App.actualActivity != null) {
                try {
                    facturasDescargadasListener = new
                            FacturasDescargadasListener((FacturasDescargadasCallback) App.actualActivity);
                    facturasDescargadasListener.actualizarFacturasUI();
                    App.actualActivity.makeNotification("TiMovil", "Se ha descargado " + cantidadDescargada
                            + (cantidadDescargada>1?" facturas pendientes.": " factura pendiente."), false);
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (cantidadDescargada > 0) {
            if (App.actualActivity != null) {
                App.actualActivity.makeNotification("TiMovil", "Se ha descargado " + cantidadDescargada
                + (cantidadDescargada>1?" facturas pendientes.": " factura pendiente."), false);
            }
        }
    }
}
