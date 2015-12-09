package codeParser.parsers;

import java.util.Arrays;

import lombok.val;
import parser.Inclusion;
import parser.StringParserBuilder;
import parser.condition.CharConditions;
import parser.condition.ConditionPipeFilter;
import parser.condition.ParserCondition;
import parser.condition.Precondition;
import parser.condition.PreconditionImpl;
import twg2.ranges.CharSearchSet;

/**
 * @author TeamworkGuy2
 * @since 2015-11-27
 */
public class IdentifierParser {
	static int genericTypeDepth = 2;

	public static Precondition createIdentifierWithGenericTypeParser() {
		Precondition identifierWithGenericTypeParser = new PreconditionImpl<>(false, GenericTypeParser.createGenericTypeStatementCondition(genericTypeDepth, IdentifierParser::createCompoundIdentifierCondition));
		return identifierWithGenericTypeParser;
	}


	public static Precondition createIdentifierParser() {
		Precondition identifierParser = new StringParserBuilder()
			.addConditionMatcher(createIdentifierCondition())
			.build();
		return identifierParser;
	}


	/**
	 * @return a basic parser for a string of contiguous characters matching those allowed in identifiers (i.e. 'mySpecialLoopCount', '$thing', or '_stspr')
	 */
	public static CharConditions.BaseCharFilter createIdentifierCondition() {
		CharSearchSet charSet = new CharSearchSet();
		charSet.addChar('$');
		charSet.addChar('_');
		charSet.addRange('a', 'z');
		charSet.addRange('A', 'Z');

		val cond = new CharConditions.BaseCharFilter(charSet::contains, charSet.toCharList().toArray(), Inclusion.INCLUDE, charSet);
		CharConditions.setupContainsCharFilter(cond);
		return cond;
	}


	/**
	 * @return a compound identifier parser (i.e. can parse 'Aa.Bb.Cc' as one identifier token')
	 */
	public static ParserCondition createCompoundIdentifierCondition() {
		val identifierParser = Arrays.asList(IdentifierParser.createIdentifierCondition());
		val separatorParser = Arrays.asList(CharConditions.charLiteralFactory().create('.'));
		return ConditionPipeFilter.createPipeRepeatableSeparator(identifierParser, separatorParser);
	}

}
