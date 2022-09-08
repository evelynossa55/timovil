package com.cm.timovil2.backup;

import android.os.Environment;
import android.util.JsonWriter;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.FacturaDAL;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.FacturaDTO;
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
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 27/06/18.
 */

@SuppressWarnings("unchecked")
public class FacturaBackUp implements IBackUp {

    private ActivityBase context;

    public FacturaBackUp(ActivityBase context){
        this.context = context;
    }

    @Override
    public void makeBackUp() throws Exception {
        File facturasFile = new File(Environment.getExternalStorageDirectory(), Utilities.FACTURAS_BACKUP_JSON);

        String jsonFacturas = getJsonFromFile(facturasFile);
        Utilities.writeToFile(facturasFile, jsonFacturas);
    }

    @Override
    public <T> void makeBackUpAfterSync(ArrayList<T> data) throws Exception {
        File facturasFile = new File(Environment.getExternalStorageDirectory(), Utilities.FACTURAS_BACKUP_JSON);
        ArrayList<FacturaDTO> facturasPendientes = getOnlyPendentEntries((ArrayList<FacturaDTO>)data);
        String jsonFacturasPendientes = createJsonFromEntries(facturasPendientes);
        Utilities.writeToFile(facturasFile, jsonFacturasPendientes);
    }

    @Override
    public String getJsonFromFile(File file) throws Exception {
        DateTime now = DateTime.now();
        FacturaDAL facturaDAL = new FacturaDAL(context);

        ArrayList<FacturaDTO> facturas = facturaDAL.obtenerListado(true, now, now);
        ArrayList<FacturaDTO> facturasAntesGuardadas = getLastBackUp(file);

        facturas = deleteRepeatedEntriesInBackUpList(facturas, facturasAntesGuardadas);
        if(facturas != null && facturas.size() > 0) facturas = getOnlyPendentEntries(facturas);

        return createJsonFromEntries(facturas);
    }

    @Override
    public <T> ArrayList<T> getLastBackUp(File file) throws Exception {
        ArrayList<FacturaDTO> facturasAntesGuardadas = null;
        if (file.exists()) {
            String facturasData = Utilities.readFromFile(file);
            facturasAntesGuardadas = SincroHelper.procesarJsonFacturasBackUp(facturasData);
        }
        return (ArrayList<T>) facturasAntesGuardadas;
    }

    @Override
    public <T> ArrayList<T> deleteRepeatedEntriesInBackUpList(ArrayList<T> sqliteEntries, ArrayList<T> backUpEntries) throws Exception {
        if(sqliteEntries == null || sqliteEntries.size() == 0 ) return backUpEntries;
        if(backUpEntries == null || backUpEntries.size() == 0 ) return sqliteEntries;

        ArrayList<T> toRemove = new ArrayList<>();
        for (FacturaDTO facLinq : (ArrayList<FacturaDTO>)sqliteEntries){
            for(FacturaDTO facBackUp : (ArrayList<FacturaDTO>)backUpEntries){
                if(facBackUp.NumeroFactura.equals(facLinq.NumeroFactura)){
                    T facturaToRemove = (T) facBackUp;
                    toRemove.add(facturaToRemove);
                }
            }
        }
        backUpEntries.removeAll(toRemove);
        ArrayList<FacturaDTO> facturasSinRepetir = new ArrayList<>((ArrayList<FacturaDTO>)sqliteEntries);
        if(backUpEntries.size() > 0) facturasSinRepetir.addAll((ArrayList<FacturaDTO>)backUpEntries);

        return (ArrayList<T>) facturasSinRepetir;
    }

    @Override
    public <T> ArrayList<T> getOnlyPendentEntries(ArrayList<T> entries) throws Exception {
        ArrayList<FacturaDTO> facturasPendientes = new ArrayList<>();
        if(entries == null || entries.size() == 0 ) return (ArrayList<T>)facturasPendientes;

        for (FacturaDTO factura : (ArrayList<FacturaDTO>) entries){
            if(!factura.Sincronizada){
                facturasPendientes.add(factura);
            }
        }

        return (ArrayList<T>) facturasPendientes;
    }

    @Override
    public <T> String createJsonFromEntries(ArrayList<T> entries) throws Exception {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        ArrayList<FacturaDTO> facturas = (ArrayList<FacturaDTO>) entries;
        writeFacturasArray(writer, facturas);
        writer.close();
        return stringWriter.toString();
    }

    private void writeFacturasArray(JsonWriter writer, List<FacturaDTO> facturas) throws IOException {
        writer.beginArray();
        if(facturas != null && facturas.size() > 0){
            for (FacturaDTO factura : facturas) {
                writeFactura(writer, factura);
            }
        }
        writer.endArray();
    }

