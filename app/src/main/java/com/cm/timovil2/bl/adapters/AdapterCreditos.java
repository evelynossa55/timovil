package com.cm.timovil2.bl.adapters;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
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

import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.R;
import com.cm.timovil2.data.AbonoFacturaDAL;
import com.cm.timovil2.dto.wsentities.MFactCredito;

public class AdapterCreditos extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_HOLDER_TYPE_FACTURA = 1;
    private static final int VIEW_HOLDER_TYPE_FOOTER = 2;

    private final ActivityBase context;
    private final ArrayList<MFactCredito> items;
    private int position;
    private OnRecyclerViewItemClickListener onClickListener;

    public AdapterCreditos(ActivityBase _context, ArrayList<MFactCredito> _datos) {
        context = _context;
        items = _datos;

    }

    static class ViewHolderFooter extends RecyclerView.ViewHolder {

        LinearLayout layout;

        ViewHolderFooter(View view) {
            super(view);
            layout = (LinearLayout) view;
        }
    }

    static class ViewHolderFactura extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        CardView cardView;
        TextView lblNumeroFactura;
        TextView tvDescuento;
        TextView tvDevolucion;
        TextView tvRetefuente;
        TextView tvSubtotal;
        TextView tvReteiva;
        TextView tvIva;
        TextView tvIpoconsumo;
        TextView tvTotal;
        ImageView imgEstadoFactura;
        Context context;

        ViewHolderFactura(View view) {
            super(view);
            cardView = (CardView) view;
            lblNumeroFactura = view.findViewById(R.id.lblNumeroFactura);
            tvDescuento = view.findViewById(R.id.tv_descuento);
            tvDevolucion = view.findViewById(R.id.tv_devolucion);
            tvRetefuente = view.findViewById(R.id.tv_retefuente);
            tvSubtotal = view.findViewById(R.id.tv_subtotal);
            tvReteiva = view.findViewById(R.id.tv_reteiva);
            tvIva = view.findViewById(R.id.tv_iva);
            tvIpoconsumo = view.findViewById(R.id.tv_ipoconsumo);
            tvTotal = view.findViewById(R.id.tv_total);
            imgEstadoFactura = view.findViewById(R.id.imgEstadoFactura);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Opciones de factura");
            menu.add(Menu.NONE, 1, 1, "Registrar pago");
            menu.add(Menu.NONE, 2, 2, "Imprimir");
        }
    }

    public void setOnClickListener(OnRecyclerViewItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == items.size()) {
            // This is where we'll add footer.
            return VIEW_HOLDER_TYPE_FOOTER;
        }

        return VIEW_HOLDER_TYPE_FACTURA;
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolderFooter) {
            final ViewHolderFooter viewHolderFooter = (ViewHolderFooter) holder;
            viewHolderFooter.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.onItemClick(view, viewHolderFooter.getAdapterPosition());
                }
            });

            return;
        }

        final MFactCredito factCredito = items.get(position);
        ViewHolderFactura holderFactura = (ViewHolderFactura) holder;

        String numFac = factCredito.NumeroFactura + " " + factCredito.RazonSocial;
        holderFactura.lblNumeroFactura.setText(numFac);

        if (factCredito.Anulada) {
            holderFactura.tvDescuento.setText("");
            holderFactura.tvDevolucion.setText("");
            holderFactura.tvRetefuente.setText("");
            holderFactura.tvSubtotal.setText("");
            holderFactura.tvReteiva.setText("");
            holderFactura.tvIva.setText("");
            holderFactura.tvIpoconsumo.setText("");
            holderFactura.tvTotal.setText("");
        } else {
            //NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat formatter = new DecimalFormat("###,###.##");
            holderFactura.tvDescuento.setText(formatter.format(factCredito.Descuento));
            holderFactura.tvDevolucion.setText(formatter.format(factCredito.Devolucion));
            holderFactura.tvRetefuente.setText(formatter.format(factCredito.Retefuente));
            holderFactura.tvSubtotal.setText(formatter.format(factCredito.Subtotal));
            holderFactura.tvReteiva.setText(formatter.format(factCredito.ReteIva));
            holderFactura.tvIva.setText(formatter.format(factCredito.Iva));
            holderFactura.tvIpoconsumo.setText(formatter.format(factCredito.IpoConsumo));
            holderFactura.tvTotal.setText(formatter.format(factCredito.Total));
        }

        try {
            if (new AbonoFacturaDAL(context).tieneAbonosPendientes(factCredito.IdFactura)) {
                holderFactura.imgEstadoFactura.setImageResource(R.drawable.icono_factura_pendiente);
            } else {
                holderFactura.imgEstadoFactura.setImageResource(R.drawable.icono_factura_descargada);
            }
        } catch (Exception e) {
            holderFactura.imgEstadoFactura.setImageResource(android.R.drawable.stat_notify_error);
        }

        holderFactura.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });

    }

    @Override
    public @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        switch (viewType) {
            case VIEW_HOLDER_TYPE_FACTURA:
                CardView cvf = (CardView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_factura, parent, false);
                return new ViewHolderFactura(cvf);
            case VIEW_HOLDER_TYPE_FOOTER:
                LinearLayout l = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_backtotop, parent, false);
                return new ViewHolderFooter(l);
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