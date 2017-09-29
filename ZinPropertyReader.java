package zin.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ZinPropertyReader {

	public enum PropertyFile{
		SERIALIZED("zin.serialize"),
		PROPERTY("zin.property"),
		;
		private String filePath;
		
		private PropertyFile(String file) { this.filePath = file; }
	}
	
	private static final String PROPERTY_FILE_NAME = "zin.properties";
	
	private static final ZinFile zinFile = new ZinFile();
	
	private static final Map<PropertyFile, Properties> cachedMap = new HashMap<>();
	

	public static Properties getPropertiesObjectFromFile(PropertyFile propertyFile) throws Exception{
		Properties properties = cachedMap.get(propertyFile);
		// If properties are not cached
		if(properties == null){
			properties = zinFile.getPropertiesObjectFromFile(propertyFile.filePath);
			cachedMap.put(propertyFile, properties);
		}
		return properties;
	}
	
	public String getPropertiesFileValueFromKey(PropertyFile propertyFile, String key) throws Exception{
		return getPropertiesObjectFromFile(propertyFile).getProperty(key);
	}
	
	public String getPropertiesFileValueFromKey(String key) throws Exception{
		for(PropertyFile propertyFile : PropertyFile.values()){
			String val = getPropertiesFileValueFromKey(propertyFile, key);
			if(val != null){
				return val;
			}
		}
		return null;
	}
	
	private static boolean shouldReadFromCache() throws Exception{
		return zinFile.getPropertiesFileValueFromKey(PropertyFile.PROPERTY.filePath, "readPropertyFromCache").equalsIgnoreCase("true");
	}
	
	
}
