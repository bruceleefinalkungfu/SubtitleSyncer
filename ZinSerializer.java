package zin.file;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import zin.reflect.ZinReflect;

/**
 * <pre>
 * When you want to serialize a method call with all their parameters, use this class
 * use method serializeMethodAndParameters to serialize
 * use method deserializeMethodAndParametersAndCall to deserialize and call
 * </pre>
 * - Change Class to Class<?>
 * @author anurag.awasthi
 *
 */
public class ZinSerializer {

	public static String DEFAULT_FILE_FORMAT = ".serlz";
	public static final String DEFAULT_FILE_NAME = "zin"+DEFAULT_FILE_FORMAT;
	
	
	public static ZinFile zinFile = new ZinFile();
	
	/**
	 * <pre>
	 * ZinSerializer.serializeMethodAndParameters(new Object(){}.getClass().getEnclosingMethod(),
	 * 			ZinIO.arrayToList(ServiceRequest.class, ServiceResponse.class),
	 * 			ZinIO.arrayToList(serviceRequest, serviceResponse));
	 * Object will be saved into className.methodName.serlz
	 * </pre>
	 * @param method : new Object(){}.getClass().getEnclosingMethod()
	 * @param classes : classes of each method parameters in correct order
	 * @param objList : All the method parameters in correct order
	 */
	public static void serializeMethodAndParameters(Method method, List<Class> classes, List<Object> objList){
		String fileName = method.getDeclaringClass().getSimpleName()+"."+method.getName()+DEFAULT_FILE_FORMAT;
		serializeMethodAndParameters(fileName, method, classes, objList);
	}
	
	/**
	 * ZinSerializer.serializeMethodAndParameters("zin.serlz", new Object(){}.getClass().getEnclosingMethod(),
	 * 			ZinIO.arrayToList(ServiceRequest.class, ServiceResponse.class),
	 * 			ZinIO.arrayToList(serviceRequest, serviceResponse));
	 * @param fileName
	 * @param method : new Object(){}.getClass().getEnclosingMethod()
	 * @param classes : classes of each method parameters in correct order
	 * @param objList : All the method parameters in correct order
	 */
	public static void serializeMethodAndParameters(String fileName, Method method, List<Class> classes, List<Object> objList){
		try {
				ZinPrivate.serializeMethodAndParameters(fileName, method, classes, objList);
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
	
	public static class MethodAndParametersBO implements Serializable{

		private static final long serialVersionUID = 7574699104008578078L;
		
		public Class<?> classHavingMethod;
		public String methodName;
		public Class<?> parametersTypeArr [];
		List<SerializedObjectBO> parametersList;
		public MethodAndParametersBO(Class<?> classHavingMethod, String methodName, Class<?>[] parametersTypeArr,
				List<SerializedObjectBO> parametersList) {
			this.classHavingMethod = classHavingMethod;
			this.methodName = methodName;
			this.parametersTypeArr = parametersTypeArr;
			this.parametersList = parametersList;
		}
		public MethodAndParametersBO(Method method, List<Object> parameters){
			this.classHavingMethod = method.getDeclaringClass();
			this.methodName = method.getName();
			this.parametersTypeArr = method.getParameterTypes();
			int len = parametersTypeArr.length;
			if(parameters.size() != len){
				throw new IllegalArgumentException("Number of parameters are in method is "+len+" while you sent "+parameters.size()+" params");
			}
			List<SerializedObjectBO> serializedObjectBOs = new ArrayList<>();
			for(int i=0 ; i<len ; i++){
				serializedObjectBOs.add(new SerializedObjectBO(parametersTypeArr[i], parameters.get(i)));
			}
			this.parametersList = serializedObjectBOs;
		}
		public Method getMethod() throws NoSuchMethodException, SecurityException{
			return classHavingMethod.getMethod(methodName, parametersTypeArr);
		}

		public Object[] getMethodParameterObjectsArr() throws Exception{
			List<Object> methodParametersList = new ArrayList<>(); 
			for(SerializedObjectBO objectToSerializedBO : parametersList){
				methodParametersList.add(objectToSerializedBO.getObject());
			}
			return methodParametersList.stream().toArray(Object[]::new);
		}
	}
	
	/**
	 * <pre>
	 * You shouldn't do SerializedObjectBO<T> because then when you prepare a list
	 * You HAVE TO HAVE of 'Object' List< SerializedObjectBO<Object> > objectToSerializeBOList
	 * Then you HAVE TO HAVE a cast of your own when calling getObject()
	 * </pre>
	 * 
	 */
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
			MethodAndParametersBO writeBO = new MethodAndParametersBO(method, objList);
			zinFile.writeObject(fileName, writeBO);
			/*
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
			//*/
		}
		
		public static void deserializeMethodAndParametersAndCall(String fileName) throws Exception{
			MethodAndParametersBO readBO = (MethodAndParametersBO) zinFile.readObject(fileName);
			Method method = readBO.getMethod();
			Object[] methodParametersArr = readBO.getMethodParameterObjectsArr();
			ZinReflect.invokeMethod(readBO.classHavingMethod, method, methodParametersArr);
			/*
			List<Object> objList = (List<Object>) zinFile.readObject(fileName);
			Method method = deserializeMethod(objList);
			List<SerializedObjectBO> objectToSerializeBOList = (List<>) objList.get(3);
			Object[] methodParametersArr = getMethodParametersArray(objectToSerializeBOList);
			Class<?> classToBeInstantiated = (Class<?>)objList.get(0);
			ZinReflect.invokeMethod(classToBeInstantiated, method, methodParametersArr);
			//*/
		}
	}
}
