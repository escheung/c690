import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class FSM {
	// Name Space
	public static String NS = "cmput690/";
	// Predicates
	public static String IS_A = "is_a";
	public static String HAS_NAME = "has_name";
	public static String HOME_OF = "home_of";
	public static String PLAYS_FOR = "plays_for";
	public static String NAMED_AFTER = "named_after";
	//public static String KNOWN_AS = "known_as";
	// Class
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
	
	public static String betweenBrackets(String text) {
		int i1 = text.indexOf('(');
		int i2 = text.indexOf(')',i1+1);
		if (i1>0 && i2>i1) {
			return text.substring(i1+1,i2);
		}
		return "";
	}
	
	public static String delStuffBtwCommas(String text) {
		String line = text;
		// Remove items between commas;
		line = line.replaceAll(",.*?,","");
		return line;
	}
	
	public static String betweenCommas(String text) {
		int i1 = text.indexOf(',');
		int i2 = text.indexOf(',',i1+1);
		if (i1>0 && i2>i1) {
			return text.substring(i1+1, i2);
		}
		return "";
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
	
	public static Vector<Trip> findHomeOf(String anchor, String[] words, String[] tags) {
		Vector<Trip> trips = new Vector<Trip>();
		
		if (anchor==null || anchor.isEmpty()) return trips;	// if anchor is not there, return empty vector.
		//String subject = "";
		String object = "";
		int state = 0;	// state index.
		int index = 0;	// word index in sentence.
		boolean found = false;	// flag for finding object.
		
		while (index < words.length) {
			String word = words[index];
			String tag = tags[index];
			switch (state) {
				case 0:
					if (word.equals("home")) {
						state = 1;	// found "home"; go to state 1.
					} else {
						state = 0;	// stay in state 0.
					}
					break;
				case 1:
					if (word.equals("ground") || word.equals("stadium")) {
						state = 2; 	// found "ground"; go to state 2.
					} else if (word.equals("of")) {
						state = 3;	// found "of"; go to state 3.
					} else {
						state = 7;	// unexpected word, go to end state.
					}
					break;
				case 2:
					if (word.equals("of") || words.equals("to")) {
						state = 3;	// found "of"; go to state 3.
					} else {
						state = 7;	// unexpected; go to end state.
					}
					break;
				case 3:
					if (word.equals("both")) {
						state = 4;	// found "both"; go to state 4;
					} else if (tag.matches("^NN.*")) {
						object = object.concat(word);	// found noun; add to object string
						state = 5;						// go to state 5.	
					} else {
						state = 7;	// unexpected; go to end state.
					}
					break;
				case 4:
					if (tag.matches("^NN.*")) {
						object = object.concat(word);	// found noun; add to object string.
						state = 5;
					} else {
						state = 7;	// unexpected; go to end state.
					}
					break;
				case 5:
					if ( tag.matches("^NN.*") || (word.matches("^(do|da|de)$")) ) {
						object = object.concat(" "+word);	// add word to object string.
						state = 5;	// stay in state 5.
					} else if (word.equals("and")) {
						found = true;	// set flag to store object.
						state = 6;		// go to state 6;
					} else if (word.equals("of")) {
						found = true;	// set flag to store object.
						state = 8;		// go to special state.
					} else {
						state = 7;	// unexpected; go to end state.
					}
					break;
				case 6:
					if (tag.matches("^NN.*")) {
						object = object.concat(word);	// add word to object string.
						state = 5;	// go to state 5
					} else {
						state = 7;	// unexpected; go to end state.
					}
					break;
				case 7:	// end state.
					break;
				case 8: // special state "[team] of series A/B".
					if (word.equals("and")) {
						state = 6;	// go to state 6;
					} else {
						state = 8;	// state here until "and" or end of sentenance.
					}
					break;
				default:
					break;
			}
			
			index++;

			if (found && !object.isEmpty()) {
				trips.add(new Trip(anchor,FSM.HOME_OF,object));
				object = "";	// reset object
				found = false;	// reset flag
			};
			if (state == 7) break;	// stop loop if reached end state.
		}
		
		if (!object.isEmpty()) {
			trips.add(new Trip(anchor,FSM.HOME_OF,object));
		}
		
		return trips;
	}
	
	public static Vector<Trip> findKnownAs(String anchor, String[] words, String[] tags) {
		Vector<Trip> trips = new Vector<Trip>();
		
		//String subject = "";
		String object = "";
		//String keyJJ = "";
		int state = 0;	// state index.
		int index = 0;	// word index in sentence.
		boolean found = false;
		
		while (index < words.length) {
			String word = words[index];
			String tag = tags[index];
			
			switch (state) {
			case 0:	// init/adverb state;
				if (tag.matches("^RB.*")) {
					state = 0;	// found adverb ; stay here;
				} else if (word.equals("known")) {
					state = 1;	// found "known"; go to state 1
				} else {
					state = 7;	// unexpected word; go to end state;
				}
				break;
			case 1:	// found "known";
				if (word.equals("as")) {
					state = 2;	// found "as"; go to state 2;
				} else if (word.matches("^(simply|just)$")) {
					state = 10;
				} else {
					state = 7;	// unexpected word; go to end state;
				}
				break;
			case 2: // found "as"
				if (tag.matches("^NN.*")) {
					object = object.concat(word);	// add word to object string.
					state = 5;	// go to state 5;
				} else if (word.matches("^(a|an|the|just)$")) {	// found "a,an,the,just";
					state = 3;	// go to state 3;
				} else {
					state = 7;	// unexpected word; go to end state;
				}
				break;
			case 3:	// found (a/an/the/just)
				if (tag.matches("^NN.*")) {	// found Noun;
					object = object.concat(word);	// add word to object string.
					state = 5;	// go to state 5;
				} else if (tag.matches("^JJ.*")) {	// found adjective.
					state = 4;
				} else {
					state = 7;	// unexpected; go to end state.
				};
				break;
			case 4:	// found adjective.
				if (tag.matches("^NN.*")) {	// found Noun;
					object = object.concat(word);	// add word to object string.
					state = 5;	// go to state 5.
				} else {
					state = 7;	// unexpected word;
				}
				break;
			case 5:	// found noun.
				if (tag.matches("^NN.*") ||(word.matches("^(do|da|de)$")) ) {	// found Noun;
					object = object.concat(" "+word);	// add word to object string.
					state = 5;	// stay in state 5;
				} else if (word.matches("^(or|and)$")) {	// found 'or'; flag to store triple.
					found = true;
					state = 6;
				} else {
					state = 7;	// unexpected, go to end state.
				}
				break;
			case 6:	// found 'and/or'.
				if (tag.matches("^NN.*")) {	// found beginning of another noun;
					object = object.concat(word);	// add word to object string.
					state = 5;	// go to state 5;
				} else if (tags[index].matches("^RB.*")) {	// found adverb
					state = 6;	// stay in state 6; 
				} else {	// unexpected, go to end state.
					state = 7;
				}
				break;
			case 7:
				break;	// end of FSM.
			case 8:	// found "referred"
				if (word.equals("to")) {	// found "to"
					state = 9;	// go to state 9.
				} else {
					state = 7;	// unexpected, go to end state.
				}
				break;
			case 9:	// found "to"
				if (word.matches("^(simply|just)$")) {
					state = 10;	// go to state 10.
				} else if (word.equals("as")) {
					state = 2;	// go to state 2.
				} else {
					state = 7;	// unexpected, go to end state.
				}
				break;
			case 10:	// found simply/just
				if (word.equals("as")) {
					state = 2;
				} else {
					state = 7;	// unexpected, go to end state.
				}
				break;
			default:
				break;
			}
			
			index ++;
			// if found = true; store triple.
			if (found && !object.isEmpty()) {
				// add triple to vector
				trips.add(new Trip(anchor,FSM.HAS_NAME,object));
				object = "";	// reset object.
				found = false;	// reset flag.
			}
		}
		
		if (!object.isEmpty()) {
			trips.add(new Trip(anchor,FSM.HAS_NAME,object));
		}
		
		return trips;
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
					if (tags[index].matches("^NN.*") || (words[index].matches("^(do|da|de)$"))) {
							
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
			} else if (object.contains("football club")||object.contains("sports club")) {	// is a football club
				trips.add(new Trip(anchor,FSM.IS_A,FSM.FOOTBALL_CLUB));
			}
		}
		
		return trips;
	}
	public static Vector<Trip> findNamedAfter(String anchor, String[] words, String[] tags) {
		Vector<Trip> trips = new Vector<Trip>();
		
		//String subject = "";
		String object = "";
		int state = 0;	// state index.
		int index = 0;	// word index in sentence.
		while (index < words.length) {
			String word = words[index];
			String tag = tags[index];
			
			switch (state) {
				case 0:
					if (word.matches("^named|renamed$")) {
						state = 1;	// found the word "plays"; go to state 1.
					} else {
						state = 0;	// otherwise; stays here.
					}
					break;
				case 1:
					if (word.equals("after")) {
						state = 2;	// found "after", go to state 2.
					} else {
						state = 6;	// unexpected; go to end.
					}
					break;
				case 2:
					if (word.equals("former")) {
						state = 3;	// found "former", go to state 3.
					} else {
						state = 2;	// otherwise; stay here.
					}
					break;
				case 3:
					if (word.matches("^presidents?$")) {
						state = 4;	// found president, go to state 4.
					} else {
						state = 3; 	// otherwise; stay here.
					}
					break;
				case 4:
					if (tag.matches("^NN.*$")) {
						object = object.concat(word);	// add noun to object.
						state = 5;						// go to 5.
					} else {
						state = 4;	// otherwise; stay here.
					}
					break;
				case 5:
					if (tag.matches("^NN.*$")) {
						object = object.concat(" " + word);	// add noun to object.
						state = 5;							// stay in 5.
					} else {
						state = 6;	// unexpected go to 6.
					}
				case 6:
					break;
				default:
					break;
					
			}
			
			if (state==6) break;
			index++;
		}
		
		if (!object.isEmpty() && !anchor.isEmpty()) {
			trips.add(new Trip(anchor, FSM.NAMED_AFTER, object));
		}
		return trips;
	}
	public static Vector<Trip> findPlaysFor(String anchor, String[] words, String[] tags) {
		Vector<Trip> trips = new Vector<Trip>();
		
		//String subject = "";
		String object = "";
		int state = 0;	// state index.
		int index = 0;	// word index in sentence.
		
		while (index < words.length) {
			String word = words[index];
			String tag = tags[index];
			
			switch (state) {
			case 0:
				if (word.matches("^plays|playing|played$")) {
					state = 1;	// found the word "plays"; go to state 1.
				} else {
					state = 0;	// otherwise; stays here.
				}
				break;
			case 1:
				if (word.equalsIgnoreCase("for")) {
					state = 2;	// found the word "for"; go to state 2;
				} else {
					state = 4;
				}
				break;
			case 2:
				if (tag.matches("^NN.*|JJ.*$")) {	// found a noun or adjective
					object = object.concat(word);	// add noun to object.
					state = 3;	// go to state 3;
				} else {
					state = 4;	// unexpected word. go to state 4;
				}
				break;
			case 3:
				if (tag.matches("^NN.*")) {
					object = object.concat(" " + word);	// add noun to object.
					state = 3;	// found a noun, add to object;
				} else {
					state = 4;	// no more noun. go to state 4;
				}
				break;
			default:
				break;
			}
			
			index++;
		}
		if (!object.isEmpty() && !anchor.isEmpty()) {
			trips.add(new Trip(anchor, FSM.PLAYS_FOR, object));
		}
		// TODO:  finish it.
		return trips;
	}
}
