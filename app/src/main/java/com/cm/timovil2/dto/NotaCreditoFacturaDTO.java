package com.cm.timovil2.dto;

import com.cm.timovil2.bl.utilities.Utilities;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 5/03/18.
 */

public class NotaCreditoFacturaDTO {

    public int IdNotaCreditoFactura;
    public String NumeroDocumento;
    public String NumeroFactura;
    public long Fecha;
    public double Subtotal;
    public double Descuento;
    public double Ipoconsumo;
    public double Iva5;
    public double Iva19;
    public double Iva;
    public double Valor;
    public String CodigoBodega;
    public String EnviadaDesde;

    //Estados
    public boolean Sincronizada;
    public boolean Impresa;
    public boolean Anulada;

    /**
     * Valor para generar el código QR, en caso de que se esté manejando Facturación Electrónica
     */
    public String QRInputValue;
    /**
     * Valor para generar el Cufe, en caso de que se esté manejando Facturación Electrónica
     */
    public String Cufe;

    public ArrayList<DetalleNotaCreditoFacturaDTO> DetalleNotaCreditoFactura;

    public JSONObject toJSON(){

        try{
            JSONObject jsonNota = new JSONObject();
            jsonNota.put("NumDoc", NumeroDocumento);
            jsonNota.put("NumFac", NumeroFactura);
            jsonNota.put("Fecha", Utilities.FechaHoraAnsiJoda(new DateTime(Fecha)));
            jsonNota.put("Subtotal", Subtotal);
            jsonNota.put("Descuento", Descuento);
            jsonNota.put("Ipoconsumo", Ipoconsumo);
            jsonNota.put("Iva5", Iva5);
            jsonNota.put("Iva19", Iva19);
            jsonNota.put("Iva", Iva);
            jsonNota.put("Valor", Valor);
            jsonNota.put("CodigoBodega", CodigoBodega);
            jsonNota.put("EnviadaDesde", EnviadaDesde);
            jsonNota.put("Anulada", Anulada);
            jsonNota.put("QRInputValue", QRInputValue);
            jsonNota.put("Cufe", Cufe);

            JSONArray array = new JSONArray();

            for (DetalleNotaCreditoFacturaDTO oDetalle : DetalleNotaCreditoFactura) {
                JSONObject jsonDetalle = new JSONObject();
                jsonDetalle.put("IdProd", oDetalle.IdProducto);
                jsonDetalle.put("Cant", oDetalle.Cantidad);
                jsonDetalle.put("NumDoc", oDetalle.NumeroDocumento);
                jsonDetalle.put("Subtotal", oDetalle.Subtotal);
                jsonDetalle.put("Descuento", oDetalle.Descuento);
                jsonDetalle.put("Ipoconsumo", oDetalle.Ipoconsumo);
                jsonDetalle.put("Iva5", oDetalle.Iva5);
                jsonDetalle.put("Iva19", oDetalle.Iva19);
                jsonDetalle.put("Iva", oDetalle.Iva);
                jsonDetalle.put("Valor", oDetalle.Valor);
                array.put(jsonDetalle);
            }

            jsonNota.put("Detalle", array);
            return jsonNota;

        }catch (Exception e){
            return null;
        }
    }

    public String getResumenValores(){
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Utilities.getLocale());
        return "Valor: " + formatter.format(Valor);
    }

}
