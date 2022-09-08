package com.cm.timovil2.sincro;

import com.cm.timovil2.data.EntregadorDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.EntregadorDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 5/06/18.
 */

public class SincroEntregador implements ISincroData {

    private static SincroEntregador instance = null;
    private int cantidadCargada;

    public static SincroEntregador getInstance() {
        if(instance == null) {
            instance = new SincroEntregador();
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
            String url = SincroHelper.getRutaEntregadorURL(resolucionDTO.CodigoRuta, resolucionDTO.IdCliente);
            String jsonRespuesta = netWorkHelper.readService(url);
            ArrayList<EntregadorDTO> listaEntregadores = SincroHelper.procesarJsonEntregador(jsonRespuesta);
            EntregadorDAL dal = new EntregadorDAL(context);
            dal.Eliminar();
            dal.Insertar(listaEntregadores);
            cantidadCargada = listaEntregadores.size();
        }

    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
