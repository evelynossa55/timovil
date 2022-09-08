package com.cm.timovil2.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.cm.timovil2.dto.ClienteDTO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class ClienteDAL extends DAL {


    public ClienteDAL(Context context) {
        super(context, DAL.TablaCliente);
    }

    public void Insertar(ClienteDTO cliente) {
        ContentValues values = new ContentValues();
        values.put("IdCliente", cliente.IdCliente);
        values.put("Identificacion", cliente.Identificacion);
        values.put("RazonSocial", cliente.RazonSocial);
        values.put("NombreComercial", cliente.NombreComercial);
        values.put("Direccion", cliente.Direccion);
        values.put("Telefono1", cliente.Telefono1);
        values.put("Telefono2", cliente.Telefono2);
        values.put("Dia", cliente.Dia);
        values.put("Orden", cliente.Orden);
        values.put("Retenedor", cliente.ReteFuente ? 1 : 0);
        values.put("RetenedorCREE", cliente.RetenedorCREE ? 1 : 0);
        values.put("PorcentajeCREE", cliente.PorcentajeCree);
        values.put("Atendido", cliente.Atendido);
        values.put("ListaPrecios", cliente.ListaPrecios);
        values.put("ValorVentasMes", (cliente.ValorVentasMes != null ? cliente.ValorVentasMes : "SIN MOVIMIENTOS"));
        values.put("CarteraPendiente", (cliente.CarteraPendiente != null ? cliente.CarteraPendiente : ""));
        values.put("Remisiones", (cliente.Remisiones != null ? cliente.Remisiones : ""));
        values.put("Credito", (cliente.Credito ? "1" : "0"));
        values.put("IdListaPrecios", cliente.IdListaPrecios);
        values.put("Remision", (cliente.Remision ? "1" : "0"));
        values.put("FormaPagoFlexible", cliente.FormaPagoFlexible ? "1" : "0");
        values.put("VecesAtendido", cliente.VecesAtendido);
        values.put("ExentoIva", cliente.ExentoIva ? "1" : "0");
        values.put("DireccionEntrega", cliente.DireccionEntrega);
        values.put("Plazo", cliente.Plazo);
        values.put("Ubicacion", TextUtils.isEmpty(cliente.Ubicacion) ? "-" : cliente.Ubicacion);
        values.put("ReteIva", (cliente.ReteIva ? "1" : "0"));
        values.put("OblicatorioCodigoBarras", (cliente.ObligatorioCodigoBarra ? "1" : "0"));
        values.put("FacturacionElectronicaCliente", (cliente.FacturacionElectronicaCliente ? "1" : "0"));
        values.put("FacturacionPOSCliente", (cliente.FacturacionPOSCliente ? "1" : "0"));
        values.put("Longitud", (cliente.LongitudCodBarras));
        values.put("Latitud", (cliente.LatitudCodBarras));
        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener(String orderBy, String filtro, String[] parametrosFiltro) {
        return super.obtener(columnas, orderBy, filtro, parametrosFiltro);
    }

    private Cursor ObtenerPorDiaRutaAsesor(int dia) {
        String[] args = {String.valueOf(dia)};
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " +
                "_id, " +
                DAL.TablaCliente + ".IdCliente, " +
                "Identificacion, " +
                "RazonSocial, " +
                "NombreComercial, " +
                "Direccion, " +
                "Telefono1, " +
                "Telefono2, " +
                "OblicatorioCodigoBarras,"+
                DAL.TablaCliente + ".Dia, " +
                "Orden, " +
                "Retenedor, " +
                "RetenedorCREE, " +
                "PorcentajeCREE, " +
                "Atendido, " +
                "ListaPrecios, " +
                "CarteraPendiente, " +
                "Remisiones, " +
                "Credito, " +
                "IdListaPrecios, " +
                "Remision, " +
                "ValorVentasMes, " +
                "FormaPagoFlexible, " +
                "VecesAtendido, " +
                "ExentoIva, " +
                "DireccionEntrega, " +
                "Plazo, " +
                "Ubicacion, " +
                "ReteIva, " +
                "OblicatorioCodigoBarras, " +
                "FacturacionElectronicaCliente, " +
                "FacturacionPOSCliente, " +
                "Longitud, " +
                "Latitud" +
        " FROM " + DAL.TablaCliente + ", " + DAL.TablaProgramacionAsesor +
                " WHERE " + DAL.TablaCliente + ".IdCliente = " + DAL.TablaProgramacionAsesor + ".IdCliente " +
                " AND " + DAL.TablaProgramacionAsesor + ".DIA  = ?", args);
    }

    private Cursor ObtenerPorDia(int dia) {
        String filtro = "Dia = ?";
        String[] parametros = {String.valueOf(dia)};
        String orderBy = "Orden ASC";
        return super.obtener(columnas, orderBy, filtro, parametros);
    }

    public ArrayList<ClienteDTO> ObtenerListado(int dia) {
        ArrayList<ClienteDTO> lista = new ArrayList<>();
        Cursor cursor;
        if (dia == 0) { // todos
            cursor = this.Obtener(null, null, null);
        } else {
            cursor = ObtenerPorDia(dia);
        }

        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ArrayList<ClienteDTO> ObtenerListadoAsesor(int dia) {
        ArrayList<ClienteDTO> lista = new ArrayList<>();
        Cursor cursor;
        if (dia == 0) { // todos
            cursor = this.Obtener(null, null, null);
        } else {
            cursor = ObtenerPorDiaRutaAsesor(dia);
        }

        if (cursor.moveToFirst()) {
            do {
                lista.add(getFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public ClienteDTO ObtenerClientePorId(int _id) {
        Cursor c = super.obtenerPorId(_id);
        ClienteDTO dto = new ClienteDTO();
        if (c.moveToFirst()) {
            dto = getFromCursor(c);
        }
        c.close();
        return dto;
    }

    public ClienteDTO ObtenerClientePorIdCliente(String IdCliente) {
        Cursor c = obtenerPorIdCliente(IdCliente);
        ClienteDTO dto = new ClienteDTO();
        if (c.moveToFirst()) {
            dto = getFromCursor(c);
        }
        c.close();
        return dto;
    }

    private Cursor obtenerPorIdCliente(String idCliente) {
        String[] args = {idCliente};
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + DAL.TablaCliente
                + " WHERE IdCliente=?", args);
    }

    private ClienteDTO getFromCursor(Cursor cursor) {
        ClienteDTO c = new ClienteDTO();

        c._Id = Integer.parseInt(cursor.getString(0));
        c.IdCliente = Integer.parseInt(cursor.getString(1));
        c.Identificacion = cursor.getString(2);
        c.RazonSocial = cursor.getString(3);
        c.NombreComercial = cursor.getString(4);
        c.Direccion = cursor.getString(5);
        c.Telefono1 = cursor.getString(6);
        c.Telefono2 = cursor.getString(7);
        c.Dia = cursor.getInt(8);
        c.Orden = cursor.getInt(9);
        c.ReteFuente = cursor.getInt(10) == 1;
        c.RetenedorCREE = cursor.getInt(11) == 1;
        c.PorcentajeCree = cursor.getDouble(12);
        c.Atendido = cursor.getString(13);
        c.ListaPrecios = cursor.getInt(14);
        c.CarteraPendiente = cursor.getString(15);
        c.Remisiones = cursor.getString(16);
        c.Credito = (cursor.getString(17).equals("1"));
        c.IdListaPrecios = cursor.getInt(18);
        c.Remision = (cursor.getString(19).equals("1"));
        c.ValorVentasMes = (cursor.getString(20));
        c.FormaPagoFlexible = (cursor.getString(21).equals("1"));
        c.VecesAtendido = (cursor.getInt(22));
        c.ExentoIva = (cursor.getString(23).equals("1"));
        c.DireccionEntrega = (cursor.getString(24));
        c.Plazo = (cursor.getInt(25));
        c.Ubicacion = cursor.getString(26);
        c.ReteIva = (cursor.getString(27).equals("1"));
        c.ObligatorioCodigoBarra = (cursor.getString(28).equals("1"));
        c.FacturacionElectronicaCliente = (cursor.getString(29).equals("1"));
        c.FacturacionPOSCliente = (cursor.getString(30).equals("1"));
        c.LongitudCodBarras = (cursor.getString(31));
        c.LatitudCodBarras = (cursor.getString(32));
        return c;
    }

    public ArrayList<ClienteDTO> ObtenerListado() {
        return ObtenerListado(0);
    }

    public ArrayList<ClienteDTO> filtrarClientes(
            ArrayList<ClienteDTO> listaOriginal, String filtro, int dia) {
        if (filtro != null && filtro.trim().length() > 0) {
            filtro = filtro.toUpperCase();
            ArrayList<ClienteDTO> nuevaLista = new ArrayList<>();
            if (dia > 0) {
                for (ClienteDTO dto : listaOriginal) {
                    if ((dto.Identificacion.toUpperCase().contains(filtro)
                            || dto.RazonSocial.toUpperCase().contains(filtro)
                            || dto.NombreComercial.toUpperCase().contains(filtro)) && dia == dto.Dia) {
                        nuevaLista.add(dto);
                    }
                }
            } else if (dia == 0) {
                for (ClienteDTO dto : listaOriginal) {
                    if (dto.Identificacion.toUpperCase().contains(filtro)
                            || dto.RazonSocial.toUpperCase().contains(filtro)
                            || dto.NombreComercial.toUpperCase().contains(filtro)) {
                        nuevaLista.add(dto);
                    }
                }
            }
            return nuevaLista;
        } else {
            return listaOriginal;
        }
    }

    //Se actualiza el estado del cliente a [Atendido = fecha de atención]
    public void AtenderCliente(ClienteDTO clienteDTO) throws Exception {
        try {
            Calendar calendar = new GregorianCalendar();
            String fecha =
                    (calendar.get(Calendar.DAY_OF_MONTH)) + "/"
                            + (calendar.get(Calendar.MONTH) + 1) + "/"
                            + (calendar.get(Calendar.YEAR));
            String update = "update "
                    + TablaCliente
                    + " set Atendido = '" + fecha + "', VecesAtendido =  VecesAtendido + 1 WHERE IdCliente = "
                    + clienteDTO.IdCliente;
            executeQuery(update);
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de atención del cliente: "
                    + e.getMessage());
        }
    }

    public void restarVecesAntendido(int idCliente) throws Exception {
        try {
            String update = "update "
                    + TablaCliente
                    + " set VecesAtendido =  VecesAtendido - 1 WHERE IdCliente = "
                    + idCliente;
            executeQuery(update);
        } catch (Exception e) {
            throw new Exception("Error actualizando el estado de atención del cliente: "
                    + e.getMessage());
        }
    }

    @Override
    void setColumns() {
        columnas = new String[]{"_id", "IdCliente", "Identificacion",
                "RazonSocial", "NombreComercial", "Direccion", "Telefono1",
                "Telefono2", "Dia", "Orden", "Retenedor", "RetenedorCREE",
                "PorcentajeCREE", "Atendido", "ListaPrecios", "CarteraPendiente",
                "Remisiones", "Credito", "IdListaPrecios", "Remision",
                "ValorVentasMes", "FormaPagoFlexible", "VecesAtendido",
                "ExentoIva", "DireccionEntrega", "Plazo", "Ubicacion", "ReteIva",
                "OblicatorioCodigoBarras", "FacturacionElectronicaCliente",
                "FacturacionPOSCliente", "Longitud", "Latitud"};
    }
}