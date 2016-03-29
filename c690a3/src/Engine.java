
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

public class Engine {

	//private Properties properties = new Properties();
	
	private static String serializedClassifier = "resources/english.all.3class.distsim.crf.ser.gz";
	private static String posModel = "resources/english-left3words-distsim.tagger";
	
	private MaxentTagger tagger;
	private AbstractSequenceClassifier<CoreLabel> classifier;
	
	
	public Engine() throws Exception {
		
		tagger = new MaxentTagger(posModel);
		//classifier = CRFClassifier.getClassifier(serializedClassifier); // Not needed for this assignment?!
		
	}
	
	public void parseAnchorSentence() {
		// parse the anchor/first sentence of a document, related to the given anchor subject key.
		
		
	}
	
	public String[] splitToSentences(String text) {
		// split a piece of text into sentences.
		DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(text));
		Vector<String> ss = new Vector<String>();
		
		for (List<HasWord> sentence : dp) {
			ss.add(Sentence.listToString(sentence));
		}
		
		return ss.toArray(new String[ss.size()]);
	}
	
	public void posTagging(String text, Vector<String> words, Vector<String> tags) {
		// Perform POS tagging on the given text, stored values in vectors provided.
		
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(text));
		for (List<HasWord> sentence : sentences) {
			List<TaggedWord> tSentence = tagger.tagSentence(sentence);
			
			for (TaggedWord tw: tSentence) {
				words.add(tw.word());
				tags.add(tw.tag());
			}
		}
		
//		map.put("words", words.toArray(new String[words.size()]));
//		map.put("tags", tags.toArray(new String[tags.size()]));
//		return map;
		
	}
	
	public Map<String, Object> posTagging(String text) {
		// Map<words,tags>
		// Perform POS tagging on the given text and return two strings array in a map object.
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(text));
		Vector<String> tags = new Vector<String>();
		Vector<String> words = new Vector<String>();
		
		for (List<HasWord> sentence : sentences) {
			List<TaggedWord> tSentence = tagger.tagSentence(sentence);
			
			
			for (TaggedWord tw: tSentence) {
				words.add(tw.word());
				tags.add(tw.tag());
			}
		}
		
		map.put("words", words.toArray(new String[words.size()]));
		map.put("tags", tags.toArray(new String[tags.size()]));
		
		return map;
	}
	
	public Map<String, Object> nerClassify(String text) {
		// run NER classifier and return two strings array in a map object.
		// key:	"type" = an array of NER types of the entity.
		// 		"entity" = an array of named entities reported by the classifier.
		List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(text);
		Map<String, Object> map = new HashMap<String, Object>();
		Vector<String> type = new Vector<String>();
		Vector<String> entity = new Vector<String>();
		
		for (Triple<String,Integer,Integer> trip : triples) {
	
			type.add(trip.first());
			entity.add(text.substring(trip.second(), trip.third()));
			
		}
		map.put("type", type.toArray(new String[type.size()]));
		map.put("entity", entity.toArray(new String[entity.size()]));
		return map;
	}
	

	
	public void printTags(Vector<String> words, Vector<String> tags) {
		
		System.out.println(Arrays.toString(words.toArray()));
		System.out.println(Arrays.toString(tags.toArray()));
		
	}
}
