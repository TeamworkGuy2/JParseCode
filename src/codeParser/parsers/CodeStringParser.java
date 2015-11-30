package codeParser.parsers;

import parser.Inclusion;
import parser.StringBoundedParserBuilder;
import parser.condition.Precondition;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public final class CodeStringParser {

	private CodeStringParser() { throw new AssertionError("cannot instantiate static class CodeStringParser"); }


	public static final Precondition createStringParserForJava() {
		Precondition stringParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers('\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}


	// TODO make parser work with all types of C# string literals
	public static final Precondition createStringParserForCSharp() {
		Precondition stringParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers('\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}


	public static final Precondition createStringParserForJavascript() {
		Precondition stringParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers('\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}

}
