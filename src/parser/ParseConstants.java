package parser;

/** Constants used by the various parser classes in this project
 * @author TeamworkGuy2
 * @since 2014-12-21
 */
public final class ParseConstants {
	public static final char[] SIMPLE_WHITESPACE_NOT_NEWLINE = new char[] {' '/* space: 32 */, '	'/* tab: 9 */,
		12/* vertical tab: 12 */ };
	public static final char[] SIMPLE_WHITESPACE = new char[] {' '/* space: 32 */, '	'/* tab: 9 */,
		12/* vertical tab: 12 */, '\n'/* line terminators */ };

	private ParseConstants() { throw new AssertionError("cannot instantiate static class ParseConstants"); }

}
