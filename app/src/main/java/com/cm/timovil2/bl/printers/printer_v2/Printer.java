package com.cm.timovil2.bl.printers.printer_v2;

import android.bluetooth.BluetoothSocket;

import com.cm.timovil2.dto.FacturaDTO;
import com.cm.timovil2.dto.NotaCreditoFacturaDTO;
import com.cm.timovil2.dto.RemisionDTO;
import com.cm.timovil2.dto.ResumenVentasImpresionDTO;
import com.cm.timovil2.front.ActivityBase;

import java.io.OutputStream;
import java.util.List;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 24/07/18.
 */

public abstract class Printer {

    static final String GENERIC_TYPE = "GENERICA";
    static final String CITIZEN_TYPE = "CITIZEN";
    static final String ZEBRA_TYPE = "ZEBRA";
    static final String RPP_TYPE = "RPP";

    static final String ERROR_CONNECTION_FAILED = "No se pudo conectar con la impresora, verifique que ésta se encuentra encendida.";
    static final String ERROR = "No se pudo conectar con la impresora";

    ActivityBase mContext;
    BluetoothSocket mBtsocket;
    OutputStream mOutputStream;
    String mMacAddress;
    boolean mIsConnected = false;
    int mNumeroCopias;
    String mPulgadas;

    Printer(String macAdrress, ActivityBase context, int numeroCopias, String pulgadas){
        this.mMacAddress = macAdrress;
        this.mContext = context;
        this.mNumeroCopias = numeroCopias;
        this.mPulgadas = pulgadas;
    }

    abstract void connect() throws Exception;

    public abstract void printConfig() throws Exception;

    public abstract void print(FacturaDTO facturaDTO) throws Exception;

    public abstract void print(RemisionDTO remisionDTO) throws Exception;

    public abstract void print(NotaCreditoFacturaDTO notaCreditoFacturaDTO) throws Exception;

    public abstract void print(ResumenVentasImpresionDTO resumen) throws Exception;

    public abstract void printFacturas(List<FacturaDTO> facturasDTO) throws Exception;

    public abstract void printRemisiones(List<RemisionDTO> remisionesDTO) throws Exception;

    public abstract void printFacturaComoRemision(FacturaDTO facturaDTO) throws Exception;

    public abstract void printInventario() throws Exception;

}
