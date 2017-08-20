package twg2.parser.tokenizers;

import twg2.parser.condition.text.CharParser;
import twg2.parser.condition.text.CharParserPredicate;
import twg2.parser.primitive.NumericParser;
import twg2.ranges.CharSearchSet;
import twg2.text.tokenizer.CharParserMatchableFactory;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2016-2-20
 */
public class NumberTokenizer {

	public static CharParserMatchableFactory<CharParser> createNumericLiteralTokenizer() {
		// TODO create a C# numeric literal parse
		CharSearchSet notPreceedingSet = new CharSearchSet();
		notPreceedingSet.addChar('_');
		notPreceedingSet.addChar('$');
		notPreceedingSet.addRange('A', 'Z');
		notPreceedingSet.addRange('a', 'z');

		// do not parse number sign -/+
		NumericParser numParser = new NumericParser("numeric literal", false);

		CharParserPredicate charCheck = (ch, buf) -> {
			boolean isFirst = numParser.getFirstCharMatcher().test(ch, buf);
			boolean hasPrev = buf.hasPrevChar();
			if(!hasPrev) { return isFirst; }
			// TODO somewhat messy hack to look back at the previous character and ensure that it's not one of certain chars that never precede numbers
			// (e.g. if an A-Z character preceds a digit, it's not a number, it's part of an identifer)
			char prevCh = buf.prevChar();
			return isFirst && !notPreceedingSet.contains(prevCh);
		};

		return new CharParserMatchableFactory<CharParser>("numeric literal", false, Tuples.of(charCheck, numParser));
	}

}
