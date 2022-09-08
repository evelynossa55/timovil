package com.cm.timovil2.proxy;

public interface ISincroWeb {

    void InicioSincronizacion();

    void DatoRecibido(String datoRecibido);

    void DatoRecibido(String datoRecibido, String cantidad);

    void FinSincronizacion(int cantidad);

    void FinSincronizacion(String mensaje);

    void FinSincronizacion(String resultados[]);

    void ErrorSincronizacion(String error);

    void EstadoSincronizacion(); //Esto se usaría para ocultar comandos y funciones que el usuario podréa usar mientras se realiza la sincronizacién

    void EstadoNormal(); //Esto se usaréa para mostrar comandos y funciones que el usuario podréa usar mientras se realiza la sincronizacién

    void LimpiarListado(); //En caso de que se muestre un listado con los datos recibidos, y sea necesario limpiar esta lista.
}