package zin.file;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import zin.file.ZinSerializer.SerializedObjectBO;
import zin.reflect.ZinReflect;

public class ZinSerializer {

	public static String DEFAULT_FILE_FORMAT = ".serlz";
	public static final String DEFAULT_FILE_NAME = "zin"+DEFAULT_FILE_FORMAT;
	
	
	public static ZinFile zinFile = new ZinFile();
	
	/**
	 * ZinSerializer.serialize(new Object(){}.getClass().getEnclosingMethod(),
	 * 			ZinIO.arrayToList(ServiceRequest.class, ServiceResponse.class),
	 * 			ZinIO.arrayToList(serviceRequest, serviceResponse));
	 * Object will be saved into className.methodName.serlz
	 * @param method : new Object(){}.getClass().getEnclosingMethod()
	 * @param classes : classes of each method parameters in correct order
	 * @param objList : All the method parameters in correct order
	 */
	public static void serializeMethodAndParameters(Method method, List<Class> classes, List<Object> objList){
		String fileName = method.getDeclaringClass().getSimpleName()+"."+method.getName()+DEFAULT_FILE_FORMAT;
		serializeMethodAndParameters(fileName, method, classes, objList);
	}
	
	/**
	 * ZinSerializer.serialize("zin.serlz", new Object(){}.getClass().getEnclosingMethod(),
	 * 			ZinIO.arrayToList(ServiceRequest.class, ServiceResponse.class),
	 * 			ZinIO.arrayToList(serviceRequest, serviceResponse));
	 * @param fileName
	 * @param method : new Object(){}.getClass().getEnclosingMethod()
	 * @param classes : classes of each method parameters in correct order
	 * @param objList : All the method parameters in correct order
	 */
	public static void serializeMethodAndParameters(String fileName, Method method, List<Class> classes, List<Object> objList){
		try {
			String shouldSerialize = zinFile.getPropertiesFileValueFromKey("zin.properties", "shouldSerialize");
			if(shouldSerialize.equals("true")){
				ZinPrivate.serializeMethodAndParameters(fileName+DEFAULT_FILE_FORMAT, method, classes, objList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * <pre>
	 * Suppose Employee class doesn't implement Serializable
	 * But all its fields Address address, String name, do
	 * Then it will return a list of
	 * 		SerializedObjectBO (Address.class, address)
	 * 		SerializedObjectBO (String.class, name)
	 * It's not completed yet
	 * </pre>
	 * @param type
	 * @param value
	 * @return
	 */
	public static List<SerializedObjectBO> getVariableListToSerialize(Class<?> type, Object value){
		List<SerializedObjectBO> outputList = new ArrayList<>();
		if(ZinReflect.doesClassImplementOrExtend(Serializable.class, type)){
			outputList.add(new SerializedObjectBO(type, value));
			return outputList;
		}
		return null;
	}
	
	
	/**
	 * Use this method on only those files which are serialized by this class's method
	 * @param fileName
	 */
	public static void deserializeMethodAndParametersAndCall(String fileName){
		try{
			ZinPrivate.deserializeMethodAndParametersAndCall(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class SerializedObjectBO implements Serializable{
		
		private static final long serialVersionUID = 2742188140708251473L;
		
		public Class type;
		public Object value;
		public SerializedObjectBO(Class type, Object value) {
			this.type = type;
			this.value = value;
		}
		public <T> T getObject() throws Exception{
			return (T) ZinReflect.castObject(type, value);
		}
	}

	
	private static class ZinPrivate{
		public static void serializeMethodAndParameters(String fileName, Method method, List<Class> classes, List<Object> objList) throws Exception{
			int size = classes.size();
			List<SerializedObjectBO> objectToSerializedBOList = new ArrayList<ZinSerializer.SerializedObjectBO>();
			for(int i=0 ; i<size ; i++){
				objectToSerializedBOList.add(new SerializedObjectBO(classes.get(i), objList.get(i)));
			}
			List<Object> objectListToWrite = new ArrayList<>();
			objectListToWrite.add(method.getDeclaringClass());
			objectListToWrite.add(method.getName());
			objectListToWrite.add(method.getParameterTypes());
			objectListToWrite.add(objectToSerializedBOList);
			zinFile.writeObject(fileName, objectListToWrite);
		}
		@SuppressWarnings("unchecked")
		public static void deserializeMethodAndParametersAndCall(String fileName) throws Exception{
			List<Object> objList = (List<Object>) zinFile.readObject(fileName);
			Method method = deserializeMethod(objList);
			List<SerializedObjectBO> objectToSerializeBOList = (List<SerializedObjectBO>) objList.get(3);
			Object[] methodParametersArr = getMethodParametersArray(objectToSerializeBOList);
			Class<?> classToBeInstantiated = (Class<?>)objList.get(0);
			ZinReflect.invokeMethod(classToBeInstantiated, method, methodParametersArr);
		}
		private static Method deserializeMethod(List<Object> objList) throws Exception{
			Class<?> declaringClass = (Class<?>) objList.get(0);
			String methodName = (String) objList.get(1);
			Class<?>[] parameterTypes = (Class<?>[]) objList.get(2);
			return declaringClass.getMethod(methodName, parameterTypes);
		}
		private static Object[] getMethodParametersArray(List<SerializedObjectBO> objectToSerializeBOList) throws Exception{
			List<Object> methodParametersList = new ArrayList<>(); 
			for(SerializedObjectBO objectToSerializedBO : objectToSerializeBOList){
				methodParametersList.add(objectToSerializedBO.getObject());
			}
			return methodParametersList.stream().toArray(Object[]::new);
		}
	}
}
