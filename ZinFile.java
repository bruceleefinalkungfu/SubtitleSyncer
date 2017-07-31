
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZinFile {
	
	private ZinFileService zinFileService;
	// for every fileName, a fileAppender
	// cuz when appending to a file, I don't wanna open and close a file again and again 
	private Map<String, FileAppender> fileAppenderMap;
	
	public ZinFile() {
		zinFileService = new ZinFileService(); 
		fileAppenderMap = new HashMap<>();
	}
	
	
	/**
	 * @param folderPath
	 * @param onlyFileFormatsAllowed : like ".java", ".XML". Leave blank if all files allowed
	 * @return
	 */
	public List<File> getAllFiles(String folderPath, String ... onlyFileFormatsAllowed) throws Exception{
		File folder = new File(folderPath);
		if(!folder.exists() || !folder.isDirectory()){
			throw new Exception("folder either doesn't exist or not a directory");
		}
		for(int i=0 ; i<onlyFileFormatsAllowed.length ; i++){
			onlyFileFormatsAllowed[i] = onlyFileFormatsAllowed[i].toLowerCase();
		}
		return zinFileService.getAllFiles(folder, onlyFileFormatsAllowed);
	}
	
	public String getStringFromFile(String fileName) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getStringFromFile(fileName);
	}
	
	public List<String> getListOfStringLineFromFile(String fileName) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getListOfStringLineFromFile(fileName);
	}
	
	public byte[] getBytesOfFile(String fileName) throws Exception{
		saveAppendedFile(fileName);
		return zinFileService.getBytesOfFile(fileName);
	}
	
	public void write(String fileName, byte[] arr) throws Exception{
		saveAppendedFile(fileName);
		zinFileService.write(fileName, arr);
	}
	public void write(String fileName, String toWrite) throws Exception{
		saveAppendedFile(fileName);
		zinFileService.write(fileName, toWrite);
	}
	public void write(String fileName, String[] toWriteArr) throws Exception{
		saveAppendedFile(fileName);
		zinFileService.write(fileName, toWriteArr);
	}
	/**
	 * Call this method many times
	 * but in the end call saveAppendedFile to save the file
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
	 * Call this method many times
	 * but in the end call saveAppendedFile to save the file
	 * @param fileName
	 * @param toWrite
	 * @throws Exception
	 */
	public void appendln(String fileName, String toWrite) throws Exception{
		append(fileName, toWrite+System.getProperty("line.separator"));
	}
	
	/**
	 * This method MUST be called after you're done appending to a file
	 * So file can be saved
	 * @param fileName
	 * @throws Exception
	 */
	public void saveAppendedFile(String fileName) throws Exception{
		FileAppender fileAppender = fileAppenderMap.get(fileName);
		if(fileAppender != null){
			fileAppender.closeWriters();
		}
	}

	public byte[] encodeFromByteToBCD(byte[] brr){
		return Base64.getEncoder().encode(brr);
	}
	
	public byte[] decodeFromBCDToByte(byte[] arr){
		return Base64.getDecoder().decode(arr);
	}
	
	// all the processing is done in these methods here
	// before that was just preprocessing
	// class is private so its instance can't be created outside of this class
	private class ZinFileService{
		// It doesn't matter if I choose it private or public cuz inheritance isn't involved
		private ZinFileService() {
			// TODO Auto-generated constructor stub
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
			String fileName = f.getName().toLowerCase();
			for(String s : arr){
				if(fileName.endsWith(s))
					return true;
			}
			return false;
		}
		
		public String getStringFromFile(String fileName) throws Exception{
			InputStream is = new FileInputStream(fileName);
			BufferedReader buf = new BufferedReader(new InputStreamReader(is));
			String line = buf.readLine(); StringBuilder sb = new StringBuilder();
			while(line != null){ 
				sb.append(line).append("\n"); 
				line = buf.readLine(); 
			} 
			String fileAsString = sb.toString();
			return fileAsString;
		}

		public List<String> getListOfStringLineFromFile(String fileName) throws Exception{
			List<String> outputList = new LinkedList<>();
			int count = 0;
			InputStream is = new FileInputStream(fileName);
			BufferedReader buf = new BufferedReader(new InputStreamReader(is));
			String line = buf.readLine();
			StringBuilder sb = new StringBuilder();
			while(line != null){
				outputList.add(line);
				line = buf.readLine();
			}
			return outputList;
		}
		
		public byte[] getBytesOfFile(String fileName) throws Exception{
			File f = new File(fileName);
			int byteLength = (int) f.length();
			byte[] arr = new byte[byteLength];
			FileInputStream is = new FileInputStream(fileName);
			is.read(arr, 0, byteLength);
			return arr;
		}
		public void write(String fileName, byte[] arr) throws Exception{
			FileOutputStream fout = new FileOutputStream(fileName);
			fout.write(arr);
			fout.close();
		}
		public void write(String fileName, String toWrite) throws Exception{
			try(  PrintWriter out = new PrintWriter(fileName)){
				out.println( toWrite );
			}
		}
		public void write(String fileName, String[] toWriteArr) throws Exception{
			StringBuilder sb = new StringBuilder();
			for(String s : toWriteArr)
				sb.append(s+"\n");
			write(fileName, new String(sb));
		}	
		public void append(FileAppender fileAppender, String toWrite) throws Exception{
			fileAppender.printWriter.print(toWrite);
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
			if(printWriter!= null) printWriter.close();
			if(bufferedWriter!=null) bufferedWriter.close();
			if(fileWriter!=null) fileWriter.close();
			
			fileAppenderMap.remove(fileName);
		}
	}
}
