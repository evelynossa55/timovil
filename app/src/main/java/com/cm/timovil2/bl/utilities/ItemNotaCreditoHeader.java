package com.cm.timovil2.bl.utilities;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 14/03/18.
 */

public class ItemNotaCreditoHeader implements Item{

    private long fechaNota;

    public long getFechaNota() {
        return fechaNota;
    }

    public void setFechaNota(long fechaNota) {
        this.fechaNota = fechaNota;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
