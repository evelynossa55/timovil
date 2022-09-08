package com.cm.timovil2.bl.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;

import java.util.ArrayList;

public class AdapterMenuVendedor extends RecyclerView.Adapter<AdapterMenuVendedor.ViewHolder>  {

    private final ArrayList<String> values;
    //private final ArrayList<String> colors;
    private OnRecyclerViewItemClickListener onClickListener;
    private Context context;

    public AdapterMenuVendedor(ArrayList<String> values,Context context) {
        this.values = values;
        //this.colors = getMenuItemsColors();
        this.context=context;

    }

    public void setOnClickListener(OnRecyclerViewItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        //CardView cardView;
        ConstraintLayout layout;
        ImageView imgMenuItem;
        TextView tvMenu;
        LinearLayout layout_colors;

        ViewHolder(View view) {
            super(view);
            //cardView = (CardView) view;
            layout=(ConstraintLayout)view;
            imgMenuItem = view.findViewById(R.id.img_menu_item);
            tvMenu = view.findViewById(R.id.tv_menu);
            layout_colors=view.findViewById(R.id.layout_colors);

        }
    }
/**
 * Cambios en diseños del recyclerview Realizados por Juan Sebastian Arenas Borja
 * */
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final @NonNull AdapterMenuVendedor.ViewHolder holder, int position) {


        String item = values.get(position);
        holder.tvMenu.setText(item);

        holder.layout.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_transition));
        switch (item){
            case "Rutero":
                if (!App.isLocationServiceActive) {
                    holder.imgMenuItem.setImageResource(R.drawable.menu_rutero);
                    holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                } else {
                    holder.imgMenuItem.setImageResource(R.drawable.menu_rutero);
                    holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                }
                break;
            case "Facturas":
                holder.imgMenuItem.setImageResource(R.drawable.menu_facturas);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                break;
            case "Impresora":
                holder.imgMenuItem.setImageResource(R.drawable.menu_impresora);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                break;
            case "Inventario":
                holder.imgMenuItem.setImageResource(R.drawable.menu_inventario);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_green);
                break;
            case "Cargar datos":
                holder.imgMenuItem.setImageResource(R.drawable.menu_cargadatos);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                break;
            case "Créditos":
                holder.imgMenuItem.setImageResource(R.drawable.menu_creditos);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                break;
            case "Ajustes":
                holder.imgMenuItem.setImageResource(R.drawable.menu_ajustes);
                break;
            case "Gastos vehículo":
                holder.imgMenuItem.setImageResource(R.drawable.menu_gastovehiculo);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_green);
                break;
            case "Remisiones":
                holder.imgMenuItem.setImageResource(R.drawable.menu_remisiones);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                break;
            case "Pedidos logística":
                holder.imgMenuItem.setImageResource(R.drawable.menu_pedidoslogistica);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_green);
                break;
            case "No ventas":
                holder.imgMenuItem.setImageResource(R.drawable.menu_noventas);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_green);
                break;
            case "Notas Crédito":
                holder.imgMenuItem.setImageResource(R.drawable.menu_notascredito);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_blue);
                break;
            case "Ventas mes":
                holder.imgMenuItem.setImageResource(R.drawable.menu_ventasmes);
                holder.layout_colors.setBackgroundResource(R.drawable.backrground_items_recycler_green);
                break;
            default:
                holder.imgMenuItem.setImageResource(R.drawable.menu_ajustes);
                break;
        }
        //holder.layout_colors.setBackgroundColor(Color.parseColor(colors.get(position)));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onItemClick(view, holder.getAdapterPosition());
            }
        });

    }

    // Create new views (invoked by the layout manager)
    @Override
    public @NonNull AdapterMenuVendedor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                  int viewType) {
        // create a new view
        /*CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_menuvendedor, parent, false);*/
        //Cambio realizado por Juan Sebastian Arenas Borja
        ConstraintLayout cv = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_menuvendedor, parent, false);
        return new AdapterMenuVendedor.ViewHolder(cv);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values != null ? values.size() : 0;
    }

    /* Metodo con el que se le asignan los colores a los elementos del recyclerview
    private ArrayList<String> getMenuItemsColors(){
        ArrayList<String> resultado = new ArrayList<>();
        resultado.add("#3cc5e9");
        resultado.add("#336b97");
        resultado.add("#4d81bd");
        resultado.add("#5b5dac");
        resultado.add("#604373");
        resultado.add("#5e5197");
        resultado.add("#8036a3");
        resultado.add("#a1208d");
        resultado.add("#1a7f5c");
        resultado.add("#339783");
        resultado.add("#118441");
        resultado.add("#859733");
        resultado.add("#5ebc1a");
        return resultado;
    }

     */
} 