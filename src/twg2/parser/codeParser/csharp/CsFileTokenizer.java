package twg2.parser.codeParser.csharp;

import lombok.val;
import twg2.parser.Inclusion;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.tokenizers.CodeBlockTokenizer;
import twg2.parser.tokenizers.CodeStringTokenizer;
import twg2.parser.tokenizers.CodeTokenizerBuilder;
import twg2.parser.tokenizers.CommentTokenizer;
import twg2.parser.tokenizers.IdentifierTokenizer;
import twg2.parser.tokenizers.NumberTokenizer;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.StringBoundedParserBuilder;
import twg2.text.tokenizer.StringParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public class CsFileTokenizer {

	public static CodeTokenizerBuilder<CodeLanguageOptions.CSharp> createFileParser() {
		val identifierParser = IdentifierTokenizer.createIdentifierWithGenericTypeTokenizer();
		val numericLiteralParser = NumberTokenizer.createNumericLiteralTokenizer();

		val parser = new CodeTokenizerBuilder<>(CodeLanguageOptions.C_SHARP)
			.addParser(CommentTokenizer.createCommentTokenizer(CommentStyle.multiAndSingleLine()), CodeTokenType.COMMENT)
			.addParser(CodeStringTokenizer.createStringTokenizerForCSharp(), CodeTokenType.STRING)
			.addParser(CodeBlockTokenizer.createBlockTokenizer('{', '}'), CodeTokenType.BLOCK)
			.addParser(CodeBlockTokenizer.createBlockTokenizer('(', ')'), CodeTokenType.BLOCK)
			.addParser(createAnnotationTokenizer(), CodeTokenType.BLOCK)
			.addParser(identifierParser, (text, off, len) -> {
				return CsKeyword.check.isKeyword(text.toString()) ? CodeTokenType.KEYWORD : CodeTokenType.IDENTIFIER; // possible bad performance
			})
			.addParser(createOperatorTokenizer(), CodeTokenType.OPERATOR)
			.addParser(createSeparatorTokenizer(), CodeTokenType.SEPARATOR)
			.addParser(numericLiteralParser, CodeTokenType.NUMBER);

		return parser;
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
