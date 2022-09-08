package com.cm.timovil2.front;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.adapters.AdapterEntry;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemEntry;
import com.cm.timovil2.bl.utilities.ItemSection;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ActivityCartera extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cartera_activity);
        Bundle bundle = getIntent().getExtras();
        Locale local = new Locale("EN", "US");
        NumberFormat numberFormat;

        if (bundle != null) {
            String carteras = bundle.getString("cartera");
            String remisiones = bundle.getString("remisiones");
            String valorVentasMes = bundle.getString("ventasMes");
            String valorVentasMesPorRuta = bundle.getString("ventasMesRuta");
            if (carteras != null && remisiones != null && valorVentasMes != null) {
                String[] listCartera = carteras.split("\n");
                String[] listRemisiones = remisiones.split("\n");

                ArrayList<Item> carteraItems = new ArrayList<>();
                double totalPrecioCartera = formatearCartera(listCartera, carteraItems);

                ArrayList<Item> remisionesItems = new ArrayList<>();
                int totalCantidadRemisiones = formatearRemisiones(listRemisiones, remisionesItems);

                ListView lsvCartera = findViewById(R.id.ListCartera);
                ListView lsvRemisiones = findViewById(R.id.ListRemisiones);
                TextView totalCarteraView = findViewById(R.id.item_cartera_total);
                TextView totalRemisionView = findViewById(R.id.item_remisiones_total);
                TextView txtVentasMes = findViewById(R.id.txt_ventas_mes);
                TextView txtVentasMesRuta = findViewById(R.id.txt_ventas_mes_ruta);

                numberFormat = NumberFormat.getCurrencyInstance(local);
                String strTotalPrecioCartera = "TOTAL: " + numberFormat.format(totalPrecioCartera);
                String strTotalCantidadRemisiones = "TOTAL: " + totalCantidadRemisiones + " Botellas";
                totalCarteraView.setText(strTotalPrecioCartera);
                totalRemisionView.setText(strTotalCantidadRemisiones);

                String[] aux = valorVentasMes.split("\\$");

                if(aux.length == 2){
                    double c = Double.parseDouble(aux[1].replace(",", ""));
                    DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
                    valorVentasMes = "VENTAS POR: $" + decimalFormat.format(c);
                }

                txtVentasMes.setText(valorVentasMes);
                txtVentasMesRuta.setText(valorVentasMesPorRuta);

                AdapterEntry carteraAdapter = new AdapterEntry(this, carteraItems);
                AdapterEntry remisionesAdapter = new AdapterEntry(this, remisionesItems);

                lsvCartera.setAdapter(carteraAdapter);
                lsvRemisiones.setAdapter(remisionesAdapter);
            }
        }
        setControls();
    }

    @Override
    protected void setControls() {

        TabHost tabs = findViewById(android.R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("Cartera");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Cartera");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Remisiones");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Remisiones");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Ventas");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Ventas mes");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.i("AndroidTabsDemo", "Pulsada pestaña: " + tabId);
            }
        });
    }

    private double formatearCartera(String[] listCartera, ArrayList<Item> carteraItems) {

        double totalPrecioCartera = 0;
        try{
            ResolucionDTO resolucionDTO = new ResolucionDAL(this).ObtenerResolucion();

            double precioCartera;
            if (listCartera != null && listCartera.length > 0 && carteraItems != null) {
                carteraItems.clear();
                for (String cartera : listCartera) {
                    if (cartera.contains("Factura")) {
                        carteraItems.add(new ItemSection(cartera));
                    } else if (!cartera.contains("Factura")) {
                        int index = cartera.lastIndexOf(":");
                        String texto = cartera.substring(0, (index + 1));
                        String valor = cartera.substring((index + 1));
                        carteraItems.add(new ItemEntry(texto, valor));

                        if (texto.contains("Saldo")) {
                            try {
                                if(resolucionDTO.IdCliente.equals(Utilities.ID_MATERIALES_Y_HERRAMIENTAS)){
                                    precioCartera = Double.parseDouble(valor.trim().replace(".", "").replace(",", "."));
                                }else{
                                    precioCartera = Double.parseDouble(valor.trim().replace(",", ""));//.replace(",", "."));
                                }
                            } catch (NumberFormatException ex) {
                                logCaughtException(ex);
                                precioCartera = 0;
                            }
                            totalPrecioCartera += precioCartera;
                        }
                    }
                }
            }
        }catch (Exception ex){
            logCaughtException(ex);
            makeErrorDialog(ex.getMessage(), this);
        }

        return totalPrecioCartera;
    }

    private int formatearRemisiones(String[] listRemisiones, ArrayList<Item> remisionesItems) {
        int cantidadRemisiones;
        int totalCantidadRemisiones = 0;
        if (listRemisiones != null && listRemisiones.length > 0 && remisionesItems != null) {
            for (String remision : listRemisiones) {
                if (remision.contains("Remisión")) {
                    remisionesItems.add(new ItemSection(remision));
                } else if (!remision.contains("Remisión")) {
                    int index = remision.lastIndexOf(":");
                    String texto = remision.substring(0, (index + 1));
                    String valor = remision.substring((index + 1));
                    remisionesItems.add(new ItemEntry(texto, valor));
                    try {
                        cantidadRemisiones = Integer.parseInt(valor.trim());
                    } catch (NumberFormatException ex) {
                        logCaughtException(ex);
                        cantidadRemisiones = 0;
                    }
                    totalCantidadRemisiones += cantidadRemisiones;
                }
            }
        }
        return totalCantidadRemisiones;
    }
}
