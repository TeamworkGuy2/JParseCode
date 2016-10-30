package twg2.parser.tokenizers;

import twg2.parser.Inclusion;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.StringBoundedParserBuilder;

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
