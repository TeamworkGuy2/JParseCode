package twg2.parser.tokenizers;

import twg2.parser.Inclusion;
import twg2.parser.text.CharParserFactory;
import twg2.parser.text.StringBoundedParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CodeBlockTokenizer {

	public static CharParserFactory createBlockTokenizer(char startChar, char endChar) {
		CharParserFactory commentParser = new StringBoundedParserBuilder("block")
			.addStartEndMarkers("block " + startChar + " " + endChar, startChar, endChar, Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return commentParser;
	}

}
