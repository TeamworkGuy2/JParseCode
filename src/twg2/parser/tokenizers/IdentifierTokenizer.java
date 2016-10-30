package twg2.parser.tokenizers;

import java.util.Arrays;

import lombok.val;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.ranges.CharSearchSet;
import twg2.text.tokenizer.CharConditionPipe;
import twg2.text.tokenizer.CharConditions;
import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.CharParserMatchableFactory;
import twg2.text.tokenizer.StringParserBuilder;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2015-11-27
 */
public class IdentifierTokenizer {
	static int genericTypeDepth = 3;

	public static CharParserFactory createIdentifierWithGenericTypeTokenizer() {
		val typeStatementCond = GenericTypeTokenizer.createGenericTypeTokenizer(genericTypeDepth, IdentifierTokenizer::createCompoundIdentifierTokenizer);
		CharParserFactory identifierWithGenericTypeParser = new CharParserMatchableFactory<>("compound identifier with optional generic type", false, Tuples.of(typeStatementCond.getFirstCharMatcher(), typeStatementCond));
		return identifierWithGenericTypeParser;
	}


	public static CharParserFactory createIdentifierTokenizer() {
		CharParserFactory identifierParser = new StringParserBuilder("identifier")
			.addConditionMatcher(newIdentifierTokenizer())
			.build();
		return identifierParser;
	}


	/**
	 * @return a basic parser for a string of contiguous characters matching those allowed in identifiers (i.e. 'mySpecialLoopCount', '$thing', or '_stspr')
	 */
	public static CharConditions.BaseCharParserMatchable newIdentifierTokenizer() {
		CharSearchSet firstCharSet = new CharSearchSet();
		firstCharSet.addChar('$');
		firstCharSet.addChar('_');
		firstCharSet.addRange('a', 'z');
		firstCharSet.addRange('A', 'Z');

		CharSearchSet charSet = firstCharSet.copy();
		charSet.addRange('0', '9');

		val cond = new CharConditions.ContainsFirstSpecial("identifier", charSet::contains, firstCharSet::contains, firstCharSet.toCharList().toArray(), Inclusion.INCLUDE, charSet);
		return cond;
	}


	/**
	 * @return a compound identifier parser (i.e. can parse 'Aa.Bb.Cc' as one identifier token')
	 */
	public static CharParserMatchable createCompoundIdentifierTokenizer() {
		val identifierParser = Arrays.asList(newIdentifierTokenizer());
		val separatorParser = Arrays.asList(new CharConditions.Literal("identifier namespace separator", CharArrayList.of('.'), Inclusion.INCLUDE));
		return CharConditionPipe.createPipeRepeatableSeparator("compound identifier", identifierParser, separatorParser);
	}

}
