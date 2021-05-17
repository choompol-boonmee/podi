package popdig;

import java.awt.Font;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

import java.nio.charset.Charset;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.templates.StaticCodeTemplate;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import javax.swing.event.*;

//public class TurtleTextArea extends RSyntaxTextArea {
public class TurtleTextArea extends TextEditorPane {

	static Logger log = Logger.getLogger(TurtleTextArea.class);

	public boolean bNeedFold = true;

	private static final long serialVersionUID = 1L;
	private final CodeTemplateManager ctm;

	private String strTag;

	public TurtleTextArea() {

		setRows(30);
		setColumns(80);
		//super(30,80);

		setRoundedSelectionEdges(true);
		String syntx = "text/ttl";
		//String cls = org.fife.ui.rsyntaxtextarea.modes.TurtleTokenMaker.class.getCanonicalName();
		String cls = popdig.TurtleTokenMaker.class.getCanonicalName();

		FoldParserManager.get().addFoldParserMapping( syntx, new TurtleFoldParser(this) );

		RSyntaxTextArea.setTemplatesEnabled( true );
		ctm = RSyntaxTextArea.getCodeTemplateManager();

		AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
		atmf.putMapping( syntx, cls );
		setSyntaxEditingStyle( syntx );

		changeStyleViaThemeXml();
		setCodeFoldingEnabled( true );
		setAntiAliasingEnabled( true );
//		setLineWrap( true );
//		setWrapStyleWord( true );
		//setAutoIndentEnabled( false );
		//setWhitespaceVisible( true );

		Font f = getFont();
System.out.println("FONT : "+ PopiangDigital.sFontName + " size:" +PopiangDigital.iFontSize);
		setFont( new Font( PopiangDigital.sFontName, f.getStyle(), PopiangDigital.iFontSize));
//		setFont( new Font( "Cordia New", f.getStyle(), 24));

/*
		try {
		String fName = "/fonts/EkkamaiNew-Regular.ttf";
		InputStream is = getClass().getResourceAsStream(fName);
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
System.out.println("NEW FONT: "+ font);
		setFont( font.deriveFont(20f) );
		} catch(Exception z) {
			z.printStackTrace();
		}
*/

		CompletionProvider provider = createCompletionProvider();

		AutoCompletion ac = new AutoCompletion( provider );
		ac.install( this );

		setBracketMatchingEnabled( true );

		// Remove the code-folding component and it's separator from the popup menu:
		JPopupMenu popup = getPopupMenu();
		popup.remove( popup.getComponent( 9 ) ); // the separator
		popup.remove( popup.getComponent( 9 ) ); // the item
		popup.addSeparator();
		// popup.add( saver );
	}

	List<TokenInfo> aToken;

	public List<TokenInfo> getTokens() { return aToken; }

	public String getToolTipText(int off) {
		if(aToken==null) return null;
		for(TokenInfo tki : aToken) {
			if(tki.startOffset<=off && off<=tki.endOffset) {
				return tki.label;
			}
		}
		return null;
	}

	public void setTokens(List<TokenInfo> tks) {
		aToken = tks;
		Highlighter h = getHighlighter();
		h.removeAllHighlights();
		revalidate();
		for(TokenInfo tok : aToken) {
			try {
				h.addHighlight(tok.startOffset, tok.endOffset, tok.paint);
			} catch(Exception z) {}
		}
		repaint();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		bNeedFold = true;
		super.insertUpdate(e);
	}
	@Override
	public void removeUpdate(DocumentEvent e) {
		bNeedFold = true;
		super.removeUpdate(e);
	}

	public String getTag() {
		return this.strTag;
	}

	public void setTag( String strTag ) {
		this.strTag = strTag;
	}

	private void changeStyleViaThemeXml() {
		try {
			Theme theme = Theme.load( getClass().getResourceAsStream( "/idea.xml" ) );
			theme.apply( this );
		}
		catch ( IOException ioe ) { // Never happens
			Logger.getLogger( getClass() ).error( ioe, ioe );
		}
	}

	private CompletionProvider createCompletionProvider() {
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		provider.addCompletion( new BasicCompletion( provider, "prefix" ) );
		provider.addCompletion( new ShorthandCompletion( provider, "hd", 
"@prefix vp: <http://ipthailand.go.th/rdf/vc-pre#> .\n"+
"@prefix vo: <http://ipthailand.go.th/rdf/vo-obj#> .\n"+
"\n" ));
		return provider;
	}

}

