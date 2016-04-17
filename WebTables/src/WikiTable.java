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
	private int colResolved=0;
	private String[][] table;
	private List<Resource>[][] candidates;
		
	WikiTable (File tsvfile) {
		
		if (!parseTsvFile(tsvfile)) {	// if failed to parse tsv file.
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
	public int columnsResolved() {
		// return number of column types
		return colResolved;
	}
	
	public void processTableForCandidates(Engine engine) {
		// process each cell in table to find suitable candidates using given 'engine'.
		for (int r=0; r<rowSize; r++) {	// for each row
			for (int c=0; c<colSize; c++) {		// for each column
				candidates[r][c] = engine.findCandidates(table[r][c]);	// find potential candidates
			}
		}
	}
	
	public void processTableForProperties(Engine engine) {
		// TODO: complete this method.
		
	}
	
	public List<Map<Resource, Integer>> processTableForTypes(Engine engine) {
		// process table by columns and use a voting system to determine most popular class type.
		List<Map<Resource, Integer>> listOfTypeMaps= new ArrayList<Map<Resource,Integer>>();
		int classCounter=0;
		for (int c=0; c<colSize; c++) {				// for each column.
			Map<Resource, Integer> typeMap = new HashMap<Resource, Integer>();	// Map for storing class types.
			for (int r=0; r<rowSize; r++) {			// for each row.
				Iterator<Resource> it = candidates[r][c].iterator();
				while (it.hasNext()) {				// while more candidates
					Resource resource = it.next();	
					Resource type = engine.getInstanceType(resource);	// find class type of instance.
					if (type == null) continue;		// skip it if instance type not found.
					
					List<Resource> ancestors = engine.getAncestors(type);
					for (Resource aType:ancestors) {	// for each super class.
						Integer count = typeMap.get(aType);
						if (count == null) {	// first time
							typeMap.put(aType, 1);	// set instance type vote to 1.
						} else {
							typeMap.put(aType,count+1);	// increment instance type vote by 1.
						}	
					}
					
				}
			}	// end of row
			
			if (!typeMap.isEmpty()) {	// if map is not empty.
				classCounter++;			// increment class type counter.
			}
			
			// sort map by vote and added to list.
			listOfTypeMaps.add(sortByValue(typeMap));
		}	// end of column.
		this.colResolved = classCounter;
		return listOfTypeMaps; 
	}
	
	public String printTopClasses(List<Map<Resource,Integer>> list) {
		// print top ranked classes for each column.
		StringBuilder text = new StringBuilder();
		Iterator<Map<Resource,Integer>> map = list.iterator();
		int i=1;	// columne index, starts from 1.
		while (map.hasNext()) {
			text.append(String.format("Column [%d]\t",i));
			text.append(Arrays.toString(topRanked(map.next())));
			text.append("\n");
			i++;
		}
		
		return text.toString();
	}
	
	public String printOrderedTypes(Map<Resource, Integer> sortedMap) {
		// print ordered types from map as String.
		StringBuilder sb = new StringBuilder();
		Set<Resource> sortedKey = sortedMap.keySet();
		for (Resource key:sortedKey) {
			sb.append(String.format("%s\t%d\n", key.getURI(),sortedMap.get(key)));
		}
		return sb.toString();
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
					//System.err.println(String.format("Error! ParseTsvFile: Column sizes do not match. (%s)",tsvfile.getName()));
					return false;
				}
				
				rowCount++;	// increment row counter.
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// create 2D String array to store content.
		if (rowCount>0 && colCount>0) {
			this.rowSize = rowCount;
			this.colSize = colCount;
			this.table = new String[rowCount][colCount];
			this.candidates = new ArrayList[rowCount][colCount];
			
			for (int r=0; r<rowCount; r++) {
				List<String> column = tableList.get(r);
				for (int c=0; c<colCount; c++) {
					
					this.table[r][c] = column.get(c);
					
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
	
	public static String[] topRanked(Map<Resource, Integer> map) {
		// return an array of instance types that have the most 'vote'.
		
		Set<Entry<Resource,Integer>> entrySet = map.entrySet();
		
		int max = 0;
		List<String> best = new ArrayList<String>();
		
		for (Entry<Resource,Integer> entry:entrySet) {
			/*
			if (entry.getKey().getLocalName().equalsIgnoreCase("Thing")) {
				continue;	// skip type if it's a "Thing" class.
			}
			*/
			if (entry.getValue() > max) {
				best = new ArrayList<String>();	// reset list.
				best.add(entry.getKey().getLocalName());			// add 'type' to best list.
				max = entry.getValue();
			} else if (entry.getValue()== max) {
				best.add(entry.getKey().getLocalName());			// add 'type' to best list.
			};
			
		}
		
		return best.toArray(new String[0]);
	}
	

	
	
}
