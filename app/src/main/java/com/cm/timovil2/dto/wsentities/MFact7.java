package com.cm.timovil2.dto.wsentities;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;
import java.util.Vector;

public class MFact7 implements KvmSerializable {
	public String Num;
	public String CRuta;
	public int IdCli;
	public float PRet;
	public float PIca;
	public String Fecha;
	public String FP;
	public boolean Anul;
	public int IdRes;
	public Vector<MDetFact> Detalle;
	public float Efect;
	public float Devol;
	public String Latitud;
	public String Longitud;
    public String Comentario;
    public String TipoDoc;
    public String CodigoBodega;
    public String NroPedido;
    public String ComentAnu;
    public int IdEmpEntre;

	public MFact7(){
		Detalle = new Vector<>();
		Latitud = "";
		Longitud = "";
        Comentario = "";
        TipoDoc = "";
        CodigoBodega = "";
        ComentAnu = "";
        IdEmpEntre = -1;
	}

	// <Detalle>
	// <MDetFact>
	// <IdProd>int</IdProd>
	// <Cant>int</Cant>
	// <Dev>int</Dev>
	// <Rot>int</Rot>
	// <VUnit>float</VUnit>
	// <PDesc>float</PDesc>
	// <PIva>float</PIva>
	// </MDetFact>
	public Object getProperty(int arg0) {
		switch (arg0) {
		case 0:
			return Num;
		case 1:
			return CRuta;
		case 2:
			return IdCli;
		case 3:
			return PRet;
		case 4:
			return PIca;
		case 5:
			return Fecha;
		case 6:
			return FP;
		case 7:
			return Anul;
		case 8:
			return IdRes;
		case 9:
			return Detalle;
		case 10:
			return Efect;
		case 11:
			return Devol;
		case 12:
			return Latitud;
		case 13:
			return Longitud;
        case 14:
            return Comentario;
        case 15:
            return TipoDoc;
        case 16:
            return CodigoBodega;
        case 17:
            return NroPedido;
        case 18:
            return ComentAnu;
        case 19:
            return IdEmpEntre;
        }
		return null;
	}

	public int getPropertyCount() {
		return 20;
	}

	public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
		switch(index)
        {
        case 0:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "Num";
            break;
        case 1:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "CRuta";
            break;
        case 2:
            info.type = PropertyInfo.INTEGER_CLASS;
            info.name = "IdCli";
            break;
        case 3:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "PRet";
            break;
        case 4:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "PIca";
            break;
        case 5:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "Fecha";
            break;
        case 6:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "FP";
            break;
        case 7:
            info.type = PropertyInfo.BOOLEAN_CLASS;
            info.name = "Anul";
            break;
        case 8:
            info.type = PropertyInfo.INTEGER_CLASS;
            info.name = "IdRes";
            break;
        case 9:
        	PropertyInfo elementInfo = new PropertyInfo();
        	elementInfo.name = "MDetFact";
        	elementInfo.type  = Vector.class;
        	info.elementType = elementInfo;
            info.type = PropertyInfo.VECTOR_CLASS;
            info.name = "Detalle";
            break;
        case 10:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "Efect";
            break;
        case 11:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "Devol";
            break;
        case 12:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "Latitud";
            break;
        case 13:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "Longitud";
            break;
        case 14:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "Comentario";
            break;
        case 15:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "TipoDoc";
            break;
        case 16:
            info.type = PropertyInfo.STRING_CLASS;
            info.name = "CodigoBodega";
        case 17:
             info.type = PropertyInfo.STRING_CLASS;
             info.name = "NroPedido";
        case 18:
             info.type = PropertyInfo.STRING_CLASS;
             info.name = "ComentAnu";
        case 19:
             info.type = PropertyInfo.INTEGER_CLASS;
             info.name = "IdEmpEntre";
        default:break;
        }
	}

	public void setProperty(int index, Object value) {
        switch(index)
        {
        case 0:
            Num = value.toString();
            break;
        case 1:
            CRuta = value.toString();
            break;
        case 2:
            IdCli = Integer.parseInt(value.toString());
            break;
        case 3:
        	PRet = Float.parseFloat(value.toString());
            break;
        case 4:
        	PIca = Float.parseFloat(value.toString());
            break;
        case 5:
        	Fecha = value.toString();
            break;
        case 6:
        	FP = value.toString();
            break;
        case 7:
        	Anul = Boolean.parseBoolean(value.toString());
            break;
        case 8:
        	IdRes = Integer.parseInt(value.toString());
            break;
        case 9:
            Detalle = (Vector<MDetFact>)value;
            break;
        case 10:
        	Efect = Float.parseFloat(value.toString());
            break;
        case 11:
        	Devol = Float.parseFloat(value.toString());
            break;
        case 12:
        	Latitud = value.toString();
            break;
        case 13:
        	Longitud = value.toString();
            break;
        case 14:
            Comentario = value.toString();
            break;
        case 15:
            TipoDoc = value.toString();
            break;
        case 16:
            CodigoBodega = value.toString();
            break;
        case 17:
            NroPedido = value.toString();
            break;
        case 18:
             ComentAnu = value.toString();
        case 19:
             IdEmpEntre = Integer.parseInt(value.toString());
        default:
            break;
        }
	}
}