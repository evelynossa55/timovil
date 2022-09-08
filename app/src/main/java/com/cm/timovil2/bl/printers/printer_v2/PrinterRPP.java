package com.cm.timovil2.bl.printers.printer_v2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 25/07/18.
 */
class PrinterRPP extends Printer {

    private BluetoothPrintDriver mChatService = null;
    NumberFormat formatter = null;

    PrinterRPP(String macAdrress, ActivityBase context, int numeroCopias, String pulgadas) {
        super(macAdrress, context, numeroCopias, pulgadas);
        setupChat();
        formatter = NumberFormat.getCurrencyInstance(Locale.US);
    }


    @Override
    void connect() throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice bd = ba.getRemoteDevice(mMacAddress);

        if (mChatService == null) setupChat();
        mChatService.connect(bd);

    }

    @Override
    public void printConfig() throws Exception {

        connect();
        Thread.sleep(2000);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (BluetoothPrintDriver.IsNoConnection()) {
                return;
            }

            PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);
            BluetoothPrintDriver.Begin();
            String tmpString = "IMPRESORA GUARDADA CORRECTAMENTE" + printerUtil.CR();
            BluetoothPrintDriver.BT_Write(tmpString);
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CutPaper();
            //After print
            Thread.sleep(1500);
            stopBluetoothServices();
        }
    }

    @Override
    public void print(FacturaDTO factura) throws Exception {

        if(TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(2500);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {

                BluetoothPrintDriver.Begin();
                for (int i = 0; i < mNumeroCopias; i++) {

                    printFactura(factura);
                    Thread.sleep(1500);
                }

                if (factura.notaCreditoFactura != null) {
                    printNotaAux(factura.notaCreditoFactura);
                }
            }

            //After print
            Thread.sleep(1500);
            stopBluetoothServices();
        }
    }

    @Override
    public void printFacturas(List<FacturaDTO> facturas) throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(2500);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {

                BluetoothPrintDriver.Begin();
                for (FacturaDTO factura: facturas) {
                    printFactura(factura);
                    Thread.sleep(1500);
                }
            }

            //After print
            Thread.sleep(1500);
            stopBluetoothServices();
        }
    }

    @Override
    public void print(RemisionDTO remision) throws Exception {

        if(TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(2000);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {

                BluetoothPrintDriver.Begin();
                for (int i = 0; i < mNumeroCopias; i++) {
                    printRemision(remision);
                    Thread.sleep(1500);
                }

            }

            //After print
            Thread.sleep(1500);
            stopBluetoothServices();
        }

    }

    @Override
    public void printRemisiones(List<RemisionDTO> remisiones) throws Exception{

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(2500);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {

                BluetoothPrintDriver.Begin();
                for (RemisionDTO remision: remisiones) {
                    printRemision(remision);
                    Thread.sleep(1500);
                }
            }

            //After print
            Thread.sleep(1500);
            stopBluetoothServices();
        }
    }

    @Override
    public void print(NotaCreditoFacturaDTO nota) throws Exception {

        connect();
        Thread.sleep(2000);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {

                BluetoothPrintDriver.Begin();
                Thread.sleep(500);

                printNotaAux(nota);

                //After print
                Thread.sleep(1500);
                stopBluetoothServices();
            }
        }
    }

    @Override
    public void printFacturaComoRemision(FacturaDTO facturaDTO) throws Exception{
        Calculator calculator = CalculatorFactory.getCalculator(ActivityBase.resolucion, facturaDTO);
        if(calculator == null) return;

        RemisionDTO remision = (RemisionDTO) calculator.convert(Calculable.REMISION_TYPE);
        print(remision);
    }

    @Override
    public void print(ResumenVentasImpresionDTO resumen) throws Exception{

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(2000);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {

                BluetoothPrintDriver.Begin();

                PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.SetLineSpacing(LINE_SPACING);
                BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(ActivityBase.resolucion.NombreComercial);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write("Nit: " + ActivityBase.resolucion.Nit);
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.BT_Write(printerUtil.obtenerRegimen());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosContacto());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerEmail());
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.BT_Write("Vendedor: " + ActivityBase.resolucion.CodigoRuta);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write("Nombre: " + ActivityBase.resolucion.NombreRuta);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerNumeroCelularRuta());
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.SetBold(BOLD);
                BluetoothPrintDriver.BT_Write("RESUMEN DE FACTURACION");
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
                BluetoothPrintDriver.SetBold(CANCEL_BOLD);
                BluetoothPrintDriver.BT_Write(" FECHA: " + Utilities.FechaDetallada(new Date()));
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.detallePrimeraYultimaFactura);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.detalleResumenToPrint);
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.SetBold(BOLD);
                BluetoothPrintDriver.BT_Write("RESUMEN DE REMISIONES");
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
                BluetoothPrintDriver.SetBold(CANCEL_BOLD);
                if(resumen.detalleRemisionToPrint.equals("")){
                    BluetoothPrintDriver.BT_Write("No hay remisiones");
                    BluetoothPrintDriver.LF();
                }else{
                    BluetoothPrintDriver.BT_Write(resumen.detallePrimeraYultimaRemision);
                    BluetoothPrintDriver.LF();
                    BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                    BluetoothPrintDriver.LF();
                    BluetoothPrintDriver.BT_Write(resumen.detalleRemisionToPrint);
                    BluetoothPrintDriver.LF();
                }

                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.SetBold(BOLD);
                BluetoothPrintDriver.BT_Write("RESUMEN DE NOTAS CREDITO POR DEVOLUCION");
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
                BluetoothPrintDriver.SetBold(CANCEL_BOLD);
                if(resumen.detalleResumenNotasCreditoPorDevolucionToPrint.equals("")){
                    BluetoothPrintDriver.BT_Write("No hay notas credito por devolucion");
                    BluetoothPrintDriver.LF();
                }else{
                    BluetoothPrintDriver.BT_Write(resumen.detallePrimeraYultimaNotaCreditoPorDevolucion);
                    BluetoothPrintDriver.LF();
                    BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                    BluetoothPrintDriver.LF();
                    BluetoothPrintDriver.BT_Write(resumen.detalleResumenNotasCreditoPorDevolucionToPrint);
                    BluetoothPrintDriver.LF();
                }

                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.resumen);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.resumenRemisiones);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.resumenNotasCreditoPorDevolucion);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.resumenRemision);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(resumen.resumenCantidades);
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerUrlTimovil());
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CutPaper();

            }

            //After print
            Thread.sleep(1500);
            stopBluetoothServices();
        }
    }

    @Override
    public void printInventario() throws Exception{
        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(2000);

        if (mChatService != null) {

            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

            if (!BluetoothPrintDriver.IsNoConnection()) {
                PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.SetLineSpacing(LINE_SPACING);
                BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
                BluetoothPrintDriver.SetBold(CANCEL_BOLD);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(ActivityBase.resolucion.NombreComercial);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write("Nit: " + ActivityBase.resolucion.Nit);
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.BT_Write(printerUtil.obtenerRegimen());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosContacto());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerEmail());
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.BT_Write("Vendedor: " + ActivityBase.resolucion.CodigoRuta);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write("Nombre: " + ActivityBase.resolucion.NombreRuta);
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerNumeroCelularRuta());
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(" FECHA: " + Utilities.FechaDetallada(new Date()));
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
                BluetoothPrintDriver.LF();

                BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                BluetoothPrintDriver.SetBold(BOLD);
                BluetoothPrintDriver.BT_Write("INVENTARIO");
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();

                BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
                BluetoothPrintDriver.SetBold(CANCEL_BOLD);
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerImpresionIventario());
                BluetoothPrintDriver.LF();
                BluetoothPrintDriver.BT_Write(printerUtil.obtenerUrlTimovil());
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CR();
                BluetoothPrintDriver.CutPaper();
            }
        }
    }

    private void printNotaAux(NotaCreditoFacturaDTO nota) throws Exception{

        try {

            PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
            BluetoothPrintDriver.SetLineSpacing(LINE_SPACING);
            BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
            BluetoothPrintDriver.LF();

            FacturaDTO factura = new FacturaDAL(mContext).obtenerPorNumeroFac(nota.NumeroFactura);
            if (factura == null) {
                throw new Exception("La factura asociada " + nota.NumeroFactura + " ya ha sido eliminada del dispositivo");
            }

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(ActivityBase.resolucion.NombreComercial);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write("Nit: " + ActivityBase.resolucion.Nit);
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();

            BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.BT_Write(printerUtil.obtenerRegimen());
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosContacto());
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.BT_Write(printerUtil.obtenerEmail());
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write("Vendedor: " + ActivityBase.resolucion.CodigoRuta);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write("Nombre: " + ActivityBase.resolucion.NombreRuta);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerNumeroCelularRuta());
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
            BluetoothPrintDriver.SetBold(BOLD);
            BluetoothPrintDriver.BT_Write("NOTA CREDITO NRO. ");
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
            BluetoothPrintDriver.BT_Write(nota.NumeroDocumento);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
            BluetoothPrintDriver.BT_Write("POR DEVOLUCION");
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write("FACTURA DE VENTA NRO. ");
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
            BluetoothPrintDriver.BT_Write(nota.NumeroFactura);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.CR();

            BluetoothPrintDriver.SetBold(CANCEL_BOLD);
            BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
            BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloDatosCliente());
            BluetoothPrintDriver.BT_Write("\nCC/Nit: " + factura.Identificacion);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(factura.RazonSocial);
            BluetoothPrintDriver.LF();
            if (factura.Negocio != null) {
                BluetoothPrintDriver.BT_Write(factura.Negocio);
            }
            BluetoothPrintDriver.LF();
            if (factura.Direccion != null) {
                BluetoothPrintDriver.BT_Write(factura.Direccion);
            }
            BluetoothPrintDriver.LF();
            if (factura.Telefono != null) {
                BluetoothPrintDriver.BT_Write(factura.Telefono);
            }

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerMunicipioCliente(factura.IdCliente));
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerFechaFactura(factura));

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleDescripcionProducto());
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleValoresProductoNotaCredito());
            BluetoothPrintDriver.LF();

            PrinterDetailResume detailResume = printerUtil.obtenerDetalleNotaCreditoFacturaPorDevolucion(nota.DetalleNotaCreditoFactura);
            BluetoothPrintDriver.BT_Write(printerUtil.CRNL());
            BluetoothPrintDriver.BT_Write(detailResume.detailToPrint);
            BluetoothPrintDriver.BT_Write(printerUtil.CRNL());
            BluetoothPrintDriver.LF();

            String totales = printerUtil.NL() + printerUtil.obtenerTituloTotales() +
                    printerUtil.NL() + "Cantidad        :" + detailResume.cantidadNotaCredito +
                    printerUtil.NL() + "Subtotal Nota   :" + (int) nota.Subtotal +
                    printerUtil.NL() + "Descuento Nota  :" + (int) nota.Descuento +
                    printerUtil.NL() + "Iva Total Nota  :" + (int) nota.Iva +
                    printerUtil.NL() + "Total Factura   :" + (int) factura.Total +
                    printerUtil.NL() + "Total Nota      :" + (int) nota.Valor;

            BluetoothPrintDriver.BT_Write(totales);
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.SetBold(BOLD);
            BluetoothPrintDriver.SetAlignMode(RIGHT_ALIGN);
            BluetoothPrintDriver.BT_Write("Total A Pagar: ");

            BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
            BluetoothPrintDriver.BT_Write((formatter.format((int) (factura.Total - nota.Valor))));
            BluetoothPrintDriver.LF();

            BluetoothPrintDriver.SetBold(CANCEL_BOLD);
            BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
            BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerFirmas());
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();

            if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(nota.QRInputValue)){
                Bitmap bitmap = null;
                try{
                    bitmap = Utilities.TextToQRBitmap(mContext, nota.QRInputValue);
                    if(bitmap != null){
                        byte[] command = printerUtil.decodeBitmap(bitmap);
                        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                        BluetoothPrintDriver.printByteData(command);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(bitmap != null) bitmap.recycle();
                }
            }

            BluetoothPrintDriver.CR();
            BluetoothPrintDriver.CR();

            BluetoothPrintDriver.CutPaper();

        } catch (Exception e) {
            throw new Exception("Error imprimiendo la Nota Crédito: " + e.getMessage());
        }
    }

    private void printFactura(FacturaDTO factura) throws Exception{

        PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
        BluetoothPrintDriver.SetLineSpacing(LINE_SPACING);
        BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezado());

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(ActivityBase.resolucion.NombreComercial);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Nit: " + ActivityBase.resolucion.Nit);
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();

        BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerRegimen());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosContacto());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEmail());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Vendedor: " + ActivityBase.resolucion.CodigoRuta);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Nombre: " + ActivityBase.resolucion.NombreRuta);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerNumeroCelularRuta());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosEntregador(factura.IdEmpleadoEntregador));
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
        BluetoothPrintDriver.SetBold(BOLD);
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTipoDocumentoNro(factura.FacturaPos, ActivityBase.resolucion.CodigoRuta));
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
        BluetoothPrintDriver.BT_Write(factura.NumeroFactura);
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
        BluetoothPrintDriver.SetBold(BOLD);
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerReglamentacion1());
        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
        BluetoothPrintDriver.SetBold(BOLD);
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerReglamentacion2());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerAnulada(factura.Anulada));
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.CR();


        BluetoothPrintDriver.SetBold(CANCEL_BOLD);
        BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
        BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloDatosCliente());
        BluetoothPrintDriver.BT_Write("\nCC/Nit: " + factura.Identificacion);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(factura.RazonSocial);
        BluetoothPrintDriver.LF();

        if (factura.Negocio != null) {
            BluetoothPrintDriver.BT_Write(factura.Negocio);
        }

        BluetoothPrintDriver.LF();

        if (factura.Direccion != null) {
            BluetoothPrintDriver.BT_Write(factura.Direccion);
        }

        BluetoothPrintDriver.LF();
        if (factura.Telefono != null) {
            BluetoothPrintDriver.BT_Write(factura.Telefono);
        }

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerMunicipioCliente(factura.IdCliente));

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerFormaDePago(factura.FormaPago));

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosCredito(factura));
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerFechaFactura(factura));

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerFechaVencimiento(factura) + printerUtil.NL());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());

        if (!TextUtils.isEmpty(factura.Comentario)) {
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(factura.Comentario);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        }

        if (!TextUtils.isEmpty(factura.ComentarioAnulacion)) {
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(factura.ComentarioAnulacion);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        }

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleDescripcionProducto());

        BluetoothPrintDriver.LF();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleValoresProductoConIvaFactura());
        } else {
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleValoresProducto());
        }
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();

        PrinterDetailResume detailResume = printerUtil.obtenerDetalleFactura(factura);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(detailResume.detailToPrint);

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();
        if (!detailResume.rotaciones.equals("")) {
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloRotaciones());
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(detailResume.rotaciones);
            BluetoothPrintDriver.LF();
        }

        if (!detailResume.devoluciones.equals("")) {
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloDevoluciones());
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerDetailResume(detailResume));
        }

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloTotales());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Cantidad   :" + detailResume.cantidadFactura);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Subtotal   :" + (int) factura.Subtotal);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Descuento  :" + (int) factura.Descuento);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTotalDevoluciones(detailResume));
        //BluetoothPrintDriver.BT_Write(printerUtil.obtenerResumenIvas(detailResume));
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTotalIvas(factura.Iva));
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerRetefuente(factura.Retefuente));
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerIpoconsumo(factura.IpoConsumo));
        BluetoothPrintDriver.LF();

        if (factura.ReteIva > 0) {
            BluetoothPrintDriver.BT_Write("Rete Iva   :" + (int) factura.ReteIva);
            BluetoothPrintDriver.LF();
        }

        BluetoothPrintDriver.SetBold(BOLD);
        BluetoothPrintDriver.SetAlignMode(RIGHT_ALIGN);
        BluetoothPrintDriver.BT_Write("A PAGAR: ");
        BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
        BluetoothPrintDriver.BT_Write((formatter.format((int) factura.Total)));
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
        BluetoothPrintDriver.SetBold(CANCEL_BOLD);
        BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerResolucionFacturacion(factura.FacturaPos));
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerPieDePagina());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerNumeroOrden(factura));
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerFirmas());
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();

        if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(factura.QRInputValue)){
            Bitmap bitmap = null;
            try{
                bitmap = Utilities.TextToQRBitmap(mContext, factura.QRInputValue);
                if(bitmap != null){
                    byte[] command = printerUtil.decodeBitmap(bitmap);
                    BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
                    BluetoothPrintDriver.printByteData(command);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(bitmap != null) bitmap.recycle();
            }
        }

        BluetoothPrintDriver.CutPaper();
    }

    private void printRemision(RemisionDTO remision) throws Exception{

        PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);
        ClienteDTO clienteDTO = printerUtil.obtenerCliente(remision.IdCliente);

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
        BluetoothPrintDriver.SetLineSpacing(LINE_SPACING);
        BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoRemision());

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerNombreComercial());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerNitRemision());
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();

        BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerRegimen());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerDatosContacto());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEmail());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Vendedor: " + ActivityBase.resolucion.CodigoRuta);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("Nombre: " + ActivityBase.resolucion.NombreRuta);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerNumeroCelularRuta());
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.SetAlignMode(CENTER_ALIGN);
        BluetoothPrintDriver.SetBold(BOLD);
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerLabelRemisionNro(clienteDTO.Remision, clienteDTO.Credito));
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
        BluetoothPrintDriver.BT_Write(remision.NumeroRemision);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerAnulada(remision.Anulada));
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.CR();

        BluetoothPrintDriver.SetBold(CANCEL_BOLD);
        BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
        BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloDatosCliente());
        BluetoothPrintDriver.BT_Write("\nCC/Nit: " + remision.IdentificacionCliente);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(remision.RazonSocialCliente);
        BluetoothPrintDriver.LF();
        if (remision.Negocio != null) {
            BluetoothPrintDriver.BT_Write(remision.Negocio);
        }
        BluetoothPrintDriver.LF();
        if (remision.DireccionCliente != null) {
            BluetoothPrintDriver.BT_Write(remision.DireccionCliente);
        }
        BluetoothPrintDriver.LF();
        if (remision.TelefonoCliente != null) {
            BluetoothPrintDriver.BT_Write(remision.TelefonoCliente);
        }

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerMunicipioCliente(remision.IdCliente));

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write("FECHA HORA: " + printerUtil.obtenerFecha(remision.Fecha));

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());

        if (!TextUtils.isEmpty(remision.Comentario)) {
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(remision.Comentario);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        }

        if (!TextUtils.isEmpty(remision.ComentarioAnulacion)) {
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(remision.ComentarioAnulacion);
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
        }

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleDescripcionProducto());

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerEncabezadoDetalleValoresProducto());
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();

        PrinterDetailResume detailResume = printerUtil.obtenerDetalleRemision(remision);
        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(detailResume.detailToPrint);

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();
        if (!detailResume.rotaciones.equals("")) {
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloRotaciones());
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(detailResume.rotaciones);
            BluetoothPrintDriver.LF();
        }

        if (!detailResume.devoluciones.equals("")) {
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerTituloDevoluciones());
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(detailResume.devoluciones);
            BluetoothPrintDriver.LF();
        }

        BluetoothPrintDriver.LF();
        BluetoothPrintDriver.BT_Write(printerUtil.obtenerTotalRemisiones(remision, detailResume));
        BluetoothPrintDriver.LF();

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

        BluetoothPrintDriver.SetBold(BOLD);
        BluetoothPrintDriver.SetAlignMode(RIGHT_ALIGN);
        BluetoothPrintDriver.BT_Write("A PAGAR: ");
        BluetoothPrintDriver.SetFontEnlarge(TEXT_LARGE);
        if (esActivoRetefuente) {
            if (retefuente > 0) {
                if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                    BluetoothPrintDriver.BT_Write((formatter.format(Math.round(valorTotalImpresion))));
                } else {
                    BluetoothPrintDriver.BT_Write((formatter.format(Math.round((remision.Total - retefuente ) - remision.ValorReteIva))));
                }
            } else {
                BluetoothPrintDriver.BT_Write((formatter.format(Math.round(remision.Total - remision.ValorReteIva))));
            }
        } else {
            if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                BluetoothPrintDriver.BT_Write((formatter.format(Math.round(valorTotalImpresion))));
            } else {
                BluetoothPrintDriver.BT_Write((formatter.format(Math.round(remision.Total - remision.ValorReteIva))));
            }
        }
        BluetoothPrintDriver.LF();

        BluetoothPrintDriver.SetAlignMode(LEFT_ALIGN);
        BluetoothPrintDriver.SetBold(CANCEL_BOLD);
        BluetoothPrintDriver.SetFontEnlarge(TEXT_NORMAL);

        String numeroOrden = printerUtil.obtenerNumeroOrden(remision);
        if(!TextUtils.isEmpty(numeroOrden)){
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(printerUtil.obtenerSeparador());
            BluetoothPrintDriver.LF();
            BluetoothPrintDriver.BT_Write(numeroOrden);
            BluetoothPrintDriver.LF();
        }

        BluetoothPrintDriver.BT_Write(printerUtil.obtenerFirmasRemision());
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();
        BluetoothPrintDriver.CR();

        BluetoothPrintDriver.CutPaper();
    }

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothPrintDriver(mContext, mHandler);
    }

    private void stopBluetoothServices() {
        if (mChatService != null) mChatService.stop();
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothPrintDriver.STATE_CONNECTED:
                            mIsConnected = true;
                            break;
                        case BluetoothPrintDriver.STATE_CONNECTING:
                            mIsConnected = false;
                            break;
                        case BluetoothPrintDriver.STATE_LISTEN:
                        case BluetoothPrintDriver.STATE_NONE:
                            mIsConnected = false;
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    String ErrorMsg = null;
                    byte[] readBuf = (byte[]) msg.obj;
                    float Voltage;
                    if (readBuf[2] == 0)
                        ErrorMsg = "NO ERROR!         ";
                    else {
                        if ((readBuf[2] & 0x02) != 0)
                            ErrorMsg = "ERROR: No printer connected!";
                        if ((readBuf[2] & 0x04) != 0)
                            ErrorMsg = "ERROR: No paper!  ";
                        if ((readBuf[2] & 0x08) != 0)
                            ErrorMsg = "ERROR: Voltage is too low!  ";
                        if ((readBuf[2] & 0x40) != 0)
                            ErrorMsg = "ERROR: Printer Over Heat!  ";
                    }
                    Voltage = (float) ((readBuf[0] * 256 + readBuf[1]) / 10.0);
                    //if(D) Log.i(TAG, "Voltage: "+Voltage);
                    mContext.makeLToast(ErrorMsg + "                                        "
                            + "Battery voltage" + Voltage + " V");
                    break;
            }
        }
    };


    // Message types sent from the BluetoothChatService Handler
    private static final int MESSAGE_STATE_CHANGE = 1;
    private static final int MESSAGE_READ = 2;

    //Alignment
    private static final byte LEFT_ALIGN = 0;
    private static final byte CENTER_ALIGN = 1;
    private static final byte RIGHT_ALIGN = 2;

    private static final byte LINE_SPACING = 30;

    private static final byte TEXT_LARGE = 0x11;
    private static final byte TEXT_NORMAL = 0x00;

    private static final byte BOLD = 0x01;
    private static final byte CANCEL_BOLD = 0x00;

}
