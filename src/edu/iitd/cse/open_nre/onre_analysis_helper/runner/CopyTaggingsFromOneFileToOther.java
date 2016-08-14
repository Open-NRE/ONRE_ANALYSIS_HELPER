/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iitd.cse.open_nre.onre.utils.OnreIO;

/**
 * @author swarna
 *
 */
public class CopyTaggingsFromOneFileToOther {
	
	private static void classifyExtractions(List<String> lines1, List<String> lines2, 
			List<String> correctExtractions, List<String> incorrectExtractions,
			Map<String, Boolean> extractionsSecondFile) {
		
		for(String line : lines1) {
			if(line.equals(lines1.get(lines1.size()-1))) continue;
			
			String []words = line.split("===");
			
			if(words.length == 1) continue;
			
			String tag = words[words.length-1].trim();
			
			if(tag.equals("T")) {
				correctExtractions.add(words[0].trim());
			}
			else if(tag.equals("F")) {
				incorrectExtractions.add(words[0].trim());
			}
			else {
				System.out.println("Something wrong happened\n");
				System.exit(1);
			}
		}
		
		for(String line : lines2) {
			if(line.equals(lines2.get(lines2.size()-1))) continue;
			
			String []words = line.split("===");
			
			if(words.length == 1) continue;
			
			extractionsSecondFile.put(words[0].trim(), true);
			
		}
	}
	
	public static List<String> modifySecondFile(List<String> lines2, List<String> correctExtractions, List<String> incorrectExtractions,
			Map<String, Boolean> extractionsSecondFile) {
		
		List<String> output = new ArrayList<String>();
		
		for(String line : correctExtractions) {
			if(!extractionsSecondFile.containsKey(line)) {
				extractionsSecondFile.remove(line);
			}
		}
		
		for(String line : incorrectExtractions) {
			if(!extractionsSecondFile.containsKey(line)) {
				extractionsSecondFile.remove(line);
			}
			else {
				extractionsSecondFile.put(line, false);
			}
		}
		
		for(String line : lines2) {
			if(line.equals(lines2.get(lines2.size()-1))) continue;
			
			String []words = line.split("===");
			
			if(words.length == 1) {
				if(words[0].contains("::")) output.add("\n");
				output.add(line);
				continue;
			}
			
			Boolean tag = extractionsSecondFile.get(words[0].trim());
			
			if(tag == true) {
				output.add(words[0] + "=== T");
			}
			else {
				output.add(words[0] + "=== F");
			}
		}
		
		return output;
	}
	
	public static void main(String args[]) throws IOException, ClassNotFoundException {
		String inputFile1 = args[0];
		String inputFile2 = args[1];
		String outputFile = args[2];
		
		List<String> lines1 = OnreIO.readFile(inputFile1);
		List<String> lines2 = OnreIO.readFile(inputFile2);
		
		List<String> correctExtractions = new ArrayList<String>();
		List<String> incorrectExtractions = new ArrayList<String>();
		Map<String, Boolean> extractionsSecondFile = new HashMap<>();
		
		classifyExtractions(lines1, lines2, correctExtractions, incorrectExtractions, extractionsSecondFile);
		
		List<String> output = modifySecondFile(lines2, correctExtractions, incorrectExtractions, extractionsSecondFile);
		
		PrintWriter pw = new PrintWriter(outputFile);
		for(String line : output) {
			pw.println(line);
		}
		pw.close();
		
	}

}
