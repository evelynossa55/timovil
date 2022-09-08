package com.cm.timovil2.proxy;

import java.util.ArrayList;
import org.ksoap2.serialization.SoapObject;

import com.cm.timovil2.dto.FormaPagoDTO;

public class ProxyFormaPago extends ProxyWS {

	private String idCliente;

	public ProxyFormaPago() {
		METHOD_NAME = MetodosWeb.OBTENER_FORMAS_PAGO;
	}

	@Override
	SoapObject GetRequest() {
		SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
		so.addProperty("_idCliente", this.idCliente);
		return so;
	}

	@Override
	<T> T InvocarWS() throws Exception {

		SoapObject listadoDescuentos = InvokeMethod();
		ArrayList<FormaPagoDTO> l = new ArrayList<>();

		for (int i = 0; i < listadoDescuentos.getPropertyCount(); i++) {
			SoapObject soap = (SoapObject) listadoDescuentos.getProperty(i);
			FormaPagoDTO f = new FormaPagoDTO();
			f.Codigo = soap.getProperty(0).toString();
			f.Nombre = soap.getProperty(1).toString();
			l.add(f);
		}
		return (T) l;
	}

	public ArrayList<FormaPagoDTO> ObtenerFormasDePago(String idCliente) throws Exception {
        this.idCliente = idCliente;
        return InvocarWS();
	}

}