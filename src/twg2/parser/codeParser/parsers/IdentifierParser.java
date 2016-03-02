package twg2.parser.codeParser.parsers;

import java.util.Arrays;

import lombok.val;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.collections.tuple.Tuples;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.text.CharConditionPipe;
import twg2.parser.text.CharConditions;
import twg2.parser.text.CharParserFactory;
import twg2.parser.text.CharParserPlainFactoryImpl;
import twg2.parser.text.StringParserBuilder;
import twg2.ranges.CharSearchSet;

/**
 * @author TeamworkGuy2
 * @since 2015-11-27
 */
public class IdentifierParser {
	static int genericTypeDepth = 3;

	public static CharParserFactory createIdentifierWithGenericTypeParser() {
		val typeStatementCond = GenericTypeParser.createGenericTypeParser(genericTypeDepth, IdentifierParser::createCompoundIdentifierParser);
		CharParserFactory identifierWithGenericTypeParser = new CharParserPlainFactoryImpl<>("compound identifier with optional generic type", false, Tuples.of(typeStatementCond.getFirstCharMatcher(), typeStatementCond));
		return identifierWithGenericTypeParser;
	}


	public static CharParserFactory createIdentifierParser() {
		CharParserFactory identifierParser = new StringParserBuilder("identifier")
			.addConditionMatcher(newIdentifierParser())
			.build();
		return identifierParser;
	}


	/**
	 * @return a basic parser for a string of contiguous characters matching those allowed in identifiers (i.e. 'mySpecialLoopCount', '$thing', or '_stspr')
	 */
	public static CharConditions.BaseCharParserMatchable newIdentifierParser() {
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
	public static CharParserMatchable createCompoundIdentifierParser() {
		val identifierParser = Arrays.asList(newIdentifierParser());
		val separatorParser = Arrays.asList(new CharConditions.Literal("identifier namespace separator", CharArrayList.of('.'), Inclusion.INCLUDE));
		return CharConditionPipe.createPipeRepeatableSeparator("compound identifier", identifierParser, separatorParser);
	}

}
