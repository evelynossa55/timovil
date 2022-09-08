package com.cm.timovil2.sincro;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MFactCredito;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.proxy.ProxyFacturaCredito;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/06/18.
 */

public class SincroCredito implements ISincroData {

    private static SincroCredito instance = null;
    private int cantidadCargada;

    public static SincroCredito getInstance() {
        if (instance == null) {
            instance = new SincroCredito();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();
        String tipoRuta = resolucionDTO.TipoRuta;
        boolean manejarRecuadoCredito = App.obtenerConfiguracion_ManejarRecaudoCredito(context);

        if (tipoRuta.equals("Vendedor") && manejarRecuadoCredito) {
            ProxyFacturaCredito proxyFacturaCredito = new ProxyFacturaCredito(context);
            ArrayList<MFactCredito> creditos = proxyFacturaCredito.CargarFacturasPendientes();
            cantidadCargada = creditos.size();
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
