package com.cm.timovil2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cm.timovil2.bl.app.App;
import com.cm.timovil2.bl.app.Seguridad.TiposUsuario;
import com.cm.timovil2.dto.ResolucionDTO;

import java.util.Date;

public class ResolucionDAL extends DAL {

    private Context contexto;

    public ResolucionDAL(Context context) {
        super(context, DAL.TablaResolucion);
        contexto = context;
    }

    void IncrementarSiguienteFactura(boolean pos){
        ResolucionDTO dto = ObtenerResolucion();
        if (dto != null) {
            if(pos){
                dto.SiguienteFacturaPOS += 1;
            }else{
                dto.SiguienteFactura += 1;
            }
            Insertar(dto);
        }
    }

    public void DecrementarSiguienteFactura(){
        ResolucionDTO dto = ObtenerResolucion();
        if (dto != null) {
            dto.SiguienteFactura += 1;
            Insertar(dto);
        }
    }

    void IncrementarSiguienteRemision(){
        ResolucionDTO dto = ObtenerResolucion();
        if (dto != null) {
            dto.SiguienteRemision += 1;
            Insertar(dto);
        }
    }

    public void DecrementarSiguienteRemision(){
        ResolucionDTO dto = ObtenerResolucion();
        if (dto != null) {
            dto.SiguienteRemision -= 1;
            Insertar(dto);
        }
    }

    void IncrementarSiguienteNotaCredito(){
        ResolucionDTO dto = ObtenerResolucion();
        if (dto != null) {
            dto.SiguienteNotaCredito += 1;
            Insertar(dto);
        }
    }

    void DecrementarSiguienteNotaCredito(){
        ResolucionDTO dto = ObtenerResolucion();
        if (dto != null) {
            dto.SiguienteNotaCredito -= 1;
            Insertar(dto);
        }
    }

    public void Insertar(ResolucionDTO dto) {
        Eliminar();
        ContentValues values = new ContentValues();
        values.put("_id", 1);
        values.put("IdCliente", dto.IdCliente);
        values.put("IdResolucion", dto.IdResolucion);
        values.put("Regimen", dto.Regimen);
        values.put("RazonSocial", dto.RazonSocial);
        values.put("NombreComercial", dto.NombreComercial);
        values.put("Nit", dto.Nit);
        values.put("Direccion", dto.Direccion);
        values.put("Telefono", dto.Telefono);
        values.put("FacturaInicial", dto.FacturaInicial);
        values.put("FacturaFinal", dto.FacturaFinal);
        values.put("Resolucion", dto.Resolucion);
        values.put("FechaResolucion", dto.FechaResolucion);
        values.put("SiguienteFactura", dto.SiguienteFactura);
        values.put("SiguienteRemision", (dto.SiguienteRemision > 0 ? dto.SiguienteRemision : 1));
        values.put("CodigoRuta", dto.CodigoRuta);
        values.put("NombreRuta", dto.NombreRuta);
        values.put("ClaveAdmin", dto.ClaveAdmin);
        values.put("ClaveVendedor", dto.ClaveVendedor);
        values.put("PrefijoFacturacion", dto.PrefijoFacturacion);
        values.put("DevolucionAfectaVenta", dto.DevolucionAfectaVenta ? 1 : 0);
        values.put("DevolucionRestaInventario", dto.DevolucionRestaInventario ? 1 : 0);
        values.put("RotacionRestaInventario", dto.RotacionRestaInventario ? 1 : 0);
        values.put("MostrarListaPrecios", dto.MostrarListaPrecios ? 1 : 0);
        values.put("DescargarFacturasAuto", dto.DescargarFacturasAuto ? 1 : 0);
        values.put("RedondearValores", dto.RedondearValores ? 1 : 0);
        values.put("NumeroCopias", dto.NumeroCopias);
        values.put("UrlServicioWeb", dto.UrlServicioWeb);
        values.put("PermitirEliminarFacturas", dto.PermitirEliminarFacturas ? 1 : 0);
        values.put("CodigoBodega", dto.CodigoBodega);
        values.put("NombreImpresora", dto.NombreImpresora);
        values.put("TipoImpresora", dto.TipoImpresora);
        values.put("MACImpresora", dto.MACImpresora);
        values.put("ManejarInventario", dto.ManejarInventario);
        values.put("PorcentajeRetefuente", dto.PorcentajeRetefuente);
        values.put("TopeRetefuente", dto.TopeRetefuente);
        values.put("ReportarUbicacionGPS", dto.ReportarUbicacionGPS);
        values.put("Bodegas", dto.Bodegas);
        values.put("Email", dto.Email);
        values.put("ValorMetaMensual", dto.ValorMetaMensual);
        values.put("ValorVentaMensual", dto.ValorVentaMensual);
        values.put("IdResolucionPOS", dto.IdResolucionPOS);
        values.put("SiguienteFacturaPOS", dto.SiguienteFacturaPOS);
        values.put("PrefijoFacturacionPOS", dto.PrefijoFacturacionPOS);
        values.put("FacturaInicialPOS", dto.FacturaInicialPOS);
        values.put("FacturaFinalPOS", dto.FacturaFinalPOS);
        values.put("ResolucionPOS", dto.ResolucionPOS);
        values.put("FechaResolucionPOS", dto.FechaResolucionPOS);
        values.put("DiaCerrado", dto.DiaCerrado ? 1 : 0);
        values.put("FechaCierre", dto.FechaCierre);
        values.put("HoraLimiteFacturacion", dto.HoraLimiteFacturacion);
        values.put("CierreNotificadoServidor", dto.CierreNotificadoServidor);
        values.put("SiguienteNotaCredito", dto.SiguienteNotaCredito);
        super.insertar(values);
    }

