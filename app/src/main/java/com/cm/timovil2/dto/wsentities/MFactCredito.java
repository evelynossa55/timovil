package com.cm.timovil2.dto.wsentities;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.cm.timovil2.dto.FacturaDTO;

public class MFactCredito extends FacturaDTO {

	public String IdFactura;
	public float Saldo;
	public String Resolucion;
	public String NombreRuta;
	public String FechaHoraCredito;
	public ArrayList<MDetalleFactura> DetalleFactura_Credito;
	
	@Override
	public String toString() {
		return NumeroFactura + ", " + RazonSocial + ", Saldo: " + Saldo;
	}

	@Override
	public String getResumenValores() {
        DecimalFormat df = new DecimalFormat("###,###.#");
		return "Total: " + df.format(Total)
				+ " Saldo: " + df.format(Saldo);
	}
}
