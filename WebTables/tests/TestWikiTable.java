import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

public class TestWikiTable {

	
	private static String TableFolder = "resource/tables";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		TestWikiTable();
		
		
	}
	

	protected static void TestWikiTable() {
				
		System.out.println("Testing TestWikiTable");
		
		File file = new File(TableFolder,"Greater_Los_Angeles_Area_3.tsv");
		
		System.out.println(String.format("File:%s exists:%s",file.getAbsolutePath(),file.exists()));
		
		WikiTable table = new WikiTable(file);
		
		System.out.println(String.format("row:%d col:%d",table.getRowSize(),table.getColSize()));
		
		printTable(table);
		
		System.out.println("Testing ProcessForCandidates");
		
		Engine engine = new Engine();
		table.processTableForCandidates(engine);
		printCandidates(table);
		
		table.processTableForTypes(engine);
		
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
