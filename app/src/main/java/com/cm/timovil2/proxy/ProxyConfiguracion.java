package com.cm.timovil2.proxy;

import com.cm.timovil2.dto.ResolucionDTO;

import org.ksoap2.serialization.SoapObject;

class ProxyConfiguracion extends ProxyWS {

    private final String idCliente;
    private final String codigoRuta;
    private final ResolucionDTO config;

	ProxyConfiguracion(String _idCliente, String _codigoRuta, ResolucionDTO _config) {
		METHOD_NAME = MetodosWeb.OBTENER_CONFIGURACION;
		idCliente = _idCliente;
		codigoRuta = _codigoRuta;
        this.config = _config;
	}

	@Override
	SoapObject GetRequest() {
		SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
		so.addProperty("_codigoRuta", codigoRuta);
		so.addProperty("_idCliente", idCliente);
		return so;
	}

	@Override
	<T> T InvocarWS() throws Exception{

        String configuracion = invokeStringMethod();
		if (configuracion == null) {
			throw new Exception("No se pudo cargar la información desde el servidor.");
		}

        String[] separacionConfig = configuracion.split("\\|");

        config.ManejarInventario = separacionConfig[1].equals("1");
        config.TopeRetefuente = Float.valueOf(separacionConfig[2]);
        config.PorcentajeRetefuente = Float.valueOf(separacionConfig[3].replace(',', '.'));
        config.ReportarUbicacionGPS = separacionConfig[4].equals("1");
        config.Bodegas = separacionConfig[5];
        config.PermitirFacturarSinInventario = separacionConfig[6].equals("1");

        if(separacionConfig.length >= 8 && separacionConfig[7] != null){
            config.ult_version_aplicacion = separacionConfig[7];
        }

        if(separacionConfig.length >= 9 && separacionConfig[8] != null){
            config.SincronizarInventario = separacionConfig[8].equals("1");
        }

        if(separacionConfig.length >= 10 && separacionConfig[9] != null){
            config.ValorMetaMensual = Double.parseDouble(separacionConfig[9]);
        }

        if(separacionConfig.length >= 11 && separacionConfig[10] != null){
            config.PeridoReporteUbicacion = Integer.parseInt(separacionConfig[10]);
        }

        if(separacionConfig.length >= 12 && separacionConfig[11] != null){
            config.TipoRuta = separacionConfig[11];
        }

        if(separacionConfig.length >= 13 && separacionConfig[12] != null){
            config.Imprimir = Boolean.parseBoolean(separacionConfig[12]);
        }

        if(separacionConfig.length >= 14 && separacionConfig[13] != null){
            config.ManejarRecaudoCredito = Boolean.parseBoolean(separacionConfig[13]);
        }

        if(separacionConfig.length >= 15 && separacionConfig[14] != null){
            config.PermitirCambiarFormaDePago = Boolean.parseBoolean(separacionConfig[14]);
        }

        if(separacionConfig.length >= 16 && separacionConfig[15] != null){
            config.CantidadFacturasClientePorDia = Integer.parseInt(separacionConfig[15]);
        }

        if(separacionConfig.length >= 17 && separacionConfig[16] != null){
            config.TipoResolucion =separacionConfig[16];
        }

        if(separacionConfig.length >= 18 && separacionConfig[17] != null){
            config.NumeroCelularRuta =separacionConfig[17];
        }

        if(separacionConfig.length >= 19 && separacionConfig[18] != null){
            config.FechaDeResolucion = separacionConfig[18];
        }

        if(separacionConfig.length >= 20 && separacionConfig[19] != null){
            config.PermitirRemisionesConValorAcero = separacionConfig[19].equals("1");
        }

        if(separacionConfig.length >= 21 && separacionConfig[20] != null){
            config.PorcentajeReteIva = Float.parseFloat(separacionConfig[20]);
        }

        if(separacionConfig.length >= 22 && separacionConfig[21] != null){
            config.TopeReteIva = Float.parseFloat(separacionConfig[21]);
        }

        if(separacionConfig.length >= 23 && separacionConfig[22] != null){
            config.SiguienteNotaCredito = Integer.parseInt(separacionConfig[22]);
        }

        if(separacionConfig.length >= 24 && separacionConfig[23] != null){
            config.CrearNotaCreditoPorDevolucion = separacionConfig[23].equals("1");
        }

        if(separacionConfig.length >= 25 && separacionConfig[24] != null){
            config.ManejarInventarioRemisiones = separacionConfig[24].equals("1");
        }

        if(separacionConfig.length >= 26 && separacionConfig[25] != null){
            config.VigenciaDeResolucion = separacionConfig[25];
        }

        if(separacionConfig.length >= 27 && separacionConfig[26] != null){
            config.DevolucionAfectaRemision = separacionConfig[26].equals("1");
        }

        return (T) config;
	}

	ResolucionDTO ObtenerConfiguracion() throws Exception {
		return InvocarWS();
	}

}