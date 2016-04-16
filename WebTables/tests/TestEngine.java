import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

public class TestEngine {

	
	static Engine engine = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		engine = new Engine();
		
		TestMatchLabel();
		
		TestGetDisams();
		
		TestGetDisamReverse();
		
		TestFindCandidates();
		
		TestGetInstanceType();
		
		TestGetParents();
		
		engine.close();
	}

	protected static void TestFindCandidates() {
		
		System.out.println("--- Testing FindCandidates ---");
		String target = "Bleach";
		
		List<Resource> candidates = engine.findCandidates(target);
		
		for (Resource candidate:candidates) {
			System.out.println(String.format("%s",candidate.getURI()));
			
		}
		
	}
	
	protected static void TestMatchLabel() {
		
		System.out.println("--- Testing MatchLabel ---");
		long startTime = System.nanoTime() ;
		Resource resource = engine.matchLabel("Alien");
		String uri = null;
		if (resource!=null) uri=resource.getURI();
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6 ;
		
		System.out.println(String.format("MatchLabel: URI:%s Time:%f0.5", uri,time));
		
		
	}
	
	protected static void TestGetDisams() {
		
		System.out.println("--- Testing GetDisams ---");
		Resource resource = engine.matchLabel("Alien");
		long startTime = System.nanoTime() ;
		List<Resource> resources = engine.getDisams(resource);
		Iterator<Resource> it = resources.iterator();
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6 ;
		
		while (it.hasNext()) {
			Resource res = it.next();
			
			System.out.println(String.format("Disam: URI:%s NS:%s", res.getURI(), res.getNameSpace()));
		}
		System.out.println(String.format("Time: %f0.5", time));
	}
	
	protected static void TestGetDisamReverse() {
		
		System.out.println("--- Testing GetDisamReverse ---");
		Resource target = engine.matchLabel("Austin, Quebec");
		System.out.println(String.format("Target: %s", target.getURI()));
		long startTime = System.nanoTime() ;
		Resource resource = engine.getDisamReverse(target);
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6 ;
		
		if (resource != null) {
			System.out.println(String.format("Disam: URI:%s", resource.getURI()));
		}
		System.out.println(String.format("Time: %f0.5", time));
		
	}
	

	protected static void TestGetInstanceType() {
		
		System.out.println("--- Testing GetInstanceType ---");
		Resource target = engine.matchLabel("Soccer");
		System.out.println(String.format("Target: %s", target.getURI()));
		long startTime = System.nanoTime();
		Resource resource = engine.getInstanceType(target);
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6;
		if (resource !=null) {
			System.out.println(String.format("Type: URI:%s", resource.getURI()));
		} else {
			System.out.println("No Type found.");
		}
		System.out.println(String.format("Time: %f0.5", time));
		
	}
	
	protected static void TestGetParents() {
		
		System.out.println("--- Testing GetParents ---");
		Resource resource = engine.matchLabel("Bleach (anime)");
		Resource type = engine.getInstanceType(resource);
		long startTime = System.nanoTime();
		if (type!=null) {
			List<Resource> parents = engine.getAncestors(type);
			Iterator<Resource> it = parents.iterator();
			while (it.hasNext()) {
				Resource t = it.next();
				System.out.println(t.getURI());
			}
		} else {
			System.out.println("No type");
		}
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6;
		System.out.println(String.format("Time: %f0.5",  time));
	}
	
}
