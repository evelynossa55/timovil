package com.cm.timovil2.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;

import com.cm.timovil2.front.ActivityBase;
import com.cm.timovil2.dto.wsentities.MFactCredito;

import org.joda.time.DateTime;

public class FacturaCreditoDAL extends DAL {

	private ActivityBase contexto;

	public FacturaCreditoDAL(ActivityBase context) {
		super(context, DAL.TablaFacturaCredito);
		contexto = context;
	}
	
	public void Insertar(MFactCredito f){
		ContentValues values = new ContentValues();
		
		values.put("IdFactura", f.IdFactura);
		values.put("NumeroFactura", f.NumeroFactura);
		values.put("NombreRuta", f.NombreRuta);
		values.put("FechaHora", f.FechaHoraCredito);
		values.put("FormaPago", f.FormaPago);
		values.put("IdCliente", f.IdCliente);
		values.put("Subtotal", f.Subtotal);
		values.put("Descuento", f.Descuento);
		values.put("Retefuente", f.Retefuente);
		values.put("Iva", f.Iva);
		values.put("Total", f.Total);
		values.put("Saldo", f.Saldo);
		values.put("Identificacion", f.Identificacion);
		values.put("RazonSocial", f.RazonSocial);
		values.put("Negocio", f.Negocio);
		values.put("Direccion", f.Direccion);
		values.put("Telefono", f.Telefono);
		values.put("Resolucion", f.Resolucion);

		super.insertar(values);
	}
	
	public int eliminar() {
		return super.eliminar(null);
	}


	private Cursor Obtener(String[] parametrosFiltro) {
		return super.obtener(columnas, null, "Saldo > ?", parametrosFiltro);
	}

    public ArrayList<MFactCredito> obtenerListadoConSaldo(boolean cargarDetalle) throws Exception{
        ArrayList<MFactCredito> lista = new ArrayList<>();
        Cursor cursor;
        String parameters[] = {"1"};
        cursor = this.Obtener(parameters);
        DetalleFacturaCreditoDAL df = new DetalleFacturaCreditoDAL(contexto);
        if (cursor.moveToFirst()) {
            do {
                MFactCredito f = getFromCursor(cursor);
                if (cargarDetalle) {
                    f.DetalleFactura_Credito = df.ObtenerListado(f.IdFactura);
                }
                lista.add(f);

            } while (cursor.moveToNext());
        }
        cursor.close();
        new AbonoFacturaDAL(contexto).contarAbonosPorRangoFechas(DateTime.now(), DateTime.now());
        return lista;
    }
	public ArrayList<MFactCredito> obtenerListadoConFiltros(boolean cargarDetalle,String Parametro) throws Exception{
		ArrayList<MFactCredito> lista = new ArrayList<>();
		Cursor cursor;
		String parameters[] = {"1"};
		cursor = this.Obtener(parameters);
		DetalleFacturaCreditoDAL df = new DetalleFacturaCreditoDAL(contexto);
		if (cursor.moveToFirst()) {
			do {
				MFactCredito f = getFromCursor(cursor);
				if(cargarDetalle){
					if (f.RazonSocial.toUpperCase().contains(Parametro.toUpperCase())||f.NumeroFactura.toUpperCase().contains(Parametro.toUpperCase())) {
						f.DetalleFactura_Credito = df.ObtenerListado(f.IdFactura);
						lista.add(f);
					}
				}


			} while (cursor.moveToNext());
		}
		cursor.close();
		new AbonoFacturaDAL(contexto).contarAbonosPorRangoFechas(DateTime.now(), DateTime.now());
		return lista;
	}

	/*public ArrayList<MFactCredito> obtenerListado(boolean cargarDetalle) throws Exception{
		ArrayList<MFactCredito> lista = new ArrayList<MFactCredito>();
		Cursor cursor;
		cursor = this.Obtener(null, null, null);
		DetalleFacturaCreditoDAL df = new DetalleFacturaCreditoDAL(contexto);
		if (cursor.moveToFirst()) {
			do {
				MFactCredito f = getFromCursor(cursor);
				if (cargarDetalle) {
					f.DetalleFactura_Credito = df.ObtenerListado(f.IdFactura);
				}
				lista.add(f);
			} while (cursor.moveToNext());
		}
		cursor.close();
		new AbonoFacturaDAL(this.contexto).contarAbonosPorDia();
		return lista;
	}*/

	public MFactCredito ObtenerFacturaPorId(int _id) {
		Cursor c = super.obtenerPorId(_id);
		MFactCredito dto = new MFactCredito();
		if (c.moveToFirst()) {
			dto = getFromCursor(c);
		}
		c.close();
		return dto;
	}

	private MFactCredito getFromCursor(Cursor cursor) {
		MFactCredito c = new MFactCredito();
		c._Id = Integer.parseInt(cursor.getString(0));
		
		c.IdFactura = cursor.getString(1);
		c.NumeroFactura = cursor.getString(2);
		c.NombreRuta = cursor.getString(3);
		c.FechaHoraCredito = cursor.getString(4);
		c.FormaPago = cursor.getString(5);
		c.IdCliente = cursor.getInt(6);
		c.Subtotal = cursor.getFloat(7);
		c.Descuento = cursor.getFloat(8);
		c.Retefuente = cursor.getFloat(9);
		c.Iva = cursor.getFloat(10);
		c.Total = cursor.getFloat(11);
		c.Saldo = cursor.getFloat(12);
		c.Identificacion = cursor.getString(13);
		c.RazonSocial = cursor.getString(14);
		c.Negocio = cursor.getString(15);
		c.Direccion = cursor.getString(16);
		c.Telefono = cursor.getString(17);
		c.Resolucion = cursor.getString(18);
		
		return c;
	}

	@Override
	void setColumns() {
		columnas = new String[] { "_id", 
		"IdFactura",
		"NumeroFactura",
		"NombreRuta",
		"FechaHora",
		"FormaPago",
		"IdCliente",
		"Subtotal",
		"Descuento",
		"Retefuente",
		"Iva",
		"Total",
		"Saldo",
		"Identificacion",
		"RazonSocial",
		"Negocio",
		"Direccion",
		"Telefono",
		"Resolucion"};
	}

}