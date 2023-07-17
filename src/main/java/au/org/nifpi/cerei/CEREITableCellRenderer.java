package au.org.nifpi.cerei;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A class for rendering Result tables in the form specified by the client.
 * Cell Renderer that makes sure that all rows with a "Year" of 20xx, all rows with Quarter of "Qx" and all columns that contain the word "total" (in any case
 * are bolded.
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class CEREITableCellRenderer extends DefaultTableCellRenderer {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/**
	 * Parent constructor
	 */
	public  CEREITableCellRenderer() {
		super();
	}
	
	/**
	 * Format the individual cell as required by the client
	 * 
	 * @param table the JTable
	 * @param value the value to assign to the cell at [row, column]
	 * @param isSelected true if cell is selected
	 * @param hasFocus true if cell has focus
	 * @param row the row of the cell to render
	 * @param column the column of the cell to render
	 * 
	 * @return the specific table cell renderer 
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	         int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// Bold the entire row if there is an entry in column 0 (year column) that contains "20".  Breaks at year 2100.  Avoids "bolding" of columns in Lifecycle Cost Assessment tab.
		if (table.getModel().getValueAt(row, 0).toString().contains("20")) {
			c.setFont(c.getFont().deriveFont(Font.BOLD));
		}
		// Bold the entire row if there is an entry in column 1 (quarter column) that contains a "Q".  Avoids "bolding" of columns in Lifecycle Cost Assessment tab.
		if (table.getModel().getValueAt(row, 1).toString().contains("Q")) {
			c.setFont(c.getFont().deriveFont(Font.BOLD));
		}
		
		// Bold the entire column if the column heading contains the string "total" in any case
		if (table.getColumnName(column).toLowerCase().contains("total")) {
			c.setFont(c.getFont().deriveFont(Font.BOLD));
		}

		return c;	    
	}

}
