package twg2.parser.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import twg2.arrays.ArrayUtil;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;
import twg2.parser.text.CharParserFactory;
import twg2.parser.text.StringBoundedParserBuilder;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.parser.textParserUtils.EscapeSequences;
import twg2.text.stringSearch.StringCompare;
import twg2.text.stringSearch.StringIndex;
import checks.CheckTask;
import checks.TestData;
import checks.TestDataObj;

/**
 * @author TeamworkGuy2
 * @since 2016-0-0
 */
public class MiscStringTests {

	@Test
	public void startsWithTest() {
		List<TestData<String, String>> startsWithStrs = Arrays.asList(
				TestDataObj.matchFalse("this a lz3", "thisa"),
				TestDataObj.matchTrue("this a lz3", "this"),
				TestDataObj.matchFalse("a", "ab"),
				TestDataObj.matchTrue("a", "a"),
				TestDataObj.matchFalse("", "a"),
				TestDataObj.matchTrue("", "")
		);

		for(TestData<String, String> test : startsWithStrs) {
			Assert.assertTrue(test.isShouldInputEqualExpected() ==
					StringCompare.startsWith(test.getInput().toCharArray(), 0, test.getExpected().toCharArray(), 0));
		}
	}


	@Test
	public void indexOfTest() {
		String searchString = "this 32a this 32.1f is_a string";
		String[] strs = new String[] {"32a", "32z", " ", "  ", "this", "string"};
		Integer[] expect = { 5, -1, 4, -1, 0, 25 };
		char[] searchChars = searchString.toCharArray();

		CheckTask.assertTests(strs, expect, (str) -> StringIndex.indexOf(searchChars, 0, str, 0));
	}


	@Test
	public void lineBufferTest() {
		String line = "Abbb abab [very awesome] 132\n" +
				"few 142345 52132";

		TextParser tool = TextParserImpl.of(line);
		StringBuilder dst = new StringBuilder();

		tool.nextIf('A', dst);
		tool.nextIf((c) -> (c == 'b'), 2, dst);
		check(tool.nextIf('b', dst), 1, "could not read 'b'");
		check(tool.nextIf(' ', dst), 1, "could not read ' '");
		check(tool.nextIf('a', 'b', 0, dst), 4, "could not read 'a' or 'b'");
		tool.nextIf((c) -> (true), 0, dst);

		tool.nextIf((c) -> (true), 3, dst);
		tool.nextIf((c) -> (ArrayUtil.indexOf(new char[] {'1', '2', '3', '4', '5', ' '}, c) > -1) , 0, dst);
		tool.nextIf((c) -> (ArrayUtil.indexOf(new char[] {'1', '2', '3', '4', '5', ' '}, c) > -1) , 0, dst);

		Assert.assertEquals("parsed: '" + dst.toString() + "', does not match: '" + line + "'", line, dst.toString());
	}


	@Test
	public void stringBoundedSegmentParserTest() throws IOException {

		// single-character start and end markers and single-character escape markers
		String[] strs = new String[] {   "\"a \\\" b \\\"", "\"\" !", "alpha", "\"a \n\\\"\\\" z\" echo" };
		String[] expect = new String[] { "\"a \" b \"",       "\"\"",     "",      "\"a \n\"\" z\"" };

		CharParserFactory parser1 = new StringBoundedParserBuilder("stringBoundedSegmentParserTest").addStartEndNotPrecededByMarkers("string literal", '"', '\\', '"', Inclusion.INCLUDE).build();

		Function<String, String> escSeqDecoder = EscapeSequences.unicodeEscapeDecoder();
		CheckTask.assertTests(strs, expect, (s, i) -> {
			StringBuilder dst = new StringBuilder();
			//Assert.assertTrue("i=" + i + " first char '" + s.charAt(0) + "' of '" + s + "'", parser1.isMatch(s.charAt(0)));
			CharParser cond = parser1.createParser();
			cond.readConditional(TextParserImpl.of(s), dst);
			return escSeqDecoder.apply(dst.toString());
		});

		// TODO reimplement string markers
		// multi-character start and end markers and multi-character escape markers
		//strs = new String[] {   "to /**string @@/** and @@**/", "/**/", "alpha", "@@/**start \n@@/**@@**/ end**/ echo" };
		//expect = new String[] { "/**string @@/** and **/",      "/**/", "",      "/**start \n@@/****/ end**/" };
		/*
		settings.setEscapeString("@@")
			.setHandleEscapes(true)
			.setReadMultiline(true);
		input = new ParserHelper();
		*/
		//StringBoundedParser parser2 = new StringBoundedParser(settings, input, new String[] {"/**"}, new String[] {"**/"});
		/*
		Check.assertTests(strs, expect, "", "mismatch", (s) -> {
			dst.setLength(0);
			return parser2.readElement(LineBufferImpl.of(s), dst).toString();
		});
		*/
	}


	private static void check(int input, int expected, String message) {
		Assert.assertEquals(message + " (input: " + input + ", expected: " + expected + ")", expected, input);
	}

}
