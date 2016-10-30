package twg2.parser.fragment;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public interface TextToken<S, T> {

	public S getToken();

	public T getTokenType();

	@Override
	public String toString();

}
