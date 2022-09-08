package com.cm.timovil2.bl.printers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.printers.btservice.BtService;
import com.cm.timovil2.bl.printers.btservice.print.PrintUtil;

public class FinalPrinter {

    /**
     * Reflection: createRfcommSocket
     *
     * @param address MAC de impresora
     * @param msg     to be writed to the printer
     * @throws Exception If cannot connect or message is empty
     */

    public static void Print(String address, String msg) throws Exception {
        try {
            if (address == null || TextUtils.isEmpty(address))
                throw new Exception("Debe especificar la dirección de la impresora.");

            if (msg == null || TextUtils.isEmpty(msg))
                throw new Exception("Debe especificar el texto a imprimir.");

            //OBTENIENDO Y VALIDANDO LA CONEXIÓN CON LA IMPRESORA
            BluetoothDevice bt;
            BluetoothAdapter ba;
            try {
                BluetoothAdapter batmp = BluetoothAdapter.getDefaultAdapter();
                if (batmp != null) {
                    bt = batmp.getRemoteDevice(address);
                    ba = batmp;
                } else {
                    throw new Exception("La tecnología Bluetooth no es soportada por su dispositivo móvil");
                }
            } catch (IllegalArgumentException ex) {
                throw new Exception("Por favor configure nuevamente la impresora.");
            }

            Method m;
            Class[] c = new Class[1];
            c[0] = int.class;
            m = bt.getClass().getMethod("createRfcommSocket", c);
            BluetoothSocket clientSocket = (BluetoothSocket) m.invoke(bt, 1);
            try {
                ba.cancelDiscovery();
                clientSocket.connect();
            } catch (IOException ex) {
                clientSocket.close();
                throw new Exception("No se pudo conectar con la impresora, verifique que ésta se encuentra encendida.");
            }
            Thread.sleep(500);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.write(msg.getBytes()); //Aquí va el texto que se va a imprimir
            Thread.sleep(500);
            out.flush();
            out.close();
            clientSocket.close();
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new Exception("IllegalArgumentException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException: Verifique que la impresora " +
                    "se encuentra encendida" + (e.getMessage() != null ? (", " + e.getMessage()) : ""));
        } catch (Exception e) {
            throw new Exception("Error general: " + e.getMessage());
        }
    }

    /**
     * Reflection :  createRfcommSocket
     *
     * @param address mac
     * @param msg Message to be printed
     * @param context Context
     * @throws Exception If cannot connect or message is empty
     */
    public static void Print(String address, String msg, Context context) throws Exception {
        try {
            if (address == null || TextUtils.isEmpty(address))
                throw new Exception("Debe especificar la dirección de la impresora.");

            if (msg == null || TextUtils.isEmpty(msg))
                throw new Exception("Debe especificar el texto a imprimir.");

            //OBTENIENDO Y VALIDANDO LA CONEXIÓN CON LA IMPRESORA
            BluetoothDevice bt;
            BluetoothAdapter ba;
            try {
                BluetoothAdapter batmp = BluetoothAdapter.getDefaultAdapter();
                if (batmp != null) {
                    bt = batmp.getRemoteDevice(address);
                    ba = batmp;
                } else {
                    throw new Exception("La tecnología Bluetooth no es soportada por su dispositivo móvil");
                }
            } catch (IllegalArgumentException ex) {
                throw new Exception("Por favor configure nuevamente la impresora.");
            }

            Class[] c = new Class[1];
            c[0] = int.class;
            Method m = bt.getClass().getMethod("createRfcommSocket", c);
            BluetoothSocket clientSocket = (BluetoothSocket) m.invoke(bt, 1);
            try {
                ba.cancelDiscovery();
                clientSocket.connect();
            } catch (IOException ex) {
                throw new Exception("No se pudo conectar con la impresora, verifique que ésta se encuentra encendida.");
            }
            Thread.sleep(500);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            boolean dividirImpresion = App.obtenerPreferencias_DividirImpresion(context);
            if (dividirImpresion) {

                String[] separacion = msg.split("\\n");
                int cantidadLineas = App.obtenerPreferencias_LineasPorEnvio(context);
                int pendiente = 0;
                StringBuilder sbImpresion = new StringBuilder();

                for (String linea : separacion) {
                    sbImpresion.append(linea).append("\n");
                    pendiente++;
                    if (pendiente >= cantidadLineas) {
                        out.write(sbImpresion.toString().getBytes()); //Aquí va el texto que se va a imprimir
                        out.flush();
                        pendiente = 0;
                        sbImpresion = new StringBuilder();
                    }
                }

                if (pendiente > 0) {
                    out.write(sbImpresion.toString().getBytes());
                }
            } else {
                out.write(msg.getBytes());
            }

            out.flush();
            Thread.sleep(500);
            out.close();
            clientSocket.close();
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new Exception("IllegalArgumentException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException: Verifique que la impresora " +
                    "se encuentra encendida" + (e.getMessage() != null ? (", " + e.getMessage()) : ""));
        } catch (Exception e) {
            throw new Exception("Error general: " + e.getMessage());
        }
    }

