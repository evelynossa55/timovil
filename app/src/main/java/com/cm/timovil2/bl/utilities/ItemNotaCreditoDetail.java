package com.cm.timovil2.bl.utilities;

import com.cm.timovil2.dto.NotaCreditoFacturaDTO;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 14/03/18.
 */

public class ItemNotaCreditoDetail implements Item {

    private NotaCreditoFacturaDTO notaCreditoFacturaDTO;

    public NotaCreditoFacturaDTO getNotaCreditoFacturaDTO() {
        return notaCreditoFacturaDTO;
    }

    public void setNotaCreditoFacturaDTO(NotaCreditoFacturaDTO notaCreditoFacturaDTO) {
        this.notaCreditoFacturaDTO = notaCreditoFacturaDTO;
    }

    @Override
    public boolean isSection() {
        return false;
    }

}
