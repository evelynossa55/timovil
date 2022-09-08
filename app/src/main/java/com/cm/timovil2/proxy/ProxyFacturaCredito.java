package com.cm.timovil2.proxy;

import java.util.ArrayList;
import org.ksoap2.serialization.SoapObject;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.data.DetalleFacturaCreditoDAL;
import com.cm.timovil2.data.FacturaCreditoDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MDetalleFactura;
import com.cm.timovil2.dto.wsentities.MFactCredito;

public class ProxyFacturaCredito extends ProxyWS {

	private final ActivityBase contexto;

	public ProxyFacturaCredito(ActivityBase context) {
		METHOD_NAME = MetodosWeb.OBTENER_FACTURAS_PENDIENTES;
		contexto = context;
	}

	@Override
	SoapObject GetRequest(){
		SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
		ResolucionDTO resolucion = new ResolucionDAL(this.contexto).ObtenerResolucion();
		so.addProperty("_codigoRuta", resolucion.CodigoRuta);
		so.addProperty("_idCliente", resolucion.IdCliente);
        //so.addProperty("_codigoRuta", "331");//
        //so.addProperty("_idCliente", "2101");//
		return so;
	}

	@Override
	<T> T InvocarWS() throws Exception {
		SoapObject soap = InvokeMethod();

		FacturaCreditoDAL facturaDal = new FacturaCreditoDAL(contexto);
		DetalleFacturaCreditoDAL detalleDal = new DetalleFacturaCreditoDAL(contexto);

		if (soap != null) {
			new AbonoFacturaDAL(contexto).eliminar();
			detalleDal.Eliminar();
			facturaDal.eliminar();
		}else{
			throw new Exception("Respuesta inválida desde el servidor, soap null");
		}

		ArrayList<MFactCredito> facturas = new ArrayList<>();
		ArrayList<MDetalleFactura> det = new ArrayList<>();

		for (int i = 0; i < soap.getPropertyCount(); i++) {
			SoapObject pii = (SoapObject) soap.getProperty(i);
			String ClienteNegocio = pii.getProperty("RazonSocialCliente").toString();
			String separacionCliente[] = ClienteNegocio.split("\\|");

			MFactCredito factura = new MFactCredito();
			factura.IdFactura = pii.getProperty("IdFactura").toString();
			factura.NumeroFactura = pii.getProperty("NumeroFactura").toString();
			factura.NombreRuta = pii.getProperty("NombreRuta").toString();
			factura.FechaHoraCredito = pii.getProperty("FechaHora").toString();
			factura.FormaPago = pii.getProperty("FormaPago").toString();
			factura.IdCliente = Integer.parseInt(pii.getProperty("IdCliente").toString());
			
			factura.Subtotal = Float.parseFloat(pii.getProperty("Subtotal").toString());
			factura.Descuento = Float.parseFloat(pii.getProperty("Descuento").toString());
			factura.Retefuente = Float.parseFloat(pii.getProperty("Retefuente").toString());

			factura.Iva = Float.parseFloat(pii.getProperty("Iva").toString());
			factura.Total = Float.parseFloat(pii.getProperty("APagar").toString());
			factura.Saldo = Float.parseFloat(pii.getProperty("Saldo").toString());
			factura.Identificacion = pii.getProperty("IdentificacionCliente").toString();
			factura.RazonSocial = separacionCliente[0];
            if(separacionCliente.length>1){
			    factura.Negocio = separacionCliente[1];
            }else{
                factura.Negocio = " ";
            }
			factura.Direccion = pii.getProperty("DireccionCliente").toString();
			factura.Telefono = pii.getProperty("TelefonoCliente").toString();
			
			factura.Resolucion = pii.getProperty("Resolucion").toString();
			
			SoapObject detalles = (SoapObject) pii.getProperty("DetalleFactura");

			facturaDal.Insertar(factura);

			for (int j = 0; j < detalles.getPropertyCount(); j++) {
				SoapObject detalle = (SoapObject) detalles.getProperty(j);
				MDetalleFactura d = new MDetalleFactura();
				d.IdFactura = factura.IdFactura;
				d.IdProducto = Integer.parseInt(detalle.getProperty("IdProducto").toString());
				d.Codigo = detalle.getProperty("Codigo").toString();
				d.Nombre = detalle.getProperty("NombreProducto").toString();
				d.Cantidad = Integer.parseInt(detalle.getProperty("Cantidad").toString());
				d.Devolucion = Integer.parseInt(detalle.getProperty("Devolucion").toString());
				d.Rotacion = Integer.parseInt(detalle.getProperty("Rotacion").toString());
				d.Subtotal = Float.parseFloat(detalle.getProperty("SubTotal").toString());
				d.Descuento = Float.parseFloat(detalle.getProperty("Descuento").toString());
				d.Iva = Float.parseFloat(detalle.getProperty("Iva").toString());
				d.Total = Float.parseFloat(detalle.getProperty("ValorDetalle").toString());

				detalleDal.insertar(d);
				det.add(d);
			}
            factura.DetalleFactura_Credito = det;
            facturas.add(factura);
		}

		return (T) facturas;
	}

	public ArrayList<MFactCredito> CargarFacturasPendientes() throws Exception {
		return InvocarWS();
	}
}
