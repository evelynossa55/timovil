package com.cm.timovil2.bl.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.PedidoCallcenterDTO;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 23/01/17.
 */
public class AdapterPedidos extends ArrayAdapter<PedidoCallcenterDTO> {

    private final ArrayList<PedidoCallcenterDTO> items;
    private final LayoutInflater vi;
    private final Context context;

    public AdapterPedidos(Context context, ArrayList<PedidoCallcenterDTO> items) {
        super(context,0, items);
        this.items = items;
        this.context = context;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            v = vi.inflate(R.layout.list_item_pedido_callcenter, parent, false);
        }

        TextView lblCliente = v.findViewById(R.id.lblCliente);
        TextView lblComentario = v.findViewById(R.id.lblComentario);

        final PedidoCallcenterDTO i = items.get(position);
        if (i != null) {
            try {

                ClienteDTO cliente = new ClienteDAL(context).ObtenerClientePorIdCliente(String.valueOf(i.IdCliente));
                lblCliente.setText(cliente.NombreComercial);
                lblComentario.setText(i.Comentario);
                String f[] = i.FechaSolicitada.split("\\.");

                DateTime fechaPedido = new DateTime(
                        Integer.valueOf(f[0]),
                        Integer.valueOf(f[1]),
                        Integer.valueOf(f[2]),
                        Integer.valueOf(f[3]),
                        Integer.valueOf(f[4]),
                        Integer.valueOf(f[5])
                );
                DateTime actual = new DateTime();

                int minutes = Minutes.minutesBetween(actual, fechaPedido).getMinutes();

                int color;
                if(minutes <= 0){
                    //RED
                    color = Color.RED;
                }else if(minutes <= 60){
                    //BLUE
                    color = Color.BLUE;
                }else{
                    //GREEN
                    color = Color.GREEN;
                }

                v.setBackgroundColor(color);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return v;
    }
    
}