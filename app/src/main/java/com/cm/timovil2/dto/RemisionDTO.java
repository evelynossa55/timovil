package com.cm.timovil2.dto;


import com.cm.timovil2.bl.calculus.Calculable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RemisionDTO extends Calculable{

    public int IdAuto;
    public String NumeroRemision;
    public long Fecha;
    public int IdCliente;
    public String CodigoRuta;
    public String NombreRuta;
    public double Subtotal;
    public double Iva;
    public double Total;
    public String RazonSocialCliente;
    public String Negocio;
    public String IdentificacionCliente;
    public String TelefonoCliente;
    public String DireccionCliente;
    public boolean Anulada;
    public long FechaCreacion;
    public String Latitud;
    public String Longitud;
    public String Comentario;
    public String CodigoBodega;
    public boolean Sincronizada;
    public boolean PendienteAnulacion;
    public boolean Guardada;
    public String NumeroPedido = "";
    public String ComentarioAnulacion = "";
    public double Descuento;
    public String FormaPago;
    public double ValorRetefuente;
    public double RetefuenteDevolucion;
    public double ValorReteIvaDevolucion;
    public double ValorReteIva;
    public ArrayList<DetalleRemisionDTO> DetalleRemision;

    public String getResumenValores() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return "Subtotal: " + formatter.format(Subtotal)
                + " Descuento: " + formatter.format(Descuento)
                + " \nDevolución: " + formatter.format(ValorDevolucion)
                + " Ipoconsumo: " + formatter.format(Ipoconsumo)
                + " \nIva: " +  formatter.format(Iva)
                + " Total: " + formatter.format(Total);
              //  revisar si funciona + " hola " + formatter.format(Total);
    }

    public boolean Impresa;
    public boolean IsPedidoCallcenter;
    public boolean CreadaConCodigoBarras;
    public int Devolucion;
    public int Rotacion;
    public int IdPedido;
    public int IdCaso;
    public double ValorDevolucion;
    public float Ipoconsumo;
    public String EnviadaDesde;
}
