package com.cm.timovil2.backup;

import android.os.Environment;
import android.util.JsonWriter;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.GuardarMotivoNoVentaPedidoDAL;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.rest.SincroHelper;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 13/07/18.
 */

@SuppressWarnings("unchecked")
public class NoVentaPedidoBackUp implements IBackUp {

    private ActivityBase context;

    public NoVentaPedidoBackUp(ActivityBase context){this.context = context;}

    @Override
    public void makeBackUp() throws Exception {
        File noVentasPedidosFile  = new File(Environment.getExternalStorageDirectory(), Utilities.NO_VENTAS_PEDIDO_BACKUP_JSON);

        String jsonNoVentasPedidos = getJsonFromFile(noVentasPedidosFile);
        Utilities.writeToFile(noVentasPedidosFile, jsonNoVentasPedidos);
    }

    @Override
    public <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception {
        File noVentasFile = new File(Environment.getExternalStorageDirectory(), Utilities.NO_VENTAS_PEDIDO_BACKUP_JSON);
        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPendientes = getOnlyPendentEntries((ArrayList<GuardarMotivoNoVentaPedidoDTO>)data);
        String jsonNoVentasPendientes = createJsonFromEntries(noVentasPendientes);
        Utilities.writeToFile(noVentasFile, jsonNoVentasPendientes);
    }

    @Override
    public String getJsonFromFile(File file) throws Exception {
        GuardarMotivoNoVentaPedidoDAL guardarMotivoNoVentaPedidoDAL = new GuardarMotivoNoVentaPedidoDAL(context);

        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPedidos = guardarMotivoNoVentaPedidoDAL.ObtenerListado();
        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPedidosAntesGuardadas = getLastBackUp(file);

        noVentasPedidos = deleteRepeatedEntriesInBackUpList(noVentasPedidos, noVentasPedidosAntesGuardadas);
        if(noVentasPedidos != null && noVentasPedidos.size() > 0) noVentasPedidos = getOnlyPendentEntries(noVentasPedidos);

        return createJsonFromEntries(noVentasPedidos);
    }

    @Override
    public <T> ArrayList<T> getLastBackUp(File file) throws Exception {
        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPedidosAntesGuardadas = null;
        if(file.exists()){
            String noVentasPedidosData = Utilities.readFromFile(file);
            noVentasPedidosAntesGuardadas = SincroHelper.procesarJsonNoVentasPedidoBackUp(noVentasPedidosData);
        }
        return (ArrayList<T>) noVentasPedidosAntesGuardadas;
    }

    @Override
    public <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception {
        if(sqliteEntries == null || sqliteEntries.size() == 0 ) return backUpEntries;
        if(backUpEntries == null || backUpEntries.size() == 0 ) return sqliteEntries;

        ArrayList<T> toRemove = new ArrayList<>();
        for (GuardarMotivoNoVentaPedidoDTO noVentaLinq : (ArrayList<GuardarMotivoNoVentaPedidoDTO>)sqliteEntries){
            for(GuardarMotivoNoVentaPedidoDTO noVentaBackUp : (ArrayList<GuardarMotivoNoVentaPedidoDTO>)backUpEntries){
                if(noVentaBackUp.IdMotivoNoVentaPedido == noVentaLinq.IdMotivoNoVentaPedido){
                    T noVentaAeliminar = (T) noVentaBackUp;
                    toRemove.add(noVentaAeliminar);
                }
            }
        }

        backUpEntries.removeAll(toRemove);
        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasSinRepetir = new ArrayList<>((ArrayList<GuardarMotivoNoVentaPedidoDTO>)sqliteEntries);
        if(backUpEntries.size() > 0) noVentasSinRepetir.addAll((ArrayList<GuardarMotivoNoVentaPedidoDTO>)backUpEntries);

        return (ArrayList<T>)noVentasSinRepetir;
    }

    @Override
    public <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception {
        ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentasPedidosPendientes = new ArrayList<>();
        if(entries == null || entries.size() == 0 ) return (ArrayList<T>)noVentasPedidosPendientes;

        for (GuardarMotivoNoVentaPedidoDTO noVenta : (ArrayList<GuardarMotivoNoVentaPedidoDTO>)entries){
            if(!noVenta.Sincronizada){
                noVentasPedidosPendientes.add(noVenta);
            }
        }

        return (ArrayList<T>)noVentasPedidosPendientes;
    }

