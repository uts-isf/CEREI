package au.org.nifpi.cerei;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.nio.file.Path;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * A simple menu bar on the top of the energy calculator
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class MenuBar extends JMenuBar implements ActionListener {
	
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Link back to the overall UI so this can be added. */
	private JFrame UI;
	
	/**
	 * Create a menu bar for the Energy calcualtor.
	 * 
	 * @param UI Link back to the overall UI.
	 */
	public MenuBar(JFrame UI) {
		this.UI = UI;
		
		// Create Menus
		JMenu helpMenu = new JMenu("Help");
		JMenu aboutMenu = new JMenu("About");
		
		// Create Menu items.  At this stage there is only one item in each menu
		JMenuItem showHelp = new JMenuItem("Show Help");
		showHelp.setMnemonic(KeyEvent.VK_H);
		showHelp.setActionCommand("Help");
		showHelp.addActionListener(this);
		
		JMenuItem showVersion= new JMenuItem("Version");
		showVersion.setMnemonic(KeyEvent.VK_V);
		showVersion.setActionCommand("Version");
		showVersion.addActionListener(this);
		
		// Add the menu items to the menu
		helpMenu.add(showHelp);
		aboutMenu.add(showVersion);
		
		add(Box.createHorizontalGlue());
		add(helpMenu);
		// Asked to remove the About Menu item from the menubar.
		//add(aboutMenu);
		
	}
	
	@Override
	/**
	 * Display the relevant information when the menu item is clicked.
	 * 
	 * @param e Menu item click.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Help") {
			
			URI uri = Path.of(System.getProperty("user.dir")+"/help/CEREI.htm").toUri();
			
			try {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					Desktop.getDesktop().browse(uri);
				}
			} catch(Exception exception) {
				JOptionPane.showMessageDialog(this, exception.getMessage(), "Error Loading Help File",
						JOptionPane.ERROR_MESSAGE);

			}
		}
		else if (e.getActionCommand() == "Version") {
			AboutDialog aboutDialog = new AboutDialog(UI);
	        aboutDialog.setVisible(true);
		}
	}

}
