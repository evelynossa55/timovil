package com.cm.timovil2.rest;


import android.text.TextUtils;

import com.cm.timovil2.dto.AsesorProgramacionDetalleDTO;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.ConceptoMantenimientoDTO;
import com.cm.timovil2.dto.CuentaCajaDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.DetallePedidoCallcenterDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.EmpleadoDTO;
import com.cm.timovil2.dto.EntregadorDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.GestionComercialDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.dto.MotivoNoVentaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.PedidoCallcenterDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResultadoGestionCasoCallcenterDTO;
import com.cm.timovil2.dto.VehiculoDTO;
import com.cm.timovil2.dto.ControlImeiDTO;
import com.cm.timovil2.dto.wsentities.MAbono;
import com.cm.timovil2.dto.wsentities.MDetUltFac;
import com.cm.timovil2.dto.wsentities.MDetUltRem;
import com.cm.timovil2.dto.wsentities.MDetalleListaPrecios;
import com.cm.timovil2.dto.wsentities.MUltFac;
import com.cm.timovil2.dto.wsentities.MUltRem;
import com.cm.timovil2.dto.wsentities.MValFact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public abstract class SincroHelper {

    private static final String ULTIMA_FACTURA = "UltimaFactura?_idClienteTiMovil=%s&_idCliente=%s";
    private static final String ULTIMA_REMISION = "UltimaRemision?_idClienteTiMovil=%s&_idCliente=%s";

    private static final String INVENTARIO_BODEGA = "InventarioPorBodega?_idCliente=%s&_codigoBodega=%s";
    private static final String LISTA_PRECIOS = "ListaPrecios?_idCliente=%s&codigoRuta=%s&imei=%s";
    private static final String REMISION_ENVIAR = "Remision";
    private static final String FACTURA_ENVIAR = "Factura";
    private static final String FACTURA_ENVIAR_DETALLE = "MonitoreoFacturacion";
    private static final String ANULAR_REMISION = "Remision?numeroRemision=%s&comentario=%s&codigoRuta=%s&idCliente=%s&imei=%s";

    private static final String EMPLEADO = "Empleado?identificacion=%s&idCliente=%s";
    private static final String VEHICULO = "Vehiculo?placa=%s&idCliente=%s";
    private static final String CONCEPTOS_VEHICULO = "ConceptoMantenimiento?idCliente=%s";
    private static final String ENVIAR_VERSION = "Ruta?version=%s&idCliente=%s&codigoRuta=%s";
    private static final String ENVIAR_INFO = "Ruta?info=%s&idCliente=%s&codigoRuta=%s";
    private static final String ENVIAR_FECHA_ACTUALIZACION = "Ruta?idCliente=%s&codigoRuta=%s";
    private static final String FECHA_HORA_SERVER = "FechaHoraServer";
    private static final String ANULAR_FACTURA = "Factura?numeroFactura=%s&comentario=%s&codigoRuta=%s&idCliente=%s&imei=%s";
    private static final String RUTA_ENTREGADOR = "RutaEntregador?idClienteTimovil=%s&codigoRuta=%s";
    private static final String RUTERO_ASESOR = "RuteroAsesor?_idCliente=%s&_codigoRuta=%s";
    private static final String HISTORIAL_GESTION_COMERCIAL = "GestionComercial?_idClienteTimo=%s&_codigoRuta=%s&_idCliente=%s";

    private static final String VERIFICAR_IMEI = "Imei?idCliente=%s&codigoRuta=%s&imei=%s";
    private static final String CONFIG_RUTA_IMEI = "Imei?imei=%s";
    private static final String INGRESAR_NO_VENTA = "NoVenta";
    private static final String MOTIVOS_NO_VENTA = "NoVenta?idCliente=%s&codigoRuta=%s";
    public static final String INGRESAR_GESTION_COMERCIAL = "GestionComercial";
    private static final String ENVIAR_PEDIDOS = "EnviarPedidos?imei=%s&idCliente=%s&codigoRuta=%s";
    private static final String MONITOR_FACTURACION = "ValidacionFactura?idClienteTimovil=%s&numeroFactura=%s&codigoRuta=%s";
    //private static final String ABONO_FACTURA = "AbonoFactura?_codigoRuta=%s&_idCliente=%s&_numeroFactura=%s&_valor=%s&_fecha=%s&_saldo=%s&_identificador=%s&_idFactura=%s&_idCaja=%s&_imei=%s";
    public static final String ABONO_FACTURA = "AbonoFactura"; //POST request para guardar abono de factura
    private static final String CUENTAS_CAJA = "AbonoFactura?_idCliente=%s&_codigoRuta=%s&_imei=%s";

    //CALL CENTER
    private static final String OBTENER_PEDIDOS_CALLCENTER = "PedidoCallCenter?idCliente=%s&codigoRuta=%s";
    private static final String NOTIFICAR_RECEPCIOPN_PEDIDO = "RecibirPedido?idCliente=%s&idCasos=%s&codigoRuta=%s";
    //private static final String CONFIRMAR_PEDIDO = "PedidoCallcenter?idCliente=%s&idCaso=%s&codigoRuta=%s&idMotivoNegativo=%s&comentario=%s&esFactura=%s&numeroDocumento=%s";
    public static final String CONFIRMAR_PEDIDO = "PedidoCallcenter";
    private static final String OBTENER_MOTIVOS_NEGATIVOS = "PedidoCallCenter?idCliente=%s&codigoRuta=%s&imei=%s";
    //FIN CALL CENTER

    //POST
    private static final String GASTO_VEHICULO = "GastosVehiculo?concepto=%s&empleado=%s&vehiculo=%s&valor=%s&descripcion=%s&kilometraje=%s&proximoMantenimiento=%s&nombreVendedor=%s&idCliente=%s&sinNovedad=%s";
    private static final String REGISTRO_UBICACION = "UbicacionRuta?codigoRuta=%s&idClienteTiMo=%s&latitud=%s&longitud=%s&fechaAnsi=%s&imei=%s&gpsActivo=%s&comentarioError=%s";
    private static final String NOTA_CREDITO_DEVOLUCION = "NotaCredito";
    //public static final String ENVIAR_IMAGEN_GESTION_COMERCIAL = "ImagenGestion";

    public static String ObtenerNotificaRecepcionPedidoURL(String _idClienteTimo, String _idCasos, String _codigoRuta) throws Exception {

        try {
            if(_codigoRuta != null
                    && !_codigoRuta.equals("")
                    && _idClienteTimo != null
                    && !_idClienteTimo.equals("")
                    && _idCasos != null
                    && !_idCasos.equals("")){

                return  String.format(NOTIFICAR_RECEPCIOPN_PEDIDO,  _idClienteTimo.trim(), _idCasos,_codigoRuta.trim());

            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }

        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String ObtenerHistorialGestionComercialURL(String _idClienteTimo, String _codigoRuta, int _idCliente) throws Exception {

        try {
            if(_codigoRuta != null && !_codigoRuta.equals("")
                    && _idClienteTimo != null && !_idClienteTimo.equals("")
                    && _idCliente > 0){

                return  String.format(HISTORIAL_GESTION_COMERCIAL,  _idClienteTimo.trim(),_codigoRuta.trim(),_idCliente);

            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }

        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String ObtenerRuteroAsesorURL(String _idCliente, String _codigoRuta) throws Exception {

        try {
            if(_codigoRuta != null && !_codigoRuta.equals("")
                    && _idCliente != null && !_idCliente.equals("")
                    ){

                return  String.format(RUTERO_ASESOR,  _idCliente.trim(),_codigoRuta.trim());

            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }

        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String ObtenerCuentasCajaURL(String _codigoRuta, String _idCliente,
                                                String _imei) throws Exception {
        try {
            if(_codigoRuta != null && !_codigoRuta.equals("")
                    && _idCliente != null && !_idCliente.equals("")
                    && _imei != null && !_imei.equals("")
                    ){

                return  String.format(CUENTAS_CAJA,  _idCliente.trim(),_codigoRuta.trim(),
                        _imei.trim());

            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String ObtenerMotivosNegativosURL(String idCliente, String codigoRuta, String imei) throws Exception {
        try {
            if(idCliente != null && !idCliente.equals("")){
                return  String.format(OBTENER_MOTIVOS_NEGATIVOS, idCliente.trim(), codigoRuta.trim(), imei);
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String ObtenerPedidosCallcenterURL(String idCliente, String codigoRuta) throws Exception {
        try {
            if(idCliente != null && !idCliente.equals("")){
                return  String.format(OBTENER_PEDIDOS_CALLCENTER, idCliente.trim(), codigoRuta.trim());
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String ValidacionFacturaURL(String idCliente, String numeroFactura, String codigoRuta) throws Exception {
        try {
            if(numeroFactura != null && !numeroFactura.equals("")
                    && idCliente != null && !idCliente.equals("")){
                return  String.format(MONITOR_FACTURACION, idCliente.trim(), numeroFactura.trim(), codigoRuta.trim());
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getEnviarPedidosURL(String codigoRuta, String idClienteTiMo, String imei) throws Exception{
        try {
            if( codigoRuta != null && !TextUtils.isEmpty(codigoRuta) &&
                    idClienteTiMo != null && !TextUtils.isEmpty(idClienteTiMo) &&
                    imei != null && !TextUtils.isEmpty(imei)){

                return String.format(ENVIAR_PEDIDOS, imei, idClienteTiMo, codigoRuta);
            }else{
                throw new Exception("No se han especificado todos los datos de ubicación");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static String getRegistroUbicacionURL(String codigoRuta, String idClienteTiMo, String latitud,
                                     String longitud, String fechaAnsi, String imei, boolean gpsActivo,
                                                 String comentarioError) throws Exception{
        try {
            if( codigoRuta != null && !TextUtils.isEmpty(codigoRuta) &&
                    idClienteTiMo != null && !TextUtils.isEmpty(idClienteTiMo) &&
                    latitud != null && !TextUtils.isEmpty(latitud) &&
                    longitud != null && !TextUtils.isEmpty(longitud) &&
                    fechaAnsi != null && !TextUtils.isEmpty(fechaAnsi) &&
                    imei != null && !TextUtils.isEmpty(imei)){

                return String.format(REGISTRO_UBICACION, codigoRuta, idClienteTiMo,
                        latitud, longitud, URLEncoder.encode(fechaAnsi, "UTF-8"), imei, gpsActivo,
                        URLEncoder.encode(comentarioError, "UTF-8"));
            }else{
                throw new Exception("No se han especificado todos los datos de ubicación");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static String getMotivosNoVentaURL(String idCliente, String codigoRuta) throws Exception {
        try {
            if(idCliente != null && !TextUtils.isEmpty(idCliente)
                    && codigoRuta != null && !TextUtils.isEmpty(codigoRuta)){
                return  String.format(MOTIVOS_NO_VENTA, idCliente, codigoRuta);
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getIngresarNoVentaURL() throws Exception {
        try {
            return INGRESAR_NO_VENTA;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getConfigRutaImeiURL(String imei) throws Exception {
        try {
            if(imei != null && !imei.equals("")){
                return  String.format(CONFIG_RUTA_IMEI, imei.trim());
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getVerificarImeiURL(String idCliente, String codigoRuta, String imei) throws Exception {
        try {
            if(codigoRuta != null && !codigoRuta.equals("")
                    && idCliente != null && !idCliente.equals("")
                    && imei != null && !imei.equals("")){
                return  String.format(VERIFICAR_IMEI, idCliente.trim(), codigoRuta.trim(), imei.trim());
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getRutaEntregadorURL(String codigoRuta, String idCliente) throws Exception {
        try {
            if(codigoRuta != null && !codigoRuta.equals("")
                    && idCliente != null && !idCliente.equals("")){
                return  String.format(RUTA_ENTREGADOR, idCliente.trim(), codigoRuta.trim());
            } else {
                throw new Exception("Debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getAnularRemisionURL(String numeroRemision, String comentario,
                                             String codigoRuta, String idCliente, String imei)
                                                throws Exception {
        try {
            if(codigoRuta != null && !codigoRuta.equals("")
                    && idCliente != null && !idCliente.equals("")
                    && numeroRemision != null && !numeroRemision.equals("")
                    && comentario != null && !comentario.equals("")
                    && imei != null && !imei.equals("")){
                return  String.format(ANULAR_REMISION, numeroRemision.trim(), URLEncoder.encode(comentario.trim(), "UTF-8"),
                        codigoRuta.trim(), idCliente.trim(), imei);
            } else {
                throw new Exception("No se ha podido anular la remisión, debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getAnularFacturaURL(String numeroFactura, String comentario,
                                             String codigoRuta, String idCliente,
                                             String imei) throws Exception {
        try {
            if(codigoRuta != null && !codigoRuta.equals("")
                    && idCliente != null && !idCliente.equals("")
                    && numeroFactura != null && !numeroFactura.equals("")
                    && comentario != null && !comentario.equals("")
                    && imei != null && !imei.equals("")){
                return  String.format(ANULAR_FACTURA, numeroFactura.trim(), URLEncoder.encode(comentario.trim(), "UTF-8"),
                        codigoRuta.trim(), idCliente.trim(), imei);
            } else {
                throw new Exception("No se ha podido anular la factura, debe especificar todos los datos requeridos.");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getFechaHoraServerURL() throws Exception {
        try {
            return FECHA_HORA_SERVER;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getEnviarVersionURL(int version, String idCliente, String codigoRuta) throws Exception {
        try {
            if(version > 0
                    && codigoRuta != null
                    && !codigoRuta.equals("")
                    && idCliente != null
                    && !idCliente.equals("")){
                return  String.format(ENVIAR_VERSION, version, idCliente, codigoRuta);
            } else {
                throw new Exception("No se ha podido enviar la versión actual de la aplicación");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getEnviarInfoAndroidURL(String info, String idCliente, String codigoRuta) throws Exception {
        try {
            if(info != null
                    && !info.equals("")
                    && codigoRuta != null
                    && !codigoRuta.equals("")
                    && idCliente != null
                    && !idCliente.equals("")){
                return  String.format(ENVIAR_INFO, URLEncoder.encode(info, "UTF-8"), idCliente, codigoRuta);
            } else {
                throw new Exception("No se ha podido enviar la versión actual de la aplicación");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getEnviarFechaActualizacionURL(String idCliente, String codigoRuta) throws Exception {
        try {
            if(codigoRuta != null
                    && !codigoRuta.equals("")
                    && idCliente != null
                    && !idCliente.equals("")){
                return  String.format(ENVIAR_FECHA_ACTUALIZACION, idCliente, codigoRuta);
            } else {
                throw new Exception("No se ha podido enviar la versión actual de la aplicación");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getGastoURL(int concepto, int empleado, int vehiculo,
                                     float valor, String descripcion, int kilometraje,
                                     int proximoMantenimiento, String nombreVendedor,
                                     String idCliente, boolean sinNovedad) throws Exception{
        try {
            if( concepto > 0 && empleado > 0 && vehiculo > 0 &&
                    !nombreVendedor.equals("") && !idCliente.equals("")){

                if(!sinNovedad && (valor <= 0 || kilometraje <= 0) ){
                    throw new Exception("No se han especificado todos los datos del movimiento");
                }else if (sinNovedad){
                    kilometraje = 0;
                    valor = 0;
                    descripcion = URLEncoder.encode("Sin novedad", "UTF-8");
                }

                return String.format(GASTO_VEHICULO, concepto, empleado, vehiculo, valor, descripcion,
                        kilometraje, proximoMantenimiento, nombreVendedor,
                        idCliente, String.valueOf(sinNovedad));
            }else{
                throw new Exception("No se han especificado todos los datos del movimiento");
            }
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    public static String getRemision_EnviarURL() throws Exception {
        try {
            return  REMISION_ENVIAR;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getFactura_EnviarURL() throws Exception {
        try {
            return  FACTURA_ENVIAR;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getNotaCreditoDevolucion_EnviarURL() throws Exception {
        try {
            return  NOTA_CREDITO_DEVOLUCION;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getFactura_EnviarDetalleURL() throws Exception {
        try {
            return  FACTURA_ENVIAR_DETALLE;
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getListasPreciosURL(String idClienteTM, String codigoRuta,
                                             String imei) throws Exception {
        try {
            if (idClienteTM != null) {
                return String.format(LISTA_PRECIOS, idClienteTM, codigoRuta, imei);
            } else {
                throw new Exception("No se ha especificado la identificación del cliente TM");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getUltimaFacturaURL(String idClienteTM, String idCliente) throws Exception {
        try {
            if (idClienteTM != null && idCliente != null) {
                return String.format(ULTIMA_FACTURA, idClienteTM, idCliente);
            } else {
                throw new Exception("No se ha especificado la identificación del cliente");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getUltimaRemisionURL(String idClienteTM, String idCliente) throws Exception {
        try {
            if (idClienteTM != null && idCliente != null) {
                return String.format(ULTIMA_REMISION, idClienteTM, idCliente);
            } else {
                throw new Exception("No se ha especificado la identificación del cliente");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getInventarioBodega(String idClienteTM, String codigoBodega) throws Exception{
        try {
            if(idClienteTM!=null && codigoBodega!=null){
                return String.format(INVENTARIO_BODEGA, idClienteTM, codigoBodega);
            }else{
                throw new Exception("No se ha especificado la identificación del cliente ni el código de bodega");
            }
        }catch (Exception e){
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static ArrayList<GestionComercialDTO> procesarJsonHistorialGestionComercial(String jsonString) throws Exception{
        ArrayList<GestionComercialDTO> gestiones = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    throw new Exception(jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    gestiones = new ArrayList<>();
                    JSONArray cuentas = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<cuentas.length(); i++){
                        JSONObject cliente = cuentas.getJSONObject(i);
                        if(cliente!=null){
                            GestionComercialDTO _gestion = new GestionComercialDTO();

                            _gestion.IdGestion = cliente.getInt("IdGestion");
                            _gestion.IdCliente = cliente.getInt("IdCliente");
                            _gestion.CodigoRuta = cliente.getString("CodigoRuta");
                            _gestion.FechaHora = cliente.getString("FechaHoraS");
                            _gestion.Comentario = cliente.getString("Comentario");
                            _gestion.Latitud = cliente.getString("Latitud");
                            _gestion.Longitud = cliente.getString("Longitud");
                            _gestion.Contacto = cliente.getString("Contacto");
                            _gestion.TelContacto = cliente.getString("TelefonoContacto");
                            _gestion.Estado = cliente.getString("Estado");

                            _gestion.Sincronizada = true;
                            _gestion.EncodedImages = "";
                            gestiones.add(_gestion);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return gestiones;
    }

    public static ArrayList<ClienteDTO> procesarJsonAsesor(String jsonString) throws Exception{
        ArrayList<ClienteDTO> clientes = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    throw new Exception(jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    clientes = new ArrayList<>();
                    JSONArray cuentas = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<cuentas.length(); i++){
                        JSONObject cliente = cuentas.getJSONObject(i);
                        if(cliente!=null){
                            ClienteDTO _cliente = new ClienteDTO();

                            _cliente.IdCliente = cliente.getInt("IdCliente");
                            _cliente.Identificacion = cliente.getString("Identificacion");
                            _cliente.RazonSocial = cliente.getString("RazonSocial");
                            _cliente.NombreComercial = cliente.getString("Negocio");
                            _cliente.Direccion = cliente.getString("Direccion");
                            _cliente.Telefono1 = !cliente.isNull("Telefono")?
                                    cliente.getString("Telefono"):"";
                            _cliente.Telefono2 = cliente.getString("Telefono2");
                            _cliente.Dia = cliente.getInt("DiaRuta");

                            _cliente.Orden = i+1;

                            _cliente.ReteFuente = cliente.getBoolean("Retefuente");
                            _cliente.RetenedorCREE = cliente.getBoolean("CREE");
                            _cliente.PorcentajeCree = cliente.getDouble("PorcentajeCREE");
                            _cliente.Atendido = "";
                            _cliente.ListaPrecios = !cliente.isNull("ListaPrecios")?
                                    cliente.getInt("ListaPrecios"):-1;
                            _cliente.ValorVentasMes = "";

                            _cliente.CarteraPendiente = !cliente.isNull("ComentariosCartera")?
                                    cliente.getString("ComentariosCartera"):"";

                            _cliente.Remisiones = "";
                            _cliente.Remision = cliente.getBoolean("Remision");
                            _cliente.Credito = cliente.getBoolean("Credito");
                            _cliente.IdListaPrecios = !cliente.isNull("IdListaPrecio")?
                                    cliente.getInt("IdListaPrecio"):-1;
                            _cliente.FormaPagoFlexible = cliente.getBoolean("FormaPagoFlexible");
                            _cliente.DireccionEntrega = cliente.getString("DireccionEntrega");
                            _cliente.ExentoIva = cliente.getBoolean("ExentoIva");
                            _cliente.ReteIva = cliente.getBoolean("ReteIva");
                            if(!cliente.isNull("ObligatorioCodigoBarras"))
                                _cliente.ObligatorioCodigoBarra = cliente.getBoolean("ObligatorioCodigoBarras");
                            _cliente.VecesAtendido = 0;

                            if(cliente.has("ProgramacionAsesor") && !cliente.isNull("ProgramacionAsesor")){
                                JSONArray programacion = cliente.getJSONArray("ProgramacionAsesor");
                                _cliente.programacionAsesor = new ArrayList<>();
                                for(int j=0; j<programacion.length(); j++) {
                                    JSONObject j_programacion = programacion.getJSONObject(j);

                                    AsesorProgramacionDetalleDTO programacion_
                                            = new AsesorProgramacionDetalleDTO();
                                    programacion_.IdAsesorProgramacionDetalle =
                                            j_programacion.getInt("IdAsesorProgramacionDetalle");
                                    programacion_.IdAsesorProgramacion =
                                            j_programacion.getInt("IdAsesorProgramacion");
                                    programacion_.Fecha = j_programacion.getString("Fecha");
                                    programacion_.Dia = j_programacion.getInt("Dia");
                                    programacion_.IdCliente = _cliente.IdCliente;
                                    _cliente.programacionAsesor.add(programacion_);
                                }

                            }
                            clientes.add(_cliente);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return clientes;
    }

    public static ArrayList<CuentaCajaDTO> procesarJsonCuentasCaja(String jsonString) throws Exception{
        ArrayList<CuentaCajaDTO> cuentasCaja = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    throw new Exception(jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    cuentasCaja = new ArrayList<>();
                    JSONArray cuentas = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<cuentas.length(); i++){
                        JSONObject cuenta = cuentas.getJSONObject(i);
                        if(cuenta!=null){
                            CuentaCajaDTO _cuenta = new CuentaCajaDTO();
                            _cuenta.IdCuentaCaja = cuenta.getInt("IdCuentaCaja");
                            _cuenta.Nombre = cuenta.getString("Nombre");
                            _cuenta.NumeroCuenta = cuenta.getString("NumeroCuenta");
                            cuentasCaja.add(_cuenta);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return cuentasCaja;
    }

    public static ArrayList<PedidoCallcenterDTO> procesarJsonPedidoCallcenter(String jsonString) throws Exception {
        ArrayList<PedidoCallcenterDTO> pedidos = null;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null")) {

                    throw new Exception(jsonObject.getString("MensajeError"));

                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {

                    pedidos = new ArrayList<>();
                    JSONArray jsonPedidos = jsonObject.getJSONArray("Resultado");

                    for (int a=0; a<jsonPedidos.length(); a++) {

                        JSONObject jsonPedido = jsonPedidos.getJSONObject(a);
                        PedidoCallcenterDTO pedido = new PedidoCallcenterDTO();

                        pedido.IdCaso = jsonPedido.getInt("IdCaso");
                        pedido.IdCliente = jsonPedido.getInt("IdCliente");
                        pedido.Comentario = jsonPedido.getString("Comentario");
                        pedido.FechaSolicitada = jsonPedido.getString("FechaSolicitada");

                        ArrayList<DetallePedidoCallcenterDTO> detalle_pedido = new ArrayList<>();

                        if(jsonPedido.has("Detalle") && !jsonPedido.isNull("Detalle")){
                            JSONArray productos = jsonPedido.getJSONArray("Detalle");

                            for (int i=0; i<productos.length(); i++){
                                JSONObject producto = productos.getJSONObject(i);
                                if(producto!=null){
                                    DetallePedidoCallcenterDTO detalle =
                                            new DetallePedidoCallcenterDTO();

                                    detalle.IdProducto = producto.getInt("IdProducto");
                                    detalle.Cantidad = producto.getInt("Cantidad");
                                    detalle_pedido.add(detalle);
                                }
                            }
                        }
                        pedido.Detalle = detalle_pedido;
                        pedidos.add(pedido);
                    }

                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return pedidos;
    }

    public static ArrayList<MAbono> procesarJsonAbonosBackUp(String jsonAbonos) throws Exception {
        ArrayList<MAbono> abonos = new ArrayList<>();
        try{
            if (jsonAbonos != null && !jsonAbonos.equals("")) {
                JSONArray json = new JSONArray(jsonAbonos);

                for(int i=0; i<json.length(); i++){
                    JSONObject abono = json.getJSONObject(i);
                    if(abono!=null){

                        MAbono abo = new MAbono();
                        abo._Id = abono.getInt("_Id");
                        abo.Identificador = abono.getString("Identificador");
                        abo.IdFactura = abono.getString("IdFactura");
                        abo.NumeroFactura = abono.getString("NumeroFactura");
                        abo.Fecha = abono.getString("Fecha");
                        abo.Valor = (float)abono.getDouble("Valor");
                        abo.Saldo = (float)abono.getDouble("Saldo");
                        abo.Sincronizado = abono.getBoolean("Sincronizado");
                        abo.DiaCreacion = abono.getString("DiaCreacion");
                        abo.IdCuentaCaja = abono.getInt("IdCuentaCaja");
                        abo.FechaCreacion = abono.getLong("FechaCreacion");

                        abonos.add(abo);
                    }
                }
            }
        }catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return abonos;
    }

    public static ArrayList<FacturaDTO> procesarJsonFacturasBackUp(String jsonFacturas) throws Exception {
        ArrayList<FacturaDTO> facturas = new ArrayList<>();
        try{
            if (jsonFacturas != null && !jsonFacturas.equals("")) {
                JSONArray json = new JSONArray(jsonFacturas);

                for(int i=0; i<json.length(); i++){
                    JSONObject factura = json.getJSONObject(i);
                    if(factura!=null){
                        FacturaDTO fac = new FacturaDTO();
                        fac.NumeroFactura = factura.getString("NumeroFactura");
                        fac.FechaHora = factura.getLong("FechaHora");
                        fac.FormaPago = factura.getString("FormaPago");
                        fac.IdCliente = factura.getInt("IdCliente");
                        fac.Identificacion = factura.getString("Identificacion");
                        fac.RazonSocial = factura.getString("RazonSocial");
                        fac.Negocio = factura.getString("Negocio");
                        fac.Direccion = factura.getString("Direccion");
                        fac.Telefono = factura.getString("Telefono");
                        fac.Subtotal = Float.parseFloat(factura.get("Subtotal").toString());
                        fac.Descuento = Float.parseFloat(factura.get("Descuento").toString());
                        fac.Retefuente = Float.parseFloat(factura.get("Retefuente").toString());
                        fac.PorcentajeRetefuente = Float.parseFloat(factura.get("PorcentajeRetefuente").toString());
                        fac.Iva = Float.parseFloat(factura.get("Iva").toString());
                        fac.Ica = Float.parseFloat(factura.get("Ica").toString());
                        fac.Total = Float.parseFloat(factura.get("Total").toString());
                        fac.Sincronizada = factura.getInt("Sincronizada") == 1;
                        fac.Anulada = factura.getInt("Anulada") == 1;
                        fac.PendienteAnulacion = factura.getInt("PendienteAnulacion") == 1;
                        fac.EfectivoPagado = Float.parseFloat(factura.get("Efectivo").toString());
                        fac.Devolucion = factura.getInt("Devolucion");
                        fac.Latitud = factura.getString("Latitud");
                        fac.Longitud = factura.getString("Longitud");
                        fac.CREE = factura.getDouble("CREE");
                        fac.PorcentajeRetefuente = Float.parseFloat(factura.get("PorcentajeCREE").toString());
                        fac.Comentario = factura.getString("Comentario");
                        fac.IpoConsumo = Float.parseFloat(factura.get("IpoConsumo").toString());
                        fac.TipoDocumento = factura.getString("TipoDocumento");
                        fac.CodigoBodega = factura.getString("codigoBodega");
                        fac.NumeroPedido = factura.getString("NumeroPedido");
                        fac.ComentarioAnulacion = factura.getString("ComentarioAnulacion");
                        fac.IdEmpleadoEntregador = factura.getInt("IdEmpleadoEntregador");
                        fac.IdResolucion = factura.getInt("IdResolucion");
                        fac.Revisada = factura.getInt("Revisada") == 1;
                        fac.IsPedidoCallcenter = factura.getInt("IsPedidoCallcenter") == 1;
                        fac.FechaHoraVencimiento = factura.getLong("FechaHoraVencimiento");
                        fac.IdPedido = factura.getInt("IdPedido");
                        fac.IdCaso = factura.getInt("IdCaso");
                        fac.ValorDevolucion = Float.parseFloat(factura.get("ValorDevolucion").toString());
                        fac.ReteIva = Float.parseFloat(factura.get("ReteIva").toString());
                        fac.FacturaPos = factura.has("FacturaPos") ? factura.getInt("FacturaPos") == 1 : fac.IdCliente == -1;

                        if(factura.has("DetalleFactura") && !factura.isNull("DetalleFactura")){
                            JSONArray detalleFactura = factura.getJSONArray("DetalleFactura");
                            fac.DetalleFactura = new ArrayList<>();

                            for (int d=0; d<detalleFactura.length(); d++) {
                                JSONObject detalle = detalleFactura.getJSONObject(d);
                                if (detalle != null) {
                                    DetalleFacturaDTO det = new DetalleFacturaDTO();
                                    det.NumeroFactura = detalle.getString("NumeroFactura");
                                    det.IdProducto = detalle.getInt("IdProducto");
                                    det.Codigo = detalle.getString("Codigo");
                                    det.Nombre = detalle.getString("Nombre");
                                    det.Cantidad = detalle.getInt("Cantidad");
                                    det.Devolucion = detalle.getInt("Devolucion");
                                    det.Rotacion = detalle.getInt("Rotacion");
                                    det.ValorUnitario = Float.parseFloat(detalle.get("ValorUnitario").toString());
                                    det.Subtotal = Float.parseFloat(detalle.get("Subtotal").toString());
                                    det.Descuento = Float.parseFloat(detalle.get("Descuento").toString());
                                    det.PorcentajeDescuento = Float.parseFloat(detalle.get("PorcentajeDescuento").toString());
                                    det.Iva = Float.parseFloat(detalle.get("Iva").toString());
                                    det.PorcentajeIva = Float.parseFloat(detalle.get("PorcentajeIva").toString());
                                    det.Total = Float.parseFloat(detalle.get("Total").toString());
                                    det.IpoConsumo = Float.parseFloat(detalle.get("IpoConsumo").toString());
                                    det.ValorDevolucion = Float.parseFloat(detalle.get("ValorDevolucion").toString());
                                    fac.DetalleFactura.add(det);
                                }
                            }
                        }

                        facturas.add(fac);
                    }
                }
            }
        }catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return facturas;
    }

    public static ArrayList<RemisionDTO> procesarJsonRemisionesBackUp(String jsonRemisiones) throws Exception {
        ArrayList<RemisionDTO> remisiones = new ArrayList<>();
        try{
            if (jsonRemisiones != null && !jsonRemisiones.equals("")) {
                JSONArray json = new JSONArray(jsonRemisiones);

                for(int i=0; i<json.length(); i++){
                    JSONObject remision = json.getJSONObject(i);
                    if(remision!=null){
                        RemisionDTO rem = new RemisionDTO();

                        rem.NumeroRemision = remision.getString("NumeroRemision");
                        rem.Fecha = remision.getLong("Fecha");
                        rem.IdCliente = remision.getInt("IdCliente");
                        rem.CodigoRuta = remision.getString("CodigoRuta");
                        rem.NombreRuta = remision.getString("NombreRuta");
                        rem.Subtotal = remision.getDouble("Subtotal");
                        rem.Iva = remision.getDouble("Iva");
                        rem.Total = remision.getDouble("Total");
                        rem.RazonSocialCliente = remision.getString("RazonSocialCliente");
                        rem.IdentificacionCliente = remision.getString("IdentificacionCliente");
                        rem.TelefonoCliente = remision.getString("TelefonoCliente");
                        rem.DireccionCliente = remision.getString("DireccionCliente");
                        rem.Anulada = remision.getInt("Anulada") == 1;
                        rem.FechaCreacion = remision.getLong("FechaCreacion");
                        rem.Latitud = remision.getString("Latitud");
                        rem.Longitud = remision.getString("Longitud");
                        rem.PendienteAnulacion = remision.getInt("PendienteAnulacion") == 1;
                        rem.Comentario = remision.getString("Comentario");
                        rem.Sincronizada = remision.getBoolean("Sincronizada");
                        rem.CodigoBodega = remision.getString("codigoBodega");
                        rem.NumeroPedido = remision.getString("NumeroPedido");
                        rem.ComentarioAnulacion = remision.getString("ComentarioAnulacion");
                        rem.Descuento = remision.getDouble("Descuento");
                        rem.FormaPago = remision.getString("FormaPago");
                        rem.IsPedidoCallcenter = remision.getBoolean("IsPedidoCallcenter");
                        rem.Devolucion = remision.getInt("Devolucion");
                        rem.Rotacion = remision.getInt("Rotacion");
                        rem.IdPedido = remision.getInt("IdPedido");
                        rem.IdCaso = remision.getInt("IdCaso");
                        rem.ValorDevolucion = Float.parseFloat(remision.get("ValorDevolucion").toString());
                        rem.Ipoconsumo = Float.parseFloat(remision.get("Ipoconsumo").toString());
                        rem.RetefuenteDevolucion = Long.parseLong(remision.get("RetefuenteDevolucion").toString());
                        rem.ValorReteIvaDevolucion = Long.parseLong(remision.get("ValorReteIvaDevolucion").toString());
                        rem.ValorReteIva = Long.parseLong(remision.get("ValorReteIva").toString());

                        if(remision.has("DetalleRemision") && !remision.isNull("DetalleRemision")){
                            JSONArray detalleRemision = remision.getJSONArray("DetalleRemision");
                            rem.DetalleRemision = new ArrayList<>();

                            for (int d=0; d < detalleRemision.length(); d++) {
                                JSONObject detalle = detalleRemision.getJSONObject(d);
                                if (detalle != null) {
                                    DetalleRemisionDTO det = new DetalleRemisionDTO();
                                    det.NumeroRemision = detalle.getString("NumeroRemision");
                                    det.IdProducto = detalle.getInt("IdProducto");
                                    det.NombreProducto = detalle.getString("NombreProducto");
                                    det.Cantidad = detalle.getInt("Cantidad");
                                    det.ValorUnitario = detalle.getDouble("ValorUnitario");
                                    det.Subtotal = detalle.getDouble("Subtotal");
                                    det.Total = detalle.getDouble("Total");
                                    det.Iva = detalle.getDouble("Iva");
                                    det.FechaCreacion = detalle.getLong("FechaCreacion");
                                    det.PorcentajeIva = detalle.getDouble("PorcentajeIva");
                                    det.Codigo = detalle.getString("Codigo");
                                    det.Descuento = detalle.getDouble("Descuento");
                                    det.PorcentajeDescuento = detalle.getDouble("PorcentajeDescuento");
                                    det.Devolucion = detalle.getInt("Devolucion");
                                    det.Rotacion = detalle.getInt("Rotacion");
                                    det.ValorDevolucion = Float.parseFloat(detalle.get("ValorDevolucion").toString());
                                    det.Ipoconsumo = Float.parseFloat(detalle.get("Ipoconsumo").toString());
                                    det.IvaDevolucion = Float.parseFloat(detalle.get("IvaDevolucion").toString());
                                    rem.DetalleRemision.add(det);
                                }
                            }
                        }

                        remisiones.add(rem);
                    }
                }
            }
        }catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return remisiones;
    }

    public static ArrayList<NotaCreditoFacturaDTO> procesarJsonNotasCreditoFacturaDevolucionBackUp(String jsonNotas) throws Exception {
        ArrayList<NotaCreditoFacturaDTO> notas = new ArrayList<>();
        try{
            if (jsonNotas != null && !jsonNotas.equals("")) {
                JSONArray json = new JSONArray(jsonNotas);

                for(int i=0; i<json.length(); i++){
                    JSONObject nota = json.getJSONObject(i);
                    if(nota != null){

                        NotaCreditoFacturaDTO notaCreditoFacturaDTO = new NotaCreditoFacturaDTO();
                        notaCreditoFacturaDTO.IdNotaCreditoFactura = nota.getInt("IdNotaCreditoFactura");
                        notaCreditoFacturaDTO.NumeroDocumento = nota.getString("NumeroDocumento");
                        notaCreditoFacturaDTO.NumeroFactura = nota.getString("NumeroFactura");
                        notaCreditoFacturaDTO.Fecha = nota.getLong("Fecha");
                        notaCreditoFacturaDTO.Subtotal = nota.getDouble("Subtotal");
                        notaCreditoFacturaDTO.Descuento = nota.getDouble("Descuento");
                        notaCreditoFacturaDTO.Ipoconsumo = nota.getDouble("Ipoconsumo");
                        notaCreditoFacturaDTO.Iva5 = nota.getDouble("Iva5");
                        notaCreditoFacturaDTO.Iva19 = nota.getDouble("Iva19");
                        notaCreditoFacturaDTO.Iva = nota.getDouble("Iva");
                        notaCreditoFacturaDTO.Valor = Float.parseFloat(nota.get("Valor").toString());
                        notaCreditoFacturaDTO.CodigoBodega = nota.getString("CodigoBodega");
                        notaCreditoFacturaDTO.Sincronizada = nota.getBoolean("Sincronizada");
                        notaCreditoFacturaDTO.Impresa = nota.getBoolean("Impresa");

                        if(nota.has("DetalleNotaCreditoFactura") && !nota.isNull("DetalleNotaCreditoFactura")){
                            JSONArray detalleNota = nota.getJSONArray("DetalleNotaCreditoFactura");
                            notaCreditoFacturaDTO.DetalleNotaCreditoFactura = new ArrayList<>();

                            for (int d=0; d < detalleNota.length(); d++) {
                                JSONObject detalle = detalleNota.getJSONObject(d);
                                if (detalle != null) {
                                    DetalleNotaCreditoFacturaDTO det = new DetalleNotaCreditoFacturaDTO();

                                    det.IdDetalleNotaCreditoFactura = detalle.getInt("IdDetalleNotaCreditoFactura");
                                    det.NumeroDocumento = detalle.getString("NumeroDocumento");
                                    det.IdProducto = detalle.getInt("IdProducto");
                                    det.Cantidad = detalle.getInt("Cantidad");
                                    det.Subtotal = detalle.getDouble("Subtotal");
                                    det.Descuento = detalle.getDouble("Descuento");
                                    det.Ipoconsumo = detalle.getDouble("Ipoconsumo");
                                    det.Iva5 = detalle.getDouble("Iva5");
                                    det.Iva19 = detalle.getDouble("Iva19");
                                    det.Iva = detalle.getDouble("Iva");
                                    det.Valor = detalle.getDouble("Valor");
                                    det.Codigo = detalle.getString("Codigo");
                                    det.Nombre = detalle.getString("Nombre");

                                    notaCreditoFacturaDTO.DetalleNotaCreditoFactura.add(det);
                                }
                            }
                        }

                        notas.add(notaCreditoFacturaDTO);
                    }
                }
            }
        }catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return notas;
    }

    public static ArrayList<GuardarMotivoNoVentaDTO> procesarJsonNoVentasBackUp(String jsonNoVentas) throws Exception {
        ArrayList<GuardarMotivoNoVentaDTO> noVentas = new ArrayList<>();
        try{
            if (jsonNoVentas != null && !jsonNoVentas.equals("")) {
                JSONArray json = new JSONArray(jsonNoVentas);

                for(int i=0; i<json.length(); i++){
                    JSONObject noVenta = json.getJSONObject(i);
                    if(noVenta != null){

                        GuardarMotivoNoVentaDTO guardarMotivoNoVentaDTO = new GuardarMotivoNoVentaDTO();
                        guardarMotivoNoVentaDTO.IdMotivoNoVenta = noVenta.getInt("IdMotivoNoVenta");
                        guardarMotivoNoVentaDTO.IdMotivo = noVenta.getInt("IdMotivo");
                        guardarMotivoNoVentaDTO.IdCliente = noVenta.getInt("IdCliente");
                        guardarMotivoNoVentaDTO.CodigoRuta = noVenta.getString("CodigoRuta");
                        guardarMotivoNoVentaDTO.Descripcion = noVenta.getString("Descripcion");
                        guardarMotivoNoVentaDTO.Fecha = noVenta.getString("Fecha");
                        guardarMotivoNoVentaDTO.Fecha_long = noVenta.getLong("Fecha_long");
                        guardarMotivoNoVentaDTO.Latitud = noVenta.getString("Latitud");
                        guardarMotivoNoVentaDTO.Longitud = noVenta.getString("Longitud");
                        guardarMotivoNoVentaDTO.IdClienteTimovil = noVenta.getString("IdClienteTimovil");
                        guardarMotivoNoVentaDTO.esOtroMotivo = noVenta.getBoolean("esOtroMotivo");
                        guardarMotivoNoVentaDTO.Motivo = noVenta.getString("Motivo");
                        guardarMotivoNoVentaDTO.Sincronizada = noVenta.getBoolean("Sincronizada");

                        noVentas.add(guardarMotivoNoVentaDTO);
                    }
                }
            }
        }catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return noVentas;
    }

    public static ArrayList<GuardarMotivoNoVentaPedidoDTO> procesarJsonNoVentasPedidoBackUp(String jsonNoVentasPedidos) throws Exception {
        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPedidos = new ArrayList<>();
        try{
            if (jsonNoVentasPedidos != null && !jsonNoVentasPedidos.equals("")) {
                JSONArray json = new JSONArray(jsonNoVentasPedidos);

                for(int i=0; i<json.length(); i++){
                    JSONObject noVentaPedido = json.getJSONObject(i);
                    if(noVentaPedido != null){

                        GuardarMotivoNoVentaPedidoDTO guardarMotivoNoVentaPedidoDTO = new GuardarMotivoNoVentaPedidoDTO();
                        guardarMotivoNoVentaPedidoDTO.IdMotivoNoVentaPedido = noVentaPedido.getInt("IdMotivoNoVentaPedido");
                        guardarMotivoNoVentaPedidoDTO.IdClienteTimovil = noVentaPedido.getString("IdClienteTimovil");
                        guardarMotivoNoVentaPedidoDTO.CodigoRuta = noVentaPedido.getString("CodigoRuta");
                        guardarMotivoNoVentaPedidoDTO.IdResultadoGestion = noVentaPedido.getInt("IdResultadoGestion");
                        guardarMotivoNoVentaPedidoDTO.Descripcion = noVentaPedido.getString("Descripcion");
                        guardarMotivoNoVentaPedidoDTO.IdCaso = noVentaPedido.getInt("IdCaso");
                        guardarMotivoNoVentaPedidoDTO.Sincronizada = noVentaPedido.getBoolean("Sincronizada");
                        guardarMotivoNoVentaPedidoDTO.Fecha = noVentaPedido.getLong("Fecha");
                        guardarMotivoNoVentaPedidoDTO.IdCliente = noVentaPedido.getString("IdCliente");

                        noVentasPedidos.add(guardarMotivoNoVentaPedidoDTO);
                    }
                }
            }
        }catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return noVentasPedidos;
    }

    public static MUltFac procesarJsonFactura(String jsonString) throws Exception {
        MUltFac ultimaFactura = null;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null")) {
                    throw new Exception(jsonObject.getString("MensajeError"));
                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {
                    ultimaFactura = new MUltFac();
                    JSONObject jsonFactura = jsonObject.getJSONObject("Resultado");
                    ultimaFactura.NumeroFactura = jsonFactura.getString("NumeroFactura");
                    ultimaFactura.Fecha = jsonFactura.getString("Fecha");
                    ArrayList<MDetUltFac> detUltFacList = new ArrayList<>();
                    if(jsonFactura.has("Detalle") && !jsonFactura.isNull("Detalle")){
                        JSONArray productos = jsonFactura.getJSONArray("Detalle");
                        for (int i=0; i<productos.length(); i++){
                            JSONObject producto = productos.getJSONObject(i);
                            if(producto!=null){
                                MDetUltFac detalle = new MDetUltFac();
                                detalle.IdProd = producto.getInt("IdProd");
                                detalle.Cant = producto.getInt("Cant");
                                detalle.CodigoProducto = producto.getString("CodigoProducto");
                                detalle.NombreProducto = producto.getString("NombreProducto");
                                detalle.ValorUnitario = producto.getDouble("ValorUnitario");
                                detalle.PorcentajeIva = producto.getDouble("PorcentajeIva");
                                detalle.Ipoconsumo = producto.getDouble("Ipoconsumo");
                                detalle.PorcentajeDescuento = producto.getDouble("PorcentajeDescuento");
                                detalle.DevolucionesAnteriores = producto.getInt("DevolucionesAnteriores");
                                detalle.IdDetalle = producto.getInt("IdDetalle");
                                detUltFacList.add(detalle);
                            }
                        }
                    }
                    ultimaFactura.Detalle = detUltFacList;
                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return ultimaFactura;
    }

/**
 * Creado por Juan Sebastian Arenas Borja 9/9/2021*/
    public static MUltRem procesarJsonRemision(String jsonString) throws Exception {
        MUltRem ultimaRemision = null;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null")) {
                    throw new Exception(jsonObject.getString("MensajeError"));
                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {
                    ultimaRemision = new MUltRem();
                    JSONObject jsonRemision = jsonObject.getJSONObject("Resultado");
                    ultimaRemision.NumeroRemision = jsonRemision.getString("NumeroRemision");
                    ultimaRemision.Fecha = jsonRemision.getString("Fecha");
                    ArrayList<MDetUltRem> detUltRemList = new ArrayList<>();
                    if(jsonRemision.has("Detalle") && !jsonRemision.isNull("Detalle")){
                        JSONArray productos = jsonRemision.getJSONArray("Detalle");
                        for (int i=0; i<productos.length(); i++){
                            JSONObject producto = productos.getJSONObject(i);
                            if(producto!=null){
                                MDetUltRem detalle = new MDetUltRem();
                                detalle.IdProd = producto.getInt("IdProd");
                                detalle.Cant = producto.getInt("Cant");
                                detalle.CodigoProducto = producto.getString("CodigoProducto");
                                detalle.NombreProducto = producto.getString("NombreProducto");
                                detalle.ValorUnitario = producto.getDouble("ValorUnitario");
                                detalle.PorcentajeIva = producto.getDouble("PorcentajeIva");
                                detalle.Ipoconsumo = producto.getDouble("Ipoconsumo");
                                detalle.PorcentajeDescuento = producto.getDouble("PorcentajeDescuento");
                                detalle.DevolucionesAnteriores = producto.getInt("DevolucionesAnteriores");
                                detalle.IdDetalle = producto.getInt("IdDetalle");
                                detUltRemList.add(detalle);
                            }
                        }
                    }
                    ultimaRemision.Detalle = detUltRemList;
                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return ultimaRemision;
    }




    public static ArrayList<ProductoDTO> procesarJsonInventario(String jsonString, String codigoBodega) throws Exception{
        ArrayList<ProductoDTO> productos = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){
                    throw new Exception(jsonObject.getString("MensajeError"));
                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){
                    productos = new ArrayList<>();
                    JSONArray products = jsonObject.getJSONArray("Resultado");
                    for(int i=0; i<products.length(); i++){
                        JSONObject product = products.getJSONObject(i);
                        if(product!=null){
                            ProductoDTO prod = new ProductoDTO();
                            prod.IdProducto = product.getInt("IdProd");
                            prod.Codigo = product.getString("Cod");
                            prod.Nombre = product.getString("Nom");
                            prod.IpoConsumo = Float.parseFloat(product.get("IpoConsumo").toString());
                            prod.PorcentajeIva = Float.parseFloat(product.get("PIva").toString());
                            prod.StockInicial = product.getInt("Stock");
                            prod.Precio1 = Float.parseFloat(product.get("Precio").toString());
                            prod.Precio2 = Float.parseFloat(product.get("Precio2").toString());
                            prod.Precio3 = Float.parseFloat(product.get("Precio3").toString());
                            prod.Precio4 = Float.parseFloat(product.get("Precio4").toString());
                            prod.Precio5 = Float.parseFloat(product.get("Precio5").toString());
                            prod.Precio6 = Float.parseFloat(product.get("Precio6").toString());
                            prod.Precio7 = Float.parseFloat(product.get("Precio7").toString());
                            prod.Precio8 = Float.parseFloat(product.get("Precio8").toString());
                            prod.Precio9 = Float.parseFloat(product.get("Precio9").toString());
                            prod.Precio10 = Float.parseFloat(product.get("Precio10").toString());
                            prod.Precio11 = Float.parseFloat(product.get("Precio11").toString());
                            prod.Precio12 = Float.parseFloat(product.get("Precio12").toString());
                            prod.CodigoBodega = codigoBodega;
                            productos.add(prod);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return productos;
    }

    public static ArrayList<MDetalleListaPrecios> procesarJsonListaPrecios(String jsonString) throws Exception{
        ArrayList<MDetalleListaPrecios> listaPrecios = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    throw new Exception(jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    listaPrecios = new ArrayList<>();
                    JSONArray listaJson = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<listaJson.length(); i++){
                        JSONObject product = listaJson.getJSONObject(i);
                        if(product!=null){
                            MDetalleListaPrecios detalleListaPrecios = new MDetalleListaPrecios();
                            detalleListaPrecios.IdDetalleListaPrecios = product.getInt("IdDetalleListaPrecios");
                            detalleListaPrecios.IdListaPrecios = product.getInt("IdListaPrecios");
                            detalleListaPrecios.IdProducto = product.getInt("IdProducto");
                            detalleListaPrecios.Precio = product.getInt("Precio");
                            detalleListaPrecios.NombreListaPrecios = product.getString("NombreListaPrecios");
                            listaPrecios.add(detalleListaPrecios);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return listaPrecios;
    }

    public static ArrayList<EntregadorDTO> procesarJsonEntregador(String jsonString) throws Exception{
        ArrayList<EntregadorDTO> listaEntregadores = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    listaEntregadores = new ArrayList<>();

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    listaEntregadores = new ArrayList<>();
                    JSONArray listaJson = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<listaJson.length(); i++){
                        JSONObject product = listaJson.getJSONObject(i);
                        if(product!=null){
                            EntregadorDTO entregadorDTO = new EntregadorDTO();
                            entregadorDTO.IdEmpleado = product.getInt("IdEmpleado");
                            entregadorDTO.NombreCompleto = product.getString("NombreCompleto");
                            listaEntregadores.add(entregadorDTO);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return listaEntregadores;
    }

    public static ArrayList<MotivoNoVentaDTO> procesarJsonMotivosNoVenta(String jsonString) throws Exception{
        ArrayList<MotivoNoVentaDTO> listaMotivos = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    listaMotivos = new ArrayList<>();

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    listaMotivos = new ArrayList<>();
                    JSONArray listaJson = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<listaJson.length(); i++){
                        JSONObject product = listaJson.getJSONObject(i);
                        if(product!=null){
                            MotivoNoVentaDTO motivoDTO = new MotivoNoVentaDTO();
                            motivoDTO.IdMotivo = product.getInt("IdMotivo");
                            motivoDTO.Descripcion = product.getString("Descripcion");
                            listaMotivos.add(motivoDTO);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return listaMotivos;
    }

    public static ArrayList<ResultadoGestionCasoCallcenterDTO> procesarJsonResultadosGestionPedidosCallcenter(String jsonString) throws Exception{
        ArrayList<ResultadoGestionCasoCallcenterDTO> listaMotivos = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    listaMotivos = new ArrayList<>();

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    listaMotivos = new ArrayList<>();
                    JSONArray listaJson = jsonObject.getJSONArray("Resultado");

                    for(int i=0; i<listaJson.length(); i++){
                        JSONObject product = listaJson.getJSONObject(i);
                        if(product!=null){
                            ResultadoGestionCasoCallcenterDTO motivoDTO = new ResultadoGestionCasoCallcenterDTO();
                            motivoDTO.IdResultadoGestion = product.getInt("IdResultadoGestion");
                            motivoDTO.Nombre = product.getString("Nombre");
                            listaMotivos.add(motivoDTO);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return listaMotivos;
    }

    public static String procesarRemisionJson(String jsonString) throws Exception{
        String result = "NO";
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")){

                    throw new Exception(jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    result = jsonObject.getString("Resultado").trim();

                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return result;
    }

    public static String procesarOkJson(String jsonString) throws Exception{
        String result = "NO";
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")
                        && !jsonObject.getString("MensajeError").equals("")){
                    throw new Exception(jsonObject.getString("MensajeError"));
                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){
                    result = jsonObject.getString("Resultado").trim();
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return result;
    }

    //----------------------------Gastos de vehículo-----------------------------------

    public static String getEmpleadoURL(String identificacion, String idCliente) throws Exception {
        try {
            if (identificacion != null && idCliente != null) {
                return String.format(EMPLEADO, identificacion, idCliente);
            } else {
                throw new Exception("No se ha especificado la identificación del empleado");
            }
        } catch (Exception e) {
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getVehiculoURL(String placa, String idCliente) throws Exception{
        try {
            if(placa!=null && idCliente!=null){
                return String.format(VEHICULO, URLEncoder.encode(placa, "UTF-8"), idCliente);
            }else{
                throw new Exception("No se ha especificado la identificación del vehículo y el cliente");
            }
        }catch (Exception e){
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static String getConceptoMantenimientoURL(String idCliente) throws Exception{
        try {
            if( idCliente!=null){
                return String.format(CONCEPTOS_VEHICULO, idCliente);
            }else{
                throw new Exception("No se ha especificado la identificación del cliente");
            }
        }catch (Exception e){
            throw new Exception("ERROR: " + e.getMessage());
        }
    }

    public static EmpleadoDTO procesarJsonEmpleado(String jsonString) throws Exception {
        EmpleadoDTO empleado = null;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null") &&
                        !jsonObject.getString("MensajeError").equals("")) {

                    throw new Exception(jsonObject.getString("MensajeError"));

                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {

                    empleado = new EmpleadoDTO();
                    JSONObject jsonEmpleado = jsonObject.getJSONObject("Resultado");
                    empleado.IdEmpleado = jsonEmpleado.getString("IdEmpleado");
                    empleado.Identificacion = jsonEmpleado.getString("Identificacion");
                    empleado.Nombres = jsonEmpleado.getString("Nombres");
                    empleado.Apellidos = jsonEmpleado.getString("Apellidos");
                    empleado.FechaNacimiento = jsonEmpleado.getString("FechaNacimiento");
                    empleado.TelefonoFijo = jsonEmpleado.getString("TelefonoFijo");
                    empleado.TelefonoCelular = jsonEmpleado.getString("TelefonoCelular");
                    empleado.Correo = jsonEmpleado.getString("Correo");
                    empleado.Direccion = jsonEmpleado.getString("Direccion");
                    empleado.FechaVinculacion = jsonEmpleado.getString("FechaVinculacion");
                    empleado.Foto = jsonEmpleado.getString("Foto");
                    empleado.IdArea = jsonEmpleado.getString("IdArea");
                    empleado.NombreArea = jsonEmpleado.getString("NombreArea");
                    empleado.IdCargo = jsonEmpleado.getString("IdCargo");
                    empleado.NombreCargo = jsonEmpleado.getString("NombreCargo");
                    empleado.CodigoDepartamento = jsonEmpleado.getString("CodigoDepartamento");
                    empleado.NombreDepartamento = jsonEmpleado.getString("NombreDepartamento");
                    empleado.CodigoMunicipio = jsonEmpleado.getString("CodigoMunicipio");
                    empleado.NombreMunicipio = jsonEmpleado.getString("NombreMunicipio");
                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        }
        return empleado;
    }

    public static VehiculoDTO procesarJsonVehiculo(String jsonString) throws Exception {
        VehiculoDTO vehiculo = null;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null") &&
                        !jsonObject.getString("MensajeError").equals("")) {

                    throw new Exception(jsonObject.getString("MensajeError"));

                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {

                    vehiculo = new VehiculoDTO();
                    JSONObject jsonVehiculo = jsonObject.getJSONObject("Resultado");
                    vehiculo.IdVehiculo = jsonVehiculo.getInt("IdVehiculo");
                    vehiculo.Placa = jsonVehiculo.getString("Placa");
                    vehiculo.Modelo = jsonVehiculo.getString("Modelo");
                    vehiculo.IdMarca = jsonVehiculo.getInt("IdMarca");
                    vehiculo.IdArea = jsonVehiculo.getInt("IdArea");
                    vehiculo.IdCargo = jsonVehiculo.getInt("IdCargo");
                    vehiculo.IdEmpleado = jsonVehiculo.getInt("IdEmpleado");
                    vehiculo.NombreEmpleado = jsonVehiculo.getString("NombreEmpleado");
                    vehiculo.IdPropietario= jsonVehiculo.getInt("IdPropietario");
                    vehiculo.NombrePropietario= jsonVehiculo.getString("NombrePropietario");
                    vehiculo.CodigoDepartamento= jsonVehiculo.getString("CodigoDepartamento");
                    vehiculo.NombreDepartamento= jsonVehiculo.getString("NombreDepartamento");
                    vehiculo.CodigoMunicipio= jsonVehiculo.getString("CodigoMunicipio");
                    vehiculo.NombreMunicipio= jsonVehiculo.getString("NombreMunicipio");
                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        }
        return vehiculo;
    }

    public static ArrayList<ConceptoMantenimientoDTO> procesarJsonConceptos(String jsonString) throws Exception{
        ArrayList<ConceptoMantenimientoDTO> conceptos = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null") &&
                        !jsonObject.getString("MensajeError").equals("")) {

                    throw new Exception(jsonObject.getString("MensajeError"));
                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    conceptos = new ArrayList<>();
                    JSONArray products = jsonObject.getJSONArray("Resultado");
                    for(int i=0; i<products.length(); i++){
                        JSONObject product = products.getJSONObject(i);
                        if(product!=null){
                            ConceptoMantenimientoDTO concepto = new ConceptoMantenimientoDTO();
                            //concepto.IdConceptoMantenimiento = product.getString("<IdConceptoGasto>k__BackingField");
                           // concepto.IdConceptoMantenimiento = product.getString("IdConceptoGasto");
                            //concepto.Descripcion = product.getString("<Nombre>k__BackingField");
                            concepto.IdConceptoMantenimiento =String.valueOf( product.getInt("IdConceptoGasto"));
                            concepto.Descripcion = product.getString("Nombre");
                            concepto.DuracionMantenimiento = 1;
                            conceptos.add(concepto);
                        }
                    }
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }
        return conceptos;
    }

    public static boolean procesarJsonMantenimiento(String jsonString) throws Exception {
        boolean sw = false;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null") &&
                        !jsonObject.getString("MensajeError").equals("")) {

                    throw new Exception(jsonObject.getString("MensajeError"));

                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {
                    sw = true;
                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        }
        return sw;
    }

    public static String[] procesarJsonFechaHora(String jsonString) throws Exception {
        String[] fechaServer = null;
        try {
            if (jsonString != null && !jsonString.equals("")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError") &&
                        !jsonObject.getString("MensajeError").equals("null") &&
                        !jsonObject.getString("MensajeError").equals("")) {

                    throw new Exception(jsonObject.getString("MensajeError"));

                } else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")) {
                    fechaServer = jsonObject.getString("Resultado").split(":");
                }
            }
        } catch (JSONException e) {
            throw new Exception("JSONException: " + e.getMessage());
        }
        return fechaServer;
    }

    //IMEI
    public static String procesarImeiJsonEstado(String jsonString) throws Exception{
        String result = "OK";
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")
                        && !jsonObject.getString("MensajeError").equals("")){

                    result = (jsonObject.getString("MensajeError"));

                    //result = R; {Debe configurar la ruta correcta}
                    //result = B; {Bloquear aplicación instalada}
                    //result = Exception; {No interferir con el flujo, dejar continuar}

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    result = jsonObject.getString("Resultado").trim();
                    //Result = OK; {Dejar transmitir y recibir datos, operaciones normales}
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return result;
    }

    public static ControlImeiDTO procesarImeiJsonObject(String jsonString) throws Exception{
        ControlImeiDTO result = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")
                        && !jsonObject.getString("MensajeError").equals("")){

                    throw new Exception (jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    JSONObject ci = jsonObject.getJSONObject("Resultado");
                    result = new ControlImeiDTO();
                    result.IdControlImei = ci.getInt("IdControlImei");
                    result.IdCliente = ci.getString("IdCliente");
                    result.CodigoRuta = ci.getString("CodigoRuta");
                    result.Imei = ci.getString("Imei");
                    result.Habilitado = ci.getBoolean("Habilitado");
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return result;
    }

    public static MValFact procesarMonitorFacturacionJsonObject(String jsonString) throws Exception{
        MValFact result = null;
        try{
            if(jsonString != null && !jsonString.equals("")){
                JSONObject jsonObject = new JSONObject(jsonString);
                if(jsonObject.has("MensajeError") && !jsonObject.isNull("MensajeError")
                        && !jsonObject.getString("MensajeError").equals("null")
                        && !jsonObject.getString("MensajeError").equals("")){

                    throw new Exception (jsonObject.getString("MensajeError"));

                }else if (jsonObject.has("Resultado") && !jsonObject.isNull("Resultado")){

                    JSONObject ci = jsonObject.getJSONObject("Resultado");
                    result = new MValFact();
                    result.NumeroFactura = ci.getString("NumeroFactura");
                    result.Anulada = ci.getBoolean("Anulada");
                    result.CantidadDetalle = ci.getInt("CantidadDetalle");
                    result.Total = ci.getDouble("Total");
                    result.IdCliente = ci.getInt("IdCliente");
                    result.Existe = ci.getBoolean("Existe");
                }
            }
        }catch (JSONException je){
            throw new Exception("JSONException: "+ je.getMessage());
        }catch (Exception ex){
            throw new Exception(ex.getMessage());
        }
        return result;
    }
}
