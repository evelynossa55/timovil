package com.cm.timovil2.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 18/12/2016.
 */
class BDHelper {

    private static final String TablaResolucion = "Resolucion";
    private static final String TablaProducto = "Producto";
    private static final String TablaCliente = "Cliente";
    private static final String TablaDescuento = "Descuento";
    private static final String TablaFactura = "Factura";
    private static final String TablaDetalleFactura = "DetalleFactura";
    private static final String TablaFormaDePago = "FormaPago";
    private static final String TablaReferencia = "Referencia";
    private static final String TablaFacturaCredito = "FacturaCredito";
    private static final String TablaDetalleFacturaCredito = "DetalleFacturaCredito";
    private static final String TablaAbonoFactura = "AbonoFactura";
    private static final String TablaDetalleListaPrecios = "DetalleListaPrecios";
    private static final String TablaRemision = "Remision";
    private static final String TablaDetalleRemision = "DetalleRemision";
    private static final String TablaEntregador = "Entregador";
    private static final String TablaMotivoNoVenta = "MotivoNoVenta";
    private static final String TablaGestionComercial = "GestionComercial";
    private static final String TablaOperacionDiaria = "OperacionDiaria";
    private static final String TablaPedidoCallcenter = "PedidoCallcenter";
    private static final String TablaDetallePedidoCallcenter = "DetallePedidoCallcenter";
    private static final String TablaResultadoGestionCasoCallcenter = "ResultadoGestionCasoCallcenter";
    private static final String TablaGuardarMotivoNoVenta = "GuardarMotivoNoVenta";
    private static final String TablaGuardarMotivoNoVentaPedido = "GuardarMotivoNoVentaPedido";
    private static final String TablaUbicacionRuta = "UbicacionRuta";
    private static final String TablaCuentaCaja = "CuentaCaja";
    private static final String TablaProgramacionAsesor = "ProgramacionAsesor";
    private static final String TablaNotaCreditoFactura = "NotaCreditoFactura";
    private static final String TablaDetalleNotaCreditoFactura = "DetalleNotaCreditoFactura";

    private static final String TABLA_RESOLUCION = "CREATE TABLE [" + TablaResolucion + "] ("
            + "[_id] INTEGER PRIMARY KEY, "
            + "[IdCliente] TEXT  NOT NULL, "
            + "[IdResolucion] INTEGER  NOT NULL, "
            + "[Regimen] TEXT NOT NULL, "
            + "[RazonSocial] VARCHAR(100)  NOT NULL, "
            + "[NombreComercial] VARCHAR(100)  NOT NULL, "
            + "[Nit] TEXT  NOT NULL, "
            + "[Direccion] TEXT NOT NULL, "
            + "[Telefono] TEXT NOT NULL, "
            + "[FacturaInicial] TEXT NOT NULL, "
            + "[FacturaFinal] TEXT NOT NULL, "
            + "[Resolucion] TEXT NOT NULL, "
            + "[FechaResolucion] TEXT NOT NULL, "
            + "[SiguienteFactura] INTEGER  NOT NULL, "
            + "[SiguienteRemision] INTEGER  NOT NULL, "
            + "[CodigoRuta] TEXT NOT NULL, "
            + "[NombreRuta] TEXT NOT NULL, "
            + "[ClaveAdmin] TEXT NOT NULL, "
            + "[ClaveVendedor] TEXT NOT NULL, "
            + "[PrefijoFacturacion] TEXT NOT NULL, "
            + "[DevolucionAfectaVenta] INTEGER NOT NULL, "
            + "[DevolucionRestaInventario] INTEGER NOT NULL, "
            + "[RotacionRestaInventario] INTEGER NOT NULL, "
            + "[MostrarListaPrecios] INTEGER NOT NULL, "
            + "[DescargarFacturasAuto] INTEGER NOT NULL, "
            + "[RedondearValores] INTEGER NOT NULL, "
            + "[NumeroCopias] INTEGER NOT NULL, "
            + "[UrlServicioWeb] TEXT NOT NULL, "
            + "[PermitirEliminarFacturas] INTEGER NOT NULL, "
            + "[MACImpresora] TEXT NOT NULL, "
            + "[NombreImpresora] TEXT NOT NULL, "
            + "[TipoImpresora] TEXT NOT NULL, "
            + "[CodigoBodega] TEXT NOT NULL, "
            + "[ManejarInventario] INTEGER NOT NULL, "
            + "[PorcentajeRetefuente] FLOAT NOT NULL, "
            + "[TopeRetefuente] FLOAT NOT NULL, "
            + "[ReportarUbicacionGPS] INTEGER NOT NULL, "
            + "[Bodegas] TEXT NOT NULL, "
            + "[Email] TEXT NOT NULL, "
            + "[ValorMetaMensual] DOUBLE NOT NULL, "
            + "[ValorVentaMensual] DOUBLE NOT NULL, "
            + "[IdResolucionPOS] INTEGER  NOT NULL, "
            + "[FacturaInicialPOS] TEXT NOT NULL, "
            + "[FacturaFinalPOS] TEXT NOT NULL, "
            + "[ResolucionPOS] TEXT NOT NULL, "
            + "[FechaResolucionPOS] TEXT NOT NULL, "
            + "[SiguienteFacturaPOS] INTEGER  NOT NULL, "
            + "[PrefijoFacturacionPOS] TEXT NOT NULL, "
            + "[DiaCerrado] INTEGER NOT NULL, "
            + "[FechaCierre] INTEGER NOT NULL, "
            + "[CierreNotificadoServidor] INTEGER NOT NULL, "
            + "[HoraLimiteFacturacion] INTEGER NOT NULL, "
            + "[SiguienteNotaCredito] INTEGER NOT NULL)";

