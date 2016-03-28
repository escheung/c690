import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;

import com.wcohen.ss.Jaccard;
import com.wcohen.ss.JaroWinkler;



public class Solver {

	
	private static final String SOURCE = "cmput690_a3.ttl";
	//private static final String SOURCE = "sample.ttl";
	
	private static final String OUTPUT_0 = "rdf_output.tsv";
	private static final String OUTPUT_1 = "cmput690w16a3_q1_cheung.tsv";
	private static final String OUTPUT_2 = "cmput690w16a3_q2_cheung.tsv";
	private static final String OUTPUT_3 = "cmput690w16a3_q3_cheung.tsv";
	private static final String OUTPUT_4 = "cmput690w16a3_q4_cheung.tsv";
	private static final String OUTPUT_5 = "cmput690w16a3_q5_cheung.tsv";
	
	
	private static final String HasDocLocalKey = "cmput690hasDocument";
	private static final String FreebaseKey = "http://rdf.freebase.com/key/";
	private static final String FreebaseLocalKey = "wikipedia.en";
	private static final String FreebaseNameSpace = "http://rdf.freebase.com/ns/";
	private static final String FreebaseRosterTeam = "sports.sports_team_roster.team";
	private static final String FreebaseRosterPlayer = "sports.sports_team_roster.player";
	
	public static void main(String[] args) throws Exception {

	
		/*
		 * Plan 
		 * - Get documents from triple;
		 * - pass each document to with anchor subject.
		 * - Parse each document using POS and Hearst Pattern
		 * - Store new triples to Triple Store.
		 * - Solve disambiguation where possible.
		 * - Use new combined Triple Store to solve problems.
		 * 
		 */
		
		
		/* 
		 * Questions:
		 * - Which stadiums are used by which clubs?
		 * - Who plays for which teams?
		 * - Who coaches a team with Spanish players?
		 * - Which clubs have stadiums named after former presidents?
		 * - Which teams have the most nationalities amongst their roaster?
		 * 
		 */
		
		// Load data in source file into Jena model.
		Model model = RDFDataMgr.loadModel(SOURCE);
		// Create Engine for parsing and NER classifier.
		Engine engine = new Engine();
		
				
		// get a list of 'documents' text from the model.
		Map<Resource,String> docMap = getDocuments(model);
		
		// Process and disambiguate anchor terms.
		Solver.processAnchor(engine, model, docMap);
		
		// process the 'documents' for relationships and add to model.
		Solver.processRelations(engine, model, docMap);
		
		
//		printVectorString(documents);
		
//		testModel(model);
		
//		printStatements(model);	// Print out the statements in model.
		
		
		// Print answers to file
		PrintWriter writer0 = new PrintWriter(OUTPUT_0,"UTF-8");
		writer0.println(printStatements(model));	// write statements to file for testing.
		writer0.close();
		
		PrintWriter writer1 = new PrintWriter(OUTPUT_1,"UTF-8");
		writer1.print(solver1(model));
		writer1.close();
		
		PrintWriter writer2 = new PrintWriter(OUTPUT_2,"UTF-8");
		writer2.print(solver2(model));
		writer2.close();
		
		//TODO: solve the rest.
		
	}
	private static void processAnchor(Engine engine, Model model, Map<Resource,String> docs) {
		// process document to link names to keys
		
		for (Resource key: docs.keySet()) {	// for each resource key.
			
			// Get document from map using key.
			String doc = docs.get(key);
			// *** Process Stuff in Brackets ***
			//String stuffInBrackets = FSM.betweenBrackets(doc);	// extract stuff in first set of brackets.
			
			doc = FSM.delBrackets(doc);							// delete all brackets & content.
			
			// split each document into sentences.
			String[] sentences = engine.splitToSentences(doc);
			
			
			
			Vector<String> vw = new Vector<String>();	// Vector of words/tokens.
			Vector<String> vt = new Vector<String>();		// Vector of POS tags.
			Vector<Trip> trips = new Vector<Trip>();		// Vector of triples returned from FMS.

			String sent = sentences[0];				// Target only the first line for names	
			
			String stuffInCommas = FSM.betweenCommas(sent);		// extract stuff in first set of commas.

			// *** Process whole sentence ***
			// Ask engine to Tokenize and apply POS Tagging
			sent = FSM.delStuffBtwCommas(sent);					// delete stuff between commas.
			engine.posTagging(sent, vw, vt);					// POS tagging
			String[] word = vw.toArray(new String[vw.size()]);	// convert to array
			String[] tag = vt.toArray(new String[vt.size()]);	// convert to array
			
			// Perform Is_A FSM.
			trips = FSM.findIsA(key.getURI(), word, tag);
			// Process stuff between commas to look for other names.
			trips.addAll(Solver.processOtherNames(key.getURI(),stuffInCommas,engine,model));

			// Process the triples generated and add to model. 
			Solver.processTripleAsLiteral(trips, model);
			
		}
		
	}
	
