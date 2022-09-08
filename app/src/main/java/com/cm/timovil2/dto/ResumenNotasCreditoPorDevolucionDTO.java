package com.cm.timovil2.dto;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * JORGE ANDRÉS DAVID CARDONA EL 3/05/18.
 */

public class ResumenNotasCreditoPorDevolucionDTO {

    public double Subtotal;
    public double Descuento;
    public double Ipoconsumo;
    public double Iva5;
    public double Iva19;
    public double Iva;
    public float Valor;

    public void Iniciar() {
        Subtotal = 0;
        Descuento = 0;
        Ipoconsumo = 0;
        Iva5 = 0;
        Iva19 = 0;
        Iva = 0;
        Valor = 0;
    }

    @Override
    public String toString() {
        StringBuilder sbResumen = new StringBuilder();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        sbResumen
                .append(" NOTAS CREDITO POR DEVOLUCION")
                .append("\r\n Subtotal: ").append(formatter.format(Subtotal))
                .append("\r\n Descuento: ").append(formatter.format(Descuento))
                .append("\r\n Ipoconsumo: ").append(formatter.format(Ipoconsumo))
                .append("\r\n Iva 5: ").append(formatter.format(Iva5))
                .append("\r\n Iva 19: ").append(formatter.format(Iva19))
                .append("\r\n Total Iva: ").append(formatter.format(Iva))
                .append("\r\n Valor: ").append(formatter.format(Valor));

        return  sbResumen.toString();
    }
}
