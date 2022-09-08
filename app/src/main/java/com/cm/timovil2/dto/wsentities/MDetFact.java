package com.cm.timovil2.dto.wsentities;

import java.util.Hashtable;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

public class MDetFact implements KvmSerializable {
	public int IdProd;
	public int Cant;
	public int Dev;
	public int Rot;
	public float VUnit;
	public float PDesc;
	public float PIva;

	public Object getProperty(int arg0) {
		switch (arg0) {
		case 0:
			return IdProd;
		case 1:
			return Cant;
		case 2:
			return Dev;
		case 3:
			return Rot;
		case 4:
			return VUnit;
		case 5:
			return PDesc;
		case 6:
			return PIva;
		}
		return null;
	}

	public int getPropertyCount() {
		return 7;
	}

	public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info) {
		switch(index)
        {
        case 0:
            info.type = PropertyInfo.INTEGER_CLASS;
            info.name = "IdProd";
            break;
        case 1:
            info.type = PropertyInfo.INTEGER_CLASS;
            info.name = "Cant";
            break;
        case 2:
            info.type = PropertyInfo.INTEGER_CLASS;
            info.name = "Dev";
            break;
        case 3:
            info.type = PropertyInfo.INTEGER_CLASS;
            info.name = "Rot";
            break;
        case 4:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "VUnit";
            break;
        case 5:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "PDesc";
            break;
        case 6:
            info.type = PropertyInfo.OBJECT_CLASS;
            info.name = "PIva";
            break;
        default:break;
        }
	}

	public void setProperty(int index, Object value) {
//		public int IdProd;
//		public int Cant;
//		public int Dev;
//		public int Rot;
//		public float ;
//		public float ;
//		public float PIva;
//		
		switch (index) {
		case 0:
			IdProd = Integer.parseInt(value.toString());
			break;
		case 1:
			Cant = Integer.parseInt(value.toString());
			break;
		case 2:
			Dev = Integer.parseInt(value.toString());
			break;
		case 3:
			Rot = Integer.parseInt(value.toString());
			break;
		case 4:
			VUnit = Float.parseFloat(value.toString());
			break;
		case 5:
			PDesc = Float.parseFloat(value.toString());
			break;
		case 6:
			PIva = Float.parseFloat(value.toString());
			break;
		default:
			break;
		}
	}

}
