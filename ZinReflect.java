package zin.reflect;

import java.beans.Expression;
import java.beans.Statement;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zin.file.ZinSerializer.SerializedObjectBO;

/**
 * Bugs
 * 1. CreditMemoEntity printToString doesn't show fields of AbstractInvoiceEntity
 * 2. printToString fails on recursive nesting.
 * 		class A { BObj b; } class B { A aObj; } 
 * @author anurag.awasthi
 *
 */
public class ZinReflect {
	
	private static final Map<String, Boolean> CONSOLE_PRINTABLE_TYPES;
	
	public static final String NEW_LINE = System.getProperty("line.separator");
	
	private static final String IDENTATION_STRING;
	
	static{
		// local variables which are destroyed later
		int numberOfSpace = 4;
		Class<?>[] printableTypes = {Integer.class, Double.class, Float.class, BigInteger.class, BigDecimal.class, int.class, double.class, float.class,
							boolean.class, Boolean.class, String.class, StringBuilder.class, StringBuffer.class, Date.class};
		
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
	public static <T> T getPrivateFieldValue(Class<?> instanceClass, Class<T> fieldType, String fieldName) throws Exception{
		Object instanceOfClass = instanceClass.newInstance();
		Field f = instanceClass.getDeclaredField(fieldName);
		f.setAccessible(true);
		return castObject(fieldType, f.get(instanceOfClass));
		//return (T) castObject(fieldType, f.get(castObject(instanceClass, instanceFromWhichYouWantPrivateField);));
	}
	
	public static <T> void setPrivateFieldValue(Object instanceOfClass, Class<?> instanceClass, Class<T> fieldType, String fieldName, T fieldValue) throws Exception{
		Field f = instanceClass.getDeclaredField(fieldName);
		f.setAccessible(true);
		f.set(instanceOfClass, fieldValue);
	}
	
	/**
	 * <pre>
	 * There has to be at least one element in the list.
	 * Or it returns null which you've to check
	 * </pre>
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static Class getListGenericTypeClass(List list){
		if(list.size()>0)
			return list.get(0).getClass();
		else
			return null;
		/*
		// Using this way you can only fetch type of Fields of class not local var of method
		Field listField = classInstance.getDeclaredField(fieldName);
        ParameterizedType listType = (ParameterizedType) listField.getGenericType();
        Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
        return listClass;
        //*/
	}

	/**
	 * <pre>
	 * Suppose you wanna check if class ArrayList implements List
	 * doesClassImplementOrExtend(ArrayList.class, List.class);
	 * </pre> 
	 * @param dadClassOrNot : class you wanna find out if it's superclass of your class or not
	 * @param yourClass		: class you wanna check
	 * @return
	 */
	public static boolean doesClassImplementOrExtend(Class<?> dadClassOrNot, Class<?> yourClass){
		return dadClassOrNot.isAssignableFrom(yourClass);
	}
	
	/**
	 * write this in toString() return ZinReflection.printToString(this.getClass(), this);
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

	/**
	 * @param classInstance 
	 * @param methodName
	 * @param methodParametersArr
	 * @return 
	 * @throws Exception
	 */
	public static Object invokeMethod(Object classInstance, String methodName, Object[] methodParametersArr) throws Exception{
		/**
		 * unlike public Object Method.invoke(Object obj, Object... args) 
		 * this facility has not been adapted to support varargs parameters, 
		 * so you have to create a parameter array manually.
		 * AND
		 * it only works for public methods.
		 * The reason WE used STATEMENT instead of Method because I wanted methodParametersArr in an array
		 * Although it DOESN'T RETURN value.
		 */
		/*
		Statement statement = new Statement(classInstance, methodName, methodParametersArr);
		statement.execute();
		//*/
		/**
		 * We are using Expressions because we want the return value
		 */
		Expression expression = new Expression(classInstance, methodName, methodParametersArr);
		/**
		 * Calling execute, sets the 'value' field/property of Expression instance to the returned value
		 * if return type was void, 'value' is set to null.
		 */
		expression.execute();
		/**
		 * 'value' by default is set to some unique value as an indication that expression is not executed yet
		 * if getValue() is called and value is that unique then it executes/invoke the method
		 * and saves the returned value in 'value' field and returns it.
		 */
		return expression.getValue();
	}
	public static Object invokeMethod(Object classInstance, Method method, Object[] methodParametersArr) throws Exception{
		return invokeMethod(classInstance, method.getName(), methodParametersArr);
	}
	public static Object invokeMethod(Class<?> classToBeInstantiated, String methodName, Object[] methodParametersArr) throws Exception{
		return invokeMethod(classToBeInstantiated.newInstance(), methodName, methodParametersArr);
	}
	public static Object invokeMethod(Class<?> classToBeInstantiated, Method method, Object[] methodParametersArr) throws Exception{
		return invokeMethod(classToBeInstantiated.newInstance(), method.getName(), methodParametersArr);
	}
	public static Object invokeMethod(String classFullName, String methodName, Object[] methodParametersArr) throws Exception{
		return invokeMethod(getClassInstanceFromFullName(classFullName).newInstance(), methodName, methodParametersArr);
	}
	public static Class<?> getClassInstanceFromFullName(String classFullName) throws Exception{
		// Class.forName() loads and initializes the class.
		// while classLoader waits loads and delays initialization until class is used
		URLClassLoader classLoader = null;
		Class<?> loadedClass = null;
		try{
            // Create a new custom class loader, pointing to the directory that contains the compiled
            // classes, this should point to the top of the package structure!
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
		public static StringBuilder printToString(Class<?> type, List list, String identation){
			StringBuilder result = new StringBuilder();
			int size = list.size();
			Class listGenericType = getListGenericTypeClass(list);
			if(listGenericType == null) return new StringBuilder("null");
			result.append(IDENTATION_STRING + identation + "[" + NEW_LINE);
			for(int i=0 ; i<size ; i++){
				result.append( ZinPrivate.printToString(listGenericType, list.get(i), identation) );
				result.append(" ," + NEW_LINE);
			}
			return result;
		}
		public static StringBuilder printToString(Class<?> type, Object value, String identation){
			if(value == null) return new StringBuilder("null");
			if(isItList(type)){
				return printToString(type, (List)value, identation);
			}
			
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
					// Printing value
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
		
		private static boolean isItList(Class<?> fieldType){
			return doesClassImplementOrExtend(List.class, fieldType);
		}
		
		private static boolean isFieldConsolePrintable(Class<?> fieldType){
			return CONSOLE_PRINTABLE_TYPES.get(fieldType.getName()) != null;
		}
	}
}
