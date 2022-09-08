package com.cm.timovil2.sincro;

import com.cm.timovil2.data.DetalleListaPreciosDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MDetalleListaPrecios;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/06/18.
 */

public class SincroListaPrecios implements ISincroData {

    private static SincroListaPrecios instance = null;
    private int cantidadCargada;

    public static SincroListaPrecios getInstance() {
        if (instance == null) {
            instance = new SincroListaPrecios();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();
        NetWorkHelper netWorkHelper = new NetWorkHelper();
        String tipoRuta = resolucionDTO.TipoRuta;

        if (tipoRuta.equals("Vendedor")) {
            String imei = context.getInstallationId();
            String url = SincroHelper.getListasPreciosURL(resolucionDTO.IdCliente, resolucionDTO.CodigoRuta, imei);
            String jsonRespuesta = netWorkHelper.readService(url);
            ArrayList<MDetalleListaPrecios> listaPrecios = SincroHelper.procesarJsonListaPrecios(jsonRespuesta);
            DetalleListaPreciosDAL dal = new DetalleListaPreciosDAL(context);
            dal.Eliminar();
            dal.Insert(listaPrecios);
            cantidadCargada = listaPrecios.size();
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
