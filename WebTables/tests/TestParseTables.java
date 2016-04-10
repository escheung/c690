import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

public class TestParseTables {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		TestCleanCell();
		
		TestParseWikitable();
		
		TestParseHeaderRow();
		
		TestParseDataRow();
		
	}
	
	protected static void TestCleanCell() {
		
		String texts[] = {"!Hello world","[hello]"," ''[[Luke Adam]]'' ","{{sortname|Tim|Thomas|Tim Thomas (ice hockey)}}"};
		for (String text: texts) {
			System.out.println(String.format("Before:%s\tAfter:%s", text, ParseTables.CleanCell(text)));
		}
		
	}
	
	protected static void TestParseDataRow() {
		
		System.out.println("Testing ParseDataRow");
		String text = "| [[Golden Ticket Award for Best New Ride|Best New Ride (Water Park)]] || Dive Bomber || [[Six Flags White Water]]";
		
		String[] result = ParseTables.parseDataRow(text);
		
		System.out.println(Arrays.toString(result));
		
	}
	
	protected static void TestParseHeaderRow() {
		
		System.out.println("Testing ParseHeaderRow");
		String text = "! Category !! class=&quot;unsortable&quot;| 2015 Recipient !! class=&quot;unsortable&quot;| Location/Park";
		
		String[] result = ParseTables.parseHeaderRow(text);
		
		
		System.out.println(Arrays.toString(result));
	}
	
	protected static void TestParseWikitable() {
		
		System.out.println("Testing ParseWikitable");
		File input = new File("resource/tables/Air_Malawi_1.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			StringBuilder body = new StringBuilder();
			String line = "";
			while ( (line=br.readLine())!=null) {
				body.append(line+"\n");
			}
			
			String tableText = ParseTables.parseWikitable(body.toString());
			System.out.println("-- Table Text --");
			System.out.println(tableText);
			
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		
	}
	
	

}
