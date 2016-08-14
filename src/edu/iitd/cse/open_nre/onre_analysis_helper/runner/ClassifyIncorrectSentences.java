package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.iitd.cse.open_nre.onre.utils.OnreIO;

public class ClassifyIncorrectSentences {
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String inputFile = args[0];
		
		List<String> lines = OnreIO.readFile(inputFile);
		Map<Integer, Integer> correctlyClassified = new HashMap<>();
		Map<Integer, Integer> incorrectlyClassified = new HashMap<>();
		Map<Integer, Integer> patternCounts = new HashMap<>();
		Map<Integer, Double> precisionMap = new TreeMap<>();
		
		int lineCount = 0;
		
		while(lineCount < lines.size()-1) {
			String line = lines.get(lineCount);
			String []words = line.split(" ");
			if(words.length == 1 && !words[0].contains("::")) {
				Integer patternNumber = Integer.valueOf(words[0]);
				
				++lineCount;
				String nextLine = lines.get(lineCount); // get the next line
				if(!patternCounts.containsKey(patternNumber)) {
					patternCounts.put(patternNumber, 1);
				}
				else {
					patternCounts.put(patternNumber, patternCounts.get(patternNumber)+1);
				}
				words = nextLine.split("===");
				
				//populateMaps(correctlyClassified, incorrectlyClassified, patternNumber, words[words.length-1]);
			}
			++lineCount;
		}
		
		//buildPrecisionMap(correctlyClassified, incorrectlyClassified, precisionMap);
		
		String outputFile = args[1];
		writeCountsToFile(outputFile, patternCounts, correctlyClassified, incorrectlyClassified, precisionMap);
	}
	
	private static void buildPrecisionMap(Map<Integer, Integer> correctlyClassified, Map<Integer, Integer> incorrectlyClassified, 
			Map<Integer, Double> precisionMap) {
		
		for(Integer patternNumber : correctlyClassified.keySet()) {
			Integer correctCount = correctlyClassified.get(patternNumber);
			Integer incorrectCount = 0;
			
			if(incorrectlyClassified.containsKey(patternNumber)) {
				incorrectCount = incorrectlyClassified.get(patternNumber);
			}
			
			Double precision = (Double.valueOf(correctCount)/(Double.valueOf(correctCount) + 
					Double.valueOf(incorrectCount)));
			
			precisionMap.put(patternNumber, precision);
		}
		
		for(Integer patternNumber : incorrectlyClassified.keySet()) {
			if(precisionMap.containsKey(patternNumber)) continue;
			
			Integer incorrectCount = incorrectlyClassified.get(patternNumber);
			Integer correctCount = 0;
			
			if(correctlyClassified.containsKey(patternNumber)) {
				correctCount = correctlyClassified.get(patternNumber);
			}
			
			Double precision = (Double.valueOf(correctCount)/(Double.valueOf(correctCount) + 
					Double.valueOf(incorrectCount)));
			precisionMap.put(patternNumber, precision);
		}
	}
	
	private static void populateMaps(Map<Integer, Integer> correctlyClassified, 
			Map<Integer, Integer> incorrectlyClassified, Integer patternNumber, String annotation) {
		
		annotation = annotation.trim();
		
		if(annotation.equals("T")) {
			if(correctlyClassified.containsKey(patternNumber)) {
				Integer correctCount = correctlyClassified.get(patternNumber);
				correctlyClassified.put(patternNumber, correctCount+1);
			} 
			else {
				correctlyClassified.put(patternNumber, 1);
			}
		} 
		else if(annotation.equals("F")) {
			if(incorrectlyClassified.containsKey(patternNumber)) {
				Integer incorrectCount = incorrectlyClassified.get(patternNumber);
				incorrectlyClassified.put(patternNumber, incorrectCount+1);
			} 
			else {
				incorrectlyClassified.put(patternNumber, 1);
			}
		}
		else {
			System.out.println("Something wrong happened");
			System.exit(0);
		}
	}
	
	private static void writeCountsToFile(String outputFile, Map<Integer, Integer> patternCounts, Map<Integer, Integer> correctlyClassified, 
			Map<Integer, Integer> incorrectlyClassified, Map<Integer, Double> precisionMap) throws FileNotFoundException {
		
		PrintWriter pw = new PrintWriter(outputFile);
		
		Integer cumulativeCorrectCount = 0, cumulativeIncorrectCount = 0;
		Double cumulativePrecision;
		
		for(Integer patternNumber : precisionMap.keySet()) {
			Integer correctCount = 0;
			Integer incorrectCount = 0;
			
			if(incorrectlyClassified.containsKey(patternNumber)) {
				incorrectCount = incorrectlyClassified.get(patternNumber);
			}
			
			if(correctlyClassified.containsKey(patternNumber)) {
				correctCount = correctlyClassified.get(patternNumber);
			}
			
			Double precision = precisionMap.get(patternNumber);
			
			cumulativeCorrectCount += correctCount;
			cumulativeIncorrectCount += incorrectCount;
			cumulativePrecision = (Double.valueOf(cumulativeCorrectCount)/(Double.valueOf(cumulativeCorrectCount) + 
					Double.valueOf(cumulativeIncorrectCount)));
			
			pw.println(patternNumber + ";" + correctCount + ";" + incorrectCount + ";" + precision
					+ ";" + cumulativeCorrectCount + ";" + cumulativeIncorrectCount + ";" + cumulativePrecision);
		}
		
		for(Integer patternNumber : patternCounts.keySet()) {
			
			Integer count = patternCounts.get(patternNumber);
			
			pw.println(patternNumber + ";" + count);
		}
		pw.close();
	}

}
