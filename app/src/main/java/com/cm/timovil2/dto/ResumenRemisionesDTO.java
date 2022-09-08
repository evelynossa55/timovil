package com.cm.timovil2.dto;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 17/03/18.
 */

public class ResumenRemisionesDTO {

    public int cantidad = 0;
    public int devoluciones = 0;
    public int rotaciones = 0;
    public float subtotal = 0;
    public float descuento = 0;
    public float iva = 0;
    public float total = 0;
    public float retefuente = 0;
    public float reteiva = 0;
    public float porcentajeRetefuente = 0;
    public float aPagar = 0;
    public int anuladas = 0;
    public int pendientesSincronizacion = 0;
    public float ipoConsumo = 0;

    /**
     * Ventas a crédito
     */
    public float credito;

    /**
     * Abonos recibidos
     */
    public float debito;

    /**
     * Ventas de contado
     */
    public float contado;

    //public static int pendientesAnulacion = 0; //Sincronizadas, pero luego anuladas.
    public String error=null;

    /**
     * Cantidad de abonos pendientes por descargar.
     */
    public int abonosPendientes;

    public float ValorDevolucion;

    public void Iniciar() {
        cantidad = 0;
        devoluciones = 0;
        rotaciones = 0;
        subtotal = 0;
        descuento = 0;
        iva = 0;
        total = 0;
        retefuente = 0;
        reteiva = 0;
        porcentajeRetefuente = 0;
        aPagar = 0;
        anuladas = 0;
        pendientesSincronizacion = 0;
        //pendientesAnulacion = 0;
        error = null;
        credito = 0;
        contado = 0;
        debito = 0;
        abonosPendientes = 0;
        ipoConsumo = 0;
        ValorDevolucion =0;
    }

    @Override
    public String toString() {
        StringBuilder sbResumen = new StringBuilder();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        sbResumen
                .append(" REMISIONES")
                .append("\r\n Subtotal: ").append(formatter.format(subtotal))
                .append("\r\n Iva: ").append(formatter.format(iva))
                .append("\r\n Descuento: ").append(formatter.format(descuento))
                .append("\r\n Retefuente: ").append(formatter.format(retefuente))
                .append("\r\n Reteiva: ").append(formatter.format(reteiva))
                .append("\r\n Ipo Consumo: ").append(formatter.format(ipoConsumo))
                .append("\r\n Devoluciones: ").append(formatter.format(ValorDevolucion))
                .append("\r\n Débito: ").append(formatter.format(debito))
                .append("\r\n Crédito: ").append(formatter.format(credito))
                .append("\r\n Contado: ").append(formatter.format(contado))
                .append("\r\n Devolución:").append(String.valueOf(devoluciones))
                .append("\r\n Rotación:").append(String.valueOf(rotaciones))
                .append("\r\n Total: ").append(formatter.format(total))
                .append("\r\n A pagar: ").append(formatter.format(contado + debito));
        return sbResumen.toString();
    }
}
