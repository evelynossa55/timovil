package com.cm.timovil2.dto.wsentities;

import java.util.UUID;

public class MAbono {

	public MAbono(){
		Sincronizado=false;
		this.Identificador = UUID.randomUUID().toString();
	}
	public int _Id;
	public String Identificador;
	public String IdFactura;
	public String NumeroFactura;
	public String Fecha;
	public float Valor;
	public float Saldo;
	
	public boolean Sincronizado;
    public String DiaCreacion;
	public int IdCuentaCaja;
	public long FechaCreacion;
    public String EnviadoDesde;
}
