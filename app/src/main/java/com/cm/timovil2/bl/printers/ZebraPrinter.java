package com.cm.timovil2.bl.printers;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ResolucionDTO;

import com.zebra.android.comm.BluetoothPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnection;
//import com.zebra.android.comm.ZebraPrinterConnectionException;

public class ZebraPrinter{

    private final Context context;

    public ZebraPrinter(Context context){
        super();
        this.context = context;
    }

    /*public static void imprimir(String mensaje, Context context){

        try {
            ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            ZebraPrinterConnection thePrinterConn = new BluetoothPrinterConnection(resolucionDTO.MACImpresora);

            thePrinterConn.open();
            Thread.sleep(500);

            thePrinterConn.write(mensaje.getBytes());

            //Make sure the data got to the printer before closing the connection
            Thread.sleep(500);
            // Close the connection to release resources.
            thePrinterConn.close();
        } catch (Exception e) {
            Log.d("ZEBRA", e.getMessage());
        }
    }*/

    public void Print(final String msg){
        new Thread(new Runnable() {
            public void run() {
                try {
                    ResolucionDTO resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
                    ZebraPrinterConnection thePrinterConn = new BluetoothPrinterConnection(resolucionDTO.MACImpresora);
                    Looper.prepare();
                    thePrinterConn.open();
                    Thread.sleep(500);
                    thePrinterConn.write(msg.getBytes());
                    Thread.sleep(1000);
                    thePrinterConn.close();
                    Looper l = Looper.myLooper();
                    if(l!=null) l.quit();
                } catch (Exception e){
                    Log.d("ZEBRA", "Error ZEBRA PRINTER: "+ e.getMessage());
                }
            }
        }).start();
    }
} 