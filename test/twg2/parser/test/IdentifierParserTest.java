package twg2.parser.test;

import static twg2.parser.test.ParserTestUtils.parseTestSameParsed;

import org.junit.Test;

import twg2.parser.codeParser.parsers.IdentifierParser;
import twg2.parser.condition.text.CharParser;

/**
 * @author TeamworkGuy2
 * @since 2015-11-29
 */
public class IdentifierParserTest {

	@Test
	public void compoundIdentifierParse() {
		String name = "CompoundIdentifierParse";
		CharParser cond = IdentifierParser.createCompoundIdentifierParser();

		parseTestSameParsed(false, false, name, cond, "");
		parseTestSameParsed(false, false, name, cond, "thing.");
		parseTestSameParsed(false, true, name, cond, "a..c");
		parseTestSameParsed(false, true, name, cond, "12th.sing");
		parseTestSameParsed(true, false, name, cond, "th12a.sing");
		parseTestSameParsed(true, false, name, cond, "a.b.c");
		parseTestSameParsed(true, false, name, cond, "A.Bb.Ccc");
	}

}
