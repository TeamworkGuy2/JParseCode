package twg2.parser.codeParser.parsers;

import twg2.parser.Inclusion;
import twg2.parser.text.CharParserFactory;
import twg2.parser.text.StringBoundedParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public final class CodeStringParser {

	private CodeStringParser() { throw new AssertionError("cannot instantiate static class CodeStringParser"); }


	public static final CharParserFactory createStringParserForJava() {
		CharParserFactory stringParser = new StringBoundedParserBuilder("Java string")
			.addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers("char literal", '\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}


	// TODO make parser work with all types of C# string literals
	public static final CharParserFactory createStringParserForCSharp() {
		CharParserFactory stringParser = new StringBoundedParserBuilder("C# string")
			.addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers("char literal", '\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}


	public static final CharParserFactory createStringParserForJavascript() {
		CharParserFactory stringParser = new StringBoundedParserBuilder("JS string")
			.addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers("char literal", '\'', '\\', '\'', Inclusion.INCLUDE)
			.build();
		return stringParser;
	}

}
