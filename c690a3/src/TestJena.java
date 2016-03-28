
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;

import com.wcohen.ss.Jaccard;

public class TestJena {

	public static void main(String[] args) {

		String SOURCE = "sample.ttl";
		String NS = "http://rdf.freebase.com/key/";
		String PROP = "wikipedia.en";
		Model model = RDFDataMgr.loadModel(SOURCE);
		
		Property prop = model.createProperty(NS,PROP);
		
		// find stuff
		Resource resource = findLiteral("Juan Iturbe", prop, model);		
		
		if (resource != null) {
			System.out.println(resource.getURI());
			
		}

		
	}
	
	private static Resource findLiteral(String literalString, Property property, Model model) {
		Resource resource = null;
		//Literal literal = model.createLiteral(literalString,"en");
		Selector selector = new SimpleSelector(null,property,(RDFNode)null);
		
		StmtIterator it = model.listStatements(selector);
		while (it.hasNext()) {
			Statement stmt = it.nextStatement();
			Resource s = stmt.getSubject();
			Property p = stmt.getPredicate();
			RDFNode o = stmt.getObject();
			if (o.isLiteral()) {
				String l = o.asLiteral().getString();
				if (jaccardTest(l,literalString)) {
				//if (l.equalsIgnoreCase("Juan_Iturbe")) {
				//	System.out.println(String.format("%s\t%s\t%s\t%s",s.toString(),p.getNameSpace(),p.getLocalName(),o.asLiteral().getString()));
					return s;
				}
			}
		}
		
		return resource;
	}

	private static boolean jaccardTest (String s1, String s2) {
		Jaccard jaccard = new Jaccard();
		if (jaccard.score(s1, s2)==1.0) {
			return true;
		}
		return false;
	}
}
