package com.cm.timovil2.bl.printers.printer_v2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;

import com.cm.timovil2.bl.calculus.Calculable;
import com.cm.timovil2.bl.calculus.Calculator;
import com.cm.timovil2.bl.calculus.CalculatorFactory;
import com.cm.timovil2.bl.printers.btservice.print.PrintUtil;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResumenVentasImpresionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 27/07/18.
 */

class PrinterZEBRA extends Printer {

    PrinterZEBRA(String macAdrress, ActivityBase context, int numeroCopias, String pulgadas) {
        super(macAdrress, context, numeroCopias, pulgadas);
        formatter = NumberFormat.getCurrencyInstance(Locale.US);
    }
    NumberFormat formatter = null;
    @Override
    void connect() throws Exception {
        throw new Exception("connect() No aplica para Zebra");
    }

    @Override
    public void print(FacturaDTO facturaDto) throws Exception {

        final FacturaDTO factura = facturaDto;

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();

                    Thread.sleep(2000);

                    for (int i = 0; i < mNumeroCopias; i++) {
                        printFactura(thePrinterConn, factura);
                        Thread.sleep(1500);
                    }


                    Thread.sleep(2000);
                    if (factura.notaCreditoFactura != null) {
                        printNotaAux(factura.notaCreditoFactura, thePrinterConn);
                    }

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);
                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                    Looper looper = Looper.myLooper();
                    if (looper != null) looper.quit();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void printFacturas(List<FacturaDTO> facturasDto) throws Exception {

        if(facturasDto == null || facturasDto.size() <= 0) return;
        final List<FacturaDTO> facturas = facturasDto;

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();
                    Thread.sleep(2000);

                    for (FacturaDTO factura: facturas) {
                        printFactura(thePrinterConn, factura);
                        Thread.sleep(1500);
                    }

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);
                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                    Looper looper = Looper.myLooper();
                    if (looper != null) looper.quit();

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void print(RemisionDTO remisionDTO) throws Exception {
        final RemisionDTO remision = remisionDTO;

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();
                    Thread.sleep(2000);

                    for (int i = 0; i < mNumeroCopias; i++) {
                        printRemision(thePrinterConn, remision);
                        Thread.sleep(500);
                    }

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);

                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                    Looper looper = Looper.myLooper();
                    if (looper != null) looper.quit();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void printRemisiones(List<RemisionDTO> remisionesDTO) throws Exception{

        if(remisionesDTO == null || remisionesDTO.size() <= 0) return;
        final List<RemisionDTO> remisiones = remisionesDTO;

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();
                    Thread.sleep(2000);

                    for (RemisionDTO remision: remisiones) {
                        printRemision(thePrinterConn, remision);
                        Thread.sleep(1500);
                    }

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);
                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                    Looper looper = Looper.myLooper();
                    if (looper != null) looper.quit();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void printConfig() throws Exception {

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();
                    Thread.sleep(1000);

                    String textToPrint = " IMPRESORA GUARDADA CORRECTAMENTE\r\n";

                    //Print
                    thePrinterConn.write(textToPrint.getBytes());

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(1000);

                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                    Looper looper = Looper.myLooper();
                    if (looper != null) looper.quit();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void print(NotaCreditoFacturaDTO notaDTO) throws Exception {

        final NotaCreditoFacturaDTO nota = notaDTO;

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();

                    Thread.sleep(2000);

                    printNotaAux(nota, thePrinterConn);

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);

                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void printFacturaComoRemision(FacturaDTO facturaDTO) throws Exception{

        Calculator calculator = CalculatorFactory.getCalculator(ActivityBase.resolucion, facturaDTO);
        if(calculator == null) return;

        RemisionDTO remision = (RemisionDTO) calculator.convert(Calculable.REMISION_TYPE);
        print(remision);
    }

    @Override
    public void print(ResumenVentasImpresionDTO resumenVentas) throws Exception{
        final ResumenVentasImpresionDTO resumen = resumenVentas;

        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();
                    Thread.sleep(2000);

                    PrinterUtil printerUtil = new PrinterUtil(mContext, 1, mPulgadas);

                    //------------------------------------------------------------------
                    StringBuilder textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.zebraObtenerComandoInicial(150));
                    textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
                    textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoCenter());
                    textToPrint.append(printerUtil.zebraObtenerComandoFormatoTexto());

                    textToPrint.append(printerUtil.obtenerEncabezado());
                    textToPrint.append(printerUtil.CRNL());

