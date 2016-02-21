package twg2.parser.text;

import twg2.parser.codeParser.ParserBuilder;
import twg2.parser.condition.ParserFactory;
import twg2.parser.condition.text.CharParser;
import twg2.parser.textParser.TextParser;

/** A CharParserFactory, commonly used by a {@link ParserBuilder} and {@link CharMultiConditionParser}.<br>
 * This interface provides an {@link #isMatch(char, TextParser)} method to check if a single character matches
 * the beginning of this CharParserFactory and a factory method to create a {@link CharParser}
 * designed so implementers can create recyclable, precondition filters, managed by a factory,
 * to allow for faster parsers that produce less garbage.
 *
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public interface CharParserFactory extends ParserFactory<CharParser> {

	/** Check if a character matches the beginning of this {@code CharParserFactory}
	 * @param ch the character
	 * @return true if the character matches this precondition, false if not
	 */
	public boolean isMatch(char ch, TextParser pos);

}
