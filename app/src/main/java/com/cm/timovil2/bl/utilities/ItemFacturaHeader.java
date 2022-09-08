package com.cm.timovil2.bl.utilities;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 02/02/2016.
 */
public class ItemFacturaHeader implements Item {

    private long fechaFactura;

    public long getFechaFactura() {
        return fechaFactura;
    }

    public void setFechaFactura(long fechaFactura) {
        this.fechaFactura = fechaFactura;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
