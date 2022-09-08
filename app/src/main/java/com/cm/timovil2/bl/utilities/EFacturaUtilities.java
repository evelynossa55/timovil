package com.cm.timovil2.bl.utilities;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.ResolucionDTO;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EFacturaUtilities {

    public static String getCufe(FacturaDTO factura, ClienteDTO cliente, ResolucionDTO resolucion) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        if(factura == null || cliente == null || resolucion == null) return "";
        //Ver Anexo técnico de factura electrónica de venta. Página 737
        //{NumFac}{FecFac}{HorFac}{ValFac}{CodImp1}{ValImp1}{CodImp2}{ValImp2}{CodImp3}{ValImp3}{ValTot}{NitFE}{NumAdq}{ClTec}{TipoAmbiente}
        String numFac = factura.NumeroFactura.replace("-", "");
        String fecFac = fecFac(factura.FechaHora);
        String horFac = horFac(factura.FechaHora);
        String valFac = moneyEFactura(factura.Subtotal);
        String codImp1 = "01";
        String valImp1 = moneyEFactura(factura.Iva);
        String codImp2 = "04";
        String valImp2 = moneyEFactura(factura.IpoConsumo);
        String codImp3 = "03";
        String valImp3 = moneyEFactura(factura.Ica);
        String valTot = moneyEFactura(factura.Total);
        String nitFE = resolucion.Nit.replace(".","").split("-")[0];
        String numAdq = cliente.Identificacion.replace("-", "").replace(".", "");
        String clTec = resolucion.ClaveTecnica;
        String ambient = resolucion.AmbienteEFactura;

        String cufe = numFac + fecFac + horFac + valFac + codImp1 + valImp1 + codImp2 + valImp2 +
                codImp3 + valImp3 + valTot + nitFE + numAdq + clTec + ambient;

        return SHA384(cufe);
    }

    public static String getQR(FacturaDTO factura, ClienteDTO cliente, ResolucionDTO resolucion){
        if(factura == null || cliente == null || resolucion == null) return "";
        String template = "NroFactura=%s NitFacturador=%s NitAdquiriente=%s FechaFactura=%s HoraFactura=%s ValorFactura=%s ValorIVA=%s ValorOtrosImpuestos=%s ValorTotalFactura=%s CUFE=%s";
        String numFac = factura.NumeroFactura.replace("-", "");
        String nitFE = resolucion.Nit.replace(".","").split("-")[0];
        String numAdq = cliente.Identificacion.replace("-", "").replace(".", "");
        String fecFac = fecFac(factura.FechaHora);
        String horFac = horFac(factura.FechaHora);
        String valFac = moneyEFactura(factura.Subtotal);
        String valIva = moneyEFactura(factura.Iva);
        String valOtros = moneyEFactura(factura.IpoConsumo + factura.Ica);
        String valTot = moneyEFactura(factura.Total);
        String cufe = "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=" + factura.Cufe;
        return String.format(template, numFac, nitFE, numAdq, fecFac, horFac, valFac, valIva, valOtros, valTot, cufe);
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static String SHA384(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private static String fecFac(long fecFac){
        Date d = new Date(fecFac);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        return cal.get(Calendar.YEAR) + "/" +
                (cal.get(Calendar.MONTH) + 1) + "/" +
                cal.get(Calendar.DAY_OF_MONTH);
    }

    private static String horFac(long fecFac){
        Date d = new Date(fecFac);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        return cal.get(Calendar.HOUR_OF_DAY) + ":" +
                cal.get(Calendar.MINUTE) + ":" +
                cal.get(Calendar.SECOND) +
                GMTOffset();
    }

    private static String GMTOffset(){
        Locale locale = Utilities.getLocale();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), locale);
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z", locale);
        return date.format(currentLocalTime);
    }

    private static String moneyEFactura(double val){
        return String.format(Utilities.getLocale(), "%.02f", val);
    }
}