    public static void PrintBytes(String address, String msg, Context context) throws Exception {
        try {
            if (address == null || TextUtils.isEmpty(address))
                throw new Exception("Debe especificar la dirección de la impresora.");

            if (msg == null || TextUtils.isEmpty(msg))
                throw new Exception("Debe especificar el texto a imprimir.");

            //OBTENIENDO Y VALIDANDO LA CONEXIÓN CON LA IMPRESORA
            BluetoothDevice bt;
            BluetoothAdapter ba;
            try {
                BluetoothAdapter batmp = BluetoothAdapter.getDefaultAdapter();
                if (batmp != null) {
                    bt = batmp.getRemoteDevice(address);
                    ba = batmp;
                } else {
                    throw new Exception("La tecnología Bluetooth no es soportada por su dispositivo móvil");
                }
            } catch (IllegalArgumentException ex) {
                throw new Exception("Por favor configure nuevamente la impresora.");
            }
            Class[] c = new Class[1];
            c[0] = int.class;
            Method m = bt.getClass().getMethod("createRfcommSocket", c);
            BluetoothSocket clientSocket = (BluetoothSocket) m.invoke(bt, 1);
            try {
                ba.cancelDiscovery();
                clientSocket.connect();
            } catch (IOException ex) {
                throw new Exception("No se pudo conectar con la impresora, verifique que ésta se encuentra encendida.");
            }
            Thread.sleep(500);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            boolean dividirImpresion = App.obtenerPreferencias_DividirImpresion(context);
            if (dividirImpresion) {

                String[] separacion = msg.split("\\n");
                int cantidadLineas = App.obtenerPreferencias_LineasPorEnvio(context);
                int pendiente = 0;
                StringBuilder sbImpresion = new StringBuilder();
                for (String linea : separacion) {
                    sbImpresion.append(linea).append("\n");
                    pendiente++;
                    if (pendiente >= cantidadLineas) {
                        byte[] send;
                        try {
                            send = sbImpresion.toString().getBytes("GB2312");
                        } catch (UnsupportedEncodingException e) {
                            send = sbImpresion.toString().getBytes();
                        }
                        Thread.sleep(2000);
                        out.write(send);
                        out.flush();
                        Thread.sleep(1000);
                        pendiente = 0;
                        sbImpresion = new StringBuilder();
                    }
                }

                if (pendiente > 0) {
                    byte[] send;
                    try {
                        send = sbImpresion.toString().getBytes("GB2312");
                    } catch (UnsupportedEncodingException e) {
                        send = sbImpresion.toString().getBytes();
                    }
                    Thread.sleep(2000);
                    out.write(send);
                }
            } else {
                byte[] send;
                try {
                    send = msg.getBytes("GB2312");
                } catch (UnsupportedEncodingException e) {
                    send = msg.getBytes();
                }
                Thread.sleep(2000);
                out.write(send);
            }

            out.flush();
            Thread.sleep(500);
            out.close();
            clientSocket.close();
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new Exception("IllegalArgumentException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException: Verifique que la impresora " +
                    "se encuentra encendida" + (e.getMessage() != null ? (", " + e.getMessage()) : ""));
        } catch (Exception e) {
            throw new Exception("Error general: " + e.getMessage());
        }
    }

