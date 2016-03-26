import com.wcohen.ss.Jaccard;
import com.wcohen.ss.api.StringWrapper;

public class TestJaccard {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		String s1 = "Reggina Calcio";
		String s2 = "Reggina_Calcio";
		String s3 = "Reggina";
		String s4 = "/wikipedia/en/Reggina_Calcio";
		
		jCompare(s1,s2);
		
		jCompare(s1,s3);
		
		jCompare(s1,s4);
	}

	
	private static void jCompare(String s, String t) {
		
		Jaccard jaccard = new Jaccard();
		StringWrapper sw1 = jaccard.prepare(s);
		StringWrapper sw2 = jaccard.prepare(t);
		System.out.println(jaccard.explainScore(s,t));
		
	}
}
