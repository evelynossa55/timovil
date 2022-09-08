package com.cm.timovil2.backup;

import android.os.Environment;
import android.util.JsonWriter;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.GuardarMotivoNoVentaDAL;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.SincroHelper;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 28/06/18.
 */

@SuppressWarnings("unchecked")
public class NoVentaBackUp implements IBackUp {

    private ActivityBase context;

    public NoVentaBackUp(ActivityBase context){
        this.context = context;
    }

    @Override
    public void makeBackUp() throws Exception {
        File noVentasFile  = new File(Environment.getExternalStorageDirectory(), Utilities.NO_VENTAS_BACKUP_JSON);

        String jsonNoVentas = getJsonFromFile(noVentasFile);
        Utilities.writeToFile(noVentasFile, jsonNoVentas);
    }

    @Override
    public <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception {
        File noVentasFile = new File(Environment.getExternalStorageDirectory(), Utilities.NO_VENTAS_BACKUP_JSON);
        ArrayList<GuardarMotivoNoVentaDTO> noVentasPendientes = getOnlyPendentEntries((ArrayList<GuardarMotivoNoVentaDTO>)data);
        String jsonNoVentasPendientes = createJsonFromEntries(noVentasPendientes);
        Utilities.writeToFile(noVentasFile, jsonNoVentasPendientes);
    }

    @Override
    public String getJsonFromFile(File file) throws Exception {
        GuardarMotivoNoVentaDAL guardarMotivoNoVentaDAL = new GuardarMotivoNoVentaDAL(context);

        ArrayList<GuardarMotivoNoVentaDTO> noVentas = guardarMotivoNoVentaDAL.ObtenerListado();
        ArrayList<GuardarMotivoNoVentaDTO> noVentasAntesGuardadas = getLastBackUp(file);

        noVentas = deleteRepeatedEntriesInBackUpList(noVentas, noVentasAntesGuardadas);
        if(noVentas != null && noVentas.size() > 0) noVentas = getOnlyPendentEntries(noVentas);

        return createJsonFromEntries(noVentas);
    }

    @Override
    public <T> ArrayList<T> getLastBackUp(File file) throws Exception {
        ArrayList<GuardarMotivoNoVentaDTO> noVentasAntesGuardadas = null;
        if(file.exists()){
            String noVentasData = Utilities.readFromFile(file);
            noVentasAntesGuardadas = SincroHelper.procesarJsonNoVentasBackUp(noVentasData);
        }
        return (ArrayList<T>) noVentasAntesGuardadas;
    }

    @Override
    public <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception {
        if(sqliteEntries == null || sqliteEntries.size() == 0 ) return backUpEntries;
        if(backUpEntries == null || backUpEntries.size() == 0 ) return sqliteEntries;

        ArrayList<T> toRemove = new ArrayList<>();
        for (GuardarMotivoNoVentaDTO noVentaLinq : (ArrayList<GuardarMotivoNoVentaDTO>)sqliteEntries){
            for(GuardarMotivoNoVentaDTO noVentaBackUp : (ArrayList<GuardarMotivoNoVentaDTO>)backUpEntries){
                if(noVentaBackUp.IdMotivoNoVenta == noVentaLinq.IdMotivoNoVenta){
                    T noVentaAeliminar = (T) noVentaBackUp;
                    toRemove.add(noVentaAeliminar);
                }
            }
        }

        backUpEntries.removeAll(toRemove);
        ArrayList<GuardarMotivoNoVentaDTO> noVentasSinRepetir = new ArrayList<>((ArrayList<GuardarMotivoNoVentaDTO>)sqliteEntries);
        if(backUpEntries.size() > 0) noVentasSinRepetir.addAll((ArrayList<GuardarMotivoNoVentaDTO>)backUpEntries);

        return (ArrayList<T>)noVentasSinRepetir;
    }

    @Override
    public <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception {
        ArrayList<GuardarMotivoNoVentaDTO> noVentasPendientes = new ArrayList<>();
        if(entries == null || entries.size() == 0 ) return (ArrayList<T>)noVentasPendientes;

        for (GuardarMotivoNoVentaDTO noVenta : (ArrayList<GuardarMotivoNoVentaDTO>)entries){
            if(!noVenta.Sincronizada){
                noVentasPendientes.add(noVenta);
            }
        }

        return (ArrayList<T>)noVentasPendientes;
    }

