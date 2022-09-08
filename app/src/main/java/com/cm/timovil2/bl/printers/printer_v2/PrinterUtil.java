package com.cm.timovil2.bl.printers.printer_v2;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.EntregadorDAL;
import com.cm.timovil2.data.FormaPagoDAL;
import com.cm.timovil2.data.ProductoDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.EntregadorDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResolucionDTO;
import com.cm.timovil2.dto.wsentities.MFactCredito;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.front.ActivityInventario;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 26/07/18.
 */

public class PrinterUtil {

    private static final String PULGADAS_DOS = "DOS";
    private static final String PULGADAS_TRES = "TRES";
    private static final String SEPARATOR_DOSP = "--------------------------------";
    private static final String SEPARATOR_TRESP = "------------------------------------------------";
    private final String CR = "\r";
    private final String NL = "\n";
    private ActivityBase mContext;
    private int quantityLeftRightSpace;
    private String pulgadas;
    NumberFormat formatter = null;

    PrinterUtil(ActivityBase context, String pulgadas) {
        this.mContext = context;
        quantityLeftRightSpace = 0;
        this.pulgadas = pulgadas;
        formatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    PrinterUtil(ActivityBase context, int quantityLeftRightSpace, String pulgadas) {
        this.mContext = context;
        this.quantityLeftRightSpace = quantityLeftRightSpace;
        this.pulgadas = pulgadas;
    }

    String CR() {
        return CR;
    }

    String NL() {
        return NL;
    }

    String CRNL() {
        return CR() + NL();
    }

    String leftRightSpace(){
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < quantityLeftRightSpace; i++){
            space.append(" ");
        }
        return space.toString();
    }

    String obtenerSeparador(){
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
            return SEPARATOR_DOSP;
        else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
            return SEPARATOR_TRESP;
        else
            return "";

    }

    String obtenerTituloDatosCliente(){
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
            return "----------DATOS-CLIENTE---------";
        else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
            return "------------------DATOS-CLIENTE-----------------";
        else
            return "";

    }

    String obtenerTituloRotaciones(){
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
            return "-----------ROTACIONES-----------";
        else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
            return "-------------------ROTACIONES-------------------";
        else
            return "";
    }

    String obtenerTituloTotales(){
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
            return "------------TOTALES-------------";
        else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
            return "--------------------TOTALES---------------------";
        else
            return "";
    }

    private String obtenerTituloFirmas(){
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
            return "-------------FIRMAS-------------";
        else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
            return "---------------------FIRMAS---------------------";
        else
            return "";
    }

