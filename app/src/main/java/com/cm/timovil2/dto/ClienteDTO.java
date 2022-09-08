package com.cm.timovil2.dto;

import java.util.ArrayList;

public class ClienteDTO {
	public int Dia; //Déa rutero
	public int Orden; //Orden del rutero
	public int IdCliente;
	public String Identificacion;
	public String RazonSocial;
	public String NombreComercial;
	public String Direccion;
	public String Telefono1;
	public String Telefono2;

	//Ontiene si el cliente factura solo con codigo de barras
	public boolean ObligatorioCodigoBarra;

	//Si tiene obligatorio la facturacion electronica
	public boolean FacturacionElectronicaCliente;
	public boolean FacturacionPOSCliente;

	//Donde esta el codBarras.
	public String LatitudCodBarras = "";
	public String LongitudCodBarras = "";

	//DistanciaDelNegocion

	/**
	 * Obtiene é establece si el cliente realiza retención en la fuente
	 */
	public boolean ReteFuente;
	/**
	 * Obtiene é establece si el cliente realiza retención CREE.
	 * Impuesto creado en mayo-2013
	 */
	public boolean RetenedorCREE;
	public double PorcentajeCree;
	
	public int _Id;
	
	/*public ArrayList<DescuentoDTO> Descuentos;
	public ArrayList<RuteroDTO> Rutero;*/
	
	/**
	 * Cuando se le realiza una factura a un cliente, este campo se pone en Verdadero.
	 */
	public String Atendido;

    /**
     * Lista de precios asociada al cliente
     */
    public int ListaPrecios;
	
	/*public class RuteroDTO
	{
		public int Dia;
	}*/

    public String CarteraPendiente;

    public String Remisiones;
    public boolean Credito;
    public int IdListaPrecios;
    public boolean Remision;
    public String ValorVentasMes;
    public boolean FormaPagoFlexible;
	public int VecesAtendido;
	public boolean ExentoIva;
	public String DireccionEntrega;
	public int Plazo;
	public String Ubicacion;
	public boolean ReteIva;

    public ArrayList<AsesorProgramacionDetalleDTO> programacionAsesor;

    public ClienteDTO(){
        Atendido = "";
        CarteraPendiente = "";
    }

	@Override
	public String toString() {
		return Identificacion + " " + RazonSocial + " " + NombreComercial;
	}
}