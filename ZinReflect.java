package zin.reflect;

import java.beans.Statement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ZinReflect {
	static public boolean isFieldStatic(Field field){
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
	        return true;
	    }
		return false;
	}
	

	/**
	 * write this in toString() ZinReflection.printToString(this.getClass(), this);
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
				String fieldTypeName = field.getType().getSimpleName();
				result.append(fieldTypeName+"  ");
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				result.append(
						field.get(castObject(class1, obj))
						);
			} catch ( IllegalAccessException ex ) {
				System.out.println(ex);
			} catch (Exception e) {
				e.printStackTrace();
			}
			result.append(newLine);
		}
		result.append("}");
		return result.toString();
	}
	
	public static <T> T castObject(Class<T> type, Object object) throws Exception{
		/*
		 * //--Following fails--
		 * Object o = "A string";
		 * --compile time can't convert from capture#1-of ? to String--
		 * String strs = Class.forName("java.lang.String").cast(o);
		 * -- remedy--
		 * Class<String> strClass = (Class<String>) Class.forName("java.lang.String");
		 * String str = strClass.cast(object);
		 */
		if(type.isInstance(object)){
			return type.cast(object);
		}
		else{
			throw new Exception("object "+object+" is not an instance of "+type);
		}
	}

	public static void invokeMethod(Object classInstance, String methodName, Object[] methodParametersArr) throws Exception{
		Statement statement = new Statement(classInstance, methodName, methodParametersArr);
		statement.execute();
	}
	public static void invokeMethod(Class<?> classToBeInstantiated, String methodName, Object[] methodParametersArr) throws Exception{
		invokeMethod(classToBeInstantiated.newInstance(), methodName, methodParametersArr);
	}
	public static void invokeMethod(Object classInstance, Method method, Object[] methodParametersArr) throws Exception{
		invokeMethod(classInstance, method.getName(), methodParametersArr);
	}
	public static void invokeMethod(Class<?> classToBeInstantiated, Method method, Object[] methodParametersArr) throws Exception{
		invokeMethod(classToBeInstantiated.newInstance(), method.getName(), methodParametersArr);
	}
	
	private static class ZinPrivate{
	}
}
