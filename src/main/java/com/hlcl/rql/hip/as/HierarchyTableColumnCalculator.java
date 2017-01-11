package com.hlcl.rql.hip.as;

/**
 * @author lejafr
 *
 * This class offers calculation for a logical column consisting of 10 physical columns in 
 * a HTML table. It decide depending on an ordered list of deepness information,
 * the colspan and rowspan needed.
 */
public class HierarchyTableColumnCalculator {

	// always <= 0
	private int[] deepnesses;
	private int maxPhysicalColumns;

	/**
	 * Constructor
	 * 
	 *@param	deepnesses	the array with the deepness from page structure <= 0 
	 *@param	maxPhysicalColumns	the smallest value in deepnesses
	 */
	public HierarchyTableColumnCalculator(int[] deepnesses, int maxPhysicalColumns) {
		super();
		this.deepnesses = deepnesses;
		this.maxPhysicalColumns = maxPhysicalColumns;
	}
	/** 
	 * Liefert für die gegebenen Zeile zurück, ob ein zusätzliches TD mit rowspan erforderlich ist.
	 * =0 bedeutet, dass kein TD Tag erforderlich ist. 
	 */
	public int getRowspan(int rowIndex) {
	
		// never in first row
		if (rowIndex <= 0) {
			return 0;
		}
	
		// decide if current deepness is smaller than in row before = diving deeper
		int startRowDeepness = getDeepness(rowIndex); 
		if (startRowDeepness < getDeepness(rowIndex - 1)) {
			// default
			int rowspan = 1;
		
			// find end of rowspan
			int searchIndex = rowIndex+1;
			while (searchIndex < size() && getDeepness(searchIndex) <= startRowDeepness) {
				rowspan ++;
				searchIndex ++;
			}
			return rowspan;		
		}
		// stay at same deepness
		return 0;
	}
	/** 
	 * Liefert für die gegebenen Zeile zurück, welches colspan gesetzt werden muss.
	 * 1 <= return < maxPhysicalColumns
	 */
	public int getColspan(int rowIndex) {

		return maxPhysicalColumns + getDeepness(rowIndex);
	}
	/** 
	 * Liefert den Wert deepness für die gegebenen Zeile zurück.
	 */
	private int getDeepness(int rowIndex) {

		return deepnesses[rowIndex];
	}
	/** 
	 * Liefert die max Anzahl von Zeilen.
	 */
	public int size() {

		return deepnesses.length;
	}
}
