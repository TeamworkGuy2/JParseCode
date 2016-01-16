package twg2.parser.codeParser.csharp;

import lombok.val;
import twg2.parser.Inclusion;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.CommentStyle;
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
public class CsClassParser {

	public static CodeFileSrc<CodeLanguageOptions.CSharp> parse(ParseInput params) {
		try {
			val identifierParser = IdentifierParser.createIdentifierWithGenericTypeParser();

			val parser = new ParserBuilder()
				.addConstParser(CommentParser.createCommentParser(CommentStyle.multiAndSingleLine()), CodeFragmentType.COMMENT)
				.addConstParser(CodeStringParser.createStringParserForCSharp(), CodeFragmentType.STRING)
				.addConstParser(CodeBlockParser.createBlockParser(), CodeFragmentType.BLOCK)
				.addConstParser(CodeBlockParser.createBlockParser('(', ')'), CodeFragmentType.BLOCK)
				.addConstParser(createAnnotationParser(), CodeFragmentType.BLOCK)
				.addParser(identifierParser, (text, off, len) -> {
					return CsKeyword.check.isKeyword(text.toString()) ? CodeFragmentType.KEYWORD : CodeFragmentType.IDENTIFIER; // possible bad performance
				})
				.addConstParser(createOperatorParser(), CodeFragmentType.OPERATOR)
				.addConstParser(createSeparatorParser(), CodeFragmentType.SEPARATOR);
			return parser.buildAndParse(params.getSrc(), CodeLanguageOptions.C_SHARP, params.getFileName());
		} catch(Exception e) {
			if(params.getErrorHandler() != null) {
				params.getErrorHandler().accept(e);
			}
			throw e;
		}
	}


	static CharParserFactory createAnnotationParser() {
		CharParserFactory annotationParser = new StringBoundedParserBuilder("C# annotation")
			.addStartEndNotPrecededByMarkers("block [ ]", '[', '[', ']', Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return annotationParser;
	}


	// TODO only partially implemented
	static CharParserFactory createOperatorParser() {
		CharParserFactory operatorParser = new StringParserBuilder("C# operator")
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
		CharParserFactory annotationParser = new StringBoundedParserBuilder("C# separator")
			//.addCharLiteralMarker(',')
			.addCharLiteralMarker(";", ';')
			.isCompound(false)
			.build();
		return annotationParser;
	}

}
