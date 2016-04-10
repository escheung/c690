import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTables {

	
	private static String TableSource = "resource/wikitables";
	private static String TableOutput = "resource/tables";
	private static String TableStart = "{|";
	private static String TableCaption = "|+";
	private static String TableRow = "|-";
	private static String TableHeaderCell = "!";
	private static String TableDataCell = "|";
	private static String TableEnd = "|}";
	private static String TableDataRow = "||";
	private static String TableHeaderRow = "!!";
	
	public static void main(String[] args) {
		
		// Set file filter
		FilenameFilter xmlFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".xml")) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		// Read directory for files
		File folder = new File(TableSource);
		File[] listOfFiles = folder.listFiles(xmlFilter);
		
		// Send each file for parsing
		for (int i=0; i<listOfFiles.length; i++) {
			File inputfile = listOfFiles[i];
			System.out.println(String.format("*** Source: %s ***", inputfile.getName()));
			// get body of wikitables
			ArrayList<String> tables = parseFile(inputfile);
			System.out.println("\t# of tables: "+tables.size());
			for (int j=0; j<tables.size(); j++) {
				String inputname = inputfile.getName().substring(0, inputfile.getName().indexOf('.'));

				// make output txt filename (for wikitable code)
				String txtfn = String.format("%s_%d.txt", inputname,j);
				
				// make output tsv filename (for extracted data/no header)
				String tsvfn = String.format("%s_%d.tsv", inputname,j);
				
				// parse content of wikitables
				String tableText = parseWikitable(tables.get(j));
				if (tableText.isEmpty()) {
					System.out.println(String.format("\tSkipping: %s", tsvfn));
					continue;
				};
				System.out.println(String.format("\tProducing: %s", tsvfn));
				
				File tsvfile = new File(TableOutput,tsvfn);
				File txtfile = new File(TableOutput,txtfn);
				
				// write wikitables to files
				try {
					PrintWriter writer = new PrintWriter(txtfile);
					writer.print(tables.get(j));
					writer.close();
				} catch (Exception e) {
					System.err.println(e.toString());
				}
				// write extracted content to files
				try {
					PrintWriter writer = new PrintWriter(tsvfile);
					writer.print(tableText);
					writer.close();
				} catch (Exception e) {
					System.err.println(e.toString());
				}

			}
			
		}

	}
	
	protected static ArrayList<String> parseFile(File file) {
		
		ArrayList<String> tables = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			StringBuilder body = new StringBuilder();
			int state = 0;
			while ((line = br.readLine())!=null) {
				switch (state) {
				case 0:
					if (line.startsWith("{|")) {
						// table starts
						body.append(line+'\n');
						state = 1;
					} else {
						// null state 
						state = 0;
					}
				break;
				case 1:
					body.append(line+'\n');			// add line to body.
					if (line.startsWith("|}")) {
						// table ends
						tables.add(body.toString());	// add body to list of tables. 
						body = new StringBuilder();		// reset buffer.
						state = 0;						// go to state 0.
					} else {
						state = 1;						// stay in state 1.
					}
				break;
				default:
				break;
				}
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		
		
	    return tables;
	}
	
	protected static String parseWikitable(String content) {
		// parse wiki tables
		
		// parse wiki table using wiki table notation. https://en.wikipedia.org/wiki/Help:Table
		if (content==null) return "";
			
		StringReader sr = new StringReader(content);
		BufferedReader br = new BufferedReader(sr);
		ArrayList<String> header = new ArrayList<String>();
		ArrayList<String> cols = new ArrayList<String>();
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		
		String line = "";
		String message = "";
		int state = 0;
		int lineNum = 0;
		try {
				
			
			while ((line = br.readLine()) != null) {
				line = line.trim();						// strip leading or trailing spaces.
				if (line.isEmpty()) continue;	// skip empty lines.
				switch (state) {
				case 0:
					if (line.startsWith(TableStart) && line.contains("wikitable")) {	// found start of table.
						state = 1;	// go to state 1.
					} else {
						state = 0;	// stay in state 0;
					}
					break;
				case 1:
					if (line.startsWith(TableRow)) {	// line starts with table row '|-'
						state = 2;	// go to state 2.
					} else if (line.startsWith(TableCaption)) {	// table caption row; not used. '|+'
						state = 1;	// stay in 1;
					} else if (line.startsWith(TableHeaderCell) &&
								(line.contains(TableHeaderRow)||(line.contains(TableDataRow)))) {	
						// header row. '!' and ('!!' or '||')
						String[] arr = ParseTables.parseHeaderRow(line);
						header.addAll(Arrays.asList(arr));
						state = 5;
					} else if (line.startsWith(TableHeaderCell)) {	// header cell '!'
						String cell = CleanCell(line);	// clean up cell to get data.
						header.add(cell);					// add cell to column.
						state = 3;						// go to state 3
					} else {
						message = "State 1: Table Start.";
						state = 99;	// unexcepted; go to error state.
					}
					break;
				case 2:
					if (line.startsWith(TableEnd)) {	// table ends.
						state = 9;	// go to end state;
					} else if (line.contains(TableHeaderRow)) {	// combined header row '!!'
						String[] arr = ParseTables.parseHeaderRow(line);
						header.addAll(Arrays.asList(arr));
						state = 5;
					} else if (line.contains(TableDataRow)) {	// combined data row '||'
						String[] arr = ParseTables.parseDataRow(line);
						cols.addAll(Arrays.asList(arr));
						state = 6;
					} else if (line.startsWith(TableHeaderCell)) {	// header cell
						String cell = CleanCell(line);	// clean up cell to get data.
						header.add(cell);					// add cell to column.
						state = 3;						// go to state 3
					} else if (line.startsWith(TableDataCell)) {	// data cell
						String cell = CleanCell(line);	// clean up cell to get data
						cols.add(cell);					// add cell to column. 
						state = 4;						// go to state 4
					} else {
						message = "State 2: Table Row";
						state = 99;	// unexpected; go to error state.
					}
					break;
				case 3:
					if (line.startsWith(TableEnd)) {	// table ends. '|}'
						state = 9;	// go to end state;
					} else if (line.startsWith(TableRow)) {	// table row '|-'
						// header already stored in arraylist.
						state = 2;						// go back to state 2.
					} else if (line.startsWith(TableHeaderCell)) {	// '!'
						String cell = CleanCell(line);	// clean up cell to get header.
						header.add(cell);				// add cell to header arraylist
						state = 3;						// stay in state 3
					} else if (line.contains(TableDataRow)) {	// combined data row '||'
						String[] arr = ParseTables.parseDataRow(line);
						cols.addAll(Arrays.asList(arr));
						state = 6;
					} else {
						message = "State 3: Header Cell";
						state = 99;	// unexpected; go to error state.
					}
					break;
				case 4:
					if (line.startsWith(TableEnd)) {	// table ends.
						state = 9;	// go to end state;

					} else if (line.startsWith(TableRow)) {
						rows.add((ArrayList<String>)cols.clone());	// add columns to rows.
						cols.clear();					// clear columns buffer.
						state = 2;						// go to state 2
					} else if (line.startsWith(TableDataCell)) {	// data cell '|'
						cols.add(CleanCell(line));		// add cleaned cell to column.
						state = 4;						// go to state 4
					} else {
						message = "State 4: Data Cell";
						state = 99;	// unexpected;
					}
					break;
				case 5:
					if (line.startsWith(TableEnd)) {	// table ends.
						state = 9;	// go to end state;
					} else if (line.startsWith(TableRow)) {	// table row '|-'
						// header already stored.
						state = 2;						// go to state 2.
					} else {
						message = "State 5: Combined header row.";
						state = 99;	// unexpected;
					}
					break;
				case 6:
					if (line.startsWith(TableEnd)) {	// table ends.
						state = 9;	// go to end state;
					} else if (line.startsWith(TableRow)) {
						rows.add((ArrayList<String>)cols.clone());		// add column arraylist to rows arraylist
						cols.clear();					// clear columns buffer for next row.
						state = 2;
					} else {
						message = "State 6: Combined data row.";
						state = 99;	// unexpected.
					}
					break;
				default:
					break;
				}
				if (state == 9) {	// end state 
					break;
				} else if (state == 99) {
					System.err.println(String.format("Parse Error: %s. \"%s\"(%d)",message,line,lineNum));
					return "";	// return emtpy string as table.
					
				}
				lineNum++;
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		
		return TableToText(header,rows);
		
	}
	
	public static String TableToText(ArrayList<String> header, ArrayList<ArrayList<String>> table) {
		StringBuilder sb = new StringBuilder();
	
		/*
		// Print table header.	
		for (String cell:header) {
			sb.append(String.format("%s\t", cell));
		}
		
		sb.append('\n');
		*/
		
		// Print table content.
		for (ArrayList<String> row:table) {
			for (String cell:row) {
				sb.append(String.format("%s\t", cell));
				
			}
			sb.append('\n');
		}
		
		
		return sb.toString();
		
	}
	
	public static String CleanCell(String text) {
		// clean data cell by stripping leading spaces, !, |, and surrounding brackets and quotes.
		if (text == null) return null;
		String result = text;
		
		while (result.startsWith("!") ||
				result.startsWith("|") ||
				result.startsWith(" ") ||
				result.endsWith(" ")) {
			
			result = result.replaceAll("^!","");		// remove leading !.
			result = result.replaceAll("^\\|","");		// remove leading |.
			result = result.trim();						// remove leading/trailing spaces.
		}
		
		while (
				(result.startsWith("\"") && result.endsWith("\"")) ||
				(result.startsWith("\'") && result.endsWith("\'")) ||
				(result.startsWith("[") && result.endsWith("]")) ||
				(result.startsWith("{") && result.endsWith("}"))
				) 
		{
			
			result = result.replaceAll("^\"|\"$", "");	// remove leading and trailing quote.
			result = result.replaceAll("^\'|\'$", "");	// remove leading and trailing quote.
			result = result.replaceAll("^\\[|\\]$", "");	// remove leading and trailing bracket.
			result = result.replaceAll("^\\{|\\}$", "");	// remove leading and trailing bracket.
			
		};
		return result;
	}
	
	public static String[] parseHeaderRow(String text) {
		
		
		String[] result = new String[0];
		if (text.contains(TableHeaderRow)) {
			result = text.split(TableHeaderRow);
		} else if (text.contains(TableDataRow)) {
			result = text.split("\\|\\|");
		}
		
		for (int i=0; i<result.length; i++) {
			result[i] = CleanCell(result[i]);
		}
		return result;
	}
	
	public static String[] parseDataRow(String text) {
		
		String[] result = text.split("\\|\\|");
		for (int i=0; i<result.length; i++) {
			result[i] = CleanCell(result[i]);
		}
		return result;
	}
}
