package popdig;

import org.apache.log4j.Logger;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.util.regex.*;
import java.nio.file.Files;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import javax.swing.JOptionPane;
import java.io.*;

public class PopiangConfig {
	static Logger log = Logger.getLogger(PopiangConfig.class);

	JTextField tfPrefix, tfName, tfEmail, tfGitMain, tfGitOwn, tfBaseIRI;
	JLabel lbPrefix, lbName, lbEmail, lbGitMain, lbGitOwn, lbBaseIRI;
	JTextField tfRecv, tfPass, tfImap, tfSmtp;
	JLabel lbRecv, lbPass, lbImap, lbSmtp;
	JRadioButton mainNode, workNode;
	JDialog dial;

	public void config() {
System.out.println("CONNFIG....");

		log.info("config...");
		dial = new JDialog(null, "PoDy Config", Dialog.ModalityType.DOCUMENT_MODAL);
//		dial.setBounds(132, 132, 300, 200);
		Container dialogContainer = dial.getContentPane();
		dialogContainer.setLayout(new BorderLayout());
		JPanel pane = new JPanel();
		dialogContainer.add(pane, BorderLayout.CENTER);
		
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

		// NODE TYPE
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.LEFT));
		p1.add(new JLabel("Node Type"));
		mainNode = new JRadioButton(PopiangDigital.mainNode);
		workNode = new JRadioButton(PopiangDigital.workNode);
		p1.add(mainNode);
		p1.add(workNode);
		pane.add(p1);
		ButtonGroup group = new ButtonGroup();
		group.add(mainNode);
		group.add(workNode);
		mainNode.addChangeListener(new ChangeListener() { @Override
			public void stateChanged(ChangeEvent changEvent) { changeValue(); } });
		workNode.addChangeListener(new ChangeListener() { @Override
			public void stateChanged(ChangeEvent changEvent) { changeValue(); } });

		if(PopiangDigital.sNodeType==PopiangDigital.mainNode) {
			mainNode.setSelected(true);
		} else if(PopiangDigital.sNodeType==PopiangDigital.workNode) {
			workNode.setSelected(true);
		}

		JPanel p2;
		// NAME
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("NAME"));
		tfName = new JTextField(20);
		p2.add(tfName);
		lbName = new JLabel("?");
		lbName.setForeground(Color.red);
		p2.add(lbName);
		pane.add(p2);
		tfName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { changeValue(); }
			public void removeUpdate(DocumentEvent e) { changeValue(); }
			public void insertUpdate(DocumentEvent e) { changeValue(); } });
	
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("BASE-IRI"));
		tfBaseIRI = new JTextField(25);
		p2.add(tfBaseIRI);
		lbBaseIRI = new JLabel("?");
		lbBaseIRI.setForeground(Color.red);
		p2.add(lbBaseIRI);
		pane.add(p2);
		tfBaseIRI.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { changeValue(); }
			public void removeUpdate(DocumentEvent e) { changeValue(); }
			public void insertUpdate(DocumentEvent e) { changeValue(); } });

		// EMAIL
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("EMAIL"));
		tfEmail = new JTextField(20);
		p2.add(tfEmail);
		lbEmail = new JLabel("?");
		lbEmail.setForeground(Color.red);
		p2.add(lbEmail);
		pane.add(p2);
		tfEmail.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { changeValue(); }
			public void removeUpdate(DocumentEvent e) { changeValue(); }
			public void insertUpdate(DocumentEvent e) { changeValue(); } });

		// PREFIX
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("Prefix"));
		tfPrefix = new JTextField(10);
		p2.add(tfPrefix);
		lbPrefix = new JLabel("?");
		lbPrefix.setForeground(Color.red);
		p2.add(lbPrefix);
		pane.add(p2);
		tfPrefix.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { changeValue(); }
			public void removeUpdate(DocumentEvent e) { changeValue(); }
			public void insertUpdate(DocumentEvent e) { changeValue(); } });

		// GIT MAIN
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("Main Git Repo"));
		tfGitMain = new JTextField(40);
		p2.add(tfGitMain);
		lbGitMain = new JLabel("?");
		p2.add(lbGitMain);
		lbGitMain.setForeground(Color.red);
		pane.add(p2);
		tfGitMain.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { changeValue(); }
			public void removeUpdate(DocumentEvent e) { changeValue(); }
			public void insertUpdate(DocumentEvent e) { changeValue(); } });

		// GIT OWNER
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("Own Git Repo"));
		tfGitOwn = new JTextField(40);
		p2.add(tfGitOwn);
		lbGitOwn = new JLabel("?");
		p2.add(lbGitOwn);
		lbGitOwn.setForeground(Color.red);
		pane.add(p2);

		// EMAIL RECV
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("RECV EMAIL"));
		tfRecv = new JTextField(25);
		p2.add(tfRecv);
		lbRecv = new JLabel("?");
		p2.add(lbRecv);
		lbRecv.setForeground(Color.red);
		pane.add(p2);

		// EMAIL PASS
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("PASSWORD"));
		tfPass = new JTextField(10);
		p2.add(tfPass);
		lbPass = new JLabel("?");
		p2.add(lbPass);
		lbPass.setForeground(Color.red);
		pane.add(p2);

		// EMAIL IMAP
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("INCOMING SERVER"));
		tfImap = new JTextField(25);
		p2.add(tfImap);
		lbImap = new JLabel("?");
		p2.add(lbImap);
		lbImap.setForeground(Color.red);
		pane.add(p2);

		// EMAIL SMTP
		p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		p2.add(new JLabel("OUTGOING SERVER"));
		tfSmtp = new JTextField(25);
		p2.add(tfSmtp);
		lbSmtp = new JLabel("?");
		p2.add(lbSmtp);
		lbSmtp.setForeground(Color.red);
		pane.add(p2);

		// ACTION BUTTONS
		JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout());

		JButton okButton = new JButton("Ok");
		panel1.add(okButton);
		JButton check = new JButton("Check");
		panel1.add(check);
		JButton genKey = new JButton("GenKey");
		panel1.add(genKey);
		JButton copKey = new JButton("CopKey");
		panel1.add(copKey);
		JButton push = new JButton("Push");
		panel1.add(push);

		okButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { saveConfig(); }});
		check.addActionListener(new ActionListener() { @Override
			public void actionPerformed(ActionEvent e) { checkConfig(); }});
		genKey.addActionListener(new ActionListener() { @Override
			public void actionPerformed(ActionEvent e) { generateKeyPair(); }});
		copKey.addActionListener(new ActionListener() { @Override
			public void actionPerformed(ActionEvent e) { copyPubKey(); }});
		push.addActionListener(new ActionListener() { @Override
			public void actionPerformed(ActionEvent e) { gitPush(); }});

		tfName.setText("MyName");
		tfEmail.setText("dr.choompol.boonmee@gmail.com");
