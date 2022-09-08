package com.cm.timovil2.proxy;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MDetFact;
import com.cm.timovil2.dto.wsentities.MFact7;

public class ProxyFactura extends ProxyWS {

	private MFact7 factura;
	private final ResolucionDTO resolucion;

	private  enum Accion {
		DescargaFactura, AnularFactura
	}

	private final Accion accion;

	public ProxyFactura(Accion _accion, ResolucionDTO _resolucion) {

		accion = _accion;
        resolucion = _resolucion;

		if (accion == Accion.DescargaFactura) {
			METHOD_NAME = MetodosWeb.GUARDAR_FACTURA;
		} else {
			METHOD_NAME = MetodosWeb.ANULAR_FACTURA;
		}
	}

	@Override
	SoapObject GetRequest() {
		SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);

		if (accion == Accion.DescargaFactura) {

			SoapObject detalles = new SoapObject(NAMESPACE, "Detalle");
			for (MDetFact oDetalle : factura.Detalle) {
				SoapObject dt = new SoapObject(NAMESPACE, "MDetFact");
				dt.addProperty("IdProd", oDetalle.IdProd);
				dt.addProperty("Cant", oDetalle.Cant);
				dt.addProperty("Dev", oDetalle.Dev);
				dt.addProperty("Rot", oDetalle.Rot);
				dt.addProperty("VUnit", oDetalle.VUnit);
				dt.addProperty("PDesc", oDetalle.PDesc);
				dt.addProperty("PIva", oDetalle.PIva);
				detalles.addProperty("MDetFact", dt);
			}
			SoapObject oFactura = new SoapObject(NAMESPACE, "MFact7");

			oFactura.addProperty("Num", factura.Num);
			oFactura.addProperty("CRuta", factura.CRuta);
			oFactura.addProperty("IdCli", factura.IdCli);
			oFactura.addProperty("PRet", factura.PRet);
			oFactura.addProperty("PIca", factura.PIca);
			oFactura.addProperty("Fecha", factura.Fecha);
			oFactura.addProperty("FP", factura.FP);
			oFactura.addProperty("Anul", factura.Anul);
			oFactura.addProperty("IdRes", resolucion.IdResolucion);
			oFactura.addProperty("Detalle", detalles);
			oFactura.addProperty("Efect", factura.Efect);
			oFactura.addProperty("Devol", factura.Devol);
			oFactura.addProperty("Latitud", factura.Latitud);
			oFactura.addProperty("Longitud", factura.Longitud);
            oFactura.addProperty("Comentario", factura.Comentario);
            oFactura.addProperty("TipoDoc", factura.TipoDoc);
            oFactura.addProperty("CodigoBodega", factura.CodigoBodega);
            oFactura.addProperty("NroPedido", factura.NroPedido);
            oFactura.addProperty("ComentAnu", factura.ComentAnu);
            oFactura.addProperty("IdEmpEntre", factura.IdEmpEntre);

			PropertyInfo pi = new PropertyInfo();
			pi.setName("factura");
			pi.setValue(oFactura);
			pi.setType(oFactura.getClass());

			so.addProperty(pi);
			so.addProperty("_idCliente", resolucion.IdCliente);
		} else {
			so.addProperty("_idCliente", resolucion.IdCliente);
			so.addProperty("_usuario", resolucion.NombreRuta);
			so.addProperty("_numeroFactura", factura.Num);
		}
		return so;
	}	

	@Override
	<T> T InvocarWS() throws Exception {
		String respuesta = invokeStringMethod();
		return (T) respuesta;
	}

	public String descargarFactura(MFact7 _factura) throws Exception {
		factura = _factura;
		return InvocarWS();
	}

	public String anularFactura(String _numeroFactura) throws Exception {
		factura = new MFact7();
		factura.Num=_numeroFactura;
		return InvocarWS();
	}
}