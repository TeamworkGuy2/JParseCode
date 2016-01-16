package twg2.parser.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import org.junit.Test;

import twg2.collections.primitiveCollections.IntArrayList;
import twg2.parser.documentParser.block.IntermediateBlock;
import twg2.parser.documentParser.block.ParseBlocks;
import twg2.parser.documentParser.block.TextBlock;
import twg2.parser.documentParser.block.TextOffsetBlock;
import checks.CheckTask;

/**
 * @author TeamworkGuy2
 * @since 2014-9-1
 */
public class ParserTest {

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
		System.out.println("\n\n==block==");
		((IntermediateBlock)blocks).forEachLeaf((txtBlock, idx) -> {
			System.out.println("====\n" + txtBlock.toString(str).trim() + "\n====\n");
			blockList.add(txtBlock);
		});

		CheckTask.assertTests(blockList.toArray(new TextOffsetBlock[blockList.size()]), expect, (block) -> ((TextOffsetBlock)block).toString(str));
	}


	public static void main(String[] args) throws IOException {
		new IdentifierParserTest().identifierWithGenericTypeParse();
		new IdentifierParserTest().compoundIdentifierParse();
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
