import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jena.rdf.model.Resource;

public class TestEngine {

	
	static Engine engine = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		engine = new Engine();
		
		TestMatchLabel();
		
		TestGetDisams();
		
		TestGetDisamRes();
		
		engine.close();
	}

	protected static void TestMatchLabel() {
		
		long startTime = System.nanoTime() ;
		Resource resource = engine.matchLabel("Alien");
		String uri = null;
		if (resource!=null) uri=resource.getURI();
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6 ;
		
		System.out.println(String.format("MatchLabel: URI:%s Time:%f0.5", uri,time));
		
		
	}
	
	protected static void TestGetDisams() {
		
		
		Resource resource = engine.matchLabel("Alien");
		long startTime = System.nanoTime() ;
		ArrayList<Resource> resources = engine.getDisams(resource);
		Iterator<Resource> it = resources.iterator();
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6 ;
		
		while (it.hasNext()) {
			Resource res = it.next();
			
			System.out.println(String.format("Disam: URI:%s NS:%s", res.getURI(), res.getNameSpace()));
		}
		System.out.println(String.format("Time: %f0.5", time));
	}
	
	protected static void TestGetDisamRes() {
		
		Resource target = engine.matchLabel("Austin, Quebec");
		long startTime = System.nanoTime() ;
		Resource resource = engine.getDisamRes(target);
		long finishTime = System.nanoTime();
		double time = (finishTime-startTime)/1.0e6 ;
		
		if (resource != null) {
			System.out.println(String.format("Disam: URI:%s", resource.getURI()));
		}
		System.out.println(String.format("Time: %f0.5", time));
		
	}
	
}
