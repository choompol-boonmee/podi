package popdig;

import javax.swing.text.Highlighter.*;

public class TokenInfo {
	public String label;
	public int startOffset, endOffset;
	public HighlightPainter paint;
	public int type, stmPos, inList;
	public TokenInfo(String l, int st, int en, int pos, HighlightPainter p) {
		label = l;
		startOffset = st;
		endOffset = en;
		paint = p;
		stmPos = pos;
	}
}

