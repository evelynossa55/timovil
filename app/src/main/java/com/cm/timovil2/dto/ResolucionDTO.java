package com.cm.timovil2.dto;

import com.cm.timovil2.bl.app.Seguridad.TiposUsuario;
import com.cm.timovil2.bl.calculus.Calculable;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.bl.calculus.Calculator;
import java.text.NumberFormat;
import java.util.Locale;

public class ResolucionDTO {

    public int IdResolucion=-1;
    public String Regimen=""; //IVA REG COMUN
    public String RazonSocial=""; //GERARDO JARAMILLO - P.NATIPAN
    public String NombreComercial="";
    public String Nit=""; //71586506-8
    public String Direccion=""; //CALLE 38 N 56 110 BELLO
    public String Telefono="";
    public String Email="";
    public String FacturaInicial="";
    public String FacturaFinal="";
    public String Resolucion="";//resolucion facturacion
    public String FechaResolucion=""; //Enero 12, 2017 Format
    public String FechaDeResolucion="";//2017.1.12 Format
    public String VigenciaDeResolucion="";//2017.1.12 Format
    public int SiguienteFactura;
    public int SiguienteRemision;
    public int SiguienteNotaCredito;

    //Datos Vendedor
    public String CodigoRuta="";
    public String NombreRuta="";
    public String NumeroCelularRuta="";

    //Clave administrador
    public String ClaveAdmin="";

    //Id cliente TIMO
    public String IdCliente="";


    public String ClaveVendedor;
    public String PrefijoFacturacion;
    public boolean DevolucionAfectaVenta;
    public boolean DevolucionAfectaRemision;
    public boolean DevolucionRestaInventario;
    public boolean RotacionRestaInventario;
    public boolean MostrarListaPrecios;

    /**
     * Determina si las facturas se descargan automaticamente
     */
    public boolean DescargarFacturasAuto;

    public boolean RedondearValores;

    /**
     * Determina el némero de copias que se realizan al imprimir una factura
     */
    public int NumeroCopias;

    /**
     * Determina la direccién del servidor con el cual se comunica la aplicacién
     */
    public String UrlServicioWeb;

    /**
     * Determina si un usuario Vendedor puede eliminar facturas sin haberlas descargado.
     */
    public boolean PermitirEliminarFacturas;


    public boolean EsDatoValido()
    {
        return !(IdResolucion<=0);
    }

    public TiposUsuario TipoUsuario;
    public String NombreImpresora="";
    public String MACImpresora="";
    public String TipoImpresora="";
    public String CodigoBodega="";
    public boolean ManejarInventario;
    public float PorcentajeRetefuente;
    public float TopeRetefuente;
    public boolean ReportarUbicacionGPS;
    public String Bodegas;
    public boolean PermitirFacturarSinInventario;

    //Versión de la aplicación
    public String ult_version_aplicacion;

    //¿Se actualiza o no el inventario cada vez que se crea una factura o una remisión?
    public boolean SincronizarInventario;

    //Guarda el valor de las ventas que el vendedor tiene como meta para el mes actual
    public double ValorMetaMensual;

    /**
     * Valor de ventas del mes
     */
    public double ValorVentaMensual;

    public int PeridoReporteUbicacion;
    public String TipoRuta;
    public boolean Imprimir;
    public boolean ManejarRecaudoCredito;
    public boolean PermitirCambiarFormaDePago;
    public int CantidadFacturasClientePorDia;
    public int IdResolucionPOS;
    public int SiguienteFacturaPOS;
    public String PrefijoFacturacionPOS;
    public String FacturaInicialPOS;
    public String FacturaFinalPOS;
    public String ResolucionPOS;
    public String FechaResolucionPOS;
    public String TipoResolucion;
    public Long FechaCierre;
    public boolean DiaCerrado;
    public int HoraLimiteFacturacion;
    public boolean CierreNotificadoServidor;
    public boolean PermitirRemisionesConValorAcero;
    public float PorcentajeReteIva;
    public float TopeReteIva;
    public boolean CrearNotaCreditoPorDevolucion;
    public boolean ManejarInventarioRemisiones;
    public boolean EFactura;
    public String ClaveTecnica;
    public String AmbienteEFactura;

    public String getCumplimientoMeta() {
        StringBuilder sbRespuesta = new StringBuilder();
        if (ValorMetaMensual > 0) {
            sbRespuesta.append("Meta:         ").append(Utilities.FormatoMoneda(ValorMetaMensual));
            sbRespuesta.append("\nVentas:       ").append(Utilities.FormatoMoneda(ValorVentaMensual));
            if (ValorVentaMensual == 0) {
                sbRespuesta.append("\nCumplimiento: 0%");
            } else {
                sbRespuesta.append("\nCumplimiento: ").append(Utilities.FormatoPorcentaje(ValorVentaMensual / ValorMetaMensual));
            }
        } else {
            sbRespuesta.append("\nMeta:      Sin meta");
            sbRespuesta.append("\nVentas:    ").append(Utilities.FormatoMoneda(ValorVentaMensual));
        }
        return sbRespuesta.toString();
    }

    public static final String TIPO_COMPUTADOR = "COMPUTADOR";
    public static final String TIPO_POS = "POS";
    public static final String TIPO_ELECTRONICA = "ELECTRONICA";
}