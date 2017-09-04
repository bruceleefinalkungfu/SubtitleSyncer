package zin.reflect;

import java.beans.Statement;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zin.file.ZinSerializer.SerializedObjectBO;

public class ZinReflect {
	
	private static final Map<String, Boolean> CONSOLE_PRINTABLE_TYPES;
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private static final String IDENTATION_STRING;
	
	static{
		// local variables which are destroyed later
		int numberOfSpace = 4;
		Class<?>[] printableTypes = {Integer.class, Double.class, Float.class, BigInteger.class, BigDecimal.class, int.class, double.class, float.class,
							boolean.class, Boolean.class, String.class, StringBuilder.class, StringBuffer.class};
		
		CONSOLE_PRINTABLE_TYPES = new HashMap<>();
		for(Class<?> class1 : printableTypes){
			CONSOLE_PRINTABLE_TYPES.put(class1.getName(), true);
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0 ; i<numberOfSpace ; i++)
			sb.append(' ');
		IDENTATION_STRING = sb.toString();
	}
	
	static public boolean isFieldStatic(Field field){
		if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
	        return true;
	    }
		return false;
	}
	
	/**
	 * suppose class Employee has private String employeeName..
	 * @param instanceFromWhichYouWantPrivateField : new Employee()
	 * @param instanceClass : Employee.class
	 * @param fieldType : String.class
	 * @param fieldName : "employeeName"
	 * @return
	 * @throws Exception
	 */
	public static <T> T getPrivateFieldValue(Object instanceFromWhichYouWantPrivateField, Class<?> instanceClass, Class<T> fieldType, String fieldName) throws Exception{
		Field f = instanceClass.getDeclaredField(fieldName);
		f.setAccessible(true);
		return castObject(fieldType, f.get(instanceFromWhichYouWantPrivateField));
		//return (T) castObject(fieldType, f.get(castObject(instanceClass, instanceFromWhichYouWantPrivateField);));
	}

	/**
	 * Suppose you wanna check if class Zin implements Serializable
	 * isClassImplementOrExtend(Serializable.class, Zin.class); 
	 * @param dadClassOrNot : class you wanna find out if it's superclass of your class or not
	 * @param yourClass		: class you wanna check
	 * @return
	 */
	public static boolean doesClassImplementOrExtend(Class<?> dadClassOrNot, Class<?> yourClass){
		return dadClassOrNot.isAssignableFrom(yourClass);
	}
	
	/**
	 * Suppose Employee class doesn't implement Serializable
	 * But all its fields Address address, String name, do
	 * Then it will return a list of
	 * 		SerializedObjectBO (Address.class, address)
	 * 		SerializedObjectBO (String.class, name)
	 * It's not completed yet
	 * @param type
	 * @param value
	 * @return
	 */
	public static List<SerializedObjectBO> getVariableListToSerialize(Class<?> type, Object value){
		List<SerializedObjectBO> outputList = new ArrayList<>();
		if(doesClassImplementOrExtend(Serializable.class, type)){
			outputList.add(new SerializedObjectBO(type, value));
			return outputList;
		}
		
		return null;
	}
	
	/**
	 * write this in toString() ZinReflection.printToString(this.getClass(), this);
	 * @param type : this.getClass()
	 * @param value : 'this' instance
	 * @return
	 */
	static public String printToString(Class<?> type, Object value){
		return type.getName() + " = "+ ZinPrivate.printToString(type, value, "").toString();
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
	public static void invokeMethod(Object classInstance, Method method, Object[] methodParametersArr) throws Exception{
		invokeMethod(classInstance, method.getName(), methodParametersArr);
	}
	public static void invokeMethod(Class<?> classToBeInstantiated, String methodName, Object[] methodParametersArr) throws Exception{
		invokeMethod(classToBeInstantiated.newInstance(), methodName, methodParametersArr);
	}
	public static void invokeMethod(Class<?> classToBeInstantiated, Method method, Object[] methodParametersArr) throws Exception{
		invokeMethod(classToBeInstantiated.newInstance(), method.getName(), methodParametersArr);
	}
	public static void invokeMethod(String classFullName, String methodName, Object[] methodParametersArr) throws Exception{
		invokeMethod(getClassInstanceFromFullName(classFullName).newInstance(), methodName, methodParametersArr);
	}
	public static Class<?> getClassInstanceFromFullName(String classFullName) throws Exception{
		// Class.forName() loads and initializes the class.
		// while classLoader waits loads and delays initialization until class is used
		URLClassLoader classLoader = null;
		Class<?> loadedClass = null;
		try{
			classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
			loadedClass = classLoader.loadClass(classFullName);
		} catch(Exception e){
			throw e;
		} finally{
			if(classLoader != null ) classLoader.close();
		}
		return loadedClass;
	}
	
	private static class ZinPrivate{
		public static StringBuilder printToString(Class<?> type, Object value, String identation){
			StringBuilder result = new StringBuilder();
			
			result.append( "{" + NEW_LINE );
			//determine fields declared in this class only (no fields of superclass)
			Field[] fields = type.getDeclaredFields();
			//print field names paired with their values
			for ( Field field : fields  ) {
				// SecurityManager can stop us to make a variable accesible
				field.setAccessible(true);
				if(isFieldStatic(field))
					continue;
				// Identation inside loop
				try {
					String fieldTypeName = field.getType().getSimpleName();
					result.append( IDENTATION_STRING + identation );
					result.append(fieldTypeName +" "+ field.getName() + ": ");
					if(isFieldConsolePrintable(field.getType())){
						result.append( field.get(castObject(type, value)) );
					} else{
						// identation is increased
						result.append(printToString(field.getType(), field.get(castObject(type, value)), identation+IDENTATION_STRING));
					}
				} catch ( IllegalAccessException ex ) {
					System.out.println(ex);
				} catch (Exception e) {
					e.printStackTrace();
				}
				result.append(NEW_LINE);
			}
			result.append(identation+"}");
			return result;
		}
		private static boolean isFieldConsolePrintable(Class<?> fieldType){
			return CONSOLE_PRINTABLE_TYPES.get(fieldType.getName()) != null;
		}
	}
}
