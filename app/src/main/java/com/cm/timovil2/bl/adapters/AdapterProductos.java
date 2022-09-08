package com.cm.timovil2.bl.adapters;

import java.util.ArrayList;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.DetalleFacturaDTO;
import com.cm.timovil2.dto.ResolucionDTO;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AdapterProductos extends ArrayAdapter<DetalleFacturaDTO> {

    private final Activity context;
    private final ArrayList<DetalleFacturaDTO> datos;
    private ResolucionDTO resolucionDTO;

    public AdapterProductos(Activity _context, ArrayList<DetalleFacturaDTO> _datos) {
        super(_context, R.layout.listitem_productos, _datos);
        context = _context;
        datos = _datos;
        try {
            resolucionDTO = new ResolucionDAL(context).ObtenerResolucion();
            setIndexes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public
    @NonNull
    View getView(int position, View item, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        if (item == null) item = inflater.inflate(R.layout.listitem_productos, parent, false);

        TextView lblNombreProducto = item.findViewById(R.id.lblNombreProducto);

        String codigo = resolucionDTO != null ? resolucionDTO.IdCliente : "";

        DetalleFacturaDTO detalle = datos.get(position);

        if (codigo.equals(Utilities.ID_DOBLEVIA)
                || codigo.equals(Utilities.ID_PRUEBAS_TIMOVIL)
                || codigo.equals(Utilities.ID_PRUEBAS_CMPRUEBAS)) {
            lblNombreProducto.setText(detalle.Nombre);
        } else {
            String nomProducto = detalle.Codigo + ". " + detalle.Nombre;
            lblNombreProducto.setText(nomProducto);
        }

        TextView lblInventarioProducto = item.findViewById(R.id.lblInventarioProducto);
        String inv = "Disponible: " + detalle.StockDisponible;
        lblInventarioProducto.setText(inv);

        return (item);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    private void setIndexes(){

        for (int i = 0; i < datos.size(); i++) {
            datos.get(i).Index = i;
        }
    }
}