package com.cm.timovil2.bl.adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.dto.GestionComercialDTO;

import java.util.ArrayList;

/*
 * Created by JORGE ANDRES DAVID CARDONA on 24/08/2015.
 */
public class AdapterGestionesComerciales extends ArrayAdapter<GestionComercialDTO>{

    private final Activity context;
    private final ArrayList<GestionComercialDTO> datos;
    private Filter filter;

    public AdapterGestionesComerciales(Activity _context, ArrayList<GestionComercialDTO> _datos) {
        super(_context, R.layout.listitem_gestiones_comerciales, _datos);
        context = _context;
        datos = _datos;
    }

    public @NonNull View getView(int position, View item, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (item == null) {
            item = inflater.inflate(R.layout.listitem_gestiones_comerciales, parent, false);
        }

        TextView lblContacto = item.findViewById(R.id.lblContacto);
        TextView lblDatosContacto = item.findViewById(R.id.lblDatosContacto);
        ImageView imgEstadoGestion = item.findViewById(R.id.imgEstadoGestion);
        lblContacto.setText(datos.get(position).Contacto);
        String datosContacto = "Tel: " + datos.get(position).TelContacto + " - " +
                Utilities.Fecha(datos.get(position).FechaHora);
        lblDatosContacto.setText(datosContacto);
        if (!datos.get(position).Sincronizada) {
            imgEstadoGestion.setImageResource(R.drawable.icono_factura_pendiente);
        } else {
            imgEstadoGestion.setImageResource(R.drawable.icono_factura_descargada);
        }

        return item;
    }

    @Override
    public @NonNull Filter getFilter() {
        if (filter == null)
            filter = new FacturaFilter();
        return filter;
    }

    private class FacturaFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // NOTE: this function is *always* called from a background thread, and not the UI thread.
            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<GestionComercialDTO> filt = new ArrayList<>();
                for (GestionComercialDTO c: datos) {
                    if (c.CodigoRuta.toLowerCase().contains(constraint)
                            || c.Contacto.toLowerCase().contains(constraint)
                            || c.TelContacto.toLowerCase().contains(
                            constraint)) {
                        filt.add(c);
                    }
                }
                result.count = filt.size();
                result.values = filt;
            } else {
                synchronized (this) {
                    result.values = datos;
                    result.count = datos.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<GestionComercialDTO> filtered = (ArrayList<GestionComercialDTO>) results.values;
            notifyDataSetChanged();
            clear();
            for (GestionComercialDTO factura: filtered)
                add(factura);
            notifyDataSetInvalidated();
        }

    }
}
