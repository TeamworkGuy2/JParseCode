package twg2.parser.test;

import static twg2.text.tokenizer.test.ParserTestUtils.parseTest;

import org.junit.Test;

import twg2.parser.condition.text.CharParser;
import twg2.parser.tokenizers.IdentifierTokenizer;

/**
 * @author TeamworkGuy2
 * @since 2015-11-29
 */
public class IdentifierParserTest {

	@Test
	public void compoundIdentifierParse() {
		String name = "CompoundIdentifierParse";
		CharParser cond = IdentifierTokenizer.createCompoundIdentifierTokenizer();

		parseTest(false, false, name, cond, "");
		parseTest(false, false, name, cond, "thing.");
		parseTest(false, true, name, cond, "12th.sing");
		parseTest(true, false, name, cond, "a..c", "a");
		parseTest(true, false, name, cond, "th12a.sing");
		parseTest(true, false, name, cond, "a.b.c");
		parseTest(true, false, name, cond, "A.Bb.Ccc");
	}

}
