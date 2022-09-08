package com.cm.timovil2.proxy;

import org.ksoap2.serialization.SoapObject;

import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MAbono;

class ProxyAbonoFactura extends ProxyWS {

	private String idCliente;
	private String codigoRuta;
	private MAbono abono;

	public ProxyAbonoFactura() {
		METHOD_NAME = MetodosWeb.REGISTRAR_ABONO;
	}

	@Override
	SoapObject GetRequest() {
		SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);

		so.addProperty("_codigoRuta", codigoRuta);
		so.addProperty("_idCliente", idCliente);
		so.addProperty("_numeroFactura", abono.NumeroFactura);
		so.addProperty("_valor", abono.Valor);
		so.addProperty("_fecha", abono.Fecha);
		so.addProperty("_saldo", abono.Saldo);
		so.addProperty("_identificador", abono.Identificador);
		so.addProperty("_idFactura", abono.IdFactura);

		return so;
	}

	@Override
	<T> T InvocarWS() throws Exception {

		String respuesta = invokeStringMethod();

		return (T) respuesta;
	}

	public String descargarAbono(MAbono abono, ResolucionDTO resolucionDTO) throws Exception {
        idCliente = resolucionDTO.IdCliente;
        codigoRuta = resolucionDTO.CodigoRuta;
		this.abono = abono;
		return InvocarWS();
	}
}