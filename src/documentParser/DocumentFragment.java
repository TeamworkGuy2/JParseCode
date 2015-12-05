package documentParser;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public interface DocumentFragment<S, T> {

	public S getTextFragment();

	public T getFragmentType();

	@Override
	public String toString();

}