    @Override
    public <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writeNoVentasArray(writer, (List<GuardarMotivoNoVentaDTO>)entries);
        writer.close();
        return stringWriter.toString();
    }

    private void writeNoVentasArray(JsonWriter writer, List<GuardarMotivoNoVentaDTO> noVentas) throws IOException {
        writer.beginArray();
        if(noVentas != null && noVentas.size() > 0 ){
            for (GuardarMotivoNoVentaDTO noVenta : noVentas) {
                writeNoVenta(writer, noVenta);
            }
        }
        writer.endArray();
    }

    private void writeNoVenta(JsonWriter writer, GuardarMotivoNoVentaDTO noVenta) throws IOException {
        writer.beginObject();

        writer.name("IdMotivoNoVenta").value(noVenta.IdMotivoNoVenta);
        writer.name("IdMotivo").value(noVenta.IdMotivo);
        writer.name("IdCliente").value(noVenta.IdCliente);
        writer.name("CodigoRuta").value(noVenta.CodigoRuta);
        writer.name("Descripcion").value(noVenta.Descripcion);
        writer.name("Fecha").value(noVenta.Fecha);
        writer.name("Fecha_long").value(noVenta.Fecha_long);
        writer.name("Latitud").value(noVenta.Latitud);
        writer.name("Longitud").value(noVenta.Longitud);
        writer.name("IdClienteTimovil").value(noVenta.IdClienteTimovil);
        writer.name("esOtroMotivo").value(noVenta.esOtroMotivo);
        writer.name("Motivo").value(noVenta.Motivo);
        writer.name("Sincronizada").value(noVenta.Sincronizada);
        writer.endObject();
    }

    private static ArrayList<GuardarMotivoNoVentaDTO> obtenerNoVentasJsonBackUp(){
        try{
            File file = new File(Environment.getExternalStorageDirectory(), Utilities.NO_VENTAS_BACKUP_JSON);
            String data = Utilities.readFromFile(file);
            return SincroHelper.procesarJsonNoVentasBackUp(data);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void syncBackup() {
        try{
            int cantParaSincronizar = 0;
            int cantSincronizada = 0;
            GuardarMotivoNoVentaDAL guardarMotivoNoVentaDAL = new GuardarMotivoNoVentaDAL(context);
            ArrayList<GuardarMotivoNoVentaDTO> noVentas = NoVentaBackUp.obtenerNoVentasJsonBackUp();

            if(noVentas == null) return;

            for(GuardarMotivoNoVentaDTO noVenta: noVentas){
                GuardarMotivoNoVentaDTO noVentaSqlite = guardarMotivoNoVentaDAL.ObtenerMotivoNoVentaDto(noVenta.IdMotivoNoVenta);

                noVenta.Sincronizada = noVentaSqlite != null ? noVentaSqlite.Sincronizada : noVenta.Sincronizada;

                if (!noVenta.Sincronizada) {
                    cantParaSincronizar++;
                    try {
                        //La Nota Crédito no está guardada en el cel
                        noVenta.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_BACKUP;
                        String resultado = guardarMotivoNoVentaDAL.sincronizarPendiente(noVenta);
                        if (resultado != null && resultado.equalsIgnoreCase("OK")) {
                            cantSincronizada++;
                            noVenta.Sincronizada = true;
                        }
                    } catch (Exception ex) {
                        App.SincronizandoNoVentaIdMotivoNoVenta.remove(Integer.valueOf(noVenta.IdMotivoNoVenta));
                        App.SincronizandoNoVenta = App.SincronizandoNoVentaIdMotivoNoVenta.size() > 0;
                    }
                }
            }

            if(cantParaSincronizar == cantSincronizada){
                Utilities.eliminarArchivo(Utilities.NO_VENTAS_BACKUP_JSON);
            }else{
                makeBackUpAfterSync(noVentas);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
