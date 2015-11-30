package test;

import org.junit.Assert;

import parser.condition.ParserCondition;
import twg2.parser.textParser.TextParserImpl;

/**
 * @author TeamworkGuy2
 * @since 2015-11-28
 */
public interface ParserTestUtils {

	// Test also ensures that conditions work as expected when recycled (if possible) and when copied
	public static void parseTest(boolean expectComplete, boolean expectFailed, String name, ParserCondition cond, String src) {
		cond = cond.copyOrReuse();
		_parseTest(expectComplete, expectFailed, name, cond, src);
		cond = cond.copy();
		_parseTest(expectComplete, expectFailed, name, cond, src);
	}


	public static void _parseTest(boolean expectComplete, boolean expectFailed, String name, ParserCondition cond, String src) {
		TextParserImpl buf = TextParserImpl.of(src);

		while(buf.hasNext()) {
			char ch = buf.nextChar();
			cond.acceptNext(ch, buf);
		}

		Assert.assertEquals(name + " '" + src + "' isComplete() ", expectComplete, cond.isComplete());
		Assert.assertEquals(name + " '" + src + "' isFailed() ", expectFailed, cond.isFailed());
	}


}
