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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.FileLocation;

import javax.imageio.ImageIO;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import java.io.*;
import java.nio.channels.FileChannel;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Paths;

class TabInfo {
	RTextScrollPane pane;
	TurtleTextArea text;
	Path path;
}

public class PopiangBrowser {

    public static final String APP_TITLE = "IP Info Manager";
    private Desktop desktop;
    private FileSystemView fileSystemView;

    private File currentFile;

    private JPanel gui;

    private JTree tree;
    private DefaultTreeModel treeModel;

    private JProgressBar progressBar;
	
	JTabbedPane tabbedPane;
	Hashtable<String,TabInfo> hTabPane = new Hashtable<>();

//    private JTable table;
//    private FileTableModel fileTableModel;
    private ListSelectionListener listSelectionListener;
    private boolean cellSizesSet = false;
    private int rowIconPadding = 6;

    /* File controls. */
    private JButton openFile;
    private JButton printFile;
    private JButton editFile;

    /* File details. */
    private JLabel fileName;
    private JTextField path;
    private JLabel date;
    private JLabel size;
    private JCheckBox readable;
    private JCheckBox writable;
    private JCheckBox executable;
    private JRadioButton isDirectory;
    private JRadioButton isFile;

    /* GUI options/containers for new File/Directory creation.  Created lazily. */
    private JPanel newFilePanel;
    private JRadioButton newTypeFile;
    private JTextField name;

	TurtleTextArea textArea;
	static File curDir;

    public Container getGui() {
        if (gui==null) {


            gui = new JPanel(new BorderLayout());
//            gui.setBorder(new EmptyBorder(5,5,5,5));

            //gui = new JPanel(new BorderLayout(3,3));
            //gui.setBorder(new EmptyBorder(5,5,5,5));

            fileSystemView = FileSystemView.getFileSystemView();
            desktop = Desktop.getDesktop();

/*
            JPanel detailView = new JPanel(new BorderLayout(3,3));

            table = new JTable();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoCreateRowSorter(true);
            table.setShowVerticalLines(false);

            listSelectionListener = new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent lse) {
                    int row = table.getSelectionModel().getLeadSelectionIndex();
                    setFileDetails( ((FileTableModel)table.getModel()).getFile(row) );
                }
            };
            table.getSelectionModel().addListSelectionListener(listSelectionListener);
            JScrollPane tableScroll = new JScrollPane(table);
            Dimension d = tableScroll.getPreferredSize();
            tableScroll.setPreferredSize(new Dimension((int)d.getWidth(), (int)d.getHeight()/2));
            detailView.add(tableScroll, BorderLayout.CENTER);
*/

            // the File tree
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            treeModel = new DefaultTreeModel(root);

            TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent tse){
                    DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
                    showChildren(node);
//                    setFileDetails((File)node.getUserObject());
                }
            };

            // show the file system roots.
            //File[] roots = fileSystemView.getRoots();
			File[] roots = fileSystemView.getFiles(curDir, false); //!!
            for (File fileSystemRoot : roots) {
				String nm = fileSystemRoot.getName();
				if(nm.startsWith(".")) continue;
				if(!fileSystemRoot.isDirectory()) continue;
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
                root.add( node );
                File[] files = fileSystemView.getFiles(fileSystemRoot, true);
                for (File file : files) {
//                    if (file.isDirectory()) {
                        node.add(new DefaultMutableTreeNode(file));
//                    }
                }
            }

            tree = new JTree(treeModel);
            tree.setRootVisible(false);
            tree.addTreeSelectionListener(treeSelectionListener);
            tree.setCellRenderer(new FileTreeCellRenderer());
//            tree.expandRow(0);
            JScrollPane treeScroll = new JScrollPane(tree);

