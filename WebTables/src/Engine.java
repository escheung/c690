import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.RDF.Nodes;

public class Engine {

	// Public constants
	public static final String LOC_labels = "resource/labels";
	public static final String LOC_disambiguations = "resource/disambiguations";
	public static final String LOC_types = "resource/types";
	public static final String LOC_ontology = "resource/ontology";
	public static final String LOC_infobox = "resource/infobox";
	public static final String NS_dbo = "http://dbpedia.org/ontology/";
	public static final String NS_rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String NS_rdfs_type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String LN_label = "label";
	public static final String LN_wikiDisam = "wikiPageDisambiguates";
	public static final String LN_type = "type";
	public static final String LN_subclass = "subClassOf";
// <http://www.w3.org/2000/01/rdf-schema#subClassOf>
	
	// Private variables
	private Dataset ds_labels = null;
	private Dataset ds_disam = null;
	private Dataset ds_types = null;
	private Dataset ds_ontology = null;
	private Dataset ds_infobox = null;
	
	Engine () {
		this.ds_labels = TDBFactory.createDataset(Engine.LOC_labels);
		this.ds_disam = TDBFactory.createDataset(Engine.LOC_disambiguations);
		this.ds_types = TDBFactory.createDataset(Engine.LOC_types);
		this.ds_ontology = TDBFactory.createDataset(Engine.LOC_ontology);
		this.ds_infobox = TDBFactory.createDataset(Engine.LOC_infobox);
	}
	
	public List<Resource> findCandidates(String target) {
		// Given a key term, attempt to find a list of candidates from knowledge base.
		List<Resource> candidates = new ArrayList<Resource>();
		// find label
		Resource firstMatch = matchLabel(target);
		
		if (firstMatch != null) {	// label found a match
			
			// get disambiguation resources as candidates.
			candidates = getDisams(firstMatch);
			if (candidates.isEmpty()) {	// not disambiguation page.
				candidates.add(firstMatch);	// add the resource found to to candidate list.
			}
			/*
			if (candidates.isEmpty()) { // no disambiguation found.
				// reverse lookup for disambiguations
				Resource revMatch = getDisamReverse(firstMatch);
				if (revMatch != null) {
					candidates = getDisams(revMatch);
				} else {
					// no reverse match found, add first match to candidate.
					candidates.add(firstMatch);
				}
			}
			*/
			
		} else {
			// Unable to find a label from term.
			// TODO: figure out what to do if no label matches.
		}
		
		return candidates;
	}
	
	public Resource matchLabel(String target) {
		// Attempt to find the target label from the knowledge base (dbpedia) with label property.
		Model model = ds_labels.getDefaultModel();
		if (target==null) return null;							// return null if target is null.
		if (target.isEmpty()) return null;						// return null if target is empty.
		if (Character.isDigit(target.charAt(0))) return null;	// return null if target starts with a number.
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
	
	public Resource getDisamReverse(Resource object) {
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
	
/*	
	public List<Resource> getInstanceType(Resource subject) {
		// find and return the ontology (dbo) type assigned to the subject.
		List<Resource> nodes = new ArrayList<Resource>();
		Model model = ds_types.getDefaultModel();
		Property property = model.createProperty(NS_rdfs_type, LN_type);
		NodeIterator it = model.listObjectsOfProperty(subject, property);
		//NodeIterator it = model.listObjectsOfProperty(subject, null);
		while (it.hasNext()) {
			RDFNode node = it.next();
			if (node.isResource()) {
				nodes.add(node.asResource());
			}
		}
		return nodes;
	}
*/
	public Resource getInstanceType(Resource subject) {
		// find and return the ontology (dbo) type assigned to the subject.
		Model model = ds_types.getDefaultModel();
		Property property = model.createProperty(NS_rdfs_type, LN_type);
		NodeIterator it = model.listObjectsOfProperty(subject, property);
		if (it.hasNext()) {
			RDFNode node = it.next();
			if (node.isResource()) {
				return node.asResource();
			}
		}
		return null;
	}
	
	public List<Resource> getAncestors(Resource type) {
		// get the chain of all super class.
		List<Resource> chain = new ArrayList<Resource>();
		Model model = ds_ontology.getDefaultModel();
		Property property = model.createProperty(NS_rdfs, LN_subclass);

		if (type==null) return chain;	// if type is null, return empty chain.
		
		if (!type.getLocalName().equalsIgnoreCase("Thing")) {	// if type is NOT a generic type 'Thing'
			
			chain.add(type);	// add the given type to chain.
		
			NodeIterator it = model.listObjectsOfProperty(type, property);
			
			if (it.hasNext()) {
				RDFNode node = it.next();
				if (node.isResource()) {
					chain.addAll(getAncestors(node.asResource()));	// recursive call to get ancestors.
				}
			}
		}
		return chain;
	}
	
	public Property getProperty(Resource subject, Resource object) {
		// get list of infobox properties with the given subject and object. 
		Model model = ds_infobox.getDefaultModel();
		List<Property> list = new ArrayList<Property>();
		Property property = null;
		//	listStatements(Resource s, Property p, RDFNode o)
		StmtIterator sit = model.listStatements(subject,null,object);
		
		if (sit.hasNext()) {
			property = sit.next().getPredicate();
		};
		return property;
	}
	
	public void close() {
		// Close dataset
		ds_labels.end();
		ds_disam.end();
		
	}
	
	
	
}
