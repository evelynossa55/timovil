package com.cm.timovil2.bl.app;

import java.util.ArrayList;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.cm.timovil2.dto.ConceptoMantenimientoDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.EmpleadoDTO;
import com.cm.timovil2.dto.ResumenNotasCreditoPorDevolucionDTO;
import com.cm.timovil2.dto.ResumenRemisionesDTO;
import com.cm.timovil2.dto.VehiculoDTO;
import com.cm.timovil2.dto.wsentities.MUltRem;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.GestionComercialDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.dto.PedidoCallcenterDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.ResumenFacturacionDTO;
import com.cm.timovil2.dto.wsentities.MUltFac;


public class App extends Application {

    public final static String ERROR_CONECTIVIDAD = "No se ha establecido una conexión de red.";

    public static boolean SincronizandoFactura = false;
    public static ArrayList<String> SincronizandoFacturaNumero = new ArrayList<>();

    public static boolean SincronizandoRemision = false;
    public static ArrayList<String> SincronizandoRemisionNumero = new ArrayList<>();

    public static boolean SincronizandoNotaCredito = false;
    public static ArrayList<String> SincronizandoNotaCreditoNumero = new ArrayList<>();

    public static boolean SincronizandoNoVenta = false;
    public static ArrayList<Integer> SincronizandoNoVentaIdMotivoNoVenta = new ArrayList<>();

    public static boolean SincronizandoNoVentaPedido = false;
    public static ArrayList<Integer> SincronizandoNoVentaIdMotivoNoVentaPedido = new ArrayList<>();

    public static boolean SincronizandoAbonoFactura = false;
    public static ArrayList<Integer> SincronizandoAbonoFacturaId = new ArrayList<>();

    public static double EfectivoPagado = 0;
    public static String ComentarioFactura = "";

    public final static ResumenFacturacionDTO ResumenFacturacion = new ResumenFacturacionDTO();
    public final static ResumenRemisionesDTO ResumenRemisiones = new ResumenRemisionesDTO();
    public final static ResumenNotasCreditoPorDevolucionDTO ResumenNotasCreditoPorDevolucion = new ResumenNotasCreditoPorDevolucionDTO();
    public static ArrayList<DetalleFacturaDTO> DetalleFacturacion = new ArrayList<>();
    public static ArrayList<DetalleRemisionDTO> DetalleRemision = new ArrayList<>();

    //variable para saber si la activity actual es <<ActivityFacturas>> desde el broadcast receiver
    //con el propósito de actualizar el estado de las facturas una vez el servicio las descarga:
    public static boolean isFacturasActivityVisible = false;

    public static ActivityBase actualActivity = null;

    public static MUltFac ultimaFactura = null;

    public static MUltRem ultimaRemision = null;

    public static FacturaDTO aux_facturaDTO = null;
    public static RemisionDTO aux_remisionDTO = null;
    public static RemisionDTO remision_para_eliminar  = null;
    public static PedidoCallcenterDTO pedido_actual;

    public static GuardarMotivoNoVentaDTO mnv_para_eliminar  = null;
    public static GuardarMotivoNoVentaPedidoDTO mnvp_para_eliminar  = null;

    public static GestionComercialDTO vergestionComercialDTO = null;

    public static EmpleadoDTO empleado;
    public static VehiculoDTO vehiculo;
    public static ArrayList<ConceptoMantenimientoDTO> conceptoMantenimiento;

    public static boolean isLocationServiceActive;

    public static boolean validarEstadoAplicacion(Context context){
        boolean sw_estado = true;
        String estado = obtenerConfiguracionEstadoAplicacion(context);
        if(estado.equals("B")){
            MostrarToast(context, "La aplicación se encuentra bloqueada, por favor comuníquese con el administrador del sistema");
            sw_estado = false;
        }else if(estado.equals("R")){
            MostrarToast(context, "Configure la aplicación con la ruta asignada a este dispositivo");
            sw_estado = false;
        }
        return sw_estado;
    }

    public static void MostrarToast(Context contexto, String mensaje) {
        Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show();
    }

