import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class FSM {

	public static String NS = "cmput690/";
	public static String IS_A = "is_a";
	public static String HAS_NAME = "has_name";
	public static String PERSON = "person";
	public static String LOCATION = "location";
	public static String ORGANIZATION = "organization";
	public static String NATIONALITY = "nationality";
	public static String STADIUM = "stadium";
	public static String FOOTBALLER = "footballer";
	public static String FOOTBALL_CLUB = "football_club";
	
	public static String delBrackets(String text) {
		String line = text;
		line = line.replaceAll("\\(.*?\\)","");
		return line;
	}
	
	public static String getNounOnly(String[] words, String[] tags) {
		// Returns the first consecutive nouns, stops when first non-noun detected.
		StringBuilder noun = new StringBuilder();
		int index=0;
		while (index < words.length) {
			if (tags[index].matches("^NN.*")) {
				if (index>0) noun.append(' ');
				noun.append(words[index]);
			} else {
				break;
			}
			index++;
		}
		return noun.toString();
	}
	
	public static Vector<Trip> findIsA(String anchor, String[] words, String[] tags) {
		Vector<Trip> trips = new Vector<Trip>();
		
		String subject = "";
		String object = "";
		String keyJJ = "";
		int state = 0;	// state index.
		int index = 0;	// word index in sentence.
		
		while (index < words.length) {
			switch (state) {
				case 0:
					if (tags[index].matches("^NN.*")) {
						subject = subject.concat(words[index]);
						state = 1;	// found subject/anchor term.
					};
					break;
				case 1:
					if (tags[index].matches("^NN.*") 
							|| words[index].equalsIgnoreCase("do") 
							|| words[index].equalsIgnoreCase("da")) {
						// found another NN* word or a "do" , stay in state 1.
						subject = subject.concat(" " + words[index]);
						state = 1;	// found another NN* word, stay in state 1.
					} else if (words[index].matches("^(is|are)$")) {
						state = 2;	// found is/are; move to state 2.
					} else {
						state = 7;	// does not follow IS-A pattern; go to end state.
					}
					break;
				case 2:
					if (tags[index].matches("^RB.*")) {	// adverb
						state = 3;	// found an adverb; go to state 3;
					} else if (words[index].matches("^(a|an|the)$")) {
						state = 4;	// found a/an/the; move to state 4.
					} else {
						state = 7;	// does not follow IS-A pattern; go to end state.
					}
					break;
				case 3:
					if (words[index].matches("^(a|an|the)$")) {
						state = 4;	// found a/an/the; move to state 4.
					} else {
						state = 7;	// go to end state.
					}
					break;
				case 4:
					if (tags[index].matches("^JJ.*")) {
						keyJJ = words[index];	// key adjective, possibly nationality of player.
						state = 5;	// found adjective; move to state 5.
					} else if (tags[index].matches("^VB.*")) {
						state = 5; // found verb; go to state 5;
					} else if (tags[index].matches("^NN.*")) {
						object = object.concat(words[index]);
						state = 6;
					} else {
						state = 7;	// go to end state.
					}
					break;
				case 5:
					if (tags[index].matches("^NN.*")) {
						object = object.concat(words[index]);
						state = 6;
					} else if (tags[index].matches("^JJ.*")) {
						// found more adjective; stay in state 5.
						state = 5;
					} else if (tags[index].matches("^VB.*")) {
						// found more verb; stay in state 5.
						state =5;
					} else {
						state = 7;	// go to end state.
					}
					break;
				case 6:
					if (tags[index].matches("^NN.*")) {
						object = object.concat(" "+words[index]);
						state = 6;
					} else {
						state = 7;	// go to end state.
					}
					break;
				case 7:
					// End of FSM
					break;
				default: 
					break;
			}
			
			if (state == 7) break;	// stop loop if reached end state.
			index++;	// increment index;
		}
		
		if (!subject.isEmpty() && !object.isEmpty()) {	// found subject and object.
			trips.add(new Trip(anchor,FSM.IS_A,object));
			trips.add(new Trip(anchor,FSM.HAS_NAME,subject));
			if (object.contains("footballer")) {	// a footballer is a person.
				trips.add(new Trip(anchor,FSM.IS_A,FSM.PERSON));
				trips.add(new Trip(anchor,FSM.IS_A,FSM.FOOTBALLER));
				// apply nationality if available.
				if (!keyJJ.isEmpty()) {
					trips.add(new Trip(anchor,FSM.NATIONALITY,keyJJ));
				}
			} else if (object.endsWith("stadium")) {	// is a stadium
				trips.add(new Trip(anchor,FSM.IS_A,FSM.STADIUM));
			} else if (object.endsWith("football club")) {	// is a football club
				trips.add(new Trip(anchor,FSM.IS_A,FSM.FOOTBALL_CLUB));
			}
		}
		
		return trips;
	}
	
	
}
