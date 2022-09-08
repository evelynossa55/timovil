package com.cm.timovil2.backup;

import android.os.Environment;
import android.util.JsonWriter;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.RemisionDAL;
import com.cm.timovil2.dto.DetalleRemisionDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.SincroHelper;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 28/06/18.
 */

@SuppressWarnings("unchecked")
public class RemisionBackUp implements IBackUp {

    private ActivityBase context;

    public RemisionBackUp(ActivityBase context){
        this.context = context;
    }

    @Override
    public void makeBackUp() throws Exception {
        File remisionesFile = new File(Environment.getExternalStorageDirectory(), Utilities.REMISIONES_BACKUP_JSON);
        String jsonRemisiones = getJsonFromFile(remisionesFile);
        Utilities.writeToFile(remisionesFile, jsonRemisiones);
    }

    @Override
    public <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception {
        File remisionesFile = new File(Environment.getExternalStorageDirectory(), Utilities.REMISIONES_BACKUP_JSON);
        ArrayList<RemisionDTO> remisionesPendientes = getOnlyPendentEntries((ArrayList<RemisionDTO>)data);
        String jsonRemisionesPendientes = createJsonFromEntries(remisionesPendientes);
        Utilities.writeToFile(remisionesFile, jsonRemisionesPendientes);
    }

    @Override
    public String getJsonFromFile(File file) throws Exception {
        RemisionDAL remDal = new RemisionDAL(context);
        DateTime now = DateTime.now();

        ArrayList<RemisionDTO> remisiones = remDal.obtenerListado(true, now, now);
        ArrayList<RemisionDTO> remisionesAntesGuardadas = getLastBackUp(file);

        remisiones = deleteRepeatedEntriesInBackUpList(remisiones, remisionesAntesGuardadas);
        if(remisiones != null && remisiones.size() > 0) remisiones = getOnlyPendentEntries(remisiones);

        return createJsonFromEntries(remisiones);
    }

    @Override
    public <T> ArrayList<T> getLastBackUp(File file) throws Exception {
        ArrayList<RemisionDTO> remisionesAntesGuardadas = null;
        if(file.exists()){
            String remisionesData = Utilities.readFromFile(file);
            remisionesAntesGuardadas = SincroHelper.procesarJsonRemisionesBackUp(remisionesData);
        }
        return (ArrayList<T>) remisionesAntesGuardadas;
    }

    @Override
    public <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception {
        if(sqliteEntries == null || sqliteEntries.size() == 0 ) return backUpEntries;
        if(backUpEntries == null || backUpEntries.size() == 0 ) return sqliteEntries;

        ArrayList<T> toRemove = new ArrayList<>();
        for (RemisionDTO remLinq : (ArrayList<RemisionDTO>)sqliteEntries){
            for(RemisionDTO remBackUp : (ArrayList<RemisionDTO>)backUpEntries){
                if(remBackUp.NumeroRemision.equals(remLinq.NumeroRemision)){
                    T remisionAeliminar = (T) remBackUp;
                    toRemove.add(remisionAeliminar);
                }
            }
        }

        backUpEntries.removeAll(toRemove);
        ArrayList<RemisionDTO> remisionesSinRepetir = new ArrayList<>((ArrayList<RemisionDTO>)sqliteEntries);
        if(backUpEntries.size() > 0) remisionesSinRepetir.addAll((ArrayList<RemisionDTO>)backUpEntries);

        return (ArrayList<T>) remisionesSinRepetir;
    }

    @Override
    public <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception {
        ArrayList<RemisionDTO> remisionesPendientes = new ArrayList<>();
        if(entries == null || entries.size() == 0) return (ArrayList<T>) remisionesPendientes;

        for (RemisionDTO remision : (ArrayList<RemisionDTO>)entries){
            if(!remision.Sincronizada){
                remisionesPendientes.add(remision);
            }
        }

        return (ArrayList<T>) remisionesPendientes;
    }

