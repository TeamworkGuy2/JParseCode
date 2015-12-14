package parser.text;

import parser.condition.Precondition;
import codeParser.ParserBuilder;

/** A CharPrecondition, commonly used by a {@link ParserBuilder} and {@link CharMultiConditionParser}.<br>
 * This interface provides an {@link #isMatch(char)} method to check if a single character matches
 * the beginning of this CharPrecondition and a factory method to create a {@link CharParserCondition}
 * designed so implementers can create recyclable, precondition filters, managed by a factory,
 * to allow for faster parsers that produce less garbage.
 *
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public interface CharPrecondition extends Precondition<CharParserCondition> {

	/** Check if a character matches the beginning of this {@code CharPrecondition}
	 * @param ch the character
	 * @return true if the character matches this precondition, false if not
	 */
	public boolean isMatch(char ch);

}
