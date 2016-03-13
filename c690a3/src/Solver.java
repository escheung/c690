import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.*;



public class Solver {

	
	private static final String SOURCE_RDF = "cmput690_a3.ttl";
	
	public static void main(String[] args) {

		Model model = RDFDataMgr.loadModel(SOURCE_RDF);
	
		
	}

}
