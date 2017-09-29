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

public class ZinDynamicClassLoader {

	private static String DEFAULT_CLASS_NAME = "Zin.java";
	
	private static final String PROJECT_DIR = System.getProperty("user.dir");
	
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
	 * new ZinDynamicClassLoader("testcompile.HelloWorld", "zin", null)
	 * .runClassDynamically(new File(PROJECT_DIR+"\\testcompile\\HelloWorld.java"));
	 * If I don't make methods like runClassDynamically(File, File...)
	 * calling runClassDynamically() will result in a compile time error as ambiguous 
	 * </pre>
	 * @param supportingJavaFile : There has to be at least one File to call this method
	 * @param supportingJavaFiles
	 * @throws Exception
	 */
	public void runClassDynamically(final File supportingJavaFile, final File...supportingJavaFiles) throws Exception{
		runClassDynamically(classToBeInstantiatedFullName, methodName, methodParametersArr, supportingJavaFile, supportingJavaFiles);
	}
	
	/**
	 * <pre>
	 * Usage:
	 * new ZinDynamicClassLoader("testcompile.HelloWorld", "zin", null)
	 * .runClassDynamically(new File(PROJECT_DIR+"\\testcompile\\HelloWorld.java"));
	 * </pre>
	 * @param classToBeInstantiatedFullName : "java.lang.String"
	 * @param methodName : "substring"
	 * @param methodParametersArr : new Object[] {0, 3}
	 * @param file : there has to be at least one file which you wanna compile and run
	 * @param supportingJavaFiles : In this case, String class doesn't use any more classes than which already are in the classpath.
	 * 	but you can add as many classes which are to be compiled as well. So classToBeInstantiatedFullName.methodName can use them
	 * </pre>
	 * @throws Exception
	 */
	public void runClassDynamically(String classToBeInstantiatedFullName, String methodName, Object[] methodParametersArr, File file, File...supportingJavaFiles) throws Exception{
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
	        	ZinReflect.invokeMethod(classToBeInstantiatedFullName, methodName, methodParametersArr);
	        } else {
	            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
	                System.out.format("Error on line %d in %s%n",
	                        diagnostic.getLineNumber(),
	                        diagnostic.getSource().toUri());
	                System.out.println("error message ="+diagnostic.getMessage(Locale.ENGLISH));
	            }
	        }
	        fileManager.close();
		} catch(Exception e){
			throw new Exception(e);
		} finally{
			if(classLoader != null)
				classLoader.close();
		}
	}
	
	public void runClassDynamically(String classToBeInstantiatedFullName, String methodName, Object[] methodParametersArr, final String fileName, final String... fileNameArr) throws Exception{
		int n = fileNameArr.length;
		File[] files = new File[n];
		for(int i=0 ; i<n ; i++){
			files[i] = new File(fileNameArr[i]);
		}
		runClassDynamically(classToBeInstantiatedFullName, methodName, methodParametersArr, new File(fileName), files);
	}
	
	private static class ZinPrivate{

		private static <T> void copyArray(T[] sourceArr, T[] destArr){
			int i=0;
			// System.arraycopy() is only efficient if arrays are huge or it's overhead
			for(T t : sourceArr){
				destArr[i++] = t;
			}
		}
		
		private static <T> void copyArray(T[] sourceArr, T sourceElementCopyInTheEnd, T[] destArr){
			// Error: Cannot create a generic array of T
			// T[] tArr = new T[sourceArr.length];
			// Even this throws ClassCastException
			// T[] res = (T[]) new Object[input.size()];
			copyArray(sourceArr, destArr);
			destArr[sourceArr.length] = sourceElementCopyInTheEnd;
		}
	}
	
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("package testcompile;\n");
        sb.append("public class HelloWorld implements zin.reflect.ZinDynamicClassLoader.DoStuff {\n");
        sb.append("    public void zin() {\n");
        sb.append("        System.out.println(\"Zin method\");\n");
        sb.append("    }\n");
        sb.append("    public void doStuff() {\n");
        sb.append("        System.out.println(\"Dynamnically written Hello world\");\n");
        sb.append("    }\n");
        sb.append("}\n");

        File helloWorldJava = new File("testcompile/HelloWorld.java");
        if (helloWorldJava.getParentFile().exists() || helloWorldJava.getParentFile().mkdirs()) {

            try {
                Writer writer = null;
                try {
                    writer = new FileWriter(helloWorldJava);
                    writer.write(sb.toString());
                    writer.flush();
                } finally {
                    if(writer!= null)
                    	writer.close();
                }
                

    			new ZinDynamicClassLoader("testcompile.HelloWorld", "zin", null)
    			//.addClassDirectoryPath("E:\\eproc_kit\\eproc\\zinProj\\bin\\testcompile\\HelloWorld.class")
    			.runClassDynamically(new File("E:\\eproc_kit\\eproc\\zinProj\\testcompile\\HelloWorld.java"));
                
                /** Compilation Requirements *********************************************************************************************/
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                // This sets up the class path that the compiler will use.
                // I've added the .jar file that contains the DoStuff interface within in it...
                List<String> optionList = new ArrayList<String>();
                optionList.add("-classpath");
                optionList.add(System.getProperty("java.class.path") + ";./dist/zinProj.jar");

                Iterable<? extends JavaFileObject> compilationUnit
                        = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(helloWorldJava));
                JavaCompiler.CompilationTask task = compiler.getTask(
                    null, 
                    fileManager, 
                    diagnostics, 
                    optionList, 
                    null, 
                    compilationUnit);
                if (task.call()) {
    	        	ZinReflect.invokeMethod("testcompile.HelloWorld", "doStuff", null);
                    /** Load and execute *************************************************************************************************/
                    System.out.println("Yipe");
                    // Create a new custom class loader, pointing to the directory that contains the compiled
                    // classes, this should point to the top of the package structure!
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
                    // Load the class from the classloader by name....
                    Class<?> loadedClass = classLoader.loadClass("testcompile.HelloWorld");
                    // Create a new instance...
                    Object obj = loadedClass.newInstance();
                    // Santity check
                    if (obj instanceof DoStuff) {
                        // Cast to the DoStuff interface
                        DoStuff stuffToDo = (DoStuff)obj;
                        // Run it baby
                        stuffToDo.doStuff();
                    }
                    /************************************************************************************************* Load and execute **/
                } else {
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        System.out.format("Error on line %d in %s%n",
                                diagnostic.getLineNumber(),
                                diagnostic.getSource().toUri());
                    }
                }
                fileManager.close();
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }

    public static interface DoStuff {

        public void doStuff();
    }

}
