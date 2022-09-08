package com.cm.timovil2.bl.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemNotaCreditoDetail;
import com.cm.timovil2.bl.utilities.ItemNotaCreditoHeader;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 14/03/18.
 */

public class AdapterNotasCreditoPorFecha extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_HOLDER_TYPE_FECHA = 0;
    private static final int VIEW_HOLDER_TYPE_NOTA = 1;
    private static final int VIEW_HOLDER_TYPE_FOOTER = 2;

    private final ArrayList<Item> items;
    private int position;
    private OnRecyclerViewItemClickListener onClickListener;

    public AdapterNotasCreditoPorFecha(ArrayList<Item> items) {
        this.items = items;
    }

    static class ViewHolderFecha extends RecyclerView.ViewHolder {

        TextView layoutItemSectionText;

        ViewHolderFecha(View view) {
            super(view);
            layoutItemSectionText = view.findViewById(R.id.layout_item_section_text);
        }
    }

    static class ViewHolderFooter extends RecyclerView.ViewHolder{

        LinearLayout layout;

        ViewHolderFooter(View view){
            super(view);
            layout = (LinearLayout) view;
        }
    }

    static class ViewHolderNota extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        CardView cardView;
        TextView lblNumeroDocumento;
        TextView tvFactura;
        TextView tvValor;
        ImageView imgEstadoNota;
        Context context;

        ViewHolderNota(View view) {
            super(view);
            cardView = (CardView) view;
            lblNumeroDocumento = view.findViewById(R.id.lblNumeroDocumento);
            tvFactura = view.findViewById(R.id.tv_factura);
            tvValor = view.findViewById(R.id.tv_valor);
            imgEstadoNota = view.findViewById(R.id.imgEstadoNota);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Opciones de Notas Crédito");
            menu.add(Menu.NONE, 1, 1, "Abrir");
            menu.add(Menu.NONE, 3, 3, "Imprimir");
        }
    }

    public void setOnClickListener(OnRecyclerViewItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        if(position == items.size()){
            // This is where we'll add footer.
            return VIEW_HOLDER_TYPE_FOOTER;
        }

        final Item i = items.get(position);
        return (i.isSection() ? VIEW_HOLDER_TYPE_FECHA : VIEW_HOLDER_TYPE_NOTA);
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof ViewHolderFooter){
            final ViewHolderFooter viewHolderFooter = (ViewHolderFooter) holder;
            viewHolderFooter.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.onItemClick(view, viewHolderFooter.getAdapterPosition());
                }
            });

            return;
        }

        final Item i = items.get(position);

        if (i != null) {
            if (i.isSection()) {

                ViewHolderFecha holderFecha = (ViewHolderFecha) holder;
                Locale locale = Utilities.getLocale();
                SimpleDateFormat dt = new SimpleDateFormat("EEE, dd MMM yyyy", locale);
                ItemNotaCreditoHeader fh = (ItemNotaCreditoHeader) i;

                holderFecha.layoutItemSectionText.setText(dt.format(fh.getFechaNota()));
                holderFecha.layoutItemSectionText.setTextColor(Color.parseColor("#298BD5"));

            } else {

                ViewHolderNota viewHolderNota = (ViewHolderNota) holder;
                ItemNotaCreditoDetail fd = (ItemNotaCreditoDetail) i;
                NotaCreditoFacturaDTO nota = fd.getNotaCreditoFacturaDTO();
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Utilities.getLocale());

                String numDoc = "Nota: " + nota.NumeroDocumento;
                viewHolderNota.lblNumeroDocumento.setText(numDoc);
                viewHolderNota.tvFactura.setText(nota.NumeroFactura);
                viewHolderNota.tvValor.setText(formatter.format(nota.Valor));

                if (!nota.Sincronizada) {
                    viewHolderNota.imgEstadoNota.setImageResource(R.drawable.icono_factura_pendiente);
                } else if(nota.Anulada) {
                    viewHolderNota.imgEstadoNota.setImageResource(R.drawable.icono_factura_anulada);
                }else{
                    viewHolderNota.imgEstadoNota.setImageResource(R.drawable.icono_factura_descargada);
                }

                viewHolderNota.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        setPosition(holder.getAdapterPosition());
                        return false;
                    }
                });
            }
        }
    }

    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        switch (viewType) {
            case VIEW_HOLDER_TYPE_FECHA:
                LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listview_item_section, parent, false);
                return new AdapterNotasCreditoPorFecha.ViewHolderFecha(v);
            case VIEW_HOLDER_TYPE_NOTA:
                CardView cvf = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_notacredito, parent, false);
                return new AdapterNotasCreditoPorFecha.ViewHolderNota(cvf);
            case VIEW_HOLDER_TYPE_FOOTER:
                LinearLayout l = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_backtotop, parent, false);
                return new AdapterNotasCreditoPorFecha.ViewHolderFooter(l);
        }

        throw new RuntimeException("No existe un tipo igual a " + viewType + " + para la lista de notas crédito");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }

        if (items.size() == 0) {
            return 1;
        }

        // Add extra view to show the footer view
        return items.size() + 1;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}
