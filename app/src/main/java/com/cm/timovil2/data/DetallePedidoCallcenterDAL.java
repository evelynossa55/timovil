package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.dto.DetallePedidoCallcenterDTO;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 25/01/17.
 */
public class DetallePedidoCallcenterDAL extends DAL {


    public DetallePedidoCallcenterDAL(Context context) {
        super(context, DAL.TablaDetallePedidoCallcenter);
    }

    public void Insertar(DetallePedidoCallcenterDTO pedido) {
        ContentValues values = new ContentValues();
        values.put("IdPedido", pedido.IdPedido);
        values.put("IdProducto", pedido.IdProducto);
        values.put("Cantidad", pedido.Cantidad);
        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    public ArrayList<DetallePedidoCallcenterDTO> ObtenerListado(int idPedido) {
        ArrayList<DetallePedidoCallcenterDTO> lista = new ArrayList<>();
        Cursor cursor;
        String filtro = "IdPedido=?";
        cursor = this.Obtener("IdPedido ASC", filtro, new String[]{String.valueOf(idPedido)});
        if (cursor.moveToFirst()) {
            do {
                DetallePedidoCallcenterDTO dto = new DetallePedidoCallcenterDTO();
                dto.IdDetallePedido = cursor.getInt(0);
                dto.IdPedido = cursor.getInt(1);
                dto.IdProducto = cursor.getInt(2);
                dto.Cantidad = cursor.getInt(3);
                lista.add(dto);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    @Override
    void setColumns() {
        columnas = new String[] { "IdDetallePedido", "IdPedido", "IdProducto", "Cantidad" };
    }
}