package com.cm.timovil2.dto;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 20/02/17.
 */
public class CuentaCajaDTO {

    public int IdCuentaCaja;
    public String Nombre;
    public String NumeroCuenta;

    @Override
    public String toString() {
        return Nombre +": "+NumeroCuenta;
    }
}
