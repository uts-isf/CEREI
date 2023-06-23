package au.org.nifpi.cerei;

import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import static javax.swing.GroupLayout.Alignment.CENTER;

/**
 * Creates the "About" dialog from the menu.
 * Heavily modified from (probably) https://zetcode.com/javaswing/swingdialogs/ - or similar.
 * 
 * @author James Sargeant
 */
public class AboutDialog extends JDialog {
	
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Text that appears in the title of the About modal*/
	private static final String ABOUT_TEXT = "About Energy Calculator";

	/** Current version number*/
	private static final String VERSION = "1.0.0.0";
	/** Cost-Effective Renewable Energy Investments Calculator author*/
	private static final String AUTHOR = "James Sargeant";
	/** Cost-Effective Renewable Energy Investments Calculator author's email*/
	private static final String AUTHOR_EMAIL = "James.Sargeant@live.com.au";
	/** Cost-Effective Renewable Energy Investments Calculator copyright owner*/
	private static final String COPYRIGHT_NOTICE = "Copyright (c) National Institute for Forest Products Innovation 2023";
	
	/**
	 * Creates the About Dialog.
	 * 
	 * @param UI Top level frame where the About Dialog is positioned. 
	 */
	public AboutDialog(JFrame UI) {
		JLabel versionLabel = new JLabel("Version: "+VERSION);
		versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

		JLabel authorLabel = new JLabel("Author: "+ AUTHOR +" (" + AUTHOR_EMAIL +")");
		authorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

		JLabel copyrightNotice = new JLabel(COPYRIGHT_NOTICE);
		copyrightNotice.setFont(new Font("SansSerif", Font.PLAIN, 14));

		var okBtn = new JButton("OK");
		okBtn.addActionListener(event -> dispose());

		createLayout(versionLabel, authorLabel, copyrightNotice, okBtn);

		setModalityType(ModalityType.APPLICATION_MODAL);

		setTitle(ABOUT_TEXT);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(UI);
	}
	
	/**
	 * Nicely space the components of the dialog box.
	 * TO-DO.  Generalise for any number of components - currently hard-coded for 4 components. 
	 * 
	 * @param arg Components to add to the About dialog.  
	 */
    private void createLayout(JComponent... arg) {

        var pane = getContentPane();
        var gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup(CENTER)
                .addComponent(arg[0])
                .addComponent(arg[1])
                .addComponent(arg[2])
                .addComponent(arg[3])
                .addGap(200)
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(30)
                .addComponent(arg[0])
                .addGap(20)
                .addComponent(arg[1])
                .addGap(20)
                .addComponent(arg[2])
                .addGap(30)
                .addComponent(arg[3])
                .addGap(30)
        );

        pack();
    }
	
}
