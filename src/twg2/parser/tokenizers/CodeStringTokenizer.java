package twg2.parser.tokenizers;

import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-5-29
 */
public final class CodeStringTokenizer {

	private CodeStringTokenizer() { throw new AssertionError("cannot instantiate static class CodeStringParser"); }


	public static CharParserFactory createStringTokenizerForJava(boolean reusable) {
		CharParserFactory stringParser = new StringParserBuilder("Java string")
			.addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers("char literal", '\'', '\\', '\'', Inclusion.INCLUDE)
			.build(reusable);
		return stringParser;
	}


	// TODO make parser work with all types of C# string literals
	public static CharParserFactory createStringTokenizerForCSharp(boolean reusable) {
		CharParserFactory stringParser = new StringParserBuilder("C# string")
			.addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers("char literal", '\'', '\\', '\'', Inclusion.INCLUDE)
			.build(reusable);
		return stringParser;
	}


	public static CharParserFactory createStringTokenizerForJavascript(boolean reusable) {
		CharParserFactory stringParser = new StringParserBuilder("JS string")
			.addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE)
			.addStartEndNotPrecededByMarkers("char literal", '\'', '\\', '\'', Inclusion.INCLUDE)
			.build(reusable);
		return stringParser;
	}

}
