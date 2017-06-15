package james.sugden.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import james.sugden.editor.WindowPreferences;

public class ResourceBrowser extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	/**The highest directory in the directory hierarchy the user can navigate to*/
	private File rootDir;
	
	/**The currently selected directory*/
	private File dirSelected;
	
	/**The tree used to browse the directories*/
	private JTree treeFileBrowser;
	
	/**The panel to the right of the tree which displays buttons for the currently selected directory and the toolbar*/
	private JPanel pnlRight;
	
	/**Construct the resource browser, creates an instance of file for the root directory and sets up swing components*/
	public ResourceBrowser(String rootDir)
	{
		this.rootDir = new File(rootDir);
		this.setUpComponents();
	}
	
	/**Sets up swing components - tree and buttons*/
	private final void setUpComponents()
	{
		super.setLayout(new BorderLayout());
		
		pnlRight = new JPanel(new BorderLayout());
		
		JPanel pnlButtons = new JPanel(new WrapLayout(WrapLayout.LEFT));
		pnlButtons.setBackground(WindowPreferences.getClrBackground());
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Resource Browser");
		findAllFiles(rootDir, rootNode);
		treeFileBrowser = new JTree(rootNode);
		
		createToolBar(pnlButtons);				//Creates a toolbar for the button panel
		createButtonPanel(rootDir, pnlButtons);	//Adds buttons the button panel
		
		
		treeFileBrowser.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)	//Monitors when the user selects a node
			{
				if(e != null)
				{
					if(e.getNewLeadSelectionPath() != null)
					{
						File file = getFilePath((DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent());
						if(file.isDirectory())
							createButtonPanel(file, pnlButtons);
						else if(file.isFile())
							createButtonPanel(file.getParentFile(), pnlButtons);
					}
				}
			}
		});
		
		
		JScrollPane sclTree = new JScrollPane(treeFileBrowser);
		JScrollPane sclPanelButtons = new JScrollPane(pnlButtons);
		
		sclTree.setPreferredSize(new Dimension(250, 300));
		sclTree.setMinimumSize(new Dimension(250, 300));
		
		pnlRight.add(sclPanelButtons, BorderLayout.CENTER);
		
		super.add(sclTree, BorderLayout.WEST);
		super.add(pnlRight, BorderLayout.CENTER);
	}
	
	/**Recursive method which loop through all sub dirs and adds all files to the tree*/
	private final void findAllFiles(File dir, DefaultMutableTreeNode parentNode)
	{
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())	//Add a node on the tree for this directory and recurse this method to add all sub directories
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
				parentNode.add(node);
				findAllFiles(file, node);
			}
			else if(file.isFile())	//Add a node on the tree for this directory
			{
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
				parentNode.add(node);
			}
		}
	}
	
	/**Creates the button panel for the selected directory
	 * Assumes the file passed is a directory*/
	private final void createButtonPanel(File dir, JPanel pnlButtons)
	{
		pnlButtons.removeAll();
		
		dirSelected = dir;
		
		MouseListener lstOpenDir = new MouseAdapter()	//Mouse listener for opening a new directory
		{
			public void mousePressed(MouseEvent e)
			{
				JButton button = (JButton)e.getSource();
				createButtonPanel(new File(dir.getAbsolutePath() + "/" + button.getText()), pnlButtons);
			}
		};
		
		for(File file : dir.listFiles())
		{
			JButton btnNode = new JButton(file.getName());
			
			btnNode.setPreferredSize(new Dimension(100, 100));
			
			Image imgDir = new ImageIcon("engine_res/images/icon_dir.png").getImage();
			ImageIcon icoDirScaled = new ImageIcon(Utils.scaleImg(80, imgDir));
			Image imgAudio = new ImageIcon("engine_res/images/icon_audio.png").getImage();
			ImageIcon icoAudioScaled = new ImageIcon(Utils.scaleImg(80, imgAudio));
			Image imgScript = new ImageIcon("engine_res/images/icon_script.png").getImage();
			ImageIcon icoScriptScaled = new ImageIcon(Utils.scaleImg(80, imgScript));
			
			if(file.isFile())	//Enable dragging and dropping for files such as images and sounds
			{
				TransferHandler transfer = new TransferHandler("text");
				btnNode.setTransferHandler(transfer);
				
				JLabel lblSubString = new JLabel("");	//The directory which the resource belongs to - e.g. images, sounds
				
				if(file.getPath().endsWith(".png"))	//Set the icon of the button to the image
				{
					Image imgNode = new ImageIcon(file.getPath()).getImage();
					ImageIcon icoScaled = new ImageIcon(Utils.scaleImg(80, imgNode));
					btnNode.setIcon(icoScaled);
					btnNode.setHorizontalTextPosition(JButton.CENTER);
					btnNode.setVerticalTextPosition(JButton.NORTH);
					lblSubString.setText("images");
				} else if(file.getPath().endsWith(".wav"))	//Set the icon of the button to the default audio icon
				{
					btnNode.setIcon(icoAudioScaled);
					btnNode.setHorizontalTextPosition(JButton.CENTER);
					btnNode.setVerticalTextPosition(JButton.NORTH);
					lblSubString.setText("sounds");
				} else if(file.getPath().endsWith(".java"))	//Set the icon of the button to the default script icon
				{
					btnNode.setIcon(icoScriptScaled);
					btnNode.setHorizontalTextPosition(JButton.CENTER);
					btnNode.setVerticalTextPosition(JButton.NORTH);
					lblSubString.setText("scripts");
				}
				
				//TODO - Add in drops being received in game object viewer list and prefabs
				btnNode.addMouseListener(new MouseAdapter()	//Drag and dropping of file buttons mouse listener
				{
					public void mousePressed(MouseEvent e)
					{
						JLabel lblTransfer = new JLabel(file.getAbsolutePath().substring(rootDir.getAbsolutePath().length()+1).substring(lblSubString.getText().length()+1).replaceAll("\\\\", "/"));
						lblTransfer.setTransferHandler(new TransferHandler("text"));
						TransferHandler handle = btnNode.getTransferHandler();
						handle.exportAsDrag(lblTransfer, e, TransferHandler.COPY);
					}
				});	//Enable dragging and dropping of the button by adding the dnd mouse listener
			} else if(file.isDirectory())	//Enable switching of directory with button clicks
			{
				btnNode.setIcon(icoDirScaled);
				btnNode.setHorizontalTextPosition(JButton.CENTER);
				btnNode.setVerticalTextPosition(JButton.NORTH);
				btnNode.addMouseListener(lstOpenDir);
			}
			
			pnlButtons.add(btnNode);
		}
		
		pnlButtons.revalidate();
		pnlButtons.repaint();
	}
	
	/**Adds new nodes to the resource browser*/
	public final void updateBrowser(String path)
	{
		//TODO - Implement tree and panel being updated when new files imported
		//TODO - Implement tree opening when buttons are pressed
	}
	
	/**Creates a tool bar and add it to the top of the buttons panel*/
	private final void createToolBar(JPanel pnlButtons)
	{
		JToolBar toolbar = new JToolBar();
		JButton btnUp = new JButton("Up");
		JButton btnRoot = new JButton("Root Dir");
		JButton btnCreateDir = new JButton("New Dir");
		
		btnUp.addActionListener(e ->
		{
			if(dirSelected != null && !dirSelected.equals(rootDir))
			{
				createButtonPanel(new File(dirSelected.getParent()), pnlButtons);
			}
		});
		
		btnRoot.addActionListener(e ->	//Sets the directory of the file browser to the root directory
		{
			createButtonPanel(rootDir, pnlButtons);
		});
		
		btnCreateDir.addActionListener(e ->
		{
			if(!dirSelected.equals(rootDir))
			{
				String name = JOptionPane.showInputDialog(this, "Enter a name for the new directory", "new_dir");
				File dirNew = new File(dirSelected.getAbsolutePath() + "/" + name);
				
				if(!dirNew.exists())
				{
					dirNew.mkdir();
					createButtonPanel(dirSelected, pnlButtons);
				} else
				{
					JOptionPane.showMessageDialog(this, "The name entered is already in use", "Error: Name In Use", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		toolbar.add(btnUp);
		toolbar.addSeparator();
		toolbar.add(btnRoot);
		toolbar.addSeparator();
		toolbar.add(btnCreateDir);
		
		toolbar.setFloatable(false);
		
		pnlRight.add(toolbar, BorderLayout.NORTH);
	}
	
	/**Turns a tree path into a file path and returns it as a file object*/
	private File getFilePath(DefaultMutableTreeNode treeNode)
	{
		if(treeNode != null)
		{
			TreeNode[] nodes = treeNode.getPath();
			
			String filePath = rootDir.getAbsolutePath();
			
			for(int i = 1; i < nodes.length; i++)	//i starts at 1 because the first node is always "Resource Manager"
			{
				filePath += "/" + nodes[i].toString();
			}
			
			return new File(filePath);
		} else
		{
			return null;
		}
	}
}
