package twg2.parser.codeParser;

import java.util.AbstractMap;

import lombok.val;
import twg2.arrays.ArrayUtil;
import twg2.collections.primitiveCollections.CharArrayList;
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

		val charList = new CharArrayList();
		numParser.getMatchFirstChars(charList);
		char[] allowChars = charList.toArray();

		BiPredicates.CharObject<TextParser> charCheck = (ch, buf) -> {
			// TODO this should be getPosition(), but buffer doesn't allow unread() into previous lines
			int off = buf.getColumnNumber();
			if(off < 2) { return ArrayUtil.indexOf(allowChars, ch) > -1; }
			// TODO somewhat messy hack to look back at the previous character and ensure that it's not one of certain chars that never precede numbers
			// (e.g. if an A-Z character preceds a digit, it's not a number, it's part of an identifer)
			buf.unread(2);
			char prevCh = buf.nextChar();
			buf.nextChar();
			return ArrayUtil.indexOf(allowChars, ch) > -1 && !notPreceedingSet.contains(prevCh);
		};

		val numericLiteralParser = new CharParserPlainFactoryImpl<>("numeric literal", false, new AbstractMap.SimpleImmutableEntry<>(charCheck, numParser));
		return (CharParserPlainFactoryImpl)numericLiteralParser;
	}

}
