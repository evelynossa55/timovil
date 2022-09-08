package com.cm.timovil2.proxy;

import org.ksoap2.serialization.SoapObject;

import com.cm.timovil2.dto.ResolucionDTO;

public class ProxyResolucion extends ProxyWS {

    private final String idCliente;
    private final String codigoRuta;

    public ProxyResolucion(String _idCliente, String _codigoRuta) {
        METHOD_NAME = MetodosWeb.OBTENER_DATOS_FACTURACION;
        idCliente = _idCliente;
        codigoRuta = _codigoRuta;
    }

    @Override
    SoapObject GetRequest() {
        SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
        so.addProperty("_codigoRuta", codigoRuta);
        so.addProperty("_idCliente", idCliente);
        return so;
    }

    @Override
    <T> T InvocarWS() throws Exception {

        SoapObject soap = InvokeMethod();
        if (soap == null) {
            throw new Exception("No se pudo cargar la información desde el servidor.");
        }
        if (soap.getProperty(0) == null || !soap.getProperty(0).toString().equals("anyType{}")) {
            throw new Exception(soap.getProperty(0).toString()); //Error reportado desde el servidor
        }

        ResolucionDTO r = new ResolucionDTO();
        r.IdCliente = idCliente;
        r.CodigoRuta = soap.getProperty(1).toString();
        r.NombreRuta = soap.getProperty(2).toString();
        r.FacturaInicial = soap.getProperty(3).toString();
        r.FacturaFinal = soap.getProperty(4).toString();
        r.Resolucion = soap.getProperty(5).toString();
        r.FechaResolucion = soap.getProperty(6).toString();
        r.SiguienteFactura = Integer.parseInt(soap.getProperty(7).toString());
        r.Nit = soap.getProperty(8).toString();
        r.RazonSocial = soap.getProperty(9).toString();
        r.NombreComercial = soap.getProperty(10).toString();
        r.Regimen = soap.getProperty(11).toString();
        r.Direccion = soap.getProperty(12).toString();
        r.Telefono = soap.getProperty(13).toString();
        r.IdResolucion = Integer.parseInt(soap.getProperty(14).toString());
        String numeroCelular = soap.getProperty(15).toString();
        String mostrarListaPrecios = numeroCelular.split(";")[1];
        r.MostrarListaPrecios = Boolean.parseBoolean(mostrarListaPrecios);
        r.PrefijoFacturacion = soap.getProperty(16).toString();
        r.ClaveVendedor = soap.getProperty(17).toString();
        r.ClaveAdmin = soap.getProperty(18).toString();
        r.DevolucionAfectaVenta = Boolean.parseBoolean(soap.getProperty(19).toString());
        r.DevolucionRestaInventario = Boolean.parseBoolean(soap.getProperty(20).toString());
        r.RotacionRestaInventario = Boolean.parseBoolean(soap.getProperty(21).toString());
        r.NumeroCopias = Integer.parseInt(soap.getProperty(22).toString());
        r.DescargarFacturasAuto = Boolean.parseBoolean(soap.getProperty(23).toString());
        r.RedondearValores = Boolean.parseBoolean(soap.getProperty(24).toString());
        // Mostrar lista de precios 24
        r.CodigoBodega = soap.getProperty(26).toString();
        String impresora = soap.getProperty("MACImpresora").toString();
        String[] separacion = impresora.split("\\|");
        if (separacion.length > 0 && !separacion[0].equals("")) {
            r.NombreImpresora = separacion[0];
            r.MACImpresora = separacion[1];
        }
        r.UrlServicioWeb = "http://timovilwebservice.cloudapp.net/MovilServices.asmx";

        String[] configuracionesVarias = numeroCelular.split(";");
        if (configuracionesVarias.length>=3){
            r.SiguienteRemision = Integer.parseInt(configuracionesVarias[2]);
        }else{
            r.SiguienteRemision = 1;
        }
        if (configuracionesVarias.length>=4){
            r.Email = configuracionesVarias[3];
        }else{
            r.Email = "-";
        }

        r.ValorMetaMensual = Double.parseDouble(soap.getProperty("MetaVentas").toString());
        r.ValorVentaMensual = Double.parseDouble(soap.getProperty("VentaMensual").toString());

        //region Facturación POS
        r.IdResolucionPOS = Integer.parseInt(soap.getProperty("IdResolucionPOS").toString());
        r.SiguienteFacturaPOS = Integer.parseInt(soap.getProperty("SiguienteFacturaPOS").toString());
        r.PrefijoFacturacionPOS = soap.getProperty("PrefijoFacturacionPOS").toString();
        r.FacturaInicialPOS = soap.getProperty("InicioFacturacionPOS").toString();
        r.FacturaFinalPOS = soap.getProperty("FinalFacturacionPOS").toString();
        r.ResolucionPOS = soap.getProperty("NroResolucionPOS").toString();
        r.FechaResolucionPOS = soap.getProperty("FechaResolucionPOS").toString();
        //endregion

        //region Facturación Electrónica
        r.EFactura = Boolean.parseBoolean(soap.getProperty("EFactura").toString());
        r.ClaveTecnica = soap.getProperty("ClaveTecnica").toString();
        r.AmbienteEFactura = soap.getProperty("AmbienteEFactura").toString();
        //endregion

        try {
            if (soap.getProperty("DiaCerrado") != null) {
                r.DiaCerrado = Boolean.parseBoolean(soap.getProperty("DiaCerrado").toString());
                if (r.DiaCerrado) {
                    r.FechaCierre = 1L;
                } else {
                    r.FechaCierre = 0L;
                }
            } else {
                r.DiaCerrado = false;
                r.FechaCierre = 0L;
            }

            if (soap.getProperty("HoraLimiteFacturacion") != null) {
                r.HoraLimiteFacturacion = Integer.parseInt(soap.getProperty("HoraLimiteFacturacion").toString());
            } else {
                r.HoraLimiteFacturacion = 0;
            }

            //Si el día está cerrado en el servidor, no hay necesidad de notificarlo al servidor.
            r.CierreNotificadoServidor = r.DiaCerrado;
        }catch (RuntimeException rEx){
            r.DiaCerrado = false;
            r.FechaCierre = 0L;
            r.HoraLimiteFacturacion = 0;
        }

        ProxyConfiguracion proxyConfiguracion = new ProxyConfiguracion(idCliente, codigoRuta, r);
        proxyConfiguracion.ObtenerConfiguracion();

        return (T) r;
    }

    public ResolucionDTO ObtenerResolucion() throws Exception {
        return InvocarWS();
    }
}