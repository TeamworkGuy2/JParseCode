package parser.text;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import parser.Inclusion;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.collections.primitiveCollections.CharList;

/**
 * @author TeamworkGuy2
 * @since 2015-2-21
 */
public class CharParserConditionFactory<T_CONSTRUCTED_TYPE extends CharParserCondition> {
	BiFunction<? super CharList, Inclusion, T_CONSTRUCTED_TYPE> constructor;
	Consumer<T_CONSTRUCTED_TYPE> modifier;


	public CharParserConditionFactory(BiFunction<? super CharList, Inclusion, T_CONSTRUCTED_TYPE> constructor, Consumer<T_CONSTRUCTED_TYPE> modifier) {
		this.constructor = constructor;
		this.modifier = modifier;
	}


	public T_CONSTRUCTED_TYPE create(char preconditionMatch) {
		return this.create(preconditionMatch, Inclusion.INCLUDE);
	}


	public T_CONSTRUCTED_TYPE create(char preconditionMatch, Inclusion includeCondMatchInRes) {
		T_CONSTRUCTED_TYPE obj = constructor.apply(CharArrayList.of(preconditionMatch), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}


	public T_CONSTRUCTED_TYPE create(char[] preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T_CONSTRUCTED_TYPE create(char[] preconditionMatches, Inclusion includeCondMatchInRes) {
		T_CONSTRUCTED_TYPE obj = constructor.apply(CharArrayList.of(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}


	public T_CONSTRUCTED_TYPE create(CharList preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T_CONSTRUCTED_TYPE create(CharList preconditionMatches, Inclusion includeCondMatchInRes) {
		T_CONSTRUCTED_TYPE obj = constructor.apply(preconditionMatches.copy(), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class CharMatchesList<T_CONSTRUCTED_TYPE extends CharParserCondition> {
		BiFunction<? super CharList, Inclusion, T_CONSTRUCTED_TYPE> constructor;
		BiConsumer<T_CONSTRUCTED_TYPE, CharList> modifier;


		public CharMatchesList(BiFunction<? super CharList, Inclusion, T_CONSTRUCTED_TYPE> constructor, BiConsumer<T_CONSTRUCTED_TYPE, CharList> modifier) {
			this.constructor = constructor;
			this.modifier = modifier;
		}


		public T_CONSTRUCTED_TYPE create(CharList notPreceded, char preconditionMatch) {
			return this.create(notPreceded, preconditionMatch, Inclusion.INCLUDE);
		}


		public T_CONSTRUCTED_TYPE create(CharList notPreceded, char preconditionMatch, Inclusion includeCondMatchInRes) {
			CharList matchChars = CharArrayList.of(preconditionMatch);
			T_CONSTRUCTED_TYPE obj = constructor.apply(matchChars, includeCondMatchInRes);
			modifier.accept(obj, notPreceded);
			return obj;
		}


		public T_CONSTRUCTED_TYPE create(CharList notPreceded, char[] preconditionMatches) {
			return this.create(notPreceded, preconditionMatches, Inclusion.INCLUDE);
		}


		public T_CONSTRUCTED_TYPE create(CharList notPreceded, char[] preconditionMatches, Inclusion includeCondMatchInRes) {
			CharList matchChars = CharArrayList.of(preconditionMatches);
			T_CONSTRUCTED_TYPE obj = constructor.apply(matchChars, includeCondMatchInRes);
			modifier.accept(obj, notPreceded);
			return obj;
		}


		public T_CONSTRUCTED_TYPE create(CharList notPreceded, CharList preconditionMatches) {
			return this.create(notPreceded, preconditionMatches, Inclusion.INCLUDE);
		}


		public T_CONSTRUCTED_TYPE create(CharList notPreceded, CharList preconditionMatches, Inclusion includeCondMatchInRes) {
			CharList matchChars = preconditionMatches.copy();
			T_CONSTRUCTED_TYPE obj = constructor.apply(matchChars, includeCondMatchInRes);
			modifier.accept(obj, notPreceded);
			return obj;
		}

	}

}