    private static final String TABLA_PRODUCTO = "CREATE TABLE ["+ TablaProducto+ "] ("
            + "[_id] INTEGER  NOT NULL , "
            + "[Codigo] VARCHAR(10)  NOT NULL, "
            + "[Nombre] VARCHAR(100)  NOT NULL, "
            + "[Precio1] FLOAT  NOT NULL, "
            + "[Precio2] FLOAT  NOT NULL, "
            + "[Precio3] FLOAT  NOT NULL, "
            + "[PorcentajeIva] FLOAT  NOT NULL, "
            + "[StockInicial] INTEGER  NOT NULL, "
            + "[Ventas] INTEGER  NOT NULL, "
            + "[Devoluciones] INTEGER  NOT NULL, "
            + "[Rotaciones] INTEGER  NOT NULL, "
            + "[Precio4] FLOAT  NOT NULL, "
            + "[Precio5] FLOAT  NOT NULL, "
            + "[Precio6] FLOAT  NOT NULL, "
            + "[Precio7] FLOAT  NOT NULL, "
            + "[Precio8] FLOAT  NOT NULL, "
            + "[Precio9] FLOAT  NOT NULL, "
            + "[Precio10] FLOAT  NOT NULL, "
            + "[Precio11] FLOAT  NOT NULL, "
            + "[Precio12] FLOAT  NOT NULL, "
            + "IpoConsumo FLOAT NOT NULL, "
            + "CodigoBodega TEXT NOT NULL)";

