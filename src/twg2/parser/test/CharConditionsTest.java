package twg2.parser.test;

import static twg2.parser.test.ParserTestUtils.parseTest;
import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.Inclusion;
import twg2.parser.text.CharConditions;
import twg2.parser.text.CharParserCondition;
import twg2.parser.textParser.TextParserImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-2-12
 */
public class CharConditionsTest {


	@Test
	public void testStartCharCondition() {
		char[] startMarkers = new char[] {
				'\'', '"', ','
		};

		String[] testStrs = {
				"'", "\"test\"", ",a", "="
		};

		Boolean[] expect = { true, true, true, false };

		CharParserCondition cond = new CharConditions.StartCharFilter("testStartCharCondition", CharArrayList.of(startMarkers), Inclusion.INCLUDE);

		int i = 0;
		for(String testStr : testStrs) {
			TextParserImpl pos = TextParserImpl.of(testStr);
			Assert.assertTrue(i + "", cond.acceptNext(testStr.charAt(0), pos) == expect[i]);
			Assert.assertTrue(cond.isComplete() == expect[i]);
			Assert.assertTrue(cond.acceptNext((char)0, pos) == false);

			cond = cond.copyOrReuse();
			i++;
		}
	}


	@Test
	public void testEndCharCondition() {
		String name = "EndCharCondition";
		CharParserCondition cond = new CharConditions.EndCharFilter(name, CharArrayList.of('\'', '"', '!'), Inclusion.INCLUDE);

		parseTest(true, false, name, cond, "abc'");
		parseTest(true, false, name, cond, "stuff\"");
		parseTest(true, false, name, cond, "!");
		parseTest(false, true, name, cond, "=!=");
	}


	@Test
	public void testCharEndNotPrecededByCondition() {
		String name = "EndNotPrecededByCondition";
		val notPreced = CharArrayList.of('\\', '@');
		CharParserCondition cond = new CharConditions.EndCharNotPrecededByFilter(name, CharArrayList.of('\'', '"', '!'), Inclusion.INCLUDE, notPreced);

		parseTest(false, false, name, cond, "abc@'");
		parseTest(true, false, name, cond, "stuff\"");
		parseTest(true, false, name, cond, "!");
		parseTest(false, true, name, cond, "=!=");
		parseTest(false, false, name, cond, "\\!");
	}

}
