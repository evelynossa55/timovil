package com.cm.timovil2.bl.printers.printer_v2;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.utilities.Utilities;
import com.cm.timovil2.front.ActivityBase;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 25/07/18.
 */

public class PrinterFactory {

    public static Printer getPrinter(String macAdrress, ActivityBase context, int numeroCopias){

        String type = App.obtenerPreferencias_TipoImpresora(context);
        String pulgadas = App.obtenerPreferencias_PulgadasImpresion(context);
        //ActivityBase.resolucion.IdCliente = Utilities.ID_COMESCOL;
        switch (type){
            case Printer.GENERIC_TYPE:
                return new PrinterGeneric(macAdrress, context, numeroCopias, pulgadas);
            case Printer.RPP_TYPE:
                return new PrinterRPP(macAdrress, context, numeroCopias, pulgadas);
            case Printer.ZEBRA_TYPE:
                return new PrinterZEBRA(macAdrress, context, numeroCopias, pulgadas);
            case Printer.CITIZEN_TYPE:
                return new PrinterCITIZEN(macAdrress, context, numeroCopias, pulgadas);
            default:
                return new PrinterGeneric(macAdrress, context, 1, pulgadas);
        }
    }
}
