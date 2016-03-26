import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.Triple;


public class TestStanford {
	
	
	public static void main(String[] args) throws Exception {

		String serializedClassifier = "resources/english.all.3class.distsim.crf.ser.gz";
		String posModel = "resources/english-left3words-distsim.tagger";
		
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		MaxentTagger tagger = new MaxentTagger(posModel);
		
		String text = "Wayne Gretzky is a Canadian hockey player.  He is also called The Great One.";
		
	    List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(text));
	    for (List<HasWord> sentence : sentences) {
	      List<TaggedWord> tSentence = tagger.tagSentence(sentence);
	      System.out.format("Sentence:%s", sentence.toString());
	      for (TaggedWord tw: tSentence) {
	    	  System.out.format("%s-%s\n",tw.word(),tw.tag());
	      }
	      
	      //System.out.println(Sentence.listToString(tSentence, false));
	    }
		
	    System.out.println("****");
	    
        List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(text);
        for (Triple<String,Integer,Integer> trip : triples) {
          System.out.printf("%s over character offsets [%d, %d) in sentence.",
                  trip.first(), trip.second(), trip.third);
        }
        
        String[] ss = splitToSentences(text);
        for (String s: ss) {
        	System.out.format("Sentence:%s", s);
        }
        
	}

	public static String[] splitToSentences(String text) {
		// split a piece of text into sentences.
		
		DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(text));
		Vector<String> ss = new Vector<String>();
		
		for (List<HasWord> sentence : dp) {
			ss.add(Sentence.listToString(sentence));
			//System.out.println(sentence);
		}
		
		return ss.toArray(new String[ss.size()]);
	}

}