	private static void processTripleAsLiteral(Vector<Trip> triples, Model model) {
		// Add text triples to model as literal statement.
		// Useful for new literal objects.
		Iterator<Trip> it = triples.iterator();	// get vector iterator.
		while (it.hasNext()) {	// for each triple
			Trip triple = it.next();
			// Add a triple with Object as literal.
			Resource subject = model.createResource(triple.getSubject());
			Property predicate = model.createProperty(FSM.NS,triple.getPredicate());
			model.add(model.createLiteralStatement(subject, predicate, triple.getObject()));
		}
		
	}
	
	private static void processTriple(Vector<Trip> triples, Model model) {
		// Add text triples to model by disambiguating object where possible.
		// If unable to find match, report and skip.
		
		Iterator<Trip> it = triples.iterator();	// get vector iterator.
		Property wikiKey = model.createProperty(Solver.FreebaseKey, Solver.FreebaseLocalKey);	// create wiki key property
		Property hasNameKey = model.createProperty(FSM.NS,FSM.HAS_NAME);	// create "has_name" property

		while (it.hasNext()) {	// for each triple
			Trip triple = it.next();
			
			Resource subject = model.createResource(triple.getSubject());			// create subject based on subject URI. 
			Property predicate = model.createProperty(FSM.NS,triple.getPredicate());	// create property based on predicate.  
			
			Resource object = Solver.findResource(wikiKey, triple.getObject(), model);	// find the object resource based on literal string.
			if (object==null) {	// No match found using wiki key, try using "has_name" key
				object = Solver.findResource(hasNameKey, triple.getObject(), model);
			};
			
			if (object != null) {	// an object key is found.
				// add relation to model using keys.
				model.add(model.createStatement(subject, predicate, object));		// add new statement to model.
			} else {	// can't find object key
				// add relation to model as literal.
				model.add(model.createLiteralStatement(subject, predicate, triple.getObject()));
			}

		}
		
	}
	
	private static void processRelations(Engine engine, Model model, Map<Resource,String> docs) {
		// Parse documents stored in model and extract additional triples using TSM. 
		
		// For each map keys with document.
		for (Resource key: docs.keySet()) {
			
			String doc = docs.get(key);					// Get document from map using key.
			doc = FSM.delBrackets(doc);					// delete all brackets & content.
			
			String[] sentences = engine.splitToSentences(doc);	// split each document into sentences.
			
			//System.out.format("Relating URI: %s ***\n",key.getURI());
			
			for (int i=0; i < sentences.length; i++) {
			//for (String sentence: sentences) {
				Vector<String> vw = new Vector<String>();		// Vector of words/tokens.
				Vector<String> vt = new Vector<String>();		// Vector of POS tags.
				Vector<Trip> trips = new Vector<Trip>();		// Vector of triples returned from FMS.
				
				String sent = sentences[i];
				sent = FSM.delStuffBtwCommas(sent);				// delete stuff between commas.

				// Ask engine to Tokenize and apply POS Tagging
				engine.posTagging(sent, vw, vt);					// POS tagging
				String[] word = vw.toArray(new String[vw.size()]);	// convert to array
				String[] tag = vt.toArray(new String[vt.size()]);	// convert to array

				// *** HOME_OF relations ***
				trips.addAll(FSM.findHomeOf(key.getURI(), word, tag));	// find "home of" relationships.
				
				// *** Process and Map triples objects to model.
				Solver.processTriple(trips, model);
				
			}
		}
		
	}
	
