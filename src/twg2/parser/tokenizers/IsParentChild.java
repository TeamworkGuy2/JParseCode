package twg2.parser.tokenizers;

import java.util.function.BiPredicate;

/** Determine if a object is a child of another object
 * @author TeamworkGuy2
 * @since 2015-11-27
 */
@FunctionalInterface
public interface IsParentChild<T> extends BiPredicate<T, T> {

	@Override
	public boolean test(T parent, T child);

}