    @Override
    public <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writeRemisionesArray(writer, (ArrayList<RemisionDTO>)entries);
        writer.close();
        return stringWriter.toString();
    }

    private void writeRemisionesArray(JsonWriter writer, List<RemisionDTO> remisiones) throws IOException {
        writer.beginArray();
        for (RemisionDTO rem : remisiones) {
            writeRemision(writer, rem);
        }
        writer.endArray();
    }

    private void writeRemision(JsonWriter writer, RemisionDTO remision) throws IOException {
        writer.beginObject();
        writer.name("NumeroRemision").value(remision.NumeroRemision);
        writer.name("Fecha").value(remision.Fecha);
        writer.name("IdCliente").value(remision.IdCliente);
        writer.name("CodigoRuta").value(remision.CodigoRuta);
        writer.name("NombreRuta").value(remision.NombreRuta);
        writer.name("Subtotal").value(remision.Subtotal);
        writer.name("Iva").value(remision.Iva);
        writer.name("Total").value(remision.Total);
        writer.name("RazonSocialCliente").value(remision.RazonSocialCliente);
        writer.name("IdentificacionCliente").value(remision.IdentificacionCliente);
        writer.name("TelefonoCliente").value(remision.TelefonoCliente);
        writer.name("DireccionCliente").value(remision.DireccionCliente);
        writer.name("Anulada").value((remision.Anulada ? 1 : 0));
        writer.name("FechaCreacion").value(remision.FechaCreacion);
        writer.name("Latitud").value(remision.Latitud);
        writer.name("Longitud").value(remision.Longitud);
        writer.name("PendienteAnulacion").value(remision.PendienteAnulacion ? 1 : 0);
        writer.name("Comentario").value(remision.Comentario);
        writer.name("Sincronizada").value(remision.Sincronizada);
        writer.name("codigoBodega").value(remision.CodigoBodega);
        writer.name("NumeroPedido").value(remision.NumeroPedido);
        writer.name("ComentarioAnulacion").value(remision.ComentarioAnulacion);
        writer.name("Descuento").value(remision.Descuento);
        writer.name("FormaPago").value(remision.FormaPago);
        writer.name("IsPedidoCallcenter").value(remision.IsPedidoCallcenter);
        writer.name("Devolucion").value(remision.Devolucion);
        writer.name("Rotacion").value(remision.Rotacion);
        writer.name("IdPedido").value(remision.IdPedido);
        writer.name("IdCaso").value(remision.IdCaso);
        writer.name("ValorDevolucion").value(remision.ValorDevolucion);
        writer.name("Ipoconsumo").value(remision.Ipoconsumo);


        if (remision.DetalleRemision != null) {
            writer.name("DetalleRemision");
            writeDetalleRemisionArray(writer, remision.DetalleRemision);
        } else {
            writer.name("DetalleRemision").nullValue();
        }
        writer.endObject();
    }

    private void writeDetalleRemisionArray(JsonWriter writer, List<DetalleRemisionDTO> detalle) throws IOException {
        writer.beginArray();
        for (DetalleRemisionDTO value : detalle) {
            writeDetalleRemision(writer, value);
        }
        writer.endArray();
    }


    private void writeDetalleRemision(JsonWriter writer, DetalleRemisionDTO detalle) throws IOException {
        writer.beginObject();
        writer.name("NumeroRemision").value(detalle.NumeroRemision);
        writer.name("IdProducto").value(detalle.IdProducto);
        writer.name("NombreProducto").value(detalle.NombreProducto);
        writer.name("Cantidad").value(detalle.Cantidad);
        writer.name("ValorUnitario").value(detalle.ValorUnitario);
        writer.name("Subtotal").value(detalle.Subtotal);
        writer.name("Total").value(detalle.Total);
        writer.name("Iva").value(detalle.Iva);
        writer.name("FechaCreacion").value(detalle.FechaCreacion);
        writer.name("PorcentajeIva").value(detalle.PorcentajeIva);
        writer.name("Codigo").value(detalle.Codigo);
        writer.name("Descuento").value(detalle.Descuento);
        writer.name("PorcentajeDescuento").value(detalle.PorcentajeDescuento);
        writer.name("Devolucion").value(detalle.Devolucion);
        writer.name("Rotacion").value(detalle.Rotacion);
        writer.name("ValorDevolucion").value(detalle.ValorDevolucion);
        writer.name("Ipoconsumo").value(detalle.Ipoconsumo);
        writer.endObject();
    }

    private static ArrayList<RemisionDTO> obtenerRemisionesJsonBackUp() {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), Utilities.REMISIONES_BACKUP_JSON);
            FileInputStream in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            return SincroHelper.procesarJsonRemisionesBackUp(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void syncBackup() {
        try {
            int cantParaSincronizar = 0;
            int cantSincronizada = 0;
            RemisionDAL remDal = new RemisionDAL(context);
            ArrayList<RemisionDTO> remisiones = RemisionBackUp.obtenerRemisionesJsonBackUp();
            if (remisiones == null) return;

            for (RemisionDTO rem : remisiones) {
                RemisionDTO remSqlite = remDal.obtenerPorNumeroFac(rem.NumeroRemision);

                rem.Sincronizada = remSqlite != null ? remSqlite.Sincronizada : rem.Sincronizada;

                if (!rem.Sincronizada) {
                    cantParaSincronizar++;
                    try {
                        //La Rremisión no está guardada en el cel
                        rem.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_BACKUP;
                        String resultado = remDal.sincronizarRemision(rem);
                        if (resultado != null && resultado.equalsIgnoreCase("OK")) {
                            cantSincronizada++;
                            rem.Sincronizada = true;
                        }
                    } catch (Exception ex) {
                        App.SincronizandoRemisionNumero.remove(rem.NumeroRemision);
                        App.SincronizandoRemision = App.SincronizandoRemisionNumero.size() > 0;
                    }
                }
            }

            if (cantParaSincronizar == cantSincronizada) {
                Utilities.eliminarArchivo(Utilities.REMISIONES_BACKUP_JSON);
            }else{
                makeBackUpAfterSync(remisiones);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
