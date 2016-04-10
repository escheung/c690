import java.util.ArrayList;
import java.util.Vector;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;

public class Engine {

	// Public constants
	public static final String LOC_labels = "resource/labels";
	public static final String LOC_disambiguations = "resource/disambiguations";
	public static final String NS_dbo = "http://dbpedia.org/ontology/";
	public static final String NS_rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String LN_label = "label";
	public static final String LN_wikiDisam = "wikiPageDisambiguates";
	
	
	// Private variables
	private Dataset ds_labels = null;
	private Dataset ds_disam = null;
	
	Engine () {
		this.ds_labels = TDBFactory.createDataset(Engine.LOC_labels);
		this.ds_disam = TDBFactory.createDataset(Engine.LOC_disambiguations);
		
	}
	
	public Resource matchLabel(String target) {
		// Attempt to find the target label from the knowledge base (dbpedia) with label property.
		Model model = ds_labels.getDefaultModel();
		Property property = model.createProperty(NS_rdfs, LN_label);
		RDFNode node = model.createLiteral(target, "en");
		ResIterator it = model.listResourcesWithProperty(property, node);
		
		if (it.hasNext()) {
			Resource resource = it.next();
			return resource;
		}
		return null;
	}
	
	public ArrayList<Resource> getDisams(Resource resource) {
		// Find and return the resources's disambiguations, return null if nothing.
		ArrayList<Resource> nodes = new ArrayList<Resource>();
		Model model = ds_disam.getDefaultModel();
		Property property = model.createProperty(NS_dbo,LN_wikiDisam);
		NodeIterator it = model.listObjectsOfProperty(resource, property);
		while (it.hasNext()) {
			RDFNode node = it.next();
			if (node.isResource()) {
				nodes.add(node.asResource());
			}
		}
		return nodes;
	}
	
	public Resource getDisamRes(Resource object) {
		// Try reverse lookup for disambiguation page using source as object, return null if nothing.
		Model model = ds_disam.getDefaultModel();
		Property property = model.createProperty(NS_dbo, LN_wikiDisam);
		ResIterator it = model.listResourcesWithProperty(property, object);
		
		if (it.hasNext()) {
			Resource resource = it.next();
			return resource;
		}
		return null;
	}
	
	public void close() {
		// Close dataset
		ds_labels.end();
		ds_disam.end();
		
	}
	
	
	
}
