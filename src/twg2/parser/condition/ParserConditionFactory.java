package twg2.parser.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import twg2.collections.util.ListBuilder;
import twg2.parser.Inclusion;

/**
 * @author TeamworkGuy2
 * @since 2015-2-21
 */
public class ParserConditionFactory<T_CONSTRUCTED_TYPE extends ParserCondition, T_PARAM_1> {
	BiFunction<? super Collection<T_PARAM_1>, Inclusion, ? extends T_CONSTRUCTED_TYPE> constructor;
	Consumer<T_CONSTRUCTED_TYPE> modifier;


	public ParserConditionFactory(BiFunction<? super Collection<T_PARAM_1>, Inclusion, ? extends T_CONSTRUCTED_TYPE> constructor, Consumer<T_CONSTRUCTED_TYPE> modifier) {
		this.constructor = constructor;
		this.modifier = modifier;
	}


	public T_CONSTRUCTED_TYPE create(T_PARAM_1 preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T_CONSTRUCTED_TYPE create(T_PARAM_1 preconditionMatches, Inclusion includeCondMatchInRes) {
		T_CONSTRUCTED_TYPE obj = constructor.apply(ListBuilder.newMutable(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}


	public T_CONSTRUCTED_TYPE create(T_PARAM_1[] preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T_CONSTRUCTED_TYPE create(T_PARAM_1[] preconditionMatches, Inclusion includeCondMatchInRes) {
		T_CONSTRUCTED_TYPE obj = constructor.apply(ListBuilder.newMutable(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}


	public T_CONSTRUCTED_TYPE create(Collection<T_PARAM_1> preconditionMatches) {
		return this.create(preconditionMatches, Inclusion.INCLUDE);
	}


	public T_CONSTRUCTED_TYPE create(Collection<T_PARAM_1> preconditionMatches, Inclusion includeCondMatchInRes) {
		T_CONSTRUCTED_TYPE obj = constructor.apply(new ArrayList<>(preconditionMatches), includeCondMatchInRes);
		modifier.accept(obj);
		return obj;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-11-22
	 * @param <T_CONSTRUCTED_TYPE> the type of objects constructed by this factory
	 * @param <C> a collection member type, used for factory's constructor {@link #create(Collection)}
	 */
	public static class CompoundFactory<T_CONSTRUCTED_TYPE extends ParserCondition, C> {
		Function<? super Collection<C>, ? extends T_CONSTRUCTED_TYPE> constructor;
		Consumer<T_CONSTRUCTED_TYPE> modifier;


		public CompoundFactory(Function<? super Collection<C>, ? extends T_CONSTRUCTED_TYPE> constructor, Consumer<T_CONSTRUCTED_TYPE> modifier) {
			this.constructor = constructor;
			this.modifier = modifier;
		}


		public T_CONSTRUCTED_TYPE create(C preconditionMatches) {
			T_CONSTRUCTED_TYPE obj = constructor.apply(ListBuilder.newMutable(preconditionMatches));
			modifier.accept(obj);
			return obj;
		}


		public T_CONSTRUCTED_TYPE create(C[] preconditionMatches) {
			T_CONSTRUCTED_TYPE obj = constructor.apply(ListBuilder.newMutable(preconditionMatches));
			modifier.accept(obj);
			return obj;
		}


		public T_CONSTRUCTED_TYPE create(Collection<C> preconditionMatches) {
			T_CONSTRUCTED_TYPE obj = constructor.apply(new ArrayList<>(preconditionMatches));
			modifier.accept(obj);
			return obj;
		}

	}

}
