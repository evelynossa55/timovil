package com.cm.timovil2.bl.printers.btservice;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.cm.timovil2.bl.printers.btservice.print.GPrinterCommand;
import com.cm.timovil2.bl.printers.btservice.print.PrintPic;
import com.cm.timovil2.bl.printers.btservice.print.PrintQueue;
import com.cm.timovil2.bl.printers.btservice.print.PrintUtil;

import java.io.BufferedInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by yefeng on 6/2/15.
 * github:yefengfreedom
 * <p/>
 * print ticket service
 */
public class BtService extends IntentService {

    private String macAddress;

    public BtService() {
        super("BtService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BtService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        this.macAddress = intent.getStringExtra(PrintUtil.MAC_EXTRA);

        if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TEST)) {
            printTest();
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT)) {
            print(intent.getByteArrayExtra(PrintUtil.PRINT_EXTRA));
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TICKET)) {
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_BITMAP)) {
            printBitmapTest();
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_PAINTING)) {
            printPainting();
        }
    }

    private void printTest() {
        try {
            ArrayList<byte[]> bytes = new ArrayList<>();
            String message = "Prueba de impresión Bluetooth\r\nPrueba de impresion Bluetooth\r\nPrueba de impresion Bluetooth\r\n";
            bytes.add(GPrinterCommand.reset);
            bytes.add(message.getBytes("gbk"));
            bytes.add(GPrinterCommand.print);
            bytes.add(GPrinterCommand.print);
            bytes.add(GPrinterCommand.print);
            PrintQueue.getQueue(getApplicationContext(), macAddress).add(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void print(byte[] byteArrayExtra) {
        if (null == byteArrayExtra || byteArrayExtra.length <= 0) {
            return;
        }
        PrintQueue.getQueue(getApplicationContext(), macAddress).add(byteArrayExtra);
    }

    private void printBitmapTest() {
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(getAssets().open(
                    "name.bmp"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(bis);
        PrintPic printPic = PrintPic.getInstance();
        printPic.init(bitmap);
        if (null != bitmap) {
            if (bitmap.isRecycled()) {
                bitmap = null;
            } else {
                bitmap.recycle();
                bitmap = null;
            }
        }
        byte[] bytes = printPic.printDraw();
        ArrayList<byte[]> printBytes = new ArrayList<byte[]>();
        printBytes.add(GPrinterCommand.reset);
        printBytes.add(GPrinterCommand.print);
        printBytes.add(bytes);
        Log.e("BtService", "image bytes size is :" + bytes.length);
        printBytes.add(GPrinterCommand.print);
        PrintQueue.getQueue(getApplicationContext(), macAddress).add(bytes);
    }

    private void printPainting() {
        byte[] bytes = PrintPic.getInstance().printDraw();
        ArrayList<byte[]> printBytes = new ArrayList<byte[]>();
        printBytes.add(GPrinterCommand.reset);
        printBytes.add(GPrinterCommand.print);
        printBytes.add(bytes);
        Log.e("BtService", "image bytes size is :" + bytes.length);
        printBytes.add(GPrinterCommand.print);
        PrintQueue.getQueue(getApplicationContext(), macAddress).add(bytes);
    }
}