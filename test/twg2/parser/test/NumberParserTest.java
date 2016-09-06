package twg2.parser.test;

import lombok.val;

import org.junit.Test;

import twg2.parser.tokenizers.NumberTokenizer;

/**
 * @author TeamworkGuy2
 * @since 2016-2-21
 */
public class NumberParserTest {

	@Test
	public void numberParser() {
		String name = "numberParser";
		val parser = NumberTokenizer.createNumericLiteralTokenizer();

		ParserTestUtils.parseTest(false, true, name, parser.createParser(), "t30)", "30");
		ParserTestUtils.parseTest(false, true, name, parser.createParser(), " 30)", "30");
		ParserTestUtils.parseTest(false, true, name, parser.createParser(), "(30)", "30");
		ParserTestUtils.parseTest(false, true, name, parser.createParser(), "(30", "30");
	}

}