//		tfEmail.setText("choompol@drnum.net");
		tfGitMain.setText("git@gitlab.com:dr.choompol.boonmee/maindoc.git");
		tfGitOwn.setText("git@gitlab.com:choompol-drnumnet/workdoc.git");
		tfPrefix.setText(PopiangDigital.sCom);
//		tfPrefix.setText("main");
		tfBaseIRI.setText("http://ipthailand.go.th/rdf/");
		tfRecv.setText("gitlabprj@ipthailand.go.th");
		tfPass.setText("Pass1234");
		tfImap.setText("incoming.mail.go.th");
		tfSmtp.setText("outgoing.mail.go.th");

		dialogContainer.add(panel1, BorderLayout.SOUTH);
		dial.pack();

		dial.setVisible(true);
	}

	void gitPush() {
		try {
//			String url = tfGitMain.getText();
//			String own = tfGitOwn.getText();
			changeValue();
			log.info(">>>>> VALUE: "+ cntOK);
			if(bMain && cntOK>=minOK) {
				log.info("PUSH MAIN...");
				PopiangUtil.gitPushMain0(gitmain);
			} else if(bWork && cntOK>=minOK) {
				if(gitmain.equals(gitown)) {
					log.info("MAIN: "+ gitmain);
					log.info("OWN : "+ gitown);
				} else if(pref.equals("main")) {
					log.info("PREFIX: "+ pref);
				} else {
					log.info("PUSH MEMBER...");
					PopiangUtil.gitPushWork0(gitmain, gitown, pref);
				}
			}
		} catch(Exception z) {
		}
	}

	void generateKeyPair() {
		try {
			String email = tfEmail.getText();
			if(PAT_EMAIL.matcher(email).find()==false) {
				JOptionPane.showConfirmDialog (null,"Please input email"
					, "ERROR",JOptionPane.WARNING_MESSAGE);
				return;
			}
			PopiangUtil.generateKeyPair(email);
			String pub1 = new String(Files.readAllBytes(PopiangDigital.fPub.toPath()));
			log.info("public key: "+ pub1);
			StringSelection stringSelection = new StringSelection(pub1);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
		} catch(Exception z) {
		}
	}

	void copyPubKey() {
		log.info("copy public key");
		try {
			String pub1 = new String(Files.readAllBytes(PopiangDigital.fPub.toPath()));
			log.info("public key: "+ pub1);
			StringSelection stringSelection = new StringSelection(pub1);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
		} catch(Exception z) {
			log.error(z);
		}
	}

	public static final Pattern PAT_EMAIL = Pattern.compile(
		"^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	public static final Pattern PAT_PREFIX = Pattern.compile("[a-z]{4,}");
	public static final Pattern PAT_NAME = Pattern.compile(".+");
	public static final Pattern PAT_GITURL = Pattern.compile("git@(.+):(.+).git$");
	public static final Pattern PAT_IRI = Pattern.compile("(http|https):.+");

	int cntOK = 0, minOK = 7;
	boolean bMain = false, bWork = false;
	String pref="",name="",mail="",gitmain="",gitown="",recv="",pass="",imap="",smtp="",baseiri="";

	void changeValue() {
		boolean bMain0 = bMain;
		boolean bWork0 = bWork;
		bMain = bWork = false;
		cntOK = 0;
		pref = tfPrefix.getText();
		name = tfName.getText();
		mail = tfEmail.getText();
		gitmain = tfGitMain.getText();
		gitown = tfGitOwn.getText();
		recv = tfRecv.getText();
		pass = tfPass.getText();
		imap = tfImap.getText();
		smtp = tfSmtp.getText();
		baseiri = tfBaseIRI.getText();
log.info("START CHANGE VALUE");
		if(mainNode.isSelected()) {
			bMain = true;
			cntOK++;
log.info(cntOK+": TYPE is OK");
		} else if(workNode.isSelected()) {
			bWork = true;
			cntOK++;
log.info(cntOK+": TYPE is OK");
		} else {
		}
		if(PAT_NAME.matcher(name).find()) {
			lbName.setForeground(Color.green);
			lbName.setText("OK");
			cntOK++;
log.info(cntOK+": NAME is OK");
		} else {
			lbName.setForeground(Color.red);
			lbName.setText("?");
		}
		if(PAT_EMAIL.matcher(mail).find()) {
			lbEmail.setForeground(Color.green);
			lbEmail.setText("OK");
			cntOK++;
log.info(cntOK+": EMAIL is OK");
		} else {
			lbEmail.setForeground(Color.red);
			lbEmail.setText("?");
		}
		if(PAT_PREFIX.matcher(pref).find()) {
			lbPrefix.setForeground(Color.green);
			lbPrefix.setText("OK");
			cntOK++;
log.info(cntOK+": PREFIX is OK");
		} else {
			lbPrefix.setForeground(Color.red);
			lbPrefix.setText("?");
		}
		if(PAT_GITURL.matcher(gitmain).find()) {
			lbGitMain.setForeground(Color.green);
			lbGitMain.setText("OK");
			cntOK++;
log.info(cntOK+": MAINREPO is OK");
		} else {
			lbGitMain.setForeground(Color.red);
			lbGitMain.setText("?");
		}
		if(PAT_IRI.matcher(baseiri).find()) {
			lbBaseIRI.setForeground(Color.green);
			lbBaseIRI.setText("OK");
			cntOK++;
log.info(cntOK+": BASEIRI is OK");
		} else {
			lbBaseIRI.setForeground(Color.red);
			lbBaseIRI.setText("?");
		}
		if(PAT_GITURL.matcher(gitown).find()) {
			lbGitOwn.setForeground(Color.green);
			lbGitOwn.setText("OK");
			cntOK++;
log.info(cntOK+": WORKREPO is OK");
		} else {
			lbGitOwn.setForeground(Color.red);
			lbGitOwn.setText("?");
		}
		if(bWork) {
			if(gitmain.equals(gitown)) {
				cntOK--;
log.info(cntOK+": WORKREPO is SAME");
			}
//			if(pref.equals("main")) {
			if(pref.equals(PopiangDigital.sCom)) {
				cntOK--;
log.info(cntOK+": PREFIX is MAIN");
			}
		}
		if(bMain && !bMain0) {
			SwingUtilities.invokeLater(new Runnable() { public void run() { 
				tfGitOwn.setEnabled(false);
				tfPrefix.setText("main");
				tfPrefix.setEnabled(false);
				tfBaseIRI.setEnabled(true);
				tfRecv.setEnabled(true);
				tfPass.setEnabled(true);
				tfImap.setEnabled(true);
				tfSmtp.setEnabled(true);
			}});
		} else if(bWork && !bWork0) {
			SwingUtilities.invokeLater(new Runnable() { public void run() { 
				tfGitOwn.setEnabled(true);
				tfPrefix.setEnabled(true);
				tfBaseIRI.setEnabled(false);
				tfRecv.setEnabled(false);
				tfPass.setEnabled(false);
				tfImap.setEnabled(false);
				tfSmtp.setEnabled(false);
			}});
		}
	}
	void checkConfig() {
		changeValue();
	}
	void saveConfig() {
		changeValue();
		try {
			FileOutputStream fis = new FileOutputStream(PopiangDigital.fNode);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fis));
			bw.write("@prefix vp:     <http://ipthailand.go.th/rdf/voc-pred#> .\n");
			bw.write("@prefix vo:     <http://ipthailand.go.th/rdf/voc-obj#> .\n");
			bw.write("@prefix nd:     <https://ipthailand.go.th/rdf/node#> .\n");
			bw.newLine();
			bw.write(           	"nd:1        a           vo:PopiNode ;\n");
			if(bMain)	bw.write(	"            vp:type     vo:MainNode ;\n");
			else 		bw.write(	"            vp:type     vo:WorkNode ;\n");
			bw.write(           	"            vp:owner    nd:2 ;\n");
			bw.write(				"            vp:prefix   '"+pref+"' ;\n");
			bw.write(				"            vp:mainrep   '"+gitmain+"' ;\n");
			bw.write(				"            vp:workrep   '"+gitown+"' ;\n");
			bw.write(				"            vp:baseiri   '"+baseiri+"' ;\n");
			bw.write(				"            vp:channel  nd:3 ;\n");
			bw.write(				"            vp:gui      nd:4\n");
			bw.write(				".\n");
			bw.write(				"nd:2        a           vo:Person ;\n");
			bw.write(				"            vp:email    '"+mail+"' ;\n");
			bw.write(				"            vp:name     '"+name+"'\n");
			bw.write(				".\n");
			bw.write(				"nd:3        a           vo:RecvEmail ;\n");
			bw.write(				"            vp:email    '"+recv+"' ;\n");
			bw.write(				"            vp:passwd   '"+pass+"' ;\n");
			bw.write(				"            vp:imap     '"+imap+"' ;\n");
			bw.write(				"            vp:smtp     '"+smtp+"' \n");
			bw.write(				".\n");
			bw.write(				"nd:4        a       vo:GuiSetting ;\n");
			bw.write(				"            vp:fontName     'Cordia New' ;\n");
			bw.write(				"            vp:fontSize     20\n");
			bw.write(				".\n");
			bw.close();
		} catch(Exception z) {
		}
		dial.setVisible(false);
	}
}

