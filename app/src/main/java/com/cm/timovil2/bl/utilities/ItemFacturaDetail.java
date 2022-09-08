package com.cm.timovil2.bl.utilities;

import com.cm.timovil2.dto.FacturaDTO;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 02/02/2016.
 */
public class ItemFacturaDetail implements Item {

    private FacturaDTO facturaDTO;

    public FacturaDTO getFacturaDTO() {
        return facturaDTO;
    }

    public void setFacturaDTO(FacturaDTO facturaDTO) {
        this.facturaDTO = facturaDTO;
    }

    @Override
    public boolean isSection() {
        return false;
    }
}
