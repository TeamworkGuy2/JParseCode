package codeParser.csharp;

import lombok.val;
import parser.Inclusion;
import parser.StringBoundedParserBuilder;
import parser.StringParserBuilder;
import parser.condition.Precondition;
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
public class CSharpClassParser {

	public static CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguageOptions.CSharp> parse(ParseInput params) {
		try {
			val parser = new ParserBuilder()
				.addConstParser(CommentParser.createCommentParser(CommentStyle.multiAndSingleLine()), CodeFragmentType.COMMENT)
				.addConstParser(CodeStringParser.createStringParserForCSharp(), CodeFragmentType.STRING)
				.addConstParser(CodeBlockParser.createBlockParser(), CodeFragmentType.BLOCK)
				.addConstParser(CodeBlockParser.createBlockParser('(', ')'), CodeFragmentType.BLOCK)
				.addConstParser(createAnnotationParser(), CodeFragmentType.BLOCK)
				.addParser(IdentifierParser.createIdentifierWithGenericTypeParser(), (text, off, len) -> {
					return CSharpKeyword.isKeyword(text.toString()) ? CodeFragmentType.KEYWORD : CodeFragmentType.IDENTIFIER; // possible bad performance
				})
				.addConstParser(createOperatorParser(), CodeFragmentType.OPERATOR);
			return parser.buildAndParse(params.getSrc(), CodeLanguageOptions.C_SHARP);
		} catch(Exception e) {
			if(params.getErrorHandler() != null) {
				params.getErrorHandler().accept(e);
			}
			throw e;
		}
	}


	static Precondition createAnnotationParser() {
		Precondition annotationParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('[', '[', ']', Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return annotationParser;
	}


	// TODO only partially implemented
	static Precondition createOperatorParser() {
		Precondition operatorParser = new StringParserBuilder()
			.addCharLiteralMarker('+')
			.addCharLiteralMarker('-')
			.addCharLiteralMarker('=')
			.build();
		return operatorParser;
	}

}
