import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.Vector;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;

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
	private static final String FreebaseRosterFrom = "sports.sports_team_roster.from";
	private static final String FreebaseTenureFrom = "soccer.football_team_management_tenure.from";
	private static final String FreebaseTenureTo = "soccer.football_team_management_tenure.to";
	private static final String FreebaseTenureTeam = "soccer.football_team_management_tenure.team";
	private static final String FreebaseTenureManager = "soccer.football_team_management_tenure.manager";
	

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
		
		PrintWriter writer3 = new PrintWriter(OUTPUT_3,"UTF-8");
		writer3.print(solver3(model));
		writer3.close();
		
		PrintWriter writer4 = new PrintWriter(OUTPUT_4,"UTF-8");
		writer4.print(solver4(model));
		writer4.close();
		
		PrintWriter writer5 = new PrintWriter(OUTPUT_5,"UTF-8");
		writer5.print(solver5(model));
		writer5.close();
		
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
				//trips.addAll(FSM.findHomeOf(key.getURI(), word, tag));	// find "home of" relationships.
				Solver.processTriple(FSM.findHomeOf(key.getURI(), word, tag), model);
				// *** PLAYS_FOR relations ***
				//trips.addAll(FSM.findPlaysFor(key.getURI(), word, tag));	// find "plays for" relationships.
				Solver.processTriple(FSM.findPlaysFor(key.getURI(), word, tag), model);
				
				// *** NAMED_AFTER relations ***
				Solver.processTripleAsLiteral(FSM.findNamedAfter(key.getURI(), word, tag),model);

				// *** Process and Map triples objects to model.
				//Solver.processTriple(trips, model);
			}
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
		
		Property home_of = model.createProperty(FSM.NS, FSM.HOME_OF);	// home_of property
		
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
		// TODO: Remove duplicate if possible.
		StringBuilder sb = new StringBuilder();
		
		// Select "Roster" relations.
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
				
				sb.append(String.format("%s\t%s\t%s\t%s\n", playerKey, playerLiteral, clubKey, clubLiteral ));
			}
		}
		// Select "Plays For" relations.
		Property playsForProp = model.createProperty(FSM.NS,FSM.PLAYS_FOR);
		Selector selectPlaysFor = new SimpleSelector(null,playsForProp,(RDFNode)null);
		StmtIterator it2 = model.listStatements(selectPlaysFor);
		while (it2.hasNext()) {	// each "plays for" relationship.
			Statement stmt = it2.nextStatement();
			Resource player = stmt.getSubject();
			RDFNode club = stmt.getObject();
			String clubKey = "";
			String clubLiteral = "";
			String playerKey = player.getURI();						// get player URI key.
			String playerLiteral = Solver.findName(player, model);	// get player literal. 
			if (club.isResource()) {
				clubKey = club.asResource().getURI();
				clubLiteral = Solver.findName(club.asResource(), model);
			} else if (club.isLiteral()) {
				clubLiteral = club.asLiteral().getString();
			};
			sb.append(String.format("%s\t%s\t%s\t%s\n", playerKey, playerLiteral, clubKey, clubLiteral));

		}
		
		return sb.toString();
	}
	
	private static String solver3(Model model) {
		// Who coaches a team with Spanish players?
		StringBuilder sb = new StringBuilder();

		Property rosterPlayer = model.createProperty(Solver.FreebaseNameSpace,Solver.FreebaseRosterPlayer);
		Property nationality = model.createProperty(FSM.NS,FSM.NATIONALITY);
		Property rosterFrom = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseRosterFrom);
		Property rosterTeam = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseRosterTeam);
		Property tenureFrom = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseTenureFrom);
		Property tenureTo = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseTenureTo);
		Property tenureTeam = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseTenureTeam);
		Property tenureManager = model.createProperty(Solver.FreebaseNameSpace, Solver.FreebaseTenureManager);
		Set<String> setOfManagers = new HashSet<String>();
		
		RDFNode literalSpanish = model.createLiteral("Spanish");

		// 1) find players of Nationality Spanish.
		ResIterator listOfSpanish = model.listSubjectsWithProperty(nationality, literalSpanish);
		
		while (listOfSpanish.hasNext()) {
			Resource player = listOfSpanish.nextResource();
			//sb.append(String.format("Spanish Player %s\n", player.getURI()));
			
			// 2) find team and year of the players was party of.	
			ResIterator listOfRoster = model.listSubjectsWithProperty(rosterPlayer,player);
			while (listOfRoster.hasNext()) {	// for each roster with the player.
				Resource roster = listOfRoster.nextResource();
				NodeIterator rosterTimeNode = model.listObjectsOfProperty(roster, rosterFrom);
				NodeIterator rosterTeamNode = model.listObjectsOfProperty(roster,rosterTeam); 
				int playerYear = 0;
				Resource team = null;
				if (rosterTimeNode.hasNext()) {	// find year of roster.
					String timeLiteral = rosterTimeNode.nextNode().asLiteral().getLexicalForm();	// get year as literal.
					playerYear = Integer.parseInt(timeLiteral.substring(0, 4));	// parse year as first 4 digit.
				} else {
					continue; 	// skip this roster because no year given.
				}
				
				if (rosterTeamNode.hasNext()) {	// find team of roster.
					team = rosterTeamNode.nextNode().asResource();	// get team as resource.
				} else {
					continue;	// skip this roster because no team given.
				}
				
				
				// 3) find manager of the team of that year.
				
				ResIterator listOfTenure = model.listResourcesWithProperty(tenureTeam, team);
				while (listOfTenure.hasNext()) {	// for each management tenure of the team.
					Resource tenure = listOfTenure.nextResource();
					NodeIterator tenureFromNode = model.listObjectsOfProperty(tenure, tenureFrom);
					NodeIterator tenureToNode = model.listObjectsOfProperty(tenure, tenureTo);
					NodeIterator tenureManagerNode = model.listObjectsOfProperty(tenure, tenureManager);
					int tenureYearFrom = 9999;
					int tenureYearTo = 9999;
					Resource manager = null;
					
					if (tenureFromNode.hasNext()) {	// find tenure start year.
						String fromLiteral = tenureFromNode.nextNode().asLiteral().getLexicalForm();	// get from year as literal.
						tenureYearFrom = Integer.parseInt(fromLiteral.substring(0, 4));
					} else {
						continue;	// skip this tenure because no year of Start given.
					}
					if (tenureToNode.hasNext()) {	// find tenure end year.
						String toLiteral = tenureToNode.nextNode().asLiteral().getLexicalForm();	// get from year as literal.
						tenureYearTo = Integer.parseInt(toLiteral.substring(0, 4));
					}
					if (tenureManagerNode.hasNext()) {	// find tenure manager.
						manager = tenureManagerNode.next().asResource();	// get team manager.
					} else {
						continue;	// skip this tenure if no manager given.
					}
					
					// check if year is within bound.
					if ((playerYear <= tenureYearTo) && (playerYear >= tenureYearFrom)) {
						
						setOfManagers.add(manager.getURI());
						
					}
					
					
				}
				
			}
		}
		
		// print out managers.
		for (String uri:setOfManagers) {
			sb.append(String.format("%s\n",uri));
		}
		
		return sb.toString();
	}
	
	private static String solver4(Model model) {
		// Which clubs have stadiums named after former presidents?
		StringBuilder sb = new StringBuilder();
		
		Property namedAfter = model.createProperty(FSM.NS,FSM.NAMED_AFTER);
		Property homeOf = model.createProperty(FSM.NS,FSM.HOME_OF);
		
		// 1) find stadiums named after president.
		Selector selectNamedAfter = new SimpleSelector(null,namedAfter,(RDFNode)null);
		StmtIterator it = model.listStatements(selectNamedAfter);
		
		//ResIterator listOfStadiums = model.listSubjectsWithProperty(namedAfter);
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			String clubKey = "";
			String clubLiteral = "";
			String stadiumKey = "";
			String stadiumLiteral = "";
			String presidentLiteral = "";
			
			Resource stadium = stmt.getSubject();	
			stadiumKey = stadium.getURI();			// get stadium uri.
			stadiumLiteral = Solver.findName(stadium, model);	// get stadium name.
			RDFNode president = stmt.getObject();
			if (president.isLiteral()) {
				presidentLiteral = president.asLiteral().getString();
			};
			
			NodeIterator listOfClubs = model.listObjectsOfProperty(stadium, homeOf);	// find clubs.
			while (listOfClubs.hasNext()) {
				RDFNode club = listOfClubs.nextNode();
				if (club.isResource()) {
					clubKey = club.asResource().getURI();
					clubLiteral = Solver.findName(club.asResource(), model);
				} else if (club.isLiteral()){
					clubLiteral = club.asLiteral().getString();
				}
			}
			
			// generate output.
			sb.append(String.format("%s\t%s\t%s\t%s\t%s\n",
					clubKey,clubLiteral,stadiumKey,stadiumLiteral,presidentLiteral));
			
		}
		
		return sb.toString();
	}
	
	private static String solver5(Model model) {
		//Which teams have the most nationalities amongst their roaster?
		StringBuilder sb = new StringBuilder();
		
		Property rosterTeam = model.createProperty(Solver.FreebaseNameSpace,Solver.FreebaseRosterTeam);
		Property rosterPlayer = model.createProperty(Solver.FreebaseNameSpace,Solver.FreebaseRosterPlayer);
		Property nationality = model.createProperty(FSM.NS,FSM.NATIONALITY);
		
		// 1. Get list of teams with roster.
		NodeIterator listOfTeams = model.listObjectsOfProperty(rosterTeam);
		Set<RDFNode> setOfTeams = listOfTeams.toSet();
		Map<Resource,Integer> rank = new HashMap<Resource, Integer>();
		
		// 2. For each team, get its roster nodes.
		for (RDFNode teamKey: setOfTeams) {
			
			ResIterator listOfRosters = model.listResourcesWithProperty(rosterTeam, teamKey);
			Set<String> nationalitySet = new HashSet<String>();
			
			// 3. For each roster, get all its players.
			while (listOfRosters.hasNext()) {	
				
				NodeIterator listOfPlayers = model.listObjectsOfProperty(listOfRosters.nextResource(),rosterPlayer);
				
				while (listOfPlayers.hasNext()) {
					
					NodeIterator listOfNation = model.listObjectsOfProperty(listOfPlayers.nextNode().asResource(), nationality);
					
					if (listOfNation.hasNext()) {
						RDFNode nat = listOfNation.nextNode();
						if (nat.isLiteral()) {
							nationalitySet.add(nat.asLiteral().getString());
							
						}
					}
					
					
				}
			}
			
			rank.put(teamKey.asResource(), nationalitySet.size());
			
		}
		
		rank = Solver.sortByValue(rank);	// Sorting map by value (reverse).
		
		for (Resource team:rank.keySet()) {
			sb.append(String.format("%s\t%s\t%d\n", team.getURI(),Solver.findName(team, model),rank.get(team)));
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
	
	private static boolean ssCompare (String s1, String s2) {
		// String distance measurement
		JaroWinkler jw = new JaroWinkler();
		if (jw.score(s1, s2) > 0.8) {
			return true;
		}
		return false;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		// This method was found at: http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ) {
				//return (o1.getValue()).compareTo( o2.getValue() );
				return (o2.getValue()).compareTo( o1.getValue() );
			}
		} );
		
		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
}
}
