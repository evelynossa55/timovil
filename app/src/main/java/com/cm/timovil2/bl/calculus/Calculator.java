package com.cm.timovil2.bl.calculus;

import com.cm.timovil2.dto.ResolucionDTO;

public abstract class Calculator {

    ResolucionDTO mResolucion;
    Calculable mCalculable;

    Calculator(ResolucionDTO resolucion, Calculable calculable){
        mResolucion = resolucion;
        mCalculable = calculable;
    }

    public abstract Calculable convert(String toCalculableType);
}
