/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IVerbFrame;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * @author swarna
 *
 */
public class WordnetSynonymsInflectionsList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		//construct URL to WordNet Dictionary directory on the computer
        String wordNetDirectory = "/home/harinder/Documents/IITD_MTP/swarna/WordNet-3.0";
        String path = wordNetDirectory + File.separator + "dict";
        URL url = new URL("file", null, path);      

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();
        
        Map<String, Set<String>> stemmedWordToInflectedWordsMap = new HashMap<String, Set<String>>();
        Map<String, Set<String>> stemmedWordToSynonymsMap = new HashMap<String, Set<String>>();
        
        WordnetStemmer stemmer = new WordnetStemmer(dict);
        
        getInflectedWords(stemmedWordToInflectedWordsMap, stemmer);
        getSynonyms(stemmedWordToSynonymsMap, stemmer, dict);
        
        dict.close();
        
        String inflectedWordsFile = args[0];
        String synonymsFile = args[1];
        writeToFile(stemmedWordToInflectedWordsMap, stemmedWordToSynonymsMap, inflectedWordsFile, synonymsFile);
	}
	
	private static void getInflectedWords(Map<String, Set<String>> stemmedWordToInflectedWordsMap, WordnetStemmer stemmer) throws IOException {
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
	
	private static void getSynonyms(Map<String, Set<String>> stemmedWordToSynonymsMap, 
			WordnetStemmer stemmer, IDictionary dict) throws IOException {
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
	
	@SuppressWarnings("resource")
	private static void writeToFile(Map<String, Set<String>> stemmedWordToInflectedWordsMap, 
			Map<String, Set<String>> stemmedWordToSynonymsMap, String inflectedWordsFile, String synonymsFile) throws FileNotFoundException {
		
		PrintWriter pw = new PrintWriter(inflectedWordsFile);
		
        Iterator<Entry<String, Set<String>>> iter = stemmedWordToInflectedWordsMap.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
            @SuppressWarnings("unchecked")
			Set<String> value = (Set<String>) entry.getValue();
            if(value.isEmpty()) {
            	iter.remove();
            }
            pw.println((String)entry.getKey()+" " +value.toString());
        }
        
        pw = new PrintWriter(synonymsFile);
        
        iter = stemmedWordToSynonymsMap.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
            @SuppressWarnings("unchecked")
			Set<String> value = (Set<String>) entry.getValue();
            if(value.isEmpty()) {
            	iter.remove();
            }
            pw.println((String)entry.getKey()+" " +value.toString());
        }
        
        pw.close();
	}

}
