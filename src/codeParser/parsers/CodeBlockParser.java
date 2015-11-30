package codeParser.parsers;

import parser.Inclusion;
import parser.StringBoundedParserBuilder;
import parser.condition.Precondition;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CodeBlockParser {

	public static Precondition createBlockParser() {
		return createBlockParser('{', '}');
	}


	public static Precondition createBlockParser(char startChar, char endChar) {
		Precondition commentParser = new StringBoundedParserBuilder()
			.addStartEndMarkers(startChar, endChar, Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return commentParser;
	}

}
