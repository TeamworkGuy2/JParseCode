package codeParser.parsers;

import parser.Inclusion;
import parser.text.CharPrecondition;
import parser.text.StringBoundedParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CodeBlockParser {

	public static CharPrecondition createBlockParser() {
		return createBlockParser('{', '}');
	}


	public static CharPrecondition createBlockParser(char startChar, char endChar) {
		CharPrecondition commentParser = new StringBoundedParserBuilder()
			.addStartEndMarkers(startChar, endChar, Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return commentParser;
	}

}
