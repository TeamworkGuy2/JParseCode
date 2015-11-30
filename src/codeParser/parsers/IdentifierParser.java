package codeParser.parsers;

import lombok.val;
import parser.Inclusion;
import parser.StringParserBuilder;
import parser.condition.CharConditions;
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
		Precondition identifierWithGenericTypeParser = new PreconditionImpl<>(false, GenericTypeParser.createGenericTypeStatementCondition(genericTypeDepth));
		return identifierWithGenericTypeParser;
	}


	public static Precondition createIdentifierParser() {
		Precondition identifierParser = new StringParserBuilder()
			.addConditionMatcher(createIdentifierCondition())
			.build();
		return identifierParser;
	}


	public static CharConditions.BaseCharFilter createIdentifierCondition() {
		CharSearchSet charSet = new CharSearchSet();
		charSet.addChar('$');
		charSet.addRange('a', 'z');
		charSet.addChar('_');
		charSet.addRange('A', 'Z');

		val cond = new CharConditions.BaseCharFilter(charSet::contains, charSet.toCharList().toArray(), Inclusion.INCLUDE, charSet);
		CharConditions.setupContainsCharFilter(cond);
		return cond;
	}

}
