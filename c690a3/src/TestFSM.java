import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class TestFSM {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String text = "Wayne Gretzky is a American footballer.  He is also called The Great One.";
		
		Engine engine = new Engine();
		
		String[] sentences = engine.splitToSentences(text);
		
		for (String sent: sentences) {
			System.out.format("%s\n", sent);
			
			Map<String, Object> map = engine.posTagging(sent);
			
			String[] w = (String[])map.get("words");
			String[] t = (String[])map.get("tags");

			Vector<Trip> triples = FSM.findIsA("123",w,t);
			Iterator<Trip> it = triples.iterator();

			System.out.println(Arrays.toString(w));
			System.out.println(Arrays.toString(t));
			
			while (it.hasNext()) {
				Trip trip = it.next();
				System.out.format("%s\t%s\t%s\n", 
						trip.getSubject(), trip.getPredicate(), trip.getObject());
			}
			
		}
		
		
	}

}
