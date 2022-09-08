package com.cm.timovil2.sincro;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ControlImeiDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.proxy.ProxyResolucion;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 5/06/18.
 */

public class SincroResolucion implements ISincroData {

    private static SincroResolucion instance = null;
    private ControlImeiDTO controlImei = null;
    private ResolucionDTO resolucionActualDTO;

    public static SincroResolucion getInstance() {
        if(instance == null) {
            instance = new SincroResolucion();
        }
        return instance;
    }

    @Override
    public void download(ActivityBase context) throws Exception{
        ResolucionDAL resolucionDAL = new ResolucionDAL(context);
        resolucionActualDTO = resolucionDAL.ObtenerResolucion();

        if ( controlImei != null) {
            //Se está descargando la resolución por primera vez
            resolucionActualDTO = new ResolucionDTO();
            resolucionActualDTO.IdCliente = controlImei.IdCliente;
            resolucionActualDTO.CodigoRuta = controlImei.CodigoRuta;
        }

        String idCliente = resolucionActualDTO.IdCliente;
        String codigoRuta = resolucionActualDTO.CodigoRuta;

        ResolucionDTO resolucionDTO = new ProxyResolucion(idCliente, codigoRuta).ObtenerResolucion();
        resolucionDTO.TipoImpresora = App.obtenerPreferencias_TipoImpresora(context);
        resolucionDTO.NombreImpresora = App.obtenerPreferencias_NombreImpresora(context);
        resolucionDTO.MACImpresora = App.obtenerPreferencias_MacImpresora(context);
        resolucionDTO.NumeroCopias = App.obtenerPreferencias_NroCopias(context);
        resolucionDAL.Insertar(resolucionDTO);

        App.guardarConfiguracionRuta(resolucionDTO, context);
        App.guardarConfiguracion_PermitirFacturarSinInventario(context, resolucionDTO.PermitirFacturarSinInventario);
        App.guardarConfiguracion_LastVersionAplicacion(context, resolucionDTO.ult_version_aplicacion);
        App.guardarConfiguracion_SincronizarInventario(context, resolucionDTO.SincronizarInventario);
        App.guardarConfiguracion_valorVentaMensual(context, (float) resolucionDTO.ValorMetaMensual);
        App.guardarConfiguracion_periodoReporteUbicacion(context, resolucionDTO.PeridoReporteUbicacion);
        App.guardarConfiguracion_tipoRuta(context, resolucionDTO.TipoRuta);
        App.guardarConfiguracion_imprimir(context, resolucionDTO.Imprimir);
        App.guardarConfiguracion_ManejarRecaudoCredito(context, resolucionDTO.ManejarRecaudoCredito);
        App.guardarConfiguracion_PermitirCambiarFormaDePago(context, resolucionDTO.PermitirCambiarFormaDePago);
        App.guardarConfiguracion_CantidadFacturasClientePorDia(context, resolucionDTO.CantidadFacturasClientePorDia);
        App.guardarConfiguracion_TipoResolucion(context, resolucionDTO.TipoResolucion);
        App.guardarConfiguracion_NumeroCelularRuta(context, resolucionDTO.NumeroCelularRuta);
        App.guardarConfiguracion_FechaDeResolucion(context, resolucionDTO.FechaDeResolucion);
        App.guardarConfiguracion_PermitirRemisionesConValorCero(context, resolucionDTO.PermitirRemisionesConValorAcero);
        App.guardarConfiguracion_TopeReteIva(context, resolucionDTO.TopeReteIva);
        App.guardarConfiguracion_PorcentajeReteIva(context, resolucionDTO.PorcentajeReteIva);
        App.guardarConfiguracion_CrearNotaCreditoPorDevolucion(context, resolucionDTO.CrearNotaCreditoPorDevolucion);
        App.guardarConfiguracion_ManejarInventarioRemisiones(context, resolucionDTO.ManejarInventarioRemisiones);
        App.guardarConfiguracion_FechaVigenciaDeResolucion(context, resolucionDTO.VigenciaDeResolucion);
        App.guardarConfiguracion_DevolucionAfectaRemision(context, resolucionDTO.DevolucionAfectaRemision);
        App.guardarConfiguracion_EFactura(context, resolucionDTO.EFactura);
        App.guardarConfiguracion_ClaveTecnica(context, resolucionDTO.ClaveTecnica);
        App.guardarConfiguracion_AmbienteEFactura(context, resolucionDTO.AmbienteEFactura);
        resolucionActualDTO = resolucionDTO;
    }

    public void setControlImei(ControlImeiDTO controlImei) {
        this.controlImei = controlImei;
    }

    public ResolucionDTO getResolucionActualDTO() {
        return resolucionActualDTO;
    }
}
