import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Start {

	private static String ResourceFolder = "resource";
	private static String TableFolder = "resource/tables";
	
	public static void main(String[] args) {
		
		// stats counters
		int tableCount=0;
		int tableFailCount=0;
		long startTime = System.nanoTime() ;

		
		// Engine for processing datasets.
		Engine engine = new Engine();
		
		// prepare summary tsv file
		File summaryfile = new File(ResourceFolder,"Summary.tsv");
		
		// get list of tsv files.
		//File[] tsvfiles = GetListOfFiles(TableFolder,"Greater_Los_Angeles_Area_3.tsv");
		File[] tsvfiles = GetListOfFiles(TableFolder,".tsv");
		
		// Summary text.
		StringBuilder summarysb = new StringBuilder();
		
		// parse tsv file for key terms.
		for (File inputfile: tsvfiles) {
			int colCount=0;			// columns
			int colResolved=0;		// columns resolved
			int rowCount=0;			// rows
			int propResolved=0;		// properties resolved.
			tableCount++;			// increment table count.
			
			System.out.println("Processing: "+inputfile.getName());
			
			// parse table from tsv file.
			WikiTable table = new WikiTable(inputfile);
			// prepare output file for result.
			String tablename = FilenameUtils.removeExtension(inputfile.getName());
			String resultfn = String.format("%s_out.txt", tablename);
			File outputfile = new File(TableFolder,resultfn);
			StringBuilder outputsb = new StringBuilder();	// table output file.
			
			if (table.ready()) {
				rowCount = table.getRowSize();				// row count.
				colCount = table.getColSize();				// col count.		
				
				List<Map<Resource, Integer>> classMaps;
				
				// process each table for candidates.
				table.processTableForCandidates(engine);	// discover potential candidates for each cell.
				
				// process each table for top instance type.
				classMaps = table.processTableForTypes(engine);			// process column candidates to find instance types.
				colResolved = table.columnsResolved();		// resolved col count.
				
				// process table for properties.
				Property[][] props = table.processTableForProperties(engine);
				propResolved = table.propertiesResolved();
				
				// write class maps.
				outputsb.append(table.printTopClasses(classMaps));
				
				// write properties.
				outputsb.append(table.printProperties(props));
				
				
			} else {
				// table not available for processing.
				System.err.println("Error! Unable to process table: "+inputfile.getName());
				tableFailCount++;		// failed table counter increment.
			}
			
			// Write table summary to string.
			// <TableName> <# of rows> <# of cols> <# of cols with class>
			summarysb.append(String.format("%s\t%d\t%d\t%d\t%d\n", tablename,rowCount,colCount,colResolved,propResolved));
			
			// write table output text to file.
			try {
				PrintWriter outputWriter  = new PrintWriter(outputfile,"UTF-8");
				outputWriter.println(outputsb.toString());	// write output string to file.
				outputWriter.close();
			} catch (Exception e) {
				System.err.print(e.toString());
			}
			
		}
		
		try {
			PrintWriter summaryWriter = new PrintWriter(summaryfile,"UTF-8");
			summaryWriter.println(summarysb.toString()); 	// Write summary string to file.
			summaryWriter.close();
		} catch (Exception e) {
			System.err.print(e.toString());
		}
		long endTime = System.nanoTime() ;
		double timeUsed = (endTime-startTime)/1.0e9 ;
		System.out.println(String.format("Table Count:%d\tTable Error:%d\tTime Taken:%.2fs\n", tableCount, tableFailCount, timeUsed));
		
	}

	/*
	private static String ProcessTypes(List<List<List<Resource>>> candidates) {
		// process all candidates and assign instance Type for the columns.
		StringBuilder sb = new StringBuilder();
		
		Iterator<List<List<Resource>>> rowIt = candidates.iterator();	// row iterator.
		List<List<Resource>> colTypes = new ArrayList<List<Resource>>();
		List<Map<String, Integer>> typeList = new ArrayList<Map<String,Integer>>();	// list row
		
		while (rowIt.hasNext()) {
			
			List<List<Resource>> row = rowIt.next();	// this row.
			Map<String, Integer> typeMap = new HashMap<String, Integer>();	// a map of types.
			Iterator<List<Resource>> colIt = row.iterator();	// col iterator.
			
			while (colIt.hasNext()) {
				List<Resource> cell = colIt.next();		// this cell.
				
				
				
			}
		}
		
		return sb.toString();
	}
	*/
	/*
	private static List<List<List<Resource>>> ProcessTable(Engine engine, List<List<String>> table) {
		// Process Table to get all potential candidates for each key term.
		List<List<List<Resource>>> allCandidates = new ArrayList<List<List<Resource>>>();
		
		for (int r=0; r<table.size(); r++) {	// for each row
			System.out.println(String.format("row:%d",r));
			List<String> cols = table.get(r);
			List<List<Resource>> listOfKeyCandidates = new ArrayList<List<Resource>>();
			
			for (int c=0; c<cols.size(); c++) {		// for each column
				List<Resource> candidates = engine.findCandidates(cols.get(c));	// find potential candidates
				
				listOfKeyCandidates.add(candidates);
				
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("\tcol:%d\n", c));
				Iterator<Resource> it = candidates.iterator();
				while (it.hasNext()) {
					Resource resource = it.next();
					sb.append(String.format("\t\tcandidate:%s\n", resource.getURI()));
					Resource instType = engine.getInstanceType(resource);
					List<Resource> parents = engine.getParents(instType);
					Iterator<Resource> itOnt = parents.iterator();
					sb.append("\t\t\tontology:\n");
					while (itOnt.hasNext()) {
						Resource type = itOnt.next();
						sb.append(String.format("\t\t\t%s\n",type.getURI()));
					}
				}
				System.out.println(sb);
			}
			
			allCandidates.add(listOfKeyCandidates);
		}
		return allCandidates;
	}
	*/
	/*
	private static String PrintTable(List<List<String>> table) {
		
		StringBuilder sb = new StringBuilder();

		for (int i=0; i<table.size(); i++) {
			List<String> cols = table.get(i);
			for (int j=0; j<cols.size(); j++) {
				
				System.out.print(cols.get(j)+"\t");
				
			}
			System.out.println();
			
		}
		
		return sb.toString();
	}
	*/
	/*
	private static List<List<String>> ParseTsvFile(File tsvfile) {
		
		List<List<String>> table = new ArrayList<List<String>>();
		
		try {
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(tsvfile));
			while ((line=br.readLine())!=null) {	// for each line
				// split line by tabs.
				String[] cols = line.split("\t");
				table.add(Arrays.asList(cols));	// add columns to table.
			}
		} catch (Exception e) {
			
			System.err.println(e.toString());
			
		}
		
		return table;
	}
	*/
	
	private static File[] GetListOfFiles(String folderLocation, String ext) {
		
		File folder = new File(folderLocation);
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(ext.toLowerCase())) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		return folder.listFiles(filter);
	}
}
