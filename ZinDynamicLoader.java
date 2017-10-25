package zin.reflect;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

// Added comment
public class ZinDynamicClassLoader {

	private String classToBeInstantiatedFullName;
	private String methodName;
	private Object[] methodParametersArr;
	
	/**
	 * @param classToBeInstantiatedFullName : "java.lang.String"
	 * @param methodName : "substring"
	 * @param methodParametersArr : new Object[] {0, 3}
	 */
	public ZinDynamicClassLoader(String classToBeInstantiatedFullName, String methodName, Object[] methodParametersArr) {
		this.classToBeInstantiatedFullName = classToBeInstantiatedFullName;
		this.methodName = methodName;
		this.methodParametersArr = methodParametersArr;
	}

	/**
	 * <pre>
	 * Usage:
	 * String className = "zinData.ZinDynamicCaller"; 
	 * String filePath = PROJECT_DIR+"\\zinData\\ZinDynamicCaller.java"; 
	 * new ZinDynamicClassLoader(className, "callDynamically", new Object[] {"someStringParameter"})
	 * .runClassDynamically(new File(filePath));
	 * 
	 * If I don't make methods like runClassDynamically(File, File...)
	 * calling runClassDynamically() will result in a compile time error as ambiguous 
	 * </pre>
	 * @param supportingJavaFile : There has to be at least one File to call this method
	 * @param supportingJavaFiles
	 * @return : returns the returned value if any. null in case of void methods
	 * @throws Exception
	 */
	public Object runClassDynamically(final File supportingJavaFile, final File...supportingJavaFiles) throws Exception{
		return runClassDynamically(classToBeInstantiatedFullName, methodName, methodParametersArr, supportingJavaFile, supportingJavaFiles);
	}
	
	/**
	 * @param classToBeInstantiatedFullName : "java.lang.String"
	 * @param methodName : "substring"
	 * @param methodParametersArr : new Object[] {0, 3}
	 * @param file : there has to be at least one file which you wanna compile and run
	 * @param supportingJavaFiles : In this case, String class doesn't use any more classes than which already are in the classpath.
	 * 	but you can add as many classes which are to be compiled as well. So classToBeInstantiatedFullName.methodName can use them
	 * @return : returns the returned value if any. null in case of void methods
	 * @throws Exception
	 */
	public Object runClassDynamically(String classToBeInstantiatedFullName, String methodName, Object[] methodParametersArr, File file, File...supportingJavaFiles) throws Exception{
		URLClassLoader classLoader = null;
		try{
			/** Compilation Requirements *********************************************************************************************/
	        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	        // Adding all the classpaths before compiling because dynamically loaded class could be using your classes
	        List<String> optionList = new ArrayList<String>();
	        optionList.add("-classpath");
	        optionList.add(System.getProperty("java.class.path"));
	        
	        File[] temp = new File[supportingJavaFiles.length+1];
	        ZinPrivate.copyArray(supportingJavaFiles, file, temp);
	        supportingJavaFiles = temp;
	
	        Iterable<? extends JavaFileObject> compilationUnit
	                = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(supportingJavaFiles));
	        JavaCompiler.CompilationTask task = compiler.getTask(
	            null, 
	            fileManager, 
	            diagnostics, 
	            optionList, 
	            null, 
	            compilationUnit);
            /** 
             * <pre>
             * Compilation Requirements
             * Following method task.call() compiles the class. This method should only be called once.
             * Subsequent calls to this method throw IllegalStateException.
             * </pre> 
             */
	        if (task.call()) {
	        	return ZinReflect.invokeMethod(classToBeInstantiatedFullName, methodName, methodParametersArr);
	        } else {
	            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
	                System.out.format("Error on line %d in %s%n",
	                        diagnostic.getLineNumber(),
	                        diagnostic.getSource().toUri());
	                System.out.println("error message ="+diagnostic.getMessage(Locale.ENGLISH));
	            }
	        }
	        fileManager.close();
	        return null;
		} catch(Exception e){
			throw new Exception(e);
		} finally{
			if(classLoader != null)
				classLoader.close();
		}
	}
	
	public Object runClassDynamically(String classToBeInstantiatedFullName, String methodName, Object[] methodParametersArr, final String fileName, final String... fileNameArr) throws Exception{
		int n = fileNameArr.length;
		File[] files = new File[n];
		for(int i=0 ; i<n ; i++){
			files[i] = new File(fileNameArr[i]);
		}
		return runClassDynamically(classToBeInstantiatedFullName, methodName, methodParametersArr, new File(fileName), files);
	}
	
	private static class ZinPrivate{

		private static <T> void copyArray(T[] sourceArr, T[] destArr){
			int i=0;
			// System.arraycopy() is only efficient if arrays are huge or it's overhead
			for(T t : sourceArr){
				destArr[i++] = t;
			}
		}
		
		/**
		 * @param sourceArr : source Array
		 * @param sourceElementCopyInTheEnd : source Element Copy In The End
		 * @param destArr : dest array = sourceArr + sourceElementCopyInTheEnd
		 */
		private static <T> void copyArray(T[] sourceArr, T sourceElementCopyInTheEnd, T[] destArr){
			// Error: Cannot create a generic array of T
			// T[] tArr = new T[sourceArr.length];
			// Even this throws ClassCastException
			// T[] res = (T[]) new Object[input.size()];
			copyArray(sourceArr, destArr);
			destArr[sourceArr.length] = sourceElementCopyInTheEnd;
		}
	}
	

	public String getClassToBeInstantiatedFullName() {
		return classToBeInstantiatedFullName;
	}

	public void setClassToBeInstantiatedFullName(String classToBeInstantiatedFullName) {
		this.classToBeInstantiatedFullName = classToBeInstantiatedFullName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getMethodParametersArr() {
		return methodParametersArr;
	}

	public void setMethodParametersArr(Object[] methodParametersArr) {
		this.methodParametersArr = methodParametersArr;
	}

}
