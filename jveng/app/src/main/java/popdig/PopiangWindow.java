package popdig;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Insets;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.FileLocation;

import javax.imageio.ImageIO;

import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import java.io.*;
import java.nio.channels.FileChannel;
import javax.swing.UIManager.LookAndFeelInfo;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import org.fife.rsta.ui.*;
import org.fife.rsta.ui.search.*;
import javax.swing.text.BadLocationException;
import java.awt.Toolkit;
import org.fife.ui.rtextarea.ToolTipSupplier;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import java.io.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.*;
import org.apache.jena.query.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import java.util.HashMap;
import org.fife.ui.rsyntaxtextarea.folding.FoldManager;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import java.util.regex.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import java.util.*;
import java.util.stream.*;

public class PopiangWindow implements SearchListener, ToolTipSupplier {

	static Logger log = Logger.getLogger(PopiangWindow.class);

	private Desktop desktop;
	private FileSystemView fileSystemView;

	private File currentFile;

	private JPanel gui;

	private JTree tree;
	private DefaultTreeModel treeModel;

	JTabbedPane tabbedPane;
	Hashtable<String,TextFileInfo> hTabPane = new Hashtable<>();

	private ListSelectionListener listSelectionListener;
	private boolean cellSizesSet = false;
	private int rowIconPadding = 6;

	private JButton openFile;
	private JButton printFile;
	private JButton editFile;

	private JLabel fileName;
	private JTextField path;
	private JLabel date;
	private JLabel size;
	private JCheckBox readable;
	private JCheckBox writable;
	private JCheckBox executable;
	private JRadioButton isDirectory;
	private JRadioButton isFile;

	private JPanel newFilePanel;
	private JRadioButton newTypeFile;
	private JTextField name;
	private CollapsibleSectionPanel csp;
	private FindDialog findDialog;
	private ReplaceDialog replaceDialog;
	private FindToolBar findToolBar;
	private ReplaceToolBar replaceToolBar;
	private StatusBar statusBar;
	File ownDir = null;
	DefaultMutableTreeNode ownNode = null;
	DefaultMutableTreeNode selectedNode = null;
	DefaultMutableTreeNode root = null;

	static File curDir;

	HashMap<KeyStroke, Action> actionMap = new HashMap<KeyStroke, Action>();

	public Container getGui() {
        if (gui==null) {

			initSearchDialogs();

			//JPanel contentPane = new JPanel(new BorderLayout());
			//setContentPane(contentPane);
			csp = new CollapsibleSectionPanel();
			//contentPane.add(csp);

			gui = new JPanel(new BorderLayout());
			fileSystemView = FileSystemView.getFileSystemView();
			desktop = Desktop.getDesktop();

			f.setJMenuBar(createMenuBar());

			root = new DefaultMutableTreeNode();
//			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			treeModel = new DefaultTreeModel(root);

			TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent tse){
					selectedNode = (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
					showChildren(selectedNode);
				}
			};

			populateAllTreeNode();

			tree = new JTree(treeModel);
			tree.setRootVisible(false);
			tree.addTreeSelectionListener(treeSelectionListener);
			tree.setCellRenderer(new FileTreeCellRenderer());
			JScrollPane treeScroll = new JScrollPane(tree);

            // as per trashgod tip
			tree.setVisibleRowCount(15);

			Dimension preferredSize = treeScroll.getPreferredSize();
			Dimension widePreferred = new Dimension( 120, (int)preferredSize.getHeight());
			treeScroll.setPreferredSize( widePreferred );


			tabbedPane = new JTabbedPane();
            tabbedPane.setBorder(new EmptyBorder(0,0,0,0));
			tabbedPane.setPreferredSize(new Dimension(300,200));

			String week[]= { 
				"job1"
				,"job2"
			}; 
			JList jl = new JList(week); 
			jl.setPreferredSize(new Dimension(100,100));

            gui.add(treeScroll, BorderLayout.WEST);
            gui.add(tabbedPane, BorderLayout.CENTER);
            gui.add(jl, BorderLayout.EAST);

			statusBar = new StatusBar();
			gui.add(statusBar, BorderLayout.SOUTH);

			// action key
			int ctrl = getToolkit().getMenuShortcutKeyMask();
			int shift = InputEvent.SHIFT_MASK;

			KeyStroke keyS = KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl);
			actionMap.put(keyS, new AbstractAction("actionS") { @Override
				public void actionPerformed(ActionEvent e) { saveFile(); } });
			KeyStroke key1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, ctrl);
			actionMap.put(key1, new AbstractAction("action1") { @Override
				public void actionPerformed(ActionEvent e) { genDoc1(); } });
			KeyStroke keyI = KeyStroke.getKeyStroke(KeyEvent.VK_I, ctrl);
			actionMap.put(keyI, new AbstractAction("actionI") { @Override
				public void actionPerformed(ActionEvent e) { newId(); } });
			KeyStroke keyM = KeyStroke.getKeyStroke(KeyEvent.VK_M, ctrl);
			actionMap.put(keyM, new AbstractAction("actionM") { @Override
				public void actionPerformed(ActionEvent e) { PopiangUtil.readAllModel(); } });
			KeyStroke keyK = KeyStroke.getKeyStroke(KeyEvent.VK_K, ctrl);
			actionMap.put(keyK, new AbstractAction("actionK") { @Override
				public void actionPerformed(ActionEvent e) { keyShowAction(); } });
			KeyStroke keyPl = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, ctrl);
			actionMap.put(keyPl, new AbstractAction("actionPl") { @Override
				public void actionPerformed(ActionEvent e) { addRdfFileAction(); } });
			KeyStroke keyN = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ctrl);
			actionMap.put(keyN, new AbstractAction("actionN") { @Override
				public void actionPerformed(ActionEvent e) { delRdfFileAction(); } });
			KeyStroke keyW = KeyStroke.getKeyStroke(KeyEvent.VK_W, ctrl);
			actionMap.put(keyW, new AbstractAction("actionW") { @Override
				public void actionPerformed(ActionEvent e) { clsRdfFileAction(); } });
			KeyStroke keyO = KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl);
			actionMap.put(keyO, new AbstractAction("actionO") { @Override
				public void actionPerformed(ActionEvent e) { foldAll(); } });
			KeyStroke keyR = KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl);
			actionMap.put(keyR, new AbstractAction("actionR") { @Override
				public void actionPerformed(ActionEvent e) { fixMisSpell(); } });
			KeyStroke keyP = KeyStroke.getKeyStroke(KeyEvent.VK_P, ctrl);
			actionMap.put(keyP, new AbstractAction("actionP") { @Override
				public void actionPerformed(ActionEvent e) { pushGit(); } });

			if(PopiangDigital.sNodeType==PopiangDigital.mainNode) {
				KeyStroke keyJ = KeyStroke.getKeyStroke(KeyEvent.VK_J, ctrl);
				actionMap.put(keyJ, new AbstractAction("actionJ") { @Override
					public void actionPerformed(ActionEvent e) { gitJoin(); } });
			}

			KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			kfm.addKeyEventDispatcher( new KeyEventDispatcher() {@Override
				public boolean dispatchKeyEvent(KeyEvent e) {
					KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
					if ( actionMap.containsKey(keyStroke) ) {
						final Action a = actionMap.get(keyStroke);
						final ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), null );
						a.actionPerformed(ae);
					}
					return false;
				}});
        }
        return gui;
    }

	void populateAllTreeNode() {
		root.removeAllChildren();
//		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
//		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		for (File infile : PopiangDigital.aRdfFold) {
			String nm = infile.getName();
			if(nm.startsWith(".")) continue;
			if(!infile.isDirectory()) continue;
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(infile);
			root.add( node );
			if(infile.getName().equals(PopiangDigital.sPrefix)) {
				ownDir = infile;
				ownNode = node;
			}
			populateTreeNode(node, infile);
		}
	}

	void populateTreeNode(DefaultMutableTreeNode node, File infile) {
		log.info("populate files");
		node.removeAllChildren();
		File[] files = fileSystemView.getFiles(infile, true);
		Arrays.sort(files, Collections.reverseOrder());
		for (File file : files) {
//			log.info(" file:" + file);
			node.add(new DefaultMutableTreeNode(file));
		}
	}

	private JMenuBar createMenuBar() {

		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("Search");
		menu.add(new JMenuItem(new ShowFindDialogAction()));
		menu.add(new JMenuItem(new ShowReplaceDialogAction()));
		menu.add(new JMenuItem(new GoToLineAction()));
		menu.addSeparator();

		int ctrl = getToolkit().getMenuShortcutKeyMask();
		int shift = InputEvent.SHIFT_MASK;
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrl|shift);
		Action a = csp.addBottomComponent(ks, findToolBar);
		a.putValue(Action.NAME, "Show Find Search Bar");
		menu.add(new JMenuItem(a));
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_H, ctrl|shift);
		a = csp.addBottomComponent(ks, replaceToolBar);
		a.putValue(Action.NAME, "Show Replace Search Bar");
		menu.add(new JMenuItem(a));

		mb.add(menu);

