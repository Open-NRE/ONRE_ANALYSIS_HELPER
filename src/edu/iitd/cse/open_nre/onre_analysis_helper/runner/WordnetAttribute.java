/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.tools.JavaFileObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

/**
 * @author swarna
 *
 */
public class WordnetAttribute {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		//construct URL to WordNet Dictionary directory on the computer
        String wordNetDirectory = "/home/harinder/Documents/IITD_MTP/swarna/WordNet-3.0";
        String path = wordNetDirectory + File.separator + "dict";
        URL url = new URL("file", null, path);      

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();
        
        Map<String, Set<String> > wordToAttributeListMap = new HashMap<String, Set<String> >();
        
        Iterator<ISynset> it = dict.getSynsetIterator(POS.NOUN);
        while(it.hasNext()){
            ISynset synset = it.next();
            
            List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.ATTRIBUTE);
            Set<String> attributeList = new HashSet<>();
            
            for(ISynsetID id : hypernyms) {
            	List<IWord> attributedIWords = dict.getSynset(id).getWords();
            	for(IWord attributedIWord : attributedIWords) {
            		attributeList.add(attributedIWord.getLemma());
            	}
            }
            
            List<IWord> keyIWords = synset.getWords();
            for(IWord keyIWord : keyIWords) {
            	if(!wordToAttributeListMap.containsKey(keyIWord.getLemma())) {
            		wordToAttributeListMap.put(keyIWord.getLemma(), attributeList);
            	}
            	else {
            		Set<String> attributes = wordToAttributeListMap.get(keyIWord.getLemma());
            		attributes.addAll(attributeList);
            		wordToAttributeListMap.put(keyIWord.getLemma(), attributes);
            	}
            }
        }
        
        Iterator<Entry<String, Set<String>>> iter = wordToAttributeListMap.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
            @SuppressWarnings("unchecked")
			Set<String> value = (Set<String>) entry.getValue();
            if(value.isEmpty()) {
            	iter.remove();
            }
        }
        
        Gson gson = new GsonBuilder().create();
        System.out.println(gson.toJson(wordToAttributeListMap));

	}

}
