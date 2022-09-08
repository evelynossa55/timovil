package com.cm.timovil2.bl.utilities;

public class ItemEntry implements Item {

    public final String text;
    public final String number;

    public ItemEntry(String text, String number) {
        this.text = text;
        this.number = number;
    }

    @Override
    public boolean isSection() {
        return false;
    }
}
