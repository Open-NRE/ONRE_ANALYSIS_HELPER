/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.iitd.cse.open_nre.onre.utils.OnreIO;
import edu.iitd.cse.open_nre.onre.utils.OnreUtils_number;

/**
 * @author swarna
 *
 */
public class RemoveExtractionsWithoutQuantities {
	
	public static void main(String args[]) throws IOException, ClassNotFoundException {
		String inputFile = args[0];
		String outputFile = args[1];
		
		List<String> lines = OnreIO.readFile(inputFile);
		List<String> outputLines = new ArrayList<String>();;
		
		int lineCount = 0;
		
		while(lineCount < lines.size()-1) {
			String line = lines.get(lineCount);
			
			line = lines.get(lineCount);
			if(line.contains("Context")) {
				lineCount++;
				continue;
			}
			
			String []words = line.split(" \\(");
			if(words.length == 1) {
				outputLines.add("\n");
				outputLines.add(line);
				lineCount++;
				continue;
			}
			
			String extraction = words[1];
			
			String []extractionParts = extraction.split(";");
			String lastExtractionPart = extractionParts[extractionParts.length - 1];
			
			words = lastExtractionPart.split(" ");
			
			for(String word : words) {
				if(OnreUtils_number.isNumber(word)) {
					outputLines.add(line);
					break;
				}
			}
			lineCount++;
		}
		
		PrintWriter pw = new PrintWriter(outputFile);
		for(String line : outputLines) {
			pw.println(line);
		}
		pw.close();
	}

}
