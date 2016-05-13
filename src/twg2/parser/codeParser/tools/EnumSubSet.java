package twg2.parser.codeParser.tools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import twg2.collections.util.ToStringUtil;

/** A class and builder for a list of enum values and names and find() method to retrieve the value associated with a name
 * @author TeamworkGuy2
 * @since 2016-2-20
 */
public class EnumSubSet<E> {
	protected String[] enumNames;
	protected E[] enumValues;


	public EnumSubSet(String[] enumNames, E[] enumValues) {
		this.enumNames = enumNames;
		this.enumValues = enumValues;
	}


	public EnumSubSet(Iterable<E> enums, Predicate<E> filter, Function<E, String> getName) {
		List<String> enumNameList = new ArrayList<>();
		List<E> enumValuesList = new ArrayList<>();

		for(E enm : enums) {
			if(filter.test(enm)) {
				enumNameList.add(getName.apply(enm));
				enumValuesList.add(enm);
			}
		}

		String[] matchingNames = enumNameList.toArray(new String[enumNameList.size()]);
		Arrays.sort(matchingNames);

		E[] matchingEnums = sortSecondListIntoArrayMatchingFirst(enumNameList, enumNames, enumValuesList);

		this.enumNames = matchingNames;
		this.enumValues = matchingEnums;
	}


	public E find(String name) {
		int idx = Arrays.binarySearch(enumNames, name);
		return idx > -1 ? enumValues[idx] : null;
	}


	@Override
	public String toString() {
		return ToStringUtil.toStringKeyValuePairs(enumNames, enumValues, enumNames.length, null).toString();
	}


	/** Given a list of names in original order (names) and sorted order (namesSorted) and a list of values (values), return a sorted 'values' array
	 * in the same order as (namesSorted) by using names.indexOf() to determine which index from (values) goes into each index of the returned array
	 */
	private static <G> G[] sortSecondListIntoArrayMatchingFirst(List<String> names, String[] namesSorted, List<G> values) {
		if(names.size() != namesSorted.length || namesSorted.length != values.size()) {
			throw new IllegalArgumentException("enum names and values arrays must be the same length: " +
					"enumNames=" + names.size() + ", enumNamesSorted=" + namesSorted.length + ", enumValues=" + values.size());
		}
		if(names.size() == 0) {
			@SuppressWarnings("unchecked")
			G[] res = (G[])new Object[0];
			return res;
		}

		@SuppressWarnings("unchecked")
		G[] valuesSortedDst = (G[])Array.newInstance(values.get(0).getClass(), values.size());

		for(int i = 0, size = namesSorted.length; i < size; i++) {
			int idx = names.indexOf(namesSorted[i]);

			if(idx == -1) {
				throw new IllegalArgumentException("could not find enum name '" + namesSorted[i] + "' in original list of enums");
			}

			G enm = values.get(idx);

			valuesSortedDst[i] = enm;
		}

		return valuesSortedDst;
	}




	/** Build a subset of names and values from a enum
	 * @author TeamworkGuy2
	 * @since 2016-2-20
	 * @param <F> this is the type of enum
	 */
	public static class Builder<F> {
		List<String> enumNameList = new ArrayList<>();
		List<F> enumValuesList = new ArrayList<>();
		Predicate<F> filter;
		Function<F, String> getName;


		/** Create a build for a subset of enum names and values
		 * @param filter predicate to determine if an enum instance matches 
		 * @param getName used to transform each matching enum's name into the form that will be matched by {@link EnumSubSet#find(String)}
		 */
		public Builder(Predicate<F> filter, Function<F, String> getName) {
			this.filter = filter;
			this.getName = getName;
		}


		public boolean add(F enm) {
			if(filter.test(enm)) {
				enumNameList.add(getName.apply(enm));
				enumValuesList.add(enm);
				return true;
			}
			return false;
		}


		public EnumSubSet<F> build() {
			String[] enumNames = enumNameList.toArray(new String[enumNameList.size()]);
			Arrays.sort(enumNames);

			F[] enumValues = sortSecondListIntoArrayMatchingFirst(enumNameList, enumNames, enumValuesList);

			return new EnumSubSet<>(enumNames, enumValues);
		}

	}

}
