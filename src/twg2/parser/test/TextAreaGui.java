package twg2.parser.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import twg2.collections.primitiveCollections.IntArrayList;
import twg2.parser.documentParser.block.IntermediateBlock;
import twg2.parser.documentParser.block.ParseBlocks;
import twg2.parser.documentParser.block.TextOffsetBlock;

/**
 * @author TeamworkGuy2
 * @since 2014-12-13
 */
public class TextAreaGui {
	private int WINDOW_WIDTH = 800;
	private int WINDOW_HEIGHT = 600;
	private int TEXTAREA_WIDTH = 1100;
	private int TEXTAREA_HEIGHT = 800;
	private JFrame mainFrame;
	private JPanel mainPanel; // Panel reference to hold components
	private BasicMenuBar menuBar; // The class which builds and returns the window's menu bar
	private JTextPane textArea;
	private Document doc;


	public TextAreaGui() {
		mainFrame = new JFrame();
		// Set the title bar text.
		mainFrame.setTitle("Text Area Parsing");
		// Set the size of the window.
		mainFrame.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Specify an action for the close button.

		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) { closingAction(); }
		});

		// Create and set the menu bar as well as the parser.main content pane
		// Create the menu bar program
		this.menuBar = new BasicMenuBar();

		SwingUtilities.invokeLater(() -> {
			// Build the menu bard and set it as the frame's menu bar
			mainFrame.setJMenuBar( menuBar.getMenuBar() );
			// Builds the entire parser.main panel, including buttons, labels, and scroll panes, as well as assigning listeners
			mainFrame.add( buildPanel() );
			// Display the window.
			mainFrame.pack(); // If size is not explicitly set using setSize or setBounds
			mainFrame.setVisible(true); // Make the window visible
		});

	}


	private JPanel buildPanel() {
		JPanel panel = new JPanel();
		//CaretListener textAreaCaretListener = new TextAreaCaretListener();
		TextAreaDocListener textAreaDocListener = new TextAreaDocListener();

		textArea = new JTextPane();
		doc = textArea.getDocument();
		doc.addDocumentListener(textAreaDocListener);
		JScrollPane textAreaScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textAreaScroll.setPreferredSize(new Dimension(TEXTAREA_WIDTH, TEXTAREA_HEIGHT));
		textAreaScroll.setViewportView(textArea);
		JPanel textPanel = new JPanel();
		textPanel.add(textAreaScroll);

		panel.add(textPanel);

		return panel;
	}


	private void closingAction() {
		// window closing
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2014-12-13
	 */
	public class TextAreaCaretListener implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
		}

	}


	/**
	 * @author TeamworkGuy2
	 * @since 2014-12-13
	 */
	public class TextAreaDocListener implements DocumentListener {

		@Override
		public void removeUpdate(DocumentEvent e) {
			try {
				Document doc = e.getDocument();
				System.out.println("remove update: " + doc.getLength() + ": " + e);
				String docText;
				docText = doc.getText(0, doc.getLength());
				IntermediateBlock blocks = new IntermediateBlock();
				IntArrayList points = new IntArrayList();
				ParseBlocks.splitIntoBlocks(blocks, docText, '{', '}', points);
				doc.remove(0, doc.getLength());
				blocks.forEachLeaf((textBlock, depth) -> {
					TextBlockElement elem = new TextBlockElement((AbstractDocument)doc, null, (TextOffsetBlock)textBlock);
					elem.addAttribute("fontColor", Color.GREEN);
					try {
						doc.insertString(doc.getLength(), textBlock.getText(docText), elem);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				});
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			Document doc = e.getDocument();
			System.out.println("insert update: " + doc.getLength() + ": " + e);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			Document doc = e.getDocument();
			System.out.println("change update: " + doc.getLength() + ": " + e);
		}

	}


	/**
	 * @author TeamworkGuy2
	 * @since 2014-12-13
	 */
	public static class BasicMenuBar {
		// Menu items needed for creating menu
		JMenuBar menuBar; // The menu bar
		JMenu menu; // A menu
		JMenuItem  settingsMenuItem; // A menu item 
		JMenuItem programInfoMenuItem; // Information about the program menu button


		/** Create this program's menu bar
		 */
		public BasicMenuBar() {
			ActionListener menuListener = new MenuBarItemListener(); // The listener to listen for menu actions

			//Create the menu bar.
			menuBar = new JMenuBar();

			//Build the first menu.
			menu = new JMenu("Main Menu");
			menu.setMnemonic(KeyEvent.VK_A);
			menu.getAccessibleContext().setAccessibleDescription("Main menu access description");
			menuBar.add(menu);

			// Settings Menu item
			settingsMenuItem = new JMenuItem("Settings");
			settingsMenuItem.setMnemonic(KeyEvent.VK_T);
			settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
			settingsMenuItem.addActionListener( menuListener ); // Add custom menu listener
			menu.add(settingsMenuItem); // Add the newly configured menu item to the menu

			menu.addSeparator(); // Horizontal menu seperator bar

			programInfoMenuItem = new JMenuItem("Application Information");
			programInfoMenuItem.setMnemonic(KeyEvent.VK_I);
			programInfoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
			programInfoMenuItem.addActionListener( menuListener ); // Add custom menu listener
			menu.add(programInfoMenuItem); // Add the newly configured menu item to the menu
		}


		/** getMenuBar
		 * @return the menu bar created by the constructor, multiple calls will return the same menu bar
		 */
		public JMenuBar getMenuBar() {
			return menuBar; // Return the created menu bar
		}


		/** MenuBarItemListener class for catching menu item events
		 */
		private class MenuBarItemListener implements ActionListener, ItemListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem source = (JMenuItem)(e.getSource());

				System.out.println("Action performed");

				if(source == programInfoMenuItem) {
					JOptionPane.showMessageDialog(null,
							"Text parsing test. Specifically parsing programming language formatted text.\n" +
							"\n" +
							"Developer: TeamworkGuy2\n",
							"Program Information", JOptionPane.INFORMATION_MESSAGE);
				}
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				JMenuItem source = (JMenuItem)(e.getSource());

				System.out.println("item state change");

				if(e.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("Menu Checkbox Selected\n");
				}
				else if(e.getStateChange() == ItemEvent.DESELECTED) {
					System.out.println("Menu Checkbox Deselected\n");
				}
			}
		}
	}


	public static void main(String[] args) {
		TextAreaGui window = new TextAreaGui();
	}

}
