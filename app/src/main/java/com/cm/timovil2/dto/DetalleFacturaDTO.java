package com.cm.timovil2.dto;

import android.content.Context;

import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.text.NumberFormat;
import java.util.Locale;

public class DetalleFacturaDTO {

    public DetalleFacturaDTO() {
        Cantidad = 0;
        Devolucion = 0;
        Rotacion = 0;
        PorcentajeDescuento = 0;
        PorcentajeIva = 0;
        Total = 0;
        Index = Integer.MIN_VALUE;
        PorcentajeDescuento = 0;
    }

    public int _Id;
    public String NumeroFactura;
    public int IdProducto;
    public String Codigo;
    public String Nombre;
    public int Cantidad;
    public int Devolucion;
    public int Rotacion;
    public float ValorUnitario;

    //Para la selección de productos en detalleFacturación
    public int Index;
    public double Subtotal;
    public float PorcentajeDescuento;
    public double Descuento;
    public float PorcentajeIva;
    public double Iva;
    public double Total;
    public int StockDisponible = 0;

    public float Precio1;
    public float Precio2;
    public float Precio3;
    public float Precio4;
    public float Precio5;
    public float Precio6;
    public float Precio7;
    public float Precio8;
    public float Precio9;
    public float Precio10;
    public float Precio11;
    public float Precio12;
    public float IpoConsumo;
    public float ValorIpoConsumo;
    public float DescuentoAdicional;

    //Devoluciones
    public float SubtotalDevolucion;
    public float DescuentoDevolucion;
    public float Iva5Devolucion;
    public float Iva19Devolucion;
    public float IvaDevolucion;
    public float IpoconsumoDevolucion;
    public double ValorDevolucion;

    @Override
    public String toString() {
        return Codigo.trim() + ": " + Nombre.trim() + " (" + StockDisponible + " disponibles)";
    }

    public String getResume(Context context){

        try {
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            StringBuilder sbDetalle = new StringBuilder();
            int devolucion = Devolucion;
            int rotacion = Rotacion;

            if(resolucionDTO != null && !resolucionDTO.IdCliente.equalsIgnoreCase(Utilities.ID_DOBLEVIA)){
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
                sbDetalle.append((!Codigo.equals("") ? Codigo + ". " : ""))
                        .append(Nombre).append(": ")
                        .append(Cantidad).append("  ")
                        .append("Descuento: ").append(formatter.format(Descuento)).append(" (")
                        .append(PorcentajeDescuento).append("%)");
            }else{
                sbDetalle.append(Nombre).append(": ").append(Cantidad);
            }

            if (devolucion > 0) {
                sbDetalle.append(" Devolución: ").append(Devolucion);
            }

            if (rotacion > 0) {
                sbDetalle.append(" Rotación: ").append(Rotacion);
            }

            return sbDetalle.toString();

        } catch (Exception ex) {
            return ex.toString();
        }
    }
}