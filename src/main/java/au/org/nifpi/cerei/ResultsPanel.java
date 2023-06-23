package au.org.nifpi.cerei;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Panel that holds all the results of the EnergyCalculator calculations - added as a tab in the JTabbedPane in the gui.
 * 
 * @author James Sargeant
 */
public class ResultsPanel extends JPanel {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely. */
	private static final long serialVersionUID = 2023062301L;

	/** Appears as the tab name. */
	public String resultPanelName;
	/** Text that appears just above the top of the table. */
	public JLabel resultPanelText;
	/** The table that contains the results of this type of calculation. */
	public JTable resultTable;
	/** A panel to hold various buttons - typically buttons to save results to a .csv file. */
	public JPanel resultButtonPanel;

	/**
	 * Create the results panel.
	 * TO DO - Fix sizes of panel and its components so that it always looks nice during resizing.
	 * 
	 * @param panelName - The name of the panel - appears as the tab name.
	 */
	public ResultsPanel(String panelName) {
		this.resultPanelName = panelName;
		this.setLayout(new BorderLayout());
		this.setPreferredSize(
				new Dimension(CostEffectiveRenewableEnergyInvestments.CALCULATOR_WIDTH - 25, CostEffectiveRenewableEnergyInvestments.CALCULATOR_HEIGHT - 370));

		resultPanelText = new JLabel("Please select files", SwingConstants.CENTER);

		JPanel separatorPanel = new JPanel();

		separatorPanel.setPreferredSize(new Dimension(CostEffectiveRenewableEnergyInvestments.CALCULATOR_WIDTH - 25, 10));

		// JScrollPane topResultPane = new JScrollPane();
		JScrollPane resultPane = new JScrollPane();

		// JPanel tablePanel = new JPanel(new BorderLayout());

		resultTable = new JTable();
		resultTable.setDefaultRenderer(Object.class, new CEREITableCellRenderer());
		// topResultPane.setViewportView(resultTopTable);
		resultPane.setViewportView(resultTable);

		// tablePanel.add(resultTopTable, BorderLayout.NORTH);
		// tablePanel.add(separatorPanel, BorderLayout.CENTER);
		// tablePanel.add(resultPane, BorderLayout.SOUTH);

		resultButtonPanel = new JPanel();
		this.add(resultPanelText, BorderLayout.NORTH);
		this.add(resultPane, BorderLayout.CENTER);
		this.add(resultButtonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Resize column widths so the summaries look nice! From
	 * https://www.tabnine.com/code/java/methods/javax.swing.JTable/setAutoResizeMode
	 */
	public void formatResultTable() {
		int columnWidth;

		// We want scrollbars if the table exceeds the size of the window.
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Nice heading for table
		resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		((DefaultTableCellRenderer) resultTable.getTableHeader().getDefaultRenderer())
				.setHorizontalAlignment(JLabel.CENTER);

		// Recalculate column sizes
		resultTable.doLayout();

		for (int column = 0; column < resultTable.getColumnCount(); column++) {
			// Set the minimum width of the column to the width of the header.
			columnWidth = (int)resultTable.getTableHeader().getHeaderRect(column).getWidth();
			int columnWidth2 = resultTable.getTableHeader().getColumnModel().getColumn(column).getPreferredWidth();
			columnWidth = Math.max(columnWidth, columnWidth2);
			// Get the Column
			TableColumn tableColumn = resultTable.getColumnModel().getColumn(column);

			// Cell by Cell in the column.
			for (int row = 0; row < resultTable.getRowCount(); row++) {
				TableCellRenderer cellRenderer = resultTable.getCellRenderer(row, column);
				Component c = resultTable.prepareRenderer(cellRenderer, row, column);
				int width = c.getMinimumSize().width + resultTable.getIntercellSpacing().width;
				// Update the column width if this width is larger than the previous value.
				columnWidth = Math.max(columnWidth, width);
			}

			// Update the width of the column.
			tableColumn.setPreferredWidth(columnWidth);
		}

		// Align values to the right of the cells.
		DefaultTableCellRenderer rightRenderer = (DefaultTableCellRenderer) resultTable.getDefaultRenderer(getClass());
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

	}

	/**
	 * Zero out the Panel so that it is empty.
	 */
	public void clearResultTable() {
		this.resultPanelText.setText("Please select files");
		this.resultTable.setModel(new CEREITableModel());
	}

}
