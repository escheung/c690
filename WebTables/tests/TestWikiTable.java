import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class TestWikiTable {

	
	private static String TableFolder = "resource/tables";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		TestingWikiTable();
		
		
	}
	

	protected static void TestingWikiTable() {
				
		System.out.println("Testing TestWikiTable");
		
		File file = new File(TableFolder,"1933_in_film_0.tsv");
		
		System.out.println(String.format("File:%s exists:%s",file.getAbsolutePath(),file.exists()));
		
		WikiTable table = new WikiTable(file);
		
		System.out.println(String.format("row:%d col:%d",table.getRowSize(),table.getColSize()));
		
		printTable(table);
		
		System.out.println("Testing ProcessForCandidates");
		
		Engine engine = new Engine();
		table.processTableForCandidates(engine);
		printCandidates(table);
		
		System.out.println("Testing ProcessTableForTypes");
		List<Map<Resource, Integer>> listOfTypeMaps = table.processTableForTypes(engine);
		Iterator<Map<Resource,Integer>> itmap = listOfTypeMaps.iterator();
		while (itmap.hasNext()) {
			System.out.println("----------------");
			Map<Resource,Integer> map = itmap.next();
			System.out.println("-Full Ordered List-");
			System.out.println(table.printOrderedTypes(map));
		}
		
		System.out.println("Testing ProcessTableForProperties");
		Property[][] props = table.processTableForProperties(engine);
		for (int c1=0; c1<table.getColSize(); c1++) {
			for (int c2=0; c2<table.getColSize(); c2++) {
				if (props[c1][c2]!=null) {
					System.out.print(String.format("%s\t", props[c1][c2].getLocalName()));
				} else {
					System.out.print(String.format("%s\t", "null"));
				}
				
			}
			System.out.println();
		}
		
		System.out.println("Testing PrintTopClasses");
		System.out.println(table.printTopClasses(listOfTypeMaps));
		
		
		System.out.println("*** All Resolved Columns ***");
		System.out.println(table.columnsResolved());
		
		
	}
	
	protected static void printCandidates(WikiTable t) {
		for (int r=0; r<t.getRowSize(); r++) {
			System.out.println(String.format("Row %d",r));
			for (int c=0; c<t.getColSize(); c++) {
				System.out.println(String.format("\tCol %d", c));
				List<Resource> resources = t.getCandidatesAt(r,c);
				Iterator<Resource> it = resources.iterator();
				System.out.println("\t\tCandidates:");
				while (it.hasNext()) {
					Resource res = it.next();
					System.out.println(String.format("\t\t%s", res.getURI()));
				}
			}
		}
	}
	
	protected static void printTable(WikiTable t) {
		
		for (int r=0; r<t.getRowSize(); r++) {
			for (int c=0; c<t.getColSize(); c++) {
				System.out.print(String.format("%s\t", t.get(r,c)));
			}
			System.out.println();
		}
		
	}

}
