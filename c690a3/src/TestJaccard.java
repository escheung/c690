import com.wcohen.ss.Jaccard;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.api.StringWrapper;

public class TestJaccard {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		String s1 = "Bologna F.C.";
		String s2 = "Bologna_FC";
		String s3 = "Bologna";
		String s4 = "/wikipedia/en/Reggina_Calcio";
		
		jaroCompare(s1,s2);
		jaroCompare(s1,s3);
		jaroCompare(s2,s3);
		
		//jCompare(s1,s2);
		
	}

	private static void jaroCompare(String s, String t) {
		
		JaroWinkler jw = new JaroWinkler();
		System.out.println(jw.score(s, t));
		
	}
	
	private static void jCompare(String s, String t) {
		
		Jaccard jaccard = new Jaccard();
		StringWrapper sw1 = jaccard.prepare(s);
		StringWrapper sw2 = jaccard.prepare(t);
		System.out.println(jaccard.explainScore(s,t));
		System.out.println(jaccard.score(s, t));
		
	}
}
