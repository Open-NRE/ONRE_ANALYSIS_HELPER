/**
 * 
 */
package edu.iitd.cse.open_nre.onre_analysis_helper.runner;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import catalog.QuantityCatalog;
import catalog.Unit;
import eval.UnitExtractor;
import edu.stanford.nlp.util.HashableCoreMap;

/**
 * @author swarna
 *
 */
public class getQuantityUnitMap {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String outputFile = args[0];
		
		UnitExtractor ue = new UnitExtractor();
		QuantityCatalog dict = ue.quantDict;

		List<Unit> units = new ArrayList<Unit>();

		Map<String, Set<String>> QuantityToUnitsMap = new HashMap<String, Set<String>>();

		for (int i = 0; i < dict.idToUnitMap.size(); i++) {
			units.add(dict.idToUnitMap.get(i));
		}

		for (Unit unit : units) {
			String quantity = unit.getParentQuantity().getConcept();
			if(QuantityToUnitsMap.containsKey(quantity)) {
				Set<String> tempUnits = QuantityToUnitsMap.get(quantity);
				if(unit.getParentQuantity() == null) continue;
				if(unit.getParentQuantity().getCanonicalUnit() == null) continue;
				
				// add all possible units of parent quantity
				for(Unit tempUnit : unit.getParentQuantity().getUnits()) {
					tempUnits.add(tempUnit.getBaseName());
				}
				
				// add base symbols
				for(String symbol : unit.getBaseSymbols()) {
					tempUnits.add(symbol);
				}
				QuantityToUnitsMap.put(quantity, tempUnits);
			}
			else {
				Set<String> tempUnits = new HashSet<String>();
				if(unit.getParentQuantity() == null) continue;
				if(unit.getParentQuantity().getCanonicalUnit() == null) continue;
				
				// add all possible units of parent quantity
				for(Unit tempUnit : unit.getParentQuantity().getUnits()) {
					tempUnits.add(tempUnit.getBaseName());
				}
				
				// add base symbols
				for(String symbol : unit.getBaseSymbols()) {
					tempUnits.add(symbol);
				}
				QuantityToUnitsMap.put(quantity, tempUnits);
				QuantityToUnitsMap.put(quantity, tempUnits);
			}
		}
		
		PrintWriter pw = new PrintWriter(outputFile);
		for(String quantity : QuantityToUnitsMap.keySet()) {
			pw.println(quantity + " " + QuantityToUnitsMap.get(quantity));
		}
	}

}
