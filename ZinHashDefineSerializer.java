package zin.hashdefine;

import zin.file.ZinFile;
import zin.io.ZinIO;

public class ZinHashDefineSerializer {
	private static final String HASH_DEFINE_DIR = ZinIO.PROJECT_DIR + "zinHashDefineDir";
	
	private ZinFile zinFile;
	
	public ZinHashDefineSerializer() {
		zinFile = new ZinFile();
	}
	
	public String getDefinition(String definitionName, String...params){
		String hashDefineValue = zinFile.getStringFromFile(definitionName);
		for(String param : params){
			
		}
		return "";
	}
}
package zin.hashdefine;

import java.io.Serializable;

public class ZinHashDefineBO implements Serializable{
	public static final String variableName = "zinVarYuuio";
	private enum Type{
		VARIABLE, FUNCTION;
	}
	private Type type;
	private String definitionName;
	private Value value;
	private static class Value{
		
	}
}
