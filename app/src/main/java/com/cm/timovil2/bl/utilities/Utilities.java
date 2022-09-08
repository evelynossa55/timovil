package com.cm.timovil2.bl.utilities;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import androidx.core.os.ConfigurationCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.front.ActivityBase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utilities {

    public final static String IMEI_ERROR = "IMEI_ERROR";

    public static String Fecha(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);

        return cal.get(Calendar.DAY_OF_MONTH) + "/"
                + (cal.get(Calendar.MONTH) + 1) + "/"
                + cal.get(Calendar.YEAR);
    }

    public static String Fecha(String fechaAnsi) {
        String year = fechaAnsi.substring(0, 4);
        String mes = fechaAnsi.substring(4, 6);
        String dia = fechaAnsi.substring(6, 8);

        return dia + "/" + mes + "/" + year + fechaAnsi.substring(8);
    }

    private static String HoraAnsi(Date fecha) throws Exception {


        try {
            String horaAnsiCompleta;
            String hora;
            String minuto;
            String segundo;

            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);

            int intHora, intMinuto, intSegundo;
            intHora = cal.get(Calendar.HOUR_OF_DAY);

            if (intHora < 10) {
                hora = "0" + intHora;
            } else {
                hora = String.valueOf(intHora);
            }

            intMinuto = cal.get(Calendar.MINUTE);
            if (intMinuto < 10) {
                minuto = "0" + intMinuto;
            } else {
                minuto = String.valueOf(intMinuto);
            }

            intSegundo = cal.get(Calendar.SECOND);
            if (intSegundo < 10) {
                segundo = "0" + intSegundo;
            } else {
                segundo = String.valueOf(intSegundo);
            }

            horaAnsiCompleta = hora + ":" + minuto + ":" + segundo;

            return horaAnsiCompleta;

        } catch (Exception ex) {
            throw new Exception("Error creando la hora ansi: "
                    + ex.getMessage());
        }
    }

    public static String FechaAnsi(Date fecha) throws Exception {
        try {
            String dia;
            String mes;
            String FechaCompleta;

            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);

            String year = String.valueOf(cal.get(Calendar.YEAR));

            int intDia = cal.get(Calendar.DAY_OF_MONTH);
            if (intDia < 10) {
                dia = "0" + intDia;
            } else {
                dia = String.valueOf(intDia);
            }

            int numeroMes = cal.get(Calendar.MONTH) + 1;
            if (numeroMes < 10) {
                mes = "0" + numeroMes;
            } else {
                mes = String.valueOf(numeroMes);
            }

            // Formato ANSI: YYYYMMDD
            FechaCompleta = year + mes + dia;

            return FechaCompleta;
        } catch (Exception ex) {
            throw new Exception("Error FechaAnsi: " + ex.getMessage());
        }
    }

    public static String FechaHoraAnsi(Date fecha) throws Exception {
        try {
            String _fechaAnsi = FechaAnsi(fecha);
            String _horaAnsi = HoraAnsi(fecha);

            return  _fechaAnsi + " " + _horaAnsi;
        } catch (Exception ex) {
            throw new Exception("Error FechaHoraAnsi: " + ex.getMessage());
        }
    }

    public static String FechaHoraAnsiJoda(DateTime fecha) throws Exception {
        try {
            int year = fecha.getYear();
            int month = fecha.getMonthOfYear();
            int day = fecha.getDayOfMonth();
            int hora = fecha.getHourOfDay();
            int minuto = fecha.getMinuteOfHour();
            int segundo = fecha.getSecondOfMinute();

            String dia;
            String mes;
            String FechaCompleta;


            String _year = String.valueOf(year);

            if (day < 10) {
                dia = "0" + day;
            } else {
                dia = String.valueOf(day);
            }

            if (month < 10) {
                mes = "0" + month;
            } else {
                mes = String.valueOf(month);
            }

            FechaCompleta = _year + mes + dia;

            String horaAnsiCompleta;
            String _hora;
            String _minuto;
            String _segundo;


            if (hora < 10) {
                _hora = "0" + hora;
            } else {
                _hora = String.valueOf(hora);
            }

            if (minuto < 10) {
                _minuto = "0" + minuto;
            } else {
                _minuto = String.valueOf(minuto);
            }

            if (segundo < 10) {
                _segundo = "0" + segundo;
            } else {
                _segundo = String.valueOf(segundo);
            }

            horaAnsiCompleta = _hora + ":" + _minuto + ":" + _segundo;


            return  FechaCompleta + " " + horaAnsiCompleta;
        } catch (Exception ex) {
            throw new Exception("Error FechaHoraAnsi: " + ex.getMessage());
        }
    }

    public static  String FechaDetallada(Date date) {
        if (date != null) {
            String formatoFecha = "dd MMM yyyy hh:mm aaa";
            SimpleDateFormat sdf = new SimpleDateFormat(formatoFecha, Locale.getDefault());
            return sdf.format(date);
        }else{
            return null;
        }
    }

    public static String completarEspacios(String cadena, int tamanoEsperado) {
        try {
            int tamanoActual = cadena.length();
            int diferencia = tamanoEsperado - tamanoActual;
            if (diferencia != 0) {
                if (diferencia > 0) // Agrego los espacios que faltan para completar el tamaño esperado
                {
                    StringBuilder cadenaBuilder = new StringBuilder(cadena);
                    for (int i = 1; i <= diferencia; i++) cadenaBuilder.append(" ");
                    cadena = cadenaBuilder.toString();
                    // cadena = cadena + (new String(' ', diferencia));
                }
            }// Si la diferencia es igual a cero, no se hace nada
            return cadena;
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isNetworkReachable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = null;
        if(manager != null) current = manager.getActiveNetworkInfo();
        return (current != null && current.isConnectedOrConnecting());
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = null;
        if(manager != null) current = manager.getActiveNetworkInfo();
        return  (current != null && current.isConnected());
    }

    public static void makeDialog(Context context, String mensaje) {
        Builder d = new Builder(context);
        d.setTitle("Timovil");
        d.setMessage(mensaje);
        d.setCancelable(false);
        d.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
            }
        });
        d.show();
    }

    /**
     * Función que elimina acentos y caracteres especiales de
     * una cadena de texto.
     *
     * @param texto para ser limpiado
     * @return Cadena de texto limpia de acentos y caracteres especiales.
     */
    public static String LimpiarTexto(String texto) {
        // Cadena de caracteres original a sustituir.
        String original = "ééééééééééééééuééééééééééééééééééé";
        // Cadena de caracteres ASCII que reemplazarén los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = texto;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }

    public static String quitarTildes(String texto) {
        return texto
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");
    }

    public static String FormatoMoneda(Object cadena){
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
        return  f.format(cadena);
    }

    public static String FormatoPorcentaje(Object cadena){
        NumberFormat f = NumberFormat.getPercentInstance(Locale.US);
        return  f.format(cadena);
    }

    public static boolean fechaResolucionEsMayorA5julio2016(Context context){
        String fechaResolucionStr = App.obtenerConfiguracion_FechaDeResolucion(context);
        if(!TextUtils.isEmpty(fechaResolucionStr)){
            String[] fechaResolucionArray = fechaResolucionStr.split("\\.");
            int year = Integer.parseInt(fechaResolucionArray[0]);
            int month = Integer.parseInt(fechaResolucionArray[1]);
            int day = Integer.parseInt(fechaResolucionArray[2]);
            DateTime fechaResolucion = new DateTime(year, month, day, 0, 0);
            return fechaResolucion.isAfter(fecha_validacion_resolucion);
        }else{
            return false;
        }
    }

    public static boolean eliminarArchivo(String nombreArchivo){
        File file = new File(Environment.getExternalStorageDirectory(), nombreArchivo) ;
        return file.exists() && file.delete();
    }

    public static void writeToFile(File file, String data) throws Exception{
        Writer output = new BufferedWriter(new FileWriter(file));
        output.write(data);
        output.close();
    }

    public static String readFromFile(File file) throws Exception{
        FileInputStream in = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static String readFileAsBase64String(String path) {
        try {
            InputStream is = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
            byte[] buffer = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = is.read(buffer)) > -1) {
                    b64os.write(buffer, 0, bytesRead);
                }
                return baos.toString();
            } catch (IOException e) {
                return "";
            } finally {
                closeQuietly(is);
                closeQuietly(b64os); // This also closes baos
            }
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap TextToQRBitmap(ActivityBase context, String Value) throws WriterException {

        BitMatrix bitMatrix;
        int QRcodeWidth = 250;
        int QRcodeHeight = 250;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeHeight, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        Resources res = context.getResources();
        int black = res.getColor(R.color.black);
        int white = res.getColor(R.color.white);

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ? black : white;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 250, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    public static Locale getLocale(){
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    }

    public static double sDecimalesDouble(double valor) {
        DecimalFormat formato = new DecimalFormat("#.00");
        String nuevoFormato = formato.format(valor);
        double numero;
        try {
            return numero = Double.valueOf(nuevoFormato);
        } catch (NumberFormatException e) {
            Log.e("error", e.getMessage());
        }
        return 1.0;
    }

    //https://stackoverflow.com/a/2808648
    public static double dosDecimalesDouble(double valor, int decimales) {
        if (decimales < 0) throw new IllegalArgumentException();

        BigDecimal numeroDecimal = BigDecimal.valueOf(valor);
        numeroDecimal = numeroDecimal.setScale(decimales, RoundingMode.HALF_UP);
        return numeroDecimal.doubleValue();
    }

    //----------------------CLIENTES TIMOVIL--------------------------
    public final static String ID_LETICIA = "1027";
    public final static String ID_NATIPAN = "9182";
    public final static String ID_IGLU = "2304";
    public final static String ID_POLAR = "1912";
    public final static String ID_DOBLEVIA = "2101";
    public final static String ID_FERNANDO = "5109";
    public final static String ID_MATERIALES_Y_HERRAMIENTAS = "1308";
    public final static String ID_ALMIRANTE = "0110";
    public final static String ID_FR = "0228";
    public final static String ID_PANIFICADORA = "1209";
    public final static String ID_ESTEBAN_GALLEGO = "0220";
    public final static String ID_CESAR_GALLEGO = "0210";
    public final static String ID_VEGA = "2602";
    public final static String ID_PRUEBAS_TIMOVIL = "0503";
    public final static String ID_PRUEBAS_CMPRUEBAS = "1702";
    public final static String ID_JAF = "1705";
    public final static String ID_DEFRUTA = "2612";
    public final static String ID_SARY = "0113";
    public final static String ID_GUAYAZAN = "7133";
    public final static String ID_FOGON_PAISA = "1309";
    public final static String ID_FERNANDO_CARDONA = "5109";
    public final static String ID_PRUEBAS_IGLU = "0999";
    public final static String ID_DISTRIBUCIONES_WILL = "0117";
    public final static String ID_DISTRIBUIDOR_PAISAPAN = "0120";
    public final static String ID_COMESCOL = "0124";
    public final static String ID_HERNAN_MORA = "0126";
    //public final static String ID_AREPAS_DEL_FOGON = "0112";
    //public final static String ID_ANDERSON = "1124";
    //public final static String ID_F_VALENCIA = "2608";
    //public final static String ID_LONCHISEDA = "1908";

    //------------------------FECHAS-----------------------------------
    private final static DateTime fecha_validacion_resolucion = new DateTime(2016, 7, 5, 0, 0);

    //PATHS
    public final static String INSTALLATION_FILE = "timovil_installation";
    public final static String FACTURAS_BACKUP_JSON = "ghaxswilzqw.json";
    public final static String ABONOS_BACKUP_JSON = "ghaxswilzqt.json";
    public final static String REMISIONES_BACKUP_JSON = "jhdgstrbelks.json";
    public final static String NOTACREDITOFACTURA_DEVOLUCION_BACKUP_JSON = "asdefrgthy.json";
    public final static String NO_VENTAS_BACKUP_JSON = "dsasokgft.json";
    public final static String NO_VENTAS_PEDIDO_BACKUP_JSON = "dsasokgfp.json";

    public final static String FACTURA_ENVIADA_DESDE_MONITOR = "MONITOR";
    public final static String FACTURA_ENVIADA_DESDE_SINCRO = "SINCRONIZACION";
    public final static String FACTURA_ENVIADA_DESDE_FACTURACION = "FACTURACION";
    public final static String FACTURA_ENVIADA_DESDE_BACKUP = "BACKUP";
}