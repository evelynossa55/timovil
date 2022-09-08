package com.cm.timovil2.proxy;

import java.util.ArrayList;

import org.ksoap2.serialization.SoapObject;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;

public class ProxyProducto extends ProxyWS {

    private String idCliente;
    private String codigoRuta;
    private String codigoBodega;

	public ProxyProducto() {
		METHOD_NAME = MetodosWeb.OBTENER_PRODUCTOS;
	}

	
	@Override
	SoapObject GetRequest() {
		SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
		so.addProperty("_idCliente", idCliente);
		so.addProperty("_codigoRuta", codigoRuta);
		return so;
	}
	
	@Override
	<T> T InvocarWS() throws Exception{
		SoapObject soap = InvokeMethod();
		ArrayList<ProductoDTO> productos = new ArrayList<>();
        for (int i = 0; i < soap.getPropertyCount(); i++) {
            SoapObject pii = (SoapObject)soap.getProperty(i);
            ProductoDTO producto = new ProductoDTO();
            producto.IdProducto = Integer.parseInt(pii.getProperty(0).toString());
            producto.Codigo = pii.getProperty(1).toString();
            producto.Nombre = pii.getProperty(2).toString();
            producto.Precio1 = Float.parseFloat(pii.getProperty(3).toString());
            producto.Precio2 = Float.parseFloat(pii.getProperty(4).toString());
            producto.Precio3 = Float.parseFloat(pii.getProperty(5).toString());
            producto.Precio4 = Float.parseFloat(pii.getProperty("Precio4").toString());
            producto.Precio5 = Float.parseFloat(pii.getProperty("Precio5").toString());
            producto.Precio6 = Float.parseFloat(pii.getProperty("Precio6").toString());
            producto.Precio7 = Float.parseFloat(pii.getProperty("Precio7").toString());
            producto.Precio8 = Float.parseFloat(pii.getProperty("Precio8").toString());
            producto.Precio9 = Float.parseFloat(pii.getProperty("Precio9").toString());
            producto.Precio10 = Float.parseFloat(pii.getProperty("Precio10").toString());
            producto.Precio11 = Float.parseFloat(pii.getProperty("Precio11").toString());
            producto.Precio12 = Float.parseFloat(pii.getProperty("Precio12").toString());

            producto.PorcentajeIva = Float.parseFloat(pii.getProperty(6).toString());
            producto.StockInicial = Integer.parseInt(pii.getProperty(7).toString());
            producto.IpoConsumo = Float.parseFloat(pii.getProperty("IpoConsumo").toString());
            producto.CodigoBodega = codigoBodega;
            productos.add(producto);
        }
        return (T) productos;
	}
	
	public ArrayList<ProductoDTO> CargarProductos(ResolucionDTO resolucionDTO)
            throws Exception	{
        idCliente = resolucionDTO.IdCliente;
        codigoRuta = resolucionDTO.CodigoRuta;
        codigoBodega = resolucionDTO.Bodegas.split(":")[0];
		return InvocarWS();
	}

}