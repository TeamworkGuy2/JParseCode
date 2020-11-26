package twg2.parser.tokenizers;

import java.util.EnumSet;

import twg2.parser.codeParser.CommentStyle;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public final class CommentTokenizer {

	private CommentTokenizer() { throw new AssertionError("cannot instantiate static class CommentTokenizer"); }


	public static CharParserFactory createCommentTokenizer(boolean reusable, EnumSet<CommentStyle> style) {
		StringParserBuilder commentParser = new StringParserBuilder("comment " + style);
		int markerCount = 0;

		if(style.contains(CommentStyle.MULTILINE_C_STYLE)) {
			commentParser.addStartEndMarkers("multi-line comment", "/*", "*/", Inclusion.INCLUDE);
			markerCount++;
		}
		if(style.contains(CommentStyle.END_OF_LINE)) {
			commentParser.addStartEndMarkers("single-line comment", "//", '\n', Inclusion.EXCLUDE);
			markerCount++;
		}
		if(style.contains(CommentStyle.XML_COMMENT)) {
			commentParser.addStartEndMarkers("XML comment", "<!--", "-->", Inclusion.INCLUDE);
			markerCount++;
		}

		if(markerCount == 0) {
			EnumSet<CommentStyle> validCommentSet = EnumSet.of(CommentStyle.MULTILINE_C_STYLE, CommentStyle.END_OF_LINE, CommentStyle.XML_COMMENT);
			throw new IllegalArgumentException("comment parser style enum set '" + style + "' is not valid, must contain at least one of '" + validCommentSet + "'");
		}

		return commentParser.build(reusable);
	}

}
