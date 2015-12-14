package parser.condition;

/** A Precondition.<br>
 * This interface contains a factory method to create a {@link ParserCondition}
 * designed so implementers can create recyclable, precondition filters, managed by a factory,
 * to allow for faster parsers that produce less garbage.
 *
 * @author TeamworkGuy2
 * @since 2015-2-9
 */
public interface Precondition<T extends ParserCondition> {

	/**
	 * @return true if the element parsed by this condition can contain sub-elements, i.e. if other elements can be parsed from within the source of this element,
	 * false if this condition cannot contain sub-elements
	 */
	public boolean isCompound();


	/** Each call creates a new {@code ParserCondition}
	 * @return a new {@link ParserCondition} that match against this {@code Precondition}
	 */
	public T createParser();

}
