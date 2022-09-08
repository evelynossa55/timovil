package com.cm.timovil2.sincro;

import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.proxy.ProxyProducto;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 5/06/18.
 */

public class SincroProducto implements ISincroData {

    private static SincroProducto instance = null;
    private int cantidadCargada;

    public static SincroProducto getInstance() {
        if(instance == null) {
            instance = new SincroProducto();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception {
        ProxyProducto proxy = new ProxyProducto();
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        ResolucionDTO resolucionActualDTO = resolucionDAL.ObtenerResolucion();

        String tipoRuta = resolucionActualDTO.TipoRuta;
        if (tipoRuta.equals("Vendedor")) {

            ArrayList<ProductoDTO> l;
            if ((resolucionActualDTO.ManejarInventario
                    || resolucionActualDTO.ManejarInventarioRemisiones)
                    && resolucionActualDTO.Bodegas.split("-").length > 1) {

                String[] bodegas = resolucionActualDTO.Bodegas.split("-");
                l = new ArrayList<>();
                for (String bodega : bodegas) {
                    String[] detalleBodega = bodega.split(":");
                    NetWorkHelper netWorkHelper = new NetWorkHelper();
                    String url = SincroHelper.getInventarioBodega(resolucionActualDTO.IdCliente, detalleBodega[0]);
                    String jsonRespuesta = netWorkHelper.readService(url);
                    ArrayList<ProductoDTO> tmp = SincroHelper.procesarJsonInventario(jsonRespuesta, detalleBodega[0]);
                    if (tmp != null && !tmp.isEmpty()) {
                        l.addAll(tmp);
                    }
                }

            } else {
                l = proxy.CargarProductos(resolucionActualDTO);
            }

            if (l != null && !l.isEmpty()) {

                ProductoDAL dal = new ProductoDAL(context);
                dal.Eliminar();
                int cantidad = l.size();
                cantidadCargada = 0;
                for (int i = 0; i < cantidad; i++) {
                    ProductoDTO prod = l.get(i);
                    dal.Insertar(prod);
                    cantidadCargada++;
                }

            }
        }
    }

    public int getCantidadCargada() {
        return cantidadCargada;
    }
}
