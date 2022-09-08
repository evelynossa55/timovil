package com.cm.timovil2.proxy;

import java.util.ArrayList;

import org.ksoap2.serialization.SoapObject;

import android.content.Context;
import android.util.Log;

import com.cm.timovil2.data.ClienteDAL;
import com.cm.timovil2.data.DescuentoDAL;
import com.cm.timovil2.data.ResolucionDAL;
import com.cm.timovil2.dto.ClienteDTO;
import com.cm.timovil2.dto.DescuentoDTO;
import com.cm.timovil2.dto.ResolucionDTO;

public class ProxyCliente extends ProxyWS {

    private final Context contexto;

    public ProxyCliente(Context _contexto) {
        METHOD_NAME = MetodosWeb.OBTENER_RUTERO;
        contexto = _contexto;
    }

    @Override
    SoapObject GetRequest() {
        SoapObject so = new SoapObject(NAMESPACE, METHOD_NAME);
        // so.addProperty("_dia", dia);
        ResolucionDTO resolucion = new ResolucionDAL(this.contexto).ObtenerResolucion();
        so.addProperty("_codigoRuta", resolucion.CodigoRuta);
        so.addProperty("_idCliente", resolucion.IdCliente);
        return so;
    }

    @Override
    <T> T InvocarWS() throws Exception {
        int diaRutero, posicionRutero, idCliente;
        try {
            SoapObject listadoRutero = InvokeMethod();
            ClienteDAL clienteDal = new ClienteDAL(contexto);
            clienteDal.Eliminar();
            DescuentoDAL descuentoDal = new DescuentoDAL(contexto);
            descuentoDal.Eliminar();
            ArrayList<ClienteDTO> l = new ArrayList<>();

            String[] diaPosicion;

            for (int i = 0; i < listadoRutero.getPropertyCount(); i++) {

                SoapObject soap = (SoapObject) listadoRutero.getProperty(i);
                SoapObject descuentos = (SoapObject) soap.getProperty(0);
                String info = soap.getProperty(1).toString();
                String rutero = soap.getProperty(2).toString();

                String separacionInfo[] = info.split("\\|");
                String separacionRutero[] = rutero.split("\\|");

                idCliente = Integer.valueOf(separacionInfo[0]);
                for (String separacion : separacionRutero) {
                    try {
                        diaPosicion = separacion.split(",");
                        diaRutero = Integer.parseInt(diaPosicion[0]);
                        posicionRutero = Integer.parseInt(diaPosicion[1]);

                        ClienteDTO oCliente = new ClienteDTO();

                        oCliente.Dia = diaRutero;
                        oCliente.Orden = posicionRutero;
                        //    0  |      1       |     2     |   3   |    4    |    5    |    6
                        //  IdCli|Identificacion|RazonSocial|Negocio|Direccion|Telefonos|Ubicacion

                        //|        7         |       8     |  9  |       10         |      11      |    12   |       13         | 14 |   15         | 16         |17              |   18
                        //|TipoIdentificacion|CodigoRegimen|Email|DescuentoComercial|Plazo créditos|Retenedor|Codigo del cliente|CREE|PorcentajeCREE|ListaPrecios|CarteraPendiente|Remisiones

                        oCliente.IdCliente = idCliente;
                        oCliente.Identificacion = separacionInfo[1];

                        oCliente.RazonSocial = separacionInfo[2];
                        oCliente.NombreComercial = separacionInfo[3];
                        Log.d("ProxyCliente", oCliente.RazonSocial + " - " + oCliente.NombreComercial + " - " + oCliente.IdCliente);

                        oCliente.Direccion = separacionInfo[4];
                        String[] telefonos = separacionInfo[5].split(",");
                        if (telefonos != null && telefonos.length > 0 && telefonos[0] != null
                                && !telefonos[0].equals("")) {
                            oCliente.Telefono1 = telefonos[0];
                        } else {
                            oCliente.Telefono1 = "PENDIENTE";
                        }
                        if (telefonos != null && telefonos.length == 2 && telefonos[1] != null
                                && !telefonos[1].equals("")) {
                            oCliente.Telefono2 = telefonos[1];
                        } else {
                            oCliente.Telefono2 = oCliente.Telefono1;
                        }
                        oCliente.Ubicacion = separacionInfo[6];
                        oCliente.Plazo = Integer.valueOf(separacionInfo[11].trim());
                        oCliente.ReteFuente = separacionInfo[12].equals("1");

                        oCliente.ListaPrecios = Integer.valueOf(separacionInfo[16].trim());

                        if (separacionInfo.length >= 18) {
                            oCliente.CarteraPendiente = separacionInfo[17].trim();
                            if (separacionInfo.length >= 19) {
                                oCliente.Remisiones = separacionInfo[18].trim();
                                if (separacionInfo.length >= 20) {
                                    oCliente.Credito = (separacionInfo[19].trim().equals("1"));
                                    if (separacionInfo.length >= 21) {
                                        try {
                                            oCliente.IdListaPrecios = Integer.parseInt(separacionInfo[20].trim());
                                        } catch (Exception e) {
                                            oCliente.IdListaPrecios = 0;
                                        }
                                        if (separacionInfo.length >= 22) {
                                            oCliente.Remision = (separacionInfo[21].trim().equals("1"));
                                            if (separacionInfo.length >= 23) {
                                                oCliente.ValorVentasMes = (separacionInfo[22].trim());

                                                if (separacionInfo.length >= 24) {
                                                    oCliente.FormaPagoFlexible = (separacionInfo[23].trim().equals("1"));

                                                    if (separacionInfo.length >= 25) {

                                                        oCliente.ExentoIva = (separacionInfo[24].trim().equals("1"));

                                                        if (separacionInfo.length >= 26) {
                                                            oCliente.DireccionEntrega = (separacionInfo[25].trim());

                                                            if (separacionInfo.length >= 27) {
                                                                oCliente.ReteIva = (separacionInfo[26].trim().equals("1"));

                                                                if(separacionInfo.length >= 28){
                                                                    oCliente.ObligatorioCodigoBarra = (separacionInfo[27].trim().equals("1"));

                                                                    if(separacionInfo.length >= 29){
                                                                        oCliente.FacturacionElectronicaCliente = (separacionInfo[28].trim().equals("1"));

                                                                        if(separacionInfo.length >= 30){
                                                                            oCliente.FacturacionPOSCliente = (separacionInfo[29].trim().equals("1"));

                                                                            if(separacionInfo.length >= 31){
                                                                                oCliente.LatitudCodBarras = (separacionInfo[30].trim());
                                                                            }
                                                                                if(separacionInfo.length >= 32){
                                                                                    oCliente.LongitudCodBarras = (separacionInfo[31].trim());
                                                                                }
                                                                        }

                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        oCliente.Atendido = "No";
                        l.add(oCliente);

                        // Insertamos el cliente
                        try {
                            clienteDal.Insertar(oCliente);
                        } catch (Exception e) {
                            Log.d("ProxyCliente", idCliente + "|" + e.getMessage());
                            throw new Exception(idCliente + "|" + e.getMessage());
                        }

                    } catch (Exception e) {
                        Log.d("ProxyCliente", idCliente + "|" + e.getMessage());
                        throw new Exception(idCliente + "|" + e.getMessage());
                    }
                }

                // Insertamos los descuentos
                for (int j = 0; j < descuentos.getPropertyCount(); j++) {
                    try {
                        SoapObject descuento = (SoapObject) descuentos.getProperty(j);
                        DescuentoDTO d = new DescuentoDTO();
                        d.IdCliente = idCliente;
                        d.IdProducto = Integer.parseInt(descuento.getProperty(0).toString());
                        d.Porcentaje = Float.valueOf(descuento.getProperty(1).toString());
                        descuentoDal.Insertar(d);
                    } catch (Exception e) {
                        Log.d("ProxyCliente", idCliente + "|" + e.getMessage());
                        throw new Exception(idCliente + "|" + e.getMessage());
                    }
                }
            }
            return (T) l;

        } catch (Exception e) {
            Log.d("ProxyCliente", e.getLocalizedMessage());
            throw new Exception(e.getMessage());
        }
    }

    public ArrayList<ClienteDTO> ObtenerClientes() throws Exception {
        return InvocarWS();
    }
}