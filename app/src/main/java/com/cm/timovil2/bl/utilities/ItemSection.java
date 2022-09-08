package com.cm.timovil2.bl.utilities;

public class ItemSection implements Item {

    private final String title;

    public ItemSection(String title) {
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    @Override
    public boolean isSection() {
        return true;
    }
}
