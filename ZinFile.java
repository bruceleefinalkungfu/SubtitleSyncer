
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;

public class ZinFile {

	public String getStringFromFile(String fileName){
		try{
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
		catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}

	public String[] getArrayOfStringLineFromFile(String fileName) throws Exception{
		String[] output = new String[1000];
		int count = 0;
		InputStream is = new FileInputStream(fileName);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while(line != null){
			output[count++] = line;
			line = buf.readLine();
		}
		return output;
	}
	
	public byte[] getBytesOfFile(String fileName) throws Exception{
		File f = new File(fileName);
		int byteLength = (int) f.length();
		byte[] arr = new byte[byteLength];
		FileInputStream is = new FileInputStream(fileName);
		is.read(arr, 0, byteLength);
		return arr;
	}
	
	public byte[] encodeFromByteToBCD(byte[] brr){
		return Base64.getEncoder().encode(brr);
	}
	
	public byte[] decodeFromBCDToByte(byte[] arr){
		return Base64.getDecoder().decode(arr);
	}
	public void write(String fileName, byte[] arr) throws IOException{
		FileOutputStream fout = new FileOutputStream(fileName);
		fout.write(arr);
		fout.close();
	}
	public void write(String fileName, String toWrite) throws IOException{
		try(  PrintWriter out = new PrintWriter(fileName)){
			out.println( toWrite );
		}
	}
	public void write(String fileName, String[] toWriteArr) throws IOException{
		StringBuilder sb = new StringBuilder();
		for(String s : toWriteArr)
			sb.append(s+"\n");
		write(fileName, new String(sb));
	}
}
