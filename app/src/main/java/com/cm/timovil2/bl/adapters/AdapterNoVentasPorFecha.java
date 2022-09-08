package com.cm.timovil2.bl.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemFacturaHeader;
import com.cm.timovil2.bl.utilities.ItemNoVentaDetail;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.data.ResultadoGestionCasoCallcenterDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaDTO;
import com.cm.timovil2.dto.GuardarMotivoNoVentaPedidoDTO;
import com.cm.timovil2.dto.ResultadoGestionCasoCallcenterDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 14/06/17.
 */
public class AdapterNoVentasPorFecha extends ArrayAdapter<Item> {

    private final ArrayList<Item> items;
    private final LayoutInflater vi;
    private final ResultadoGestionCasoCallcenterDAL dal;

    public AdapterNoVentasPorFecha(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        this.items = items;
        dal = new ResultadoGestionCasoCallcenterDAL(context);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public
    @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View v = convertView;
        final Item i = items.get(position);
        if (i != null) {
            if (i.isSection()) {

                v = vi.inflate(R.layout.listview_item_section, parent, false);
                SimpleDateFormat dt = new SimpleDateFormat("EEE, dd MMM yyyy", Utilities.getLocale());
                ItemFacturaHeader fh = (ItemFacturaHeader) i;
                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);
                final TextView sectionView = v.findViewById(R.id.layout_item_section_text);
                sectionView.setText(dt.format(fh.getFechaFactura()));
                sectionView.setTextColor(Color.parseColor("#298BD5"));

            } else {

                ItemNoVentaDetail fd = (ItemNoVentaDetail) i;
                v = vi.inflate(R.layout.lista_item_noventa, parent, false);

                GuardarMotivoNoVentaDTO dto;
                GuardarMotivoNoVentaPedidoDTO dtop;
                ClienteDTO cliente;

                TextView lblCliente = v.findViewById(R.id.lblCliente);
                TextView lblMotivo = v.findViewById(R.id.lblMotivo);
                TextView lblDesc = v.findViewById(R.id.lblDesc);
                ImageView imgEstadoFactura = v.findViewById(R.id.imgEstadoFactura);

                boolean sincronizada;
                String titleCliente;
                String titleMotivo;
                String titleDescripcion;
                cliente = fd.getCliente();
                titleCliente = cliente.RazonSocial + " " + cliente.NombreComercial;

                if (!fd.isPedido()) {
                    dto = fd.getMotivoNvDTO();
                    sincronizada = dto.Sincronizada;
                    titleMotivo = dto.Motivo;
                    titleDescripcion = dto.Descripcion;
                } else {
                    dtop = fd.getMotivoNvpDTO();
                    sincronizada = dtop.Sincronizada;
                    ResultadoGestionCasoCallcenterDTO r =
                            dal.Obtener(dtop.IdResultadoGestion);
                    titleMotivo = r != null ? r.Nombre : "";
                    titleDescripcion = dtop.Descripcion;
                }

                lblCliente.setText(titleCliente);
                lblMotivo.setText(titleMotivo);
                lblDesc.setText(titleDescripcion);

                if (!sincronizada) {
                    imgEstadoFactura.setImageResource(R.drawable.icono_factura_pendiente);
                } else {
                    imgEstadoFactura.setImageResource(R.drawable.icono_factura_descargada);
                }
            }
        }
        return v;
    }
}