    @Override
    public <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writeNoVentasArray(writer, (List<GuardarMotivoNoVentaPedidoDTO>)entries);
        writer.close();
        return stringWriter.toString();
    }

    private void writeNoVentasArray(JsonWriter writer, List<GuardarMotivoNoVentaPedidoDTO> noVentasPedidos) throws IOException {
        writer.beginArray();
        if(noVentasPedidos != null && noVentasPedidos.size() > 0 ){
            for (GuardarMotivoNoVentaPedidoDTO noVentaPedido : noVentasPedidos) {
                writeNoVenta(writer, noVentaPedido);
            }
        }
        writer.endArray();
    }

    private void writeNoVenta(JsonWriter writer, GuardarMotivoNoVentaPedidoDTO noVentaPedido) throws IOException {
        writer.beginObject();
        writer.name("IdMotivoNoVentaPedido").value(noVentaPedido.IdMotivoNoVentaPedido);
        writer.name("IdClienteTimovil").value(noVentaPedido.IdClienteTimovil);
        writer.name("CodigoRuta").value(noVentaPedido.CodigoRuta);
        writer.name("IdResultadoGestion").value(noVentaPedido.IdResultadoGestion);
        writer.name("Descripcion").value(noVentaPedido.Descripcion);
        writer.name("IdCaso").value(noVentaPedido.IdCaso);
        writer.name("Sincronizada").value(noVentaPedido.Sincronizada);
        writer.name("Fecha").value(noVentaPedido.Fecha);
        writer.name("IdCliente").value(noVentaPedido.IdCliente);
        writer.endObject();
    }

    private static ArrayList<GuardarMotivoNoVentaPedidoDTO> obtenerNoVentasPedidosJsonBackUp(){
        try{
            File file = new File(Environment.getExternalStorageDirectory(), Utilities.NO_VENTAS_PEDIDO_BACKUP_JSON);
            String data = Utilities.readFromFile(file);
            return SincroHelper.procesarJsonNoVentasPedidoBackUp(data);
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
            GuardarMotivoNoVentaPedidoDAL guardarMotivoNoVentaPedidoDAL = new GuardarMotivoNoVentaPedidoDAL(context);
            ArrayList<GuardarMotivoNoVentaPedidoDTO> noVentas = NoVentaPedidoBackUp.obtenerNoVentasPedidosJsonBackUp();

            if(noVentas == null) return;

            for(GuardarMotivoNoVentaPedidoDTO noVenta: noVentas){
                GuardarMotivoNoVentaPedidoDTO noVentaSqlite = guardarMotivoNoVentaPedidoDAL.ObtenerMotivoNoVentaDto(noVenta.IdMotivoNoVentaPedido);

                noVenta.Sincronizada = noVentaSqlite != null ? noVentaSqlite.Sincronizada : noVenta.Sincronizada;

                if (!noVenta.Sincronizada) {
                    cantParaSincronizar++;
                    try {
                        //La Nota Crédito no está guardada en el cel
                        noVenta.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_BACKUP;
                        String resultado = guardarMotivoNoVentaPedidoDAL.sincronizarPendiente(noVenta);
                        if (resultado != null && resultado.equalsIgnoreCase("OK")) {
                            cantSincronizada++;
                            noVenta.Sincronizada = true;
                        }
                    } catch (Exception ex) {
                        App.SincronizandoNoVentaIdMotivoNoVentaPedido.remove(Integer.valueOf(noVenta.IdMotivoNoVentaPedido));
                        App.SincronizandoNoVentaPedido = App.SincronizandoNoVentaIdMotivoNoVentaPedido.size() > 0;
                    }
                }
            }

            if(cantParaSincronizar == cantSincronizada){
                Utilities.eliminarArchivo(Utilities.NO_VENTAS_PEDIDO_BACKUP_JSON);
            }else{
                makeBackUpAfterSync(noVentas);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
