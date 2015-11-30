package parser.textFragment;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
@FunctionalInterface
public interface TextTransformer<T> {

	public T apply(CharSequence text, int textOff, int textLen);

}
