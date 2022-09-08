package com.cm.timovil2.dto;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/03/18.
 */

public class DetalleNotaCreditoFacturaDTO {

    public int IdDetalleNotaCreditoFactura;
    public String NumeroDocumento;
    public int IdProducto;
    public int Cantidad;
    public double Subtotal;
    public double Descuento;
    public double Ipoconsumo;
    public double Iva5;
    public double Iva19;
    public double Iva;
    public double Valor;
    public String Codigo;
    public String Nombre;

    public String getResume(){
        try {
            return  ((!Codigo.equals("") ? Codigo + ". " : ""))
                        + (Nombre)
                        + (" Cant: ")
                        + (String.valueOf(Cantidad))
                        + (" Valor: ")
                        + ((int)Valor);

        } catch (Exception ex) {
            return ex.toString();
        }
    }

}
