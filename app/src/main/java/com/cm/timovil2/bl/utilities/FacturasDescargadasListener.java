package com.cm.timovil2.bl.utilities;


class FacturasDescargadasListener {

    final private FacturasDescargadasCallback callback;

    FacturasDescargadasListener(FacturasDescargadasCallback callback){
        this.callback = callback;
    }

    void actualizarFacturasUI(){
        callback.actualizarFacturas();
    }
}