	private static Vector<Trip> processOtherNames(String anchor, String text, Engine engine, Model m) {
		Vector<String> words = new Vector<String>();
		Vector<String> tags = new Vector<String>();
		
		engine.posTagging(text, words, tags);
		String[] w = words.toArray(new String[words.size()]);	// convert to array
		String[] t = tags.toArray(new String[tags.size()]);	// convert to array
		
		return FSM.findKnownAs(anchor, w, t);
	}
	

	private static Map<Resource,String> getDocuments(Model m) {
		// Look for "hasDocument" predicate in the model, 
		// create a map using with the resources as map keys and the document strings as map content.
		
		//String hasDocLocalKey = "cmput690hasDocument";
		Map<Resource, String> map = new HashMap<Resource, String>();
		Selector selector = new SimpleSelector(null,null,(RDFNode)null);
		StmtIterator it = m.listStatements(selector);
		
		while(it.hasNext()) {
			
			Statement stmt = it.nextStatement();
			Resource s = stmt.getSubject();
			Property p = stmt.getPredicate();
			RDFNode o = stmt.getObject();
			
			// Look for the "cmput690hasDocument" property.
			if (p.getLocalName().equalsIgnoreCase(Solver.HasDocLocalKey) && o.isLiteral()) {
				map.put(s, o.asLiteral().getString());
			};
		}
		return map;
	}
	
