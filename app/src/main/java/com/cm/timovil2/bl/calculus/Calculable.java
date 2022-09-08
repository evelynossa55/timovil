package com.cm.timovil2.bl.calculus;

public abstract class Calculable {

    public final static String REMISION_TYPE = "R";
    public final static String FACTURA_TYPE = "F";

    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
