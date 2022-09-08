package com.cm.timovil2.sincro;

import com.cm.timovil2.data.FormaPagoDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.FormaPagoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.proxy.ProxyFormaPago;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/06/18.
 */

public class SincroFormaPago implements ISincroData {

    private static SincroFormaPago instance = null;
    private int cantidadCargada;

    public static SincroFormaPago getInstance() {
        if (instance == null) {
            instance = new SincroFormaPago();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();
        String tipoRuta = resolucionDTO.TipoRuta;

        if (tipoRuta.equals("Vendedor")) {

            FormaPagoDAL fpDal = new FormaPagoDAL(context);
            ArrayList<FormaPagoDTO> formasDePago = new ProxyFormaPago()
                    .ObtenerFormasDePago(resolucionDTO.IdCliente);

            fpDal.Eliminar();
            cantidadCargada = 0;
            for (int i = 0; i < formasDePago.size(); i++) {
                fpDal.Insertar(formasDePago.get(i));
                cantidadCargada++;
            }
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