    public int Eliminar() {
        return super.eliminar(null);
    }

    private Cursor Obtener() {
        return super.obtenerPorId(1);
    }

    public void CerrarDia() throws Exception {
        String update = "update "
                + TablaResolucion
                + " set DiaCerrado = 1, FechaCierre = " + String.valueOf(new Date().getTime()) + ", "
                + " CierreNotificadoServidor = 0";
        executeQuery(update);
    }

    public void ActualizarNotificacionCierreServidor(boolean notificado) throws Exception {
        String update = "update "
                + TablaResolucion
                + " CierreNotificadoServidor = " + (notificado?"1":"0");
        executeQuery(update);
    }

    public ResolucionDTO ObtenerResolucion(){
        Cursor cursor = Obtener();
        ResolucionDTO r = null;
        if (cursor.moveToFirst()) {
            r = new ResolucionDTO();
            r.IdCliente = cursor.getString(1);
            r.IdResolucion = Integer.valueOf(cursor.getString(2));
            r.Regimen = cursor.getString(3);
            r.RazonSocial = cursor.getString(4);
            r.NombreComercial = cursor.getString(5);
            r.Nit = cursor.getString(6);
            r.Direccion = cursor.getString(7);
            r.Telefono = cursor.getString(8);
            r.FacturaInicial = cursor.getString(9);
            r.FacturaFinal = cursor.getString(10);
            r.Resolucion = cursor.getString(11);
            r.FechaResolucion = cursor.getString(12);
            r.SiguienteFactura = Integer.valueOf(cursor.getString(13));
            r.SiguienteRemision = Integer.valueOf(cursor.getString(14));
            r.CodigoRuta = cursor.getString(15);
            r.NombreRuta = cursor.getString(16);
            r.ClaveAdmin = cursor.getString(17);
            r.ClaveVendedor = cursor.getString(18);
            r.PrefijoFacturacion = cursor.getString(19);
            r.DevolucionAfectaVenta = cursor.getInt(20) == 1;
            r.DevolucionRestaInventario = cursor.getInt(21) == 1;
            r.RotacionRestaInventario = cursor.getInt(22) == 1;
            r.MostrarListaPrecios = cursor.getInt(23) == 1;
            r.DescargarFacturasAuto = cursor.getInt(24) == 1;
            r.RedondearValores = cursor.getInt(25) == 1;
            r.NumeroCopias = Integer.valueOf(cursor.getString(26));
            r.UrlServicioWeb = cursor.getString(27);
            r.PermitirEliminarFacturas = cursor.getInt(28) == 1;
            r.MACImpresora = cursor.getString(29);
            r.NombreImpresora = cursor.getString(30);
            r.TipoImpresora = cursor.getString(31);
            r.CodigoBodega = cursor.getString(32);
            r.ManejarInventario = cursor.getInt(33) == 1;
            r.PorcentajeRetefuente = cursor.getFloat(34);
            r.TopeRetefuente = cursor.getFloat(35);
            r.ReportarUbicacionGPS = cursor.getInt(36) == 1;
            r.Bodegas = cursor.getString(37);
            r.Email = cursor.getString(38);

            r.ValorMetaMensual = cursor.getDouble(cursor.getColumnIndex("ValorMetaMensual"));
            r.ValorVentaMensual = cursor.getDouble(cursor.getColumnIndex("ValorVentaMensual"));

            r.IdResolucionPOS = cursor.getInt(cursor.getColumnIndex("IdResolucionPOS"));
            r.SiguienteFacturaPOS = cursor.getInt(cursor.getColumnIndex("SiguienteFacturaPOS"));
            r.PrefijoFacturacionPOS = cursor.getString(cursor.getColumnIndex("PrefijoFacturacionPOS"));

            r.FacturaInicialPOS = cursor.getString(cursor.getColumnIndex("FacturaInicialPOS"));
            r.FacturaFinalPOS = cursor.getString(cursor.getColumnIndex("FacturaFinalPOS"));
            r.ResolucionPOS = cursor.getString(cursor.getColumnIndex("ResolucionPOS"));
            r.FechaResolucionPOS = cursor.getString(cursor.getColumnIndex("FechaResolucionPOS"));

            r.TipoUsuario = TiposUsuario.Admin;//TODO: OJO, CAMBIAR ESTO

            r.DiaCerrado = cursor.getInt(cursor.getColumnIndex("DiaCerrado")) == 1;
            r.FechaCierre = cursor.getLong(cursor.getColumnIndex("FechaCierre"));
            r.HoraLimiteFacturacion = cursor.getInt(cursor.getColumnIndex("HoraLimiteFacturacion"));
            r.CierreNotificadoServidor = cursor.getInt(cursor.getColumnIndex("DiaCerrado")) == 1;
            r.SiguienteNotaCredito = cursor.getInt(cursor.getColumnIndex("SiguienteNotaCredito"));

            r.PermitirFacturarSinInventario = App.obtenerConfiguracion_PermitirFacturarSinInventario(contexto);
            r.ult_version_aplicacion = App.obtenerConfiguracion_LastVersionAplicacion(contexto);
            r.SincronizarInventario = App.obtenerConfiguracionSincronizarInventario(contexto);
            r.ValorMetaMensual = App.obtenerConfiguracionValorVentasMensual(contexto);
            r.PeridoReporteUbicacion = App.obtenerConfiguracion_periodoReporteUbicacion(contexto);
            r.TipoRuta = App.obtenerConfiguracion_tipoRuta(contexto);
            r.Imprimir = App.obtenerConfiguracion_imprimir(contexto);
            r.ManejarRecaudoCredito = App.obtenerConfiguracion_ManejarRecaudoCredito(contexto);
            r.PermitirCambiarFormaDePago = App.obtenerConfiguracion_PermitirCambiarFormaDePago(contexto);
            r.CantidadFacturasClientePorDia = App.obtenerConfiguracion_CantidadFacturasClientePorDia(contexto);
            r.TipoResolucion = App.obtenerConfiguracion_TipoResolucion(contexto);
            r.NumeroCelularRuta = App.obtenerConfiguracion_NumeroCelularRuta(contexto);
            r.FechaDeResolucion = App.obtenerConfiguracion_FechaDeResolucion(contexto);
            r.PermitirRemisionesConValorAcero = App.obtenerConfiguracion_PermitirRemisionesConValorCero(contexto);
            r.PorcentajeReteIva = App.obtenerConfiguracion_PorcentajeReteIva(contexto);
            r.TopeReteIva = App.obtenerConfiguracion_TopeReteIva(contexto);
            r.CrearNotaCreditoPorDevolucion = App.obtenerConfiguracion_CrearNotaCreditoPorDevolucion(contexto);
            r.ManejarInventarioRemisiones = App.obtenerConfiguracion_ManejarInventarioRemisiones(contexto);
            r.VigenciaDeResolucion = App.obtenerConfiguracion_FechaVigenciaDeResolucion(contexto);
            r.DevolucionAfectaRemision = App.obtenerConfiguracion_DevolucionAfectaRemision(contexto);
            r.EFactura = App.obtenerConfiguracion_EFactura(contexto);
            r.ClaveTecnica = App.obtenerConfiguracion_ClaveTecnica(contexto);
            r.AmbienteEFactura = App.obtenerConfiguracion_AmbienteEFactura(contexto);
        }
        cursor.close();
        return r;
    }

    @Override
    void setColumns() {
    }
}