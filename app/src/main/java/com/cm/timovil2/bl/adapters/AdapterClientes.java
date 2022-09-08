package com.cm.timovil2.bl.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.OnRecyclerViewItemClickListener;
import com.cm.timovil2.dto.ClienteDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AdapterClientes extends RecyclerView.Adapter<AdapterClientes.ViewHolder> {

    private final Activity context;
    private final ArrayList<ClienteDTO> datos;
    private final Calendar calendar;
    private OnRecyclerViewItemClickListener onClickListener;

    public AdapterClientes(Activity _context, ArrayList<ClienteDTO> _datos) {
        context = _context;
        datos = _datos;
        calendar = new GregorianCalendar();
    }

    public void setOnClickListener(OnRecyclerViewItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView lblRazonSocial;
        TextView lblNegocio;
        TextView lblIdentificacion;
        TextView lblDireccion;
        TextView lblTelefono;

        ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            lblRazonSocial = view.findViewById(R.id.lblRazonSocial);
            lblNegocio = view.findViewById(R.id.lblNegocio);
            lblIdentificacion = view.findViewById(R.id.lblIdentificacion);
            lblDireccion = view.findViewById(R.id.lblDireccion);
            lblTelefono = view.findViewById(R.id.lblTelefono);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final @NonNull AdapterClientes.ViewHolder holder, int position) {
        ClienteDTO cliente = datos.get(position);

        String razonSocial = cliente.Orden + ". "
                + cliente.RazonSocial;
        holder.lblRazonSocial.setText(razonSocial.trim());

        String nombreComercial = (!TextUtils.isEmpty(cliente.NombreComercial)?cliente.NombreComercial:"---");
        holder.lblNegocio.setText(nombreComercial);

        String identificacionCliente = cliente.Identificacion;
        holder.lblIdentificacion.setText(identificacionCliente);

        String direccionCliente =
                (TextUtils.isEmpty(cliente.DireccionEntrega)
                        || cliente.DireccionEntrega.equals("NULL")?
                        cliente.Direccion: cliente.DireccionEntrega);
        holder.lblDireccion.setText(direccionCliente);

        String telefonoCliente = cliente.Telefono1;
        holder.lblTelefono.setText(telefonoCliente);

        String fecha = (calendar.get(Calendar.DAY_OF_MONTH)) + "/"
                        + (calendar.get(Calendar.MONTH) + 1) + "/"
                        + (calendar.get(Calendar.YEAR));

        Drawable round_check_white = ContextCompat.getDrawable(context, R.drawable.round_check_circle_white_18);
        if (cliente.Atendido.equals(fecha)) {
            holder.lblRazonSocial.setCompoundDrawablesWithIntrinsicBounds(null, null, round_check_white, null);
        } else {
            holder.lblRazonSocial.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onItemClick(view, holder.getAdapterPosition());
            }
        });

        /*
        TextView lblNegocio =  item.findViewById(R.id.lblNegocio);
        String negocio;
        if (datos.get(position).Identificacion.length() <= 1) {
            negocio = (datos.get(position).Credito ? "CREDITO" : "CONTADO");
        } else {
            negocio = " ID: " + (datos.get(position).Identificacion);
        }
        */
    }

    // Create new views (invoked by the layout manager)
    @Override
    public @NonNull AdapterClientes.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                    int viewType) {
        // create a new view
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_cliente, parent, false);
        return new AdapterClientes.ViewHolder(cv);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return datos != null ? datos.size() : 0;
    }

    public ClienteDTO getClient(int position){
        if(datos != null && position < datos.size()){
            return datos.get(position);
        }
        return null;
    }
}