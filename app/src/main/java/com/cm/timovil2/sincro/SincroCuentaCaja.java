package com.cm.timovil2.sincro;

import com.cm.timovil2.data.CuentaCajaDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.CuentaCajaDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 5/06/18.
 */

public class SincroCuentaCaja implements ISincroData {

    private static SincroCuentaCaja instance = null;
    private int cantidadCargada;

    public static SincroCuentaCaja getInstance() {
        if (instance == null) {
            instance = new SincroCuentaCaja();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();
        String tipoRuta = resolucionDTO.TipoRuta;

        if (tipoRuta.equals("Vendedor")) {
            ArrayList<CuentaCajaDTO> cuentas;
            String peticion = SincroHelper.ObtenerCuentasCajaURL(resolucionDTO.CodigoRuta,
                    resolucionDTO.IdCliente, context.getInstallationId());
            String resultado = new NetWorkHelper().readService(peticion);
            cuentas = SincroHelper.procesarJsonCuentasCaja(resultado);

            new CuentaCajaDAL(context).Insertar(cuentas);
            cantidadCargada = cuentas.size();
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
