package com.cm.timovil2.dto;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 04/08/2015.
 */
public class MotivoNoVentaDTO {

    public long IdMotivo;
    public String Descripcion;

    @Override
    public String toString() {
        return Descripcion;
    }
}
