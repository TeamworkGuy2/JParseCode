package twg2.parser.test;

import org.junit.Assert;
import org.junit.Test;

import twg2.parser.Inclusion;
import twg2.parser.text.CharParserCondition;
import twg2.parser.text.StringConditions;
import twg2.parser.textParser.TextParserImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-2-12
 */
public class StringConditionsTest {


	@Test
	public void testStartStringCondition() {
		String[] startMarkers = new String[] {
				"//", "\"", "/**", "!#"
		};

		String[] testStrs = new String[] {
				"// comment", "\"", "/** ", " !#"
		};

		int[] testOff = { 0, 0, 0, 1 };

		Boolean[] expect = { true, true, true, false };

		CharParserCondition cond = new StringConditions.StartStringFilter("testStartStringCondition", startMarkers, Inclusion.INCLUDE);

		int i = 0;
		for(String testStr : testStrs) {
			TextParserImpl pos = TextParserImpl.of(testStr);
			int chI = 0;
			for(char ch : testStr.toCharArray()) {
				pos.nextChar();
				if(chI >= startMarkers[i].length()) {
					break;
				}
				cond.acceptNext(ch, pos);
				chI++;
			}
			Assert.assertTrue(i + "." + chI, cond.isComplete() == expect[i]);
			Assert.assertTrue(cond.acceptNext((char)0, pos) == false);
			cond = cond.copyOrReuse();
			i++;
		}
	}


	@Test
	public void testEndStringCondition() {
		String[] endMarkers = new String[] {
				"-->", "\"\"\"", "!#"
		};

		String[] testStrs = new String[] {
				"<!-- comment -->", "\"stuff\"\"\"", "!#", "!#="
		};

		Boolean[] expect = { true, true, true, false };

		CharParserCondition cond = new StringConditions.EndStringFilter("testEndStringCondition", endMarkers, Inclusion.INCLUDE);

		int i = 0;
		for(String testStr : testStrs) {
			for(int ii = 0, size = endMarkers.length; ii < size; ii++) {
				TextParserImpl pos = TextParserImpl.of(testStr);
				int chI = 0;
				for(char ch : testStr.toCharArray()) {
					pos.nextChar();
					cond.acceptNext(ch, pos);
					chI++;
				}
				Assert.assertTrue(i + "." + ii, cond.isComplete() == expect[i]);
				cond = cond.copyOrReuse();
			}
			i++;
		}
	}

}
