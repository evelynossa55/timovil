package com.cm.timovil2.dto;

import java.util.ArrayList;
//import java.util.Date;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 23/01/17.
 */
public class PedidoCallcenterDTO {

    public int IdPedido;
    public int IdCaso;
    public int IdCliente;
    public String Comentario;
    public String FechaSolicitada;
    public ArrayList<DetallePedidoCallcenterDTO> Detalle;

}
