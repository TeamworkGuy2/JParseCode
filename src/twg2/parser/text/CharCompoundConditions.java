package twg2.parser.text;

import java.util.Collection;
import java.util.function.Consumer;

import streamUtils.StreamMap;
import twg2.collections.util.dataStructures.Bag;
import twg2.functions.BiPredicates;
import twg2.parser.condition.ParserConditionFactory;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-3-07
 */
public class CharCompoundConditions {
	private static ParserConditionFactory.CompoundFactory<BaseFilter, CharParserCondition> filterFactory = new ParserConditionFactory.CompoundFactory<>(BaseFilter::new, CharCompoundConditions::setupFilter);
	private static ParserConditionFactory.CompoundFactory<BaseFilter, CharParserCondition> startFactory = new ParserConditionFactory.CompoundFactory<>(BaseFilter::new, CharCompoundConditions::setupStartFilter);
	private static ParserConditionFactory.CompoundFactory<BaseFilter, CharParserCondition> endFactory = new ParserConditionFactory.CompoundFactory<>(BaseFilter::new, CharCompoundConditions::setupEndFilter);


	public static ParserConditionFactory.CompoundFactory<BaseFilter, CharParserCondition> filterFactory() {
		return filterFactory;
	}


	public static ParserConditionFactory.CompoundFactory<BaseFilter, CharParserCondition> startFilterFactory() {
		return startFactory;
	}


	public static ParserConditionFactory.CompoundFactory<BaseFilter, CharParserCondition> endFilterFactory() {
		return endFactory;
	}




	/** A collection of {@link CharParserCondition ParserConditions}
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class BaseFilter implements CharParserCondition {
		CharParserCondition[] originalConds;
		Bag<CharParserCondition> matchingConds;
		boolean anyComplete = false;
		boolean failed = false;
		int acceptedCount;
		StringBuilder dstBuf = new StringBuilder();
		TextFragmentRef.ImplMut coords = new TextFragmentRef.ImplMut();
		/** Sets up accept and reset functions given this object */
		Consumer<BaseFilter> copyFunc;
		BiPredicates.CharObject<TextParser> acceptNextFunc;
		Runnable resetFunc;


		public BaseFilter(Collection<CharParserCondition> conds) {
			this(conds.toArray(new CharParserCondition[conds.size()]));
		}


		@SafeVarargs
		public BaseFilter(CharParserCondition... conds) {
			this.originalConds = conds;
			this.matchingConds = new Bag<>(this.originalConds, 0, this.originalConds.length);
			this.anyComplete = false;
		}


		@Override
		public CharParserCondition copy() {
			BaseFilter copy = new BaseFilter(StreamMap.map(originalConds, (c) -> c.copy()));
			if(copyFunc != null) {
				copyFunc.accept(copy);
			}
			return copy;
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
		public TextFragmentRef getCompleteMatchedTextCoords() {
			return coords;
		}


		@Override
		public boolean isComplete() {
			return anyComplete && !failed;
		}


		@Override
		public boolean isFailed() {
			return failed;
		}


		@Override
		public boolean canRecycle() {
			return true;
		}


		@Override
		public CharParserCondition recycle() {
			this.reset();
			return this;
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			return acceptNextFunc.test(ch, buf);
		}


		// package-private
		void reset() {
			matchingConds.clearAndAddAll(originalConds);
			anyComplete = false;
			failed = false;
			dstBuf.setLength(0);
			coords = new TextFragmentRef.ImplMut();
			acceptedCount = 0;

			if(resetFunc != null) {
				resetFunc.run();
			}
		}


		@Override
		public String toString() {
			return StringJoin.Objects.join(originalConds, ", or ");
		}

	}




	/** Accept input that matches this parse condition
	 * @author TeamworkGuy2
	 * @since 2015-6-26
	 */
	public static BaseFilter setupFilter(BaseFilter cond) {
		return setupStartFilter(cond);
	}


	/** Accept input that matches this parse condition
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static BaseFilter setupStartFilter(BaseFilter cond) {
		cond.copyFunc = CharCompoundConditions::setupStartFilter;

		cond.acceptNextFunc = (char ch, TextParser buf) -> {
			int off = cond.dstBuf.length();
			if(cond.acceptedCount > off) {
				cond.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<CharParserCondition> matchingConds = cond.matchingConds;
			CharParserCondition condI = null;
			// reverse iterate through the bag so we don't have to adjust the loop variable when we remove elements
			for(int i = matchingConds.size() - 1; i > -1; i--) {
				condI = matchingConds.get(i);
				if(!condI.isFailed()) {
					if(!condI.acceptNext(ch, buf)) {
						matchingConds.remove(i);
					}
					else {
						anyFound = true;
						if(condI.isComplete()) {
							cond.anyComplete = true;
						}
					}
				}
				else {
					matchingConds.remove(i);
				}
			}

			if(anyFound) {
				if(cond.acceptedCount == 0) {
					cond.coords.setStart(buf);
				}
				cond.acceptedCount++;
				cond.dstBuf.append(ch);
				if(cond.anyComplete) {
					cond.coords.setEnd(buf);
				}

				// TODO debugging - ensure that this compound condition ends up with the same parsed text fragment as the sub-conditions
				//if(cond.anyComplete && !condI.getCompleteMatchedTextCoords().equals(cond.coords)) {
				//	throw new RuntimeException("CharCompoundConditions " + condI.getCompleteMatchedTextCoords().toString() + " not equal to sub " + cond + " " + cond.coords.toString());
				//}

				return true;
			}
			else {
				cond.failed = true;
				cond.anyComplete = false;
				return false;
			}
		};

		return cond;
	}


	/** Accept input until a full match for this parse condition is encountered
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static BaseFilter setupEndFilter(BaseFilter cond) {
		cond.copyFunc = CharCompoundConditions::setupEndFilter;

		cond.acceptNextFunc = (char ch, TextParser buf) -> {
			if(cond.isComplete()) {
				cond.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<CharParserCondition> matchingConds = cond.matchingConds;
			// reverse iterate through the bag so we don't have to adjust the loop variable when we remove elements
			for(int i = matchingConds.size() - 1; i > -1; i--) {
				CharParserCondition condI = matchingConds.get(i);
				if(!condI.isFailed()) {
					if(!condI.acceptNext(ch, buf)) {
						matchingConds.remove(i);
					}
					else {
						anyFound = true;
						if(condI.isComplete()) {
							cond.anyComplete = true;
						}
					}
				}
				else {
					matchingConds.remove(i);
				}
			}

			if(anyFound) {
				if(cond.acceptedCount == 0) {
					cond.coords.setStart(buf);
				}
				cond.acceptedCount++;
				cond.dstBuf.append(ch);
				if(cond.anyComplete) {
					cond.coords.setEnd(buf);
				}
			}
			else {
				cond.reset();
			}
			return true;
		};

		return cond;
	}

}
