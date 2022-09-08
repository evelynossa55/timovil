package com.cm.timovil2.bl.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ProductoDTO;
import com.cm.timovil2.dto.ResolucionDTO;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class AdapterInventario extends RecyclerView.Adapter<AdapterInventario.ViewHolder>{

    private final ArrayList<ProductoDTO> datos;
    private ResolucionDTO resolucionDTO;

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView lblNombreProducto;
        TextView lblCodigo;
        TextView lblBodega;
        TextView lblDev;
        TextView lblRot;
        TextView lblVlru;
        TextView lblVentas;
        TextView lblStock;

        ViewHolder(View view) {
            super(view);
            lblNombreProducto = view.findViewById(R.id.lblNombreProducto);
            lblCodigo = view.findViewById(R.id.tv_codigo);
            lblBodega = view.findViewById(R.id.tv_bodega);
            lblDev = view.findViewById(R.id.tv_dev);
            lblRot = view.findViewById(R.id.tv_rot);
            lblVlru = view.findViewById(R.id.tv_vlru);
            lblVentas = view.findViewById(R.id.tv_ventas);
            lblStock = view.findViewById(R.id.tv_stock);
        }
    }

    public AdapterInventario(Activity _context, ArrayList<ProductoDTO> _datos) {
        datos = _datos;
        try {
            resolucionDTO = new ResolucionDAL(_context).ObtenerResolucion();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ProductoDTO producto = datos.get(position);

        holder.lblNombreProducto.setText(producto.Nombre.trim());
        holder.lblBodega.setText(producto.CodigoBodega);
        holder.lblDev.setText(String.valueOf(producto.Devoluciones));
        holder.lblRot.setText(String.valueOf(producto.Rotaciones));
        holder.lblVlru.setText(Utilities.FormatoMoneda(producto.Precio1));
        holder.lblVentas.setText(String.valueOf(producto.Ventas));
        holder.lblStock.setText(String.valueOf(producto.getStock(resolucionDTO)));
        holder.lblCodigo.setText(producto.Codigo.trim());

    }

    // Create new views (invoked by the layout manager)
    @Override
    public @NonNull AdapterInventario.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
        // create a new view
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_inventario, parent, false);
        return new ViewHolder(cv);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return datos.size();
    }

}