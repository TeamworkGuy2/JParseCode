package twg2.parser.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.primitiveCollections.CharListSorted;
import twg2.collections.util.ListBuilder;
import twg2.parser.condition.ParserCondition;
import twg2.parser.condition.text.CharParser;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;
import twg2.text.stringUtils.StringJoin;

/** A compound {@link CharParser} that does not complete until all internal conditions have been completed
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class CharConditionPipe {


	public static abstract class BasePipe<T extends ParserCondition> implements CharParser {
		final boolean canReuse;
		final List<List<T>> conditionSets; // FIFO list of conditions in this pipe
		int curSetIndex;
		int curCondIndex;
		T curCondition; // the current condition
		boolean anyComplete;
		boolean failed;
		/** true if conditionSets after the initial one are optional, false if not */
		boolean subseqentConditionSetsOptional;
		StringBuilder dstBuf;
		String name;


		{
			this.curSetIndex = 0;
			this.curCondIndex = 0;
			this.dstBuf = new StringBuilder();
		}


		@SafeVarargs
		public BasePipe(String name, T... filters) {
			List<T> condSet0 = new ArrayList<>();
			this.conditionSets = new ArrayList<>();
			this.conditionSets.add(condSet0);

			Collections.addAll(condSet0, filters);
			this.curCondition = condSet0.get(0);
			this.canReuse = ParserCondition.canRecycleAll(condSet0);
			this.name = name;
		}


		public BasePipe(String name, Collection<T> filters) {
			List<T> condSet0 = new ArrayList<>();
			this.conditionSets = new ArrayList<>();
			this.conditionSets.add(condSet0);

			condSet0.addAll(filters);
			this.curCondition = condSet0.get(0);
			this.canReuse = ParserCondition.canRecycleAll(condSet0);
			this.name = name;
		}


		public BasePipe(String name, List<? extends List<T>> filterSets) {
			@SuppressWarnings("unchecked")
			List<List<T>> filterSetsCast = (List<List<T>>)filterSets;
			this.conditionSets = filterSetsCast;
			this.curCondition = this.conditionSets.size() > 0 ? this.conditionSets.get(0).get(0) : null;
			boolean reusable = true;
			for(List<T> filterSet : filterSets) {
				reusable &= ParserCondition.canRecycleAll(filterSet);
			}
			this.canReuse = reusable;
			this.name = name;
		}


		@Override
		public String name() {
			return name;
		}


		@Override
		public StringBuilder getParserDestination() {
			return this.dstBuf;
		}


		@Override
		public void setParserDestination(StringBuilder parserDestination) {
			this.dstBuf = parserDestination;
		}


		@Override
		public boolean isComplete() {
			return !failed && anyComplete;
		}


		@Override
		public boolean isFailed() {
			return failed;
		}


		@Override
		public boolean canRecycle() {
			return canReuse;
		}


		@Override
		public CharParser recycle() {
			this.reset();
			return this;
		}


		void reset() {
			curCondIndex = 0;
			curSetIndex = 0;
			anyComplete = false;
			failed = false;
			dstBuf.setLength(0);
			for(int i = 0, size = conditionSets.size(); i < size; i++) {
				val condSet = conditionSets.get(i);
				for(int ii = 0, sizeI = condSet.size(); ii < sizeI; ii++) {
					@SuppressWarnings("unchecked")
					T condCopy = (T)condSet.get(ii).copyOrReuse();
					condSet.set(ii, condCopy);
				}
			}
			curCondition = conditionSets.size() > 0 && conditionSets.get(0).size() > 0 ? conditionSets.get(0).get(0) : null;

			/* TODO testing
			if(this.funcs.resetFunc != null) {
				this.funcs.resetFunc.run();
			}
			*/
		}


		@Override
		public String toString() {
			return conditionSetToString(conditionSets, ", then ", "", '(', ')');
		}


		public static <S extends ParserCondition> List<List<S>> copyConditionSets(BasePipe<S> src) {
			List<List<S>> condCopies = new ArrayList<>(src.conditionSets.size());
			for(int i = 0, size = src.conditionSets.size(); i < size; i++) {
				List<S> condSet = src.conditionSets.get(i);
				List<S> condCopy = new ArrayList<>(condSet.size());
				condCopies.add(condCopy);
				for(int ii = 0, sizeI = condSet.size(); ii < sizeI; ii++) {
					@SuppressWarnings("unchecked")
					S copy = (S)condSet.get(ii).copy();
					condCopy.add(copy);
				}
			}

			return condCopies;
		}


		public static <S extends ParserCondition> BasePipe<S> copyTo(BasePipe<S> src, BasePipe<S> dst) {
			// TODO testing BasePipe<T> copy = new BasePipe<>(condCopies);
			dst.subseqentConditionSetsOptional = src.subseqentConditionSetsOptional;

			/* TODO testing
			if(funcs.copyFunc != null) {
				funcs.copyFunc.accept(copy);
			}
			*/
			return dst;
		}


		public static String conditionSetToString(List<? extends List<?>> lists, String joiner, String prefixFirst, char prefixDelimiter, char suffixDelimiter) {
			StringBuilder sb = new StringBuilder();
			sb.append(prefixFirst);
			int maxI = lists.size() - 1;
			for(int i = 0; i < maxI; i++) {
				sb.append(prefixDelimiter);
				sb.append(StringJoin.Objects.join(lists.get(i), joiner));
				sb.append(suffixDelimiter);
				sb.append(joiner);
			}
			if(maxI > -1) {
				sb.append(prefixDelimiter);
				sb.append(StringJoin.Objects.join(lists.get(maxI), joiner));
				sb.append(suffixDelimiter);
			}
			return sb.toString();
		}

	}




	/** A {@link CharConditionPipe} with the same type of {@link CharParser} from start to end
	 * @param <T> the type of parser mark conditions in this pipe
	 * @author TeamworkGuy2
	 * @since 2015-2-22
	 */
	public static abstract class WithMarks<T extends CharParser> extends BasePipe<CharParser> implements CharParser.WithMarks {
		private char[] firstChars;


		public WithMarks(String name, CharParser.WithMarks firstFilter, T filter) {
			super(name, firstFilter);
			val condSet = super.conditionSets.get(0);
			condSet.add(filter);

			initFirstChars(firstFilter);
		}


		@SafeVarargs
		public WithMarks(String name, CharParser.WithMarks firstFilter, T... filters) {
			super(name, firstFilter);
			val condSet = super.conditionSets.get(0);
			Collections.addAll(condSet, filters);

			initFirstChars(firstFilter);
		}


		public WithMarks(String name, CharParser.WithMarks firstFilter, Collection<T> filters) {
			super(name, firstFilter);
			val condSet = super.conditionSets.get(0);
			condSet.addAll(filters);

			initFirstChars(firstFilter);
		}


		@SuppressWarnings("unchecked")
		public WithMarks(String name, List<? extends List<? extends T>> filterSets) {
			super(name, (List<List<CharParser>>)filterSets);
			CharParser.WithMarks firstFilter = (CharParser.WithMarks)super.conditionSets.get(0).get(0);
			initFirstChars(firstFilter);
		}


		private final void initFirstChars(CharParser.WithMarks firstFilter) {
			CharList firstCharsList = new CharListSorted();
			firstFilter.getMatchFirstChars(firstCharsList);
			this.firstChars = firstCharsList.toArray();
		}


		@Override
		public void getMatchFirstChars(CharList dst) {
			dst.addAll(firstChars);
		}

	}


	@SafeVarargs
	public static <S extends CharParser> BasePipe<CharParser> createPipeAllRequired(String name, Iterable<S>... requiredConditionSets) {
		val requiredSets = new ArrayList<List<S>>();
		for(Iterable<S> requiredCondSet : requiredConditionSets) {
			val requiredSet = ListBuilder.newMutable(requiredCondSet);
			requiredSets.add(requiredSet);
		}
		
		val cond = new AllRequired<S>(name, requiredSets);
		return cond;
	}


	public static <S extends CharParser> BasePipe<CharParser> createPipeRepeatableSeparator(String name, Iterable<? extends S> requiredConditions, Iterable<? extends S> separatorConditions) {
		val elementSet = ListBuilder.newMutable(requiredConditions);
		val separatorSet = ListBuilder.newMutable(separatorConditions);

		val cond = new RepeatableSeparator<S>(name, new ArrayList<>(Arrays.asList(elementSet, separatorSet)));
		return cond;
	}


	@SafeVarargs
	public static <S extends CharParser> BasePipe<CharParser> createPipeOptionalSuffix(String name, Iterable<? extends S> requiredConditions, Iterable<? extends S>... optionalConditions) {
		val conditionSets = new ArrayList<List<S>>();
		@SuppressWarnings("unchecked")
		List<S> requiredCondsCopy = ListBuilder.newMutable((Iterable<S>)requiredConditions);
		conditionSets.add(requiredCondsCopy);

		@SuppressWarnings("unchecked")
		Iterable<S>[] optionalCondsCast = (Iterable<S>[])optionalConditions;
		for(Iterable<S> condSet : optionalCondsCast) {
			val requiredSet = ListBuilder.newMutable(condSet);
			conditionSets.add(requiredSet);
		}

		val cond = new OptionalSuffix<S>(name, conditionSets);
		return cond;
	}




	public static class AllRequired<S extends CharParser> extends WithMarks<S> {
		private TextFragmentRef.ImplMut coords = null;


		@SafeVarargs
		public AllRequired(String name, CharParser.WithMarks filter, S... filters) {
			super(name, filter, filters);
		}


		public AllRequired(String name, CharParser.WithMarks filter, Collection<S> filters) {
			super(name, filter, filters);
		}


		public AllRequired(String name, List<? extends List<S>> filterSets) {
			super(name, filterSets);
		}


		@Override
		public TextFragmentRef getCompleteMatchedTextCoords() {
			return this.coords;
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			if(this.curCondition == null) {
				this.failed = true;
				return false;
			}

			boolean res = this.curCondition.acceptNext(ch, buf);
			// when complete
			if(this.curCondition.isComplete()) {
				val curCondCoords = this.curCondition.getCompleteMatchedTextCoords();
				this.coords = this.coords == null ? TextFragmentRef.copyMutable(curCondCoords) : TextFragmentRef.merge(this.coords, this.coords, curCondCoords);

				// get the next condition
				this.curCondIndex++;
				if(this.curCondIndex < this.conditionSets.get(this.curSetIndex).size()) {
					this.curCondition = this.conditionSets.get(this.curSetIndex).get(this.curCondIndex);
				}
				else if(this.curSetIndex < this.conditionSets.size() - 1) {
					this.curSetIndex++;
					this.curCondIndex = 0;
					this.curCondition = this.conditionSets.get(this.curSetIndex).get(this.curCondIndex);
				}
				// or there are no conditions left (this precondition filter is complete)
				else {
					this.anyComplete = true;
					this.curCondition = null;
				}
			}

			if(!res) {
				this.failed = true;
			}
			else {
				this.dstBuf.append(ch);
			}

			return res;
		}


		@Override
		void reset() {
			super.reset();
			this.coords = null;
		}


		@Override
		public CharParser copy() {
			val copy = new AllRequired<>(name, BasePipe.copyConditionSets(this));
			BasePipe.copyTo(this, copy);
			return copy;
		}

	}




	public static abstract class AcceptNextAllRequired<S extends CharParser> extends WithMarks<S> {
		private TextFragmentRef.ImplMut coords = null;


		@SafeVarargs
		public AcceptNextAllRequired(String name, CharParser.WithMarks filter, S... filters) {
			super(name, filter, filters);
		}


		public AcceptNextAllRequired(String name, CharParser.WithMarks filter, Collection<S> filters) {
			super(name, filter, filters);
		}


		public AcceptNextAllRequired(String name, List<? extends List<? extends S>> filterSets) {
			super(name, filterSets);
		}


		public abstract CharParser nextCondition();


		@Override
		public TextFragmentRef getCompleteMatchedTextCoords() {
			return this.coords;
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			if(this.curCondition == null) {
				this.failed = true;
				return false;
			}

			boolean res = this.curCondition.acceptNext(ch, buf);
			// when complete
			if(this.curCondition.isComplete()) {
				val curCondCoords = this.curCondition.getCompleteMatchedTextCoords();
				this.coords = this.coords == null ? TextFragmentRef.copyMutable(curCondCoords) : TextFragmentRef.merge(this.coords, this.coords, curCondCoords);

				// get the next condition, or null
				this.curCondition = this.nextCondition();

				// required parser done, optional parsers next
				if(this.curSetIndex > 0 && this.curCondition != null) {
					if(buf.hasNext()) {
						// peek at next buffer character, if optional parser accepts, lock into parsing the optional parser
						char nextCh = buf.nextChar();
						buf.unread(1);
						boolean nextRes = this.curCondition.acceptNext(nextCh, buf);
						this.curCondition = this.curCondition.copyOrReuse();
						if(nextRes) {
							this.anyComplete = false;
						}
						// else, the required parser is complete, so isComplete() is valid
						else {
							this.anyComplete = true;
							this.curCondition = null;
						}
					}
					// no further parser input available, but since the required parser is already complete, isComplete() is valid
					else if(this.subseqentConditionSetsOptional) {
						this.anyComplete = true;
						this.curCondition = null;
					}
				}
			}

			if(!res) {
				this.failed = true;
			}
			else {
				this.dstBuf.append(ch);
			}

			return res;
		}


		@Override
		void reset() {
			super.reset();
			this.coords = null;
		}

	}




	public static class OptionalSuffix<S extends CharParser> extends AcceptNextAllRequired<S> {

		@SafeVarargs
		public OptionalSuffix(String name, CharParser.WithMarks filter, S... filters) {
			super(name, filter, filters);
			setup();
		}


		public OptionalSuffix(String name, CharParser.WithMarks filter, Collection<S> filters) {
			super(name, filter, filters);
			setup();
		}


		public OptionalSuffix(String name, List<? extends List<S>> filterSets) {
			super(name, filterSets);
			setup();
		}


		private final void setup() {
			super.subseqentConditionSetsOptional = true;
		}


		@Override
		public CharParser nextCondition() {
			List<CharParser> curCondSet = super.conditionSets.get(super.curSetIndex);
			curCondSet.set(super.curCondIndex, super.curCondition.copyOrReuse());

			super.curCondIndex++;

			// advance to the next condition in the current set
			if(super.curCondIndex < curCondSet.size()) {
				return curCondSet.get(super.curCondIndex);
			}
			// advance to the next set of conditions
			else if(super.curSetIndex < super.conditionSets.size() - 1) {
				super.anyComplete = true;
				super.curSetIndex++;
				super.curCondIndex = 0;
				curCondSet = super.conditionSets.get(super.curSetIndex);
				return curCondSet.size() > 0 ? curCondSet.get(super.curCondIndex) : null;
			}
			// or there are no conditions left (this precondition filter is complete)
			else {
				super.anyComplete = true;
				return null;
			}
		}


		@Override
		public CharParser copy() {
			val copy = new OptionalSuffix<>(name, BasePipe.copyConditionSets(this));
			BasePipe.copyTo(this, copy);
			return copy;
		}


		@Override
		public String toString() {
			return BasePipe.conditionSetToString(super.conditionSets, ", optional then ", "", '(', ')');
		}

	}




	public static class RepeatableSeparator<S extends CharParser> extends AcceptNextAllRequired<S> {

		@SafeVarargs
		public RepeatableSeparator(String name, CharParser.WithMarks filter, S... filters) {
			super(name, filter, filters);
			setup();
		}


		public RepeatableSeparator(String name, CharParser.WithMarks filter, Collection<S> filters) {
			super(name, filter, filters);
			setup();
		}


		public RepeatableSeparator(String name, List<? extends List<? extends S>> filterSets) {
			super(name, filterSets);
			setup();
		}


		private final void setup() {
			if(super.conditionSets.size() > 2) {
				// technically this allows for 1 element repeating parsers
				throw new IllegalStateException("a repeatable separator pipe condition can only contain 2 condition sets, the element parser and the separator parser");
			}
			super.subseqentConditionSetsOptional = false;
		}


		@Override
		public CharParser nextCondition() {
			List<CharParser> curCondSet = super.conditionSets.get(super.curSetIndex);
			curCondSet.set(super.curCondIndex, super.curCondition.copyOrReuse());

			super.curCondIndex++;

			if(super.curCondIndex < curCondSet.size()) {
				return curCondSet.get(super.curCondIndex);
			}
			// advance to the separator parser or back to the element parser
			else {
				// advance to the separator parser
				if(super.curSetIndex == 0) {
					super.anyComplete = true; // the element parser just completed, so it's a valid parser stop point
					super.curSetIndex = (super.curSetIndex + 1) % super.conditionSets.size(); // allows for 1 condition
				}
				// cycle back to the element parser set
				else if(super.curSetIndex > 0) {
					super.curSetIndex = 0;
				}
				else {
					throw new AssertionError("unknown repeatable separator pipe condition state");
				}

				super.curCondIndex = 0;
				curCondSet = super.conditionSets.get(super.curSetIndex);
				for(int i = 0, size = curCondSet.size(); i < size; i++) {
					@SuppressWarnings("unchecked")
					val curCond = (S)curCondSet.get(i).copyOrReuse();
					curCondSet.set(i, curCond);
				}
				return curCondSet.size() > 0 ? curCondSet.get(super.curCondIndex) : null;
			}
		}


		@Override
		public CharParser copy() {
			val copy = new RepeatableSeparator<>(name, BasePipe.copyConditionSets(this));
			BasePipe.copyTo(this, copy);
			return copy;
		}


		@Override
		public String toString() {
			return BasePipe.conditionSetToString(super.conditionSets, ", separator ", "element ", '(', ')');
		}

	}

}
