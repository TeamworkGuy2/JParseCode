package codeParser.csharp;

import java.io.IOException;
import java.io.UncheckedIOException;

import lombok.val;
import parser.Inclusion;
import parser.StringBoundedParserBuilder;
import parser.condition.Precondition;
import codeParser.CodeFile;
import codeParser.CodeFragmentType;
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

	public static CodeFile<DocumentFragmentText<CodeFragmentType>> parse(ParseInput params) {
		try {
			val parser = new ParserBuilder()
				.addConstParser(CommentParser.createCommentParser(CommentStyle.multiAndSingleLine()), CodeFragmentType.COMMENT)
				.addConstParser(CodeStringParser.createStringParserForCSharp(), CodeFragmentType.STRING)
				.addConstParser(CodeBlockParser.createBlockParser(), CodeFragmentType.BLOCK)
				.addConstParser(CodeBlockParser.createBlockParser('(', ')'), CodeFragmentType.BLOCK)
				.addConstParser(createAnnotationParser(), CodeFragmentType.BLOCK)
				.addParser(IdentifierParser.createIdentifierWithGenericTypeParser(), (text, off, len) -> {
					return CSharpKeyword.isKeyword(text.toString()) ? CodeFragmentType.KEYWORD : CodeFragmentType.IDENTIFIER; // possible bad performance
				});
			return parser.buildAndParse(params.getSrc());
		} catch(IOException e) {
			if(params.getErrorHandler() != null) {
				params.getErrorHandler().accept(e);
			}
			throw new UncheckedIOException(e);
		}
	}


	static Precondition createAnnotationParser() {
		Precondition annotationParser = new StringBoundedParserBuilder()
			.addStartEndNotPrecededByMarkers('[', '[', ']', Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return annotationParser;
	}

}
