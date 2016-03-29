
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
		RDFNode object = model.createLiteral("Juan_Iturbe");
		// find stuff
		ResIterator rit = model.listSubjectsWithProperty(prop, object);		
		while (rit.hasNext()) {
			Resource r = rit.nextResource();
			System.out.println("resource is:"+r.getURI());
			
		}

		//testDefaultModel();
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
				System.out.println("checking literal:"+l);
				if (jaccardTest(l,literalString)) {
					return s;
				}
			}
		}
		
		return resource;
	}

	private static void testDefaultModel() {
		String NS = "http://rdf.freebase.com/key/";
		String PROP = "wikipedia.en";
		Model model = ModelFactory.createDefaultModel();
		
		Resource s = model.createResource(NS+"resource_1");
		Property p = model.createProperty(NS, "is_a");
		model.add(model.createLiteralStatement(s,p,"literal_1"));
		
		Resource s2 = findLiteral("literal_1",p,model);
		System.out.println("default model found: "+s2.getURI());
	}
	
	private static boolean jaccardTest (String s1, String s2) {
		Jaccard jaccard = new Jaccard();
		if (jaccard.score(s1, s2)==1.0) {
			return true;
		}
		return false;
	}
}