            // as per trashgod tip
            tree.setVisibleRowCount(15);

            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension( 120, (int)preferredSize.getHeight());
            treeScroll.setPreferredSize( widePreferred );

/*
            // details for a File
            JPanel fileMainDetails = new JPanel(new BorderLayout(4,2));
            fileMainDetails.setBorder(new EmptyBorder(0,6,0,6));

            JPanel fileDetailsLabels = new JPanel(new GridLayout(0,1,2,2));
            fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

            JPanel fileDetailsValues = new JPanel(new GridLayout(0,1,2,2));
            fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

            fileDetailsLabels.add(new JLabel("File", JLabel.TRAILING));
            fileName = new JLabel();
            fileDetailsValues.add(fileName);
            fileDetailsLabels.add(new JLabel("Path/name", JLabel.TRAILING));
            path = new JTextField(5);
            path.setEditable(false);
            fileDetailsValues.add(path);
            fileDetailsLabels.add(new JLabel("Last Modified", JLabel.TRAILING));
            date = new JLabel();
            fileDetailsValues.add(date);
            fileDetailsLabels.add(new JLabel("File size", JLabel.TRAILING));
            size = new JLabel();
            fileDetailsValues.add(size);
            fileDetailsLabels.add(new JLabel("Type", JLabel.TRAILING));

            JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING,4,0));

            isDirectory = new JRadioButton("Directory");
            flags.add(isDirectory);

            isFile = new JRadioButton("File");
            flags.add(isFile);
            fileDetailsValues.add(flags);

            JToolBar toolBar = new JToolBar();
            // mnemonics stop working in a floated toolbar
            toolBar.setFloatable(false);

            JButton locateFile = new JButton("Locate");
            locateFile.setMnemonic('l');

            locateFile.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                    try {
//                        System.out.println("Locate: " + currentFile.getParentFile());
                        desktop.open(currentFile.getParentFile());
                    } catch(Throwable t) {
                        showThrowable(t);
                    }
                    gui.repaint();
                }
            });
            toolBar.add(locateFile);

            openFile = new JButton("Open");
            openFile.setMnemonic('o');

            openFile.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                    try {
//                        System.out.println("Open: " + currentFile);
                        desktop.open(currentFile);
                    } catch(Throwable t) {
                        showThrowable(t);
                    }
                    gui.repaint();
                }
            });
            toolBar.add(openFile);

            editFile = new JButton("Edit");
            editFile.setMnemonic('e');
            editFile.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                    try {
                        desktop.edit(currentFile);
                    } catch(Throwable t) {
                        showThrowable(t);
                    }
                }
            });
            toolBar.add(editFile);

            printFile = new JButton("Print");
            printFile.setMnemonic('p');
            printFile.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                    try {
                        desktop.print(currentFile);
                    } catch(Throwable t) {
                        showThrowable(t);
                    }
                }
            });
            toolBar.add(printFile);

            // Check the actions are supported on this platform!
            openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
            editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
            printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));

            flags.add(new JLabel("::  Flags"));
            readable = new JCheckBox("Read  ");
            readable.setMnemonic('a');
            flags.add(readable);

            writable = new JCheckBox("Write  ");
            writable.setMnemonic('w');
            flags.add(writable);

            executable = new JCheckBox("Execute");
            executable.setMnemonic('x');
            flags.add(executable);

            int count = fileDetailsLabels.getComponentCount();
            for (int ii=0; ii<count; ii++) {
                fileDetailsLabels.getComponent(ii).setEnabled(false);
            }

            count = flags.getComponentCount();
            for (int ii=0; ii<count; ii++) {
                flags.getComponent(ii).setEnabled(false);
            }

*/

/*
            JPanel fileView = new JPanel(new BorderLayout(3,3));

            fileView.add(toolBar,BorderLayout.NORTH);
            fileView.add(fileMainDetails,BorderLayout.CENTER);

            detailView.add(fileView, BorderLayout.SOUTH);
*/


			textArea = new TurtleTextArea();
			RTextScrollPane sp = new RTextScrollPane(textArea);

			tabbedPane = new JTabbedPane();
			//tabbedPane.setTabPlacement(JTabbedPane.RIGHT);
            tabbedPane.setBorder(new EmptyBorder(0,0,0,0));
			tabbedPane.setPreferredSize(new Dimension(600,500));
			//tabbedPane.add(sp,"TAB1");

            //JSplitPane splitPane = new JSplitPane(
            //    JSplitPane.HORIZONTAL_SPLIT,
            //    treeScroll, tabbedPane);

