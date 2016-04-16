import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;

public class WikiTable {

	private int rowSize=0;
	private int colSize=0;
	private String[][] table;
	private List<Resource>[][] candidates;
		
	WikiTable (File tsvfile) {
		
		if (!parseTsvFile(tsvfile)) {	// if failed parsing tsv file.
			rowSize=0;
			colSize=0;
		};
		
	}
	public boolean ready() {
		return (rowSize>0 && colSize>0);
	}
	
	public int getRowSize() {
		return rowSize;
	}
	public int getColSize() {
		return colSize;
	}
	public String get(int row, int col) {
		return table[row][col];
	}
	public List<Resource> getCandidatesAt(int row, int col) {
		return candidates[row][col];
	}
	
	public void processTableForCandidates(Engine engine) {
		
		for (int r=0; r<rowSize; r++) {	// for each row
			for (int c=0; c<colSize; c++) {		// for each column
				candidates[r][c] = engine.findCandidates(table[r][c]);	// find potential candidates
			}
		}
		
	}
	
	public void processTableForTypes(Engine engine) {
		
		List<Resource> columnTypes = new ArrayList<Resource>();
		
		for (int c=0; c<colSize; c++) {	// for each column.
			Map<Resource, Integer> typeMap = new HashMap<Resource, Integer>();
			System.out.println("Column: "+c);
			for (int r=0; r<rowSize; r++) {	// for each row.
				Iterator<Resource> it = candidates[r][c].iterator();
				while (it.hasNext()) {
					Resource resource = it.next();
					Resource type = engine.getInstanceType(resource);
					if (type == null) continue;	// skip it if instance type not found.
					
					List<Resource> ancestors = engine.getAncestors(type);
					for (Resource aType:ancestors) {	// for each ancestors.
						Integer count = typeMap.get(aType);
						if (count == null) {	// first time
							typeMap.put(aType, 1);	// set instance type to 1.
						} else {
							typeMap.put(aType,count+1);	// increment instance type
						}	
					}
					
				}
			}	// end of row
			
			Map<Resource, Integer> sortedMap = sortByValue(typeMap);
			Set<Resource> sortedKey = sortedMap.keySet();
			for (Resource key:sortedKey) {
				System.out.println(String.format("\t%s\t%d", key.getURI(),typeMap.get(key)));
			}
			
		}	// end of column.
		
	}
	
	private boolean parseTsvFile(File tsvfile) {
		// Parse and store table.  Return false if failed.
		List<List<String>> tableList = new ArrayList<List<String>>();	// temporary storage.		
		int rowCount = 0;	// row counter.
		int colCount = 0;	// column counter.
		
		try {
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(tsvfile));
			
			
			while ((line=br.readLine())!=null) {	// for each line
				// split line by tabs.
				String[] cols = line.split("\t");
				tableList.add(Arrays.asList(cols));		// add columns to table.
				
				if (rowCount==0) {	// First row.
					colCount = cols.length;
				} else if (colCount != cols.length){
					// column lengths do not match; throw error.
					System.err.println(String.format("Error! ParseTsvFile: Column sizes do not match. (%s)",tsvfile.getName()));
					return false;
				}
				
				rowCount++;	// increment row counter.
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// create 2D String array to store content.
		if (rowCount>0 && colCount>0) {
			rowSize = rowCount;
			colSize = colCount;
			table = new String[rowCount][colCount];
			candidates = new ArrayList[rowCount][colCount];
			
			for (int r=0; r<rowCount; r++) {
				List<String> column = tableList.get(r);
				for (int c=0; c<colCount; c++) {
					
					table[r][c] = column.get(c);
					
				}
			}
		}
		
		return true;
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();
		
		st.sorted(Comparator.comparing(e -> e.getValue())).forEachOrdered(e ->result.put(e.getKey(),e.getValue()));
		
		return result;
	}
	
	public String cleanCell(String cell) {
		String entity = "";
		
		
		
		return entity;
	}
	
	
}