                    textToPrint.append(ActivityBase.resolucion.NombreComercial).append(printerUtil.CRNL());
                    textToPrint.append("Nit:").append(ActivityBase.resolucion.Nit).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoPrint());
                    //print
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-------------------------------------------------------------------

                    //-------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.obtenerRegimen()).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerDatosContacto()).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerEmail()).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("Vendedor: ").append(ActivityBase.resolucion.CodigoRuta);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("Nombre: ").append(ActivityBase.resolucion.NombreRuta);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerNumeroCelularRuta());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    //Print
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.zebraObtenerComandoInicial(45));
                    textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
                    textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoCenter());
                    textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("RESUMEN DE FACTURACION")
                            .append(printerUtil.leftRightSpace())
                            .append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoPrint());
                    //print
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.leftRightSpace()).append("FECHA: ").append(Utilities.FechaDetallada(new Date()));
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(resumen.detallePrimeraYultimaFactura);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(resumen.detalleResumenToPrint);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.zebraObtenerComandoInicial(45));
                    textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
                    textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoCenter());
                    textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("RESUMEN DE REMISIONES")
                            .append(printerUtil.leftRightSpace())
                            .append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoPrint());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.CRNL());
                    if(resumen.detalleRemisionToPrint.equals("")){
                        textToPrint.append(printerUtil.leftRightSpace()).append("No hay remisiones");
                        textToPrint.append(printerUtil.CRNL());
                    }else{
                        textToPrint.append(resumen.detallePrimeraYultimaRemision);
                        textToPrint.append(printerUtil.CRNL());
                        textToPrint.append(printerUtil.obtenerSeparador());
                        textToPrint.append(printerUtil.CRNL());
                        textToPrint.append(resumen.detalleRemisionToPrint);
                        textToPrint.append(printerUtil.CRNL());
                    }

                    textToPrint.append(printerUtil.obtenerSeparador());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.zebraObtenerComandoInicial(90));
                    textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
                    textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoCenter());
                    textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("RESUMEN DE NOTAS CREDITO")
                            .append(printerUtil.CRNL())
                            .append("POR DEVOLUCION")
                            .append(printerUtil.leftRightSpace())
                            .append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoPrint());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.CRNL());
                    if(resumen.detalleResumenNotasCreditoPorDevolucionToPrint.equals("")){
                        textToPrint.append(printerUtil.leftRightSpace()).append("No hay notas credito por devolucion");
                        textToPrint.append(printerUtil.CRNL());
                    }else{
                        textToPrint.append(resumen.detallePrimeraYultimaNotaCreditoPorDevolucion);
                        textToPrint.append(printerUtil.CRNL());
                        textToPrint.append(printerUtil.obtenerSeparador());
                        textToPrint.append(printerUtil.CRNL());
                        textToPrint.append(resumen.detalleResumenNotasCreditoPorDevolucionToPrint);
                        textToPrint.append(printerUtil.CRNL());
                    }

                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(resumen.resumen);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(resumen.resumenRemisiones);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(resumen.resumenNotasCreditoPorDevolucion);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(resumen.resumenRemision);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(resumen.resumenCantidades);
                    textToPrint.append(printerUtil.CRNL());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.obtenerUrlTimovil());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.CRNL());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);
                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    @Override
    public void printInventario() throws Exception{
        if(TextUtils.isEmpty(mMacAddress)){
            throw new Exception("Debe configurar una impresora");
        }

        new Thread(new Runnable() {
            public void run() {
                try {

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(mMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();
                    Thread.sleep(2000);

                    PrinterUtil printerUtil = new PrinterUtil(mContext, 1, mPulgadas);

                    //------------------------------------------------------------------
                    StringBuilder textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.zebraObtenerComandoInicial(150));
                    textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
                    textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoCenter());
                    textToPrint.append(printerUtil.zebraObtenerComandoFormatoTexto());

                    textToPrint.append(printerUtil.obtenerEncabezado());
                    textToPrint.append(printerUtil.CRNL());

                    textToPrint.append(ActivityBase.resolucion.NombreComercial).append(printerUtil.CRNL());
                    textToPrint.append("Nit:").append(ActivityBase.resolucion.Nit).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoPrint());
                    //print
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-------------------------------------------------------------------

                    //-------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.obtenerRegimen()).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerDatosContacto()).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerEmail()).append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("Vendedor: ").append(ActivityBase.resolucion.CodigoRuta);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("Nombre: ").append(ActivityBase.resolucion.NombreRuta);
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerNumeroCelularRuta());
                    textToPrint.append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.obtenerSeparador());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(" FECHA: ").append(Utilities.FechaDetallada(new Date()));
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerSeparador());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.zebraObtenerComandoInicial(45));
                    textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
                    textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoCenter());
                    textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

                    textToPrint.append(printerUtil.leftRightSpace())
                            .append("INVENTARIO")
                            .append(printerUtil.leftRightSpace())
                            .append(printerUtil.CRNL());

                    textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
                    textToPrint.append(printerUtil.zebraObtenerComandoPrint());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    //-----------------------------------------------------------------------
                    textToPrint = new StringBuilder();
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerImpresionIventario());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.obtenerUrlTimovil());
                    textToPrint.append(printerUtil.CRNL());
                    textToPrint.append(printerUtil.CRNL());
                    thePrinterConn.write(textToPrint.toString().getBytes());
                    //-----------------------------------------------------------------------

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(2000);
                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                } catch (Exception e) {

                    // Handle communications error here.
                    e.printStackTrace();

                }
            }
        }).start();
    }

    private void printNotaAux(NotaCreditoFacturaDTO nota, Connection thePrinterConn) throws Exception {

        try {

            PrinterUtil printerUtil = new PrinterUtil(mContext, 1, mPulgadas);

            //------------------------------------------------------------------
            StringBuilder textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.zebraObtenerComandoInicial(150));
            textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
            textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
            textToPrint.append(printerUtil.zebraObtenerComandoCenter());
            textToPrint.append(printerUtil.zebraObtenerComandoFormatoTexto());

            textToPrint.append(printerUtil.obtenerEncabezado());
            textToPrint.append(printerUtil.CRNL());

            textToPrint.append(ActivityBase.resolucion.NombreComercial).append(printerUtil.CRNL());
            textToPrint.append("Nit:").append(ActivityBase.resolucion.Nit).append(printerUtil.CRNL());
            textToPrint.append(printerUtil.CRNL());

            textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
            textToPrint.append(printerUtil.zebraObtenerComandoPrint());
            //print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------

            FacturaDTO factura = new FacturaDAL(mContext).obtenerPorNumeroFac(nota.NumeroFactura);
            if (factura == null) {
                throw new Exception("La factura asociada " + nota.NumeroFactura + " ya ha sido eliminada del dispositivo");
            }

            //-------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.obtenerRegimen()).append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerDatosContacto()).append(printerUtil.CRNL());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerEmail()).append(printerUtil.CRNL());
            textToPrint.append(printerUtil.leftRightSpace())
                    .append("Vendedor: ").append(ActivityBase.resolucion.CodigoRuta);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.leftRightSpace())
                    .append("Nombre: ").append(ActivityBase.resolucion.NombreRuta);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerNumeroCelularRuta());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-----------------------------------------------------------------------

            //-----------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.zebraObtenerComandoInicial(205));
            textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
            textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
            textToPrint.append(printerUtil.zebraObtenerComandoCenter());
            textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

            textToPrint.append("NOTA CREDITO NRO. ");
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(nota.NumeroDocumento);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append("POR DEVOLUCION");
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append("FACTURA DE VENTA NRO. ");
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(nota.NumeroFactura);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerAnulada(factura.Anulada));

            textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
            textToPrint.append(printerUtil.zebraObtenerComandoPrint());
            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //------------------------------------------------------------------------

            //------------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerTituloDatosCliente());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append("CC/Nit: ").append(factura.Identificacion);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(factura.RazonSocial);
            textToPrint.append(printerUtil.CRNL());

            if (factura.Negocio != null) {
                textToPrint.append(printerUtil.leftRightSpace());
                textToPrint.append(factura.Negocio);
                textToPrint.append(printerUtil.CRNL());
            }
            if (factura.Direccion != null) {
                textToPrint.append(printerUtil.leftRightSpace());
                textToPrint.append(factura.Direccion);
                textToPrint.append(printerUtil.CRNL());
            }
            if (factura.Telefono != null) {
                textToPrint.append(printerUtil.leftRightSpace());
                textToPrint.append(factura.Telefono);
                textToPrint.append(printerUtil.CRNL());
            }

            textToPrint.append(printerUtil.obtenerMunicipioCliente(factura.IdCliente));
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            textToPrint.append(printerUtil.CRNL());

            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------------

            //-------------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.obtenerFechaFactura(factura));
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            textToPrint.append(printerUtil.CRNL());
            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------------

            //-------------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.obtenerEncabezadoDetalleDescripcionProducto());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerEncabezadoDetalleValoresProductoNotaCredito());

            PrinterDetailResume detailResume = printerUtil.obtenerDetalleNotaCreditoFacturaPorDevolucion(nota.DetalleNotaCreditoFactura);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(detailResume.detailToPrint);
            textToPrint.append(printerUtil.CRNL());

            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------------

            //-------------------------------------------------------------------------
            DecimalFormat formatea = new DecimalFormat("###,###.##");
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append(printerUtil.obtenerTituloTotales());
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append("Cantidad        :").append(detailResume.cantidadNotaCredito);
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append("Subtotal Nota   :").append(formatea.format((int) nota.Subtotal));
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append("Descuento Nota  :").append(formatea.format((int) nota.Descuento));
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append("Iva Total Nota  :").append(formatea.format((int) nota.Iva));
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append("Total Factura   :").append(formatea.format((int) factura.Total));
            textToPrint.append(printerUtil.CRNL()).append(printerUtil.leftRightSpace()).append("Total Nota      :").append(formatea.format((int) nota.Valor));

            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------------

            //-------------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.zebraObtenerComandoInicial(45));
            textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
            textToPrint.append(printerUtil.zebraObtenerComandoRight());
            textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append("Total A Pagar: ");
            textToPrint.append(formatea.format((int)(factura.Total - nota.Valor)));
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(printerUtil.CRNL());

            textToPrint.append(printerUtil.zebraObtenerComandoPrint());

            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------------

            //-------------------------------------------------------------------------
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.obtenerFirmas());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.CRNL());

            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
            //-------------------------------------------------------------------------

            boolean esFacturadoraElectronica = printerUtil.obtenerCliente(factura.IdCliente).FacturacionElectronicaCliente;

            if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(nota.QRInputValue) && esFacturadoraElectronica){
                Bitmap bitmap = Utilities.TextToQRBitmap(mContext, nota.QRInputValue);
                if(bitmap != null){
                    printImage(thePrinterConn, bitmap);
                    bitmap.recycle();
                }
            }

        } catch (Exception e) {
            throw new Exception("Error imprimiendo la Nota Crédito: " + e.getMessage());
        }
    }

    private void printFactura(Connection thePrinterConn, FacturaDTO factura) throws Exception{

        PrinterUtil printerUtil = new PrinterUtil(mContext, 1, mPulgadas);
        ClienteDTO clienteDTO = printerUtil.obtenerCliente(factura.IdCliente);

        //------------------------------------------------------------------
        StringBuilder textToPrint = new StringBuilder();
        String encabezado = printerUtil.obtenerEncabezado();
        int height = TextUtils.isEmpty(encabezado) ? 150 : 200;
        textToPrint.append(printerUtil.zebraObtenerComandoInicial(height));
        textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
        textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoCenter());
        textToPrint.append(printerUtil.zebraObtenerComandoFormatoTexto());

        textToPrint.append(encabezado);
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(ActivityBase.resolucion.NombreComercial).append(printerUtil.CRNL());
        textToPrint.append("Nit:").append(ActivityBase.resolucion.Nit).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoPrint());
        //print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------


        //-------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.obtenerRegimen()).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerDatosContacto()).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerEmail()).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace())
                .append("Vendedor: ").append(ActivityBase.resolucion.CodigoRuta);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace())
                .append("Nombre: ").append(ActivityBase.resolucion.NombreRuta);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerNumeroCelularRuta());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerDatosEntregador(factura.IdEmpleadoEntregador));
        textToPrint.append(printerUtil.obtenerSeparador());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-----------------------------------------------------------------------


        //-----------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.zebraObtenerComandoInicial(82));
        textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
        textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoCenter());
        textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

        textToPrint.append(printerUtil.obtenerTipoDocumentoNro(factura.FacturaPos, ActivityBase.resolucion.CodigoRuta));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(factura.NumeroFactura);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerAnulada(factura.Anulada));
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoPrint());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //------------------------------------------------------------------------


        //------------------------------------------------------------------------
        String reg1 = printerUtil.obtenerReglamentacion1();
        String reg2 = printerUtil.obtenerReglamentacion2();
        if(!TextUtils.isEmpty(reg1) || !TextUtils.isEmpty(reg2)){
            textToPrint = new StringBuilder();
            textToPrint.append(printerUtil.zebraObtenerComandoInicial(82));
            textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
            textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
            textToPrint.append(printerUtil.zebraObtenerComandoCenter());
            textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

            textToPrint.append(reg1);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(reg2);
            textToPrint.append(printerUtil.CRNL());

            textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
            textToPrint.append(printerUtil.zebraObtenerComandoPrint());
            //Print
            thePrinterConn.write(textToPrint.toString().getBytes());
        }

        if (clienteDTO.FacturacionPOSCliente) {
            factura.FacturaPos = true;
            factura.Identificacion = clienteDTO.Identificacion;
            factura.RazonSocial = clienteDTO.RazonSocial;
            factura.Negocio = clienteDTO.NombreComercial;
            factura.Direccion = clienteDTO.Direccion;
            factura.Telefono = clienteDTO.Telefono1;
        }

        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.obtenerTituloDatosCliente());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append("CC/Nit: ").append(factura.Identificacion);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace()).append(factura.RazonSocial);
        textToPrint.append(printerUtil.CRNL());
        if (factura.Negocio != null) {
            textToPrint.append(printerUtil.leftRightSpace()).append(factura.Negocio);
            textToPrint.append(printerUtil.CRNL());
        }
        if (factura.Direccion != null) {
            textToPrint.append(printerUtil.leftRightSpace()).append(factura.Direccion);
            textToPrint.append(printerUtil.CRNL());
        }
        if (factura.Telefono != null) {
            textToPrint.append(printerUtil.leftRightSpace()).append(factura.Telefono);
            textToPrint.append(printerUtil.CRNL());
        }
        textToPrint.append(printerUtil.obtenerMunicipioCliente(factura.IdCliente));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerFormaDePago(factura.FormaPago));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerDatosCredito(factura));
        textToPrint.append(printerUtil.obtenerSeparador());
        textToPrint.append(printerUtil.CRNL());

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------

        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.obtenerFechaFactura(factura));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerFechaVencimiento(factura));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerSeparador());
        textToPrint.append(printerUtil.CRNL());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();

        if (!TextUtils.isEmpty(factura.Comentario)) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(factura.Comentario);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            textToPrint.append(printerUtil.CRNL());
        }

        if (!TextUtils.isEmpty(factura.ComentarioAnulacion)) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(factura.ComentarioAnulacion);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            textToPrint.append(printerUtil.CRNL());
        }

        if (TextUtils.isEmpty(factura.Comentario) && TextUtils.isEmpty(factura.ComentarioAnulacion)) {
            textToPrint.append(printerUtil.leftRightSpace())
                    .append("COMENTARIO")
                    .append(printerUtil.CRNL())
                    .append(printerUtil.obtenerSeparador())
                    .append(printerUtil.CRNL());
        }

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------

        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        String descProduct = printerUtil.obtenerEncabezadoDetalleDescripcionProducto();
        if(!TextUtils.isEmpty(descProduct)){
            textToPrint.append(descProduct);
            textToPrint.append(printerUtil.CRNL());
        }

        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)
            || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            textToPrint.append(printerUtil.obtenerEncabezadoDetalleValoresProductoConIvaFactura());
        } else {
            textToPrint.append(printerUtil.obtenerEncabezadoDetalleValoresProducto());
        }

        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerSeparador());
        PrinterDetailResume detailResume = printerUtil.obtenerDetalleFactura(factura);
        textToPrint.append(detailResume.detailToPrint);
        textToPrint.append(printerUtil.CRNL());

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        if (!detailResume.rotaciones.equals("")) {
            textToPrint.append(printerUtil.obtenerTituloRotaciones());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(detailResume.rotaciones);
            textToPrint.append(printerUtil.CRNL());
        }

        if (!detailResume.devoluciones.equals("")) {
            textToPrint.append(printerUtil.obtenerTituloDevoluciones());
            textToPrint.append(printerUtil.obtenerDetailResume(detailResume));
        }

        //Print
        if (textToPrint.length() > 0)
            thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerTituloTotales());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append("Cantidad   :").append(detailResume.cantidadFactura);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append("Subtotal   :").append(formatea.format(factura.Subtotal));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append("Descuento  :").append(formatea.format(factura.Descuento));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerTotalDevoluciones(detailResume));
        //textToPrint.append(printerUtil.obtenerResumenIvas(detailResume));
        textToPrint.append(printerUtil.obtenerTotalIvas(factura.Iva));
        textToPrint.append(printerUtil.CRNL());

        String ret = printerUtil.obtenerRetefuente(factura.Retefuente);
        if(!TextUtils.isEmpty(ret)) {
            textToPrint.append(formatea.format(ret));
        }

        String ipo = printerUtil.obtenerIpoconsumo(factura.IpoConsumo);
        if(!TextUtils.isEmpty(ipo)){
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(formatea.format(ipo));
        }


        if (factura.ReteIva > 0) {
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append("Rete Iva   :").append(formatea.format((int) factura.ReteIva));
        }

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.zebraObtenerComandoInicial(45));
        textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
        textToPrint.append(printerUtil.zebraObtenerComandoRight());
        textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

        textToPrint.append(printerUtil.leftRightSpace()).append("A PAGAR: ");
        textToPrint.append(formatea.format(factura.Total));
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(printerUtil.zebraObtenerComandoPrint());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------

        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.obtenerSeparador());

        textToPrint.append(printerUtil.obtenerResolucionFacturacion(factura.FacturaPos));
        textToPrint.append(printerUtil.obtenerPieDePagina());
        textToPrint.append(printerUtil.obtenerNumeroOrden(factura));
        textToPrint.append(printerUtil.obtenerFirmas());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.CRNL());

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------

        boolean esFacturadoraElectronica = clienteDTO.FacturacionElectronicaCliente;

        if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(factura.QRInputValue) && esFacturadoraElectronica){
            Bitmap bitmap = Utilities.TextToQRBitmap(mContext, factura.QRInputValue);
            if(bitmap != null){
                printImage(thePrinterConn, bitmap);
                bitmap.recycle();
            }
        }
    }

    private void printRemision(Connection thePrinterConn, RemisionDTO remision) throws Exception{

        PrinterUtil printerUtil = new PrinterUtil(mContext, 1, mPulgadas);
        ClienteDTO clienteDTO = printerUtil.obtenerCliente(remision.IdCliente);

        //------------------------------------------------------------------
        StringBuilder textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.zebraObtenerComandoInicial(150));
        textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
        textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoCenter());
        textToPrint.append(printerUtil.zebraObtenerComandoFormatoTexto());

        textToPrint.append(printerUtil.obtenerEncabezadoRemision());
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(printerUtil.obtenerNombreComercial());
        textToPrint.append(printerUtil.obtenerNitRemision());
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoPrint());
        //print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------

        //-------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.obtenerRegimen()).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerDatosContacto()).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerEmail()).append(printerUtil.CRNL());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace())
                .append("Vendedor: ").append(ActivityBase.resolucion.CodigoRuta);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace())
                .append("Nombre: ").append(ActivityBase.resolucion.NombreRuta);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerNumeroCelularRuta());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerSeparador());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-----------------------------------------------------------------------


        //-----------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.zebraObtenerComandoInicial(82));
        textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
        textToPrint.append(printerUtil.zebraObtenerComandoMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoCenter());
        textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());

        textToPrint.append(printerUtil.obtenerLabelRemisionNro(clienteDTO.Remision, clienteDTO.Credito));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(remision.NumeroRemision);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerAnulada(remision.Anulada));

        textToPrint.append(printerUtil.zebraObtenerComandoCancelarMultilinea());
        textToPrint.append(printerUtil.zebraObtenerComandoPrint());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //------------------------------------------------------------------------


        //------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerTituloDatosCliente());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append("CC/Nit: ").append(remision.IdentificacionCliente);
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append(remision.RazonSocialCliente);
        textToPrint.append(printerUtil.CRNL());

        if (remision.Negocio != null) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(remision.Negocio);
            textToPrint.append(printerUtil.CRNL());
        }

        if (remision.DireccionCliente != null) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(remision.DireccionCliente);
            textToPrint.append(printerUtil.CRNL());
        }

        if (remision.TelefonoCliente != null) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(remision.TelefonoCliente);
            textToPrint.append(printerUtil.CRNL());
        }

        textToPrint.append(printerUtil.obtenerMunicipioCliente(remision.IdCliente));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerSeparador());
        textToPrint.append(printerUtil.CRNL());

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.leftRightSpace()).append("FECHA HORA: ");
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerFecha(remision.Fecha));
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerSeparador());
        textToPrint.append(printerUtil.CRNL());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();

        if (!TextUtils.isEmpty(remision.Comentario)) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(remision.Comentario);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            textToPrint.append(printerUtil.CRNL());
        }

        if (!TextUtils.isEmpty(remision.ComentarioAnulacion)) {
            textToPrint.append(printerUtil.leftRightSpace());
            textToPrint.append(remision.ComentarioAnulacion);
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerSeparador());
            textToPrint.append(printerUtil.CRNL());
        }

        if (TextUtils.isEmpty(remision.Comentario)
                && TextUtils.isEmpty(remision.ComentarioAnulacion)) {
            textToPrint.append(printerUtil.leftRightSpace())
                    .append("COMENTARIO")
                    .append(printerUtil.CRNL()).append(printerUtil.obtenerSeparador())
                    .append(printerUtil.CRNL());
        }
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.obtenerEncabezadoDetalleDescripcionProducto());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerEncabezadoDetalleValoresProducto());
        textToPrint.append(printerUtil.CRNL());
        textToPrint.append(printerUtil.obtenerSeparador());
        PrinterDetailResume detailResume = printerUtil.obtenerDetalleRemision(remision);
        textToPrint.append(detailResume.detailToPrint);
        textToPrint.append(printerUtil.CRNL());

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        if (!detailResume.rotaciones.equals("")) {
            textToPrint.append(printerUtil.obtenerTituloRotaciones());
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(detailResume.rotaciones);
            textToPrint.append(printerUtil.CRNL());
        }

        if (!detailResume.devoluciones.equals("")) {
            textToPrint.append(printerUtil.CRNL());
            textToPrint.append(printerUtil.obtenerTituloDevoluciones());
            textToPrint.append(detailResume.devoluciones);
            textToPrint.append(printerUtil.CRNL());
        }
        //Print
        if (textToPrint.length() > 0)
            thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        // Aca calcular el retefuente a restar A PAGAR y pasar al metodo obtenerTotalRemisiones
        textToPrint.append(printerUtil.obtenerTotalRemisiones(remision, detailResume));

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------


        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();
        textToPrint.append(printerUtil.zebraObtenerComandoInicial(45));
        textToPrint.append(printerUtil.zebraObtenerComandoTipoCodificacion());
        textToPrint.append(printerUtil.zebraObtenerComandoRight());
        textToPrint.append(printerUtil.zebraObtenerComandoFormatoTextoBold());
        DecimalFormat formatea = new DecimalFormat("###,###.##");

        textToPrint.append(printerUtil.leftRightSpace()).append("A PAGAR: ");
        double valorRetefuenteVenta = printerUtil.obtenerValorRetefuente(remision.Subtotal, remision.Descuento);
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

        if (esActivoRetefuente) {
            if (valorRetefuenteVenta > 0) {
                if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                    textToPrint.append(formatea.format(Math.round(valorTotalImpresion)));
                } else {
                    textToPrint.append(formatea.format(Math.round((remision.Total - valorRetefuenteVenta) - remision.ValorReteIva)));
                }
            } else {
                textToPrint.append(formatea.format(Math.round(remision.Total - remision.ValorReteIva)));
            }
        } else {
            if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                textToPrint.append(formatea.format(Math.round(valorTotalImpresion)));
            } else {
                textToPrint.append(formatea.format(Math.round(remision.Total - remision.ValorReteIva)));
            }
        }

        textToPrint.append(printerUtil.leftRightSpace());
        textToPrint.append(printerUtil.CRNL());

        textToPrint.append(printerUtil.zebraObtenerComandoPrint());
        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------

        //-------------------------------------------------------------------------
        textToPrint = new StringBuilder();

        String numeroOrden = printerUtil.obtenerNumeroOrden(remision);
        if(!TextUtils.isEmpty(numeroOrden)){
            textToPrint.append(numeroOrden);
        }

        textToPrint.append(printerUtil.obtenerFirmasRemision());
        textToPrint.append(printerUtil.CRNL());

        //Print
        thePrinterConn.write(textToPrint.toString().getBytes());
        //-------------------------------------------------------------------------
    }

    private void printImage(Connection thePrinterConn, Bitmap bitmap) throws Exception{
        ZebraPrinter printer = ZebraPrinterFactory.getInstance(thePrinterConn);
        printer.printImage(new ZebraImageAndroid(bitmap), 0, 0, 350, 350, false);
    }
}
