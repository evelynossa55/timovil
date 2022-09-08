package com.cm.timovil2.dto;

/*
 * Created by JORGE A. DAVID CARDONA on 29/04/2015.
 */
public class EntregadorDTO {

    public int IdEntregador;
    public int IdEmpleado;
    public String NombreCompleto;

    @Override
    public String toString() {
        return NombreCompleto;
    }
}
