package com.cm.timovil2.bl.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cm.timovil2.R;
import com.cm.timovil2.bl.utilities.Item;
import com.cm.timovil2.bl.utilities.ItemEntry;
import com.cm.timovil2.bl.utilities.ItemSection;

import java.util.ArrayList;

public class AdapterEntry extends ArrayAdapter<Item>{
    private final ArrayList<Item> items;
    private final LayoutInflater vi;

    public AdapterEntry(Context context, ArrayList<Item> items) {
        super(context,0, items);
        this.items = items;
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        final Item i = items.get(position);
        if (i != null) {
            if(i.isSection()){
                ItemSection si = (ItemSection)i;
                v = vi.inflate(R.layout.listview_item_section, parent, false);
                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);
                final TextView sectionView =  v.findViewById(R.id.layout_item_section_text);
                sectionView.setText(si.getTitle());
            }else{
                ItemEntry ei = (ItemEntry)i;
                v = vi.inflate(R.layout.listview_item_entry, parent, false);
                final TextView entrada = v.findViewById(R.id.layout_item_entry);
                final TextView value = v.findViewById(R.id.layout_item_entry_val);
                if (entrada != null)
                    entrada.setText(ei.text);
                if(value != null)
                    value.setText(ei.number);
            }
        }
        return v;
    }
}
