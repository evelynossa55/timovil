package com.cm.timovil2.dto;

import java.util.Hashtable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class ProductoDTO implements KvmSerializable {

    public int IdProducto;
    public String Codigo;
    public String Nombre;
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

    public float PorcentajeIva;

    public int StockInicial = 0;
    public int Ventas = 0;
    public int Devoluciones = 0;
    public int Rotaciones = 0;

    public float IpoConsumo;
    public String CodigoBodega;

    public int getStock(ResolucionDTO resolucionDTO) {
        int stock = StockInicial - this.Ventas;

        boolean dr = true;
        boolean rr = true;
        if (resolucionDTO.EsDatoValido()) {
            dr = resolucionDTO.DevolucionRestaInventario;
            rr = resolucionDTO.RotacionRestaInventario;
        }

        if (dr) {
            stock -= Devoluciones;
        }
        if (rr) {
            stock -= Rotaciones;
        }

        return stock;
    }

    public ProductoDTO() {
    }

    // http://ws.tiendamovil.com.co/MovilServices.asmx?WSDL
    public Object getProperty(int arg0) {
        switch (arg0) {
            case 0:
                return IdProducto;
            case 1:
                return Codigo;
            case 2:
                return Nombre;
            case 3:
                return Precio1;
            case 4:
                return Precio2;
            case 5:
                return Precio3;
            case 6:
                return PorcentajeIva;
            default:
                return null;
        }
    }

    public int getPropertyCount() {
        return 7;
    }

    public void getPropertyInfo(int arg0, Hashtable arg1, PropertyInfo info) {
        switch (arg0) {
            case 0:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "IdProducto";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Codigo";
                break;
            case 2:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Nombre";
                break;
            case 3:
                info.type = PropertyInfo.LONG_CLASS;
                info.name = "Precio1";
                break;
            case 4:
                info.type = PropertyInfo.LONG_CLASS;
                info.name = "Precio2";
                break;
            case 5:
                info.type = PropertyInfo.LONG_CLASS;
                info.name = "Precio3";
                break;
            case 6:
                info.type = PropertyInfo.LONG_CLASS;
                info.name = "PorcentajeIva";
                break;
            default:
                break;
        }
    }

    public void setProperty(int arg0, Object valor) {
        switch (arg0) {
            case 0:
                IdProducto = Integer.parseInt(valor.toString());
                break;
            case 1:
                Codigo = valor.toString();
                break;
            case 2:
                Nombre = valor.toString();
                break;
            case 3:
                Precio1 = Float.parseFloat(valor.toString());
                break;
            case 4:
                Precio2 = Float.parseFloat(valor.toString());
                break;
            case 5:
                Precio3 = Float.parseFloat(valor.toString());
                break;
            case 6:
                PorcentajeIva = Float.parseFloat(valor.toString());
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return this.Codigo + ". " + this.Nombre + " Stock: " + String.valueOf(StockInicial);
    }
}