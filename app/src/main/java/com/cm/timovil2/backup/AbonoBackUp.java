package com.cm.timovil2.backup;

import android.os.Environment;
import android.util.JsonWriter;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.dto.wsentities.MAbono;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.SincroHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CREADO POR  JORGE ANDRÉS DAVID CARDONA EL 29/06/18.
 */

@SuppressWarnings("unchecked")
public class AbonoBackUp implements IBackUp {

    private ActivityBase context;

    public AbonoBackUp(ActivityBase context){
        this.context = context;
    }

    @Override
    public void makeBackUp() throws Exception {
        File abonosFile = new File(Environment.getExternalStorageDirectory(), Utilities.ABONOS_BACKUP_JSON);

        String jsonAbonos = getJsonFromFile(abonosFile);
        Utilities.writeToFile(abonosFile, jsonAbonos);
    }

    @Override
    public <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception {
        File abonosFile = new File(Environment.getExternalStorageDirectory(), Utilities.ABONOS_BACKUP_JSON);
        ArrayList<MAbono> abonosPendientes = getOnlyPendentEntries((ArrayList<MAbono>)data);
        String jsonAbonosPendientes = createJsonFromEntries(abonosPendientes);
        Utilities.writeToFile(abonosFile, jsonAbonosPendientes);
    }

    @Override
    public String getJsonFromFile(File file) throws Exception {
        AbonoFacturaDAL abonoDAL = new AbonoFacturaDAL(context);

        ArrayList<MAbono> abonos = abonoDAL.obtenerListado();
        ArrayList<MAbono> abonosAntesGuardados = getLastBackUp(file);

        abonos = deleteRepeatedEntriesInBackUpList(abonos, abonosAntesGuardados);
        if(abonos != null && abonos.size() > 0) abonos = getOnlyPendentEntries(abonos);

        return createJsonFromEntries(abonos);
    }

    @Override
    public <T> ArrayList<T> getLastBackUp(File file) throws Exception {
        ArrayList<MAbono> abonosAntesGuardados = null;
        if (file.exists()) {
            String abonosData = Utilities.readFromFile(file);
            abonosAntesGuardados = SincroHelper.procesarJsonAbonosBackUp(abonosData);
        }
        return (ArrayList<T>) abonosAntesGuardados;
    }

    @Override
    public <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception {
        if(sqliteEntries == null || sqliteEntries.size() == 0 ) return backUpEntries;
        if(backUpEntries == null || backUpEntries.size() == 0 ) return sqliteEntries;

        ArrayList<T> toRemove = new ArrayList<>();
        for (MAbono aboLinq : (ArrayList<MAbono>)sqliteEntries){
            for(MAbono aboBackUp : (ArrayList<MAbono>)backUpEntries){
                if(aboBackUp._Id == aboLinq._Id){
                    T abonoToRemove = (T) aboBackUp;
                    toRemove.add(abonoToRemove);
                }
            }
        }

        backUpEntries.removeAll(toRemove);
        ArrayList<MAbono> abonosSinRepetir = new ArrayList<>((ArrayList<MAbono>)sqliteEntries);
        if(backUpEntries.size() > 0) abonosSinRepetir.addAll((ArrayList<MAbono>)backUpEntries);

        return (ArrayList<T>) abonosSinRepetir;
    }

    @Override
    public <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception {
        ArrayList<MAbono> abonosPendientes = new ArrayList<>();
        if(entries == null || entries.size() == 0 ) return (ArrayList<T>)abonosPendientes;

        for (MAbono abono : (ArrayList<MAbono>) entries){
            if(!abono.Sincronizado){
                abonosPendientes.add(abono);
            }
        }

        return (ArrayList<T>) abonosPendientes;
    }

    @Override
    public <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        ArrayList<MAbono> abonos = (ArrayList<MAbono>) entries;
        writeAbonosArray(writer, abonos);
        writer.close();
        return stringWriter.toString();
    }

    private void writeAbonosArray(JsonWriter writer, List<MAbono> abonos) throws IOException {
        writer.beginArray();
        if(abonos != null && abonos.size() > 0){
            for (MAbono abono : abonos) {
                writeAbono(writer, abono);
            }
        }
        writer.endArray();
    }

    private void writeAbono(JsonWriter writer, MAbono abono) throws IOException {
        writer.beginObject();
        writer.name("_Id").value(abono._Id);
        writer.name("Identificador").value(abono.Identificador);
        writer.name("IdFactura").value(abono.IdFactura);
        writer.name("NumeroFactura").value(abono.NumeroFactura);
        writer.name("Fecha").value(abono.Fecha);
        writer.name("Valor").value(abono.Valor);
        writer.name("Saldo").value(abono.Saldo);
        writer.name("Sincronizado").value(abono.Sincronizado);
        writer.name("DiaCreacion").value(abono.DiaCreacion);
        writer.name("IdCuentaCaja").value(abono.IdCuentaCaja);
        writer.name("FechaCreacion").value(abono.FechaCreacion);
        writer.endObject();
    }

    @Override
    public void syncBackup() {
        try{
            int cantParaSincronizar = 0;
            int cantSincronizada = 0;
            AbonoFacturaDAL abonoFacturaDAL = new AbonoFacturaDAL(context);
            ArrayList<MAbono> abonos = AbonoBackUp.obtenerAbonoFacturasJsonBackUp();

            if(abonos == null) return;

            for(MAbono abono: abonos){
                MAbono abonoSqlite = abonoFacturaDAL.obtenerAbono(abono._Id);

                abono.Sincronizado = abonoSqlite != null ? abonoSqlite.Sincronizado: abono.Sincronizado;

                if (!abono.Sincronizado) {
                    cantParaSincronizar++;
                    try {
                        //El abono no está guardado en el cel
                        abono.EnviadoDesde = Utilities.FACTURA_ENVIADA_DESDE_BACKUP;
                        String resultado = abonoFacturaDAL.sincronizarAbono(abono);
                        if (resultado != null && resultado.equalsIgnoreCase("OK")) {
                            cantSincronizada++;
                            abono.Sincronizado = true;
                        }
                    } catch (Exception ex) {
                        App.SincronizandoAbonoFacturaId.remove(Integer.valueOf(abono._Id));
                        App.SincronizandoAbonoFactura = App.SincronizandoAbonoFacturaId.size() > 0;
                    }
                }
            }

            if(cantParaSincronizar == cantSincronizada){
                Utilities.eliminarArchivo(Utilities.ABONOS_BACKUP_JSON);
            }else{
                //Eliminamos del backup los sincronizados, y generamos un nuevo con los pendientes
                makeBackUpAfterSync(abonos);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static ArrayList<MAbono> obtenerAbonoFacturasJsonBackUp(){
        try{
            File file = new File(Environment.getExternalStorageDirectory(), Utilities.ABONOS_BACKUP_JSON);
            FileInputStream in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            return SincroHelper.procesarJsonAbonosBackUp(sb.toString());
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
