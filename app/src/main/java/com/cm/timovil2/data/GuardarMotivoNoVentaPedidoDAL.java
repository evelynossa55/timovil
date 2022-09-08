package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.rest.NetWorkHelper;
import com.cm.timovil2.rest.SincroHelper;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 31/01/17.
 */
public class GuardarMotivoNoVentaPedidoDAL extends DAL {


    private final Context context;

    public GuardarMotivoNoVentaPedidoDAL(Context context) {
        super(context, DAL.TablaGuardarMotivoNoVentaPedido);
        this.context = context;
    }

    public void Insertar(GuardarMotivoNoVentaPedidoDTO pedido) {
        ContentValues values = new ContentValues();
        //values.put("IdMotivoNoVentaPedido", pedido.IdMotivoNoVentaPedido);
        values.put("CodigoRuta", pedido.CodigoRuta);
        values.put("Descripcion", pedido.Descripcion);
        values.put("IdClienteTimovil", pedido.IdClienteTimovil);
        values.put("IdResultadoGestion", pedido.IdResultadoGestion);
        values.put("IdCaso", pedido.IdCaso);
        values.put("Sincronizada", pedido.Sincronizada);
        values.put("Fecha", pedido.Fecha);
        values.put("IdCliente", pedido.IdCliente);

        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    public String eliminar(GuardarMotivoNoVentaPedidoDTO dto) throws Exception {
        try {
            String update = "DELETE FROM " + TablaGuardarMotivoNoVentaPedido
                    + " WHERE IdMotivoNoVentaPedido=" + dto.IdMotivoNoVentaPedido;
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error eliminando la No venta:" + e.getMessage());
        }
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public GuardarMotivoNoVentaPedidoDTO ObtenerMotivoNoVentaDto(int idMotivoNoVentaPedido) {
        GuardarMotivoNoVentaPedidoDTO dto = null;
        Cursor cursor;
        String filtro = "IdMotivoNoVentaPedido=?";
        cursor = this.Obtener(null, filtro, new String[]{String.valueOf(idMotivoNoVentaPedido)});
        if (cursor.moveToFirst()) {
            dto = new GuardarMotivoNoVentaPedidoDTO();
            dto.IdMotivoNoVentaPedido = cursor.getInt(0);
            dto.IdClienteTimovil = cursor.getString(1);
            dto.CodigoRuta = cursor.getString(2);
            dto.IdResultadoGestion = cursor.getInt(3);
            dto.Descripcion = cursor.getString(4);
            dto.IdCaso = cursor.getInt(5);
            dto.Sincronizada = cursor.getInt(6) == 1;
            dto.Fecha = cursor.getLong(7);
            dto.IdCliente = cursor.getString(8);
        }
        cursor.close();
        return dto;
    }

    public ArrayList<GuardarMotivoNoVentaPedidoDTO> obtenerListadoPendientes() {
        ArrayList<GuardarMotivoNoVentaPedidoDTO> lista = new ArrayList<>();
        String filtro = "Sincronizada = ?";
        String[] parametros = {"0"};

        Cursor cursor;
        cursor = this.Obtener(null, filtro, parametros);
        if (cursor.moveToFirst()) {
            do {

                GuardarMotivoNoVentaPedidoDTO dto = new GuardarMotivoNoVentaPedidoDTO();
                dto.IdMotivoNoVentaPedido = cursor.getInt(0);
                dto.IdClienteTimovil = cursor.getString(1);
                dto.CodigoRuta = cursor.getString(2);
                dto.IdResultadoGestion = cursor.getInt(3);
                dto.Descripcion = cursor.getString(4);
                dto.IdCaso = cursor.getInt(5);
                dto.Sincronizada = cursor.getInt(6) == 1;
                dto.Fecha = cursor.getLong(7);
                dto.IdCliente = cursor.getString(8);
                lista.add(dto);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ArrayList<GuardarMotivoNoVentaPedidoDTO> ObtenerListado(int idResultadoGestion) {
        ArrayList<GuardarMotivoNoVentaPedidoDTO> lista = new ArrayList<>();
        Cursor cursor;
        String filtro = "IdResultadoGestion=?";
        cursor = this.Obtener("IdResultadoGestion ASC", filtro, new
                String[]{String.valueOf(idResultadoGestion)});
        if (cursor.moveToFirst()) {
            do {
                GuardarMotivoNoVentaPedidoDTO dto = new GuardarMotivoNoVentaPedidoDTO();
                dto.IdMotivoNoVentaPedido = cursor.getInt(0);
                dto.IdClienteTimovil = cursor.getString(1);
                dto.CodigoRuta = cursor.getString(2);
                dto.IdResultadoGestion = cursor.getInt(3);
                dto.Descripcion = cursor.getString(4);
                dto.IdCaso = cursor.getInt(5);
                dto.Sincronizada = cursor.getInt(6) == 1;
                dto.Fecha = cursor.getLong(7);
                dto.IdCliente = cursor.getString(8);
                lista.add(dto);


            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ArrayList<GuardarMotivoNoVentaPedidoDTO> ObtenerListado() {
        ArrayList<GuardarMotivoNoVentaPedidoDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener(null, null, null);
        if (cursor.moveToFirst()) {
            do {

                GuardarMotivoNoVentaPedidoDTO dto = new GuardarMotivoNoVentaPedidoDTO();
                dto.IdMotivoNoVentaPedido = cursor.getInt(0);
                dto.IdClienteTimovil = cursor.getString(1);
                dto.CodigoRuta = cursor.getString(2);
                dto.IdResultadoGestion = cursor.getInt(3);
                dto.Descripcion = cursor.getString(4);
                dto.IdCaso = cursor.getInt(5);
                dto.Sincronizada = cursor.getInt(6) == 1;
                dto.Fecha = cursor.getLong(7);
                dto.IdCliente = cursor.getString(8);
                lista.add(dto);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public String descargarPendientes() throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<GuardarMotivoNoVentaPedidoDTO> lista = obtenerListadoPendientes();
            for (GuardarMotivoNoVentaPedidoDTO f : lista) {
                f.EnviadaDesde = Utilities.FACTURA_ENVIADA_DESDE_SINCRO;
                String respuesta = sincronizarPendiente(f);
                sb.append("Caso: ").append(f.IdCaso).append(": ").append(respuesta).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String sincronizarPendiente(GuardarMotivoNoVentaPedidoDTO pendiente) throws Exception {


        if (App.SincronizandoNoVentaPedido &&
                App.SincronizandoNoVentaIdMotivoNoVentaPedido.contains(pendiente.IdMotivoNoVentaPedido)) {
            return "Sincronizando";
        }

        Log.i("sincronizarAbono", "Sincronizando " + pendiente.IdMotivoNoVentaPedido);
        App.SincronizandoNoVentaPedido = true;
        App.SincronizandoNoVentaIdMotivoNoVentaPedido.add(pendiente.IdMotivoNoVentaPedido);

        if (!Utilities.isNetworkReachable(context) || !Utilities.isNetworkConnected(context)) {
            throw new Exception(App.ERROR_CONECTIVIDAD);
        }

        NetWorkHelper netWorkHelper = new NetWorkHelper();
        JSONObject jsonConfirmacion = new JSONObject();
        jsonConfirmacion.put("IdClienteTiMovil", pendiente.IdClienteTimovil);
        jsonConfirmacion.put("CodigoRuta", pendiente.CodigoRuta);
        jsonConfirmacion.put("IdMotivoNegativo", pendiente.IdResultadoGestion);
        jsonConfirmacion.put("Comentario", pendiente.Descripcion);
        jsonConfirmacion.put("IdCaso", pendiente.IdCaso);
        jsonConfirmacion.put("EsFactura", true);
        jsonConfirmacion.put("NumeroDocumento", "");
        jsonConfirmacion.put("EnviadaDesde", pendiente.EnviadaDesde);

        String respuesta_confirmar_pedido = netWorkHelper.writeService(jsonConfirmacion, SincroHelper.CONFIRMAR_PEDIDO);
        respuesta_confirmar_pedido = SincroHelper.procesarOkJson(respuesta_confirmar_pedido);

        if (respuesta_confirmar_pedido.equals("OK")) {
            // Aquí debo actualizar el estado del Motivo
            actualizarEstadoDescarga(pendiente.IdMotivoNoVentaPedido);
        }

        App.SincronizandoNoVentaIdMotivoNoVentaPedido.remove(Integer.valueOf(pendiente.IdMotivoNoVentaPedido));
        App.SincronizandoNoVentaPedido = App.SincronizandoNoVentaIdMotivoNoVentaPedido.size() > 0;
        return respuesta_confirmar_pedido;
    }

    private String actualizarEstadoDescarga(int idMotivoNoVentaPedido) throws Exception {

        try {

            String update = "update " + TablaGuardarMotivoNoVentaPedido
                    + " set Sincronizada = 1 where IdMotivoNoVentaPedido = " + idMotivoNoVentaPedido;
            executeQuery(update);
            return "OK";
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de descarga:"
                    + e.getMessage());
        }

    }

    @Override
    void setColumns() {
        columnas = new String[]{"IdMotivoNoVentaPedido", "IdClienteTimovil", "CodigoRuta",
                "IdResultadoGestion", "Descripcion", "IdCaso", "Sincronizada", "Fecha", "IdCliente"};
    }
}