    public static String Dia(int _dia) {
        switch (_dia) {
            case 1:
                return "Lunes";
            case 2:
                return "Martes";
            case 3:
                return "Miércoles";
            case 4:
                return "Jueves";
            case 5:
                return "Viernes";
            case 6:
                return "Sábado";
            case 7:
                return "Domingo";
            default:
                return "Todos";
        }
    }

    //region SharedPreferences utils
    /**
     * Guarda las configuraciones de la impresora en DefaultSharedPreferences
     * @param resolucion : ResolucionDTO
     * @param context    : Context
     */
    public static void guardarConfiguracionImpresora(ResolucionDTO resolucion, Context context) {
        if (resolucion != null && context != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            preferences.edit().putString("pref_print_type", (resolucion.TipoImpresora)).apply();
            preferences.edit().putString("pref_nombre_impresora", (resolucion.NombreImpresora)).apply();
            preferences.edit().putString("pref_mac_impresora", (resolucion.MACImpresora)).apply();
        }
    }

    /**
     * Obtiene el tipo de escaner guardado en las preferencias de la app
     * @param context : Context
     * @return tipo de escaner
     */
    public static String obtenerPreferencias_ScannerType(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_scanner_type", "CAMARA");
    }

    /**
     * Obtiene el valor referente al número de copias guardado en las preferencias de la app
     * @param context : Context
     * @return número de copias preferidas
     */
    public static int obtenerPreferencias_NroCopias(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String nroCopias = preferences.getString("pref_cantidad_impresiones", "1");
        if(nroCopias != null && !TextUtils.isEmpty(nroCopias))
            return Integer.parseInt(nroCopias);
        else return 1;
    }

    /**
     * Obtiene el valor referente al tipo de impresora guardada en las preferencias de la app
     * @param context : Context
     * @return El tipo de impresora utilizada por el vendedor
     */
    public static String obtenerPreferencias_TipoImpresora(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_print_type", "GENERICA");
    }

    /**
     * Obtiene el valor referente al nombre de impresora guardada en las preferencias de la app
     * @param context : Context
     * @return El nombre de impresora utilizada por el vendedor
     */
    public static String obtenerPreferencias_NombreImpresora(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_nombre_impresora", "SIN NOMBRE");
    }

    /**
     * Obtiene el valor referente a la mac de la impresora guardada en las preferencias de la app
     * @param context : Context
     * @return La mac de la impresora utilizada por el vendedor
     */
    public static String obtenerPreferencias_MacImpresora(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_mac_impresora", "");
    }

    /**
     * Recupera el valor donde se indica si se debe o no dividir la impresion
     * @param context : Context
     */
    public static boolean obtenerPreferencias_DividirImpresion(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_partir_impresion", true);
    }

    /**
     * Obtiene el número de lineas por envio o cantidad de parrafos que se imprimen a la vez
     * @param context : Context
     * @return número de lineas por envio
     */
    public static int obtenerPreferencias_LineasPorEnvio(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lineasPorEnvio = preferences.getString("pref_lineas_por_envio", "20");
        if(lineasPorEnvio != null && !TextUtils.isEmpty(lineasPorEnvio))
            return Integer.parseInt(lineasPorEnvio);
        else return 20;
    }

