package parser.condition;

import java.util.Arrays;
import java.util.function.Consumer;

import lombok.val;
import parser.Inclusion;
import parser.textFragment.TextFragmentRef;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.primitiveCollections.CharListReadOnly;
import twg2.functions.BiPredicates;
import twg2.functions.Predicates;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParserUtils.ReadIsMatching;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class CharConditions {
	private static ParserConditionFactory.CharFilterFactory<BaseCharFilter> charLiteralFactory = new ParserConditionFactory.CharFilterFactory<>(BaseCharFilter::new, CharConditions::setupCharLiteralFilter);
	private static ParserConditionFactory.CharFilterFactory<BaseCharFilter> startCharFactory = new ParserConditionFactory.CharFilterFactory<>(BaseCharFilter::new, CharConditions::setupStartCharFilter);
	private static ParserConditionFactory.CharFilterFactory<BaseCharFilter> containsCharFactory = new ParserConditionFactory.CharFilterFactory<>(BaseCharFilter::new, CharConditions::setupContainsCharFilter);
	private static ParserConditionFactory.CharFilterFactory<BaseCharFilter> endCharFactory = new ParserConditionFactory.CharFilterFactory<>(BaseCharFilter::new, CharConditions::setupEndCharFilter);
	private static ParserConditionFactory.CharAugmentedFilterFactory<BaseCharFilter> endNotPrecededByCharFactory = new ParserConditionFactory.CharAugmentedFilterFactory<>(BaseCharFilter::new, CharConditions::setupEndCharNotPrecededByFilter);


	public static ParserConditionFactory.CharFilterFactory<BaseCharFilter> charLiteralFactory() {
		return charLiteralFactory;
	}


	public static ParserConditionFactory.CharFilterFactory<BaseCharFilter> startCharFactory() {
		return startCharFactory;
	}


	public static ParserConditionFactory.CharFilterFactory<BaseCharFilter> containsCharFactory() {
		return containsCharFactory;
	}


	public static ParserConditionFactory.CharFilterFactory<BaseCharFilter> endCharFactory() {
		return endCharFactory;
	}


	public static ParserConditionFactory.CharAugmentedFilterFactory<BaseCharFilter> endCharNotPrecededByFactory() {
		return endNotPrecededByCharFactory;
	}




	public static class Functionality {
		Consumer<BaseCharFilter> copyFunc;
		BiPredicates.CharObject<TextParser> acceptNextFunc;
		Runnable resetFunc;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class BaseCharFilter implements ParserCondition.WithMarks {
		char[] originalChars;
		// TODO testing CharBag matchingChars;
		boolean anyComplete = false;
		boolean failed = false;
		char acceptedChar = 0;
		/** count all accepted characters (including characters not explicitly part of 'matchingChars') */
		int acceptedCount = 0;
		/** count accepted characters (only from 'matchingChars') */
		int matchCount = 0;
		CharListReadOnly notPreceding;
		boolean lastCharNotMatch;
		Inclusion includeMatchInRes;
		TextFragmentRef.ImplMut coords = new TextFragmentRef.ImplMut();
		StringBuilder dstBuf = new StringBuilder();
		/** Sets up accept and reset functions given this object */
		Predicates.Char charMatcher;
		Object toStringSrc;
		Functionality funcs;


		public BaseCharFilter(CharList chars, Inclusion includeCondMatchInRes) {
			this(chars::contains, chars.toArray(), includeCondMatchInRes, null);
		}


		public BaseCharFilter(Predicates.Char charMatcher, char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			// TODO testing this.originalChars = chars;
			// TODO testing this.matchingChars = new CharBag(this.originalChars);
			this.originalChars = matchChars;
			this.charMatcher = charMatcher;
			this.anyComplete = false;
			this.includeMatchInRes = includeCondMatchInRes;
			this.funcs = new Functionality();
			this.toStringSrc = toStringSrc;
		}


		@Override
		public BaseCharFilter copy() {
			BaseCharFilter copy = new BaseCharFilter(charMatcher, originalChars, includeMatchInRes, toStringSrc);
			if(this.funcs.copyFunc != null) {
				this.funcs.copyFunc.accept(copy);
			}
			return copy;
		}


		@Override
		public TextFragmentRef getCompleteMatchedTextCoords() {
			return coords;
		}


		@Override
		public StringBuilder getParserDestination() {
			return dstBuf;
		}


		@Override
		public void setParserDestination(StringBuilder parserDestination) {
			this.dstBuf = parserDestination;
		}


		@Override
		public void getMatchFirstChars(CharList dst) {
			dst.addAll(originalChars);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			return this.funcs.acceptNextFunc.test(ch, buf);
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
		public ParserCondition recycle() {
			this.reset();
			return this;
		}


		// package-private
		void reset() {
			// TODO testing matchingChars.clear();
			// TODO testing matchingChars.addAll(originalChars);
			anyComplete = false;
			failed = false;
			acceptedChar = 0;
			acceptedCount = 0;
			matchCount = 0;
			lastCharNotMatch = false;
			coords = new TextFragmentRef.ImplMut();
			dstBuf.setLength(0);

			if(this.funcs.resetFunc != null) {
				this.funcs.resetFunc.run();
			}
		}


		@Override
		public String toString() {
			return "one " + (toStringSrc != null ? toStringSrc.toString() : Arrays.toString(this.originalChars));
		}

	}




	/**
	 */
	public static BaseCharFilter setupCharLiteralFilter(BaseCharFilter cond) {
		return setupStartCharFilter(cond);
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static BaseCharFilter setupStartCharFilter(BaseCharFilter cond) {
		val funcs = new Functionality();
		cond.funcs = funcs;

		funcs.copyFunc = CharConditions::setupStartCharFilter;

		funcs.acceptNextFunc = (char ch, TextParser buf) -> {
			// TODO testing CharList matchingChars = cond.matchingChars;
			if(cond.anyComplete || cond.failed) {
				cond.failed = true;
				return false;
			}
			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			cond.anyComplete = cond.charMatcher.test(ch);

			if(cond.anyComplete) {
				if(cond.acceptedCount == 0) {
					cond.coords.setStart(buf);
				}
				cond.dstBuf.append(ch);
				cond.acceptedChar = ch;
				cond.acceptedCount++;
				cond.matchCount++;
				cond.coords.setEnd(buf);
				return true;
			}
			else {
				cond.failed = true;
				return false;
			}
		};

		return cond;
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2015-11-27
	 */
	public static BaseCharFilter setupContainsCharFilter(BaseCharFilter cond) {
		val funcs = new Functionality();
		cond.funcs = funcs;

		funcs.copyFunc = CharConditions::setupContainsCharFilter;

		funcs.acceptNextFunc = (char ch, TextParser buf) -> {
			// TODO testing CharList matchingChars = cond.matchingChars;
			// fail if the condition is already complete
			if(cond.anyComplete) {
				cond.failed = true;
				return false;
			}

			if(cond.charMatcher.test(ch)) {
				if(cond.matchCount == 0) {
					cond.coords.setStart(buf);
				}
				cond.dstBuf.append(ch);
				cond.acceptedChar = ch;
				cond.acceptedCount++;
				cond.matchCount++;
				// this condition doesn't complete until the first non-matching character
				if(!ReadIsMatching.isNext(buf, cond.charMatcher, 1)) {
					cond.anyComplete = true;
					cond.coords.setEnd(buf); // TODO somewhat inefficient, but we can't be sure that calls to this function are sequential parser positions, so we can't move this to the failure condition
				}
				return true;
			}
			else {
				return false;
			}
		};

		return cond;
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static BaseCharFilter setupEndCharFilter(BaseCharFilter cond) {
		val funcs = new Functionality();
		cond.funcs = funcs;

		funcs.copyFunc = CharConditions::setupEndCharFilter;

		funcs.acceptNextFunc = (char ch, TextParser buf) -> {
			// TODO testing CharList matchingChars = cond.matchingChars;
			if(cond.anyComplete || cond.failed) {
				cond.failed = true;
				return false;
			}
			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			cond.anyComplete = cond.charMatcher.test(ch);

			if(cond.matchCount == 0) {
				cond.coords.setStart(buf);
			}

			if(cond.anyComplete) {
				cond.acceptedCount++;
				cond.acceptedChar = ch;
				cond.coords.setEnd(buf);
			}

			cond.dstBuf.append(ch);
			cond.matchCount++;

			return true;
		};

		return cond;
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static BaseCharFilter setupEndCharNotPrecededByFilter(BaseCharFilter cond, CharList notPrecededBy) {
		cond.notPreceding = notPrecededBy;

		val funcs = new Functionality();
		cond.funcs = funcs;

		funcs.copyFunc = (BaseCharFilter condToSetup) -> {
			CharConditions.setupEndCharNotPrecededByFilter(condToSetup, notPrecededBy);
			condToSetup.notPreceding = cond.notPreceding;
		};

		funcs.acceptNextFunc = (char ch, TextParser buf) -> {
			// TODO testing CharList matchingChars = cond.matchingChars;
			if(cond.anyComplete || cond.failed) {
				cond.failed = true;
				return false;
			}

			if(cond.notPreceding.contains(ch)) {
				cond.lastCharNotMatch = true;
				return true;
			}
			if(cond.lastCharNotMatch) {
				cond.reset();
				return true;
			}

			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			cond.anyComplete = cond.charMatcher.test(ch);

			if(cond.matchCount == 0) {
				cond.coords.setStart(buf);
			}

			if(cond.anyComplete) {
				cond.acceptedChar = ch;
				cond.acceptedCount++;
				cond.coords.setEnd(buf);
			}

			cond.dstBuf.append(ch);
			cond.matchCount++;

			return true;
		};

		return cond;
	}

}
