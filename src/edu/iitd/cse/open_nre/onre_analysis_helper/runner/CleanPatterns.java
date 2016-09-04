/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_WordNet;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_pattern;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre_ds.helper.Onre_dsHelper;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * @author swarna
 *
 */
public class CleanPatterns {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputPatternFile = args[0];
		String outputPatternFile = args[1];
		
		Map<String, Set<String>> stemmedWordToInflectedWordsMap = new HashMap<String, Set<String>>();
		
		String wordNetDirectory = "/home/harinder/Documents/IITD_MTP/swarna/WordNet-3.0";
        String path = wordNetDirectory + File.separator + "dict";
        URL url = new URL("file", null, path);      

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();
        
        WordnetStemmer stemmer = new WordnetStemmer(dict);
		
		getInflectedWords(stemmer, stemmedWordToInflectedWordsMap);
		
		//dict.close();
		
		List<String> patterns = OnreIO.readFile(inputPatternFile);
		List<String> outputPatterns = new ArrayList<String>();
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
		
		for(String pattern : patterns) {
			pattern = replaceWithInflectedWords(stemmer, pattern, stemmedWordToInflectedWordsMap, q_yetToExpand);
			outputPatterns.add(pattern);
		}
		dict.close();		
		OnreIO.writeFile(outputPatternFile, outputPatterns);
	}
	
	private static String replaceWithInflectedWords(WordnetStemmer stemmer, String pattern, Map<String, Set<String>> stemmedWordToInflectedWordsMap, Queue<OnrePatternNode> q_yetToExpand) throws IOException {
		OnrePatternNode patternNode = OnreHelper_pattern.convertPattern2PatternTree(pattern);
		
		q_yetToExpand.add(patternNode);
		while(!q_yetToExpand.isEmpty()) {
			OnrePatternNode currNode = q_yetToExpand.remove();
			if(currNode.dependencyLabel.equals("prep")) continue;
			
			// replace with inflected words of the current word
			String stemmedWord = null;
			List<String> stemmedWords = stemmer.findStems(currNode.word, POS.VERB);
			if(stemmedWords.size() == 0) {
				stemmedWords = stemmer.findStems(currNode.word, POS.NOUN);
				if(stemmedWords.size() == 0) {
					stemmedWords = stemmer.findStems(currNode.word, POS.ADJECTIVE);
					if(stemmedWords.size() == 0) {
						stemmedWords = stemmer.findStems(currNode.word, POS.ADVERB);
					}
				}
			}
			
			for (int i = 0; i < stemmedWords.size(); i++) {
	            if(!stemmedWords.equals(currNode.word)) {
	            	stemmedWord = stemmedWords.get(i);
	            }
	        }
			if(stemmedWord == null) stemmedWord = currNode.word;
			
			if(stemmedWordToInflectedWordsMap.containsKey(stemmedWord)) {
				String inflectedWords = stemmedWordToInflectedWordsMap.get(stemmedWord).toString();
				inflectedWords = inflectedWords.replaceAll(", ", "|");
				inflectedWords = inflectedWords.substring(1, inflectedWords.length()-1);
				currNode.word = inflectedWords;
			}
			
			for(OnrePatternNode child : currNode.children) {
				q_yetToExpand.add(child);
			}
		}
		
		// make the pattern
		StringBuilder sb_pattern = new StringBuilder();
		sb_pattern.append("<");
		Onre_dsHelper.makePattern_helper(patternNode, sb_pattern, true);
		sb_pattern.append(">");
		pattern = sb_pattern.toString();
		
		
		pattern = pattern.trim().toLowerCase();
		return pattern;
		
	}
	
	private static void getInflectedWords(WordnetStemmer stemmer, Map<String, Set<String>> stemmedWordToInflectedWordsMap) throws IOException {
        
        // store all inflected words
        List<String> englishWords = OnreIO.readFile_classPath("words");
		
		for(String word : englishWords) {
			// try out stems with all part of speeches
        	List<String> stemmedWords = stemmer.findStems(word, POS.VERB);
        	if(stemmedWords.size() == 0) {
        		stemmedWords = stemmer.findStems(word, POS.NOUN);
        		if(stemmedWords.size() == 0) {
        			stemmedWords = stemmer.findStems(word, POS.ADJECTIVE);
        			if(stemmedWords.size() == 0) {
        				stemmedWords = stemmer.findStems(word, POS.ADVERB);
        			}
        		}
        	}
        	
        	// for the stemmedWord add the word
        	for(String stemmedWord : stemmedWords) {
        		if(stemmedWordToInflectedWordsMap.containsKey(stemmedWord)) {
        			Set<String> unstemmedWords = stemmedWordToInflectedWordsMap.get(stemmedWord);
        			unstemmedWords.add(word);
        			stemmedWordToInflectedWordsMap.put(stemmedWord, unstemmedWords);
        		}
        		else {
        			Set<String> unstemmedWords = new HashSet<String>();
        			unstemmedWords.add(word);
        			stemmedWordToInflectedWordsMap.put(stemmedWord, unstemmedWords);
        		}
        	}
        }
	}

}
