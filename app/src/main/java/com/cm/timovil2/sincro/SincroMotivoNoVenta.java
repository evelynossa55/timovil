package com.cm.timovil2.sincro;

import com.cm.timovil2.data.MotivoNoVentaDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.MotivoNoVentaDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 5/06/18.
 */

public class SincroMotivoNoVenta implements ISincroData {

    private static SincroMotivoNoVenta instance = null;
    private int cantidadCargada;

    public static SincroMotivoNoVenta getInstance() {
        if(instance == null) {
            instance = new SincroMotivoNoVenta();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();

        String tipoRuta = resolucionDTO.TipoRuta;
        if (tipoRuta.equals("Vendedor")) {
            NetWorkHelper netWorkHelper = new NetWorkHelper();
            String url = SincroHelper.getMotivosNoVentaURL(resolucionDTO.IdCliente, resolucionDTO.CodigoRuta);
            String jsonRespuesta = netWorkHelper.readService(url);
            ArrayList<MotivoNoVentaDTO> listaMotivos = SincroHelper.procesarJsonMotivosNoVenta(jsonRespuesta);
            MotivoNoVentaDAL motivoDal = new MotivoNoVentaDAL(context);
            motivoDal.eliminarTodo();
            motivoDal.Insertar(listaMotivos);
            cantidadCargada  = listaMotivos.size();
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