	private static Resource findResource(Property property, String literalText, Model model) {
		// Find assigned entity class of the given uri (first occurance); 
		// Return empty string if nothing found.
		
		Selector selector = new SimpleSelector(null, property, (RDFNode)null);	// create selector.
		
		StmtIterator it = model.listStatements(selector);	// query model with selector.
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			Resource s = stmt.getSubject();
			RDFNode o = stmt.getObject();
			if (o.isLiteral()) {
				String l = o.asLiteral().getString();
				if (ssCompare(l,literalText)) 
					return s;
			}
		}
		return null;	// nothing found, return null;
	}
	
	private static String findLiteral(Property property, Resource subject, Model model) {
		// Find and return literal string from model, with given property and subject.
		
		Selector selector = new SimpleSelector(subject,property,(RDFNode)null);	// create selector.
		String literal = "";
		StmtIterator it = model.listStatements(selector);	// query model
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			RDFNode object = stmt.getObject();
			if (object.isLiteral()) {
				literal = object.asLiteral().getString();
				if (!literal.contains("$")) {	// does not contain '$'
					return literal;
				};
			}
		}
		return literal;
	}

	private static String solver1(Model model) {
		// Which stadiums are used by which clubs?
		StringBuilder sb = new StringBuilder();
		
		Property home_of = model.createProperty(FSM.NS, FSM.HOME_OF);	// home_of peroperty
		
		Selector selector = new SimpleSelector(null,home_of,(RDFNode)null);
		StmtIterator it = model.listStatements(selector);
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			Resource stadium = stmt.getSubject();
			RDFNode club = stmt.getObject();
			
			String stadiumName = findName(stadium,model);
			String stadiumKey = stadium.getURI();
			String clubKey = "";
			String clubName = "";
			if (club.isResource()) {
				clubName = findName(club.asResource(),model);
				clubKey = club.asResource().getURI();
			} else if (club.isLiteral()) {
				clubName = club.asLiteral().getString();
			}
			sb.append(String.format("%s\t%s\t%s\t%s\n",
					stadiumKey,		
					stadiumName,
					clubKey,
					clubName));
		}
		return sb.toString();
	}
	
	private static String solver2(Model model) {
		// Who plays for which teams?
		// TODO
		StringBuilder sb = new StringBuilder();
		
		Property rosterTeamProp = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseRosterTeam);
		Property rosterPlayerProp = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseRosterPlayer);
		Selector selectRosterTeam = new SimpleSelector(null,rosterTeamProp,(RDFNode)null);
		StmtIterator it = model.listStatements(selectRosterTeam);
		while (it.hasNext()) {	// each team with roster.
			
			Statement stmt = it.nextStatement();
			Resource rosterNode = stmt.getSubject();
			RDFNode club = stmt.getObject();
			String clubKey = "";
			String clubLiteral = "";
			String playerKey = "";
			String playerLiteral = "";
			
			if (club.isResource()) {
				clubKey = club.asResource().getURI();
				clubLiteral = Solver.findName(club.asResource(), model);
			}
			// get iterator of players
			NodeIterator nit = model.listObjectsOfProperty(rosterNode, rosterPlayerProp);
			while (nit.hasNext()) {	// each player in this team.
				RDFNode player = nit.nextNode();
				if (player.isResource()) {
					playerKey = player.asResource().getURI();
					playerLiteral = Solver.findName(player.asResource(), model);
				}
				
				sb.append(String.format("%s\t%s\t%s\t%s\n", clubKey, clubLiteral, playerKey, playerLiteral));
			}
			
			
		}
		
		return sb.toString();
	}
	
	private static String findName(Resource subject, Model model) {
		String literal = "";
		
		Property wikiEn = model.createProperty(Solver.FreebaseKey, Solver.FreebaseLocalKey);
		Property hasName = model.createProperty(FSM.NS, FSM.HAS_NAME);
		literal = Solver.findLiteral(wikiEn, subject, model);
		if (literal.isEmpty()) {
			literal = Solver.findLiteral(hasName, subject, model);
		}
		return literal;
	}
	
	
	private static void printVectorString(Vector<String> v) {
		Iterator<String> it = v.iterator();
		
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
	
	private static String printStatements(Model m) {
		
		StringBuilder sb = new StringBuilder();
		
		// list the statements in the Model
		StmtIterator iter = m.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
		    Statement stmt      = iter.nextStatement();  // get next statement
		    Resource  subject   = stmt.getSubject();     // get the subject
		    Property  predicate = stmt.getPredicate();   // get the predicate
		    RDFNode   object    = stmt.getObject();      // get the object

		    sb.append(subject.toString());
		    //System.out.print(" " + predicate.toString() + " ");
		    sb.append(" " + predicate.getNameSpace()+" "+predicate.getLocalName() + " ");
		    if (object instanceof Resource) {
		       sb.append(object.toString());
		    } else {
		        // object is a literal
		        sb.append(" \"" + object.toString() + "\"");
		    }
		    sb.append(" .\n");
		}
		return sb.toString();
	}
	

	
	private static void testModel(Model m) {
		//Literal footballer = m.createLiteral("footballer","en");
		//Literal carlo = m.createLiteral("Carlo","en");
		Property isA = m.createProperty("c690","isA");
		Resource footballer = m.createResource("somewhere.com/footballer");
		Resource carlo = m.createResource("somewhere.com/Carlo");
		Statement s = m.createStatement(carlo, isA, footballer);
		m.add(s);
		
	}
	
	private static boolean ssCompare (String s1, String s2) {
		JaroWinkler jw = new JaroWinkler();
		if (jw.score(s1, s2) > 0.9) {
			return true;
		}
		return false;
	}
}
