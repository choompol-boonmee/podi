package popdig;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.folding.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rtextarea.RTextAreaHighlighter;
import org.fife.ui.rtextarea.SmartHighlightPainter;
import org.apache.log4j.Logger;

public class TurtleFoldParser implements FoldParser {

	static Logger log = Logger.getLogger(TurtleFoldParser.class);

	private boolean foldableMultiLineComments;

	protected static final char[] C_MLC_END = "*/".toCharArray();

	TurtleTextArea txtArea;

	public TurtleFoldParser(TurtleTextArea tta) {
		txtArea = tta;
	}

	public boolean getFoldableMultiLineComments() {
		return foldableMultiLineComments;
	}


	int cnt = 0;
	final static int RESERVED_DOT = 1;
	final static int RESERVED_SEMICOLON = 2;
	final static int RESERVED_NOTYET = 3;

	@Override
	public List<Fold> getFolds(RSyntaxTextArea textArea0) {

		log.info("getFolds");

		List<Fold> folds = new ArrayList<>();

		Fold currentFold = null;
		int lineCount = txtArea.getLineCount();
		boolean inMLC = false;
		boolean inSTM = false;
		boolean inPFX = false;
		int idord = 0;
		int mlcStart = 0;
		int importStartLine = -1;
		int lastSeenImportLine = -1;
		int importGroupStartOffs = -1;
		int importGroupEndOffs = -1;
		int lastRightCurlyLine = -1;
		boolean inBRK = false;
		Fold prevFold = null;

		List<TokenInfo> aToken = new ArrayList<>();

		SmartHighlightPainter pnt1 = new SmartHighlightPainter(new Color(0xfffff0));
		pnt1.setRoundedEdges(true);
		SmartHighlightPainter pnt2 = new SmartHighlightPainter(new Color(0xf0fff0));
		pnt2.setRoundedEdges(true);
		SmartHighlightPainter pnt3 = new SmartHighlightPainter(new Color(0xf0f0ff));
		pnt3.setRoundedEdges(true);

		try {

			cnt++;
			int ln1=-1, ln2=-1;

			int endLineWith = RESERVED_DOT;
			inBRK = false;

			for (int line=0; line<lineCount; line++) {

				Token t = txtArea.getTokenListForLine(line);

				if(endLineWith==RESERVED_DOT) {
					idord = 0;
				} else if(endLineWith==RESERVED_SEMICOLON) {
					idord = 1;
				} else {
				}
				endLineWith=RESERVED_NOTYET;
				
				while (t!=null && t.isPaintable()) {
					if(t.getType()==Token.WHITESPACE) {
					} else if(t.getType()==Token.SEPARATOR) {
						inBRK = true;
					} else {
						idord++;
					}

					if(!inPFX) {
						if(idord==1 && t.getType()==Token.PREPROCESSOR) {
							inPFX = true;
							currentFold = new Fold(FoldType.CODE, txtArea, t.getOffset());
							folds.add(currentFold);
						}
					} else {
						if(t.isIdentifier()) {
							inPFX = false;
						} else if(t.isSingleChar('.')) {
							endLineWith = RESERVED_DOT;
							currentFold.setEndOffset(t.getOffset());
						}
					}
					if(!inSTM) {
						if(idord==1 && t.isIdentifier()) {
							String id = t.getLexeme();
							int of = t.getOffset();
//							DocumentRange rg = new DocumentRange(of, of+id.length());
//							aRange.add(rg);

							aToken.add(new TokenInfo(id, of, of+id.length(), idord, pnt1));

//							System.out.println("l:"+line+" t:"+idord+" i:"+id+" o:"+t.getOffset());
							inSTM = true;
							ln1 = line;
							currentFold = new Fold(FoldType.CODE, txtArea, t.getOffset());
							folds.add(currentFold);
						}
					} else {
						if(t.isIdentifier()) {
							String id = t.getLexeme();
							int of = t.getOffset();
//							DocumentRange rg = new DocumentRange(of, of+id.length());
//							aRange.add(rg);

							aToken.add(new TokenInfo(id, of, of+id.length(), idord, pnt2));

//							System.out.println("l:"+line+" t:"+idord+" i:"+id+" o:"+t.getOffset());
						} else if(t.isSingleChar(';')) {
							endLineWith = RESERVED_SEMICOLON;
						} else if(t.isSingleChar('.')) {
							endLineWith = RESERVED_DOT;
							inSTM = false;
							if(ln1==line) {
								folds.remove(currentFold);
							} else {
								currentFold.setEndOffset(t.getOffset());
							}
						} else if(t.getType()==Token.LITERAL_STRING_DOUBLE_QUOTE) {
							String id = t.getLexeme();
							int of = t.getOffset();
//							DocumentRange rg = new DocumentRange(of, of+id.length());
//							aRange.add(rg);

							aToken.add(new TokenInfo(id, of, of+id.length(), idord, pnt3));

//							System.out.println("l:"+line+" t:"+idord+" p:"+t.getType()+" o:"+of);
						} else if(t.getType()==Token.LITERAL_CHAR) {
							String id = t.getLexeme();
							int of = t.getOffset();
//							DocumentRange rg = new DocumentRange(of, of+id.length());
//							aRange.add(rg);

							aToken.add(new TokenInfo(id, of, of+id.length(), idord, pnt3));

//							System.out.println("l:"+line+" t:"+idord+" p:"+t.getType()+" o:"+of);
						} else if(t.getType()==Token.LITERAL_NUMBER_DECIMAL_INT) {
							String id = t.getLexeme();
							int of = t.getOffset();
//							DocumentRange rg = new DocumentRange(of, of+id.length());
//							aRange.add(rg);

							aToken.add(new TokenInfo(id, of, of+id.length(), idord, pnt3));

//							System.out.println("l:"+line+" t:"+idord+" i:"+id+" o:"+of);
						}
					}

					t = t.getNextToken();

				}

			}

		} catch (BadLocationException ble) { // Should never happen
			ble.printStackTrace();
		}

		if(txtArea.bNeedFold) {
			txtArea.bNeedFold = false;
			SwingUtilities.invokeLater(() -> {
				//for(int i=0; i<folds.size(); i++) {
				//	folds.get(i).setCollapsed(true);
				//}
				//txtArea.revalidate();
				//txtArea.repaint();
				//tasp.revalidate();
				//tasp.repaint();
				//setCaretPos(tta.getCaretPosition());
				//txtArea.getFoldManager().reparse();

				//txtArea.clearMarkAllHighlights();

				txtArea.setTokens(aToken);

			});
		}

		return folds;

	}


	public boolean isLeftCurly(Token t) {
		return t.isLeftCurly();
	}

	public boolean isRightCurly(Token t) {
		return t.isRightCurly();
	}

	public void setFoldableMultiLineComments(boolean foldable) {
		this.foldableMultiLineComments = foldable;
	}

}
