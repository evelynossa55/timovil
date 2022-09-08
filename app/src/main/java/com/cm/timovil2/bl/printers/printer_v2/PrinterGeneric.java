package com.cm.timovil2.bl.printers.printer_v2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

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
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 24/07/18.
 */

class PrinterGeneric extends Printer {

    PrinterGeneric(String macAddress, ActivityBase context, int numeroCopias, String pulgadas) {
        super(macAddress, context, numeroCopias, pulgadas);
        formatter = NumberFormat.getCurrencyInstance(Locale.US);
    }
    NumberFormat formatter = null;

    @Override
    void connect() throws Exception {

        try {
            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bd = ba.getRemoteDevice(mMacAddress);

            //1ER INTENTO
            try {
                if (Build.VERSION.SDK_INT >= 15) {
                    UUID uuid = bd.getUuids()[0].getUuid();
                    mBtsocket = bd.createRfcommSocketToServiceRecord(uuid);
                    mBtsocket.connect();
                    mIsConnected = true;
                }
            } catch (Exception ex) {
                mIsConnected = false;
            }

            Class[] c = new Class[1];
            c[0] = int.class;

            //2DO INTENTO
            if (!mIsConnected) {
                Method m = bd.getClass().getMethod("createRfcommSocket", c);
                mBtsocket = (BluetoothSocket) m.invoke(bd, 1);
                try {
                    ba.cancelDiscovery();
                    mBtsocket.connect();
                    mIsConnected = true;
                } catch (IOException ex) {
                    mIsConnected = false;
                }
            }

            //3ER INTENTO
            if (!mIsConnected) {
                Method m = bd.getClass().getMethod("createInsecureRfcommSocket", c);
                mBtsocket = (BluetoothSocket) m.invoke(bd, 1);
                try {
                    ba.cancelDiscovery();
                    mBtsocket.connect();
                    mIsConnected = true;
                } catch (IOException ex) {
                    mIsConnected = false;
                }
            }

            //4TO INTENTO
            if (!mIsConnected) {
                UUID uid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                mBtsocket = bd.createRfcommSocketToServiceRecord(uid);
                try {
                    ba.cancelDiscovery();
                    mBtsocket.connect();
                    mIsConnected = true;
                } catch (IOException ex) {
                    mIsConnected = false;
                }
            }

            //5TO INTENTO
            if (!mIsConnected) {
                UUID uid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                mBtsocket = bd.createInsecureRfcommSocketToServiceRecord(uid);
                try {
                    ba.cancelDiscovery();
                    mBtsocket.connect();
                    mIsConnected = true;
                } catch (IOException ex) {
                    mIsConnected = false;
                }
            }

            if (!mIsConnected) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

        } catch (Exception ex) {
            throw new Exception(ERROR);
        }

    }

