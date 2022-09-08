package com.cm.timovil2.dto.wsentities;

public class MDetalleListaPrecios {

    public int IdDetalleListaPrecios;
    public int IdListaPrecios;
    public int IdProducto;
    public double Precio;
    public String NombreListaPrecios;

    @Override
    public String toString() {
        return NombreListaPrecios;
    }
}
