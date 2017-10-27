package zin.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import zin.tools.ZinConstant;
import zin.tools.ZinConstant.OperatingSystem;

/**
 * Bug
 * 	-getBytesOfFile doesn't work for large file having more bytes than Integer.MAX_VALUE
 * 	-close FileInputStream
 * @author anurag.awasthi
 *
 */
public class ZinFile {
	
	private static final String			DEFAULT_ENCODING 				=			"ASCII";
	private static final Charset 		DEFAULT_CHARSET_ENCODING		=			Charsets.toCharset(DEFAULT_ENCODING);
	
	private ZinFileService zinFileService;
	// for every fileName, a fileAppender
	// cuz when appending to a file, I don't wanna open and close a file again and again 
	private static Map<String, FileAppender> fileAppenderMap;
	
	private final ZinPrivate zinPrivate;
	
	static{ 
		fileAppenderMap = new HashMap<>();
	}
	
	public ZinFile() {
		zinPrivate = new ZinPrivate();
		zinFileService = new ZinFileService();
	}
	
	
	/**
	 * <pre>
	 * </pre>
	 * @param fileName : file should be written in a properties file format
	 * @return
	 * @throws Exception
	 */
	public Properties getPropertiesObjectFromFile(String fileName) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getPropertiesObjectFromFile(fileName);
	}
	
	/**
	 * <pre>
	 * Properties files are in a key, value format. get value from key
	 * </pre>
	 * @param fileName
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public String getPropertiesFileValueFromKey(String fileName, String key) throws Exception{
		return getPropertiesObjectFromFile(fileName).getProperty(key);
	}
	

	public Map<String, String> getValueKeyMapFromPropertiesFile(String propertiesFileName) throws Exception{
		Properties prop = getPropertiesObjectFromFile(propertiesFileName);
		Map<String, String> reverseMap = new HashMap<>();
		Set<Object> set = prop.keySet();
		for(Object o : set){
			String key = (String) o;
			reverseMap.put(prop.getProperty(key), key);
		}
		return reverseMap;
	}
	
	public Map<String, String> getKeyValueMapFromPropertiesFile(String propertiesFileName) throws Exception{
		Properties prop = getPropertiesObjectFromFile(propertiesFileName);
		Map<String, String> reverseMap = new HashMap<>();
		Set<Object> set = prop.keySet();
		for(Object o : set){
			String key = (String) o;
			reverseMap.put(key, prop.getProperty(key));
		}
		return reverseMap;
	}
	
	/**
	 * @param directoryPath
	 * @param onlyFileFormatsAllowed : ".java", ".XML". Leave blank if all files allowed
	 * @return
	 */
	public List<File> getAllFiles(String directoryPath, String ... onlyFileFormatsAllowed) throws Exception{
		File folder = getFileFromFileName(directoryPath);
		if(!folder.exists() || !folder.isDirectory()){
			throw new Exception("directory either doesn't exist or not a directory");
		}
		for(int i=0 ; i<onlyFileFormatsAllowed.length ; i++){
			onlyFileFormatsAllowed[i] = onlyFileFormatsAllowed[i].toLowerCase();
		}
		return zinFileService.getAllFiles(folder, onlyFileFormatsAllowed);
	}

	public String getStringFromFile(String fileName) throws Exception{
		return getStringFromFile(fileName, DEFAULT_CHARSET_ENCODING);
	}
	public String getStringFromFile(String fileName, String encoding) throws Exception{
		saveAppendedFile(fileName);
		return getStringFromFile(fileName, Charsets.toCharset(encoding));
	}
	/**
	 * <pre>
	 * It returns null if file doesn't exist in classpath nor in absolute path
	 * </pre>
	 * @param fileName
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public String getStringFromFile(String fileName, Charset encoding) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getStringFromFile(getFileFromFileName(fileName), encoding);
	}

	public List<String> getListOfStringLineFromFile(String fileName) throws Exception{
		return getListOfStringLineFromFile(fileName, DEFAULT_CHARSET_ENCODING);
	}
	public List<String> getListOfStringLineFromFile(String fileName, String encoding) throws Exception{
		return getListOfStringLineFromFile(fileName, Charsets.toCharset(encoding));
	}
	public List<String> getListOfStringLineFromFile(String fileName, Charset encoding) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getListOfStringLineFromFile(getFileFromFileName(fileName), encoding);
	}

	public byte[] getBytesOfFile(String fileName) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getBytesOfFile(getFileFromFileName(fileName));
	}
	
	public void writeObject(String fileName, Object obj) throws Exception{
		saveAppendedFile(fileName);
		zinFileService.writeObject(fileName, obj);
	}
	
	public Object readObject(String fileName) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.readObject(fileName);
	}
	
	public void write(String fileName, byte[] arr) throws Exception{
		saveAppendedFile(fileName);
		zinFileService.write(getFileFromFileName(fileName), arr);
	}

	public void write(String fileName, String toWrite) throws Exception{
		write(fileName, toWrite, DEFAULT_CHARSET_ENCODING);
	}
	public void write(String fileName, String toWrite, String encoding) throws Exception{
		write(fileName, toWrite, Charsets.toCharset(encoding));
	}
	public void write(String fileName, String toWrite, Charset encoding) throws Exception{
		saveAppendedFile(fileName);
		zinFileService.write(getFileIfDoesNotexistCreateIt(fileName), toWrite, encoding);
	}

	public void createFile(String filePathRelativeOrAbsolute) throws IOException{
		zinFileService.createFile(filePathRelativeOrAbsolute);
	}

	/**
	 * <pre>
	 * Call this method many times
	 * but in the end call saveAppendedFile to save the file
	 * </pre>
	 * @param fileName 
	 * @param toWrite
	 * @throws Exception 
	 */
	public void append(String fileName, String toWrite) throws Exception{
		FileAppender fileAppender = fileAppenderMap.get(fileName);
		if(fileAppender==null){
			fileAppender = new FileAppender(fileName);
		}
		zinFileService.append(fileAppender, toWrite);
	}
	/**
	 * <pre>
	 * Call this method many times
	 * but in the end call saveAppendedFile to save the file
	 * </pre>
	 * but in the end call saveAppendedFile to save the file
	 * @param fileName
	 * @param toWrite
	 * @throws Exception
	 */
	public void appendln(String fileName, String toWrite) throws Exception{
		append(fileName, toWrite+System.getProperty("line.separator"));
	}
	
	/**
	 * <pre>
	 * This method MUST be called after you're done appending to a file
	 * So file can be saved, otherwise File won't be saved
	 * </pre>
	 * @param fileName
	 * @throws Exception
	 */
	public void saveAppendedFile(String fileName) throws Exception{
		FileAppender fileAppender = fileAppenderMap.get(fileName);
		if(fileAppender != null){
			fileAppender.closeWriters();
		}
	}
	
	/**
	 * <pre>
	 * Use this method when you have to append the file only once
	 * </pre>
	 * @param fileName
	 * @param toWrite
	 * @throws Exception
	 */
	public void appendOnlyOnce(String fileName, String toWrite) throws Exception{
		append(fileName, toWrite);
		saveAppendedFile(fileName);
	}
	
	public File getFileIfDoesNotexistCreateIt(String filePathRelativeOrAbsolute) throws Exception{
		File f = getFileFromFileName(filePathRelativeOrAbsolute);
		if(f == null)
			createFile(filePathRelativeOrAbsolute);
		return getFileFromFileName(filePathRelativeOrAbsolute);
	}
	
	/**
	 * <pre>
	 * It'll first try to get file from absolute path
	 * If fails it will look for relative path in all classpaths
	 * If fails to get file from classpaths as well, it returns null
	 * </pre>
	 * @param filePathRelativeOrAbsolute
	 * @return
	 * @throws URISyntaxException 
	 */
	public File getFileFromFileName(String filePathRelativeOrAbsolute) throws URISyntaxException{
		if(isFilePathAbsolute(filePathRelativeOrAbsolute)){
			File absoluteFile = zinFileService.getFileFromAbsolutePath(filePathRelativeOrAbsolute);
			return absoluteFile;
		}
		return zinFileService.getFileInstanceFromClasspath(filePathRelativeOrAbsolute);
	}
	
	public boolean isFilePathAbsolute(String filePath){
		if(ZinConstant.OS == OperatingSystem.WINDOWS)
			return filePath.contains(":");
		else
			throw new Error("set value for linux or whatever your os is");
	}
	
	/**
	 * <pre>
	 * It checks for absolutePath if fails then in relative paths 
	 * </pre>
	 * @param filePathRelativeOrAbsolute
	 * @return
	 * @throws Exception
	 */
	public InputStream getInputStreamFromFileName(String filePathRelativeOrAbsolute) throws Exception{
		InputStream in = zinFileService.getInputStreamFromAbsolutePath(filePathRelativeOrAbsolute);
		if( in != null)
			return in;
		return zinFileService.getInputStreamFromClasspath(filePathRelativeOrAbsolute);
	}
	
	private class ZinFileService{
		// It doesn't matter if I choose it private or public cuz inheritance isn't involved
		private ZinFileService() {
		}
		
		public void createFile(String filePathRelativeOrAbsolute) throws IOException{
			FileUtils.touch(new File(filePathRelativeOrAbsolute));
		}
		
		private void writeObject(String fileName, Object obj) throws Exception{
			FileOutputStream fos = new FileOutputStream(fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.close();
		}
		
		private Object readObject(String fileName) throws Exception{
			Object readObject = null;
			ObjectInputStream ois = null;
			FileInputStream fis = null;
			try{
				fis = new FileInputStream(fileName);
				ois = new ObjectInputStream(fis);
				readObject = ois.readObject();
			} catch(Exception e){
				throw e;
			} finally{
				// Close them in order
				if(ois!=null) ois.close();
				if(fis!=null) fis.close();
			}
			return readObject;
		}
		
		private List<File> getAllFiles(File folder, String ... onlyFileFormatsAllowed){
			List<File> files = new LinkedList<>();
			File[] fileArr = folder.listFiles();
			for(File f : fileArr){
				if(f.isDirectory()){
					files.addAll(getAllFiles(f, onlyFileFormatsAllowed));
				} else if(f.isFile()){
					if(isFileOfAllowedFormats(f, onlyFileFormatsAllowed))
						files.add(f);
				}
			}
			return files;
		}
		private boolean isFileOfAllowedFormats(File f, String...arr){
			if(arr.length == 0) return true;
			String fileName = f.getName().toLowerCase();
			for(String s : arr){
				if(fileName.endsWith(s))
					return true;
			}
			return false;
		}
		
		public String getStringFromFile(final File file, final Charset encoding) throws IOException{
			if(file == null)
				return null;
			return FileUtils.readFileToString(file, encoding);
		}

		public List<String> getListOfStringLineFromFile(final File file, final Charset encoding) throws Exception{
			return FileUtils.readLines(file, encoding);
		}

		public byte[] getBytesOfFile(File file) throws Exception{
			return FileUtils.readFileToByteArray(file);
		}
		
		public void write(final File file, final byte[] arr) throws Exception{
			FileUtils.writeByteArrayToFile(file, arr);
		}
		public void write(File file, String toWrite, Charset encoding) throws Exception{
			/**
			 * 	Method I used earlier
			 *	try(  PrintWriter out = new PrintWriter(fileName)){
			 *		out.println( toWrite );
			 *	}
			 * 
			*/
			FileUtils.write(file, toWrite, encoding);
		}
		public void append(FileAppender fileAppender, String toWrite) throws Exception{
			fileAppender.printWriter.print(toWrite);
		}
		public Properties getPropertiesObjectFromFile(String fileName) throws Exception{
			Properties prop = new Properties();
			InputStream in = getInputStreamFromFileName(fileName);
			prop.load(in);
			in.close();
			return prop;
		}

		private File getFileInstanceFromClasspath(String fileRelativePath) throws URISyntaxException {
			/*
			 * URL was and still is giving null all the time
			 * URL url = this.getClass().getResource(fileRelativePath);
			 */
			/** --Both following methods work now but Searching 'weka.jar' was giving NULL
			 * 		because lib wasn't in the classPath, but weka.jar itself was
			 * URL url = ZinFile.class.getClassLoader().getResource(fileRelativePath);
			 * URL url = Thread.currentThread().getContextClassLoader().getResource( fileRelativePath );
			 */
			URL url = Thread.currentThread().getContextClassLoader().getResource( fileRelativePath );
			if(url == null)
				return null;
			File file = new File(url.toURI());
			return file;
		}
		
		private File getFileFromAbsolutePath(String absolutePath){
			File f = new File(absolutePath);
			/**
			 * if(f.exists()) returned false for "\\abc_dir\\zinBuild.xml"
			 * if(f.exists()) returned false for "C:\\abc_dir\\..\\xyz\\..\\abc\\zinBuild.xml"
			 * hence I used 
			 * 		f.getAbsoluteFile().exists()
			 * 		f.getCanonicalFile().exists()
			 * it returned false in both cases, probably because file was open.
			 * 	RCA-Unknown as of yet
			 */
			if(f.getAbsoluteFile().exists())
				return f;
			return null;
		}
		private InputStream getInputStreamFromAbsolutePath(String absolutePath){
			try{
				InputStream in = new FileInputStream(absolutePath);
				return in;
			} catch(FileNotFoundException e){
				return null;
			}
		}

		private InputStream getInputStreamFromClasspath(String fileRelativePath) throws Exception{
			return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileRelativePath);
		}
	}

	private class FileAppender{
		FileWriter fileWriter;
		BufferedWriter bufferedWriter;
		PrintWriter printWriter;
		String fileName;
		public FileAppender(String fileName) throws Exception {
			this.fileName = fileName;
			fileWriter = new FileWriter(fileName, true);
			bufferedWriter = new BufferedWriter(fileWriter);
			printWriter = new PrintWriter(bufferedWriter);
			
			fileAppenderMap.put(fileName, this);
		}
		public void closeWriters() throws Exception{
			// Close them in this order only
			// if you close the fileWriter, printWriter will become null
			// AND file will NOT BE saved
			if(printWriter!= null) printWriter.close();
			if(bufferedWriter!=null) bufferedWriter.close();
			if(fileWriter!=null) fileWriter.close();
			
			fileAppenderMap.remove(fileName);
		}
	}
	
	private class ZinPrivate{
		private void hmm() throws Exception{
		}
	}
}
