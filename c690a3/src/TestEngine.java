import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class TestEngine {

	public static void main(String[] args) throws Exception{
		String text = "Wayne Gretzky is a Canadian hockey player.  He is also called The Great One.";
		
		Engine engine = new Engine();
		
		String[] sentences = engine.splitToSentences(text);
		
		for (String sent: sentences) {
			System.out.format("%s\n", sent);
			
			Vector<String> w = new Vector<String>();
			Vector<String> t = new Vector<String>();
			
			//Map<String, Object> map = engine.posTagging(sent);
			engine.posTagging(sent,w,t);
			
			//String[] w = (String[])map.get("words");
			//String[] t = (String[])map.get("tags");
			
			System.out.println(Arrays.toString(w.toArray()));
			System.out.println(Arrays.toString(t.toArray()));
			
			
			Map<String, Object> ner = engine.nerClassify(sent);
			String[] type = (String[])ner.get("type");
			String[] entity = (String[])ner.get("entity");
			
			System.out.println(Arrays.toString(type));
			System.out.println(Arrays.toString(entity));
			
			
		}
		
		
	}
	
}
