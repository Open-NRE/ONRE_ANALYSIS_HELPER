/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import edu.iitd.cse.open_nre.onre.constants.OnreFilePaths;
import edu.iitd.cse.open_nre.onre.helper.OnreHelper_WordNet;
import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_number;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_string;

/**
 * @author swarna
 *
 */
public class cleanFacts {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		String inputFile = args[0];
		String outputFile = args[1];
		
		List<String> yagoRelations = OnreIO.readFile_classPath(OnreFilePaths.filePath_yagoRelationsList);
		
		List<String> englishWords = getEnglishWords();
		
		List<String> pronouns = OnreIO.readFile_classPath(OnreFilePaths.filePath_pronounsList);
		
		List<String> lines = OnreIO.readFile(inputFile);
		List<String> outLines = new ArrayList<String>();
		
		int i=0;
		
		for(String line : lines) {
			
			if(line.charAt(0) != '(') continue;
			else {
				String fact = line.substring(1, line.length() - 1);
				
				if(isValidFact(fact, pronouns, englishWords, yagoRelations)) {
					outLines.add(line);
				}
			}
			
			i++;
			if(i%100000 == 0) System.out.println(i);
		}
		
		PrintWriter pw = new PrintWriter(outputFile);
		for(String line : outLines) {
			line = line.replace("(NNP)", "");
			line = line.replace("(NNPS)", "");
			pw.println(line);
			addFactForDerivationallyRelatedWord(pw,line);
		}
		pw.close();
		
		/*pw = new PrintWriter(outputFile+"temp");
		for(String line : outLines) {
			line = line.replace("(NNP)", "");
			line = line.replace("(NNPS)", "");
			String []factParts = line.split(";");
			if(!OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(factParts[4].trim(), factParts[1].trim())) {
				pw.println(line);
			}
		}
		pw.close();*/
	}
	
	private static void addFactForDerivationallyRelatedWord(PrintWriter pw, String line) throws IOException {
		String fact = line.substring(1, line.length() - 1);
		String []factParts = fact.split(";");
		if(!OnreUtils_string.isIgnoreCaseIgnoreCommaIgnoreSpaceContains(factParts[4].trim(), factParts[1].trim())) {
			String derivedWord = OnreHelper_WordNet.getWhoseAttributeIsWord(factParts[1].trim());
			
			if(derivedWord == null) {
				derivedWord = OnreHelper_WordNet.getDerivationallyRelatedNounWord(factParts[1].trim(), 1);
				if(derivedWord == null) {
					derivedWord = OnreHelper_WordNet.getDerivationallyRelatedNounWord(factParts[1].trim(), 2);
				}
				if(derivedWord == null) {
					return;
				}
			}
			
			if(derivedWord.equals("duration")) {
				line = line.replace(factParts[1], " length ");
			}
			else {
				line = line.replace(factParts[1], " "+derivedWord+" ");
			}
			pw.println(line);
		}
	}
	
	/*private static Set<String> getYagoNumericalRelations() throws FileNotFoundException {
		List<String[]> yagoFacts = getYagoFacts();
		Set<String> yagoRelations = new HashSet<String>();
		for(String []fact : yagoFacts) {
			String relation = fact[2].substring(1, fact[2].length()-1);
			String value = fact[4];
			if(value != null && OnreUtils_number.isNumber(value)) {
				yagoRelations.add(relation);
			}
			else {
				value = fact[3].substring(1, fact[3].length()-1);
				if(value != null && OnreUtils_number.isNumber(value)) {
					yagoRelations.add(relation);
				}
			}
		}
		for(String relation : yagoRelations)
		System.out.println(relation);
		
		return yagoRelations;
	}*/
	
	private static boolean isValidFact(String fact, List<String> pronouns, List<String> englishWords, List<String> yagoRelations) throws ClassNotFoundException, IOException {
		String []factParts = fact.split(";");
		
		if(!isArgProperNoun(factParts[0])) {
			return false;
		}
		
		factParts[0] = factParts[0].replace("(NNP)", "").replace("(NNPS)", "");
		if(pronouns.contains(factParts[0].toLowerCase().trim())) {
			return false;
		}
		
		for(int i=0; i<factParts.length-2; i++) {
			for(int j=i+1; j<factParts.length-1; j++)
			{
				if(factParts[i].toLowerCase().trim().equals(factParts[j].toLowerCase().trim()))
				{
					return false;
				}
			}
		}
		
		if(!englishWords.contains(factParts[1].trim().toLowerCase())) {
			return false;
		}
		
		if(!isValidUnit(factParts[3])) {
			return false;
		}
		
		if(!isFactInYagoRelations(yagoRelations, fact)) {
			return false;
		}
		
		return true;
	}
	
	private static boolean isFactInYagoRelations(List<String> yagoRelations, String fact) {
		for(String yagoRelation : yagoRelations) {
			if(OnreUtils_string.isIgnoreCaseContainsPhrase(fact, yagoRelation)) {
				return true;
			}
		}
		return false;
	}
	
	private static List<String> getEnglishWords() throws IOException {
		List<String> englishWords = OnreIO.readFile_classPath("words"), lowerEnglishWords = new ArrayList<String>();
		for(int i=0; i<englishWords.size(); i++) {
			String lowerWord = englishWords.get(i).toLowerCase();
			lowerEnglishWords.add(lowerWord);
		}
		
		return englishWords;
	}
	
	private static boolean isValidUnit(String unit) {
		if(unit.isEmpty()) return true;
		
		for(int i=0; i<unit.length(); i++) {
			if((unit.charAt(i) >= '0' && unit.charAt(i) <= '9' ) || unit.charAt(i) == '.')
				return false;
		}
		return true;
	}
	
	private static boolean isArgProperNoun(String arg) {
		return arg.contains("NNP");
	}
	
	private static List<String[]> getYagoFacts() throws FileNotFoundException {
		TsvParserSettings settings = new TsvParserSettings();
		TsvParser parser = new TsvParser(settings);

		// parses all rows in one go.
		List<String[]> allRows = parser.parseAll(new FileReader("/home/harinder/Documents/IITD_MTP/Open_nre/ONRE_ANALYSIS_HELPER/src/data/yagoLiteralFacts.tsv"));
		return allRows;
	}

}
