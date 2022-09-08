package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.DetallePedidoCallcenterDTO;
import com.cm.timovil2.dto.PedidoCallcenterDTO;

import java.util.ArrayList;

/**
 * CREADO POR JORGE AMDRÉS DAVID CARDONA EL 23/01/17.
 */
public class PedidoCallcenterDAL extends DAL {


    private final Context context;
    public PedidoCallcenterDAL(Context context) {
        super(context, DAL.TablaPedidoCallcenter);
        this.context = context;
    }

    public void Insertar(PedidoCallcenterDTO pedido) {
        ContentValues values = new ContentValues();
        values.put("IdCliente", pedido.IdCliente);
        values.put("IdCaso", pedido.IdCaso);
        values.put("FechaSolicitada", pedido.FechaSolicitada);
        values.put("Comentario", pedido.Comentario);
        long id = super.insertar(values);
        if(id > 0){
            for (DetallePedidoCallcenterDTO d: pedido.Detalle) {
                d.IdPedido = id;
                new DetallePedidoCallcenterDAL(context).Insertar(d);
            }
        }
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    public void eliminarPedido(int idPedido, int idCaso){

        String update = "DELETE FROM "
                + TablaDetallePedidoCallcenter
                + " WHERE " + "IdPedido = "+idPedido;

        super.executeQuery(update);

        String update2 = "DELETE FROM "
                + TablaPedidoCallcenter
                + " WHERE " + "IdPedido = "+idPedido
                + " OR IdCaso = " + idCaso;

        super.executeQuery(update2);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<PedidoCallcenterDTO> ObtenerListado() {
        ArrayList<PedidoCallcenterDTO> lista = new ArrayList<>();
        Cursor cursor;
        cursor = this.Obtener("IdPedido ASC", null, null);
        if (cursor.moveToFirst()) {
            do {
                PedidoCallcenterDTO dto = new PedidoCallcenterDTO();
                dto.IdPedido = cursor.getInt(0);
                dto.IdCliente = cursor.getInt(1);
                dto.IdCaso = cursor.getInt(2);
                dto.FechaSolicitada = cursor.getString(3);
                dto.Comentario = cursor.getString(4);
                dto.Detalle = new DetallePedidoCallcenterDAL(context).ObtenerListado(dto.IdPedido);
                lista.add(dto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public PedidoCallcenterDTO Obtener(int idCaso) {
        PedidoCallcenterDTO pedido = null;
        Cursor cursor;
        cursor = this.Obtener(null, "IdCaso = ?", new String[]{String.valueOf(idCaso)});
        if (cursor.moveToFirst()) {

                pedido = new PedidoCallcenterDTO();
                pedido.IdPedido = cursor.getInt(0);
                pedido.IdCliente = cursor.getInt(1);
                pedido.IdCaso = cursor.getInt(2);
                pedido.FechaSolicitada = cursor.getString(3);
                pedido.Comentario = cursor.getString(4);
                pedido.Detalle = new DetallePedidoCallcenterDAL(context).ObtenerListado(pedido.IdPedido);

        }
        cursor.close();
        return pedido;
    }

    public PedidoCallcenterDTO ObtenerPorCliente(int idCliente) {
        PedidoCallcenterDTO pedido = null;
        Cursor cursor;
        cursor = this.Obtener(null, "IdCliente = ?", new String[]{String.valueOf(idCliente)});
        if (cursor.moveToFirst()) {

            pedido = new PedidoCallcenterDTO();
            pedido.IdPedido = cursor.getInt(0);
            pedido.IdCliente = cursor.getInt(1);
            pedido.IdCaso = cursor.getInt(2);
            pedido.FechaSolicitada = cursor.getString(3);
            pedido.Comentario = cursor.getString(4);
            pedido.Detalle = new DetallePedidoCallcenterDAL(context).ObtenerListado(pedido.IdPedido);

        }
        cursor.close();
        return pedido;
    }

    @Override
    void setColumns() {
        columnas = new String[] { "IdPedido", "IdCliente", "IdCaso", "FechaSolicitada", "Comentario" };
    }
}
