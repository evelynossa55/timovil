package com.cm.timovil2.backup;

import android.os.Environment;
import android.util.JsonWriter;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.NotaCreditoFacturaDAL;
import com.cm.timovil2.dto.DetalleNotaCreditoFacturaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 28/06/18.
 */

@SuppressWarnings("unchecked")
public class NotaCreditoFacturaPorDevolucionBackUp implements IBackUp {

    private ActivityBase context;

    public NotaCreditoFacturaPorDevolucionBackUp(ActivityBase context){
        this.context = context;
    }

    @Override
    public void makeBackUp() throws Exception {
        File notasCreditoDevolucionFile = new File(Environment.getExternalStorageDirectory(), Utilities.NOTACREDITOFACTURA_DEVOLUCION_BACKUP_JSON);

        String jsonNotasCreditoPorDevolucion = getJsonFromFile(notasCreditoDevolucionFile);
        Utilities.writeToFile(notasCreditoDevolucionFile, jsonNotasCreditoPorDevolucion);
    }

    @Override
    public <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception {
        File notasCreditoDevolucionFile = new File(Environment.getExternalStorageDirectory(), Utilities.NOTACREDITOFACTURA_DEVOLUCION_BACKUP_JSON);
        ArrayList<NotaCreditoFacturaDTO> notasPendientes = getOnlyPendentEntries((ArrayList<NotaCreditoFacturaDTO>)data);
        String jsonNotasPendientes = createJsonFromEntries(notasPendientes);
        Utilities.writeToFile(notasCreditoDevolucionFile, jsonNotasPendientes);
    }

    @Override
    public String getJsonFromFile(File file) throws Exception {
        DateTime now = DateTime.now();
        NotaCreditoFacturaDAL notaCreditoFacturaDAL = new NotaCreditoFacturaDAL(context);

        ArrayList<NotaCreditoFacturaDTO> notas = notaCreditoFacturaDAL.obtenerListado(true, now, now);
        ArrayList<NotaCreditoFacturaDTO> notasAntesGuardadas = getLastBackUp(file);

        notas = deleteRepeatedEntriesInBackUpList(notas, notasAntesGuardadas);
        if(notas != null && notas.size() > 0) notas = getOnlyPendentEntries(notas);

        return createJsonFromEntries(notas);
    }

    @Override
    public <T> ArrayList<T> getLastBackUp(File file) throws Exception {
        ArrayList<NotaCreditoFacturaDTO> notasAntesGuardadas = null;
        if (file.exists()) {
            String notasData = Utilities.readFromFile(file);
            notasAntesGuardadas = SincroHelper.procesarJsonNotasCreditoFacturaDevolucionBackUp(notasData);
        }
        return (ArrayList<T>) notasAntesGuardadas;
    }

    @Override
    public <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception {
        if(sqliteEntries == null || sqliteEntries.size() == 0 ) return backUpEntries;
        if(backUpEntries == null || backUpEntries.size() == 0 ) return sqliteEntries;

        ArrayList<T> toRemove = new ArrayList<>();
        for (NotaCreditoFacturaDTO notaLinq : (ArrayList<NotaCreditoFacturaDTO>)sqliteEntries){
            for(NotaCreditoFacturaDTO notaBackUp : (ArrayList<NotaCreditoFacturaDTO>)backUpEntries){
                if(notaBackUp.NumeroDocumento.equals(notaLinq.NumeroDocumento)){
                    T notaAeliminar = (T) notaBackUp;
                    toRemove.add(notaAeliminar);
                }
            }
        }

        backUpEntries.removeAll(toRemove);
        ArrayList<NotaCreditoFacturaDTO> notasSinRepetir = new ArrayList<>((ArrayList<NotaCreditoFacturaDTO>)sqliteEntries);
        if(backUpEntries.size() > 0) notasSinRepetir.addAll((ArrayList<NotaCreditoFacturaDTO>)backUpEntries);

        return (ArrayList<T>)notasSinRepetir;
    }

    @Override
    public <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception {
        ArrayList<NotaCreditoFacturaDTO> notasPendientes = new ArrayList<>();
        if(entries == null || entries.size() == 0 ) return (ArrayList<T>) notasPendientes;

        for (NotaCreditoFacturaDTO nota : (ArrayList<NotaCreditoFacturaDTO>) entries){
            if(!nota.Sincronizada){
                notasPendientes.add(nota);
            }
        }
        return (ArrayList<T>) notasPendientes;
    }

