package twg2.parser.codeParser.tools;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

/** Static methods for splitting/extracting subsets of values from enums
 * @author TeamworkGuy2
 * @since 2016-4-9
 */
public class EnumSplitter {

	/** Split a array of values using filters
	 * @param values
	 * @param getName
	 * @param filters
	 * @return the enum names (retrieved from the 'getName' parameter) and an array of {@link EnumSubSet EnumSubSets} built from the 'filters' parameter
	 */
	@SafeVarargs
	public static <E> Entry<String[], EnumSubSet<E>[]> split(E[] values, Function<E, String> getName, Predicate<E>... filters) {
		@SuppressWarnings("unchecked")
		EnumSubSet.Builder<E>[] subSetBldrs = new EnumSubSet.Builder[filters.length];
		for(int i = 0, size = filters.length; i < size; i++) {
			subSetBldrs[i] = new EnumSubSet.Builder<>(filters[i], getName);
		}

		String[] names = new String[values.length];

		for(int i = 0, size = values.length; i < size; i++) {
			E enm = values[i];
			names[i] = getName.apply(enm);

			for(int k = 0, sizeK = subSetBldrs.length; k < sizeK; k++) {
				subSetBldrs[k].add(enm);
			}
		}

		@SuppressWarnings("unchecked")
		EnumSubSet<E>[] subSets = new EnumSubSet[subSetBldrs.length];

		for(int k = 0, sizeK = subSetBldrs.length; k < sizeK; k++) {
			subSets[k] = subSetBldrs[k].build();
		}

		return new AbstractMap.SimpleImmutableEntry<>(names, subSets);
	}

}
