package twg2.parser.tokenizers;

import twg2.parser.condition.text.CharParser;
import twg2.parser.condition.text.CharParserPredicate;
import twg2.parser.primitive.NumericParser;
import twg2.text.tokenizer.CharParserMatchableFactory;

/**
 * @author TeamworkGuy2
 * @since 2016-2-20
 */
public class NumberTokenizer {

	public static CharParserMatchableFactory<CharParser> createNumericLiteralTokenizer() {
		// TODO create a C# numeric literal parse

		// do not parse number sign -/+
		NumericParser numParser = new NumericParser("numeric literal", false);

		CharParserPredicate charCheck = (ch, buf) -> {
			boolean isFirst = numParser.getFirstCharMatcher().test(ch, buf);
			boolean hasPrev = buf.hasPrevChar();
			if(!hasPrev) { return isFirst; }
			// TODO somewhat messy hack to look back at the previous character and ensure that it's not one of certain chars that never precede numbers
			// (e.g. if an A-Z character precedes a digit, it's not a number, it's part of an identifier)
			char prevCh = buf.prevChar();
			return isFirst &&
					// if a digit is preceded by any of these, than it's part of an identifier, not a number 
					prevCh != '_' && prevCh != '$' && (prevCh < 'A' || prevCh > 'Z') && (prevCh < 'a' || prevCh > 'z');
		};

		return new CharParserMatchableFactory<CharParser>("numeric literal", false, new CharParserPredicate[] { charCheck }, new CharParser[] { numParser });
	}

}