    @Override
    public <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writeNotasArray(writer, (ArrayList<NotaCreditoFacturaDTO>) entries);
        writer.close();
        return stringWriter.toString();
    }

    private void writeNotasArray(JsonWriter writer, List<NotaCreditoFacturaDTO> notas) throws IOException {
        writer.beginArray();
        if(notas != null && notas.size() > 0 ){
            for (NotaCreditoFacturaDTO nota : notas) {
                writeNota(writer, nota);
            }
        }
        writer.endArray();
    }

    private void writeNota(JsonWriter writer, NotaCreditoFacturaDTO nota) throws IOException {
        writer.beginObject();

        writer.name("IdNotaCreditoFactura").value(nota.IdNotaCreditoFactura);
        writer.name("NumeroDocumento").value(nota.NumeroDocumento);
        writer.name("NumeroFactura").value(nota.NumeroFactura);
        writer.name("Fecha").value(nota.Fecha);
        writer.name("Subtotal").value(nota.Subtotal);
        writer.name("Descuento").value(nota.Descuento);
        writer.name("Ipoconsumo").value(nota.Ipoconsumo);
        writer.name("Iva5").value(nota.Iva5);
        writer.name("Iva19").value(nota.Iva19);
        writer.name("Iva").value(nota.Iva);
        writer.name("Valor").value(nota.Valor);
        writer.name("CodigoBodega").value(nota.CodigoBodega);
        writer.name("Sincronizada").value(nota.Sincronizada);
        writer.name("Impresa").value(nota.Impresa);

        if (nota.DetalleNotaCreditoFactura != null) {
            writer.name("DetalleNotaCreditoFactura");
            writeDetalleNotaCreditoArray(writer, nota.DetalleNotaCreditoFactura);
        } else {
            writer.name("DetalleNotaCreditoFactura").nullValue();
        }
        writer.endObject();
    }

    private void writeDetalleNotaCreditoArray(JsonWriter writer, List<DetalleNotaCreditoFacturaDTO> detalle) throws IOException {
        writer.beginArray();
        for (DetalleNotaCreditoFacturaDTO value : detalle) {
            writeDetalleFactura(writer, value);
        }
        writer.endArray();
    }

    private void writeDetalleFactura(JsonWriter writer, DetalleNotaCreditoFacturaDTO detalle) throws IOException {
        writer.beginObject();
        writer.name("IdDetalleNotaCreditoFactura").value(detalle.IdDetalleNotaCreditoFactura);
        writer.name("NumeroDocumento").value(detalle.NumeroDocumento);
        writer.name("IdProducto").value(detalle.IdProducto);
        writer.name("Cantidad").value(detalle.Cantidad);
        writer.name("Subtotal").value(detalle.Subtotal);
        writer.name("Descuento").value(detalle.Descuento);
        writer.name("Ipoconsumo").value(detalle.Ipoconsumo);
        writer.name("Iva5").value(detalle.Iva5);
        writer.name("Iva19").value(detalle.Iva19);
        writer.name("Iva").value(detalle.Iva);
        writer.name("Valor").value(detalle.Valor);
        writer.name("Codigo").value(detalle.Codigo);
        writer.name("Nombre").value(detalle.Nombre);
        writer.endObject();
    }

    private static ArrayList<NotaCreditoFacturaDTO> obtenerNotasCreditoFacturaDevolucionJsonBackUp(){
        try{
            File file = new File(Environment.getExternalStorageDirectory(), Utilities.NOTACREDITOFACTURA_DEVOLUCION_BACKUP_JSON);
            String data = Utilities.readFromFile(file);
            return SincroHelper.procesarJsonNotasCreditoFacturaDevolucionBackUp(data);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void syncBackup() {
        try {
            int cantParaSincronizar = 0;
            int cantSincronizada = 0;
            NotaCreditoFacturaDAL notaCreditoFacturaDAL = new NotaCreditoFacturaDAL(context);
            ArrayList<NotaCreditoFacturaDTO> notas = NotaCreditoFacturaPorDevolucionBackUp.obtenerNotasCreditoFacturaDevolucionJsonBackUp();
            if (notas == null) return;

            for (NotaCreditoFacturaDTO nota : notas) {
                NotaCreditoFacturaDTO notaSqlite = notaCreditoFacturaDAL.obtenerPorNumeroDocumento(nota.NumeroDocumento);

                nota.Sincronizada = notaSqlite != null ? notaSqlite.Sincronizada : nota.Sincronizada;

                if (!nota.Sincronizada) {
                    cantParaSincronizar++;
                    try {
                        //La Nota Crédito no está guardada en el cel
                        nota.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_BACKUP;
                        String resultado = notaCreditoFacturaDAL.sincronizarNotaCreditoFactura(nota);
                        if (resultado != null && resultado.equalsIgnoreCase("OK")) {
                            cantSincronizada++;
                            nota.Sincronizada = true;
                        }
                    } catch (Exception ex) {
                        App.SincronizandoNotaCreditoNumero.remove(nota.NumeroDocumento);
                        App.SincronizandoNotaCredito = App.SincronizandoNotaCreditoNumero.size() > 0;
                    }
                }
            }

            if (cantParaSincronizar == cantSincronizada) {
                Utilities.eliminarArchivo(Utilities.NOTACREDITOFACTURA_DEVOLUCION_BACKUP_JSON);
            }else{
                makeBackUpAfterSync(notas);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
