package codeParser.parsers;

import parser.Inclusion;
import parser.text.CharPrecondition;
import parser.text.StringBoundedParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public final class CodeStringParser {

	private CodeStringParser() { throw new AssertionError("cannot instantiate static class CodeStringParser"); }


	public static final CharPrecondition createStringParserForJava() {
		CharPrecondition stringParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers('\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}


	// TODO make parser work with all types of C# string literals
	public static final CharPrecondition createStringParserForCSharp() {
		CharPrecondition stringParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers('\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}


	public static final CharPrecondition createStringParserForJavascript() {
		CharPrecondition stringParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers('\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}

}