    private void writeFactura(JsonWriter writer, FacturaDTO factura) throws IOException {
        writer.beginObject();
        writer.name("NumeroFactura").value(factura.NumeroFactura);
        writer.name("FechaHora").value(factura.FechaHora);
        writer.name("FormaPago").value(factura.FormaPago);
        writer.name("IdCliente").value(factura.IdCliente);
        writer.name("Identificacion").value(factura.Identificacion);
        writer.name("RazonSocial").value(factura.RazonSocial);
        writer.name("Negocio").value(factura.Negocio);
        writer.name("Direccion").value(factura.Direccion);
        writer.name("Telefono").value(factura.Telefono);
        writer.name("Subtotal").value(factura.Subtotal);
        writer.name("Descuento").value(factura.Descuento);
        writer.name("Retefuente").value(factura.Retefuente);
        writer.name("PorcentajeRetefuente").value(factura.PorcentajeRetefuente);
        writer.name("Iva").value(factura.Iva);
        writer.name("Ica").value(factura.Ica);
        writer.name("Total").value(factura.Total);
        writer.name("Sincronizada").value(factura.Sincronizada ? 1 : 0);
        writer.name("Anulada").value(factura.Anulada ? 1 : 0);
        writer.name("PendienteAnulacion").value(factura.PendienteAnulacion ? 1 : 0);
        writer.name("Efectivo").value(factura.EfectivoPagado);
        writer.name("Devolucion").value(factura.Devolucion);
        writer.name("Latitud").value(factura.Latitud);
        writer.name("Longitud").value(factura.Longitud);
        writer.name("CREE").value(factura.CREE);
        writer.name("PorcentajeCREE").value(factura.PorcentajeCREE);
        writer.name("Comentario").value(factura.Comentario);
        writer.name("IpoConsumo").value(factura.IpoConsumo);
        writer.name("TipoDocumento").value(factura.TipoDocumento);
        writer.name("codigoBodega").value(factura.CodigoBodega);
        writer.name("NumeroPedido").value(factura.NumeroPedido);
        writer.name("ComentarioAnulacion").value(factura.ComentarioAnulacion);
        writer.name("IdEmpleadoEntregador").value(factura.IdEmpleadoEntregador);
        writer.name("IdResolucion").value(factura.IdResolucion);
        writer.name("Revisada").value(factura.Revisada ? 1 : 0);
        writer.name("IsPedidoCallcenter").value(factura.IsPedidoCallcenter ? 1 : 0);
        writer.name("FechaHoraVencimiento").value( factura.FechaHoraVencimiento);
        writer.name("IdPedido").value(factura.IdPedido);
        writer.name("IdCaso").value(factura.IdCaso);
        writer.name("ValorDevolucion").value(factura.ValorDevolucion);
        writer.name("ReteIva").value(factura.ReteIva);
        writer.name("QRInputValue").value(factura.QRInputValue);
        writer.name("FacturaPos").value(factura.FacturaPos ? 1 : 0);

        if (factura.DetalleFactura != null) {
            writer.name("DetalleFactura");
            writeDetalleFacturaArray(writer, factura.DetalleFactura);
        } else {
            writer.name("DetalleFactura").nullValue();
        }
        writer.endObject();
    }

    private void writeDetalleFacturaArray(JsonWriter writer, List<DetalleFacturaDTO> detalle) throws IOException {
        writer.beginArray();
        for (DetalleFacturaDTO value : detalle) {
            writeDetalleFactura(writer, value);
        }
        writer.endArray();
    }

    private void writeDetalleFactura(JsonWriter writer, DetalleFacturaDTO detalle) throws IOException {
        writer.beginObject();
        writer.name("NumeroFactura").value(detalle.NumeroFactura);
        writer.name("IdProducto").value(detalle.IdProducto);
        writer.name("Codigo").value(detalle.Codigo);
        writer.name("Nombre").value(detalle.Nombre);
        writer.name("Cantidad").value(detalle.Cantidad);
        writer.name("Devolucion").value(detalle.Devolucion);
        writer.name("Rotacion").value(detalle.Rotacion);
        writer.name("ValorUnitario").value(detalle.ValorUnitario);
        writer.name("Subtotal").value(detalle.Subtotal);
        writer.name("Descuento").value(detalle.Descuento);
        if(detalle.DescuentoAdicional > 0){
            writer.name("PorcentajeDescuento").value(detalle.DescuentoAdicional);
        }else {
            writer.name("PorcentajeDescuento").value(detalle.PorcentajeDescuento);
        }
        writer.name("Iva").value(detalle.Iva);
        writer.name("PorcentajeIva").value(detalle.PorcentajeIva);
        writer.name("Total").value(detalle.Total);
        writer.name("IpoConsumo").value(detalle.ValorIpoConsumo);
        writer.name("ValorDevolucion").value(detalle.ValorDevolucion);
        writer.endObject();
    }

    private static ArrayList<FacturaDTO> obtenerFacturasJsonBackUp(){
        try{
            File file = new File(Environment.getExternalStorageDirectory(), Utilities.FACTURAS_BACKUP_JSON);
            FileInputStream in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            return SincroHelper.procesarJsonFacturasBackUp(sb.toString());
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void syncBackup(){
        try {
            int cantParaSincronizar = 0;
            int cantSincronizada = 0;
            FacturaDAL facDal = new FacturaDAL(context);
            ArrayList<FacturaDTO> facturas = FacturaBackUp.obtenerFacturasJsonBackUp();
            if (facturas == null) return;

            for (FacturaDTO fac : facturas) {
                FacturaDTO facSqlite = facDal.obtenerPorNumeroFac(fac.NumeroFactura);

                fac.Sincronizada = facSqlite != null ? facSqlite.Sincronizada : fac.Sincronizada;

                if (!fac.Sincronizada) {
                    //La factura no está guardada en el cel
                    cantParaSincronizar++;
                    try {
                        fac.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_BACKUP;
                        String resultado = facDal.sincronizarFactura(fac);
                        if (resultado != null && resultado.equalsIgnoreCase("OK")) {
                            cantSincronizada++;
                            fac.Sincronizada = true;
                        }
                    } catch (Exception ex) {
                        App.SincronizandoFacturaNumero.remove(fac.NumeroFactura);
                        App.SincronizandoFactura = App.SincronizandoFacturaNumero.size() > 0;
                    }
                }
            }

            if (cantParaSincronizar == cantSincronizada) {
                Utilities.eliminarArchivo(Utilities.FACTURAS_BACKUP_JSON);
            }else{
                makeBackUpAfterSync(facturas);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