            JPanel simpleOutput = new JPanel(new BorderLayout(3,3));
            progressBar = new JProgressBar();
            simpleOutput.add(progressBar, BorderLayout.CENTER);
            progressBar.setVisible(true);
        	simpleOutput.setPreferredSize(new Dimension(100,20));

			String week[]= { "vp","vo","trj-1", 
                         "trj-2","trj-3","trj-4","trj-5"}; 
			JList jl = new JList(week); 
			jl.setPreferredSize(new Dimension(100,100));

            gui.add(treeScroll, BorderLayout.WEST);
            gui.add(tabbedPane, BorderLayout.CENTER);
            gui.add(jl, BorderLayout.EAST);

//            gui.add(simpleOutput, BorderLayout.SOUTH);
//            gui.add(new JPanel(), BorderLayout.NORTH);

        }
        return gui;
    }

    public void showRootFile() {
        // ensure the main files are displayed
        tree.setSelectionInterval(0,0);
    }

    private TreePath findTreePath(File find) {
        for (int ii=0; ii<tree.getRowCount(); ii++) {
            TreePath treePath = tree.getPathForRow(ii);
            Object object = treePath.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)object;
            File nodeFile = (File)node.getUserObject();

            if (nodeFile==find) {
                return treePath;
            }
        }
        return null;
    }

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

/*
    private void setTableData(final File[] files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (fileTableModel==null) {
                    fileTableModel = new FileTableModel();
                    table.setModel(fileTableModel);
                }
                table.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                table.getSelectionModel().addListSelectionListener(listSelectionListener);
                if (!cellSizesSet) {
                    Icon icon = fileSystemView.getSystemIcon(files[0]);

                    // size adjustment to better account for icons
                    table.setRowHeight( icon.getIconHeight()+rowIconPadding );

                    setColumnWidth(0,-1);
                    setColumnWidth(3,60);
                    table.getColumnModel().getColumn(3).setMaxWidth(120);
                    setColumnWidth(4,-1);
                    setColumnWidth(5,-1);
                    setColumnWidth(6,-1);
                    setColumnWidth(7,-1);
                    setColumnWidth(8,-1);
                    setColumnWidth(9,-1);

                    cellSizesSet = true;
                }
            }
        });
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width<0) {
            // use the preferred width of the header..
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }
*/

    private void showChildren(final DefaultMutableTreeNode node) {
        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                if (file.isDirectory()) {
                    //File[] files = fileSystemView.getFiles(file, true); //!!
                    File[] files = fileSystemView.getFiles(file, false); //!!
                    if (node.isLeaf()) {
                        for (File child : files) {
//                            if (child.isDirectory()) {
//System.out.println("child: "+ child);
                                publish(child);
//                            }
                        }
                    }
                } else {
					String fn = file.getName();
					if(fn.endsWith(".ttl")) {
						try {
							int cnt = tabbedPane.getComponentCount();
							String d0 = curDir.getAbsolutePath();
							String d1 = file.getAbsolutePath();
							String tn = d1.substring(d0.length()+1);
//							System.out.println(d0+":"+d1+":"+tn);
							TabInfo tinf;
							if((tinf=hTabPane.get(tn))==null) {
								String fs = file.getAbsolutePath();
								Path fileName = Paths.get(fs);
								byte[] buf = Files.readAllBytes(fileName);
								String actual = new String(buf,"UTF-8");
								//String actual = Files.readString(fileName,Charset.forName("UTF-8"));
								//String actual = Files.readString(fileName);
								TurtleTextArea tta = new TurtleTextArea();
								tta.setText(actual);
								tta.setDirty(false);
								RTextScrollPane tasp = new RTextScrollPane(tta);
								tabbedPane.add(tasp, tn);
								int idx = tabbedPane.indexOfComponent(tasp);
								tabbedPane.setTabComponentAt(idx, getTitlePanel(tabbedPane, tasp, tn));
								tabbedPane.setSelectedIndex(idx);

								tinf = new TabInfo();
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

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                tree.setEnabled(true);
            }
        };
        worker.execute();
    }

/*
    private void setFileDetails(File file) {
        currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);
        fileName.setIcon(icon);
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        path.setText(file.getPath());
        date.setText(new Date(file.lastModified()).toString());
        size.setText(file.length() + " bytes");
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());

        isFile.setSelected(file.isFile());

        JFrame f = (JFrame)gui.getTopLevelAncestor();
        if (f!=null) {
            f.setTitle(
                APP_TITLE +
                " :: " +
                fileSystemView.getSystemDisplayName(file) );
        }

        gui.repaint();
    }
*/

	private  JPanel getTitlePanel(final JTabbedPane tabbedPane, RTextScrollPane panel, String title) {
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		titlePanel.setOpaque(false);
		JLabel titleLbl = new JLabel(title);
		titleLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		titlePanel.add(titleLbl);
		JButton closeButton = new JButton("x");
		closeButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

		closeButton.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				TabInfo tinf = hTabPane.get(title);
				if(tinf.text.isDirty()) {
					int dialogResult = JOptionPane.showConfirmDialog (null, "เนื่องจากมีการแก้ไข จะบันทึกหรือไม่","Warning",JOptionPane.YES_NO_OPTION);
					if(dialogResult == JOptionPane.YES_OPTION){
						String tx = tinf.text.getText();
						try {
							Files.write(tinf.path, tx.getBytes("UTF-8"));
						} catch(IOException x) {
						}
					}
				}
				hTabPane.remove(title);
				tabbedPane.remove(panel);
			}
		});
		titlePanel.add(closeButton);
		return titlePanel;
	}
	public void proc(File dir) {
		curDir = dir;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Significantly improves the look of the output in
                    // terms of the file names returned by FileSystemView!
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
					UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
                } catch(Exception weTried) {
                }
                JFrame f = new JFrame("THARISTREE.J (TRJ)");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    public static void main(String[] args) {
		if(args.length==0) {
			curDir = new File("../.");
		} else {
			curDir = new File(args[0]);
			//System.out.println("args:"+ curDir);
		}
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Significantly improves the look of the output in
                    // terms of the file names returned by FileSystemView!
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
					UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
                } catch(Exception weTried) {
                }
                JFrame f = new JFrame(APP_TITLE);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                PopiangBrowser popi = new PopiangBrowser();
                f.setContentPane(popi.getGui());

                try {
                    URL urlBig = popi.getClass().getResource("fb-icon-32x32.png");
                    URL urlSmall = popi.getClass().getResource("fb-icon-16x16.png");
                    ArrayList<Image> images = new ArrayList<Image>();
                    images.add( ImageIO.read(urlBig) );
                    images.add( ImageIO.read(urlSmall) );
                    f.setIconImages(images);
                } catch(Exception weTried) {}

                f.pack();
                f.setLocationByPlatform(true);
                f.setMinimumSize(f.getSize());
                f.setVisible(true);

                popi.showRootFile();
            }
        });
    }
}

