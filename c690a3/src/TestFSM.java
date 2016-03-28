import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class TestFSM {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		//String text = "Wayne Gretzky is a American footballer.  He is also called The Great One.";
		String text = "Teresa Rivero, or rivero, is a multi-use stadium in Madrid.  The stadium (or field) is the home ground of Deportivo de La Coruña Football Club";
		Engine engine = new Engine();
		
		
		String stuffInBrackets = FSM.betweenBrackets(text);
		System.out.println("Stuff In Brackets:"+stuffInBrackets);
		text = FSM.delBrackets(text);
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
