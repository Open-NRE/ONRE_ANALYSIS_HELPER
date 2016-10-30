/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.iitd.cse.open_nre.onre.domain.OnrePatternNode;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_pattern;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre_ds.helper.Onre_dsHelper;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
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
		Map<String, Set<String>> stemmedWordToSynonymsMap = new HashMap<String, Set<String>>();
		
		String wordNetDirectory = "/home/harinder/Documents/IITD_MTP/swarna/WordNet-3.0";
        String path = wordNetDirectory + File.separator + "dict";
        URL url = new URL("file", null, path);      

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();
        
        WordnetStemmer stemmer = new WordnetStemmer(dict);
		
		getInflectedWords(stemmer, stemmedWordToInflectedWordsMap);
		getSynonyms(dict, stemmer, stemmedWordToSynonymsMap);
				
		List<String> patterns = OnreIO.readFile(inputPatternFile);
		List<String> outputPatterns = new ArrayList<String>();
		
		for(String pattern : patterns) {
			pattern = replaceWithInflectedWords(stemmer, pattern, stemmedWordToInflectedWordsMap);
			pattern = replaceWithSynonyms(stemmer, pattern, stemmedWordToSynonymsMap, stemmedWordToInflectedWordsMap);
			outputPatterns.add(pattern);
		}
		dict.close();		
		OnreIO.writeFile(outputPatternFile, outputPatterns);
	}
	
	private static String replaceWithSynonyms(WordnetStemmer stemmer, String pattern, Map<String, Set<String>> stemmedWordToSynonymsMap,
			Map<String, Set<String>> stemmedWordToInflectedWordsMap) {
		OnrePatternNode patternNode = OnreHelper_pattern.convertPattern2PatternTree(pattern);
		
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
		q_yetToExpand.add(patternNode);
		while(!q_yetToExpand.isEmpty()) {
			OnrePatternNode currNode = q_yetToExpand.remove();
			
			for(OnrePatternNode child : currNode.children) {
				q_yetToExpand.add(child);
			}
			
			if(currNode.word.equals("am|is|are|was|were|been|be") || currNode.word.equals("been|be|are|were|was|is|am") || currNode.word.equals("has|have|had|having")) {
				continue;
			}
			if(currNode.dependencyLabel.equals("prep")) continue;
			if(currNode.word.equals("{rel}") || currNode.word.equals("{arg}") || currNode.word.equals("{quantity}")) continue;
			
			String []words = currNode.word.split("\\|");
			Set<String> newWords = new HashSet<String>();
			String stemmedWord = null;
			for(String word : words) {
				if(stemmedWordToInflectedWordsMap.containsKey(word)) {
					stemmedWord = word;
				}
				newWords.add(word);
			}
			if(words.length == 1) {
				stemmedWord = words[0];
			}
			
			if(stemmedWord == null) {
				continue;
			}
			
			// get the synonyms
			if(!stemmedWordToSynonymsMap.containsKey(stemmedWord)) {
				continue;
			}
			Set<String> synonyms = stemmedWordToSynonymsMap.get(stemmedWord);
			
			for(String synonym : synonyms) {
				newWords.add(synonym); // try adding inflected words of synonyms as well
				Set<String> inflectedWordsOfSyns = stemmedWordToInflectedWordsMap.get(synonym);
				if(inflectedWordsOfSyns != null) {
					for(String inflectedWord : inflectedWordsOfSyns) {
						newWords.add(inflectedWord);
					}
				}
			}
			
			String appendedWord = "";
			int count = 0;
			for(String word : newWords) {
				appendedWord += word;
				if(count < newWords.size()-1) {
					appendedWord += "|";
				}
				count++;
			}
			
			currNode.word = appendedWord;
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
	
	private static String replaceWithInflectedWords(WordnetStemmer stemmer, String pattern, Map<String, Set<String>> stemmedWordToInflectedWordsMap) throws IOException {
		OnrePatternNode patternNode = OnreHelper_pattern.convertPattern2PatternTree(pattern);
		
		Queue<OnrePatternNode> q_yetToExpand = new LinkedList<OnrePatternNode>();
		q_yetToExpand.add(patternNode);
		while(!q_yetToExpand.isEmpty()) {
			OnrePatternNode currNode = q_yetToExpand.remove();
			for(OnrePatternNode child : currNode.children) {
				q_yetToExpand.add(child);
			}
			
			if(currNode.dependencyLabel.equals("prep")) continue;
			
			if(currNode.word.equals("is|are|was|were|been|be")) {
				currNode.word = "am|is|are|was|were|been|be";
			}
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
	
	private static void getSynonyms(IDictionary dict, WordnetStemmer stemmer, Map<String, Set<String>> stemmedWordToSynonymsMap) throws IOException {
		List<String> englishWords = OnreIO.readFile_classPath("words");
		
		for(String word : englishWords) {
        	IIndexWord idxWord = dict.getIndexWord(word, POS.NOUN);
        	if(idxWord == null) {
        		idxWord = dict.getIndexWord(word, POS.VERB);
        		if(idxWord == null) {
        			idxWord = dict.getIndexWord(word, POS.ADJECTIVE);
        			if(idxWord == null) {
        				idxWord = dict.getIndexWord(word, POS.ADVERB);
        				if(idxWord == null) {
        					continue;
        				}
        			}
        		}
        	}
        	
        	List<IWordID> wordIDs = idxWord.getWordIDs();
        	Set<String> synonyms = new HashSet<String>();
        	
        	for(IWordID wordID : wordIDs) {
        		IWord iword = dict.getWord(wordID);
        		
        		ISynset synset = iword.getSynset();
        		if(synset == null) continue;
                for (IWord w : synset.getWords()) {
                    synonyms.add(w.getLemma());
                }
                stemmedWordToSynonymsMap.put(word, synonyms);
        	}
        }
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
		
		Set<String> beVerbs = new HashSet<String>(Arrays.asList("am", "is", "are", "was", "were", "been", "be"));
		stemmedWordToInflectedWordsMap.put("be", beVerbs);
	}

}
