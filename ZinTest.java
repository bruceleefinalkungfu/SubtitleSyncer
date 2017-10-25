
public class ZinTest {
	private ZinFile zinFile = new ZinFile();
	FileModifier fileModifier = new FileModifier();
	public static void main(String [] args) {
		try{
			ZinTest zinTest = new ZinTest();
			zinTest.init();
			System.out.println("done");
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void init() throws Exception{
		fileModifier.addStringToALine("zin.txt", 2, "import com.ZinUtil;");
	}
}
