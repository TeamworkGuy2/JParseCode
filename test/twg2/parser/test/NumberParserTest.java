package twg2.parser.test;

import static twg2.text.tokenizer.test.ParserTestUtils.parseTest;

import org.junit.Test;

import twg2.parser.condition.text.CharParser;
import twg2.parser.tokenizers.NumberTokenizer;
import twg2.text.tokenizer.CharParserMatchableFactory;

/**
 * @author TeamworkGuy2
 * @since 2016-2-21
 */
public class NumberParserTest {

	@Test
	public void numberParser() {
		String name = "numberParser";
		CharParserMatchableFactory<CharParser> parser = NumberTokenizer.createNumericLiteralTokenizer();

		parseTest(false, true, name, parser.createParser(), "t30)", "30");
		parseTest(false, true, name, parser.createParser(), " 30)", "30");
		parseTest(false, true, name, parser.createParser(), "(30)", "30");
		parseTest(false, true, name, parser.createParser(), "(30", "30");
	}

}
