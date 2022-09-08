package com.cm.timovil2.bl.calculus;

import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;

import java.util.ArrayList;

public class CalculatorFactura extends Calculator {

    CalculatorFactura(ResolucionDTO resolucionDTO, Calculable calculable){
        super(resolucionDTO, calculable);
    }
// hacer la misma operacion y entregarle la variable
    @Override
    public Calculable convert(String toCalculableType) {

        FacturaDTO factura = (FacturaDTO) mCalculable;

        switch (toCalculableType) {
            case Calculable.REMISION_TYPE:
                return obtenerRemisionDesdeFactura(factura);
            default:
                return null;

        }
    }

    private RemisionDTO obtenerRemisionDesdeFactura(FacturaDTO factura) {

        RemisionDTO remision = new RemisionDTO();
        remision.NumeroRemision = factura.NumeroFactura;
        remision.Fecha = factura.FechaHora;
        remision.IdCliente = factura.IdCliente;
        remision.CodigoRuta = ActivityBase.resolucion.CodigoRuta;
        remision.NombreRuta = ActivityBase.resolucion.NombreRuta;
        remision.Subtotal = factura.Subtotal + factura.Iva;
        remision.Iva = 0;
        remision.Total = factura.Total;
        remision.RazonSocialCliente = factura.RazonSocial;
        remision.Negocio = factura.Negocio;
        remision.IdentificacionCliente = factura.Identificacion;
        remision.TelefonoCliente = factura.Telefono;
        remision.DireccionCliente = factura.Direccion;
        remision.Anulada = factura.Anulada;
        remision.FechaCreacion = factura.FechaHora;
        remision.Latitud = factura.Latitud;
        remision.Longitud = factura.Longitud;
        remision.PendienteAnulacion = factura.PendienteAnulacion;
        remision.Comentario = factura.Comentario;
        remision.Sincronizada = factura.Sincronizada;
        remision.CodigoBodega = factura.CodigoBodega;
        remision.NumeroPedido = factura.NumeroPedido;
        remision.ComentarioAnulacion = factura.ComentarioAnulacion;
        remision.Descuento = factura.Descuento;
        remision.FormaPago = factura.FormaPago;
        remision.DetalleRemision = new ArrayList<>();

        for (DetalleFacturaDTO detalle : factura.DetalleFactura) {
            DetalleRemisionDTO detallere = new DetalleRemisionDTO();
            detallere.NumeroRemision = factura.NumeroFactura;
            detallere.IdProducto = detalle.IdProducto;
            detallere.NombreProducto = detalle.Nombre;
            detallere.Cantidad = detalle.Cantidad;
            detallere.ValorUnitario = detalle.ValorUnitario + (detalle.ValorUnitario * (detalle.PorcentajeIva / 100));
            detallere.Subtotal = detallere.Cantidad * detallere.ValorUnitario;
            detallere.Total = detalle.Total;
            detallere.Iva = 0;
            detallere.FechaCreacion = factura.FechaHora;
            detallere.PorcentajeIva = detalle.PorcentajeIva;
            detallere.Codigo = detalle.Codigo;
            detallere.Descuento = detalle.Descuento;
            detallere.PorcentajeDescuento = detalle.PorcentajeDescuento;
            remision.DetalleRemision.add(detallere);
        }

        return remision;
    }
}