/*
		menu = new JMenu("LookAndFeel");
		ButtonGroup bg = new ButtonGroup();
		LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo info : infos) {
			addItem(new LookAndFeelAction(info), bg, menu);
		}
		mb.add(menu);
*/

		return mb;

	}

	public String getToolTipText(RTextArea textArea, MouseEvent e) {
		TurtleTextArea tta = (TurtleTextArea) textArea;
		Point at = e.getPoint();
		int off = textArea.viewToModel(at);
		return tta.getToolTipText(off);
	}

    public void showRootFile() {
        tree.setSelectionInterval(0,0);
    }

/*
    private TreePath findTreePath(File find) {
        for (int ii=0; ii<tree.getRowCount(); ii++) {
            TreePath treePath = tree.getPathForRow(ii);
            Object object = treePath.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)object;
            File nodeFile = (File)node.getUserObject();
System.out.println("findTreePath: "+ nodeFile);

            if (nodeFile==find) {
                return treePath;
            }
        }
        return null;
    }
*/

    private void showErrorMessage(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(
            gui,
            errorMessage,
            errorTitle,
            JOptionPane.ERROR_MESSAGE
            );
    }

    private void showThrowable(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(
            gui,
            t.toString(),
            t.getMessage(),
            JOptionPane.ERROR_MESSAGE
            );
        gui.repaint();
    }

	private void showChildren(final DefaultMutableTreeNode node) {
//		tree.setEnabled(false);

		SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
			@Override
			public Void doInBackground() {
				File file = (File) node.getUserObject();
				if (file.isDirectory()) {
/*
					//File[] files = fileSystemView.getFiles(file, true); //!!
					File[] files = fileSystemView.getFiles(file, false); //!!
System.out.println("show children 3:"+files.length+" : "+node.isLeaf());
					Arrays.sort(files);
					if (node.isLeaf()) {
						for (File child : files) {
System.out.println("show children 4:"+child);
							publish(child);
						}
					}
*/
				} else {
					String fn = file.getName();
					if(fn.endsWith(".ttl")) {
						try {
							int cnt = tabbedPane.getComponentCount();
							String d0 = curDir.getAbsolutePath();
							String d1 = file.getAbsolutePath();
							String tn = d1.substring(d0.length()+1);
							TextFileInfo tinf;
							if((tinf=hTabPane.get(tn))==null) {
								String fs = file.getAbsolutePath();
								Path fileName = Paths.get(fs);
								byte[] buf = Files.readAllBytes(fileName);
								String actual = new String(buf,"UTF-8");
								TurtleTextArea tta = new TurtleTextArea();
								tta.setText(actual);
								tta.setDirty(false);
								tta.setToolTipSupplier(PopiangWindow.this);
//								tta.setSelectedTextColor(Color.black);
								tta.setSelectedTextColor(Color.red);
								RTextScrollPane tasp = new RTextScrollPane(tta);
								tabbedPane.add(tasp, tn);
								int idx = tabbedPane.indexOfComponent(tasp);
								tabbedPane.setTabComponentAt(idx, getTitlePanel(tabbedPane, tasp, tn));
								tabbedPane.setSelectedIndex(idx);

								tinf = new TextFileInfo();
								tinf.text = tta;
								tinf.pane = tasp;
								tinf.path = fileName;
								hTabPane.put(tn, tinf);
							} else {
								int idx = tabbedPane.indexOfComponent(tinf.pane);
								tabbedPane.setSelectedIndex(idx);
							}
						} catch(Exception x) {}
					}
				}
   				return null;
			}

/*
			@Override
			protected void process(List<File> chunks) {
System.out.println("show children 5:");
				for (File child : chunks) {
					System.out.println(".."+child);
					node.add(new DefaultMutableTreeNode(child));
				}
			}
			@Override
			protected void done() {
System.out.println("show children 7:");
				tree.setEnabled(true);
			}
*/
        };
		worker.execute();
    }


	private JPanel getTitlePanel(final JTabbedPane tabbedPane, RTextScrollPane panel, String title) {
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePanel.setOpaque(false);
		JLabel titleLbl = new JLabel(title);
		titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		titlePanel.add(titleLbl);
		return titlePanel;
	}

	JFrame f;
	Toolkit getToolkit() {
		return f.getToolkit();
	}

	public void proc(File dir) {
		log.setLevel(Level.INFO);
		log.info("=========== PROC =========");

		curDir = dir;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
					UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
				} catch(Exception weTried) {
				}
				String title = PopiangDigital.sName
					+" ("+PopiangDigital.sPrefix+") "
					+PopiangDigital.sEmail;
				f = new JFrame(title);
