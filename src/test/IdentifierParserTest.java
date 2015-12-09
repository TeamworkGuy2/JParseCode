package test;

import static test.ParserTestUtils.parseTest;

import org.junit.Test;

import parser.condition.ParserCondition;
import codeParser.parsers.GenericTypeParser;
import codeParser.parsers.IdentifierParser;

/**
 * @author TeamworkGuy2
 * @since 2015-11-29
 */
public class IdentifierParserTest {

	@Test
	public void identifierWithGenericTypeParse() {
		String name = "IdentifierWithGenericTypeParse";
		ParserCondition cond = GenericTypeParser.createGenericTypeStatementCondition(3, IdentifierParser::createCompoundIdentifierCondition);

		parseTest(false, false, name, cond, "thing<");
		parseTest(false, true, name, cond, "thing<,>");
		parseTest(false, true, name, cond, "thing<abc, >");
		parseTest(true, false, name, cond, "thing<abc>");
		parseTest(true, false, name, cond, "thing<abc, _def>");
		parseTest(true, false, name, cond, "thing<abc, _def<sub>>");
		parseTest(true, false, name, cond, "thing<abc, _def<sub, wub, tub>>");
		parseTest(true, false, name, cond, "Modified<A, B>");
	}


	@Test
	public void compoundIdentifierParse() {
		String name = "CompoundIdentifierParse";
		ParserCondition cond = IdentifierParser.createCompoundIdentifierCondition();

		parseTest(false, false, name, cond, "");
		parseTest(false, false, name, cond, "thing.");
		parseTest(false, true, name, cond, "a..c");
		parseTest(true, false, name, cond, "thing.sing");
		parseTest(true, false, name, cond, "a.b.c");
		parseTest(true, false, name, cond, "A.Bb.Ccc");
	}

}
