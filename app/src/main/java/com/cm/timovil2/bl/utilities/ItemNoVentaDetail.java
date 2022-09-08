package com.cm.timovil2.bl.utilities;

import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 12/06/17.
 */
public class ItemNoVentaDetail implements Item {

    private GuardarMotivoNoVentaDTO gmnv;
    private GuardarMotivoNoVentaPedidoDTO gmnvp;
    private ClienteDTO cliente;
    private boolean isPedido;

    public GuardarMotivoNoVentaDTO getMotivoNvDTO() {
        return gmnv;
    }
    public GuardarMotivoNoVentaPedidoDTO getMotivoNvpDTO() {
        return gmnvp;
    }

    public void setMotivoNvDTO(GuardarMotivoNoVentaDTO gmnv) {
        this.gmnv = gmnv;
    }

    public void setMotivoNvpDTO(GuardarMotivoNoVentaPedidoDTO gmnvp) {
        this.gmnvp = gmnvp;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    public boolean isPedido() {
        return isPedido;
    }

    public void setPedido(boolean pedido) {
        isPedido = pedido;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }
}