    /**
     * Obtiene las pulgadas de la impresora (Número)
     * @param context : Context
     * @return Pulgadas de impresión
     */
    public static String obtenerPreferencias_PulgadasImpresion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_print_pulgadas", "DOS");
    }

    /**
     * Guarda el IdCliente y el CodigoRuta junto con las Default Shared Preferences
     * @param resolucionDTO : ResolucionDTO, para acceder a la configuración
     * @param context : Context, para crear un objeto SharedPreferences
     */
    public static void guardarConfiguracionRuta(ResolucionDTO resolucionDTO, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_id_Cliente", resolucionDTO.IdCliente).apply();
        preferences.edit().putString("pref_codigo_ruta", resolucionDTO.CodigoRuta).apply();
    }

    /*
     * Obtiene los datos de configuración(ResolucionDTO[IdCliente - CodigoRuta]) guardados en la Default Shared
     * Preferences, al momento de configurar la ruta en la pantalla ActivityConfigRuta.
     * @param context : Context, para crear un objeto SharedPreferences
     * @return ResolucionDTO object, que contiene el IdCliente y CodigoRuta con los que se ha
     * configurado la aplicación

    public static ResolucionDTO obtenerConfiguracionRuta(Context context){
        ResolucionDTO resolucionDTO = new ResolucionDTO();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        resolucionDTO.IdCliente = preferences.getString("pref_id_Cliente", null);
        resolucionDTO.CodigoRuta = preferences.getString("pref_codigo_ruta", null);
        resolucionDTO.NombreImpresora = preferences.getString("pref_nombre_impresora", null);
        resolucionDTO.MACImpresora = preferences.getString("pref_mac_impresora", null);
        return resolucionDTO;
    }
     */

    /**
     * Guarda en configuraciones si es posible o no el facturar haciendo cuenta del inventario
     * @param context [Para acceder a las SharedPreferences]
     * @param sw [true : Permite Facturar aúnque hayan cero productos]
     */
    public static void guardarConfiguracion_PermitirFacturarSinInventario(Context context, boolean sw){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_permitir_facturar_sin_inventario", sw).apply();
    }

    /**
     * Obtiene de las configuraciones si es posible o no el facturar haciendo cuenta del inventario
     * @param context [Para acceder a las SharedPreferences]
     * @return true si sí es posible, false si no es posible.
     */
    public static boolean obtenerConfiguracion_PermitirFacturarSinInventario(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_permitir_facturar_sin_inventario", false);
    }

    /**
     * Guarda en configuraciones la versión actual de la aplicación, que se encuentra instalada
     * en el teléfono del vendedor
     * @param context [Para acceder a las sharedPreferences]
     * @param version [Número de la versión que se encuentra instalada]
     */
    public static void guardarConfiguracion_VersionAplicacion(Context context, String version){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_version_aplicacion", version).apply();
    }

    /**
     * Guarda en configuraciones la última versión de la aplicación, que debe estar instalada
     * en el teléfono del vendedor
     * @param context [Para acceder a las sharedPreferences]
     * @param version [Número de la versión que debe ser instalada]
     */
    public static void guardarConfiguracion_LastVersionAplicacion(Context context, String version){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_last_version_aplicacion", version).apply();
    }

    /**
     * Obtiene el valor de la versión actual instalada de la aplicación
     * @param context [Para acceder a las SharedPreferences]
     * @return versión instalada de la aplicación
     */
    public static String obtenerConfiguracion_VersionAplicacion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_version_aplicacion", "NULL");
    }

    /**
     * Obtiene el valor de la última versión de la aplicación que debe estar instalada
     * @param context [Para acceder a las SharedPreferences]
     * @return última versión de la aplicación
     */
    public static String obtenerConfiguracion_LastVersionAplicacion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_last_version_aplicacion", "NULL");
    }

    /**
     * Guarda la últims fecha en la que se realizó la carga de datos por el usuario
     * @param fecha valor fecha a guardar
     * @param context [Para acceder a las SharedPreferences]
     */
    public static void guardarFechaDescargaDatos(String fecha, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_fecha_descarga", fecha).apply();
    }

    /**
     * Obtiene el valor equivalente a la última fecha de carga de datos por el usuario
     * @param context [Para acceder a las SharedPreferences]
     * @return última fecha de carga de datos
     */
    public static String obtenerFechaDescargaDatos(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_fecha_descarga", "NULL");
    }

    /**
     * Guarda la última fecha en la que facturó
     * @param fecha valor fecha a guardar
     * @param context [Para acceder a las SharedPreferences]
     */
    public static void guardarFechaUltimaFactura(String fecha, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_fecha_ultima_factura", fecha).apply();
    }

    /**
     * Obtiene el valor equivalente a la última fecha de factura
     * @param context [Para acceder a las SharedPreferences]
     * @return última fecha de carga de datos
     */
    public static String obtenerFechaUltimaFactura(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_fecha_ultima_factura", "NULL");
    }


    /**
     * Guarda la última fecha en la que se remisionó
     * @param fecha valor fecha a guardar
     * @param context [Para acceder a las SharedPreferences]
     */
    public static void guardarFechaUltimaRemision(String fecha, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_fecha_ultima_remision", fecha).apply();
    }

    /**
     * Obtiene el valor equivalente a la última fecha de remisión
     * @param context [Para acceder a las SharedPreferences]
     * @return última fecha de carga de datos
     */
    public static String obtenerFechaUltimaRemision(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_fecha_ultima_remision", "NULL");
    }

    /**
     * Guarda el estado [B, OK, R] obtenido desde la validación del IMEI en el servidor
     * @param estado Estado a guardar
     * @param context [Para acceder a las SharedPrefences]
     */
    public static void guardarConfiguracionEstadoAplicacion(String estado, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_estado_app", estado).apply();
        if(!estado.equals("OK")){
            new ClienteDAL(context).Eliminar();
        }
    }

    /**
     *  Obtiene el estado de la aplicación instalada
     * @param context [Para acceder a las SharedPreferences]
     * @return estado [B, OK, R]
     */
    private static String obtenerConfiguracionEstadoAplicacion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_estado_app", "OK");
    }

    /**
     * Guarda en configuraciones el valor que indíca si se debe o no sincronizar el inventario,
     * después de crear una factura o una remisión
     * @param context [Para acceder a las sharedPreferences]
     * @param sincronizarInventario [valor booleano]
     */
    public static void guardarConfiguracion_SincronizarInventario(Context context, boolean sincronizarInventario){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_sincronizar_inventario", sincronizarInventario).apply();
    }

    /**
     * Obtiene el valor que indíca si se debe o no sincronizar el inventario,
     * después de crear una factura o una remisión
     * @param context [Para acceder a las SharedPreferences]
     * @return false|true
     */
    public static boolean obtenerConfiguracionSincronizarInventario(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_sincronizar_inventario", false);
    }

    /**
     * Guarda en configuraciones el valor que indíca el total de la meta de ventas del vendedor
     * para el mes actual
     * @param context [Para acceder a las sharedPreferences]
     * @param valorVentaMensual [valor venta mensual]
     */
    public static void guardarConfiguracion_valorVentaMensual(Context context, float valorVentaMensual){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putFloat("pref_valor_venta_mensual", valorVentaMensual).apply();
    }

    /**
     * Obtiene el valor de la venta mensual del vendedor // se esta obteniendo es el valor de la meta no de las ventas
     * @param context [Para acceder a las SharedPreferences]
     * @return valor tipo float
     */
    public static float obtenerConfiguracionValorVentasMensual(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat("pref_valor_venta_mensual", 0);
    }

    /**
     * Guarda la latitud actual del usuario
     * @param context [Para acceder a las sharedPreferences]
     * @param latitud [latitud a guardar]
     */
    public static void guardarConfiguracion_latitudActual(Context context, String latitud){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_latitud_actual", latitud).apply();
    }

    /**
     * Obtiene la latitud actual del usuario
     * @param context [Para acceder a las SharedPreferences]
     * @return latitud
     */
    public static String obtenerConfiguracion_latitudActual(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_latitud_actual", "");
    }

    /**
     * Guarda la longitud actual del usuario
     * @param context [Para acceder a las sharedPreferences]
     * @param longitud [longitud a guardar]
     */
    public static void guardarConfiguracion_longitudActual(Context context, String longitud){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_longitud_actual", longitud).apply();
    }

    /**
     * Obtiene la latitud actual del usuario
     * @param context [Para acceder a las SharedPreferences]
     * @return longitud
     */
    public static String obtenerConfiguracion_longitudActual(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_longitud_actual", "");
    }

    /**
     * Guarda el periodo en minutos necesario para reportar la ubicación GPS
     * @param context [Para acceder a las sharedPreferences]
     * @param periodo [periodo en minútos]
     */
    public static void guardarConfiguracion_periodoReporteUbicacion(Context context, int periodo){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt("pref_periodo_reporte_ubicacion", (periodo > 60 ? 10 : periodo)).apply();
    }

    /**
     * Obtiene el periodo en minutos necesario para reportar la ubicación GPS
     * @param context [Para acceder a las SharedPreferences]
     * @return periodo en minútos
     */
    public static int obtenerConfiguracion_periodoReporteUbicacion(Context context){
        if(context == null) return 10;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("pref_periodo_reporte_ubicacion", 10);
    }

    /**
     * Guarda el tipo de ruta [Vendedor|Asesor comercial]
     * @param context [Para acceder a las sharedPreferences]
     * @param tipoRuta [Tipo de la ruta]
     */
    public static void guardarConfiguracion_tipoRuta(Context context, String tipoRuta){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_tipo_ruta",
                (tipoRuta == null || TextUtils.isEmpty(tipoRuta) ? "Vendedor" : tipoRuta)).apply();
    }

    /**
     * Obtiene el tipo de ruta [Vendedor|Asesor comercial]
     * @param context [Para acceder a las SharedPreferences]
     * @return el tipo de ruta
     */
    public static String obtenerConfiguracion_tipoRuta(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_tipo_ruta", "Vendedor");
    }

    /**
     * Guarda la configuración, usar impresora o no
     * @param context [Para acceder a las sharedPreferences]
     * @param imprimir [boolean]
     */
    public static void guardarConfiguracion_imprimir(Context context, boolean imprimir){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_imprimir", imprimir).apply();
    }

    /**
     * Obtiene la configuración, usar impresora o no
     * @param context [Para acceder a las SharedPreferences]
     * @return valor booleano
     */
    public static boolean obtenerConfiguracion_imprimir(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_imprimir", true);
    }

    /**
     * Guarda la fecha en milisegundos del último registro de ubicación generado
     * @param context [Para acceder a las sharedPreferences]
     * @param time [long fecha]
     */
    public static void guardarConfiguracion_ultimoRegistroUbicacion(Context context, long time){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong("pref_ultimoRegistroUbicacion", time).apply();
    }

    /**
     * Obtiene la configuración, usar impresora o no
     * @param context [Para acceder a las SharedPreferences]
     * @return valor long fecha en milisegundos
     */
    public static long obtenerConfiguracion_ultimoRegistroUbicacion(Context context){
        if(context == null) return -1;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong("pref_ultimoRegistroUbicacion", -1);
    }

    /**
     * Guarda la configuración, usar ManejarRecaudoCredito o no
     * @param context [Para acceder a las sharedPreferences]
     * @param imprimir [boolean]
     */
    public static void guardarConfiguracion_ManejarRecaudoCredito(Context context, boolean imprimir){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_ManejarRecaudoCredito", imprimir).apply();
    }

    /**
     * Obtiene la configuración, usar ManejarRecaudoCredito o no
     * @param context [Para acceder a las SharedPreferences]
     * @return valor booleano
     */
    public static boolean obtenerConfiguracion_ManejarRecaudoCredito(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_ManejarRecaudoCredito", true);
    }

    /**
     * Guarda la configuración, usar PermitirCambiarFormaDePago o no
     * @param context [Para acceder a las sharedPreferences]
     * @param imprimir [boolean]
     */
    public static void guardarConfiguracion_PermitirCambiarFormaDePago(Context context, boolean imprimir){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_PermitirCambiarFormaDePago", imprimir).apply();
    }

    /**
     * Obtiene la configuración, usar PermitirCambiarFormaDePago o no
     * @param context [Para acceder a las SharedPreferences]
     * @return valor booleano
     */
    public static boolean obtenerConfiguracion_PermitirCambiarFormaDePago(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_PermitirCambiarFormaDePago", true);
    }

    /**
     * Guarda la configuración, usar CantidadFacturasClientePorDia
     * @param context [Para acceder a las sharedPreferences]
     * @param cant [int]
     */
    public static void guardarConfiguracion_CantidadFacturasClientePorDia(Context context, int cant){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt("pref_CantidadFacturasClientePorDia", cant).apply();
    }

    /**
     * Obtiene la configuración, usar PermitirCambiarFormaDePago o no
     * @param context [Para acceder a las SharedPreferences]
     * @return valor booleano
     */
    public static int obtenerConfiguracion_CantidadFacturasClientePorDia(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("pref_CantidadFacturasClientePorDia", 0);
    }

    /**
     * Guarda la configuración, tipo de resolución de facturación
     * @param context [Para acceder a las sharedPreferences]
     * @param tipo [String]
     */
    public static void guardarConfiguracion_TipoResolucion(Context context, String tipo){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_TipoResolucion", tipo).apply();
    }

    /**
     * Obtiene la configuración, tipo de resolución de facturación
     * @param context [Para acceder a las SharedPreferences]
     * @return valor booleano
     */
    public static String obtenerConfiguracion_TipoResolucion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_TipoResolucion", ResolucionDTO.TIPO_COMPUTADOR);
    }

    /**
     * Guarda la configuración, número de celular del vendedor
     * @param context [Para acceder a las sharedPreferences]
     * @param numeroCelular [String]
     */
    public static void guardarConfiguracion_NumeroCelularRuta(Context context, String numeroCelular){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_NumeroCelularRuta", numeroCelular).apply();
    }

    /**
     * Obtiene la configuración, número de celular del vendedor
     * @param context [Para acceder a las SharedPreferences]
     * @return valor String
     */
    public static String obtenerConfiguracion_NumeroCelularRuta(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_NumeroCelularRuta", "");
    }

    /**
     * Guarda la configuración, fecha de la resolución del vendedor
     * @param context [Para acceder a las sharedPreferences]
     * @param fechaDeResolucion [String] 2017.1.24
     */
    public static void guardarConfiguracion_FechaDeResolucion(Context context, String fechaDeResolucion){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_FechaDeResolucion", fechaDeResolucion).apply();
    }

    /**
     * Obtiene la configuración, fecha de la resolución del vendedor
     * @param context [Para acceder a las SharedPreferences]
     * @return valor String 2017.1.24
     */
    public static String obtenerConfiguracion_FechaDeResolucion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_FechaDeResolucion", "");
    }

    /**
     * Guarda la configuración, permitir crear remisiones con valor cero
     * @param context [Para acceder a las sharedPreferences]
     * @param permitir [boolean]
     */
    public static void guardarConfiguracion_PermitirRemisionesConValorCero(Context context, boolean permitir){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_PermitirRemisionesConValorCero", permitir).apply();
    }

    /**
     * Obtiene la configuración, permitir crear remisiones con valor cero
     * @param context [Para acceder a las SharedPreferences]
     * @return valor booleano
     */
    public static boolean obtenerConfiguracion_PermitirRemisionesConValorCero(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_PermitirRemisionesConValorCero", false);
    }

    /**
     * Guarda la configuración, Porcentaje de Rete Iva
     * @param context [Para acceder a las sharedPreferences]
     * @param porcentaje [float]
     */
    public static void guardarConfiguracion_PorcentajeReteIva(Context context, float porcentaje){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putFloat("pref_PorcentajeReteIva", porcentaje).apply();
    }

    /**
     * Obtiene la configuración, permitir crear remisiones con valor cero
     * @param context [Para acceder a las SharedPreferences]
     * @return valor float porcentaje
     */
    public static float obtenerConfiguracion_PorcentajeReteIva(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat("pref_PorcentajeReteIva", 0);
    }

    /**
     * Guarda la configuración, tope de rete iva
     * @param context [Para acceder a las sharedPreferences]
     * @param topeReteIva float valor
     */
    public static void guardarConfiguracion_TopeReteIva(Context context, float topeReteIva){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putFloat("pref_TopeReteIva", topeReteIva).apply();
    }

    /**
     * Obtiene la configuración, tope de rete iva
     * @param context [Para acceder a las SharedPreferences]
     * @return valor float tope
     */
    public static float obtenerConfiguracion_TopeReteIva(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat("pref_TopeReteIva", 0);
    }

    /**
     * Guarda la configuración, crear nota crédito por devolución
     * @param context [Para acceder a las sharedPreferences]
     * @param crearNotaCreditoPorDevolucion boolean valor
     */
    public static void guardarConfiguracion_CrearNotaCreditoPorDevolucion(Context context, boolean crearNotaCreditoPorDevolucion){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_CrearNotaCreditoPorDevolucion", crearNotaCreditoPorDevolucion).apply();
    }

    /**
     * Obtiene la configuración, crear nota crédito por devolución
     * @param context [Para acceder a las SharedPreferences]
     * @return valor boolean
     */
    public static boolean obtenerConfiguracion_CrearNotaCreditoPorDevolucion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_CrearNotaCreditoPorDevolucion", false);
    }

    /**
     * Guarda la configuración, manejar inventario remisiones
     * @param context [Para acceder a las sharedPreferences]
     * @param manejarInventarioRemisiones boolean valor
     */
    public static void guardarConfiguracion_ManejarInventarioRemisiones(Context context, boolean manejarInventarioRemisiones){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_ManejarInventarioRemisiones", manejarInventarioRemisiones).apply();
    }

    /**
     * Obtiene la configuración, manejar inventario remisiones
     * @param context [Para acceder a las SharedPreferences]
     * @return valor boolean
     */
    public static boolean obtenerConfiguracion_ManejarInventarioRemisiones(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_ManejarInventarioRemisiones", false);
    }

    /**
     * Guarda la configuración, fecha vigencia de la resolución del vendedor
     * @param context [Para acceder a las sharedPreferences]
     * @param fechaVigenciaDeResolucion [String] 2017.1.24
     */
    public static void guardarConfiguracion_FechaVigenciaDeResolucion(Context context, String fechaVigenciaDeResolucion){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_FechaVigenciaDeResolucion", fechaVigenciaDeResolucion).apply();
    }

    /**
     * Obtiene la configuración, fecha vigencia de la resolución del vendedor
     * @param context [Para acceder a las SharedPreferences]
     * @return valor String 2017.1.24
     */
    public static String obtenerConfiguracion_FechaVigenciaDeResolucion(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_FechaVigenciaDeResolucion", "");
    }

    /**
     * Guarda la configuración, devolucion  afecta la remisión
     * @param context [Para acceder a las sharedPreferences]
     * @param devolucionAfectaRemision boolean valor
     */
    public static void guardarConfiguracion_DevolucionAfectaRemision(Context context, boolean devolucionAfectaRemision){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_DevolucionAfectaRemision", devolucionAfectaRemision).apply();
    }

    /**
     * Obtiene la configuración, manejar inventario remisiones
     * @param context [Para acceder a las SharedPreferences]
     * @return valor boolean
     */
    public static boolean obtenerConfiguracion_DevolucionAfectaRemision(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_DevolucionAfectaRemision", false);
    }

    /**
     * Guarda la configuración, EFactura... que indica si el cliente usa o no Facturación Electrónica
     * @param context [Para acceder a las sharedPreferences]
     * @param efactura boolean valor
     */
    public static void guardarConfiguracion_EFactura(Context context, boolean efactura){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean("pref_EFactura", efactura).apply();
    }

    /**
     * Obtiene la configuración, EFactura... que indica si el cliente usa o no Facturación Electrónica
     * @param context [Para acceder a las SharedPreferences]
     * @return valor boolean
     */
    public static boolean obtenerConfiguracion_EFactura(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_EFactura", false);
    }

    /**
     * Guarda la configuración, Clave Tecnica... para crear QR y Cufe a los clientes de TiMovil que usan
     * Facturación Electrónica
     * @param context [Para acceder a las sharedPreferences]
     * @param claveTecnica String valor
     */
    public static void guardarConfiguracion_ClaveTecnica(Context context, String claveTecnica){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_ClaveTecnica", claveTecnica).apply();
    }

    /**
     * Obtiene la configuración, Clave Tecnica... para crear QR y Cufe a los clientes de TiMovil que usan
     * Facturación Electrónica
     * @param context [Para acceder a las SharedPreferences]
     * @return valor String
     */
    public static String obtenerConfiguracion_ClaveTecnica(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_ClaveTecnica", "");
    }

    /**
     * Guarda la configuración, Ambiente EFactura... para crear QR y Cufe a los clientes de TiMovil que usan
     * Facturación Electrónica
     * @param context [Para acceder a las sharedPreferences]
     * @param ambienteEFactura String valor
     */
    public static void guardarConfiguracion_AmbienteEFactura(Context context, String ambienteEFactura){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("pref_AmbienteEFactura", ambienteEFactura).apply();
    }

    /**
     * Obtiene la configuración, Ambiente EFactura... para crear QR y Cufe a los clientes de TiMovil que usan
     * Facturación Electrónica
     * @param context [Para acceder a las SharedPreferences]
     * @return valor String
     */
    public static String obtenerConfiguracion_AmbienteEFactura(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("pref_AmbienteEFactura", "2");
    }
    //endregion
}