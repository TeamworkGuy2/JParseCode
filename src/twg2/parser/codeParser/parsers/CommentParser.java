package twg2.parser.codeParser.parsers;

import java.util.EnumSet;

import twg2.parser.Inclusion;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.text.CharPrecondition;
import twg2.parser.text.StringBoundedParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public final class CommentParser {

	private CommentParser() { throw new AssertionError("cannot instantiate static class CommentParser"); }


	public static final CharPrecondition createCommentParserForJava() {
		CharPrecondition commentParser = new StringBoundedParserBuilder()
			.addStartEndMarkers("/*", "*/", Inclusion.INCLUDE)
			.addStartEndMarkers("//", '\n', Inclusion.EXCLUDE)
			.build();
		return commentParser;
	}


	public static final CharPrecondition createCommentParser(EnumSet<CommentStyle> style) {
		StringBoundedParserBuilder commentParser = new StringBoundedParserBuilder();
		int markerCount = 0;

		if(style.contains(CommentStyle.MULTILINE_C_STYLE)) {
			commentParser.addStartEndMarkers("/*", "*/", Inclusion.INCLUDE);
			markerCount++;
		}
		if(style.contains(CommentStyle.END_OF_LINE)) {
			commentParser.addStartEndMarkers("//", '\n', Inclusion.EXCLUDE);
			markerCount++;
		}
		if(style.contains(CommentStyle.XML_COMMENT)) {
			commentParser.addStartEndMarkers("<!--", "-->", Inclusion.INCLUDE);
			markerCount++;
		}

		if(markerCount == 0) {
			EnumSet<CommentStyle> validCommentSet = EnumSet.of(CommentStyle.MULTILINE_C_STYLE, CommentStyle.END_OF_LINE, CommentStyle.XML_COMMENT);
			throw new IllegalArgumentException("comment parser style enum set '" + style + "' is not valid, must contain at least one of '" + validCommentSet + "'");
		}

		return commentParser.build();
	}

}
