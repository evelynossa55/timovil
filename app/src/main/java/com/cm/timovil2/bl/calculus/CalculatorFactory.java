package com.cm.timovil2.bl.calculus;

import com.cm.timovil2.dto.ResolucionDTO;

public class CalculatorFactory {

    public static Calculator getCalculator(ResolucionDTO resolucionDTO, Calculable calculable){

        switch (calculable.getType()){
            case Calculable.FACTURA_TYPE:
                return new CalculatorFactura(resolucionDTO, calculable);
            default:
                return null;
        }
    }
}
