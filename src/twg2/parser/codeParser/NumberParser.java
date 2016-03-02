package twg2.parser.codeParser;

import lombok.val;
import twg2.collections.tuple.Tuples;
import twg2.functions.BiPredicates;
import twg2.parser.condition.text.CharParser;
import twg2.parser.primitive.NumericParser;
import twg2.parser.text.CharParserPlainFactoryImpl;
import twg2.parser.textParser.TextParser;
import twg2.ranges.CharSearchSet;

/**
 * @author TeamworkGuy2
 * @since 2016-2-20
 */
public class NumberParser {

	public static CharParserPlainFactoryImpl<CharParser> createNumericLiteralParser() {
		// TODO create a C# numeric literal parse
		CharSearchSet notPreceedingSet = new CharSearchSet();
		notPreceedingSet.addChar('_');
		notPreceedingSet.addChar('$');
		notPreceedingSet.addRange('A', 'Z');
		notPreceedingSet.addRange('a', 'z');

		val numParser = new NumericParser("numeric literal");

		BiPredicates.CharObject<TextParser> charCheck = (ch, buf) -> {
			boolean isFirst = numParser.getFirstCharMatcher().test(ch, buf);
			boolean hasPrev = buf.hasPrevChar();
			if(!hasPrev) { return isFirst; }
			// TODO somewhat messy hack to look back at the previous character and ensure that it's not one of certain chars that never precede numbers
			// (e.g. if an A-Z character preceds a digit, it's not a number, it's part of an identifer)
			char prevCh = buf.prevChar();
			return isFirst && !notPreceedingSet.contains(prevCh);
		};

		val numericLiteralParser = new CharParserPlainFactoryImpl<>("numeric literal", false, Tuples.of(charCheck, numParser));
		return (CharParserPlainFactoryImpl)numericLiteralParser;
	}

}
