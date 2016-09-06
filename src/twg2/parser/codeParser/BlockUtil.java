package twg2.parser.codeParser;


/** A utility for {@link BlockType}
 * @author TeamworkGuy2
 * @since 2016-09-03
 * @param <T_BLOCK> the type of blocks handled by this util
 * @param <T_KEYWORD> the type of keywords identifying the type of blocks handled by this util
 */
public interface BlockUtil<T_BLOCK, T_KEYWORD> {

	public T_BLOCK parseKeyword(T_KEYWORD keyword);

	public T_BLOCK tryParseKeyword(T_KEYWORD keyword);
}
