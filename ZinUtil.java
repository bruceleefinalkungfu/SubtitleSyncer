package com.zycus.einvoice.startup;

import java.beans.Statement;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.zycus.workflow.actor.Actor;
import com.zycus.workflow.actor.RoleActor;
import com.zycus.workflow.actor.SelectableActor;
import com.zycus.workflow.actor.SubprocessActor;
import com.zycus.workflow.actor.UserActor;

import zin.file.ZinFile;
import zin.file.ZinIO;
import zin.file.ZinSerializer;
import zin.reflect.ZinReflect;

public class ZinUtil {
	
	private static ZinSerializer serializer;
	private static ZinReflect reflect;
	private static ZinFile zinFileService;
	private static ZinIO zinIO = ZinIO.INSTANCE;
	
	private static final String DEFAULT_FILE_NAME = "zin.serlz"; 
	
	static{
		zinFileService = new ZinFile();
	}
	
	private ZinUtil(){
	}
	

	/**
	 * @param fileName : file should be written in a properties file format
	 * @return
	 * @
	 */
	public static Properties getPropertiesObjectFromFile(String fileName) {
		try {
			return zinFileService.getPropertiesObjectFromFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Properties files are in a key, value format. get value from key
	 * @param fileName
	 * @param key
	 * @return
	 * @
	 */
	public static String getPropertiesFileValueFromKey(String fileName, String key) {
		try{
			return getPropertiesObjectFromFile(fileName).getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Class getActorTypeClass(Actor actor){
		if(SubprocessActor.class.isInstance(actor))
			return SubprocessActor.class;
		else if(SelectableActor.class.isInstance(actor))
			return SelectableActor.class;
		else if(UserActor.class.isInstance(actor))
			return UserActor.class;
		else if(RoleActor.class.isInstance(actor))
			return RoleActor.class;
		//if(true)
		return null;
	}
	
	/**
	 * @param folderPath
	 * @param onlyFileFormatsAllowed : like ".java", ".XML". Leave blank if all files allowed
	 * @return
	 */
	public static List<File> getAllFiles(String folderPath, String ... onlyFileFormatsAllowed) {
		try {
			return zinFileService.getAllFiles(folderPath, onlyFileFormatsAllowed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getStringFromFile(String fileName) {
		try {
			return zinFileService.getStringFromFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<String> getListOfStringLineFromFile(String fileName) {
		try {
			return zinFileService.getListOfStringLineFromFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getBytesOfFile(String fileName) {
		try {
			return zinFileService.getBytesOfFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeObject(String fileName, Object obj) {
		try {
			zinFileService.writeObject(fileName, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Object readObject(String fileName) {
		try {
			return zinFileService.readObject(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void write(String fileName, byte[] arr) {
		try {
			zinFileService.write(fileName, arr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void write(String fileName, String toWrite) {
		try {
			zinFileService.write(fileName, toWrite);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void write(String fileName, String[] toWriteArr) {
		try {
			zinFileService.write(fileName, toWriteArr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static byte[] encodeFromByteToBCD(byte[] brr) {
		return zinFileService.encodeFromByteToBCD(brr);
	}
	/**
	 * Call this method many times
	 * but in the end call saveAppendedFile to save the file
	 * @param fileName 
	 * @param toWrite
	 * @ 
	 */
	public static void append(String fileName, String toWrite) {
		try {
			zinFileService.append(fileName, toWrite);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Call this method many times
	 * but in the end call saveAppendedFile to save the file
	 * @param fileName
	 * @param toWrite
	 * @
	 */
	public static void appendln(String fileName, String toWrite) {
		append(fileName, toWrite+System.getProperty("line.separator"));
	}


	/**
	 * ZinUtil.serialize(new Object(){}.getClass().getEnclosingMethod(), 
	 * 		Arrays.asList(new Class[]{workflowInstance.getClass(), userTaskNode.getClass()}),
	 * 	 	Arrays.asList(new Object[]{workflowInstance, userTaskNode}));
	 * @param method : new Object(){}.getClass().getEnclosingMethod()
	 * @param classes : classes of each method parameters in correct order
	 * @param objList : All the method parameters in correct order
	 */
	public static void serialize(Method method, List<Class> classes, List<Object> objList){
		ZinSerializer.serialize(method, classes, objList);
	}
	
	/**
	 * ZinUtil.serialize("zin.serlz", new Object(){}.getClass().getEnclosingMethod(), 
	 * 		Arrays.asList(new Class[]{workflowInstance.getClass(), userTaskNode.getClass()}),
	 * 	 	Arrays.asList(new Object[]{workflowInstance, userTaskNode}));
	 * @param fileName
	 * @param method : new Object(){}.getClass().getEnclosingMethod()
	 * @param classes : classes of each method parameters in correct order
	 * @param objList : All the method parameters in correct order
	 */
	public static void serialize(String fileName, Method method, List<Class> classes, List<Object> objList){
		ZinSerializer.serialize(fileName, method, classes, objList);
	}
	
	/**
	 * Use this method on only those files which are serialized by this class's method
	 * @param fileName
	 */
	public static void deserializeAndCall(String fileName){
		ZinSerializer.deserializeAndCall(fileName);
	}

	/**
	 * write this in toString() ZinReflection.printToString(this.getClass(), this);
	 * @param class1 : this.getClass()
	 * @param obj : 'this' instance
	 * @return
	 */
	static public String printToString(Class class1, Object obj){
		return ZinReflect.printToString(class1, obj);
	}
	public static String input(){
		return zinIO.input();
	}
	/**
	 * @param msg : msg like "please enter file name"
	 * @return
	 */
	public static String input(String msg){
		return zinIO.input(msg);
	}
	public static void print(String str){
		zinIO.print(str);
	}
	public static void invokeMethod(Object classInstance, String methodName, Object[] methodParametersArr) throws Exception{
		ZinReflect.invokeMethod(classInstance, methodName, methodParametersArr);
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
	
	public static <T> T getPrivateFieldValue(Object instanceFromWhichYouWantPrivateField, Class<?> instanceClass, Class<T> fieldType, String fieldName){
		try{
			return ZinReflect.getPrivateFieldValue(instanceFromWhichYouWantPrivateField, instanceClass, fieldType, fieldName);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void changeServiceRequest(Object[] methodParams){
	}

	public static void handleRestService(String task){
		String fileName = DEFAULT_FILE_NAME;
		if(task==null){
			System.out.println("default file "+DEFAULT_FILE_NAME+" opened");
		} else if(task.equals("create")){
			System.out.println("create task");
			fileName = input("enter file name");
		}
		deserializeAndCall(fileName);
	}
}
