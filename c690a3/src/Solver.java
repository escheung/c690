import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;



public class Solver {

	
	//private static final String SOURCE = "cmput690_a3.ttl";
	private static final String SOURCE = "sample.ttl";
	
	private static final String OUTPUT_1 = "cmput690w16a3_q1_cheung.tsv";
	private static final String OUTPUT_2 = "cmput690w16a3_q2_cheung.tsv";
	private static final String OUTPUT_3 = "cmput690w16a3_q3_cheung.tsv";
	private static final String OUTPUT_4 = "cmput690w16a3_q4_cheung.tsv";
	private static final String OUTPUT_5 = "cmput690w16a3_q5_cheung.tsv";
	
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
		//Map<Resource,String> docs = getDocuments(model);
		
		// process the 'documents' for relationships and add to model.
		parseDocuments(engine, model);
		
		
//		printVectorString(documents);
		
//		testModel(model);
		
		printStatements(model);	// Print out the statements in model.
		
		
/*	Print answers to file
		PrintWriter writer1 = new PrintWriter(OUTPUT_1,"UTF-8");
		writer1.print(solver1(model));
		writer1.close(); 	
 */
		
	}
	
	private static void parseDocuments(Engine engine, Model m) {
		// Parse documents stored in model and extract additional triples using TSM. 
		
		// Get all documents text from model.
		Map<Resource, String> docs = getDocuments(m);
		
		// For each map keys with document.
		for (Resource key: docs.keySet()) {
			// Get document from map using key.
			String doc = docs.get(key);
			doc = FSM.delBrackets(doc);	// delete all brackets & content.
			
			// split each document into sentences.
			String[] sentences = engine.splitToSentences(doc);
			
			System.out.format("*** URI: %s ***\n",key.getURI());
			
			for (int i=0; i < sentences.length; i++) {
			//for (String sentence: sentences) {
				Vector<String> words = new Vector<String>();	// Vector of words/tokens.
				Vector<String> tags = new Vector<String>();		// Vector of POS tags.
				Vector<Trip> trips = new Vector<Trip>();		// Vector of triples returned from FMS.
				
				String sent = sentences[i];
				
				// Ask engine to Tokenize and apply POS Tagging
				engine.posTagging(sent, words, tags);
				
				// ** Process first sentence **
				if (i==0) {
					// Perform Is_A FSM.
					trips = FSM.findIsA(key.getURI(), words.toArray(new String[words.size()]), tags.toArray(new String[tags.size()]));
				}
				
				Iterator<Trip> it = trips.iterator();
				while (it.hasNext()) {		// for each triple
					Trip t = it.next();
					addTripToModel(t, m);	// Add discovered Triples to Model
				}
				
				
				
				
			}
 
					
			
			
		}
		
		
		
	}
	



	private static Map<Resource,String> getDocuments(Model m) {
		// Look for "hasDocument" predicate in the model, 
		// create a map using with the resources as map keys and the document strings as map content.
		
		String hasDocLocalKey = "cmput690hasDocument";
		Map<Resource, String> map = new HashMap<Resource, String>();
		Selector selector = new SimpleSelector(null,null,(RDFNode)null);
		StmtIterator it = m.listStatements(selector);
		
		while(it.hasNext()) {
			
			Statement stmt = it.nextStatement();
			Resource s = stmt.getSubject();
			Property p = stmt.getPredicate();
			RDFNode o = stmt.getObject();
			
			// Look for the "cmput690hasDocument" property.
			if (p.getLocalName().equalsIgnoreCase(hasDocLocalKey) && o.isLiteral()) {
				map.put(s, o.asLiteral().getString());
			};
		}
		return map;
	}
	
	private static String solver1(Model m) {
		// Which stadiums are used by which clubs?
		StringBuilder sb = new StringBuilder();
		
		sb.append("Hello from solver.1");
		
		return sb.toString();
	}
	
	private static void printVectorString(Vector<String> v) {
		Iterator<String> it = v.iterator();
		
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
	
	private static void printStatements(Model m) {
		
		// list the statements in the Model
		StmtIterator iter = m.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
		    Statement stmt      = iter.nextStatement();  // get next statement
		    Resource  subject   = stmt.getSubject();     // get the subject
		    Property  predicate = stmt.getPredicate();   // get the predicate
		    RDFNode   object    = stmt.getObject();      // get the object

		    System.out.print(subject.toString());
		    //System.out.print(" " + predicate.toString() + " ");
		    System.out.print(" " + predicate.getNameSpace()+" "+predicate.getLocalName() + " ");
		    if (object instanceof Resource) {
		       System.out.print(object.toString());
		    } else {
		        // object is a literal
		        System.out.print(" \"" + object.toString() + "\"");
		    }

		    System.out.println(" .");
		} 		
	}
	
	private static void addTripToModel(Trip trip, Model m) {
		
		Resource subject = m.createResource(trip.getSubject());
		Property predicate = m.createProperty(FSM.NS,trip.getPredicate());
		Statement s = m.createLiteralStatement(subject, predicate, trip.getObject());
		m.add(s);
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
}
