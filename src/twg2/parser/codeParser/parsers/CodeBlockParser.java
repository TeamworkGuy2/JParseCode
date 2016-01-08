package twg2.parser.codeParser.parsers;

import twg2.parser.Inclusion;
import twg2.parser.text.CharPrecondition;
import twg2.parser.text.StringBoundedParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CodeBlockParser {

	public static CharPrecondition createBlockParser() {
		return createBlockParser('{', '}');
	}


	public static CharPrecondition createBlockParser(char startChar, char endChar) {
		CharPrecondition commentParser = new StringBoundedParserBuilder("block")
			.addStartEndMarkers("block " + startChar + " " + endChar, startChar, endChar, Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return commentParser;
	}

}
