package test;

import static test.ParserTestUtils.parseTest;

import org.junit.Test;

import parser.condition.ParserCondition;
import codeParser.parsers.GenericTypeParser;

/**
 * @author TeamworkGuy2
 * @since 2015-11-29
 */
public class IdentifierParserTest {

	@Test
	public void identifierWithGenericTypeParse() {
		String name = "IdentifierWithGenericTypeParse";
		ParserCondition cond = GenericTypeParser.createGenericTypeStatementCondition(3);

		parseTest(false, false, name, cond, "thing<");
		parseTest(false, true, name, cond, "thing<,>");
		parseTest(false, true, name, cond, "thing<abc, >");
		parseTest(true, false, name, cond, "thing<abc>");
		parseTest(true, false, name, cond, "thing<abc, _def>");
		parseTest(true, false, name, cond, "thing<abc, _def<sub>>");
	}

}