    @Override
    public void printConfig() throws Exception {
        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(1500);

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {
                Thread.sleep(1000);

                String textToPrint = "IMPRESORA GUARDADA CORRECTAMENTE\r\n";

                printCustom(textToPrint, TEXT_BOLD_MEDIUM, ALIGN_CENTER);
                printNewLine();
                printNewLine();
                printNewLine();

            }
        }
    }

    @Override
    public void print(FacturaDTO factura) throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(1500);

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {

                for (int i = 0; i < mNumeroCopias; i++) {
                    printFactura(factura);
                    Thread.sleep(1500);
                }

                if (factura.notaCreditoFactura != null) {
                    printNotaAux(factura.notaCreditoFactura);
                    Thread.sleep(1500);
                }

                Thread.sleep(1000);
                mOutputStream.flush();
            }

        }
    }

    @Override
    public void printFacturas(List<FacturaDTO> facturas) throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(1500);

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {

                for (FacturaDTO factura : facturas) {
                    printFactura(factura);
                    Thread.sleep(2000);
                }

                mOutputStream.flush();
            }

        }
    }

    @Override
    public void print(RemisionDTO remision) throws Exception {

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {

                for (int i = 0; i < mNumeroCopias; i++) {
                    printRemision(remision);
                    Thread.sleep(1500);
                }
            }
        }

    }

    @Override
    public void printRemisiones(List<RemisionDTO> remisiones) throws Exception{

        if (TextUtils.isEmpty(mMacAddress))
            throw new Exception("La impresora no está configurada");

        connect();
        Thread.sleep(1500);

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {

                for (RemisionDTO remision : remisiones) {
                    printRemision(remision);
                    Thread.sleep(2000);
                }

                mOutputStream.flush();
            }

        }
    }

    @Override
    public void print(NotaCreditoFacturaDTO nota) throws Exception {
        connect();

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {

                printNotaAux(nota);

                //After print
                Thread.sleep(1500);
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
        Thread.sleep(1500);

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {
                Thread.sleep(1000);

                PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                printNewLine();
                printCustom(ActivityBase.resolucion.NombreComercial, TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom("Nit: " + ActivityBase.resolucion.Nit, TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printNewLine();

                printCustom(printerUtil.obtenerRegimen(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerDatosContacto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerEmail(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printNewLine();

                printCustom("Vendedor: " + ActivityBase.resolucion.CodigoRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom("Nombre: " + ActivityBase.resolucion.NombreRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerNumeroCelularRuta(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();

                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();

                printCustom("RESUMEN DE FACTURACION", TEXT_BOLD_MEDIUM, ALIGN_CENTER);
                printNewLine();
                printCustom(" FECHA: " + Utilities.FechaDetallada(new Date()), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(resumen.detallePrimeraYultimaFactura, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom(resumen.detalleResumenToPrint, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();

                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();

                printCustom("RESUMEN DE REMISIONES", TEXT_BOLD_MEDIUM, ALIGN_CENTER);
                printNewLine();

                if(resumen.detalleRemisionToPrint.equals("")){
                    printCustom("No hay remisiones", TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                }else{
                    printCustom(resumen.detallePrimeraYultimaRemision, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                    printNewLine();
                    printCustom(resumen.detalleRemisionToPrint, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                }

                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom("RESUMEN DE NOTAS CREDITO POR DEVOLUCION", TEXT_BOLD_MEDIUM, ALIGN_CENTER);
                printNewLine();
                if(resumen.detalleResumenNotasCreditoPorDevolucionToPrint.equals("")){
                    printCustom("No hay notas credito por devolucion", TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                }else{
                    printCustom(resumen.detallePrimeraYultimaNotaCreditoPorDevolucion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                    printNewLine();
                    printCustom(resumen.detalleResumenNotasCreditoPorDevolucionToPrint, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                }

                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom(resumen.resumen, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom(resumen.resumenRemisiones, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom(resumen.resumenNotasCreditoPorDevolucion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom(resumen.resumenRemision, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
                printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printCustom(resumen.resumenCantidades, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();

                printCustom(printerUtil.obtenerUrlTimovil(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                printNewLine();
                printNewLine();
                printNewLine();

            }
        }
    }

    @Override
    public void printInventario() throws Exception{
        connect();
        Thread.sleep(1500);

        if (mIsConnected && ActivityBase.resolucion != null) {

            try {
                mOutputStream = mBtsocket.getOutputStream();
            } catch (IOException e) {
                throw new Exception(ERROR_CONNECTION_FAILED);
            }

            if (mOutputStream != null) {

                if (ActivityBase.resolucion != null) {

                    PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

                    printNewLine();
                    printCustom(ActivityBase.resolucion.NombreComercial, TEXT_BOLD_NORMAL, ALIGN_CENTER);
                    printNewLine();
                    printCustom("Nit: " + ActivityBase.resolucion.Nit, TEXT_BOLD_NORMAL, ALIGN_CENTER);
                    printNewLine();
                    printNewLine();

                    printCustom(printerUtil.obtenerRegimen(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerDatosContacto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerEmail(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printNewLine();

                    printCustom("Vendedor: " + ActivityBase.resolucion.CodigoRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom("Nombre: " + ActivityBase.resolucion.NombreRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerNumeroCelularRuta(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();

                    printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                    printNewLine();
                    printCustom(" FECHA: " + Utilities.FechaDetallada(new Date()), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
                    printNewLine();

                    printCustom("INVENTARIO", TEXT_BOLD_MEDIUM, ALIGN_CENTER);
                    printNewLine();

                    printCustom(printerUtil.obtenerImpresionIventario(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printCustom(printerUtil.obtenerUrlTimovil(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
                    printNewLine();
                    printNewLine();
                    printNewLine();
                }
            }
        }
    }

    private void printNotaAux(NotaCreditoFacturaDTO nota) throws Exception {

        try {

            PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

            FacturaDTO factura = new FacturaDAL(mContext).obtenerPorNumeroFac(nota.NumeroFactura);
            if (factura == null) {
                throw new Exception("La factura asociada " + nota.NumeroFactura + " ya ha sido eliminada del dispositivo");
            }

            printNewLine();
            printCustom(ActivityBase.resolucion.NombreComercial, TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom("Nit: " + ActivityBase.resolucion.Nit, TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printNewLine();

            printCustom(printerUtil.obtenerRegimen(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerDatosContacto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerEmail(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printNewLine();

            printCustom("Vendedor: " + ActivityBase.resolucion.CodigoRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom("Nombre: " + ActivityBase.resolucion.NombreRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerNumeroCelularRuta(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();

            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom("NOTA CREDITO NRO. ", TEXT_BOLD_LARGE, ALIGN_CENTER);
            printNewLine();
            printCustom(nota.NumeroDocumento, TEXT_BOLD_MEDIUM, ALIGN_CENTER);
            printNewLine();
            printCustom("POR DEVOLUCION", TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom("FACTURA DE VENTA NRO. ", TEXT_BOLD_LARGE, ALIGN_CENTER);
            printNewLine();
            printCustom(nota.NumeroFactura, TEXT_BOLD_MEDIUM, ALIGN_CENTER);
            printNewLine();
            printNewLine();

            printCustom(printerUtil.obtenerTituloDatosCliente(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom("CC/Nit: " + factura.Identificacion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(factura.RazonSocial, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();

            if (factura.Negocio != null) {
                printCustom(factura.Negocio, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
            }
            if (factura.Direccion != null) {
                printCustom(factura.Direccion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
            }
            if (factura.Telefono != null) {
                printCustom(factura.Telefono, TEXT_BOLD_NORMAL, ALIGN_LEFT);
                printNewLine();
            }

            printCustom(printerUtil.obtenerMunicipioCliente(factura.IdCliente), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();

            printCustom(printerUtil.obtenerFechaFactura(factura), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();

            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();

            printCustom(printerUtil.obtenerEncabezadoDetalleDescripcionProducto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerEncabezadoDetalleValoresProductoNotaCredito(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printNewLine();

            PrinterDetailResume detailResume = printerUtil.obtenerDetalleNotaCreditoFacturaPorDevolucion(nota.DetalleNotaCreditoFactura);
            printCustom(detailResume.detailToPrint, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            DecimalFormat formatea = new DecimalFormat("###,###.##");
            String totales = printerUtil.NL() + printerUtil.obtenerTituloTotales() +
                    printerUtil.NL() + "Cantidad        :" + detailResume.cantidadNotaCredito +
                    printerUtil.NL() + "Subtotal Nota   :" + formatea.format((int)nota.Subtotal) +
                    printerUtil.NL() + "Descuento Nota  :" + formatea.format((int) nota.Descuento) +
                    printerUtil.NL() + "Iva Total Nota  :" + formatea.format((int) nota.Iva) +
                    printerUtil.NL() + "Total Factura   :" + formatea.format((int) factura.Total) +
                    printerUtil.NL() + "Total Nota      :" + formatea.format((int) nota.Valor);

            printCustom(totales, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();

            printCustom("Total A Pagar: " + formatea.format((int) (factura.Total - nota.Valor)), TEXT_BOLD_LARGE, ALIGN_RIGHT);
            printNewLine();

            printCustom(printerUtil.obtenerFirmas(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printNewLine();
            printNewLine();

            boolean esFacturadoraElectronica = printerUtil.obtenerCliente(factura.IdCliente).FacturacionElectronicaCliente;

            if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(nota.QRInputValue) && esFacturadoraElectronica){
                Bitmap bitmap = Utilities.TextToQRBitmap(mContext, nota.QRInputValue);
                printBitmap(bitmap);
            }

        } catch (Exception e) {
            throw new Exception("Error imprimiendo la Nota Crédito: " + e.getMessage());
        }
    }

    private void printFactura(FacturaDTO factura) throws Exception {

        PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);

        byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
        mOutputStream.write(printformat);
        Thread.sleep(1000);

        printNewLine();
        printCustom(printerUtil.obtenerEncabezado(), TEXT_BOLD_NORMAL, ALIGN_CENTER);

        printNewLine();
        printCustom(ActivityBase.resolucion.NombreComercial, TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom("Nit: " + ActivityBase.resolucion.Nit, TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printNewLine();

        printCustom(printerUtil.obtenerRegimen(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerDatosContacto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerEmail(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printNewLine();

        printCustom("Vendedor: " + ActivityBase.resolucion.CodigoRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom("Nombre: " + ActivityBase.resolucion.NombreRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerNumeroCelularRuta(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerDatosEntregador(factura.IdEmpleadoEntregador), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerTipoDocumentoNro(factura.FacturaPos, ActivityBase.resolucion.CodigoRuta), TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();
        printCustom(factura.NumeroFactura, TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerReglamentacion1(), TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerReglamentacion2(), TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerAnulada(factura.Anulada), TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();

        printCustom(printerUtil.obtenerTituloDatosCliente(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom("CC/Nit: " + factura.Identificacion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(factura.RazonSocial, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        if (factura.Negocio != null) {
            printCustom(factura.Negocio, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        if (factura.Direccion != null) {
            printCustom(factura.Direccion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        if (factura.Telefono != null) {
            printCustom(factura.Telefono, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        printCustom(printerUtil.obtenerMunicipioCliente(factura.IdCliente), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerFormaDePago(factura.FormaPago), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerDatosCredito(factura), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();

        printCustom(printerUtil.obtenerFechaFactura(factura), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerFechaVencimiento(factura), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();

        if (!TextUtils.isEmpty(factura.Comentario)) {
            printCustom(factura.Comentario, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        if (!TextUtils.isEmpty(factura.ComentarioAnulacion)) {
            printCustom(factura.ComentarioAnulacion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
        }

        printCustom(printerUtil.obtenerEncabezadoDetalleDescripcionProducto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        if (ActivityBase.resolucion.IdCliente.equals(Utilities.ID_HERNAN_MORA)
                || ActivityBase.resolucion.IdCliente.equals(Utilities.ID_PRUEBAS_TIMOVIL)) {
            printCustom(printerUtil.obtenerEncabezadoDetalleValoresProductoConIvaFactura(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        } else {
            printCustom(printerUtil.obtenerEncabezadoDetalleValoresProducto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        }
        printNewLine();

        PrinterDetailResume detailResume = printerUtil.obtenerDetalleFactura(factura);
        printCustom(detailResume.detailToPrint, TEXT_BOLD_NORMAL, ALIGN_LEFT);

        printNewLine();
        if (!detailResume.rotaciones.equals("")) {
            printCustom(printerUtil.obtenerTituloRotaciones(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom(detailResume.rotaciones, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        if (!detailResume.devoluciones.equals("")) {
            printCustom(printerUtil.obtenerTituloDevoluciones(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printCustom(printerUtil.obtenerDetailResume(detailResume), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        }
        DecimalFormat formatea = new DecimalFormat("###,###.##");
        printNewLine();
        printCustom(printerUtil.obtenerTituloTotales(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom("Cantidad   :" + detailResume.cantidadFactura, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom("Subtotal   :" + formatea.format((int) factura.Subtotal), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom("Descuento  :" + formatea.format((int) factura.Descuento), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerTotalDevoluciones(detailResume), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        //printCustom(printerUtil.obtenerResumenIvas(detailResume), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printCustom(printerUtil.obtenerTotalIvas(factura.Iva), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerRetefuente(factura.Retefuente), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerIpoconsumo(factura.IpoConsumo), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        if (factura.ReteIva > 0) {
            printCustom("Rete Iva   :" + formatea.format((int) factura.ReteIva), TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        printCustom("A PAGAR: " + formatea.format((int) factura.Total), TEXT_BOLD_LARGE, ALIGN_RIGHT);
        printNewLine();
        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();

        printCustom(printerUtil.obtenerResolucionFacturacion(factura.FacturaPos), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printCustom(printerUtil.obtenerPieDePagina(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printCustom(printerUtil.obtenerNumeroOrden(factura), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerFirmas(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printNewLine();

        boolean esFacturadoraElectronica = printerUtil.obtenerCliente(factura.IdCliente).FacturacionElectronicaCliente;

        if(ActivityBase.resolucion.EFactura && !TextUtils.isEmpty(factura.QRInputValue) && esFacturadoraElectronica){
            Bitmap bitmap = Utilities.TextToQRBitmap(mContext, factura.QRInputValue);
            printBitmap(bitmap);
        }
    }

    private void printRemision(RemisionDTO remision) throws Exception {

        PrinterUtil printerUtil = new PrinterUtil(mContext, mPulgadas);
        ClienteDTO clienteDTO = printerUtil.obtenerCliente(remision.IdCliente);

        byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
        mOutputStream.write(printformat);
        Thread.sleep(1000);

        printNewLine();

        printCustom(printerUtil.obtenerEncabezadoRemision(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();

        printCustom(printerUtil.obtenerNombreComercial(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerNitRemision(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printNewLine();

        printCustom(printerUtil.obtenerRegimen(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerDatosContacto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerEmail(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printNewLine();

        printCustom("Vendedor: " + ActivityBase.resolucion.CodigoRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom("Nombre: " + ActivityBase.resolucion.NombreRuta, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(printerUtil.obtenerNumeroCelularRuta(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerLabelRemisionNro(clienteDTO.Remision, clienteDTO.Credito), TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();
        printCustom(remision.NumeroRemision, TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();
        printCustom(printerUtil.obtenerAnulada(remision.Anulada), TEXT_BOLD_LARGE, ALIGN_CENTER);
        printNewLine();

        printCustom(printerUtil.obtenerTituloDatosCliente(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();
        printCustom("CC/Nit: " + remision.IdentificacionCliente, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printCustom(remision.RazonSocialCliente, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        if (remision.Negocio != null) {
            printCustom(remision.Negocio, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }
        if (remision.DireccionCliente != null) {
            printCustom(remision.DireccionCliente, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }
        if (remision.TelefonoCliente != null) {
            printCustom(remision.TelefonoCliente, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        printCustom(printerUtil.obtenerMunicipioCliente(remision.IdCliente), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        printNewLine();

        printCustom("FECHA HORA: " + printerUtil.NL()
                + printerUtil.obtenerFecha(remision.Fecha), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);

        if (!TextUtils.isEmpty(remision.Comentario)) {
            printNewLine();
            printCustom(remision.Comentario, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        }

        if (!TextUtils.isEmpty(remision.ComentarioAnulacion)) {
            printNewLine();
            printCustom(remision.ComentarioAnulacion, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
            printCustom(printerUtil.obtenerSeparador(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
        }

        printNewLine();
        printCustom(printerUtil.obtenerEncabezadoDetalleDescripcionProducto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

        printCustom(printerUtil.obtenerEncabezadoDetalleValoresProducto(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printNewLine();

        PrinterDetailResume detailResume = printerUtil.obtenerDetalleRemision(remision);
        printCustom(detailResume.detailToPrint, TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printNewLine();

        if (!detailResume.rotaciones.equals("")) {
            printCustom(printerUtil.obtenerTituloRotaciones(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom(detailResume.rotaciones, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        if (!detailResume.devoluciones.equals("")) {
            printNewLine();
            printCustom(printerUtil.obtenerTituloDevoluciones(), TEXT_BOLD_NORMAL, ALIGN_CENTER);
            printNewLine();
            printCustom(detailResume.devoluciones, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }

        printCustom(printerUtil.obtenerTotalRemisiones(remision, detailResume), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();

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
                    printCustom("A PAGAR: " + formatea.format(Math.round(valorTotalImpresion)), TEXT_BOLD_LARGE, ALIGN_RIGHT);
                } else {
                    printCustom("A PAGAR: " + formatea.format(Math.round(((remision.Total - retefuente)) - remision.ValorReteIva)), TEXT_BOLD_LARGE, ALIGN_RIGHT);
                }
            } else {
                printCustom("A PAGAR: " + formatea.format(Math.round(remision.Total - remision.ValorReteIva)), TEXT_BOLD_LARGE, ALIGN_RIGHT);
            }
        } else {
            if (ActivityBase.resolucion.DevolucionAfectaRemision) {
                printCustom("A PAGAR: " + formatea.format(Math.round(valorTotalImpresion)), TEXT_BOLD_LARGE, ALIGN_RIGHT);
            } else {
                printCustom("A PAGAR: " + formatea.format(Math.round(remision.Total - remision.ValorReteIva)), TEXT_BOLD_LARGE, ALIGN_RIGHT);
            }
        }
        printNewLine();

        String numeroOrden = printerUtil.obtenerNumeroOrden(remision);
        if(!TextUtils.isEmpty(numeroOrden)) {
            printCustom(numeroOrden, TEXT_BOLD_NORMAL, ALIGN_LEFT);
            printNewLine();
        }
        printCustom(printerUtil.obtenerFirmasRemision(), TEXT_BOLD_NORMAL, ALIGN_LEFT);
        printNewLine();
        printNewLine();
        printNewLine();
    }

    private void printNewLine() {
        try {
            mOutputStream.write(FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Generic Printer Config
    private void printCustom(String msg, int size, int align) {

        if (TextUtils.isEmpty(msg)) return;

        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text

        try {
            switch (size) {
                case TEXT_NORMAL:
                    mOutputStream.write(cc);
                    break;
                case TEXT_BOLD_NORMAL:
                    mOutputStream.write(bb);
                    break;
                case TEXT_BOLD_MEDIUM:
                    mOutputStream.write(bb2);
                    break;
                case TEXT_BOLD_LARGE:
                    mOutputStream.write(bb3);
                    break;
            }

            switch (align) {
                case ALIGN_LEFT:
                    //left align
                    mOutputStream.write(ESC_ALIGN_LEFT);
                    break;
                case ALIGN_CENTER:
                    //center align
                    mOutputStream.write(ESC_ALIGN_CENTER);
                    break;
                case ALIGN_RIGHT:
                    //right align
                    mOutputStream.write(ESC_ALIGN_RIGHT);
                    break;
            }

            mOutputStream.write(msg.getBytes());
            //mOutputStream.write(LF);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printBitmap(Bitmap bitmap){
        try {
            mOutputStream.write(ESC_ALIGN_CENTER);
            byte[] bytes = new PrinterUtil(mContext, mPulgadas).decodeBitmap(bitmap);
            mOutputStream.write(bytes);
            printNewLine();
            printNewLine();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            bitmap.recycle();
        }
    }

    /*private String leftRightAlign(String str1, String str2) {
        String ans = str1 + str2;
        if (ans.length() < 31) {
            int n = (31 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }*/

    //private static final byte LF = 0x0A;

    //private static final byte[] ESC_FONT_COLOR_DEFAULT = new byte[]{0x1B, 'r', 0x00};
    //private static final byte[] FS_FONT_ALIGN = new byte[]{0x1C, 0x21, 1, 0x1B, 0x21, 1};
    private static final byte[] ESC_ALIGN_LEFT = new byte[]{0x1b, 'a', 0x00};
    private static final byte[] ESC_ALIGN_RIGHT = new byte[]{0x1b, 'a', 0x02};
    private static final byte[] ESC_ALIGN_CENTER = new byte[]{0x1b, 'a', 0x01};
    //private static final byte[] ESC_CANCEL_BOLD = new byte[]{0x1B, 0x45, 0};

    private static final int ALIGN_LEFT = 0;
    private static final int ALIGN_CENTER = 1;
    private static final int ALIGN_RIGHT = 2;

    private static final int TEXT_NORMAL = 0;
    private static final int TEXT_BOLD_NORMAL = 1;
    private static final int TEXT_BOLD_MEDIUM = 2;
    private static final int TEXT_BOLD_LARGE = 3;

    private static byte[] FEED_LINE = {10};
}
