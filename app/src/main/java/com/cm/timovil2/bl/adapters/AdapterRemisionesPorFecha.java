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
import com.cm.timovil2.bl.utilities.ItemFacturaHeader;
import com.cm.timovil2.bl.utilities.ItemRemisionDetail;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.RemisionDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * CREADO PO JORGE ANDRES DAVID CARDONA EL 09/02/2016.
 */

public class AdapterRemisionesPorFecha extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_HOLDER_TYPE_FECHA = 0;
    private static final int VIEW_HOLDER_TYPE_FACTURA = 1;
    private static final int VIEW_HOLDER_TYPE_FOOTER = 2;

    private final ArrayList<Item> items;
    private int position;
    private OnRecyclerViewItemClickListener onClickListener;

    public AdapterRemisionesPorFecha(ArrayList<Item> items) {
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

    static class ViewHolderRemision extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        CardView cardView;
        TextView lblNumeroFactura;
        TextView tvDescuento;
        TextView tvDevolucion;
        TextView tvSubtotal;
        TextView tvIva;
        TextView tvIpoconsumo;
        TextView tvTotal;
        ImageView imgEstadoFactura;
        Context context;

        ViewHolderRemision(View view) {
            super(view);
            cardView = (CardView) view;
            lblNumeroFactura = view.findViewById(R.id.lblNumeroFactura);
            tvDescuento = view.findViewById(R.id.tv_descuento);
            tvDevolucion = view.findViewById(R.id.tv_devolucion);
            tvSubtotal = view.findViewById(R.id.tv_subtotal);
            tvIva = view.findViewById(R.id.tv_iva);
            tvIpoconsumo = view.findViewById(R.id.tv_ipoconsumo);
            tvTotal = view.findViewById(R.id.tv_total);
            imgEstadoFactura = view.findViewById(R.id.imgEstadoFactura);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Opciones de remisión");
            menu.add(Menu.NONE, 1, 1, "Abrir");
            menu.add(Menu.NONE, 2, 2, "Anular");
            menu.add(Menu.NONE, 3, 3, "Imprimir");
            menu.add(Menu.NONE, 4, 4, "Eliminar");
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
        return (i.isSection() ? VIEW_HOLDER_TYPE_FECHA : VIEW_HOLDER_TYPE_FACTURA);
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
                ItemFacturaHeader fh = (ItemFacturaHeader) i;

                holderFecha.layoutItemSectionText.setText(dt.format(fh.getFechaFactura()));
                holderFecha.layoutItemSectionText.setTextColor(Color.parseColor("#298BD5"));

            } else {

                ItemRemisionDetail fd = (ItemRemisionDetail) i;
                ViewHolderRemision remisionHolder = (ViewHolderRemision) holder;

                RemisionDTO remision = fd.getRemisionDTO();

                String numFac = remision.NumeroRemision + " " + remision.RazonSocialCliente;
                remisionHolder.lblNumeroFactura.setText(numFac);

                if (remision.Anulada) {
                    remisionHolder.tvDescuento.setText("");
                    remisionHolder.tvDevolucion.setText("");
                    remisionHolder.tvSubtotal.setText("");
                    remisionHolder.tvIva.setText("");
                    remisionHolder.tvIpoconsumo.setText("");
                    remisionHolder.tvTotal.setText("");
                } else {
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
                    remisionHolder.tvDescuento.setText(formatter.format(remision.Descuento));
                    remisionHolder.tvDevolucion.setText(formatter.format(remision.Devolucion));
                    remisionHolder.tvSubtotal.setText(formatter.format(remision.Subtotal));
                    remisionHolder.tvIva.setText(formatter.format(remision.Iva));
                    remisionHolder.tvIpoconsumo.setText(formatter.format(remision.Ipoconsumo));
                    remisionHolder.tvTotal.setText(formatter.format(remision.Total));
                }

                if (!remision.Sincronizada) {
                    remisionHolder.imgEstadoFactura.setImageResource(R.drawable.icono_factura_pendiente);
                } else {
                    if (remision.PendienteAnulacion && remision.Anulada) {
                        remisionHolder.imgEstadoFactura.setImageResource(R.drawable.icono_factura_pendiente);
                    } else if (!remision.PendienteAnulacion && remision.Anulada) {
                        remisionHolder.imgEstadoFactura.setImageResource(R.drawable.icono_factura_anulada);
                    } else if (!remision.PendienteAnulacion && !remision.Anulada) {
                        remisionHolder.imgEstadoFactura.setImageResource(R.drawable.icono_factura_descargada);
                    }
                }

                remisionHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
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
                return new ViewHolderFecha(v);
            case VIEW_HOLDER_TYPE_FACTURA:
                CardView cvf = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_remision, parent, false);
                return new ViewHolderRemision(cvf);
            case VIEW_HOLDER_TYPE_FOOTER:
                LinearLayout l = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_backtotop, parent, false);
                return new ViewHolderFooter(l);
        }

        throw new RuntimeException("No existe un tipo igual a " + viewType + " + para las remisiones");
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
