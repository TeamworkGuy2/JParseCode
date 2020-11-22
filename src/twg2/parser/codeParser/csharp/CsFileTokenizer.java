package twg2.parser.codeParser.csharp;

import twg2.collections.dataStructures.PairList;
import twg2.parser.codeParser.CommentStyle;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.textFragment.TextTransformer;
import twg2.parser.tokenizers.CodeBlockTokenizer;
import twg2.parser.tokenizers.CodeStringTokenizer;
import twg2.parser.tokenizers.CodeTokenizer;
import twg2.parser.tokenizers.CommentTokenizer;
import twg2.parser.tokenizers.IdentifierTokenizer;
import twg2.parser.tokenizers.NumberTokenizer;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringParserBuilder;

import static twg2.parser.tokenizers.CodeTokenizer.ofType;

/**
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public class CsFileTokenizer {
	/** Supported depth of recursive generic type tokenization (i.e. Map<String, List<String>> has a depth of 2).
	 * !!!!==== NOTE: this has a huge impact on performance (last tested 0.20.0 - 2020-11-21) ====!!!!
	 */
	public static int maxGenericTypeDepth = 3;
	public static int cnt = 0;


	public static CodeTokenizer createCsTokenizer() {
		return CodeTokenizer.createTokenizer(CodeLanguageOptions.C_SHARP, createCsTokenizers());
	}


	// this gets call once per file parsed
	public static PairList<CharParserFactory, TextTransformer<CodeTokenType>> createCsTokenizers() {
		var identifierParser = IdentifierTokenizer.createIdentifierWithGenericTypeTokenizer(maxGenericTypeDepth);
		var numericLiteralParser = NumberTokenizer.createNumericLiteralTokenizer();

		var parsers = new PairList<CharParserFactory, TextTransformer<CodeTokenType>>();

		parsers.add(CommentTokenizer.createCommentTokenizer(CommentStyle.multiAndSingleLine()), ofType(CodeTokenType.COMMENT));
		parsers.add(CodeStringTokenizer.createStringTokenizerForCSharp(), ofType(CodeTokenType.STRING));
		//parsers.add(CodeBlockTokenizer.createBlockTokenizer('{', '('), ofType(CodeTokenType.BLOCK)); // this appears ~8% SLOWER in total program time (2020-11-21)
		parsers.add(CodeBlockTokenizer.createBlockTokenizer('{'), ofType(CodeTokenType.BLOCK));
		parsers.add(CodeBlockTokenizer.createBlockTokenizer('('), ofType(CodeTokenType.BLOCK));
		parsers.add(createAnnotationTokenizer(), ofType(CodeTokenType.BLOCK));
		parsers.add(identifierParser, (text, off, len) -> {
			cnt++;
			// TODO performance
			return CsKeyword.check.isKeyword(text.toString()) ? CodeTokenType.KEYWORD : CodeTokenType.IDENTIFIER; // possible bad performance
		});
		parsers.add(createOperatorTokenizer(), ofType(CodeTokenType.OPERATOR));
		parsers.add(createSeparatorTokenizer(), ofType(CodeTokenType.SEPARATOR));
		parsers.add(numericLiteralParser, ofType(CodeTokenType.NUMBER));

		return parsers;
	}


	public static CharParserFactory createAnnotationTokenizer() {
		CharParserFactory annotationParser = new StringParserBuilder("C# annotation")
			.addStartEndNotPrecededByMarkers("block [ ]", '[', '[', ']', Inclusion.INCLUDE)
			.isCompound(true)
			.build();
		return annotationParser;
	}


	// TODO only partially implemented
	public static CharParserFactory createOperatorTokenizer() {
		CharParserFactory operatorParser = new StringParserBuilder("C# operator")
			//.addCharLiteralMarker("+-=?:", '+', '-', '=', '?', ':') // this is SLOWER!! IDK why!?! (2020-11-21)
			.addCharLiteralMarker("+", '+')
			.addCharLiteralMarker("-", '-')
			.addCharLiteralMarker("=", '=')
			.addCharLiteralMarker("?", '?')
			.addCharLiteralMarker(":", ':')
			//.addCharLiteralMarker("*", '*') // causes issue parsing comments..?
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
