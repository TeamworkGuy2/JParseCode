package parser.textFragment;

/**
 * @author TeamworkGuy2
 * @since 2015-5-27
 */
@FunctionalInterface
public interface TextTokenConsumer<T> {

	public void accept(T fragmentType, CharSequence text, int textOff, int textLen, int fragmentDepth, T parentFragmentType);

}
