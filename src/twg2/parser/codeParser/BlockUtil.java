package twg2.parser.codeParser;


/** A utility for {@link BlockType}
 * @author TeamworkGuy2
 * @since 2016-09-03
 * @param <T_BLOCK> the type of blocks handled by this util
 * @param <T_KEYWORD> the type of keywords identifying the type of blocks handled by this util
 */
public interface BlockUtil<T_BLOCK, T_KEYWORD> {

	/** Check if a keyword is a valid compound block keyword/identifier
	 * @param keyword the keyword/identifier to check
	 * @return the {@code T_BLOCK} type of the keyword or null if the keyword is not a valid block keyword/identifier
	 */
	public T_BLOCK tryToBlock(T_KEYWORD keyword);
}
