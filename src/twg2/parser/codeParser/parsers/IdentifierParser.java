package twg2.parser.codeParser.parsers;

import java.util.Arrays;

import lombok.val;
import twg2.parser.Inclusion;
import twg2.parser.text.CharConditionPipe;
import twg2.parser.text.CharConditions;
import twg2.parser.text.CharParserCondition;
import twg2.parser.text.CharPrecondition;
import twg2.parser.text.CharPreconditionImpl;
import twg2.parser.text.StringParserBuilder;
import twg2.ranges.CharSearchSet;

/**
 * @author TeamworkGuy2
 * @since 2015-11-27
 */
public class IdentifierParser {
	static int genericTypeDepth = 2;

	public static CharPrecondition createIdentifierWithGenericTypeParser() {
		CharPrecondition identifierWithGenericTypeParser = new CharPreconditionImpl<>(false, GenericTypeParser.createGenericTypeStatementCondition(genericTypeDepth, IdentifierParser::createCompoundIdentifierCondition));
		return identifierWithGenericTypeParser;
	}


	public static CharPrecondition createIdentifierParser() {
		CharPrecondition identifierParser = new StringParserBuilder()
			.addConditionMatcher(createIdentifierCondition())
			.build();
		return identifierParser;
	}


	/**
	 * @return a basic parser for a string of contiguous characters matching those allowed in identifiers (i.e. 'mySpecialLoopCount', '$thing', or '_stspr')
	 */
	public static CharConditions.BaseCharFilter createIdentifierCondition() {
		CharSearchSet firstCharSet = new CharSearchSet();
		firstCharSet.addChar('$');
		firstCharSet.addChar('_');
		firstCharSet.addRange('a', 'z');
		firstCharSet.addRange('A', 'Z');

		CharSearchSet charSet = firstCharSet.copy();
		charSet.addRange('0', '9');

		val cond = new CharConditions.BaseCharFilter(charSet::contains, firstCharSet::contains, charSet.toCharList().toArray(), Inclusion.INCLUDE, charSet);
		CharConditions.setupContainsCharFirstSpecialFilter(cond);
		return cond;
	}


	/**
	 * @return a compound identifier parser (i.e. can parse 'Aa.Bb.Cc' as one identifier token')
	 */
	public static CharParserCondition createCompoundIdentifierCondition() {
		val identifierParser = Arrays.asList(IdentifierParser.createIdentifierCondition());
		val separatorParser = Arrays.asList(CharConditions.charLiteralFactory().create('.'));
		return CharConditionPipe.createPipeRepeatableSeparator(identifierParser, separatorParser);
	}

}
