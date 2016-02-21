package twg2.parser.codeParser.java;

import lombok.val;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.codeParser.NumberParser;
import twg2.parser.codeParser.ParseInput;
import twg2.parser.codeParser.ParserBuilder;
import twg2.parser.codeParser.parsers.CodeBlockParser;
import twg2.parser.codeParser.parsers.CodeStringParser;
import twg2.parser.codeParser.parsers.CommentParser;
import twg2.parser.codeParser.parsers.IdentifierParser;
import twg2.parser.text.CharParserFactory;
import twg2.parser.text.StringBoundedParserBuilder;
import twg2.parser.text.StringParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public class JavaClassParser {

	public static CodeFileSrc<CodeLanguageOptions.Java> parse(ParseInput params) {
		try {
			val identifierParser = IdentifierParser.createIdentifierWithGenericTypeParser();
			val numericLiteralParser = NumberParser.createNumericLiteralParser();

			val parser = new ParserBuilder()
				.addConstParser(CommentParser.createCommentParser(CommentStyle.multiAndSingleLine()), CodeFragmentType.COMMENT)
				.addConstParser(CodeStringParser.createStringParserForJava(), CodeFragmentType.STRING)
				.addConstParser(CodeBlockParser.createBlockParser(), CodeFragmentType.BLOCK)
				.addConstParser(CodeBlockParser.createBlockParser('(', ')'), CodeFragmentType.BLOCK)
				// no annotation parser, instead we parse
				.addParser(identifierParser, (text, off, len) -> {
					return JavaKeyword.check.isKeyword(text.toString()) ? CodeFragmentType.KEYWORD : CodeFragmentType.IDENTIFIER; // possible bad performance
				})
				.addConstParser(createOperatorParser(), CodeFragmentType.OPERATOR)
				.addConstParser(createSeparatorParser(), CodeFragmentType.SEPARATOR)
				.addConstParser(numericLiteralParser, CodeFragmentType.NUMBER);
			return parser.buildAndParse(params.getSrc(), CodeLanguageOptions.JAVA, params.getFileName());
		} catch(Exception e) {
			if(params.getErrorHandler() != null) {
				params.getErrorHandler().accept(e);
			}
			throw e;
		}
	}


	// TODO only partially implemented
	static CharParserFactory createOperatorParser() {
		CharParserFactory operatorParser = new StringParserBuilder("Java operator")
			.addCharLiteralMarker("+", '+')
			.addCharLiteralMarker("-", '-')
			.addCharLiteralMarker("=", '=')
			.addCharLiteralMarker("?", '?')
			.addCharLiteralMarker(":", ':')
			.build();
		return operatorParser;
	}


	// TODO couldn't get this working with identifier parser which needs to parse ', ' in strings like 'Map<String, String>'
	static CharParserFactory createSeparatorParser() {
		CharParserFactory annotationParser = new StringBoundedParserBuilder("Java separator")
			//.addCharLiteralMarker(',')
			.addCharLiteralMarker(";", ';')
			.addCharLiteralMarker("@", '@')
			.addStringLiteralMarker("::", "::") // TODO technically not a separator, integrate with identifier parser
			.isCompound(false)
			.build();
		return annotationParser;
	}

}
