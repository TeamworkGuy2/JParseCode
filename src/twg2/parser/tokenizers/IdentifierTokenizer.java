package twg2.parser.tokenizers;

import java.util.Arrays;

import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.text.tokenizer.CharConditionPipe;
import twg2.text.tokenizer.CharConditions;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.CharParserMatchableFactory;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringConditions;

/** Static methods for creating C language like identifier parsers (i.e. parsing strings '_myVar', '$num', 'camelCaseStr', etc.)
 * @author TeamworkGuy2
 * @since 2015-11-27
 */
public class IdentifierTokenizer {

	private IdentifierTokenizer() { throw new AssertionError("cannot instantiate static class IdentifierTokenizer"); }


	public static CharParserFactory createIdentifierWithGenericTypeTokenizer(int maxGenericTypeDepth) {
		var typeStatementCond = GenericTypeTokenizer.createGenericTypeTokenizer(maxGenericTypeDepth, IdentifierTokenizer::createCompoundIdentifierTokenizer);
		return new CharParserMatchableFactory<>("compound identifier with optional generic type", false, new CharParserMatchable[] { typeStatementCond });
	}


	/**
	 * @return a basic parser for a string of contiguous characters matching those allowed in identifiers (i.e. 'FancyObject.LoopCount', '$thing', or '_stspr')
	 */
	public static CharConditions.BaseCharParserMatchable createIdentifierTokenizer() {
		return CharConditions.Identifier.newInstance("identifier", false);
	}


	/**
	 * @return a compound identifier parser (i.e. can parse 'Aa.Bb.Cc' as one identifier token')
	 */
	public static CharParserMatchable createCompoundIdentifierTokenizer() {
		var identifierParser = createIdentifierTokenizer();

		return CharConditionPipe.createPipeOptionalSuffix("compound identifier (nullable)",
			Arrays.asList(identifierParser),
			Arrays.asList(new CharConditions.Literal("nullable '?' suffix", CharArrayList.of('?'), Inclusion.INCLUDE)
					, new StringConditions.Literal("params '...' suffix", new String[] { "..." }, Inclusion.INCLUDE))
		);
	}

}
