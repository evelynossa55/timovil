package com.cm.timovil2.bl.printers.printer_v2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.citizen.jpos.command.ESCPOSConst;
import com.citizen.jpos.printer.CMPPrint;
import com.citizen.jpos.printer.ESCPOSPrinter;
import com.citizen.port.android.BluetoothPort;
import com.citizen.request.android.RequestHandler;
import com.cm.timovil2.bl.calculus.Calculable;
import com.cm.timovil2.bl.calculus.Calculator;
import com.cm.timovil2.bl.calculus.CalculatorFactory;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResumenVentasImpresionDTO;
import com.cm.timovil2.front.ActivityBase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 30/07/18.
 */

class PrinterCITIZEN extends Printer {

    private final ESCPOSPrinter mPrinter;
    private Thread mThread;
    private BluetoothPort mBluetoothPort;
    private BluetoothAdapter mBluetoothAdapter;
    private FacturaDTO mFactura;
    private List<FacturaDTO> mFacturas;
    private RemisionDTO mRemision;
    private List<RemisionDTO> mRemisiones;
    private NotaCreditoFacturaDTO mNota;
    private ResumenVentasImpresionDTO mResumenVentas;
    private boolean mImprimirInventario;
    NumberFormat formatter = null;

    PrinterCITIZEN(String macAdrress, ActivityBase context, int numeroCopias, String pulgadas) {
        super(macAdrress, context, numeroCopias, pulgadas);
        mPrinter = new ESCPOSPrinter();
        formatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    void connect() throws Exception {
        bluetoothSetup();
        new connTask().execute();
    }

    @Override
    public void printConfig() throws Exception {
        if (!mIsConnected) {
            connect();
        } else {
            Thread.sleep(2000);
            printConfigAfterConnect();
        }
    }

    @Override
    public void print(FacturaDTO factura) throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mFactura = factura;
        if (!mIsConnected) {
            connect();
        } else {
            Thread.sleep(2000);
            printFacturaAfterConnect();
        }
    }

    @Override
    public void printFacturas(List<FacturaDTO> facturas) throws Exception{

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mFacturas = facturas;
        if(!mIsConnected){
            connect();
        }else{
            printFacturasAfterConnect();
        }
    }

    @Override
    public void print(RemisionDTO remisionDTO) throws Exception {
        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mRemision = remisionDTO;
        if (!mIsConnected) {
            connect();
        } else {
            Thread.sleep(2000);
            printRemisionAfterConnect();
        }
    }

    @Override
    public void printRemisiones(List<RemisionDTO> remisinesDTO) throws Exception{
        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mRemisiones = remisinesDTO;
        if(!mIsConnected){
            connect();
        }else{
            printRemisionesAfterConnect();
        }
    }

    @Override
    public void print(NotaCreditoFacturaDTO nota) throws Exception {
        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mNota = nota;
        if (!mIsConnected) {
            connect();
        } else {
            Thread.sleep(2000);
            printNotaAfterConnect();
        }
    }

    @Override
    public void printFacturaComoRemision(FacturaDTO facturaDTO) throws Exception{

        Calculator calculator = CalculatorFactory.getCalculator(ActivityBase.resolucion, facturaDTO);
        if(calculator == null) return;

        mRemision = (RemisionDTO) calculator.convert(Calculable.REMISION_TYPE);
        print(mRemision);
    }

