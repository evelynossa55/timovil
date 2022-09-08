package com.cm.timovil2.bl.utilities;

import com.cm.timovil2.dto.RemisionDTO;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 09/02/2016.
 */
public class ItemRemisionDetail implements Item {

    private RemisionDTO remisionDTO;

    public RemisionDTO getRemisionDTO() {
        return remisionDTO;
    }

    public void setRemisionDTO(RemisionDTO facturaDTO) {
        this.remisionDTO = facturaDTO;
    }

    @Override
    public boolean isSection() {
        return false;
    }
}