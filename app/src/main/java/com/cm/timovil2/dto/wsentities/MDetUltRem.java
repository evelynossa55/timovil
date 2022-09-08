package com.cm.timovil2.dto.wsentities;

import android.content.Context;

import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

public class MDetUltRem {
    /**
     * Id de la remision
     */
    public int IdProd;
    /**
     * Cantidad vendida
     */
    public int Cant;
    /**
     * Código del producto
     */
    public String CodigoProducto;
    /**
     * Nombre del producto
     */
    public String NombreProducto;
    public double ValorUnitario;
    public double PorcentajeIva;
    public double Ipoconsumo;
    public double PorcentajeDescuento;
    public int DevolucionesAnteriores;
    public int IdDetalle;

    public String obtenerResumen(Context context) {
        try {
            StringBuilder sbDetalle = new StringBuilder();
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            if (resolucionDTO != null && !resolucionDTO.IdCliente.equalsIgnoreCase("2101")){
                sbDetalle.append((!CodigoProducto.equals("") ? CodigoProducto + ". " : "")).append(NombreProducto).append(": ").append(String.valueOf(Cant));
            }else{
                sbDetalle.append(NombreProducto).append(": ").append(String.valueOf(Cant));
            }
            return sbDetalle.toString();
        }catch (Exception e){
            return e.toString();
        }
    }
}
