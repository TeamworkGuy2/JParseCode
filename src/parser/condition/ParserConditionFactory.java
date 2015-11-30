package parser.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import parser.Inclusion;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.util.ListBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-2-21
 */
public class ParserConditionFactory<T extends ParserCondition, C> {
	BiFunction<? super Collection<C>, Inclusion, ? extends T> constructor;
	Consumer<T> modifier;


	public ParserConditionFactory(BiFunction<? super Collection<C>, Inclusion, ? extends T> constructor, Consumer<T> modifier) {
		this.constructor = constructor;
		this.modifier = modifier;
	}


	public T create(C preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T create(C preconditionMatches, Inclusion includeCondMatchInRes) {
		T obj = constructor.apply(ListBuilder.newMutable(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}


	public T create(C[] preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T create(C[] preconditionMatches, Inclusion includeCondMatchInRes) {
		T obj = constructor.apply(ListBuilder.newMutable(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}


	public T create(Collection<C> preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T create(Collection<C> preconditionMatches, Inclusion includeCondMatchInRes) {
		T obj = constructor.apply(new ArrayList<>(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-11-22
	 * @param <T>
	 * @param <C>
	 */
	public static class CompoundFactory<T extends ParserCondition, C> {
		Function<? super Collection<C>, ? extends T> constructor;
		Consumer<T> modifier;


		public CompoundFactory(Function<? super Collection<C>, ? extends T> constructor, Consumer<T> modifier) {
			this.constructor = constructor;
			this.modifier = modifier;
		}


		public T create(C preconditionMatches) {
			T obj = constructor.apply(ListBuilder.newMutable(preconditionMatches));
			modifier.accept(obj);
			return obj;
		}


		public T create(C[] preconditionMatches) {
			T obj = constructor.apply(ListBuilder.newMutable(preconditionMatches));
			modifier.accept(obj);
			return obj;
		}


		public T create(Collection<C> preconditionMatches) {
			T obj = constructor.apply(new ArrayList<>(preconditionMatches));
			modifier.accept(obj);
			return obj;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class CharFilterFactory<T extends ParserCondition> {
		BiFunction<? super CharList, Inclusion, T> constructor;
		Consumer<T> modifier;


		public CharFilterFactory(BiFunction<? super CharList, Inclusion, T> constructor, Consumer<T> modifier) {
			this.constructor = constructor;
			this.modifier = modifier;
		}


		public T create(char preconditionMatch) {
			return this.create(preconditionMatch, Inclusion.INCLUDE);
		}


		public T create(char preconditionMatch, Inclusion includeCondMatchInRes) {
			T obj = constructor.apply(CharArrayList.of(preconditionMatch), includeCondMatchInRes);
			modifier.accept(obj);
			return obj;
		}


		public T create(char[] preconditionMatches) {
			return this.create(preconditionMatches, Inclusion.INCLUDE);
		}


		public T create(char[] preconditionMatches, Inclusion includeCondMatchInRes) {
			T obj = constructor.apply(CharArrayList.of(preconditionMatches), includeCondMatchInRes);
			modifier.accept(obj);
			return obj;
		}


		public T create(CharList preconditionMatches) {
			return this.create(preconditionMatches, Inclusion.INCLUDE);
		}


		public T create(CharList preconditionMatches, Inclusion includeCondMatchInRes) {
			T obj = constructor.apply(preconditionMatches.copy(), includeCondMatchInRes);
			modifier.accept(obj);
			return obj;
		}

	}




	public static class CharAugmentedFilterFactory<T extends ParserCondition> {
		BiFunction<? super CharList, Inclusion, T> constructor;
		BiConsumer<T, CharList> modifier;


		public CharAugmentedFilterFactory(BiFunction<? super CharList, Inclusion, T> constructor, BiConsumer<T, CharList> modifier) {
			this.constructor = constructor;
			this.modifier = modifier;
		}


		public T create(CharList notPreceded, char preconditionMatch) {
			return this.create(notPreceded, preconditionMatch, Inclusion.INCLUDE);
		}


		public T create(CharList notPreceded, char preconditionMatch, Inclusion includeCondMatchInRes) {
			CharList matchChars = CharArrayList.of(preconditionMatch);
			T obj = constructor.apply(matchChars, includeCondMatchInRes);
			modifier.accept(obj, notPreceded);
			return obj;
		}


		public T create(CharList notPreceded, char[] preconditionMatches) {
			return this.create(notPreceded, preconditionMatches, Inclusion.INCLUDE);
		}


		public T create(CharList notPreceded, char[] preconditionMatches, Inclusion includeCondMatchInRes) {
			CharList matchChars = CharArrayList.of(preconditionMatches);
			T obj = constructor.apply(matchChars, includeCondMatchInRes);
			modifier.accept(obj, notPreceded);
			return obj;
		}


		public T create(CharList notPreceded, CharList preconditionMatches) {
			return this.create(notPreceded, preconditionMatches, Inclusion.INCLUDE);
		}


		public T create(CharList notPreceded, CharList preconditionMatches, Inclusion includeCondMatchInRes) {
			CharList matchChars = preconditionMatches.copy();
			T obj = constructor.apply(matchChars, includeCondMatchInRes);
			modifier.accept(obj, notPreceded);
			return obj;
		}

	}

}
