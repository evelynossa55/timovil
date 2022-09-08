package com.cm.timovil2.bl.printers.btservice.print;

/**
 * Created by yefeng on 6/2/15.
 * github:yefengfreedom
 * <p/>
 * printer command
 */
public class GPrinterCommand {

    public static final byte[] left = new byte[]{0x1b, 0x61, 0x00};// Izquierda
    public static final byte[] center = new byte[]{0x1b, 0x61, 0x01};// Centrado
    public static final byte[] right = new byte[]{0x1b, 0x61, 0x02};// Derecha
    public static final byte[] bold = new byte[]{0x1b, 0x45, 0x01};// Seleccione el modo negrita
    public static final byte[] bold_cancel = new byte[]{0x1b, 0x45, 0x00};// Cancelar el modo negrita
    public static final byte[] text_normal_size = new byte[]{0x1d, 0x21, 0x00};// La fuente no está ampliada
    public static final byte[] text_big_height = new byte[]{0x1b, 0x21, 0x10};// Doble alto
    public static final byte[] text_big_size = new byte[]{0x1d, 0x21, 0x11};// Doble el ancho y la altura
    public static final byte[] reset = new byte[]{0x1b, 0x40};//Restablecer impresora
    public static final byte[] print = new byte[]{0x0a};//Imprimir y envolver
    public static final byte[] under_line = new byte[]{0x1b, 0x2d, 2};//Subrayado
    public static final byte[] under_line_cancel = new byte[]{0x1b, 0x2d, 0};//Subrayado

    /**
     * Papel
     *
     * @param n Número de filas
     * @return Comando
     */
    public static byte[] walkPaper(byte n) {
        return new byte[]{0x1b, 0x64, n};
    }

    /**
     * Establecer unidades de movimiento horizontal y vertical
     *
     * @param x Movimiento lateral
     * @param y Movimiento vertical
     * @return Comando
     */
    public static byte[] move(byte x, byte y) {
        return new byte[]{0x1d, 0x50, x, y};
    }

}
