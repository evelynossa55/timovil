package com.cm.timovil2.dto;

import com.cm.timovil2.bl.calculus.Calculable;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class FacturaDTO extends Calculable{

	public FacturaDTO() {
		Guardada = false;
		Sincronizada = false;
        Comentario = "";
		Iniciar();
	}

	public int _Id;
	public int IdCliente;
	public int IdResolucion;
	public String Identificacion;
	public String RazonSocial;
	public String Negocio;
	public String Direccion;
	public String Telefono;
	public float Iva;
	public float Ica;
	public float PorcentajeRetefuente = 0;
	public boolean PendienteAnulacion;
	public boolean Sincronizada;
	private float APagar;
	public boolean Anulada;
	public int Cantidad;
	public int Rotacion;
	public float Descuento = 0;
	public long FechaHora;
	public String FormaPago;
	public String NumeroFactura;
	public double Retefuente;
	public double Subtotal;
	public double Total;

	/**
	 * Solo se usa para control en la pantalla de facturacién.
	 */
	public boolean Guardada;
	// Esto es para saber la devuelta que le toca al cliente, solicitado por Avandes
	public float EfectivoPagado = 0;
	public int Devolucion = 0;

	// Para implementar la Georeferenciación con la diferencia de distancia con el codBaras.

	//Donde esat el rutero.
	public String Latitud ;
	public String Longitud ;

	//Diferencia de Distancia entre la lectura del códigi de barras y el sitio del cliente
	public String DistanciaCodBarras;

	// **Fin Georeferenciación
	
	public double CREE;
	public double PorcentajeCREE;
    public String Comentario;
    public float IpoConsumo;
    public String TipoDocumento;
    public String CodigoBodega;
    public String NumeroPedido;
    public boolean Impresa;
    public String ComentarioAnulacion = "";
    public int IdEmpleadoEntregador;
    public boolean Remision;

	/**
	 *Indica si hay o no necesidad de verificar la integridad de la factura con el servicio
	 * ServiceMonitorFacturacion
	 */
	public boolean Revisada;
    public boolean IsPedidoCallcenter;
	public long FechaHoraVencimiento;
	public int IdPedido;
	public int IdCaso;
	public double ValorDevolucion;
	public float ReteIva;
	public String EnviadaDesde;

    /**
     * Manejo diferente al tipo de la resolución pos. Se utiliza para la creación de facturas
     * a un tipo de cliente genérico
     */
	public boolean FacturaPos;

	/**
	 * Valor para generar el código QR, en caso de que se esté manejando Facturación Electrónica
	 */
	public String QRInputValue;

	/**
	 * CÖDIGO CUFE, en caso de que se esté manejando Facturación Electrónica
	 */
	public String Cufe;

	/**
	 * Indica si la factura se creó leyendo el código de barras del cliente
	 */
	public boolean CreadaConCodigoBarras;

	public NotaCreditoFacturaDTO notaCreditoFactura;

    public ArrayList<DetalleFacturaDTO> DetalleFactura;

	public String toString() {
		return NumeroFactura + "-" + APagar;
	}

	public String getResumenValores() {
		NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
		DecimalFormat formatter2 = new DecimalFormat("$###,###.##");
		return "Subtotal: " + formatter.format(Subtotal)
				+ " Descuento: " + formatter.format(Descuento)
				+ "\nDevolución: " + formatter.format(ValorDevolucion)
				+ " Retefuente: " + formatter.format(Retefuente)
				+ "\nReteiva: " + formatter.format(ReteIva)
				+ " Iva: " + formatter.format(Iva)
				+ "\nIpoConsumo: " + formatter.format(IpoConsumo)
				+ " Total: " + formatter.format(Total);
	}


    private void Iniciar() {
        Total = 0;
        APagar = 0;
        Subtotal = 0;
        Descuento = 0;
        Retefuente = 0;
        ReteIva = 0;
        PorcentajeRetefuente = 0;
        Iva = 0;
        Guardada = false;
        EfectivoPagado = 0;
        CREE = 0;
        PorcentajeCREE = 0;
        IpoConsumo = 0;
    }

}
