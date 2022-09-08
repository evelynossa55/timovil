package com.cm.timovil2.dto;

import android.content.Context;

import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;

import java.text.NumberFormat;
import java.util.Locale;

public class DetalleRemisionDTO {
    public int IdDetalle;
    public String Codigo;
    public String NumeroRemision;
    public int IdProducto;
    public String NombreProducto;
    public int Cantidad;
    public double ValorUnitario;
    public double Subtotal;
    public double Total;
    public double Iva;
    public long FechaCreacion;
    public double PorcentajeIva;
    public double Descuento;
    public double PorcentajeDescuento;
    public int Devolucion;
    public int Rotacion;
    public int StockDisponible = 0;
    public double ValorDevolucion;
    public float Ipoconsumo;
    public float DescuentoAdicional;
    public double IvaDevolucion;

    public String getResume(Context context){
        try {
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            StringBuilder sbDetalle = new StringBuilder();
            if (resolucionDTO != null
                    && !resolucionDTO.IdCliente.equalsIgnoreCase(Utilities.ID_DOBLEVIA)){

                    sbDetalle.append((!Codigo.equals("") ? Codigo + ". " : "")).append(NombreProducto)
                            .append(": ") .append(String.valueOf(Cantidad))
                            .append(", Valor unitario: ").append(formatter.format(ValorUnitario))
                            .append(", Descuento: ").append(formatter.format(Descuento)).append(" (")
                            .append(PorcentajeDescuento).append("%)")
                            .append("; Val Devolución: ") .append(String.valueOf(ValorDevolucion))
                            .append("; Ipoconsumo: ") .append(String.valueOf(Ipoconsumo));

            }else{
                sbDetalle.append(NombreProducto).append(": ").append(String.valueOf(Cantidad));
            }
            return sbDetalle.toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    public String getResumeDetallado(Context context){
        try {
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            StringBuilder sbDetalle = new StringBuilder();
            if (resolucionDTO != null
                    && !resolucionDTO.IdCliente.equalsIgnoreCase(Utilities.ID_DOBLEVIA)){

                sbDetalle.append((!Codigo.equals("") ? Codigo + ". " : "")).append(NombreProducto)
                        .append("; Cantidad: ") .append(String.valueOf(Cantidad))
                        .append("; Devolución: ") .append(String.valueOf(Devolucion))
                        .append("; Val Devolución: ") .append(String.valueOf(ValorDevolucion))
                        .append("; Ipoconsumo: ") .append(String.valueOf(Ipoconsumo))
                        .append("; Rotación: ") .append(String.valueOf(Rotacion))
                        .append("; Valor unitario: ").append(formatter.format(ValorUnitario))
                        .append("; Subtotal: ").append(formatter.format(Subtotal))
                        .append("; Porcentaje Iva: ").append(PorcentajeIva).append("%")
                        .append("; Iva: ").append(formatter.format(Iva))
                        .append("; Descuento: ")
                        .append(formatter.format(Descuento)).append(" (")
                        .append(PorcentajeDescuento).append("%)");

            }else{
                sbDetalle.append(NombreProducto).append(": ").append(String.valueOf(Cantidad));
            }
            return sbDetalle.toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }
}
