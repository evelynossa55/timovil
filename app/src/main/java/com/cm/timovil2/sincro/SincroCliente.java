package com.cm.timovil2.sincro;

import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.ProgramacionAsesorDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.proxy.ProxyCliente;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/06/18.
 */

public class SincroCliente implements ISincroData{

    private static SincroCliente instance = null;
    private int cantidadCargada;

    public static SincroCliente getInstance() {
        if(instance == null) {
            instance = new SincroCliente();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ProxyCliente proxy = new ProxyCliente(context);
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionDTO = resolucionDAL.ObtenerResolucion();
        NetWorkHelper netWorkHelper = new NetWorkHelper();

        String tipoRuta = resolucionDTO.TipoRuta;
        ArrayList<ClienteDTO> l;

        if (tipoRuta.equals("Vendedor")) {
            l = proxy.ObtenerClientes();
            cantidadCargada = l.size();
        }else{
            String url = SincroHelper.ObtenerRuteroAsesorURL(resolucionDTO.IdCliente, resolucionDTO.CodigoRuta);
            String ruteroAsesor = netWorkHelper.readService(url);
                    ArrayList<ClienteDTO> clientes = SincroHelper.procesarJsonAsesor(ruteroAsesor);
            cantidadCargada = clientes.size();

            ClienteDAL cdal = new ClienteDAL(context);
            ProgramacionAsesorDAL pdal = new ProgramacionAsesorDAL(context);

            cdal.Eliminar();
            pdal.eliminarTodo();

            for (int i = 0; i < clientes.size(); i++) {
                ClienteDTO c = clientes.get(i);
                cdal.Insertar(c);
                if (c.programacionAsesor != null && c.programacionAsesor.size() > 0) {
                    pdal.Insertar(c.programacionAsesor);
                }
            }
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
