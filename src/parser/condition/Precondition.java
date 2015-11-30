package parser.condition;

import codeParser.ParserBuilder;

/** A Precondition, commonly used by a {@link ParserBuilder} and {@link MultiConditionParser}.<br>
 * This interface provides an {@link #isMatch(char)} method to check if a single character matches
 * the beginning of this Precondition and a factory method to create a {@link ParserCondition}
 * in hopes that implementers will create recyclable, precondition filters, managed by a factory,
 * to allow for faster parsers that produce less garbage.
 *
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public interface Precondition {

	/** Check if a character matches the beginning of this {@code Precondition}
	 * @param ch the character
	 * @return true if the character matches this precondition, false if not
	 */
	public boolean isMatch(char ch);


	/**
	 * @return true if the element parsed by this condition can contain sub-elements, i.e. if other elements can be parsed from within the source of this element,
	 * false if this condition cannot contain sub-elements
	 */
	public boolean isCompound();


	/** Each call creates a new {@code ParserConditionFactory}
	 * @return a new {@link ParserCondition} that match against this {@code Precondition}
	 */
	public ParserCondition createParserCondition();

}
