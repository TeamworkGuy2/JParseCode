package twg2.parser.codeParser.csharp;

import lombok.val;
import twg2.parser.Inclusion;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.ParseInput;
import twg2.parser.codeParser.ParserBuilder;
import twg2.parser.fragment.CodeFragmentType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.text.CharParserFactory;
import twg2.parser.text.StringBoundedParserBuilder;
import twg2.parser.text.StringParserBuilder;
import twg2.parser.tokenizers.CodeBlockTokenizer;
import twg2.parser.tokenizers.CodeStringTokenizer;
import twg2.parser.tokenizers.CommentTokenizer;
import twg2.parser.tokenizers.IdentifierTokenizer;
import twg2.parser.tokenizers.NumberTokenizer;

/**
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public class CsFileTokenizer {

	public static CodeFileSrc<CodeLanguageOptions.CSharp> parse(ParseInput params) {
		try {
			val identifierParser = IdentifierTokenizer.createIdentifierWithGenericTypeTokenizer();
			val numericLiteralParser = NumberTokenizer.createNumericLiteralTokenizer();

			val parser = new ParserBuilder()
				.addConstParser(CommentTokenizer.createCommentTokenizer(CommentStyle.multiAndSingleLine()), CodeFragmentType.COMMENT)
				.addConstParser(CodeStringTokenizer.createStringTokenizerForCSharp(), CodeFragmentType.STRING)
				.addConstParser(CodeBlockTokenizer.createBlockTokenizer('{', '}'), CodeFragmentType.BLOCK)
				.addConstParser(CodeBlockTokenizer.createBlockTokenizer('(', ')'), CodeFragmentType.BLOCK)
				.addConstParser(createAnnotationTokenizer(), CodeFragmentType.BLOCK)
				.addParser(identifierParser, (text, off, len) -> {
					return CsKeyword.check.isKeyword(text.toString()) ? CodeFragmentType.KEYWORD : CodeFragmentType.IDENTIFIER; // possible bad performance
				})
				.addConstParser(createOperatorTokenizer(), CodeFragmentType.OPERATOR)
				.addConstParser(createSeparatorTokenizer(), CodeFragmentType.SEPARATOR)
				.addConstParser(numericLiteralParser, CodeFragmentType.NUMBER);
			return parser.buildAndParse(params.getSrc(), CodeLanguageOptions.C_SHARP, params.getFileName(), true);
		} catch(Exception e) {
			if(params.getErrorHandler() != null) {
				params.getErrorHandler().accept(e);
			}
			throw e;
		}
	}


	public static CharParserFactory createAnnotationTokenizer() {
		CharParserFactory annotationParser = new StringBoundedParserBuilder("C# annotation")
			.addStartEndNotPrecededByMarkers("block [ ]", '[', '[', ']', 1, Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return annotationParser;
	}


	// TODO only partially implemented
	public static CharParserFactory createOperatorTokenizer() {
		CharParserFactory operatorParser = new StringParserBuilder("C# operator")
			.addCharLiteralMarker("+", '+')
			.addCharLiteralMarker("-", '-')
			//.addCharLiteralMarker("*", '*') // causes issue parsing comments..?
			.addCharLiteralMarker("=", '=')
			.addCharLiteralMarker("?", '?')
			.addCharLiteralMarker(":", ':')
			.build();
		return operatorParser;
	}


	// TODO couldn't get this working with identifier parser which needs to parse ', ' in strings like 'Map<String, String>'
	public static CharParserFactory createSeparatorTokenizer() {
		CharParserFactory annotationParser = new StringParserBuilder("C# separator")
			//.addCharLiteralMarker(',')
			.addCharLiteralMarker(";", ';')
			.build();
		return annotationParser;
	}

}
