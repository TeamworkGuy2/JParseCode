package twg2.parser.codeParser.java;

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
import twg2.text.tokenizer.StringParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public class JavaFileTokenizer {

	public static CodeTokenizerBuilder<CodeLanguageOptions.Java> createFileParser() {
		var identifierParser = IdentifierTokenizer.createIdentifierWithGenericTypeTokenizer();
		var numericLiteralParser = NumberTokenizer.createNumericLiteralTokenizer();

		var parser = new CodeTokenizerBuilder<>(CodeLanguageOptions.JAVA)
			.addParser(CommentTokenizer.createCommentTokenizer(CommentStyle.multiAndSingleLine()), CodeTokenType.COMMENT)
			.addParser(CodeStringTokenizer.createStringTokenizerForJava(), CodeTokenType.STRING)
			.addParser(CodeBlockTokenizer.createBlockTokenizer('{', '}'), CodeTokenType.BLOCK)
			.addParser(CodeBlockTokenizer.createBlockTokenizer('(', ')'), CodeTokenType.BLOCK)
			// no annotation parser, instead we parse
			.addParser(identifierParser, (text, off, len) -> {
				return JavaKeyword.check.isKeyword(text.toString()) ? CodeTokenType.KEYWORD : CodeTokenType.IDENTIFIER; // possible bad performance
			})
			.addParser(createOperatorTokenizer(), CodeTokenType.OPERATOR)
			.addParser(createSeparatorTokenizer(), CodeTokenType.SEPARATOR)
			.addParser(numericLiteralParser, CodeTokenType.NUMBER);

		return parser;
	}


	// TODO only partially implemented
	static CharParserFactory createOperatorTokenizer() {
		CharParserFactory operatorParser = new StringParserBuilder("Java operator")
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
	static CharParserFactory createSeparatorTokenizer() {
		CharParserFactory annotationParser = new StringParserBuilder("Java separator")
			//.addCharLiteralMarker(',')
			.addCharLiteralMarker(";", ';')
			.addCharLiteralMarker("@", '@')
			.addStringLiteralMarker("::", "::") // TODO technically not a separator, integrate with identifier parser
			.build();
		return annotationParser;
	}

}