/*
class FileTableModel extends AbstractTableModel {

    private File[] files;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private String[] columns = {
        "Icon",
        "File",
        "Path/name",
        "Size",
        "Last Modified",
        "R",
        "W",
        "E",
        "D",
        "F",
    };

    FileTableModel() {
        this(new File[0]);
    }

    FileTableModel(File[] files) {
        this.files = files;
    }

    public Object getValueAt(int row, int column) {
        File file = files[row];
        switch (column) {
            case 0:
                return fileSystemView.getSystemIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
                return file.getPath();
            case 3:
                return file.length();
            case 4:
                return file.lastModified();
            case 5:
                return file.canRead();
            case 6:
                return file.canWrite();
            case 7:
                return file.canExecute();
            case 8:
                return file.isDirectory();
            case 9:
                return file.isFile();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 3:
                return Long.class;
            case 4:
                return Date.class;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return Boolean.class;
        }
        return String.class;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.length;
    }

    public File getFile(int row) {
        return files[row];
    }

    public void setFiles(File[] files) {
        this.files = files;
        fireTableDataChanged();
    }
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
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        File file = (File)node.getUserObject();
        label.setIcon(fileSystemView.getSystemIcon(file));
        label.setText(fileSystemView.getSystemDisplayName(file));
        label.setToolTipText(file.getPath());

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

