package twg2.parser.textFragment;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
@FunctionalInterface
public interface TextConsumer {

	/**
	 * @param text the text fragment. This value is already a sub-string, {@code off} and {@code len} should not be applied to it.
	 * @param off 0 based absolute offset of the text fragment
	 * @param len length of the text fragment in characters
	 * @param lineStart the starting line number, 0 based
	 * @param columnStart the starting line offset, 0 based
	 * @param lineEnd the ending line number, inclusive, 0 based
	 * @param columnEnd the ending line offset, inclusive, 0 based
	 */
	public void accept(CharSequence text, int off, int len, int lineStart, int columnStart, int lineEnd, int columnEnd);

}
