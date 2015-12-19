package twg2.parser.test;

import lombok.val;

import org.junit.Assert;

import twg2.parser.text.CharParserCondition;
import twg2.parser.textParser.TextParserImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-11-28
 */
public interface ParserTestUtils {

	// Test also ensures that conditions work as expected when recycled (if possible) and when copied
	public static void parseTest(boolean expectComplete, boolean expectFailed, String name, CharParserCondition cond, String src) {
		cond = cond.copyOrReuse();
		_parseTest(expectComplete, expectFailed, name, cond, src, null);
		cond = cond.copy();
		_parseTest(expectComplete, expectFailed, name, cond, src, null);
	}


	/** If parsing is successfully, the result text should be identical to the src text
	 */
	public static void parseTestSameParsed(boolean expectComplete, boolean expectFailed, String name, CharParserCondition cond, String src) {
		parseTest(expectComplete, expectFailed, name, cond, src, src);
	}


	public static void parseTest(boolean expectComplete, boolean expectFailed, String name, CharParserCondition cond, String src, String expectedParsedResult) {
		cond = cond.copyOrReuse();
		_parseTest(expectComplete, expectFailed, name, cond, src, expectedParsedResult);
		cond = cond.copy();
		_parseTest(expectComplete, expectFailed, name, cond, src, expectedParsedResult);
	}


	public static void _parseTest(boolean expectComplete, boolean expectFailed, String name, CharParserCondition cond, String src, String srcExpect) {
		TextParserImpl buf = TextParserImpl.of(src);

		while(buf.hasNext()) {
			char ch = buf.nextChar();
			cond.acceptNext(ch, buf);
		}

		val isComplete = cond.isComplete();
		Assert.assertEquals(name + " '" + src + "' isComplete() ", expectComplete, isComplete);
		val isFailed = cond.isFailed();
		Assert.assertEquals(name + " '" + src + "' isFailed() ", expectFailed, isFailed);

		if(isComplete && srcExpect != null) {
			val parsedText = cond.getCompleteMatchedTextCoords().getText(src);
			Assert.assertEquals(srcExpect, parsedText);
		}
	}


}