    private String obtenerPieDePaginaZibor(){
        StringBuilder result = new StringBuilder();
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
            result.append(CRNL()).append("-------------------------------");
            result.append(CRNL()).append("   ZIBOR DESARROLLADORES SAS   ");
            result.append(CRNL()).append("      NIT: 900 114 098         ");
            result.append(CRNL()).append("   cliente.tiendamovil.com.co  ");
            result.append(CRNL()).append("-------------------------------");
        }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
            result.append(CRNL()).append("------------------------------------------------");
            result.append(CRNL()).append("           ZIBOR DESARROLLADORES SAS            ");
            result.append(CRNL()).append("               NIT: 900 114 098                 ");
            result.append(CRNL()).append("           cliente.tiendamovil.com.co           ");
            result.append(CRNL()).append("------------------------------------------------");
        }
        return result.toString();
    }

    String obtenerUrlTimovil(){
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
            return
                    "--------------------------------" + CRNL() +
                    "   cliente.tiendamovil.com.co   " + CRNL() +
                    "--------------------------------" + CRNL();
        }else {
            return
                    "------------------------------------------------" + CRNL() +
                    "           cliente.tiendamovil.com.co           " + CRNL() +
                    "------------------------------------------------" + CRNL();
        }
    }

    String obtenerEncabezado() {
        StringBuilder result = new StringBuilder();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)) {
            result.append(leftRightSpace())
                    .append("PANIFICADORA FAMILIAR")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("Productos Don Chalo")
                    .append(leftRightSpace());
        }
        return result.toString();
    }

    String obtenerEncabezadoRemision(){
        StringBuilder result = new StringBuilder();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)) {
            result.append(leftRightSpace())
                    .append("PANIFICADORA FAMILIAR")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("Productos Don Chalo")
                    .append(leftRightSpace());
        }

        return result.toString();
    }

    String obtenerNombreComercial(){
        StringBuilder result = new StringBuilder();
        result.append(ActivityBase.resolucion.NombreComercial).append(NL());
        return result.toString();
    }

    String obtenerNitRemision(){
        StringBuilder result = new StringBuilder();
        result.append("Nit:").append(ActivityBase.resolucion.Nit).append(NL());
        return result.toString();
    }

    String obtenerRegimen() {
        StringBuilder result = new StringBuilder();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)) {
            result.append(leftRightSpace())
                    .append("REGIMEN COMUN")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("ACTIVIDAD ECONOMICA 1089")
                    .append(leftRightSpace());
        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)) {
            result.append(leftRightSpace())
                    .append("REGIMEN COMUN")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("ACTIVIDAD ECONOMICA 4631")
                    .append(leftRightSpace());
        } else {
            result.append(leftRightSpace())
                    .append(Utilities.quitarTildes(ActivityBase.resolucion.Regimen))
                    .append(leftRightSpace());
        }
        return result.toString();
    }

    String obtenerDatosContacto() {
        StringBuilder result = new StringBuilder();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            result.append(leftRightSpace())
                    .append("DIRECCION:")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("Pereira: KM5 via Armenia Vereda Huertas (tel:3388062)")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("Bogota: Calle 20C Nro 97-48 Villemar Fontibon (tel:4042966)")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("Bello: Calle 33 Nro 56-110 (tel:4441480)")
                    .append(leftRightSpace());
        }else if(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)){
            result.append(leftRightSpace())
                    .append("CRA 13 No 51 - 54 SUR")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("PBX 7603131 - 3134458598")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("ventas@comescol.com")
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("www.comescol.com")
                    .append(leftRightSpace());
        } else {
            result.append(leftRightSpace())
                    .append(ActivityBase.resolucion.Direccion)
                    .append(leftRightSpace())
                    .append(CRNL())
                    .append(leftRightSpace())
                    .append("Tel: ").append(ActivityBase.resolucion.Telefono)
                    .append(leftRightSpace());
        }
        return result.toString();
    }

    String obtenerEmail() {
        StringBuilder result = new StringBuilder();
        if (!ActivityBase.resolucion.IdCliente.equals(Utilities.ID_FR)
                && !ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DISTRIBUCIONES_WILL)) {
            result.append(leftRightSpace())
                    .append("Email: ").append(ActivityBase.resolucion.Email)
                    .append(leftRightSpace());
        }
        return result.toString();
    }

    String obtenerNumeroCelularRuta() {
        return leftRightSpace()
                + "Tel Ruta:" + App.obtenerConfiguracion_NumeroCelularRuta(mContext)
                + leftRightSpace();
    }

    String obtenerDatosEntregador(int idEmpleadoEntregador) {
        StringBuilder result = new StringBuilder();
        if (idEmpleadoEntregador > 0) {
            EntregadorDTO entregadorDTO = new EntregadorDAL(mContext)
                    .obtenerPorIdEmpleado(new String[]{String.valueOf(idEmpleadoEntregador)});

            if (entregadorDTO != null && !TextUtils.isEmpty(entregadorDTO.NombreCompleto)) {

                result.append(leftRightSpace())
                        .append("Entregador: ").append(entregadorDTO.NombreCompleto)
                        .append(leftRightSpace())
                        .append(CRNL());
            }

        }
        return result.toString();
    }

    String obtenerTipoDocumentoNro(boolean pos, String codigoRuta) {

        if(pos)
                return leftRightSpace() + "FACTURA POS Nro." + leftRightSpace();
        else {
            if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_SARY)) {
                if (codigoRuta != null && codigoRuta.equals("2"))
                    return leftRightSpace() + "FACTURA DE VENTA Nro." + leftRightSpace();
                else
                    return leftRightSpace() + "CUENTA DE COBRO Nro." + leftRightSpace();
            }
            if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)){
                return leftRightSpace() + "FACTURA DE VENTA Nro." + leftRightSpace();
            } else {
                return leftRightSpace() + "FACTURA DE VENTA Nro." + leftRightSpace();
            }
        }
    }

    String obtenerReglamentacion1() {

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)) {

             return "SI NO RECIBE SU FACTURA";
        }else{
            return "";
        }
    }

    String obtenerReglamentacion2() {

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)) {

            return "EL PEDIDO SERA GRATIS.";
        }else{
            return "";
        }
    }

    String obtenerLabelRemisionNro(boolean remision, boolean credito) {
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DISTRIBUCIONES_WILL)) {
            return leftRightSpace() + "ORDEN Nro." + leftRightSpace();
        } else if (remision && credito == false
                && (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR))) {
            return leftRightSpace() + "ORDEN DE PEDIDO" + leftRightSpace();
        } else {
            return leftRightSpace() + "REMISION Nro." + leftRightSpace();
        }
    }

    String obtenerAnulada(boolean anulada) {
        return (anulada ? leftRightSpace() + "ANULADA" + leftRightSpace() + CRNL() : "");
    }

    String obtenerMunicipioCliente(int idCliente) {
        ClienteDTO client = new ClienteDAL(mContext).ObtenerClientePorIdCliente(String.valueOf(idCliente));
        String city = client.Ubicacion;
        return city != null ? leftRightSpace() + city + leftRightSpace(): "";
    }

    ClienteDTO obtenerCliente(int idCliente) {
        ClienteDTO clienteDTO = new ClienteDAL(mContext).ObtenerClientePorIdCliente(String.valueOf(idCliente));
        return clienteDTO;
    }

    double obtenerValorRetefuente(double subtotalRemision, double descuento) {
        double porcentajeRetefuente = (ActivityBase.resolucion.PorcentajeRetefuente / 100);
        double topeRetefuente = ActivityBase.resolucion.TopeRetefuente;
        if (subtotalRemision - descuento  >= topeRetefuente) {
            return ((subtotalRemision - descuento) * porcentajeRetefuente);
        }
        return 0;
    }

    String obtenerFormaDePago(String codigo) {
        String formaPago;
        FormaPagoDAL fpDal = new FormaPagoDAL(mContext);
        formaPago = fpDal.obtenerPorCodigo(codigo).Nombre;
        if (formaPago == null || formaPago.equals("")) {
            if (!codigo.equals("")) {
                formaPago = codigo;
            } else {
                formaPago = "SIN DETERMINAR";
            }
        }
        formaPago = leftRightSpace() + "FORMA DE PAGO: " + formaPago + leftRightSpace();
        return formaPago;
    }

    String obtenerDatosCredito(FacturaDTO factura) {
        StringBuilder result = new StringBuilder();
        if (factura instanceof MFactCredito) {

            result.append(leftRightSpace())
                    .append("SALDO PENDIENTE: ").append(((MFactCredito) factura).Saldo)
                    .append(leftRightSpace());

            if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)) {
                result.append(CRNL())
                        .append(leftRightSpace())
                        .append("NUMERO PEDIDO: ").append(factura.NumeroPedido)
                        .append(leftRightSpace());
            }
            result.append(CRNL());
        }
        return result.toString();
    }

    String obtenerFecha(long millis) {
        if (millis > 0) {
            String dateFormat = "EEE, dd MMM yyyy hh:mm aaa";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
            Date d = new Date(millis);
            String dateText = leftRightSpace() + sdf.format(d) + leftRightSpace();
            return Utilities.quitarTildes(dateText);
        } else {
            return " ------";
        }
    }

    String obtenerFechaFactura(FacturaDTO factura) throws Exception{
        if (factura instanceof MFactCredito) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                return leftRightSpace()
                        + "FECHA HORA: " + CRNL() + ((MFactCredito) factura).FechaHoraCredito
                        + leftRightSpace();
            }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                return leftRightSpace()
                        + "FECHA HORA: " + ((MFactCredito) factura).FechaHoraCredito
                        + leftRightSpace();
            }
        } else {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                return leftRightSpace()
                        + "FECHA HORA: " + CRNL() + obtenerFecha(factura.FechaHora)
                        + leftRightSpace();
            }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                return leftRightSpace()
                        + "FECHA HORA: " + obtenerFecha(factura.FechaHora)
                        + leftRightSpace();
            }
        }
        throw new Exception("Pulgadas no especificadas");
    }

    String obtenerFechaVencimiento(FacturaDTO factura) throws Exception{
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
            return leftRightSpace() +
                    "FECHA VENC: " + CRNL() + obtenerFecha(factura.FechaHoraVencimiento)
                    + leftRightSpace();
        }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
            return leftRightSpace() +
                    "FECHA VENC: " + obtenerFecha(factura.FechaHoraVencimiento)
                    + leftRightSpace();
        }else{
            throw new Exception("Pulgadas no especificadas");
        }
    }

    String obtenerTituloDevoluciones(){

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DEFRUTA)) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
                return "---------BONIFICACIONES--------" + CRNL();
            else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
                return "-----------------BONIFICACIONES-----------------" + CRNL();
            else return "";
        }else{
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
                return "----------DEVOLUCIONES---------" + CRNL();
            else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
                return "------------------DEVOLUCIONES------------------" + CRNL();
            else return "";
        }

    }

    String obtenerDetailResume(PrinterDetailResume resume){
        if(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)){
            return "";
        }

        return resume.devoluciones + CRNL();
    }

    String obtenerTotalDevoluciones(PrinterDetailResume detailResume){
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        if(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)){
            return "";
        }

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DEFRUTA)) {
            return " Bonificacion :" + formatea.format((int) detailResume.totalDevoluciones) + CRNL();
        }else{
            return " Devolucion :" + formatea.format((int) detailResume.totalDevoluciones) + CRNL();
        }
    }

    String obtenerEncabezadoDetalleDescripcionProducto() throws Exception{
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
                return leftRightSpace() + "Desc" + leftRightSpace();
            else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
                return leftRightSpace() + "Codigo Descripcion" + leftRightSpace();
            else
                throw new Exception("Pulgadas no especificadas");
        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)) {
            return leftRightSpace() + "Producto" + leftRightSpace();
        }else {
            return leftRightSpace() + "Descripcion" + leftRightSpace();
        }
    }

    String obtenerEncabezadoDetalleValoresProducto() throws Exception{
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
                return leftRightSpace() + "Cant. V.Unit.  Subt.   Iva" + leftRightSpace();
            else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
                return leftRightSpace() + "Cantidad    V.Unitario   Subtotal     Iva" + leftRightSpace();
            else
                throw new Exception("Pulgadas no especificadas");
        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
                return leftRightSpace() + "Cant.  V.Unit.   Subt." + leftRightSpace();
            else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
                return leftRightSpace() + "Cantidad    V.Unitario   Subtotal" + leftRightSpace();
            else
                throw new Exception("Pulgadas no especificadas");
        } else {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
                return leftRightSpace() + "Cant. V.Unit.  Subt.   Iva" + leftRightSpace();
            else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
                return leftRightSpace() + "Cantidad    V.Unitario   Subtotal     Iva" + leftRightSpace();
            else
                throw new Exception("Pulgadas no especificadas");
        }
    }

    String obtenerEncabezadoDetalleValoresProductoConIvaFactura() throws Exception{
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS))
            return leftRightSpace() + "Cant. V.Unit.  Subt.   Iva" + leftRightSpace();
        else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES))
            return leftRightSpace() + "Cantidad    V.Unitario   Subtotal     Iva" + leftRightSpace();
        else
            throw new Exception("Pulgadas no especificadas");
    }

    String obtenerEncabezadoDetalleValoresProductoNotaCredito() {
        return leftRightSpace() + " Cod.   Cantidad.   Valor." + leftRightSpace();
    }

    PrinterDetailResume obtenerDetalleFactura(FacturaDTO factura) throws Exception{

        StringBuilder result = new StringBuilder();
        boolean isCredito = (factura instanceof MFactCredito);

        StringBuilder devoluciones = new StringBuilder();
        float total_devoluciones = 0;
        StringBuilder rotaciones = new StringBuilder();
        int cantidadFactura = 0;

        float iva5 = 0;
        float iva10 = 0;
        float iva16 = 0;
        float iva19 = 0;

        if (!isCredito) {
            for (DetalleFacturaDTO oDetalle : factura.DetalleFactura) {

                if (oDetalle.Cantidad > 0) {

                    cantidadFactura += oDetalle.Cantidad;
                    double valorConDescuento = oDetalle.ValorUnitario;
                    double subtotalConDescuento = oDetalle.Subtotal;

                    if (oDetalle.PorcentajeDescuento > 0) {
                        float descuentoValorUnitario = (oDetalle.ValorUnitario * (oDetalle.PorcentajeDescuento / 100));
                        valorConDescuento = oDetalle.ValorUnitario - descuentoValorUnitario;
                        subtotalConDescuento = oDetalle.Subtotal - oDetalle.Descuento;
                    }

                    result.append(NL())
                            .append(leftRightSpace())
                            .append(oDetalle.Nombre)
                            .append(CRNL())
                            .append(leftRightSpace());

                    if (oDetalle.PorcentajeIva > 0) {
                        switch ((int) oDetalle.PorcentajeIva) {
                            case 5:
                                iva5 += oDetalle.Iva;
                                break;
                            case 10:
                                iva10 += oDetalle.Iva;
                                break;
                            case 16:
                                iva16 += oDetalle.Iva;
                                break;
                            case 19:
                                iva19 += oDetalle.Iva;
                                break;
                        }
                    }

                    boolean twoP = pulgadas.equalsIgnoreCase(PULGADAS_DOS);
                    String vlrDesc = String.valueOf((int) valorConDescuento);
                    String vlrSubDes = String.valueOf((int) subtotalConDescuento);
                    long valorIva = Math.round((oDetalle.Cantidad * oDetalle.ValorUnitario) * (oDetalle.PorcentajeIva / 100));
                    String valorIvaVenta = String.valueOf(valorIva);

                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), twoP ? 5: 10)).append(" ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(vlrDesc, twoP ? 8 : 11)).append(" ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(vlrSubDes, twoP ? 7 : 10)).append(" ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(valorIvaVenta, 8));
                    result.append(leftRightSpace());
                }

                if (oDetalle.Devolucion > 0) {

                    String cantidadDevolucion = String.valueOf(oDetalle.Devolucion);
                    String valorUnitarioDevolucion = String.valueOf(Math.round(oDetalle.ValorUnitario));
                    String nombreProductoDevolucion = String.valueOf(oDetalle.Nombre);

                    long valor_dev = Math.round(oDetalle.ValorUnitario * oDetalle.Devolucion);
                    long valor_iva_dev = Math.round(((oDetalle.PorcentajeIva / 100) * oDetalle.ValorUnitario) * oDetalle.Devolucion);

                    String valorProductorDevolucion = String.valueOf(valor_dev);
                    String valorIvaDevolucion = String.valueOf(valor_iva_dev);

                    boolean twoP = pulgadas.equalsIgnoreCase(PULGADAS_DOS);

                    devoluciones.append(NL())
                            .append(leftRightSpace())
                            .append(nombreProductoDevolucion)
                            .append(CRNL())
                            .append(leftRightSpace());

                    devoluciones
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(cantidadDevolucion, twoP ? 5: 10)).append(" ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorUnitarioDevolucion, twoP ? 8 : 11)).append(" ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorProductorDevolucion, twoP ? 7 : 10)).append(" ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorIvaDevolucion, 8))
                            .append(leftRightSpace());

                    total_devoluciones += (valor_iva_dev + valor_dev);
                }

                if (oDetalle.Rotacion > 0) {
                    rotaciones.append(CRNL())
                            .append(leftRightSpace())
                            .append(oDetalle.Rotacion)
                            .append(" ")
                            .append(oDetalle.Nombre)
                            .append(leftRightSpace());
                }

            }
        } else {
            MFactCredito credito = (MFactCredito) factura;
            for (DetalleFacturaDTO oDetalle : credito.DetalleFactura_Credito) {
                if (oDetalle.Cantidad > 0) {
                    cantidadFactura += oDetalle.Cantidad;
                    double valorConDescuento = oDetalle.ValorUnitario;
                    double subtotalConDescuento = oDetalle.Subtotal;

                    if (oDetalle.PorcentajeDescuento > 0) {
                        float descuentoValorUnitario = (oDetalle.ValorUnitario * (oDetalle.PorcentajeDescuento / 100f));
                        valorConDescuento = oDetalle.ValorUnitario - descuentoValorUnitario;
                        subtotalConDescuento = oDetalle.Subtotal - oDetalle.Descuento;
                    }

                    result.append(NL()).append(leftRightSpace());
                    result.append(obtenerNombreProducto(oDetalle));

                    if (oDetalle.PorcentajeIva > 0) {

                        switch ((int) oDetalle.PorcentajeIva) {
                            case 5:
                                iva5 += oDetalle.Iva;
                                break;
                            case 10:
                                iva10 += oDetalle.Iva;
                                break;
                            case 16:
                                iva16 += oDetalle.Iva;
                                break;
                            case 19:
                                iva19 += oDetalle.Iva;
                                break;
                        }
                    }

                    boolean twoP = pulgadas.equalsIgnoreCase(PULGADAS_DOS);
                    String vlrDesc = String.valueOf((int) valorConDescuento);
                    String vlrSubDes = String.valueOf((int) subtotalConDescuento);

                    result.append(obtenerCantidadDetalle(oDetalle));
                    result.append(Utilities.completarEspacios(vlrDesc, twoP ? 9 : 17));
                    result.append(Utilities.completarEspacios(vlrSubDes, twoP ? 9 : 17));
                    result.append(obtenerIvaDetalle(oDetalle));
                    result.append(leftRightSpace());
                }

                if (oDetalle.Devolucion > 0) {
                    float valor_dev = (oDetalle.ValorUnitario * oDetalle.Devolucion);
                    float valor_iva_dev = (((oDetalle.PorcentajeIva / 100) * oDetalle.ValorUnitario) * oDetalle.Devolucion);

                    devoluciones.append(CRNL())
                            .append(leftRightSpace())
                            .append(oDetalle.Devolucion)
                            .append(" ")
                            .append(oDetalle.Nombre)
                            .append(" (")
                            .append(valor_iva_dev)
                            .append(", ")
                            .append(valor_dev)
                            .append(")")
                            .append(leftRightSpace());

                    total_devoluciones += (valor_iva_dev + valor_dev);
                }

                if (oDetalle.Rotacion > 0) {
                    rotaciones.append(CRNL())
                            .append(leftRightSpace())
                            .append(oDetalle.Rotacion)
                            .append(" ")
                            .append(oDetalle.Nombre)
                            .append(leftRightSpace());
                }
            }
        }

        PrinterDetailResume detailResume = new PrinterDetailResume();
        detailResume.cantidadFactura = cantidadFactura;
        detailResume.devoluciones = devoluciones.toString();
        detailResume.totalDevoluciones = total_devoluciones;
        detailResume.rotaciones = rotaciones.toString();
        detailResume.iva5 = iva5;
        detailResume.iva10 = iva10;
        detailResume.iva16 = iva16;
        detailResume.iva19 = iva19;
        detailResume.detailToPrint = result.toString();
        return detailResume;
    }

    private String obtenerNombreProducto(DetalleFacturaDTO oDetalle) {
        StringBuilder result = new StringBuilder();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            result.append(Utilities.quitarTildes(oDetalle.Nombre));
        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)){
            result.append(" ").append(Utilities.quitarTildes(oDetalle.Nombre));
        } else {
            result.append(Utilities.quitarTildes(oDetalle.Nombre));
        }
        return result.toString();
    }

    private String obtenerPorcentajeDescuentoDetalle(DetalleFacturaDTO oDetalle) {
        StringBuilder result = new StringBuilder();
        if (!(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL))) {

            if (oDetalle.PorcentajeDescuento > 0) {
                result.append(" (Descuento:").append(oDetalle.PorcentajeDescuento).append("%) ");
            }
        }

        return result.toString();
    }

    private String obtenerCantidadDetalle(DetalleFacturaDTO oDetalle) throws Exception{
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                return leftRightSpace() + Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 4);
            }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                return leftRightSpace() + Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 12);
            }else{
                throw new Exception("Pulgadas no especificadas");
            }

        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                return CRNL() + leftRightSpace()
                        + Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 5) + "|";
            }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                return CRNL() + leftRightSpace()
                        + Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 10) + "|";
            }else{
                throw new Exception("Pulgadas no especificadas");
            }
        }else {
            if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                return CRNL() + leftRightSpace()
                        + " " + Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 4);
            }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                return CRNL() + leftRightSpace()
                        + " " + Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 12);
            }else{
                throw new Exception("Pulgadas no especificadas");
            }
        }
    }

    private String obtenerIvaDetalle(DetalleFacturaDTO oDetalle) throws Exception{
        String result = "";
        if (!(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL))) {

            if(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)){
                if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                    result = Utilities.completarEspacios(String.valueOf(oDetalle.Iva), 8);
                }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                    result = "  " + Utilities.completarEspacios(String.valueOf(oDetalle.Iva), 8);
                }else{
                    throw new Exception("Pulgadas no especificadas");
                }
            }else{
                if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                    result = Utilities.completarEspacios(String.valueOf(oDetalle.Iva), 8);
                }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                    result = Utilities.completarEspacios(String.valueOf(oDetalle.Iva), 12);
                }else{
                    throw new Exception("Pulgadas no especificadas");
                }
            }

        }
        return result;
    }

    PrinterDetailResume obtenerDetalleRemision(RemisionDTO remision) {

        StringBuilder result = new StringBuilder();
        StringBuilder devoluciones = new StringBuilder();
        float total_devoluciones = 0;
        StringBuilder rotaciones = new StringBuilder();
        int cantidadFactura = 0;

        for (DetalleRemisionDTO oDetalle : remision.DetalleRemision) {
            if (oDetalle.Cantidad > 0) {
                cantidadFactura += oDetalle.Cantidad;

                long valorUnitarioConIVA;
                long validarValorUnitarioConIVA = Math.round(oDetalle.ValorUnitario * ((oDetalle.PorcentajeIva / 100) + 1));
                if (validarValorUnitarioConIVA > 0) {
                    valorUnitarioConIVA = validarValorUnitarioConIVA;
                } else {
                    valorUnitarioConIVA = Math.round(oDetalle.ValorUnitario);
                }

                double valorConDescuento = oDetalle.ValorUnitario;
                double subtotalConDescuento = oDetalle.Subtotal;
                String productName = oDetalle.NombreProducto;

                result.append(NL())
                        .append(leftRightSpace())
                        .append(productName)
                        .append(CRNL())
                        .append(leftRightSpace());

                boolean twoP = pulgadas.equalsIgnoreCase(PULGADAS_DOS);
                String vlrCant = String.valueOf(oDetalle.Cantidad);
                String vlrConDesc = String.valueOf((int) valorConDescuento);
                String vlrSubDes = String.valueOf((int) subtotalConDescuento);
                String valorUnitarioIVA = String.valueOf(valorUnitarioConIVA);
                long valorIva = Math.round((oDetalle.Cantidad * oDetalle.ValorUnitario) * (oDetalle.PorcentajeIva / 100));
                String valorIvaVenta = String.valueOf(valorIva);
                //String vlrIva = String.valueOf((int) oDetalle.Iva);

                String valorSubtotalConIVA = String.valueOf(Math.round(subtotalConDescuento + valorIva));

                if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)) {
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(vlrCant, twoP ? 5: 10)).append("  ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(valorUnitarioIVA, twoP ? 8 : 11)).append("  ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(valorSubtotalConIVA, twoP ? 7 : 10)).append("  ");
                } else {
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(vlrCant, twoP ? 5: 10)).append(" ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(vlrConDesc, twoP ? 8 : 11)).append(" ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(vlrSubDes, twoP ? 7 : 10)).append(" ");
                    result.append(twoP ? "" : " ").append(Utilities.completarEspacios(valorIvaVenta, 8));
                }
                result.append(leftRightSpace());
            }

            if (oDetalle.Devolucion > 0) {

                String cantidadDevolucion = String.valueOf(oDetalle.Devolucion);
                String valorUnitarioDevolucion = String.valueOf(Math.round(oDetalle.ValorUnitario));
                String nombreProductoDevolucion = String.valueOf(oDetalle.NombreProducto);

                long valor_iva_dev = Math.round((oDetalle.Devolucion * oDetalle.ValorUnitario) * (oDetalle.PorcentajeIva / 100));
                long valor_dev = Math.round(oDetalle.ValorUnitario * oDetalle.Devolucion);

                String valorProductorDevolucion = String.valueOf(valor_dev);
                String valorIvaDevolucion = String.valueOf(valor_iva_dev);

                boolean twoP = pulgadas.equalsIgnoreCase(PULGADAS_DOS);

                devoluciones.append(NL())
                        .append(leftRightSpace())
                        .append(nombreProductoDevolucion)
                        .append(CRNL())
                        .append(leftRightSpace());

                if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)) {
                    devoluciones
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(cantidadDevolucion, twoP ? 5: 10)).append("  ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorUnitarioDevolucion, twoP ? 8 : 11)).append("  ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorProductorDevolucion, twoP ? 7 : 10)).append(" ")
                            .append(leftRightSpace());
                } else {
                    devoluciones
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(cantidadDevolucion, twoP ? 5: 10)).append(" ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorUnitarioDevolucion, twoP ? 8 : 11)).append(" ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorProductorDevolucion, twoP ? 7 : 10)).append(" ")
                            .append(twoP ? "" : " ").append(Utilities.completarEspacios(valorIvaDevolucion, 8))
                            .append(leftRightSpace());
                }

                total_devoluciones += oDetalle.ValorDevolucion;
            }

            if (oDetalle.Rotacion > 0) {
                rotaciones.append(CR())
                        .append(leftRightSpace())
                        .append(oDetalle.Rotacion)
                        .append(" ")
                        .append(oDetalle.NombreProducto)
                        .append(leftRightSpace());
            }
        }

        PrinterDetailResume detailResume = new PrinterDetailResume();
        detailResume.cantidadFactura = cantidadFactura;
        detailResume.devoluciones = devoluciones.toString();
        detailResume.totalDevoluciones = total_devoluciones;
        detailResume.rotaciones = rotaciones.toString();
        detailResume.detailToPrint = result.toString();
        return detailResume;
    }

    String obtenerResumenIvas(PrinterDetailResume resume) {
        StringBuilder result = new StringBuilder();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {

            if (resume.iva5 > 0) {
                result.append(leftRightSpace())
                        .append("Iva 5      :").append((int) resume.iva5)
                        .append(CRNL());
            }
            if (resume.iva10 > 0) {
                result.append(leftRightSpace())
                        .append("Iva 10     :").append((int) resume.iva10)
                        .append(CRNL());
            }
            if (resume.iva16 > 0) {
                result.append(leftRightSpace())
                        .append("Iva 16     :").append((int) resume.iva16)
                        .append(CRNL());
            }
            if (resume.iva19 > 0) {
                result.append(leftRightSpace())
                        .append("Iva 19     :").append((int) resume.iva19)
                        .append(CRNL());
            }

            //if (result.length() > 0) result.append(CRNL());
        }

        return result.toString();
    }

    PrinterDetailResume obtenerDetalleNotaCreditoFacturaPorDevolucion(ArrayList<DetalleNotaCreditoFacturaDTO> detalle){
        StringBuilder result = new StringBuilder();
        int cantidadNotaCredito = 0;
        for (DetalleNotaCreditoFacturaDTO oDetalle : detalle) {
            if (oDetalle.Cantidad > 0) {
                cantidadNotaCredito += oDetalle.Cantidad;

                result.append(CRNL())
                        .append(leftRightSpace())
                        .append(oDetalle.Nombre);

                result.append(CRNL()).append(leftRightSpace());
                result.append(" ").append(Utilities.completarEspacios(String.valueOf(oDetalle.Codigo), 4));
                result.append("   ").append(Utilities.completarEspacios(String.valueOf(oDetalle.Cantidad), 4));
                result.append("      ").append(Utilities.completarEspacios(String.valueOf(Math.round(oDetalle.Valor)), 9));
            }
        }

        PrinterDetailResume printerDetailResume = new PrinterDetailResume();
        printerDetailResume.detailToPrint = result.toString();
        printerDetailResume.cantidadNotaCredito = cantidadNotaCredito;
        return printerDetailResume;
    }

    String obtenerTotalIvas(float iva) {
        StringBuilder result = new StringBuilder();
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)) {
            result.append(leftRightSpace()).append("Total Iva  :").append(formatea.format((int) iva));
        } else {
            result.append(leftRightSpace()).append("Iva        :").append(formatea.format((int) iva));
        }
        return result.toString();
    }

    String obtenerRetefuente(double retefuente) {
        StringBuilder result = new StringBuilder();
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        if (retefuente >0) {
            result.append(leftRightSpace())
                    .append("Ret.Fte    :")
                    .append(formatea.format((int) retefuente));
        }
        return result.toString();
    }

    String obtenerIpoconsumo(float ipoconsumo) {
        StringBuilder result = new StringBuilder();
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        if (!(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL))) {
            if (ipoconsumo > 0.0) {
                result.append(leftRightSpace())
                        .append("Ipoconsumo :")
                        .append(formatea.format((int) ipoconsumo));
            }
        }
        return result.toString();
    }

    String obtenerResolucionFacturacion(boolean clienteGenericoPos) {
        StringBuilder result = new StringBuilder();
        String tipoResolucion = App.obtenerConfiguracion_TipoResolucion(mContext);

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE)) {

            if(clienteGenericoPos)
                result.append(obtenerResolucionIgluPos());
            else
                result.append(obtenerResolucionIglu(tipoResolucion));

        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_FOGON_PAISA)) {

            if(clienteGenericoPos)
                result.append(obtenerResolucionFogonPaisaPos());
            else
                result.append(obtenerResolucionFogonPaisa(tipoResolucion));

        } else if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_VEGA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_NATIPAN)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            if (clienteGenericoPos)
                result.append(obtenerResolucionVegaPos());
            else
                result.append(obtenerResolucionVega(tipoResolucion));
        }/* else if(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_COMESCOL)) {
            result.append(obtenerResolucionComescol(tipoResolucion));
        }*/ else if (!ActivityBase.resolucion.IdCliente.equals(Utilities.ID_SARY) ||
                (!TextUtils.isEmpty(ActivityBase.resolucion.CodigoRuta)
                        && ActivityBase.resolucion.CodigoRuta.equals("2"))) {

            if(clienteGenericoPos)
                result.append(obtenerResolucionPos());
            else
                result.append(obtenerResolucion(tipoResolucion));
        }

        return result.toString();
    }

    private StringBuilder obtenerResolucion(String tipo) {
        StringBuilder result = new StringBuilder();

        switch (tipo) {
            case ResolucionDTO.TIPO_ELECTRONICA:
                result.append(CRNL()).append(leftRightSpace())
                        .append("Resolucion Dian Factura Electronica");
                break;
            case ResolucionDTO.TIPO_COMPUTADOR:
                result.append(CRNL()).append(leftRightSpace())
                        .append("Resolucion Dian Por Computador");
                break;
            case ResolucionDTO.TIPO_POS:
                result.append(CRNL()).append(leftRightSpace())
                        .append("Resolucion Dian POS");
                break;
        }

        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Inicial Fact: ").append(ActivityBase.resolucion.FacturaInicial);
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Final   Fact: ").append(ActivityBase.resolucion.FacturaFinal);
        result.append(CRNL()).append(leftRightSpace())
                .append("Resolucion      : ").append(ActivityBase.resolucion.Resolucion);
        result.append(CRNL()).append(leftRightSpace())
                .append("Fec.Resolucion  : ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucion);

        return result;
    }

    private StringBuilder obtenerResolucionPos() {
        StringBuilder result = new StringBuilder();

        result.append(CRNL()).append(leftRightSpace())
                        .append("Resolucion Dian POS");
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Inicial Fact: ").append(ActivityBase.resolucion.FacturaInicialPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Final   Fact: ").append(ActivityBase.resolucion.FacturaFinalPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Resolucion      : ").append(ActivityBase.resolucion.ResolucionPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Fec.Resolucion  : ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucionPOS);

        return result;
    }

    private StringBuilder obtenerResolucionIglu(String tipo) {
        StringBuilder result = new StringBuilder();
        switch (tipo) {
            case ResolucionDTO.TIPO_ELECTRONICA:
                result.append(CRNL()).append(leftRightSpace())
                        .append("RESOLUCION DIAN FACTURA ELECTRÓNICA");
                break;
            case ResolucionDTO.TIPO_COMPUTADOR:
                result.append(CRNL()).append(leftRightSpace())
                        .append("RESOLUCION DIAN POR COMPUTADOR");
                break;
            case ResolucionDTO.TIPO_POS:
                result.append(CRNL()).append(leftRightSpace())
                        .append("RESOLUCION DIAN POS");
                break;
        }

        result.append(CRNL()).append(leftRightSpace())
                .append("NUMERO ").append(ActivityBase.resolucion.Resolucion)
                .append(CRNL()).append(leftRightSpace())
                .append("AUTORIZA DESDE ").append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaInicial)
                .append(CRNL()).append(leftRightSpace())
                .append("HASTA ").append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaFinal)
                .append(CRNL()).append(leftRightSpace())
                .append("FECHA RESOLUCION ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucion)
                .append(CRNL()).append(leftRightSpace())
                .append("VIGENCIA RESOLUCION ").append(getVigenciaResolucionEnMeses());

        return result;
    }

    private StringBuilder obtenerResolucionIgluPos() {
        StringBuilder result = new StringBuilder();

        result.append(CRNL()).append(leftRightSpace())
                .append("RESOLUCION DIAN POS");

        result.append(CRNL()).append(leftRightSpace())
                .append("NUMERO ").append(ActivityBase.resolucion.ResolucionPOS)
                .append(CRNL()).append(leftRightSpace())
                .append("AUTORIZA DESDE ").append(ActivityBase.resolucion.PrefijoFacturacionPOS)
                .append("-").append(ActivityBase.resolucion.FacturaInicialPOS)
                .append(CRNL()).append(leftRightSpace())
                .append("HASTA ").append(ActivityBase.resolucion.PrefijoFacturacionPOS)
                .append("-").append(ActivityBase.resolucion.FacturaFinalPOS)
                .append(CRNL()).append(leftRightSpace())
                .append("FECHA RESOLUCION ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucionPOS)
                .append(CRNL());

        return result;
    }

    private StringBuilder obtenerResolucionFogonPaisa(String tipo) {
        StringBuilder result = new StringBuilder();
        result.append(CRNL()).append(leftRightSpace()).append("Resolucion   Nro: ");
        switch (tipo) {
            case ResolucionDTO.TIPO_ELECTRONICA:
                result.append(ActivityBase.resolucion.Resolucion).append(" Electrónica");
                break;
            case ResolucionDTO.TIPO_COMPUTADOR:
                result.append(ActivityBase.resolucion.Resolucion).append(" Por computador");
                break;
            case ResolucionDTO.TIPO_POS:
                result.append(ActivityBase.resolucion.Resolucion).append(" Pos");
                break;
        }

        result.append(CRNL()).append(leftRightSpace())
                .append("Desde           : ")
                .append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaInicial);
        result.append(CRNL()).append(leftRightSpace())
                .append("Hasta           : ")
                .append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaFinal);
        result.append(CRNL()).append(leftRightSpace())
                .append("Fecha Resolucion: ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucion);

        return result;
    }

    private StringBuilder obtenerResolucionFogonPaisaPos() {
        StringBuilder result = new StringBuilder();
        result.append(CRNL()).append(leftRightSpace()).append("Resolucion   Nro: ");
        result.append(ActivityBase.resolucion.ResolucionPOS).append(" Pos");

        result.append(CRNL()).append(leftRightSpace())
                .append("Desde           : ")
                .append(ActivityBase.resolucion.PrefijoFacturacionPOS)
                .append("-").append(ActivityBase.resolucion.FacturaInicialPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Hasta           : ")
                .append(ActivityBase.resolucion.PrefijoFacturacionPOS)
                .append("-").append(ActivityBase.resolucion.FacturaFinalPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Fecha Resolucion: ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucionPOS);

        return result;
    }

    private StringBuilder obtenerResolucionVega(String tipo) {
        StringBuilder result = new StringBuilder();
        result.append(CRNL()).append(leftRightSpace())
                .append("No somos grandes contribuyentes, ni autoretenedores. ");

        if (Utilities.fechaResolucionEsMayorA5julio2016(mContext)) {
            result.append(CRNL()).append(leftRightSpace()).append("Formulario Dian #");
        } else {
            result.append(CRNL()).append(leftRightSpace()).append("Resolucion Nro ");
        }

        result.append(ActivityBase.resolucion.Resolucion).append(" ").append(tipo.toLowerCase());
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Inicial Fact: ")
                .append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaInicial);
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Final   Fact: ")
                .append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaFinal);
        result.append(CRNL()).append(leftRightSpace())
                .append("Resolucion      : ").append(ActivityBase.resolucion.Resolucion);
        result.append(CRNL()).append(leftRightSpace())
                .append("Fec.Resolucion  : ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucion);

        return result;
    }

    private StringBuilder obtenerResolucionVegaPos() {
        StringBuilder result = new StringBuilder();
        result.append(CRNL()).append(leftRightSpace())
                .append("No somos grandes contribuyentes, ni autoretenedores. ");

        if (Utilities.fechaResolucionEsMayorA5julio2016(mContext)) {
            result.append(CRNL()).append(leftRightSpace()).append("Formulario Dian #");
        } else {
            result.append(CRNL()).append(leftRightSpace()).append("Resolucion Nro ");
        }

        result.append(ActivityBase.resolucion.ResolucionPOS).append(" POS");
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Inicial Fact: ")
                .append(ActivityBase.resolucion.PrefijoFacturacionPOS)
                .append("-").append(ActivityBase.resolucion.FacturaInicialPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Final   Fact: ")
                .append(ActivityBase.resolucion.PrefijoFacturacionPOS)
                .append("-").append(ActivityBase.resolucion.FacturaFinalPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Resolucion      : ")
                .append(ActivityBase.resolucion.ResolucionPOS);
        result.append(CRNL()).append(leftRightSpace())
                .append("Fec.Resolucion  : ")
                .append(CRNL()).append(leftRightSpace())
                .append(ActivityBase.resolucion.FechaResolucionPOS);

        return result;
    }

    private StringBuilder obtenerResolucionComescol(String tipo) {
        StringBuilder result = new StringBuilder();
        if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)) {

            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("NO SOMOS AUTORRETENEDORES, NI ");
            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("GRANDES CONTRIBUYENTES");

            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("ACTIVIDAD ECONOMICA 1053 - 1081");

            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("- 4631 TARIFA ICA 4,14 x 1000");

        }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("NO SOMOS AUTORRETENEDORES, NI GRANDES ");

            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("CONTRIBUYENTES. ");

            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("ACTIVIDAD ECONOMICA 1052 - 1081 - 4631 ");

            result.append(CRNL())
                    .append(leftRightSpace())
                    .append("TARIFA ICA 4,14 x 1000");
        }

        result.append(CRNL()).append(leftRightSpace()).append("Resolucion Nro ");
        result.append(ActivityBase.resolucion.Resolucion).append(" ").append(tipo.toLowerCase());
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Inicial Fact: ")
                .append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaInicial);
        result.append(CRNL()).append(leftRightSpace())
                .append("Num.Final   Fact: ")
                .append(ActivityBase.resolucion.PrefijoFacturacion)
                .append("-").append(ActivityBase.resolucion.FacturaFinal);
        result.append(CRNL()).append(leftRightSpace())
                .append("Resolucion      : ").append(ActivityBase.resolucion.Resolucion);

        result.append(CRNL()).append(leftRightSpace())
                .append("Fec.Resolucion  : ")
                .append(ActivityBase.resolucion.FechaResolucion);

        return result;
    }

    String obtenerPieDePagina() {
        StringBuilder result = new StringBuilder();

        switch (ActivityBase.resolucion.IdCliente){
            case Utilities.ID_IGLU:
            case Utilities.ID_POLAR:

                result.append(CRNL()).append(obtenerSeparador());

                result.append(CRNL()).append(leftRightSpace())
                        .append("NO SOMOS GRANDES CONTRIBUYENTES NI")
                        .append("RESPONSABLES DE IVA")
                        .append(CRNL()).append(leftRightSpace());

                result.append(leftRightSpace())
                        .append("ESTA FACTURA DE VENTA SE ASIMILA EN SUS")
                        .append(leftRightSpace()).append(CRNL())
                        .append(leftRightSpace())
                        .append("EFECTOS A LA LETRA DE CAMBIO Y PRESTA")
                        .append(leftRightSpace()).append(CRNL())
                        .append(leftRightSpace())
                        .append("MERITO EJECUTIVO DE CONFORMIDAD CON EL")
                        .append(leftRightSpace()).append(CRNL())
                        .append(leftRightSpace())
                        .append("ARTICULO 619 Y SUBSIGUIENTES DEL CODIGO")
                        .append(leftRightSpace()).append(CRNL())
                        .append(leftRightSpace())
                        .append("DE COMERCIO")
                        .append(leftRightSpace());

                if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)) {
                    result.append(obtenerSeparador());
                    result.append(CRNL()).append(leftRightSpace())
                            .append("servicioalcliente@hieloiglu.com");
                }
                break;

            case Utilities.ID_ALMIRANTE:
                /*result.append(CRNL()).append(obtenerSeparador());
                result.append(CRNL()).append(leftRightSpace())
                        .append("NO PRACTICAR RETENCION EN LA FUENTE")
                        .append(leftRightSpace()).append(CRNL())
                        .append(leftRightSpace()).append("ARTICULO 4 LEY 1429 DE DICIEMBRE")
                        .append(leftRightSpace()).append(CRNL())
                        .append(leftRightSpace()).append("29 DE 2010")
                        .append(leftRightSpace());*/
                break;
            case Utilities.ID_VEGA:
            case Utilities.ID_NATIPAN:
            case Utilities.ID_PRUEBAS_TIMOVIL:
                result.append(CRNL()).append(obtenerSeparador());
                if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                    result.append(CRNL()).append(leftRightSpace())
                            .append("ESTA FACTURA SE ASIMILA EN TODOS")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("SUS EFECTOS LEGALES A UNA LETRA DE")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("CAMBIO SEGUN ARTICULO 774 DEL")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace()).append("CODIGO DE COMERCIO");
                }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                    result.append(CRNL()).append(leftRightSpace())
                            .append("ESTA FACTURA SE ASIMILA EN TODOS SUS EFECTOS")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("LEGALES A UNA LETRA DE CAMBIO SEGUN ARTICULO")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("774 DEL CODIGO DE COMERCIO");
                }
                break;
            case Utilities.ID_COMESCOL:
                result.append(CRNL()).append(obtenerSeparador());
                if(pulgadas.equalsIgnoreCase(PULGADAS_DOS)){
                    result.append(CRNL()).append(leftRightSpace())
                            .append("ESTA FACTURA SE ASIMILA EN TODOS")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("SUS EFECTOS LEGALES A UNA LETRA DE")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("CAMBIO SEGUN ARTICULO 774 DEL")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace()).append("CODIGO DE COMERCIO");

                    result.append(CRNL()).append(leftRightSpace())
                            .append("DESPUES DEL VENCIMIENTO SE COBRARAN")
                            .append("INTERESES DE MORA BANCARIOS");

                }else if(pulgadas.equalsIgnoreCase(PULGADAS_TRES)){
                    result.append(CRNL()).append(leftRightSpace())
                            .append("ESTA FACTURA SE ASIMILA EN TODOS SUS EFECTOS")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("LEGALES A UNA LETRA DE CAMBIO SEGUN ARTICULO")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("774 DEL CODIGO DE COMERCIO");

                    result.append(CRNL()).append(leftRightSpace())
                            .append("DESPUES DEL VENCIMIENTO SE COBRARAN INTERESES")
                            .append(leftRightSpace()).append(CRNL())
                            .append(leftRightSpace())
                            .append("DE MORA BANCARIOS");
                }
                break;
        }

        return result.toString();
    }

    String obtenerNumeroOrden(FacturaDTO factura) {
        StringBuilder result = new StringBuilder();
        if ((ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE))
                && factura.NumeroPedido != null
                && !factura.NumeroPedido.trim().equals("")
                && !factura.NumeroPedido.equals(factura.NumeroFactura)) {
            result.append(CRNL());
            result.append(leftRightSpace())
                    .append("Numero orden: ").append(factura.NumeroPedido);
        }
        return result.toString();
    }

    String obtenerNumeroOrden(RemisionDTO remision) {
        StringBuilder result = new StringBuilder();
        if ((ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE))
                && remision.NumeroPedido != null
                && !remision.NumeroPedido.trim().equals("")
                && !remision.NumeroPedido.equals(remision.NumeroRemision)) {
            result.append(CRNL());
            result.append(leftRightSpace())
                    .append("Numero orden: ").append(remision.NumeroPedido);
        }
        return result.toString();
    }

    String obtenerFirmas() {
        StringBuilder result = new StringBuilder();

        if(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_GUAYAZAN)){
            result.append(CRNL()).append("ANTES DE CANCELAR VERIFIQUE SU PEDIDO").append(CRNL());
        }

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_FR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)) {
            result.append(obtenerUrlTimovil());
        } else {

            if(!ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DISTRIBUIDOR_PAISAPAN)){
                result.append(CRNL()).append(obtenerTituloFirmas());
                result.append(CRNL());
                result.append(CRNL()).append(leftRightSpace()).append("Cliente  :________________");
                result.append(CRNL());
                result.append(CRNL());
                result.append(CRNL()).append(leftRightSpace()).append("Vendedor :________________");
                result.append(CRNL());
            }

            if (!(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE))) {
                result.append(obtenerPieDePaginaZibor());
            }
        }

        return result.toString();
    }

    String obtenerFirmasRemision() {
        StringBuilder result = new StringBuilder();

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_FR)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PANIFICADORA)) {
            result.append(obtenerUrlTimovil());
        } else {

            if(!ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DISTRIBUIDOR_PAISAPAN)){
                result.append(CRNL()).append(obtenerTituloFirmas());
                result.append(CRNL());
                result.append(CRNL()).append(leftRightSpace()).append("Funcionario :________________");
                result.append(CRNL());

                if (!ActivityBase.resolucion.IdCliente.equals(Utilities.ID_DISTRIBUCIONES_WILL)){
                    result.append(CRNL()).append(leftRightSpace()).append("Vendedor :________________");
                    result.append(CRNL());
                    result.append(CRNL());
                    result.append(CRNL());
                    result.append(CRNL());
                }
            }

            if (!(ActivityBase.resolucion.IdCliente.equals(Utilities.ID_IGLU)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_POLAR)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_ALMIRANTE))
                    ) {
                result.append(obtenerPieDePaginaZibor());
            }
        }
        return result.toString();
    }

    String obtenerTotalRemisiones(RemisionDTO remision, PrinterDetailResume detailResume){

        DecimalFormat formatea = new DecimalFormat("###,###.##");

        ClienteDTO clienteDTO = obtenerCliente(remision.IdCliente);

        StringBuilder result = new StringBuilder();
        result.append(CRNL());
        result.append(obtenerTituloTotales());
        result.append(CRNL());
        result.append(leftRightSpace());
        result.append("Cantidad          :").append(Math.round(detailResume.cantidadFactura));
        result.append(CRNL());
        result.append(leftRightSpace());
        result.append("Subtotal          :").append(formatea.format((Math.round(remision.Subtotal))));
        result.append(CRNL());
        result.append(leftRightSpace());
        result.append("Descuento         :").append(formatea.format(Math.round(remision.Descuento)));
        result.append(CRNL());
        result.append(leftRightSpace());

        double retefuente = obtenerValorRetefuente(remision.Subtotal, remision.Descuento);
        boolean esActivoRetefuente = clienteDTO.ReteFuente;
        boolean esActivoReteIva = clienteDTO.ReteIva;

        result.append("IVA               :").append(formatea.format(Math.round(remision.Iva)));
        result.append(CRNL());
        result.append(leftRightSpace());
        result.append("Total venta       :").append(formatea.format(Math.round(((remision.Subtotal - remision.Descuento) + remision.Iva))));
        result.append(CRNL());
        result.append(leftRightSpace());

        if (esActivoRetefuente && retefuente > 0) {
            result.append("Retefuente        :").append(formatea.format(Math.round(retefuente)));
            result.append(CRNL());
            result.append(leftRightSpace());
        }

        if (esActivoReteIva && remision.ValorReteIva > 0) {
            result.append("ReteIVA           :").append(formatea.format(Math.round(remision.ValorReteIva)));
            result.append(CRNL());
            result.append(leftRightSpace());
        }

        if (ActivityBase.resolucion.DevolucionAfectaRemision) {
            double valorSubtotalDevolucion = 0;
            double valorTotalDescuentoDevolucion = 0;
            double valorTotalIvaDevolucion = 0;
            for (DetalleRemisionDTO detalleRemision: remision.DetalleRemision) {
                valorSubtotalDevolucion = valorSubtotalDevolucion + (detalleRemision.Devolucion * detalleRemision.ValorUnitario);
                valorTotalDescuentoDevolucion = valorTotalDescuentoDevolucion + ((detalleRemision.Devolucion * detalleRemision.ValorUnitario) * (detalleRemision.PorcentajeDescuento / 100));
                valorTotalIvaDevolucion = valorTotalIvaDevolucion + (((detalleRemision.Devolucion * detalleRemision.ValorUnitario) - ((detalleRemision.Devolucion * detalleRemision.ValorUnitario) * (detalleRemision.PorcentajeDescuento / 100))) * (detalleRemision.PorcentajeIva / 100));
            }

            double valorTotalRetefuenteDevolucion = (valorSubtotalDevolucion - valorTotalDescuentoDevolucion) * (ActivityBase.resolucion.PorcentajeRetefuente / 100);

            if (!ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)) {
                result.append("Subtotal (NC)     :").append(formatea.format(Math.round(valorSubtotalDevolucion)));
                result.append(CRNL());
                result.append(leftRightSpace());
                result.append("Descuento Dev (NC):").append(formatea.format(Math.round(valorTotalDescuentoDevolucion)));
                result.append(CRNL());
                result.append(leftRightSpace());
                result.append("IVA Dev (NC)      :").append(formatea.format(Math.round(valorTotalIvaDevolucion)));
                result.append(CRNL());
                result.append(leftRightSpace());
            }
            if (esActivoRetefuente && valorSubtotalDevolucion > 0 && retefuente > 0) {
                result.append("Retefuente (NC)   :").append(formatea.format(Math.round(valorTotalRetefuenteDevolucion)));
                result.append(CRNL());
                result.append(leftRightSpace());
            }
            if (esActivoReteIva && valorTotalIvaDevolucion > 0 && remision.ValorReteIva > 0) {
                result.append("ReteIVA (NC)      :").append(formatea.format(Math.round(remision.ValorReteIvaDevolucion)));
                result.append(CRNL());
                result.append(leftRightSpace());
            }

            if (retefuente > 0) {
                result.append("Total (NC)        :").append(formatea.format(Math.round((((valorSubtotalDevolucion - valorTotalDescuentoDevolucion) + valorTotalIvaDevolucion) - valorTotalRetefuenteDevolucion) - remision.ValorReteIvaDevolucion)));
            } else {
                result.append("Total (NC)        :").append(formatea.format(Math.round(((valorSubtotalDevolucion - valorTotalDescuentoDevolucion) + valorTotalIvaDevolucion))));
            }
        }
        return result.toString();
    }

    //region ZEBRA UTILS

    String zebraObtenerComandoInicial(int height) {
        return "! 0 200 200 " + height + " 1" + CR + NL;
        /*
         * <!> {offset} <200> <200> {height} {qty}
         *  where:
         *  <!>: Use ‘!’ to begin a control session.
         *  {offset}:The horizontal offset for the entire label. This value causes all fields to
         *  be offset horizontally by the specified number of UNITS.
         *  <200>:Horizontal resolution (in dots-per-inch).
         *  <200>:Vertical resolution (in dots-per-inch).
         *  {height}:The maximum height of the label.
         *  {qty}: Quantity of labels to be printed. Maximum = 1024.
         */
    }

    String zebraObtenerComandoTipoCodificacion() {
        return "ENCODING UTF-8" + CR + NL;
        //“ASCII” - “UTF-8” - “GB18030”
    }

    String zebraObtenerComandoMultilinea() {
        return "ML 40" + CR + NL;
        /*
         * ML COMMAND
         * {command} {height}
         * where:
         * <ML>:  allows you to print multiple lines of text using the same font and line-height
         * <40>: specified line-height
         */
    }

    String zebraObtenerComandoCancelarMultilinea() {
        return "ENDML" + CR + NL;
    }

    String zebraObtenerComandoFormatoTexto() {
        return "TEXT 7 0 2 20" + CR + NL;
        /*
         * TEXT COMMAND
         * {command} {font} {size} {x} {y} {data}
         * {font}: Name/number of the font.
         * {size}: Size identifier for the font.
         * {x}: Horizontal starting position.
         * {y}: Vertical starting position.
         * {data}: The text to be printed
         */

    }

    String zebraObtenerComandoFormatoTextoBold() {
        return "TEXT 5 0 2 20" + CR + NL;
        /*
         * TEXT COMMAND
         * {command} {font} {size} {x} {y} {data}
         * {font}: Name/number of the font.
         * {size}: Size identifier for the font.
         * {x}: Horizontal starting position.
         * {y}: Vertical starting position.
         * {data}: The text to be printed
         */

    }

    //String zebraObtenerComandoForm() {
    //    return "FORM" + CR + NL;
        //The FORM command instructs the printer to feed to top of form after printing.
    //}F

    String zebraObtenerComandoPrint() {
        return "PRINT" + CR + NL;
        /*
         * The PRINT command terminates and prints the file. This must always be the last command (except
         * when in Line Print Mode). Upon execution of the PRINT command, the printer will exit from a control
         * session. Be sure to terminate this and all commands with both carriage-return and line-feed characters.
         */
    }

    String zebraObtenerComandoCenter() {
        return "CENTER" + CR + NL;
        /*
         * CENTER: Center justifies all subsequent fields.
         * LEFT: Left justifies all subsequent fields.
         * RIGHT: Right justifies all subsequent fields.
         */
    }

    //String zebraObtenerComandoLeft() {
    //    return "LEFT" + CR + NL;
    //    /*
    //     * CENTER: Center justifies all subsequent fields.
    //     * LEFT: Left justifies all subsequent fields.
    //     * RIGHT: Right justifies all subsequent fields.
    //     */
    //}

    String zebraObtenerComandoRight() {
        return "RIGHT" + CR + NL;
        /*
         * CENTER: Center justifies all subsequent fields.
         * LEFT: Left justifies all subsequent fields.
         * RIGHT: Right justifies all subsequent fields.
         */
    }

    //String zebraObtenerComandoNegrita() {
    //    return " ! U1 SETBOLD 0" + CR + NL;
    //}

    //end-region

    /***
     * Calcula los meses entre la Fecha de la resolución, y la fecha Vigencia de la resolución
     * @return # MESES
     */

    private String getVigenciaResolucionEnMeses() {

        String fechaDeResolucion = App.obtenerConfiguracion_FechaDeResolucion(mContext);
        String vigenciaDeResolucion = App.obtenerConfiguracion_FechaVigenciaDeResolucion(mContext);

        if (TextUtils.isEmpty(fechaDeResolucion) || TextUtils.isEmpty(vigenciaDeResolucion)) {
            return "";
        }

        String[] arrayFechaDeResolucion = fechaDeResolucion.split("\\.");
        String[] arrayVigenciaDeResolucion = vigenciaDeResolucion.split("\\.");

        DateTime date1 = new DateTime(Integer.parseInt(arrayFechaDeResolucion[0]),
                Integer.parseInt(arrayFechaDeResolucion[1]), Integer.parseInt(arrayFechaDeResolucion[2]), 0, 0);

        DateTime date2 = new DateTime(Integer.parseInt(arrayVigenciaDeResolucion[0]),
                Integer.parseInt(arrayVigenciaDeResolucion[1]), Integer.parseInt(arrayVigenciaDeResolucion[2]), 0, 0);

        DateTime lastDayOfMonth1 = date1.dayOfMonth().withMaximumValue();
        DateTime lastDayOfMonth2 = date2.dayOfMonth().withMaximumValue();

        int dayDate1 = date1.dayOfMonth().get();
        int yearDate1 = date1.year().get();
        int monthDate1 = date1.monthOfYear().get();

        int dayDate2 = date2.dayOfMonth().get();
        int yearDate2 = date2.year().get();
        int monthDate2 = date2.monthOfYear().get();

        int finalDay1 = (dayDate1 > 15 ? lastDayOfMonth1.dayOfMonth().get() : 1);
        int finalDay2 = (dayDate2 > 15 ? lastDayOfMonth2.dayOfMonth().get() : 1);

        DateTime finalDate1 = new DateTime(yearDate1, monthDate1, finalDay1, 0, 0);
        DateTime finalDate2 = new DateTime(yearDate2, monthDate2, finalDay2, 0, 0);

        finalDate1 = finalDate1.plusDays(-1);

        //System.out.println(finalDate1);
        //System.out.println(finalDate2);
        //System.out.println(Months.monthsBetween(finalDate1, finalDate2).getMonths(););
        return leftRightSpace() + Months.monthsBetween(finalDate1, finalDate2).getMonths() + " MESES";
    }

    public String obtenerImpresionIventario(){
        StringBuilder textoToPrint = new StringBuilder();
        textoToPrint.append(leftRightSpace())
                .append(Utilities.completarEspacios("Producto", 18))
                .append(CRNL())
                .append(leftRightSpace())
                .append(" ").append("Inicial")
                .append(" ").append("Ventas")
                .append(" ").append("Devol")
                .append(" ").append("Saldo")
                .append(CRNL())
                .append(CRNL());

        ArrayList<ProductoDTO> listado = new ProductoDAL(mContext).obtenerListadoCompleto();
        for (ProductoDTO prod : listado) {
            if(prod.StockInicial > 0){
                textoToPrint.append(leftRightSpace())
                        .append(Utilities.completarEspacios(prod.Nombre, 20)).append(CRNL());

                textoToPrint.append(leftRightSpace())
                        .append("  ").append(Utilities.completarEspacios(String.valueOf(prod.StockInicial), 7)).append("  ");
                textoToPrint.append(Utilities.completarEspacios(String.valueOf(prod.Ventas), 5)).append("  ");
                textoToPrint.append(Utilities.completarEspacios(String.valueOf(prod.Devoluciones), 5)).append("  ");
                textoToPrint.append(prod.getStock(ActivityBase.resolucion)).append(CRNL());

            }

        }
        return textoToPrint.toString();
    }

    //region decode Bitmap

    private String hexStr = "0123456789ABCDEF";
    private String[] binaryArray = { "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111" };

    public byte[] decodeBitmap(Bitmap bmp){
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<String>(); //binaryString list
        StringBuffer sb;


        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;

        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }

        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        String widthHexString = Integer
                .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8
                        : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 2) {
            Log.e("decodeBitmap error", " height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString+widthHexString+heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    private List<String> binaryListToHexStringList(List<String> list) {
        List<String> hexList = new ArrayList<String>();
        for (String binaryStr : list) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    private String myBinaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }

        return hex;
    }

    private byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        return sysCopy(commandList);
    }

    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    //endregion
}
