package au.org.nifpi.cerei;

import javax.swing.table.DefaultTableModel;

/**
 * Extension of the Default Table Model so Cells cannot be edited.
 * 
 * @author James Sargeant
 */
public class CEREITableModel extends DefaultTableModel {
	
 	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;
	
	/**
	 * Parent constructor
	 */
	public  CEREITableModel() {
		super();
	}

	/**
	 * Parent constructor
	 * 
	 * @param data table body
	 * @param cols table column headings
	 */
	public CEREITableModel(String[][] data, String[] cols) {
		super(data, cols);
	}
	
	/**
	 * Disable editing of all cells in the JTable.
	 * 
	 * @param row row index.
	 * @param column column index.
	 * 
	 * @return always false.
	 */
	@Override
	public boolean isCellEditable(int row, int column){  
        return false;  
    }
}