    private static final String TABLA_CLIENTE = "CREATE TABLE [" + TablaCliente + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdCliente] INTEGER  NOT NULL, "
            + "[Identificacion] TEXT  NOT NULL, "
            + "[RazonSocial] TEXT  NOT NULL, "
            + "[NombreComercial] TEXT  NOT NULL, "
            + "[Direccion] TEXT  NOT NULL, "
            + "[Telefono1] TEXT  NOT NULL, "
            + "[Telefono2] TEXT  NOT NULL, "
            + "[Dia] INTEGER  NOT NULL, "
            + "[Orden] INTEGER  NOT NULL, "
            + "[Retenedor] INTEGER  NOT NULL, "
            + "[RetenedorCREE] INTEGER  NOT NULL, "
            + "[PorcentajeCREE] FLOAT NOT NULL, "
            + "[Atendido] TEXT NOT NULL, "
            + "[ListaPrecios] INTEGER NOT NULL, "
            + "[CarteraPendiente] TEXT NOT NULL, "
            + "[Remisiones] TEXT NOT NULL, "
            + "[Credito] TEXT NOT NULL, "
            + "[IdListaPrecios] INTEGER NOT NULL, "
            + "[Remision] TEXT NOT NULL, "
            + "[ValorVentasMes] TEXT NOT NULL, "
            + "[FormaPagoFlexible] TEXT NOT NULL, "
            + "[VecesAtendido] INTEGER NOT NULL, "
            + "[ExentoIva] TEXT NOT NULL, "
            + "[DireccionEntrega] TEXT NOT NULL, "
            + "[Plazo] INTEGER NOT NULL, "
            + "[Ubicacion] TEXT NOT NULL, "
            + "[ReteIva] TEXT NOT NULL,"
            + "[OblicatorioCodigoBarras] TEXT NOT NULL,"
            + "[FacturacionElectronicaCliente] TEXT NOT NULL,"
            + "[FacturacionPOSCliente] TEXT NOT NULL,"
            + "[Latitud] TEXT NOT NULL,"
            + "[Longitud] TEXT NOT NULL)";

    private static final String TABLA_DESCUENTOS = ("CREATE TABLE [" + TablaDescuento + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdCliente] INTEGER  NOT NULL, "
            + "[IdProducto] INTEGER  NOT NULL, "
            + "[Porcentaje] FLOAT  NOT NULL)");

    private static final String TABLA_FORMA_PAGO = ("CREATE TABLE [" + TablaFormaDePago + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[Codigo] TEXT NOT NULL, "
            + "[Nombre] TEXT NOT NULL)");

    private static final String TABLA_REFERENCIA = ("CREATE TABLE [" + TablaReferencia + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdCliente] INT NOT NULL, "
            + "[Nombre] TEXT NOT NULL, "
            + "[Apellidos] TEXT NOT NULL, "
            + "[Telefono1] TEXT NOT NULL, "
            + "[Telefono2] TEXT NOT NULL)");

    private static final String TABLA_FACTURA = ("CREATE TABLE [" + TablaFactura + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[NumeroFactura] TEXT NOT NULL UNIQUE, "
            + "[FechaHora] INTEGER NOT NULL, "
            + "[FormaPago] TEXT NOT NULL, "
            + "[IdCliente] INT NOT NULL, "
            + "[Identificacion] TEXT NOT NULL, "
            + "[RazonSocial] TEXT NOT NULL, "
            + "[Negocio] TEXT NOT NULL, "
            + "[Direccion] TEXT NOT NULL, "
            + "[Telefono] TEXT NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[Retefuente] FLOAT NOT NULL, "
            + "[PorcentajeRetefuente] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[Ica] FLOAT NOT NULL, "
            + "[Total] TEXT NOT NULL, "
            + "[Sincronizada] Integer NOT NULL, "
            + "[Anulada] INTEGER NOT NULL, "
            + "[PendienteAnulacion] INTEGER NOT NULL, "
            + "[Efectivo] FLOAT NOT NULL, "
            + "[Devolucion] INTEGER NOT NULL, "
            + "[Latitud] TEXT NOT NULL, "
            + "[Longitud] TEXT NOT NULL, "
            + "[CREE] FLOAT NOT NULL, "
            + "[PorcentajeCREE] FLOAT NOT NULL, "
            + "[Comentario] TEXT NOT NULL, "
            + "[IpoConsumo] FLOAT NOT NULL, "
            + "[TipoDocumento] TEXT NOT NULL, "
            + "[codigoBodega] TEXT NOT NULL, "
            + "[NumeroPedido] TEXT NOT NULL, "
            + "[ComentarioAnulacion] TEXT NULL, "
            + "[IdEmpleadoEntregador] INTEGER NULL, "
            + "[IdResolucion] INTEGER NULL, "
            + "[Remision] INTEGER NOT NULL, "
            + "[Revisada] INTEGER NOT NULL, "
            + "[IsPedidoCallcenter] INTEGER NOT NULL, "
            + "[FechaHoraVencimiento] INTEGER NOT NULL, "
            + "[IdPedido] INTEGER NOT NULL, "
            + "[IdCaso] INTEGER NOT NULL, "
            + "[ValorDevolucion] FLOAT, "
            + "[ReteIva] FLOAT NOT NULL, "
            + "[FacturaPos] INTEGER NOT NULL, "
            + "[Cantidad] INTEGER NOT NULL, "
            + "[Rotacion] INTEGER NOT NULL, "
            + "[QRInputValue] TEXT NULL,"
            + "[Cufe] TEXT NOT NULL, "
            + "[DistanciaDelNegocio] TEXT NULL, "
            + "[CreadaConCodigoBarras] INTEGER NOT NULL) ");

    private static final String TABLA_DETALLE_FACTURA = ("CREATE TABLE [" + TablaDetalleFactura + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[NumeroFactura] TEXT NOT NULL, "
            + "[IdProducto] INTEGER NOT NULL, "
            + "[Codigo] TEXT NOT NULL, "
            + "[Nombre] TEXT NOT NULL, "
            + "[Cantidad] INTEGER NOT NULL, "
            + "[Devolucion] INTEGER NOT NULL, "
            + "[Rotacion] INTEGER NOT NULL, "
            + "[ValorUnitario] FLOAT NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[PorcentajeDescuento] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[PorcentajeIva] FLOAT NOT NULL, "
            + "[Total] FLOAT NOT NULL, "
            + "IpoConsumo FLOAT NOT NULL, "
            + "ValorDevolucion FLOAT)");

    private static final String TABLA_FACTURA_CREDITO =("CREATE TABLE [" + TablaFacturaCredito + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdFactura] TEXT NOT NULL, "
            + "[NumeroFactura] TEXT NOT NULL, "
            + "[NombreRuta] TEXT NOT NULL, "
            + "[FechaHora] TEXT NOT NULL, "
            + "[FormaPago] TEXT NOT NULL, "
            + "[IdCliente] INT NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[Retefuente] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[Total] FLOAT NOT NULL, "
            + "[Saldo] FLOAT NOT NULL, "
            + "[Identificacion] TEXT NOT NULL, "
            + "[RazonSocial] TEXT NOT NULL, "
            + "[Negocio] TEXT NOT NULL, "
            + "[Direccion] TEXT NOT NULL, "
            + "[Telefono] TEXT NOT NULL, "
            + "[Resolucion] TEXT NOT NULL)");

    private static final String TABLA_DETALLE_FACTURA_CREDITO = ("CREATE TABLE [" + TablaDetalleFacturaCredito + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdFactura] TEXT NOT NULL, "
            + "[IdProducto] INTEGER NOT NULL, "
            + "[Codigo] TEXT NOT NULL, "
            + "[Nombre] TEXT NOT NULL, "
            + "[Cantidad] INTEGER NOT NULL, "
            + "[Devolucion] INTEGER NOT NULL, "
            + "[Rotacion] INTEGER NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[Total] FLOAT NOT NULL)");

    private static final String TABLA_ABONO_FACTURA =("CREATE TABLE [" + TablaAbonoFactura + "] ("
            + "[_id] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdFactura] TEXT NOT NULL, "
            + "[NumeroFactura] TEXT NOT NULL, "
            + "[Fecha] TEXT NOT NULL,"
            + "[Valor] FLOAT NOT NULL, "
            + "[Saldo] FLOAT NOT NULL, "
            + "[Identificador] TEXT NOT NULL, "
            + "[Sincronizado] Integer NOT NULL,"
            + "[DiaCreacion] TEXT NOT NULL, "
            + "[IdCuentaCaja] INTEGER NOT NULL, "
            + "[FechaCreacion] INTEGER NOT NULL)");

    private static final String TABLA_DETALLE_LISTA_PRECIOS =("CREATE TABLE [" + TablaDetalleListaPrecios + "] ("
            + "[IdDetalleListaPrecios] INTEGER PRIMARY KEY, "
            + "[IdListaPrecios] INTEGER NOT NULL, "
            + "[IdProducto] INTEGER NOT NULL, "
            + "[Precio] FLOAT NOT NULL, "
            + "[NombreListaPrecios] TEXT NOT NULL)");

    private static final String TABLA_REMISION = ("CREATE TABLE [" + TablaRemision + "] ("
            + "[IdAuto] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[NumeroRemision] TEXT NOT NULL UNIQUE, "
            + "[Fecha] INTEGER NOT NULL, "
            + "[IdCliente] INTEGER NOT NULL, "
            + "[CodigoRuta] TEXT NOT NULL, "
            + "[NombreRuta] TEXT NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[Total] FLOAT NOT NULL, "
            + "[RazonSocialCliente] TEXT NOT NULL, "
            + "[IdentificacionCliente] TEXT NOT NULL, "
            + "[TelefonoCliente] TEXT NOT NULL, "
            + "[DireccionCliente] TEXT NOT NULL, "
            + "[Anulada] INTEGER NOT NULL, "
            + "[FechaCreacion] INTEGER NOT NULL, "
            + "[Latitud] TEXT NOT NULL, "
            + "[Longitud] TEXT NOT NULL, "
            + "[Comentario] TEXT NOT NULL, "
            + "[Sincronizada] INTEGER NOT NULL, "
            + "[PendienteAnulacion] INTEGER NOT NULL,"
            + "[codigoBodega] TEXT NOT NULL,"
            + "[NumeroPedido] TEXT NOT NULL, "
            + "[ComentarioAnulacion] TEXT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[FormaPago] TEXT NULL, "
            + "IsPedidoCallcenter INTEGER NOT NULL, "
            + "Devolucion INTEGER NOT NULL, "
            + "Rotacion INTEGER NOT NULL, "
            + "IdPedido INTEGER NOT NULL, "
            + "IdCaso INTEGER NOT NULL, "
            + "ValorDevolucion FLOAT NOT NULL, "
            + "Ipoconsumo FLOAT NOT NULL, "
            + "[Negocio] TEXT NOT NULL, "
            + "[ValorRetefuente] FLOAT NOT NULL, "
            + "[RetefuenteDevolucion] FLOAT NOT NULL, "
            + "[ValorReteIvaDevolucion] FLOAT NOT NULL, "
            + "[ValorReteIva] FLOAT NOT NULL)");

    private static final String TABLA_DETALLE_REMISION =("CREATE TABLE [" + TablaDetalleRemision + "] ("
            + "[IdDetalle] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[NumeroRemision] TEXT NOT NULL, "
            + "[IdProducto] INTEGER NOT NULL, "
            + "[NombreProducto] TEXT NOT NULL, "
            + "[Cantidad] INTEGER NOT NULL, "
            + "[ValorUnitario] FLOAT NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Total] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[FechaCreacion] INTEGER NOT NULL, "
            + "[PorcentajeIva] FLOAT NOT NULL, "
            + "[Codigo] TEXT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[PorcentajeDescuento] FLOAT NOT NULL, "
            + "Devolucion INTEGER NOT NULL, "
            + "Rotacion INTEGER NOT NULL, "
            + "ValorDevolucion FLOAT NOT NULL, "
            + "Ipoconsumo FLOAT NOT NULL, "
            + "IvaDevolucion FLOAT NOT NULL)");

    private static final String TABLA_ENTREGADOR =("CREATE TABLE [" + TablaEntregador + "] ("
            + "[IdEntregador] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdEmpleado] INTEGER NOT NULL, "
            + "[NombreCompleto] TEXT NOT NULL) ");

    private static final String TABLA_MOTIVO_NO_VENTA = ("CREATE TABLE [" + TablaMotivoNoVenta + "] ("
            + "[IdMotivo] INTEGER PRIMARY KEY, "
            + "[Descripcion] TEXT  NOT NULL)");

    private static final String TABLA_RESULTADO_GESTION_CASO_CALLCENTER = ("CREATE TABLE [" + TablaResultadoGestionCasoCallcenter + "] ("
            + "[IdResultadoGestion] INTEGER PRIMARY KEY, "
            + "[Nombre] TEXT  NOT NULL)");

    private static final String TABLA_GESTION_COMERCIAL =("CREATE TABLE [" + TablaGestionComercial + "] ("
            + "[IdGestion] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdCliente] INTEGER  NOT NULL, "
            + "[FechaHora] TEXT  NOT NULL, "
            + "[Comentario] TEXT  NOT NULL, "
            + "[CodigoRuta] TEXT  NOT NULL, "
            + "[Latitud] TEXT  NOT NULL, "
            + "[Longitud] TEXT  NOT NULL, "
            + "[Contacto] TEXT  NOT NULL, "
            + "[TelContacto] TEXT  NOT NULL, "
            + "[Estado] TEXT  NOT NULL, "
            + "[Sincronizada] INTEGER  NOT NULL, "
            + "[EncodedImages] TEXT NULL )");

    private static final String TABLA_PEDIDO_CALLCENTER =("CREATE TABLE [" + TablaPedidoCallcenter + "] ("
            + "[IdPedido] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdCliente] INTEGER  NOT NULL, "
            + "[IdCaso] INTEGER  NOT NULL, "
            + "[FechaSolicitada] TEXT  NOT NULL, "
            + "[Comentario] TEXT  NOT NULL )");


    private static final String TABLA_DETALLE_PEDIDO_CALLCENTER =("CREATE TABLE [" + TablaDetallePedidoCallcenter + "] ("
            + "[IdDetallePedido] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdPedido] INTEGER  NOT NULL, "
            + "[IdProducto] INTEGER  NOT NULL, "
            + "[Cantidad] INTEGER  NOT NULL )");

    private static final String TABLA_GUARDAR_MOTIVO_NO_VENTA =("CREATE TABLE [" + TablaGuardarMotivoNoVenta + "] ("
            + "[IdMotivoNoVenta] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdMotivo] INTEGER  NOT NULL, "
            + "[IdCliente] INTEGER  NOT NULL, "
            + "[CodigoRuta] TEXT  NOT NULL, "
            + "[Descripcion] TEXT  NOT NULL, "
            + "[Fecha] TEXT  NOT NULL, "
            + "[Fecha_long] TEXT  NOT NULL, "
            + "[Latitud] TEXT  NOT NULL, "
            + "[Longitud] TEXT  NOT NULL, "
            + "[IdClienteTimovil] TEXT  NOT NULL, "
            + "[esOtroMotivo] INTEGER  NOT NULL, "
            + "[Motivo] TEXT  NOT NULL, "
            + "[Sincronizada] INTEGER  NOT NULL) ");

    private static final String TABLA_GUARDAR_MOTIVO_NO_VENTA_PEDIDO =("CREATE TABLE [" + TablaGuardarMotivoNoVentaPedido + "] ("
            + "[IdMotivoNoVentaPedido] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[IdClienteTimovil] TEXT  NOT NULL, "
            + "[CodigoRuta] TEXT  NOT NULL, "
            + "[IdResultadoGestion] INTEGER  NOT NULL, "
            + "[Descripcion] TEXT  NOT NULL, "
            + "[IdCaso] INTEGER  NOT NULL, "
            + "[Sincronizada] INTEGER  NOT NULL, "
            + "[Fecha] INTEGER  NOT NULL, "
            + "[IdCliente] TEXT NOT NULL) ");

    private static final String TABLA_UBICACION_RUTA =("CREATE TABLE [" + TablaUbicacionRuta + "] ("
            + "[IdUbicacionRuta] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[CodigoRuta] TEXT  NOT NULL, "
            + "[Fecha] TEXT  NOT NULL, "
            + "[Latitud] TEXT  NULL, "
            + "[Longitud] TEXT NULL, "
            + "[GpsActivo] INTEGER  NOT NULL, "
            + "[Comentario] TEXT  NOT NULL, "
            + "[Sincronizada] INTEGER NOT NULL) ");

    private static final String TABLA_CUENTA_CAJA =("CREATE TABLE [" + TablaCuentaCaja + "] ("
            + "[IdCuentaCaja] INTEGER PRIMARY KEY, "
            + "[Nombre] TEXT  NOT NULL, "
            + "[NumeroCuenta] INTEGER NOT NULL) ");

    private static final String TABLA_ASESOR_PROGRAMACION =("CREATE TABLE [" + TablaProgramacionAsesor + "] ("
            + "[IdAsesorProgramacionDetalle] INTEGER PRIMARY KEY, "
            + "[IdAsesorProgramacion] INTEGER  NOT NULL, "
            + "[IdCliente] INTEGER  NOT NULL, "
            + "[Fecha] TEXT NOT NULL, "
            + "[Dia] INTEGER NOT NULL) ");

    private static final String TABLA_NOTA_CREDITO_FACTURA = ("CREATE TABLE ["+TablaNotaCreditoFactura+"] ("
            + "[IdNotaCreditoFactura] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[NumeroDocumento] TEXT NOT NULL, "
            + "[NumeroFactura] TEXT NOT NULL, "
            + "[Fecha] INTEGER NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[Ipoconsumo] FLOAT NOT NULL, "
            + "[Iva5] FLOAT NOT NULL, "
            + "[Iva19] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[Valor] FLOAT NOT NULL, "
            + "[Sincronizada] INTEGER NOT NULL, "
            + "[CodigoBodega] TEXT NULL, "
            + "[Anulada] INTEGER NOT NULL, "
            + "[QRInputValue] TEXT NOT NULL, "
            + "[Cufe] TEXT NOT NULL) ");

    private static final String TABLA_DETALLE_NOTA_CREDITO_FACTURA = ("CREATE TABLE ["+TablaDetalleNotaCreditoFactura+"] ("
            + "[IdDetalleNotaCreditoFactura] INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "[NumeroDocumento] TEXT NOT NULL, "
            + "[IdProducto] INTEGER NOT NULL, "
            + "[Cantidad] INTEGER NOT NULL, "
            + "[Subtotal] FLOAT NOT NULL, "
            + "[Descuento] FLOAT NOT NULL, "
            + "[Ipoconsumo] FLOAT NOT NULL, "
            + "[Iva5] FLOAT NOT NULL, "
            + "[Iva19] FLOAT NOT NULL, "
            + "[Iva] FLOAT NOT NULL, "
            + "[Valor] FLOAT NOT NULL, "
            + "[Codigo] TEXT NOT NULL, "
            + "[Nombre] TEXT NOT NULL) ");

    /*
    public static final String TABLA_OPERACION_DIARIA =
            "CREATE TABLE [" + TablaOperacionDiaria + "] ("
            + "[FechaHora] TEXT PRIMARY KEY, "
            + "[FechaCreacion] INTEGER NOT NULL, "
            + "[FechaActualizacion] INTEGER NOT NULL, "
            + "[FechaCierre] INTEGER NOT NULL, "
            + "FacturaInicial TEXT, ";
            + "FechaInicial LONG, ";
    */

    private static  final String[] querysTablas = {
            TABLA_RESOLUCION,
            TABLA_CLIENTE,
            TABLA_DESCUENTOS,
            TABLA_PRODUCTO,
            TABLA_FORMA_PAGO,
            TABLA_REFERENCIA,
            TABLA_FACTURA,
            TABLA_DETALLE_FACTURA,
            TABLA_FACTURA_CREDITO,
            TABLA_DETALLE_FACTURA_CREDITO,
            TABLA_ABONO_FACTURA,
            TABLA_DETALLE_LISTA_PRECIOS,
            TABLA_REMISION,
            TABLA_DETALLE_REMISION,
            TABLA_ENTREGADOR,
            TABLA_MOTIVO_NO_VENTA,
            TABLA_GESTION_COMERCIAL,
            TABLA_PEDIDO_CALLCENTER,
            TABLA_DETALLE_PEDIDO_CALLCENTER,
            TABLA_RESULTADO_GESTION_CASO_CALLCENTER,
            TABLA_GUARDAR_MOTIVO_NO_VENTA,
            TABLA_GUARDAR_MOTIVO_NO_VENTA_PEDIDO,
            TABLA_UBICACION_RUTA,
            TABLA_CUENTA_CAJA,
            TABLA_ASESOR_PROGRAMACION,
            TABLA_NOTA_CREDITO_FACTURA,
            TABLA_DETALLE_NOTA_CREDITO_FACTURA
    };



    static void CrearBD(SQLiteDatabase db){
        EliminarTablas(db);
        for (int i = 0; i < querysTablas.length; i++) {
            db.execSQL(querysTablas[i]);
            Log.d("DAL", "<<Tabla Creada>> : " + i);
        }
    }

    private static void EliminarTablas(SQLiteDatabase db){
        //for (int i = 0; i < tablas.length; i++)
        //{
          //  BDHelper.ExecuteQuery(db, tablas[i]);
        //}
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaResolucion);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaCliente);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDescuento);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaProducto);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaFormaDePago);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaReferencia);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaFactura);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDetalleFactura);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaFacturaCredito);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDetalleFacturaCredito);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaAbonoFactura);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDetalleListaPrecios);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaRemision);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDetalleRemision);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaEntregador);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaMotivoNoVenta);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaGestionComercial);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaOperacionDiaria);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDetallePedidoCallcenter);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaPedidoCallcenter);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaResultadoGestionCasoCallcenter);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaGuardarMotivoNoVenta);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaGuardarMotivoNoVentaPedido);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaUbicacionRuta);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaCuentaCaja);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaProgramacionAsesor);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaNotaCreditoFactura);
        BDHelper.ExecuteQuery(db, "DROP TABLE IF EXISTS " + TablaDetalleNotaCreditoFactura);
    }

    private static void ExecuteQuery(SQLiteDatabase db, String query){
        db.execSQL(query);
    }
}