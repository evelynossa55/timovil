package com.cm.timovil2.proxy;

import android.util.Log;

import java.util.Vector;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.AndroidHttpTransport;

import com.cm.timovil2.dto.wsentities.MFact7;
import com.cm.timovil2.dto.wsentities.MarshalFloat;
import com.cm.timovil2.rest.NetWorkHelper;

abstract class ProxyWS {

	final String NAMESPACE = "http://www.controlmovil.com.co/";
	String METHOD_NAME = null;

	//PRODUCCIÓN
	private final String URL = NetWorkHelper.DIRECCION_SERVICIO + "/MovilServices.asmx";

	SoapObject InvokeMethod() throws Exception {

		SoapObject request = GetRequest();
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		if (this instanceof ProxyFactura) {
			envelope.addMapping(NAMESPACE, "MFact7",MFact7.class);
			envelope.addMapping(NAMESPACE, "MDetFact", Vector.class);
			MarshalFloat md = new MarshalFloat();
			md.register(envelope);
		}

        try{
            AndroidHttpTransport androidHttpTransport = new AndroidHttpTransport(URL);
            androidHttpTransport.call(NAMESPACE + METHOD_NAME, envelope);
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }

		//Log.d("sd", envelope.getResponse().toString());
		return (SoapObject) envelope.getResponse();
	}

	String invokeStringMethod() throws Exception {

		SoapObject request = GetRequest();

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.dotNet = true;
		envelope.setOutputSoapObject(request);

		if (this instanceof ProxyFactura) {
			envelope.addMapping(NAMESPACE, "MFact7", MFact7.class);
			envelope.addMapping(NAMESPACE, "MDetFact", Vector.class);
			MarshalFloat md = new MarshalFloat();
			md.register(envelope);
		}

		if (this instanceof ProxyAbonoFactura) {
			MarshalFloat md = new MarshalFloat();
			md.register(envelope);
		}

        try{
            AndroidHttpTransport androidHttpTransport = new AndroidHttpTransport(URL);
            androidHttpTransport.call(NAMESPACE + METHOD_NAME, envelope);
        }catch (Exception e){
            throw new Exception("No se ha podido establecer conexión con el servidor (ProxyWS)");
        }

		Object o = envelope.getResponse();
		return o.toString();
	}

	abstract SoapObject GetRequest();

	abstract <T> T InvocarWS() throws Exception;

	static class MetodosWeb {
		final static String OBTENER_DATOS_FACTURACION = "GetDatosFacturacion";
		final static String OBTENER_PRODUCTOS = "GetProductos3";
		final static String OBTENER_RUTERO = "GetRutero";
		//final static String OBTENER_INVENTARIO = "GetInventario";
		//final static String OBTENER_DESCUENTOS = "ObtenerDescuentosPorRuta";
		final static String OBTENER_FORMAS_PAGO = "GetFormasPago";
		final static String GUARDAR_FACTURA = "SaveFactura7";
		final static String OBTENER_FACTURAS_PENDIENTES = "GetFacturasPendientes";
		final static String ANULAR_FACTURA = "AnularFactura";
		final static String REGISTRAR_ABONO = "RegistrarAbono";
		final static String OBTENER_CONFIGURACION = "GetConfiguracion";
	}
}