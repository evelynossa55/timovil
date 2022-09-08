package com.cm.timovil2.sincro;

import com.cm.timovil2.front.ActivityBase;

/**
 * CREADO POR JORGE ANDRES DAVID CARDONA EL 5/06/18.
 */

interface ISincroData {

    /**
     * It should be called async
     * @param context ActivityBase child
     * @throws Exception if error
     */
    void download(ActivityBase context) throws Exception;
}
