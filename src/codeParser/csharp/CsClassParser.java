package codeParser.csharp;

import lombok.val;
import parser.Inclusion;
import parser.text.CharPrecondition;
import parser.text.StringBoundedParserBuilder;
import parser.text.StringParserBuilder;
import codeParser.CodeFileSrc;
import codeParser.CodeFragmentType;
import codeParser.CodeLanguageOptions;
import codeParser.CommentStyle;
import codeParser.ParseInput;
import codeParser.ParserBuilder;
import codeParser.parsers.CodeBlockParser;
import codeParser.parsers.CodeStringParser;
import codeParser.parsers.CommentParser;
import codeParser.parsers.IdentifierParser;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public class CsClassParser {

	public static CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguageOptions.CSharp> parse(ParseInput params) {
		try {
			val identifierParser = IdentifierParser.createIdentifierWithGenericTypeParser();

			val parser = new ParserBuilder()
				.addConstParser(CommentParser.createCommentParser(CommentStyle.multiAndSingleLine()), CodeFragmentType.COMMENT)
				.addConstParser(CodeStringParser.createStringParserForCSharp(), CodeFragmentType.STRING)
				.addConstParser(CodeBlockParser.createBlockParser(), CodeFragmentType.BLOCK)
				.addConstParser(CodeBlockParser.createBlockParser('(', ')'), CodeFragmentType.BLOCK)
				.addConstParser(createAnnotationParser(), CodeFragmentType.BLOCK)
				.addParser(identifierParser, (text, off, len) -> {
					return CsKeyword.isKeyword(text.toString()) ? CodeFragmentType.KEYWORD : CodeFragmentType.IDENTIFIER; // possible bad performance
				})
				.addConstParser(createOperatorParser(), CodeFragmentType.OPERATOR)
				.addConstParser(createSeparatorParser(), CodeFragmentType.SEPARATOR);
			return parser.buildAndParse(params.getSrc(), CodeLanguageOptions.C_SHARP);
		} catch(Exception e) {
			if(params.getErrorHandler() != null) {
				params.getErrorHandler().accept(e);
			}
			throw e;
		}
	}


	static CharPrecondition createAnnotationParser() {
		CharPrecondition annotationParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('[', '[', ']', Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return annotationParser;
	}


	// TODO only partially implemented
	static CharPrecondition createOperatorParser() {
		CharPrecondition operatorParser = new StringParserBuilder()
			.addCharLiteralMarker('+')
			.addCharLiteralMarker('-')
			.addCharLiteralMarker('=')
			.addCharLiteralMarker('?')
			.build();
		return operatorParser;
	}


	// TODO couldn't get this working with identifier parser which needs to parse ', ' in strings like 'Map<String, String>'
	static CharPrecondition createSeparatorParser() {
		CharPrecondition annotationParser = new StringBoundedParserBuilder()
			//.addCharLiteralMarker(',')
			.addCharLiteralMarker(';')
			.isCompound(false)
			.build();
		return annotationParser;
	}

}
