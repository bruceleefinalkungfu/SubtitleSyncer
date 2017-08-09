package com.zycus.common.util;

import java.lang.reflect.Field;


public class ZinReflect {
	static public boolean isFieldStatic(Field field){
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
	        return true;
	    }
		return false;
	}
	
	/**
	 * write this in toString(), ZinReflect.printToString(this.getClass(), this);
	 * @param class1 : this.getClass()
	 * @param obj : 'this' instance
	 * @return
	 */
	static public String printToString(Class class1, Object obj){
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		result.append( class1.getName() );
		result.append( " = {" );
		result.append(newLine);
		//determine fields declared in this class only (no fields of superclass)
		Field[] fields = class1.getDeclaredFields();
		//print field names paired with their values
		for ( Field field : fields  ) {
			if(isFieldStatic(field))
				continue;
			result.append("  ");
			try {
				String fieldTypeName = field.getType().getName();
				result.append(fieldTypeName.substring(fieldTypeName.lastIndexOf(".")+1)+"  ");
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				result.append( field.get( class1.cast(obj)) );
			} catch ( IllegalAccessException ex ) {
				System.out.println(ex);
			}
			result.append(newLine);
		}
		result.append("}");
		return result.toString();
	}
	
	private static class ZinPrivate{
		
	}
}
