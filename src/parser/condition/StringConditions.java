package parser.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import lombok.val;
import parser.Inclusion;
import parser.textFragment.TextFragmentRef;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.util.arrayUtils.ArrayUtil;
import twg2.collections.util.dataStructures.Bag;
import twg2.functions.BiPredicates;
import twg2.parser.textParser.TextParser;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class StringConditions {
	private static ParserConditionFactory<BaseStringFilter, String> stringLiteralFactory = new ParserConditionFactory<>(BaseStringFilter::new, StringConditions::setupStringLiteralFilter);
	private static ParserConditionFactory<BaseStringFilter, String> startStringFactory = new ParserConditionFactory<>(BaseStringFilter::new, StringConditions::setupStartStringFilter);
	private static ParserConditionFactory<BaseStringFilter, String> endStringFactory = new ParserConditionFactory<>(BaseStringFilter::new, StringConditions::setupEndStringFilter);


	public static ParserConditionFactory<BaseStringFilter, String> stringLiteralFactory() {
		return stringLiteralFactory;
	}


	public static ParserConditionFactory<BaseStringFilter, String> startStringFactory() {
		return startStringFactory;
	}


	public static ParserConditionFactory<BaseStringFilter, String> endStringFactory() {
		return endStringFactory;
	}




	public static class Functionality {
		Consumer<BaseStringFilter> copyFunc;
		BiPredicates.CharObject<TextParser> acceptNextFunc;
		Runnable resetFunc;
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class BaseStringFilter implements ParserCondition.WithMarks {
		String[] originalStrs;
		Bag<String> matchingStrs;
		boolean anyComplete = false;
		boolean failed = false;
		/** count all accepted characters (including characters not explicitly part of 'matchingChars') */
		int acceptedCount = 0;
		/** count accepted characters (only from 'matchingChars') */
		int matchCount = 0;
		Inclusion includeMatchInRes;
		StringBuilder dstBuf = new StringBuilder();
		TextFragmentRef.ImplMut coords = new TextFragmentRef.ImplMut();
		/** Sets up accept and reset functions given this object */
		Functionality funcs;


		// package-private
		BaseStringFilter(Collection<String> strs, Inclusion includeCondMatchInRes) {
			this(strs.toArray(ArrayUtil.EMPTY_STRING_ARRAY), includeCondMatchInRes);
		}


		// package-private
		BaseStringFilter(String[] strs, Inclusion includeCondMatchInRes) {
			this.originalStrs = strs;
			this.matchingStrs = new Bag<String>(this.originalStrs, 0, this.originalStrs.length);
			this.anyComplete = false;
			this.includeMatchInRes = includeCondMatchInRes;
		}


		@Override
		public BaseStringFilter copy() {
			BaseStringFilter copy = new BaseStringFilter(originalStrs, includeMatchInRes);
			if(this.funcs.copyFunc != null) {
				this.funcs.copyFunc.accept(copy);
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
		public void getMatchFirstChars(CharList dst) {
			for(int i = 0, size = originalStrs.length; i < size; i++) {
				dst.add(originalStrs[i].charAt(0));
			}
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
			matchingStrs.clearAndAddAll(originalStrs);
			anyComplete = false;
			failed = false;
			dstBuf.setLength(0);
			coords = new TextFragmentRef.ImplMut();
			acceptedCount = 0;
			matchCount = 0;

			if(this.funcs.resetFunc != null) {
				this.funcs.resetFunc.run();
			}
		}


		@Override
		public String toString() {
			return "one " + Arrays.toString(this.originalStrs);
		}

	}


	/**
	 */
	public static BaseStringFilter setupStringLiteralFilter(BaseStringFilter cond) {
		return setupStartStringFilter(cond);
	}


	/**
	 */
	public static BaseStringFilter setupStartStringFilter(BaseStringFilter cond) {
		val funcs = new Functionality();
		cond.funcs = funcs;

		funcs.copyFunc = StringConditions::setupStartStringFilter;

		funcs.acceptNextFunc = (char ch, TextParser buf) -> {
			int off = cond.dstBuf.length();
			if(cond.acceptedCount > off) {
				cond.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<String> matchingStrs = cond.matchingStrs;
			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			for(int i = matchingStrs.size() - 1; i > -1; i--) {
				String strI = matchingStrs.get(i);
				int strILen = strI.length();
				// ignore string shorter than the current search offset (technically, if the precondition filter starts at offset 0, none of these should exist)
				if(strILen > off) {
					if(strI.charAt(off) != ch) {
						matchingStrs.remove(i);
					}
					else {
						anyFound = true;
						if(strILen == off + 1) {
							cond.anyComplete = true;
						}
					}
				}
				else {
					matchingStrs.remove(i);
				}
			}

			if(anyFound) {
				if(cond.acceptedCount == 0) {
					cond.coords.setStart(buf);
				}
				cond.acceptedCount++;
				cond.matchCount++;
				cond.dstBuf.append(ch);
				if(cond.anyComplete) {
					cond.coords.setEnd(buf);
				}
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


	/**
	 */
	public static BaseStringFilter setupEndStringFilter(BaseStringFilter cond) {
		val funcs = new Functionality();
		cond.funcs = funcs;

		funcs.copyFunc = StringConditions::setupEndStringFilter;

		funcs.acceptNextFunc = (char ch, TextParser buf) -> {
			int off = cond.dstBuf.length();
			if(cond.isComplete()) {
				cond.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<String> matchingStrs = cond.matchingStrs;
			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			for(int i = matchingStrs.size() - 1; i > -1; i--) {
				String strI = matchingStrs.get(i);
				int strILen = strI.length();
				// ignore string shorter than the current search offset (technically, if the precondition filter starts at offset 0, none of these should exist)
				if(strILen > off) {
					if(strI.charAt(off) != ch) {
						matchingStrs.remove(i);
					}
					else {
						anyFound = true;
						if(strILen == off + 1) {
							cond.anyComplete = true;
						}
					}
				}
				else {
					matchingStrs.remove(i);
				}
			}

			cond.matchCount++;

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