//				f = new JFrame("THARISTREE.J (TRJ)");
				PopiangDigital.frame = f;
				//f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				f.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent we) { 
						checkToClose();
					}
				});

				f.setContentPane(getGui());

				try {
					URL urlBig = getClass().getResource("fb-icon-32x32.png");
					URL urlSmall = getClass().getResource("fb-icon-16x16.png");
					ArrayList<Image> images = new ArrayList<Image>();
					images.add( ImageIO.read(urlBig) );
					images.add( ImageIO.read(urlSmall) );
					f.setIconImages(images);
				} catch(Exception weTried) {}

				f.pack();
				f.setLocationByPlatform(true);
				f.setMinimumSize(f.getSize());
				f.setVisible(true);

				showRootFile();
			}
		});
	}

	void checkToClose() {
		int cnt = tabbedPane.getTabCount();
		int cDirt = 0;
		for(int i=0; i<cnt; i++) {
			String tt = tabbedPane.getTitleAt(i);
			TextFileInfo tfin = hTabPane.get(tt);
			if(tfin.text.isDirty()) cDirt++;
		}
		String ObjButtons[] = {"Yes","No"};
		int PromptResult = JOptionPane.showOptionDialog(null, 
			(cDirt==0? "" : cDirt + " files modified\n")+ 
			"Are you sure you want to exit?", 
			"Close Confirmation ", 
			JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, 
		ObjButtons,ObjButtons[1]);
		if(PromptResult==0) {
			System.exit(0);          
		}
	}

	private void initSearchDialogs() {

		findDialog = new FindDialog(f, this);
		replaceDialog = new ReplaceDialog(f, this);

		// This ties the properties of the two dialogs together (match case,
		// regex, etc.).
		SearchContext context = findDialog.getSearchContext();
		replaceDialog.setSearchContext(context);

		// Create tool bars and tie their search contexts together also.
		findToolBar = new FindToolBar(this);
		findToolBar.setSearchContext(context);
		replaceToolBar = new ReplaceToolBar(this);
		replaceToolBar.setSearchContext(context);

	}

	private class GoToLineAction extends AbstractAction {

		GoToLineAction() {
			super("Go To Line...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			//GoToDialog dialog = new GoToDialog(RSTAUIDemoApp.this);
			GoToDialog dialog = new GoToDialog(f);
			int idx = tabbedPane.getSelectedIndex();
			String tt = tabbedPane.getTitleAt(idx);
			TextFileInfo tfin = hTabPane.get(tt);
			dialog.setMaxLineNumberAllowed(tfin.text.getLineCount());
			dialog.setVisible(true);
			int line = dialog.getLineNumber();
			if (line>0) {
				try {
					tfin.text.setCaretPosition(tfin.text.getLineStartOffset(line-1));
				} catch (BadLocationException ble) { // Never happens
					UIManager.getLookAndFeel().provideErrorFeedback(tfin.text);
					ble.printStackTrace();
				}
			}
			} catch(Exception z) {}
		}

	}

	@Override
	public String getSelectedText() {
		try {
			int idx = tabbedPane.getSelectedIndex();
			String tt = tabbedPane.getTitleAt(idx);
			TextFileInfo tfin = hTabPane.get(tt);
			return tfin.text.getSelectedText();
		} catch(Exception z) { return null; }
	}

	private class ShowFindDialogAction extends AbstractAction {

		ShowFindDialogAction() {
			super("Find...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (replaceDialog.isVisible()) {
				replaceDialog.setVisible(false);
			}
			findDialog.setVisible(true);
		}

	}

	private class ShowReplaceDialogAction extends AbstractAction {

		ShowReplaceDialogAction() {
			super("Replace...");
			int c = getToolkit().getMenuShortcutKeyMask();
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, c));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (findDialog.isVisible()) {
				findDialog.setVisible(false);
			}
			replaceDialog.setVisible(true);
		}

	}

	JLabel label;

	void addRdfFileAction() {
			log.info("=========== ADD FILE =========: "+ownDir);
			if(ownDir==null) return;
			log.info("  OWN DIR: "+ ownDir.getAbsolutePath());
			String prx = PopiangDigital.sPrefix;
			int ln = prx.length();
			String mx = "00";
			for(String nm : ownDir.list()) {
				String ord = nm.substring(ln,ln+2);
				if(ord.compareTo(mx)>0) mx = ord;
				log.info("  "+nm+" : "+ ord);
			}
			String mx2 = nextMax(mx);
			TreePath path = new TreePath(ownNode.getPath());
			tree.expandPath(path);
			File rdf0 = new File(ownDir+"/"+prx+mx2+".ttl");
			String ObjButtons[] = {"Yes","No"};
			int PromptResult = JOptionPane.showOptionDialog(null, 
				"Do you want to add a new file "+ rdf0.getName()+" ?", 
				"Add File Confirmation ", 
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, 
			ObjButtons,ObjButtons[1]);
			if(PromptResult!=0) {
				return;
			}
			try {
				label.setText("Add file "+mx2);
				log.info("NEW FILE:"+rdf0.getAbsolutePath());
				String str = "// ";
				Path pth = Paths.get(rdf0.getAbsolutePath());
				byte[] strToBytes = str.getBytes();
				Files.write(pth, strToBytes);
			} catch(Exception z) {
			}
			//rdf0.setLastModified(System.currentTimeMillis());
			populateTreeNode(ownNode, ownDir);
			((DefaultTreeModel) tree.getModel()).reload();
			path = new TreePath(ownNode.getFirstLeaf().getPath());
			tree.setSelectionPath(path);
	}

	char[] cDigi = new String("01234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
	String nextMax(String old) {
		char[] nxt = old.toCharArray();
		boolean dg2 = true;
		for(char x : cDigi) {
			if(x>nxt[1]) {
				nxt[1] = x;
				dg2 = false;
				break;
			}
		}
		if(dg2) {
			for(char y: cDigi) {
				if(y>nxt[0]) {
					nxt[0] = y;
					nxt[1] = cDigi[0];
					dg2 = false;
					break;
				}
			}
		}
		return new String(nxt);
	}

	void delRdfFileAction() {
			int id = tabbedPane.getSelectedIndex();
			if(id<0) return;
			String ttl = tabbedPane.getTitleAt(id);
			TextFileInfo tinf = hTabPane.get(ttl);
			int dialogResult = JOptionPane.showConfirmDialog (null
				, "Delete File ","Warning",JOptionPane.YES_NO_OPTION);
			if(dialogResult == JOptionPane.YES_OPTION){
				String tx = tinf.text.getText();
				tinf.path.toFile().delete();
				hTabPane.remove(ttl);
				tabbedPane.remove(tinf.pane);
				populateTreeNode(ownNode, ownDir);
				((DefaultTreeModel) tree.getModel()).reload();
			TreePath path = new TreePath(ownNode.getPath());
			tree.expandPath(path);
				log.info("=========== DELETE FILE =========");
				label.setText("Del file");
			} else {
				return;
			}
	}

	void keyShowAction() {
			String dir = PopiangDigital.workDir;
			String key1 = dir+"/.cfg/id_rsa.pub";
			try {
				Path keypath1 = Paths.get(key1);

				String pub1 = new String(Files.readAllBytes(keypath1));
				log.info("show own key: "+pub1);

				String tn = "publickey";

				TurtleTextArea tta = new TurtleTextArea();
				tta.setEditable(false);
				tta.setText(pub1);
				tta.setDirty(false);
				RTextScrollPane tasp = new RTextScrollPane(tta);
				tabbedPane.add(tasp, tn);
				int idx = tabbedPane.indexOfComponent(tasp);
				tabbedPane.setTabComponentAt(idx, getTitlePanel(tabbedPane, tasp, tn));
				tabbedPane.setSelectedIndex(idx);

				TextFileInfo tinf = new TextFileInfo();
				tinf.text = tta;
				tinf.pane = tasp;
				tinf.path = keypath1;
				hTabPane.put(tn, tinf);

				StringSelection stringSelection = new StringSelection(pub1);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);

			} catch(Exception z) {
			}
	}

	void clsRdfFileAction() {
			int id = tabbedPane.getSelectedIndex();
			if(id<0) return;
			String ttl = tabbedPane.getTitleAt(id);
			TextFileInfo tinf = hTabPane.get(ttl);
			if(tinf.text.isDirty()) {
				int dialogResult = JOptionPane.showConfirmDialog (null, 
					"This doc has been modified\n"+
					"Do you want to save ?",
					"Modified",JOptionPane.YES_NO_OPTION);
				if(dialogResult == JOptionPane.YES_OPTION){
					String tx = tinf.text.getText();
					try {
						Files.write(tinf.path, tx.getBytes("UTF-8"));
					} catch(IOException x) {
					}
				}
			}
			hTabPane.remove(ttl);
			tabbedPane.remove(tinf.pane);
			log.info("=========== CLOSE =========");
			label.setText("Close");
	}
	class StatusBar extends JPanel {


		StatusBar() {
			label = new JLabel("Ready");
			label.setPreferredSize(new Dimension(100,20));
			setLayout(new FlowLayout(FlowLayout.LEFT));
			add(label);

			JButton btAdd = new JButton("+");
			add(btAdd);
			btAdd.setMargin(new Insets(0, 0, 0, 0));
			//btAdd.addActionListener(new AddRdfFileAction());
			btAdd.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { addRdfFileAction(); }});

			JButton btDel = new JButton("-");
			add(btDel);
			btDel.setMargin(new Insets(0, 0, 0, 0));
			btDel.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { delRdfFileAction(); }});

			JButton btSave = new JButton("save(s)");
			add(btSave);
			btSave.setMargin(new Insets(0, 0, 0, 0));
			btSave.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { saveFile(); }});

			JButton btMod = new JButton("model(m)");
			add(btMod);
			btMod.setMargin(new Insets(0, 0, 0, 0));
			btMod.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { PopiangUtil.readAllModel(); }});

			JButton btCls = new JButton("close(w)");
			add(btCls);
			btCls.setMargin(new Insets(0, 0, 0, 0));
			btCls.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { clsRdfFileAction(); }});

			JButton btKey = new JButton("key");
			add(btKey);
			btKey.setMargin(new Insets(0, 0, 0, 0));
			btKey.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { keyShowAction(); }});

			JButton btDoc1 = new JButton("doc1(1)");
			add(btDoc1);
			btDoc1.setMargin(new Insets(0, 0, 0, 0));
			btDoc1.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { genDoc1(); }});

			JButton btNew = new JButton("newid(i)");
			add(btNew);
			btNew.setMargin(new Insets(0, 0, 0, 0));
			btNew.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { newId(); }});

			JButton btFld = new JButton("fold(o)");
			add(btFld);
			btFld.setMargin(new Insets(0, 0, 0, 0));
			btFld.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { foldAll(); }});

			JButton btGit = new JButton("git(p)");
			add(btGit);
			btGit.setMargin(new Insets(0, 0, 0, 0));
			btGit.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { pushGit(); }});

			JButton btEmp = new JButton("emp(e)");
			add(btEmp);
			btEmp.setMargin(new Insets(0, 0, 0, 0));
			btEmp.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { addEmpRdf(); }});

			JButton btAtt = new JButton("att(t)");
			add(btAtt);
			btAtt.setMargin(new Insets(0, 0, 0, 0));
			btAtt.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { PopiangUtil.attendReset(); }});

			JButton btRep2 = new JButton("repo(2)");
			add(btRep2);
			btRep2.setMargin(new Insets(0, 0, 0, 0));
			btRep2.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { makeAttendReport(); }});

			JButton btScan = new JButton("scan(3)");
			add(btScan);
			btScan.setMargin(new Insets(0, 0, 0, 0));
			btScan.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { makeScanFile(); }});

			JButton btRfr = new JButton("refr(s)");
			add(btRfr);
			btRfr.setMargin(new Insets(0, 0, 0, 0));
			btRfr.addActionListener(new ActionListener(){ public void
				actionPerformed(ActionEvent a) { treeRefresh(); }});

			if(PopiangDigital.sNodeType==PopiangDigital.mainNode) {

				JButton btJoin = new JButton("join(j)");
				add(btJoin);
				btJoin.setMargin(new Insets(0, 0, 0, 0));
				btJoin.addActionListener(new ActionListener(){ public void
					actionPerformed(ActionEvent a) { gitJoin(); }});
			}

		}

		void setLabel(String l) {
			label.setText(l);
			//this.label.setText(label);
		}
	}

	void treeRefresh() {
		populateAllTreeNode();
		((DefaultTreeModel) tree.getModel()).reload();
	}

	String[][] saPref;

	void makeEmpList() {
		log.info("making employee list");
		PopiangUtil.makeStaffList();
	}

	void readAllModel() {
		try {
log.info("BASE: "+ PopiangDigital.sBaseIRI);
			Enumeration<String> ePrf = PopiangDigital.hRdfModelInfo.keys();
			allModel = ModelFactory.createDefaultModel();
			List<String[]> sapref = new ArrayList<>();
			while(ePrf.hasMoreElements()) {
				String key = ePrf.nextElement();
				RdfModelInfo rmi = PopiangDigital.hRdfModelInfo.get(key);
				sapref.add(new String[] {rmi.sPrefix, rmi.sBaseIRI});
				allModel.add(rmi.model);
			}
			saPref = new String[sapref.size()][];
			saPref = sapref.toArray(saPref);
		} catch(Exception z) {
			log.error(z);
		}
	}

	List<String[]> query(String sel) {
		try {
			
			sel = sel.replace("(*)","/rdf:rest*/rdf:first");

			readAllModel();

			Map<String,String> tokens = new HashMap<String,String>();
			for(String[] wds : saPref) tokens.put(wds[0]+":",wds[1]);
			tokens.put("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

			String sPattPrf = "("+StringUtils.join(tokens.keySet(),"|")+")";
log.info("patt: "+ sPattPrf);

			Pattern patPrf = Pattern.compile(sPattPrf);
			Pattern patVar = Pattern.compile("(\\?[A-Za-z0-9]+)");

			Matcher varMat = patVar.matcher(sel);
			List<String> aVar = new ArrayList<>();
			while(varMat.find()) { aVar.add(varMat.group(1)); }

			Matcher prfMat = patPrf.matcher(sel);
			List<String> aPrf = new ArrayList<>();
			while(prfMat.find()) { aPrf.add(prfMat.group(1)); }

			List<String> var2 = aVar.stream()
				.distinct().collect(Collectors.toList());
			List<String> prf2 = aPrf.stream()
				.distinct().collect(Collectors.toList());

			StringBuilder sb = new StringBuilder();
			for(String prf : prf2) {
				sb.append(" PREFIX "+ prf + "<"+tokens.get(prf)+"> ");
			}
			sb.append(" SELECT ");
			for(String var : var2) {
				sb.append(" "+var);
			}
			sb.append(" WHERE { ");
			sb.append(sel);
			sb.append(" } ");
			
log.info("SPQ: "+sb);
			int l = PopiangDigital.sBaseIRI.length();
			List<String[]> res = PopiangUtil.sparql(allModel, sb.toString());
			for(String[] r : res) {
				for(int i=0; i<r.length; i++) {
					if(r[i].startsWith(PopiangDigital.sBaseIRI))
						r[i] = r[i].substring(l);
				}
			}
			return res;

		} catch(Exception z) {
			log.error(z);
		}
		return null;
	}

	void gitJoin() {
		log.info("GIT JOIN");
		String spq = ""
			+ " ?a vp:สมาชิก(*) ?b . "
			+ " ?b vp:รหัส  ?c . "
			+ " ?b vp:git  ?d . "
		;	
		HashMap<String,String[]> hBrn = new HashMap<>();
		List<String[]> vals = query(spq);
		if(vals==null || vals.size()==0) return;

		for(String[] v : vals) {
			hBrn.put(v[2], v);
			log.info(v[0]+", "+v[1]+", "+v[2]+" g:"+v[3]);
		}
		List<String> abbr = vals.stream().map(v -> v[2]).collect(Collectors.toList());
		String[] abbr2 = new String[abbr.size()];
		abbr2 = abbr.toArray(abbr2);
		String input = (String) JOptionPane.showInputDialog(null
			, "Choose now..."
        	, "Choose Branch to Merge"
			, JOptionPane.QUESTION_MESSAGE, null
        	, abbr2, abbr2[0]);
		if(input!=null) {
			String[] brns = hBrn.get(input);
			log.info("branch: "+ brns[2]+ " gitrepo: "+ brns[3]);
			String branch = brns[2];
			String wkiri = brns[3];
			try {
				PopiangUtil.gitJoinWork2Main(PopiangDigital.sMainRepo, wkiri, branch);
			} catch(Exception z) { log.error(z); }
			populateAllTreeNode();
			((DefaultTreeModel) tree.getModel()).reload();
		}
	}
	void pushGit() {
		log.info("push: "+ PopiangDigital.sNodeType);
		if(PopiangDigital.sNodeType==PopiangDigital.mainNode) {
			log.info("url: "+ PopiangDigital.sMainRepo);
			try {
				PopiangUtil.gitPushMain(PopiangDigital.sMainRepo);
			} catch(Exception z) {
				log.error(z);
			}
		} else if(PopiangDigital.sNodeType==PopiangDigital.workNode) {
			log.info("url: "+ PopiangDigital.sMainRepo);
			log.info("own: "+ PopiangDigital.sWorkRepo);
			try {
				PopiangUtil.gitPushWork(PopiangDigital.sMainRepo
					, PopiangDigital.sWorkRepo, PopiangDigital.sPrefix);
			} catch(Exception z) {
				log.error(z);
			}
		}
	}

	void saveFile() {
		int id = tabbedPane.getSelectedIndex();
		if(id<0) return;
		String ttl = tabbedPane.getTitleAt(id);
		TextFileInfo tinf = hTabPane.get(ttl);
		if(tinf.text.isDirty()) {
			String tx = tinf.text.getText();
			try {
				Files.write(tinf.path, tx.getBytes("UTF-8"));
			} catch(IOException x) { }
			PopiangUtil.readRdfModel(tinf.path.toFile());
			tinf.text.setDirty(false);
			log.info("SAVE FILE: "+ tinf.path);
		}
	}

	void foldAll() {
		int id = tabbedPane.getSelectedIndex();
		if(id<0) return;
		String ttl = tabbedPane.getTitleAt(id);
		TextFileInfo tinf = hTabPane.get(ttl);
		FoldManager fm = tinf.text.getFoldManager();
		int fc = fm.getFoldCount();
		for(int i=0; i<fc; i++) {
			Fold fold = fm.getFold(i);
			fold.setCollapsed(true);
		}
	}

	void addEmpRdf() {
		int id = tabbedPane.getSelectedIndex();
		if(id<0) return;
		String ttl = tabbedPane.getTitleAt(id);
		TextFileInfo tinf = hTabPane.get(ttl);
		String tx = PopiangUtil.makeStaffList(); 
		if(tx!=null && tx.length()>0) {
			tinf.text.insert(tx, tinf.text.getCaretPosition());
		}
	}

	void newId() {
		String newid = null;
		int id = tabbedPane.getSelectedIndex();
		if(id<0) return;
		String ttl = tabbedPane.getTitleAt(id);
		TextFileInfo tinf = hTabPane.get(ttl);
		List<TokenInfo> tks =  tinf.text.getTokens();
		log.info("NEW ID: "+ tks.size());
		int maxid = 0;
		String pref = null;
		for(TokenInfo tki : tks) {
			if(tki.stmPos!=1) continue;
			try {
				if(pref==null) pref = tki.label.substring(0,tki.label.indexOf(":"));
				int ii = Integer.parseInt(tki.label.substring(pref.length()+1));
				if(ii>maxid) maxid = ii;
			} catch(Exception z) {}
			log.info("   "+tki.label+" pos:"+tki.stmPos);
		}
		newid = pref+":"+(maxid+1);
		log.info("NEWID: "+ newid);
		tinf.text.insert(newid, tinf.text.getCaretPosition());
		StringSelection stringSelection = new StringSelection(newid);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	String PREFIXS = ""
			+ "PREFIX vp:   <http://ipthailand.go.th/rdf/voc-pred#> "
			+ "PREFIX vo:   <http://ipthailand.go.th/rdf/voc-obj#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
	;

	int fontSize(int lv) { return 20 - lv * 2; }
	int normSize = 16;

	void genDoc1(Model mod, String id0, XWPFDocument doc, int lv, String hd) {
		try {
			String id = id0==null? "?id" : "<"+id0+">";
			String top = id0==null? id+" vp:คือ vo:รายงาน1 . " : "";
			String sqry = PREFIXS
				+ " SELECT ?b ?c ?d ?f ?g ?h ?i WHERE { "+ top
				+ " "+id+" vp:รวม/rdf:rest*/rdf:first ?b . "
				+ " OPTIONAL { ?b vp:ชื่อ ?c } "
				+ " OPTIONAL { ?b vp:เนื้อหา ?d } "
				+ " OPTIONAL { ?b vp:รวม ?f } "
				+ " OPTIONAL { ?b vp:เวลา ?g } "
				+ " OPTIONAL { ?b vp:รหัส ?h } "
				+ " OPTIONAL { ?b vp:งบประมาณ ?i } "
				+ " } ";
			List<String[]> aRet = PopiangUtil.sparql(mod, sqry);
			int o = 0;

			for(String[] wds : aRet) {
				o++;

				XWPFParagraph pp;
				XWPFRun rr;

				log.info(wds[0]+" ชื่อ:"+wds[1]+" เนื้อ:"+wds[2]+" รวม:"+wds[3]);

				String hd0 = hd.length()==0? o+"" : hd+"."+o;
				int fsz = fontSize(lv);
				if(wds[1]!=null) { // ชื่อ
					String tx = wds[1].replace("\n"," ").replace("\r"," ");
					pp = doc.createParagraph();
					if(lv<=1) pp.setPageBreak(true);
					pp.setStyle("Heading"+lv);
					rr = pp.createRun();
					rr.addBreak();
//					rr.setFontSize(fsz);
					if(tx.startsWith("A")) rr.setText(tx);
					else rr.setText(hd0+"."+tx);
				}
				if(wds[4]!=null) { // เวลา
					String tx = wds[4].replace("\n","").replace("\r","").replace(" ","");
					pp = doc.createParagraph();
					pp.setStyle("Normal");
					pp.setIndentationFirstLine(1500);
					rr = pp.createRun();
//					rr.setFontSize(normSize);
					rr.setText(tx);
				}
				if(wds[2]!=null) { // เนื้อหา
					String tx = wds[2].replace("\n","").replace("\r","").replace(" ","");
					pp = doc.createParagraph();
					pp.setStyle("Normal");
					pp.setIndentationFirstLine(1500);
					rr = pp.createRun();
//					rr.setFontSize(normSize);
					if(wds[5]!=null) { // รหัส
						tx += " ("+wds[5]+")";
					}
					if(wds[6]!=null) { // งบประมาณ
						tx += " ("+wds[6]+")";
					}
					rr.setText(tx);
				}
				if(wds[3]!=null) { // รวม
					genDoc1(mod, wds[0], doc, lv+1, hd0);
				}
			}
		} catch(Exception z) {
			log.error(z);
		}
	}

	Model allModel = null;

	void genDoc1() {
		try {
			File tmp = new File(PopiangDigital.workDir+"/res/temp/temp1.docx");
			File out = new File(PopiangDigital.workDir+"/.out/doc/repo1.docx");
			File par = out.getParentFile();
			if(!par.exists()) par.mkdirs();

			XWPFDocument wordDoc = new XWPFDocument(new FileInputStream(tmp));
			wordDoc.removeBodyElement(0);

			Enumeration<String> ePrf = PopiangDigital.hRdfModelInfo.keys();
			Model all0 = ModelFactory.createDefaultModel();
			while(ePrf.hasMoreElements()) {
				String key = ePrf.nextElement();
				RdfModelInfo rmi = PopiangDigital.hRdfModelInfo.get(key);
				all0.add(rmi.model);
			}
			genDoc1(all0, null, wordDoc, 1, "");

			log.info("temp file: "+ tmp);
			log.info("..."+tmp.exists());

			try (FileOutputStream fos = new FileOutputStream(out)) {
				wordDoc.write(fos);
			}

			log.info("out file: "+ out);
			log.info("..."+out.exists());
		} catch(Exception z) {
			log.info(z);
		}
	}

	@Override
	public void searchEvent(SearchEvent e) {

		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result = null;

		switch (type) {
			default: // Prevent FindBugs warning later
			case MARK_ALL:
				try {
					int idx = tabbedPane.getSelectedIndex();
					String tt = tabbedPane.getTitleAt(idx);
					TextFileInfo tfin = hTabPane.get(tt);
					result = SearchEngine.markAll(tfin.text, context);
				} catch(Exception x) {}
				break;
			case FIND:
				try {
					int idx = tabbedPane.getSelectedIndex();
					String tt = tabbedPane.getTitleAt(idx);
					TextFileInfo tfin = hTabPane.get(tt);
					result = SearchEngine.find(tfin.text, context);
					if (!result.wasFound() || result.isWrapped()) {
						UIManager.getLookAndFeel().provideErrorFeedback(tfin.text);
					}
				} catch(Exception z) {}
				break;
			case REPLACE:
				try {
					int idx = tabbedPane.getSelectedIndex();
					String tt = tabbedPane.getTitleAt(idx);
					TextFileInfo tfin = hTabPane.get(tt);
					result = SearchEngine.replace(tfin.text, context);
					if (!result.wasFound() || result.isWrapped()) {
						UIManager.getLookAndFeel().provideErrorFeedback(tfin.text);
					}
				} catch(Exception z) {}
				break;
			case REPLACE_ALL:
				try {
					int idx = tabbedPane.getSelectedIndex();
					String tt = tabbedPane.getTitleAt(idx);
					TextFileInfo tfin = hTabPane.get(tt);
					result = SearchEngine.replaceAll(tfin.text, context);
					JOptionPane.showMessageDialog(null, result.getCount() +
						" occurrences replaced.");
				} catch(Exception z) {}
				break;
		}

		String text;
		if (result!=null && result.wasFound()) {
			text = "Text found; occurrences marked: " + result.getMarkedCount();
		}
		else if (type==SearchEvent.Type.MARK_ALL) {
			if (result!=null && result.getMarkedCount()>0) {
				text = "Occurrences marked: " + result.getMarkedCount();
			}
			else {
				text = "";
			}
		}
		else {
			text = "Text not found";
		}
		statusBar.setLabel(text);

	}

/*
	private void addItem(Action a, ButtonGroup bg, JMenu menu) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
		bg.add(item);
		menu.add(item);
	}
*/

	class FileTreeCellRenderer extends DefaultTreeCellRenderer {

		private FileSystemView fileSystemView;

		private JLabel label;

		FileTreeCellRenderer() {
			label = new JLabel();
			label.setOpaque(true);
			fileSystemView = FileSystemView.getFileSystemView();
		}

		@Override
		public Component getTreeCellRendererComponent( JTree tree, Object value
		, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			File file = (File)node.getUserObject();
			label.setIcon(fileSystemView.getSystemIcon(file));
			label.setText(fileSystemView.getSystemDisplayName(file));
			if(file!=null) label.setToolTipText(file.getPath());

			if (selected) {
				label.setBackground(backgroundSelectionColor);
				label.setForeground(textSelectionColor);
			} else {
				label.setBackground(backgroundNonSelectionColor);
				label.setForeground(textNonSelectionColor);
			}
			return label;
		}
	}

	void fixMisSpell() {
		int id = tabbedPane.getSelectedIndex();
		if(id<0) return;
		String ttl = tabbedPane.getTitleAt(id);
		TextFileInfo tinf = hTabPane.get(ttl);
		String tx = tinf.text.getText();
		log.info("LEN: "+ tx.length());
		Map<String,String> tokens = new HashMap<String,String>();
		for(String[] wds : aMisSpell) tokens.put(wds[0],wds[1]);
		String patts = "(" + StringUtils.join(tokens.keySet(), "|") + ")";
		Pattern patt = Pattern.compile(patts);
		StringBuffer sb = new StringBuffer();
		Matcher match = patt.matcher(tx);
		int cnt = 0;
		while(match.find()) {
			cnt++;
			String old = match.group(1);
			String thg = tokens.get(old);
			log.info(cnt+" replace '"+ old + "' with '"+thg+"'");
			match.appendReplacement(sb, thg);
		}
		match.appendTail(sb);
		tinf.text.setText(sb.toString());
	}
	String[][] aMisSpell = {
		{"ด าเนิน","ดำเนิน"},
		{"ดาเนิน","ดำเนิน"},
		{"คาแนะนา","คำแนะนำ"},
		{"คา้น","ค้าน"},
		{"คาขอ","คำขอ"},
		{"จาเป็น","จำเป็น"},
		{"ท่ี","ที่"},
		{"เพ่ือ","เพื่อ"},
		{"เม่ือ","เมื่อ"},
		{"แนะนา","แนะนำ"},
		{"กากับ","กำกับ"},
		{"สานัก","สำนัก"},
		{"ส านัก","สำนัก"},
		{"กาหนด","กำหนด"},
		{"ทาการ","ทำการ"},
		{"ทาให้","ทำให้"},
		{"กระทา","กระทำ"},
		{"ทานอง","ทำนอง"},
		{"ทางาน","ทำงาน"},
		{"ทาสัญญา","ทำสัญญา"},
		{"ทาตาม","ทำตาม"},
		{"ทาได้","ทำได้"},
		{"สามารถทา","สามารถทำ"},
		{"ทาลาย","ทำลาย"},
		{"จัดทา","จัดทำ"},
		{"จัดท า","จัดทำ"},
		{"ทาการ","ทำการ"},
		{"ทาความ","ทำความ"},
		{"ทานิติกรรม","ทำนิติกรรม"},
		{"ทาหน้าที่","ทำหน้าที่"},
		{"ทาไว้","ทำไว้"},
		{"เคร่ือง","เครื่อง"},
		{"อ่ืน","อื่น"},
		{"สาเนา","สำเนา"},
		{"คาปรึกษา","คำปรึกษา"},
		{"สารวจ","สำรวจ"},
		{"ข้ึน","ขึ้น"},
		{"ส่ี","สี่"},
		{"เก่ียว","เกี่ยว"},
		{"ท้ัง","ทั้ง"},
		{"น้ี","นี้"},
		{"นามา","นำมา"},
		{"รอ้ง","ร้อง"},
		{"เปดิ","เปิด"},
		{"ตาแหน่ง","ตำแหน่ง"},
		{"จานวน","จำนวน"},
		{"หนา้ที่","หน้าที่"},
		{"อานาจ","อำนาจ"},
		{"หรอื","หรือ"},
		{"ขอ้","ข้อ"},
		{"เกบ็","เก็บ"},
		{"เอยีด","เอียด"},
		{"สาคัญ","สำคัญ"},
		{"คาอธิบาย","คำอธิบาย"},
		{"คานึง","คำนึง"},
		{"สาหรับ","สำหรับ"},
		{"ดาเนนิ","ดำเนิน"},
		{"ตอ้ง","ต้อง"},
		{"ถงึ","ถึง"},
		{"ถงึ","ถึง"},
		{"คาสั่ง","คำสั่ง"},
	};

    void makeAttendReport() {
        int id = tabbedPane.getSelectedIndex();
        if(id<0) return;
        String ttl = tabbedPane.getTitleAt(id);
        TextFileInfo tinf = hTabPane.get(ttl);
		System.out.println("============ PATH: "+tinf.path);
		System.out.println("============ FILE: "+tinf.path.toFile());
		PopiangUtil.attendReport(tinf.path.toFile());
	}

    void makeScanFile() {
        int id = tabbedPane.getSelectedIndex();
        if(id<0) return;
        String ttl = tabbedPane.getTitleAt(id);
        TextFileInfo tinf = hTabPane.get(ttl);
		System.out.println("============ PATH: "+tinf.path);
		System.out.println("============ FILE: "+tinf.path.toFile());
		PopiangUtil.makeScanFile(tinf.path.toFile());
	}

}