    @Override
    public void print(ResumenVentasImpresionDTO resumen) throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mResumenVentas = resumen;
        if (!mIsConnected) {
            connect();
        } else {
            Thread.sleep(2000);
            printResumenVentasAfterConnect();
        }
    }

    @Override
    public void printInventario() throws Exception{
        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        mImprimirInventario = true;
        if (!mIsConnected) {
            connect();
        } else {
            Thread.sleep(2000);
            printInventarioAfterConnect();
        }
    }

    private void printFacturaAfterConnect() throws Exception {
        if (mIsConnected) {

            try {

                if (ActivityBase.resolucion != null) {

                    for (int i = 0; i < mNumeroCopias; i++) {
                        printFactura(mFactura);
                        Thread.sleep(1500);
                    }

                    //IMPRIMIR NOTA CREDITO, SI LA FACTURA TIENE
                    if (mFactura.notaCreditoFactura != null) {
                        mNota = mFactura.notaCreditoFactura;
                        printNotaAfterConnect();
                    }else {
                        Thread.sleep(1000);
                        new Desconexion().execute("");
                    }
                }

            } catch (UnsupportedEncodingException e) {
                throw new Exception("Error imprimiendo(UnsupportedEncodingException): " +
                        e.getMessage());
            } catch (Exception e) {
                throw new Exception("Error imprimiendo: " + e.getMessage());
            }
        }
    }

    private void printFacturasAfterConnect() throws Exception {
        if (mIsConnected) {

            try {

                if (ActivityBase.resolucion != null && mFacturas != null) {

                    for (FacturaDTO factura : mFacturas) {
                        printFactura(factura);
                        Thread.sleep(1500);
                    }

                    Thread.sleep(2000 * mFacturas.size());
                    new Desconexion().execute("");
                }

            } catch (UnsupportedEncodingException e) {
                throw new Exception("Error imprimiendo(UnsupportedEncodingException): " +
                        e.getMessage());
            } catch (Exception e) {
                throw new Exception("Error imprimiendo: " + e.getMessage());
            }
        }
    }

    private void printFactura(FacturaDTO factura) throws Exception {
        if(mIsConnected){

            PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

            mPrinter.printText(printerUtil.obtenerEncabezado() + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_1HEIGHT);
            mPrinter.printText(ActivityBase.resolucion.NombreComercial + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_DEFAULT,
                    CMPPrint.CMP_TXT_1HEIGHT);
            mPrinter.printText("Nit:" + ActivityBase.resolucion.Nit + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_DEFAULT,
                    CMPPrint.CMP_TXT_1HEIGHT);
            mPrinter.lineFeed(1);

            mPrinter.printNormal(printerUtil.obtenerRegimen() + printerUtil.CRNL());
            mPrinter.printNormal(printerUtil.obtenerDatosContacto() + printerUtil.CRNL());
            mPrinter.printNormal(printerUtil.obtenerEmail() + printerUtil.CRNL());
            mPrinter.lineFeed(1);
            mPrinter.printNormal("Vendedor: " + ActivityBase.resolucion.CodigoRuta + printerUtil.CRNL());
            mPrinter.printNormal("Nombre: " + ActivityBase.resolucion.NombreRuta + printerUtil.CRNL());
            mPrinter.printNormal(printerUtil.obtenerNumeroCelularRuta() + printerUtil.CRNL());
            mPrinter.printNormal(printerUtil.obtenerDatosEntregador(factura.IdEmpleadoEntregador) + printerUtil.CRNL());
            mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());

            mPrinter.printText(printerUtil.obtenerTipoDocumentoNro(factura.FacturaPos, ActivityBase.resolucion.CodigoRuta) + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_2HEIGHT);

            mPrinter.printText(factura.NumeroFactura + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_2HEIGHT);

            mPrinter.printText(printerUtil.obtenerReglamentacion1() + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_2HEIGHT);
            mPrinter.printText(printerUtil.obtenerReglamentacion2() + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_2HEIGHT);

            mPrinter.printText(printerUtil.obtenerAnulada(factura.Anulada),
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_2HEIGHT);
            mPrinter.lineFeed(1);

            mPrinter.printNormal(printerUtil.obtenerTituloDatosCliente() + printerUtil.NL());
            mPrinter.printNormal("CC/Nit: " + factura.Identificacion + printerUtil.NL());
            mPrinter.printNormal(factura.RazonSocial + printerUtil.NL());

            if (factura.Negocio != null) {
                mPrinter.printNormal(factura.Negocio + printerUtil.NL());
            }
            if (factura.Direccion != null) {
                mPrinter.printNormal(factura.Direccion + printerUtil.NL());
            }
            if (factura.Telefono != null) {
                mPrinter.printNormal(factura.Telefono + printerUtil.NL());
            }

            mPrinter.printNormal(printerUtil.obtenerMunicipioCliente(factura.IdCliente) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerFormaDePago(factura.FormaPago) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerDatosCredito(factura) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());

            mPrinter.printNormal(printerUtil.obtenerFechaFactura(factura) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerFechaVencimiento(factura) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());

            if (!TextUtils.isEmpty(factura.Comentario)) {
                mPrinter.printNormal(factura.Comentario + printerUtil.NL());
                mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());
            }

            if (!TextUtils.isEmpty(factura.ComentarioAnulacion)) {
                mPrinter.printNormal(factura.ComentarioAnulacion + printerUtil.NL());
                mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());
            }

            if (TextUtils.isEmpty(factura.Comentario) && TextUtils.isEmpty(factura.ComentarioAnulacion)) {
                mPrinter.printNormal("COMENTARIO" + printerUtil.NL());
                mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());
            }

            mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleDescripcionProducto() + printerUtil.NL());

            if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)
                    || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
                mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleValoresProductoConIvaFactura() + printerUtil.NL());
            } else {
                mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleValoresProducto() + printerUtil.NL());
            }

            PrinterDetailResume detailResume = printerUtil.obtenerDetalleFactura(factura);
            mPrinter.printNormal(detailResume.detailToPrint + printerUtil.NL());
            mPrinter.lineFeed(1);

            if (!detailResume.rotaciones.equals("")) {
                mPrinter.printNormal(printerUtil.obtenerTituloRotaciones() + printerUtil.NL());
                mPrinter.printNormal(detailResume.rotaciones + printerUtil.NL());
                mPrinter.lineFeed(1);
            }

            if (!detailResume.devoluciones.equals("")) {
                mPrinter.printNormal(printerUtil.obtenerTituloDevoluciones());
                mPrinter.printNormal(printerUtil.obtenerDetailResume(detailResume));
            }

            DecimalFormat formatea = new DecimalFormat("###,###.##");
            mPrinter.printNormal(printerUtil.obtenerTituloTotales() + printerUtil.NL());
            mPrinter.printNormal("Cantidad   :" + detailResume.cantidadFactura + printerUtil.NL());
            mPrinter.printNormal("Subtotal   :" + formatea.format((int) factura.Subtotal) + printerUtil.NL());
            mPrinter.printNormal("Descuento  :" + formatea.format((int) factura.Descuento) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerTotalDevoluciones(detailResume));
            //mPrinter.printNormal(printerUtil.obtenerResumenIvas(detailResume));
            mPrinter.printNormal(printerUtil.obtenerTotalIvas(factura.Iva) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerRetefuente(factura.Retefuente) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerIpoconsumo(factura.IpoConsumo));

            if (factura.ReteIva > 0) {
                mPrinter.printNormal("Rete Iva   :" + formatea.format((int) factura.ReteIva) + printerUtil.NL());
            }

            mPrinter.lineFeed(1);
            mPrinter.printText("A PAGAR: " + formatea.format((int)factura.Total) + printerUtil.NL(),
                    CMPPrint.CMP_ALIGNMENT_RIGHT,
                    CMPPrint.CMP_FNT_BOLD,
                    CMPPrint.CMP_TXT_2HEIGHT);

            mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerResolucionFacturacion(factura.FacturaPos) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerPieDePagina() + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerNumeroOrden(factura) + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerFirmas() + printerUtil.NL());

            mPrinter.lineFeed(3);

            boolean esFacturadoraElectronica = printerUtil.obtenerCliente(factura.IdCliente).FacturacionElectronicaCliente;

            if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(factura.QRInputValue) && esFacturadoraElectronica) {
                mPrinter.printQRCode(factura.QRInputValue, factura.QRInputValue.length(), 3, ESCPOSConst.CMP_QRCODE_EC_LEVEL_L, ESCPOSConst.CMP_ALIGNMENT_CENTER);
                mPrinter.lineFeed(3);
            }
        }
    }

    private void printRemisionAfterConnect() throws Exception {
        if (mIsConnected) {

            try {

                if (ActivityBase.resolucion != null) {

                    for (int i = 0; i < mNumeroCopias; i++) {
                        printRemision(mRemision);
                        Thread.sleep(1500);
                    }
                }

            } catch (UnsupportedEncodingException e) {
                throw new Exception("Error imprimiendo(UnsupportedEncodingException): " +
                        e.getMessage());
            } catch (Exception e) {
                throw new Exception("Error imprimiendo: " + e.getMessage());
            }finally {
                new Desconexion().execute("");
            }
        }
    }

    private void printRemisionesAfterConnect() throws Exception {
        if (mIsConnected) {

            try {

                if (ActivityBase.resolucion != null && mRemisiones != null) {

                    for (RemisionDTO remision : mRemisiones) {
                        printRemision(remision);
                        Thread.sleep(1500);
                    }

                    Thread.sleep(2000 * mRemisiones.size());
                    new Desconexion().execute("");
                }

            } catch (UnsupportedEncodingException e) {
                throw new Exception("Error imprimiendo(UnsupportedEncodingException): " +
                        e.getMessage());
            } catch (Exception e) {
                throw new Exception("Error imprimiendo: " + e.getMessage());
            }
        }
    }

    private void printRemision(RemisionDTO remision) throws Exception{

        PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);
        ClienteDTO clienteDTO = printerUtil.obtenerCliente(remision.IdCliente);

        mPrinter.printText(printerUtil.obtenerEncabezadoRemision() + printerUtil.NL(),
                CMPPrint.CMP_ALIGNMENT_CENTER,
                CMPPrint.CMP_FNT_BOLD,
                CMPPrint.CMP_TXT_1HEIGHT);
        mPrinter.printText(printerUtil.obtenerNombreComercial(),
                CMPPrint.CMP_ALIGNMENT_CENTER,
                CMPPrint.CMP_FNT_DEFAULT,
                CMPPrint.CMP_TXT_1HEIGHT);
        mPrinter.printText(printerUtil.obtenerNitRemision(),
                CMPPrint.CMP_ALIGNMENT_CENTER,
                CMPPrint.CMP_FNT_DEFAULT,
                CMPPrint.CMP_TXT_1HEIGHT);
        mPrinter.lineFeed(1);


        mPrinter.printNormal(printerUtil.obtenerRegimen() + printerUtil.CRNL());
        mPrinter.printNormal(printerUtil.obtenerDatosContacto() + printerUtil.CRNL());
        mPrinter.printNormal(printerUtil.obtenerEmail() + printerUtil.CRNL());
        mPrinter.lineFeed(1);

        mPrinter.printNormal("Vendedor: " + ActivityBase.resolucion.CodigoRuta + printerUtil.CRNL());
        mPrinter.printNormal("Nombre: " + ActivityBase.resolucion.NombreRuta + printerUtil.CRNL());
        mPrinter.printNormal(printerUtil.obtenerNumeroCelularRuta() + printerUtil.CRNL());
        mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());

        mPrinter.printText(printerUtil.obtenerLabelRemisionNro(clienteDTO.Remision, clienteDTO.Credito) + printerUtil.NL(),
                CMPPrint.CMP_ALIGNMENT_CENTER,
                CMPPrint.CMP_FNT_BOLD,
                CMPPrint.CMP_TXT_2HEIGHT);

        mPrinter.printText(remision.NumeroRemision + printerUtil.NL(),
                CMPPrint.CMP_ALIGNMENT_CENTER,
                CMPPrint.CMP_FNT_BOLD,
                CMPPrint.CMP_TXT_2HEIGHT);

        mPrinter.printText(printerUtil.obtenerAnulada(remision.Anulada),
                CMPPrint.CMP_ALIGNMENT_CENTER,
                CMPPrint.CMP_FNT_BOLD,
                CMPPrint.CMP_TXT_2HEIGHT);
        mPrinter.lineFeed(1);

        mPrinter.printNormal(printerUtil.obtenerTituloDatosCliente() + printerUtil.NL());
        mPrinter.printNormal("CC/Nit: " + remision.IdentificacionCliente + printerUtil.NL());
        mPrinter.printNormal(remision.RazonSocialCliente + printerUtil.NL());

        if (remision.Negocio != null) {
            mPrinter.printNormal(remision.Negocio + printerUtil.NL());
        }
        if (remision.DireccionCliente != null) {
            mPrinter.printNormal(remision.DireccionCliente + printerUtil.NL());
        }
        if (remision.TelefonoCliente != null) {
            mPrinter.printNormal(remision.TelefonoCliente + printerUtil.NL());
        }

        mPrinter.printNormal(printerUtil.obtenerMunicipioCliente(remision.IdCliente) + printerUtil.NL());
        mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());

        mPrinter.printNormal("FECHA HORA: " + printerUtil.NL() +
                printerUtil.obtenerFecha(remision.Fecha) + printerUtil.NL());
        mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());

        if (!TextUtils.isEmpty(remision.Comentario)) {
            mPrinter.printNormal(remision.Comentario + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());
        }

        if (!TextUtils.isEmpty(remision.ComentarioAnulacion)) {
            mPrinter.printNormal(remision.ComentarioAnulacion + printerUtil.NL());
            mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());
        }

        mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleDescripcionProducto() + printerUtil.NL());
        mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleValoresProducto() + printerUtil.NL());
        mPrinter.lineFeed(1);

        PrinterDetailResume detailResume = printerUtil.obtenerDetalleRemision(remision);
        mPrinter.printNormal(detailResume.detailToPrint + printerUtil.NL());
        mPrinter.lineFeed(1);

        if (!detailResume.rotaciones.equals("")) {
            mPrinter.printNormal(printerUtil.obtenerTituloRotaciones() + printerUtil.NL());
            mPrinter.printNormal(detailResume.rotaciones + printerUtil.NL());
            mPrinter.lineFeed(1);
        }

        if (!detailResume.devoluciones.equals("")) {
            mPrinter.printNormal(printerUtil.NL() + printerUtil.obtenerTituloDevoluciones() + printerUtil.NL());
            mPrinter.printNormal(detailResume.devoluciones);
            mPrinter.lineFeed(1);
        }

        mPrinter.printNormal(printerUtil.obtenerTotalRemisiones(remision, detailResume) + printerUtil.NL());

        mPrinter.lineFeed(1);
        double retefuente = printerUtil.obtenerValorRetefuente(remision.Subtotal, remision.Descuento);
        boolean esActivoRetefuente = clienteDTO.ReteFuente;

        double valorSubtotalDevolucion = 0;
        double valorTotalDescuentoDevolucion = 0;
        double valorTotalIvaDevolucion = 0;
        for (DetalleRemisionDTO detalleRemision: remision.DetalleRemision) {
            valorSubtotalDevolucion = valorSubtotalDevolucion + (detalleRemision.Devolucion * detalleRemision.ValorUnitario);
            valorTotalDescuentoDevolucion = valorTotalDescuentoDevolucion + ((detalleRemision.Devolucion * detalleRemision.ValorUnitario) * (detalleRemision.PorcentajeDescuento / 100));
            valorTotalIvaDevolucion = valorTotalIvaDevolucion + (((detalleRemision.Devolucion * detalleRemision.ValorUnitario) - ((detalleRemision.Devolucion * detalleRemision.ValorUnitario) * (detalleRemision.PorcentajeDescuento / 100))) * (detalleRemision.PorcentajeIva / 100));
        }

        double totalVentaSinDevolucion = (valorSubtotalDevolucion - valorTotalDescuentoDevolucion) + valorTotalIvaDevolucion;
        double valorDevolucionMenosRetenciones = ((valorSubtotalDevolucion - valorTotalDescuentoDevolucion + valorTotalIvaDevolucion) - remision.RetefuenteDevolucion) - remision.ValorReteIvaDevolucion;

        double valorTotalRemision = ((remision.Total + totalVentaSinDevolucion) - remision.ValorRetefuente) - remision.ValorReteIva;
        double valorTotalImpresion = valorTotalRemision - valorDevolucionMenosRetenciones;
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        if (esActivoRetefuente) {
            if (retefuente > 0) {
                if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                    mPrinter.printText("A PAGAR: " + formatea.format(Math.round(valorTotalImpresion)) + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_RIGHT,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_2HEIGHT);
                } else {
                    mPrinter.printText("A PAGAR: " + formatea.format(Math.round(((remision.Total - retefuente) - remision.ValorReteIva))) + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_RIGHT,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_2HEIGHT);
                }
            } else {
                mPrinter.printText("A PAGAR: " + formatea.format(Math.round((remision.Total - remision.ValorReteIva))) + printerUtil.NL(),
                        CMPPrint.CMP_ALIGNMENT_RIGHT,
                        CMPPrint.CMP_FNT_BOLD,
                        CMPPrint.CMP_TXT_2HEIGHT);
            }
        } else {
            if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                mPrinter.printText("A PAGAR: " + formatea.format(Math.round(valorTotalImpresion)) + printerUtil.NL(),
                        CMPPrint.CMP_ALIGNMENT_RIGHT,
                        CMPPrint.CMP_FNT_BOLD,
                        CMPPrint.CMP_TXT_2HEIGHT);
            } else {
                mPrinter.printText("A PAGAR: " + formatea.format(Math.round(remision.Total - remision.ValorReteIva)) + printerUtil.NL(),
                        CMPPrint.CMP_ALIGNMENT_RIGHT,
                        CMPPrint.CMP_FNT_BOLD,
                        CMPPrint.CMP_TXT_2HEIGHT);
            }
        }

        String numeroOrden = printerUtil.obtenerNumeroOrden(remision);
        if(!TextUtils.isEmpty(numeroOrden))
            mPrinter.printNormal( numeroOrden + printerUtil.NL());
        mPrinter.printNormal(printerUtil.obtenerFirmasRemision() + printerUtil.NL());

        mPrinter.lineFeed(3);
    }

    private void printConfigAfterConnect() throws Exception {
        if (mIsConnected) {

            String textToPrint = "IMPRESORA GUARDADA CORRECTAMENTE\r\n";

            mPrinter.printText(textToPrint,
                    CMPPrint.CMP_ALIGNMENT_CENTER,
                    CMPPrint.CMP_FNT_DEFAULT,
                    CMPPrint.CMP_TXT_2HEIGHT);
            mPrinter.lineFeed(3);

            new Desconexion().execute("");
        }
    }

    private void printNotaAfterConnect() throws Exception{

        if (mIsConnected) {

            NotaCreditoFacturaDTO nota = mNota;

            try {

                if (ActivityBase.resolucion != null) {

                    PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                    FacturaDTO factura = new FacturaDAL(mContext).obtenerPorNumeroFac(nota.NumeroFactura);
                    if (factura == null) {
                        throw new Exception("La factura asociada " + nota.NumeroFactura + " ya ha sido eliminada del dispositivo");
                    }

                    mPrinter.printText(ActivityBase.resolucion.NombreComercial + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_1HEIGHT);
                    mPrinter.printText("Nit:" + ActivityBase.resolucion.Nit + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_1HEIGHT);
                    mPrinter.lineFeed(1);

                    mPrinter.printNormal(printerUtil.obtenerRegimen() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerDatosContacto() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerEmail() + printerUtil.CRNL());
                    mPrinter.lineFeed(1);

                    mPrinter.printNormal("Vendedor: " + ActivityBase.resolucion.CodigoRuta + printerUtil.CRNL());
                    mPrinter.printNormal("Nombre: " + ActivityBase.resolucion.NombreRuta + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerNumeroCelularRuta() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());

                    mPrinter.printText("NOTA CREDITO NRO. " + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.printText(nota.NumeroDocumento + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_2HEIGHT);

                    mPrinter.printText("POR DEVOLUCION" + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.printText("FACTURA DE VENTA NRO. " + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.printText(nota.NumeroFactura + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_2HEIGHT);
                    mPrinter.lineFeed(1);

                    mPrinter.printNormal(printerUtil.obtenerTituloDatosCliente() + printerUtil.NL());
                    mPrinter.printNormal("CC/Nit: " + factura.Identificacion + printerUtil.NL());
                    mPrinter.printNormal(factura.RazonSocial + printerUtil.NL());

                    if (factura.Negocio != null) {
                        mPrinter.printNormal(factura.Negocio + printerUtil.NL());
                    }
                    if (factura.Direccion != null) {
                        mPrinter.printNormal(factura.Direccion + printerUtil.NL());
                    }
                    if (factura.Telefono != null) {
                        mPrinter.printNormal(factura.Telefono + printerUtil.NL());
                    }

                    mPrinter.printNormal(printerUtil.obtenerMunicipioCliente(factura.IdCliente) + printerUtil.NL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());

                    mPrinter.printNormal(printerUtil.obtenerFechaFactura(factura) + printerUtil.NL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.NL());

                    mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleDescripcionProducto() + printerUtil.NL());
                    mPrinter.printNormal(printerUtil.obtenerEncabezadoDetalleValoresProductoNotaCredito() + printerUtil.NL());

                    PrinterDetailResume detailResume = printerUtil.obtenerDetalleNotaCreditoFacturaPorDevolucion(nota.DetalleNotaCreditoFactura);
                    mPrinter.printNormal(detailResume.detailToPrint + printerUtil.NL());
                    mPrinter.lineFeed(1);
                    DecimalFormat formatea = new DecimalFormat("###,###.##");
                    String totales = printerUtil.NL() + printerUtil.obtenerTituloTotales() +
                            printerUtil.NL() + "Cantidad        :" + detailResume.cantidadNotaCredito +
                            printerUtil.NL() + "Subtotal Nota   :" + formatea.format((int) nota.Subtotal) +
                            printerUtil.NL() + "Descuento Nota  :" + formatea.format((int) nota.Descuento) +
                            printerUtil.NL() + "Iva Total Nota  :" + formatea.format((int) nota.Iva) +
                            printerUtil.NL() + "Total Factura   :" + formatea.format((int) factura.Total) +
                            printerUtil.NL() + "Total Nota      :" + formatea.format((int) nota.Valor);

                    mPrinter.printNormal (totales + printerUtil.NL());
                    mPrinter.lineFeed(1);

                    mPrinter.printText("Total A Pagar: " + formatea.format((double) (factura.Total - nota.Valor)) + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_RIGHT,
                            CMPPrint.CMP_FNT_BOLD,
                            CMPPrint.CMP_TXT_2HEIGHT);

                    mPrinter.printNormal(printerUtil.obtenerFirmas() + printerUtil.NL());
                    mPrinter.lineFeed(3);

                    boolean esFacturadoraElectronica = printerUtil.obtenerCliente(factura.IdCliente).FacturacionElectronicaCliente;

                    if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(nota.QRInputValue) && esFacturadoraElectronica) {
                        mPrinter.printQRCode(nota.QRInputValue, nota.QRInputValue.length(), 3, ESCPOSConst.CMP_QRCODE_EC_LEVEL_L, ESCPOSConst.CMP_ALIGNMENT_CENTER);
                        mPrinter.lineFeed(3);
                    }
                }

            } catch (Exception e) {
                throw new Exception("Error imprimiendo la Nota Crédito: " + e.getMessage());
            }finally {
                Thread.sleep(2500);
                new Desconexion().execute("");
            }
        }
    }

    private void printResumenVentasAfterConnect() throws Exception{

        if (mIsConnected) {

            try {

                if (ActivityBase.resolucion != null) {

                    PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                    mPrinter.printText(ActivityBase.resolucion.NombreComercial + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.printText("Nit:" + ActivityBase.resolucion.Nit + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.lineFeed(1);

                    mPrinter.printNormal(printerUtil.obtenerRegimen() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerDatosContacto() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerEmail() + printerUtil.CRNL());
                    mPrinter.lineFeed(1);

                    mPrinter.printNormal("Vendedor: " + ActivityBase.resolucion.CodigoRuta + printerUtil.CRNL());
                    mPrinter.printNormal("Nombre: " + ActivityBase.resolucion.NombreRuta + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerNumeroCelularRuta() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());

                    mPrinter.printText("RESUMEN DE FACTURACION" + printerUtil.CRNL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_2HEIGHT);

                    mPrinter.printNormal(" FECHA: " + Utilities.FechaDetallada(new Date()) + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.detallePrimeraYultimaFactura + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.detalleResumenToPrint + printerUtil.CRNL());

                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printText("RESUMEN DE REMISIONES" + printerUtil.CRNL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_2HEIGHT);

                    if(mResumenVentas.detalleRemisionToPrint.equals("")){
                        mPrinter.printNormal("No hay remisiones" + printerUtil.NL());
                    }else{
                        mPrinter.printNormal(mResumenVentas.detallePrimeraYultimaRemision + printerUtil.NL());
                        mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                        mPrinter.printNormal(mResumenVentas.detalleRemisionToPrint + printerUtil.CRNL());
                    }

                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printText("RESUMEN DE NOTAS CREDITO POR DEVOLUCION" + printerUtil.CRNL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_2HEIGHT);

                    if(mResumenVentas.detalleResumenNotasCreditoPorDevolucionToPrint.equals("")){
                        mPrinter.printNormal("No hay notas credito por devolucion" + printerUtil.NL());
                    }else{
                        mPrinter.printNormal(mResumenVentas.detallePrimeraYultimaNotaCreditoPorDevolucion + printerUtil.NL());
                        mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                        mPrinter.printNormal(mResumenVentas.detalleResumenNotasCreditoPorDevolucionToPrint + printerUtil.NL());
                    }

                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.resumen + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.resumenRemisiones + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.resumenNotasCreditoPorDevolucion + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.resumenRemision + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(mResumenVentas.resumenCantidades + printerUtil.CRNL());

                    mPrinter.printNormal(printerUtil.obtenerUrlTimovil());
                    mPrinter.lineFeed(3);
                }

            }catch (Exception e) {
                throw new Exception("Error imprimiendo la Nota Crédito: " + e.getMessage());
            }finally {
                Thread.sleep(2500);
                new Desconexion().execute("");
            }
        }
    }

    private void printInventarioAfterConnect() throws Exception{

        if (mIsConnected) {

            try {

                if (ActivityBase.resolucion != null) {

                    PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                    mPrinter.printText(ActivityBase.resolucion.NombreComercial + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.printText("Nit:" + ActivityBase.resolucion.Nit + printerUtil.NL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_1HEIGHT);

                    mPrinter.lineFeed(1);

                    mPrinter.printNormal(printerUtil.obtenerRegimen() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerDatosContacto() + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerEmail() + printerUtil.CRNL());
                    mPrinter.lineFeed(1);

                    mPrinter.printNormal("Vendedor: " + ActivityBase.resolucion.CodigoRuta + printerUtil.CRNL());
                    mPrinter.printNormal("Nombre: " + ActivityBase.resolucion.NombreRuta + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerNumeroCelularRuta() + printerUtil.CRNL());

                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());
                    mPrinter.printNormal(" FECHA: " + Utilities.FechaDetallada(new Date()) + printerUtil.CRNL());
                    mPrinter.printNormal(printerUtil.obtenerSeparador() + printerUtil.CRNL());

                    mPrinter.printText("INVENTARIO" + printerUtil.CRNL(),
                            CMPPrint.CMP_ALIGNMENT_CENTER,
                            CMPPrint.CMP_FNT_DEFAULT,
                            CMPPrint.CMP_TXT_2HEIGHT);

                    mPrinter.printNormal(printerUtil.obtenerImpresionIventario());
                    mPrinter.printNormal(printerUtil.obtenerUrlTimovil() + printerUtil.NL());
                    mPrinter.lineFeed(3);
                }

            }catch (Exception e) {
                throw new Exception("Error imprimiendo la Nota Crédito: " + e.getMessage());
            }finally {
                Thread.sleep(2500);
                new Desconexion().execute("");
            }
        }
    }

    private void bluetoothSetup() throws Exception {

        if (mBluetoothPort == null) {
            mBluetoothPort = BluetoothPort.getInstance();
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (mBluetoothAdapter == null) {
            throw new Exception("Su dispositivo no soporta Bluetooth");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    private class connTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            mIsConnected = false;
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int retVal = 0;
            try {
                BluetoothDevice bt = null;
                if (!mIsConnected) {
                    BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
                    if (ba != null) {
                        bt = ba.getRemoteDevice(mMacAddress);
                    } else {
                        retVal = -2;//("La tecnología Bluetooth no es soportada por su dispositivo móvil");
                    }
                    if (retVal == 0) {
                        ba.cancelDiscovery();
                        mBluetoothPort.connect(bt);
                        mIsConnected = true;
                    }
                }
            } catch (IllegalArgumentException ex) {
                retVal = -3;//("Por favor configure nuevamente la impresora.");
            } catch (IOException e) {
                retVal = -2;
            } catch (Exception ex) {
                retVal = -1;
            }
            return retVal;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                RequestHandler rh = new RequestHandler();
                mThread = new Thread(rh);
                mThread.start();
            } else if (result == -2) {
                mContext.makeLToast("La tecnología Bluetooth no es soportada por su dispositivo móvil");
            } else if (result == -3) {
                mContext.makeLToast("Por favor configure nuevamente la impresora.");
            } else if (result == -1) {
                mContext.makeLToast("Por favor verifique que la impresora se encuentre encendida");
            }

            try {
                if (mFactura != null) printFacturaAfterConnect();
                else if(mFacturas != null && mFacturas.size() > 0) printFacturasAfterConnect();
                else if(mRemision != null) printRemisionAfterConnect();
                else if(mRemisiones != null && mRemisiones.size() > 0) printRemisionesAfterConnect();
                else if(mNota != null) printNotaAfterConnect();
                else if(mResumenVentas != null) printResumenVentasAfterConnect();
                else if(mImprimirInventario) printInventarioAfterConnect();
                else printConfigAfterConnect();
            } catch (Exception ex) {
                mContext.makeLToast("Por favor verifique que la impresora se encuentre encendida");
            }

        }
    }

    private class Desconexion extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String error = "OK";
            try {
                Thread.sleep(2000);
                mBluetoothPort.disconnect();
                Thread.sleep(1200);
            } catch (IOException e) {
                error = e.getMessage();
            } catch (InterruptedException e) {
                error = "InterruptedException " + e.getMessage();
                e.printStackTrace();
            }
            return error;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if ((mThread != null) && (mThread.isAlive()))
                    mThread.interrupt();

                if (result.equals("OK")) {
                    mContext.makeLToast("Bluetooth desconectado");
                } else {
                    mContext.makeLToast("Error desconectando el Bluetooth:" + result);
                }
            } catch (Exception e) {
                mContext.makeLToast("Error general desconexión:" + e.getMessage());
            }
            super.onPostExecute(result);
        }
    }
}