    /**
     * Reflection: createInsecureRfcommSocket
     * @param address MAC of the printer
     * @param msg     Message to be printed
     * @param context : Context
     * @throws Exception If cannot connect or message is empty
     */
    public static void Print2(String address, String msg, Context context) throws Exception {
        try {
            if (address == null || TextUtils.isEmpty(address))
                throw new Exception("Debe especificar la dirección de la impresora.");

            if (msg == null || TextUtils.isEmpty(msg))
                throw new Exception("Debe especificar el texto a imprimir.");

            //OBTENIENDO Y VALIDANDO LA CONEXIÓN CON LA IMPRESORA
            BluetoothDevice bt;
            BluetoothAdapter ba;
            try {
                BluetoothAdapter batmp = BluetoothAdapter.getDefaultAdapter();
                if (batmp != null) {
                    bt = batmp.getRemoteDevice(address);
                    ba = batmp;
                } else {
                    throw new Exception("La tecnología Bluetooth no es soportada por su dispositivo móvil");
                }
            } catch (IllegalArgumentException ex) {
                throw new Exception("Por favor configure nuevamente la impresora.");
            }
            Class[] c = new Class[1];
            c[0] = int.class;
            Method m = bt.getClass().getMethod("createInsecureRfcommSocket", c);
            BluetoothSocket clientSocket = (BluetoothSocket) m.invoke(bt, 1);
            try {
                ba.cancelDiscovery();
                clientSocket.connect();
            } catch (IOException ex) {
                throw new Exception("No se pudo conectar con la impresora, verifique que ésta se encuentra encendida.");
            }
            Thread.sleep(500);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            boolean dividirImpresion = App.obtenerPreferencias_DividirImpresion(context);
            if (dividirImpresion) {

                String[] separacion = msg.split("\\n");
                int cantidadLineas = App.obtenerPreferencias_LineasPorEnvio(context);
                int pendiente = 0;
                StringBuilder sbImpresion = new StringBuilder();

                for (String linea : separacion) {
                    sbImpresion.append(linea).append("\n");
                    pendiente++;
                    if (pendiente >= cantidadLineas) {
                        out.write(sbImpresion.toString().getBytes()); //Aquí va el texto que se va a imprimir
                        out.flush();
                        pendiente = 0;
                        sbImpresion = new StringBuilder();
                    }
                }

                if (pendiente > 0) {
                    out.write(sbImpresion.toString().getBytes());
                }
            } else {
                out.write(msg.getBytes());
            }

            out.flush();
            Thread.sleep(500);
            out.close();
            clientSocket.close();
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new Exception("IllegalArgumentException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException: Verifique que la impresora " +
                    "se encuentra encendida" + (e.getMessage() != null ? (", " + e.getMessage()) : ""));
        } catch (Exception e) {
            throw new Exception("Error general: " + e.getMessage());
        }
    }

    /**
     * Comunicación encriptada
     * createRfcommSocketToServiceRecord(uuid: 00001101-0000-1000-8000-00805F9B34FB)
     * @param address MAC de impresora
     * @param msg     Message to be printed
     * @param context : Context
     * @throws Exception If cannot connect or message is empty
     */
    public static void Print3(String address, String msg, Context context) throws Exception {
        try {
            if (address == null || TextUtils.isEmpty(address))
                throw new Exception("Debe especificar la dirección de la impresora.");

            if (msg == null || TextUtils.isEmpty(msg))
                throw new Exception("Debe especificar el texto a imprimir.");

            BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bt = ba.getRemoteDevice(address);
            UUID uid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            BluetoothSocket clientSocket = bt.createRfcommSocketToServiceRecord(uid);
            //BluetoothSocket clientSocket = bt.createInsecureRfcommSocketToServiceRecord(uid);
            try{
                ba.cancelDiscovery();
                clientSocket.connect();
            } catch (IOException ex) {
                throw new Exception("No se pudo conectar con la impresora, verifique que ésta se encuentra encendida.");
            }

            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            boolean dividirImpresion = App.obtenerPreferencias_DividirImpresion(context);
            if (dividirImpresion) {

                String[] separacion = msg.split("\\n");
                int cantidadLineas = App.obtenerPreferencias_LineasPorEnvio(context);
                int pendiente = 0;
                StringBuilder sbImpresion = new StringBuilder();

                for (String linea : separacion) {
                    sbImpresion.append(linea).append("\n");
                    pendiente++;
                    if (pendiente >= cantidadLineas) {
                        out.write(sbImpresion.toString().getBytes()); //Aquí va el texto que se va a imprimir
                        out.flush();
                        pendiente = 0;
                        sbImpresion = new StringBuilder();
                    }
                }

                if (pendiente > 0) {
                    out.write(sbImpresion.toString().getBytes());
                }
            } else {
                out.write(msg.getBytes());
            }

            out.flush();
            out.close();
            clientSocket.close();
            SystemClock.sleep(1000);
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("IOException: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new Exception("IllegalArgumentException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException: Verifique que la impresora " +
                    "se encuentra encendida" + (e.getMessage() != null ? (", " + e.getMessage()) : ""));
        } catch (Exception e) {
            throw new Exception("Error general: " + e.getMessage());
        }
    }

    public static void PrintWithService(String adress, String msg, Context context){
        Intent intent = new Intent(context, BtService.class);
        intent.putExtra(PrintUtil.PRINT_EXTRA, msg.getBytes());
        intent.putExtra(PrintUtil.MAC_EXTRA, adress);
        intent.setAction(PrintUtil.ACTION_PRINT);
        context.startService(intent);
    }

}
