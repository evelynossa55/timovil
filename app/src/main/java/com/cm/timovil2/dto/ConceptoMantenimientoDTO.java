package com.cm.timovil2.dto;

public class ConceptoMantenimientoDTO {

    public String IdConceptoMantenimiento;
    public String Descripcion;
    public int DuracionMantenimiento;

    @Override
    public String toString() {
        return Descripcion;
    }
}
