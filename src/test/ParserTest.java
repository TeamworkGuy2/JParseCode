package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import parser.Inclusion;
import parser.StringBoundedParserBuilder;
import parser.condition.ParserCondition;
import parser.condition.Precondition;
import twg2.collections.primitiveCollections.IntArrayList;
import twg2.collections.util.arrayUtils.ArrayUtil;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.parser.textParserUtils.EscapeSequences;
import twg2.ranges.helpers.CharCategory;
import twg2.text.stringUtils.StringCompare;
import twg2.text.stringUtils.StringIndex;
import checks.Check;
import checks.CheckTask;
import checks.TestData;
import checks.TestDataObj;
import documentParser.block.IntermediateBlock;
import documentParser.block.ParseBlocks;
import documentParser.block.TextBlock;
import documentParser.block.TextOffsetBlock;

/**
 * @author TeamworkGuy2
 * @since 2014-9-1
 */
public class ParserTest {

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


	// TODO finish converting tests
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
		StringBuilder dst = new StringBuilder();

		// single-character start and end markers and single-character escape markers
		String[] strs = new String[] {   "\"a \\\" b \\\"", "\"\" !", "alpha", "\"a \n\\\"\\\" z\" echo" };
		String[] expect = new String[] { "\"a \" b \"",       "\"\"",     "",      "\"a \n\"\" z\"" };

		Precondition parser1 = new StringBoundedParserBuilder().addStartEndNotPrecededByMarkers('"', '\\', '"', Inclusion.INCLUDE).build();

		Function<String, String> escSeqDecoder = EscapeSequences.unicodeEscapeDecoder();
		CheckTask.assertTests(strs, expect, (s, i) -> {
			if(i == 3) {
				System.out.println();
			}
			dst.setLength(0);
			//Assert.assertTrue("i=" + i + " first char '" + s.charAt(0) + "' of '" + s + "'", parser1.isMatch(s.charAt(0)));
			ParserCondition cond = parser1.createParserCondition();
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


	@Test
	public void parseBlocksTest() {
		/*String str =
				"bla\n" +
				"bla\n" +
				"block %{\n" +
				"	stuff in block\n" +
				"		%{ adjacent block %}\n" +
				"		%{ nested block %{ double nesting %} nested after %}\n" +
				"	stuff after nested block\n" +
				"	%{ secondary: %{ secondary1 %}, %{ secondary2 %}, %{ secondary3 %} %}\n" +
				"%}\n" +
				"more text\n" +
				"%{ another block %}\n" +
				"xyz";
		*/
		String str = "function test(t) {\n" +
			"  t = Number.parseInt(t, 10);\n" +
			"  {\n" +
			"    var value = 3 * 5, neg = -1, pos = 1;\n" +
			"    t = t < value ? t * neg : value;\n" +
			"    t = Math.max(t, pos);\n" +
			"  }\n" +
			"  return t;\n" +
			"}";
		String[] expect = {
			"function test(t) ",

			"\n  " +
			"t = Number.parseInt(t, 10);\n  ",

			"\n" +
			"    var value = 3 * 5, neg = -1, pos = 1;\n" +
			"    t = t < value ? t * neg : value;\n" +
			"    t = Math.max(t, pos);\n" +
			"  ",

			"\n" +
			"  return t;\n",

			""
		};

		IntArrayList blockIndices = new IntArrayList();
		StringBuilder strB = new StringBuilder(str);
		IntArrayList ints = new IntArrayList();
		BiFunction<Integer, Integer, Integer> minGreater = (a, b) -> a < 0 ? (b < 0 ? a : b) : Math.min(a, b);
		for(int off = 0, i = minGreater.apply(str.indexOf('{', off + 1), str.indexOf('}', off + 1)), size = str.length();
				i < size && i > -1;
				off = i, i = minGreater.apply(str.indexOf('{', off + 1), str.indexOf('}', off + 1))) {
			ints.add(i);
		}
		System.out.println("indices {}: " + ints);

		TextBlock blocks = ParseBlocks.splitIntoBlocks(strB, '{', '}', blockIndices);
		List<TextBlock> blockList = new ArrayList<>();

		System.out.println("parse string (length: " + str.length() + "):\n" + str + "\n\n");
		System.out.println("\n\n==documentParser.block==");
		((IntermediateBlock)blocks).forEachLeaf((txtBlock, idx) -> {
			System.out.println("====\n" + txtBlock.toString(str).trim() + "\n====\n");
			blockList.add(txtBlock);
		});

		CheckTask.assertTests(blockList.toArray(new TextOffsetBlock[blockList.size()]), expect, (block) -> ((TextOffsetBlock)block).toString(str));
	}


	@Test
	public void readCharTypeTest() {
		CharCategory[] types = new CharCategory[] {
				CharCategory.ALPHA_LOWER,
				CharCategory.ALPHA_UPPER,
				CharCategory.ALPHA_LOWER,
				CharCategory.DIGIT,
				CharCategory.DIGIT,
				CharCategory.ALPHA_UPPER_OR_LOWER
		};
		String[] strs = new String[] {
				"characterswithoutspaces",
				"ALPHAUPPERCHARACTHERS",
				"with spaces",
				"932421",
				"7252_312",
				"AlphaWITHlowerCase"
		};
		String[] expect = new String[] {
				"characterswithoutspaces",
				"ALPHAUPPERCHARACTHERS",
				"with",
				"932421",
				"7252",
				"AlphaWITHlowerCase"
		};
		Check.assertLengths("types, strs, expect arrays must be the same length", types.length, strs.length, expect.length);

		StringBuilder dst = new StringBuilder();
		for(int i = 0, size = strs.length; i < size; i++) {
			TextParser tool = TextParserImpl.of(strs[i]);
			tool.nextIf(types[i], dst);
			System.out.println("read char type: " + dst.toString());
			Check.assertEqual(dst.toString(), expect[i], "");
			dst.setLength(0);
		}
	}


	private static void check(int input, int expected, String message) {
		Assert.assertEquals(message + " (input: " + input + ", expected: " + expected + ")", expected, input);
	}


	public static void main(String[] args) throws IOException {
		ParserTest parserTest = new ParserTest();
		//parserTest.parseBlocksTest();
		//parserTest.lineBufferTest();

		/*
		stringToCaseTest();
		readCharTypeTest();
		parseDateTimeTest();
		readJsonLiteArrayTest();
		readJsonLiteNumberTest();
		lineBufferTest();
		*/
		System.out.println("float min_normal: " + Float.MIN_NORMAL + ", min_value: " + Float.MIN_VALUE);
		System.out.println("double min_normal: " + Double.MIN_NORMAL + ", min_value: " + Double.MIN_VALUE);
		System.out.println();

		Random rand = new Random();
		int size = 200;
		int similarCount = 0;
		for(int i = 0; i < size; i++) {
			long randLong = rand.nextLong();
			double d = Double.longBitsToDouble(randLong);
			String numStr = Double.toString(d);
			double dParsed = Double.parseDouble(numStr);
			float fParsed = (float)dParsed; //Float.parseFloat(numStr);
			double diff = dParsed - fParsed;
			System.out.println(dParsed + "\t " + fParsed + " :\t " + diff + " | " + (diff > Float.MIN_NORMAL));
			if(diff < Float.MIN_NORMAL) {
				similarCount++;
			}
		}
		System.out.println("similar " + similarCount + "/" + size);
	}

}
