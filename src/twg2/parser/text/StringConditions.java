package twg2.parser.text;

import java.util.Arrays;
import java.util.Collection;

import lombok.val;
import twg2.arrays.ArrayUtil;
import twg2.collections.dataStructures.Bag;
import twg2.functions.BiPredicates;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class StringConditions {


	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static abstract class BaseStringParser implements CharParserMatchable {
		String[] originalStrs;
		char[] firstChars;
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
		BiPredicates.CharObject<TextParser> firstCharMatcher;
		String name;


		// package-private
		BaseStringParser(String name, Collection<String> strs, Inclusion includeCondMatchInRes) {
			this(name, strs.toArray(ArrayUtil.EMPTY_STRING_ARRAY), includeCondMatchInRes);
		}


		// package-private
		BaseStringParser(String name, String[] strs, Inclusion includeCondMatchInRes) {
			this.originalStrs = strs;
			this.firstChars = new char[strs.length];
			for(int i = 0, size = strs.length; i < size; i++) {
				this.firstChars[i] = strs[i].charAt(0);
			}
			this.matchingStrs = new Bag<String>(this.originalStrs, 0, this.originalStrs.length);
			this.anyComplete = false;
			this.includeMatchInRes = includeCondMatchInRes;
			this.name = name;
			this.firstCharMatcher = (char ch, TextParser buf) -> {
				return ArrayUtil.indexOf(firstChars, ch) > -1;
			};
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
		public TextFragmentRef getCompleteMatchedTextCoords() {
			return coords;
		}


		@Override
		public BiPredicates.CharObject<TextParser> getFirstCharMatcher() {
			return firstCharMatcher;
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
		public CharParser recycle() {
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
		}


		@Override
		public String toString() {
			return "one " + Arrays.toString(this.originalStrs);
		}


		public static BaseStringParser copyTo(BaseStringParser src, BaseStringParser dst) {
			dst.originalStrs = src.originalStrs;
			dst.includeMatchInRes = src.includeMatchInRes;
			return dst;
		}

	}




	/**
	 */
	public static class Literal extends Start {

		public Literal(String name, String[] strs, Inclusion includeCondMatchInRes) {
			super(name, strs, includeCondMatchInRes);
		}


		@Override
		public Literal copy() {
			val copy = new Literal(name, originalStrs, includeMatchInRes);
			return copy;
		}

	}




	/**
	 */
	public static class Start extends BaseStringParser {

		public Start(String name, String[] strs, Inclusion includeCondMatchInRes) {
			super(name, strs, includeCondMatchInRes);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			int off = super.dstBuf.length();
			if(super.acceptedCount > off) {
				super.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<String> matchingStrs = super.matchingStrs;
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
							super.anyComplete = true;
						}
					}
				}
				else {
					matchingStrs.remove(i);
				}
			}

			if(anyFound) {
				if(super.acceptedCount == 0) {
					super.coords.setStart(buf);
				}
				super.acceptedCount++;
				super.matchCount++;
				super.dstBuf.append(ch);
				if(super.anyComplete) {
					super.coords.setEnd(buf);
				}
				return true;
			}
			else {
				super.failed = true;
				super.anyComplete = false;
				return false;
			}
		}


		@Override
		public Start copy() {
			val copy = new Start(super.name, super.originalStrs, super.includeMatchInRes);
			return copy;
		}

	}




	/**
	 */
	public static class End extends BaseStringParser {

		public End(String name, String[] strs, Inclusion includeCondMatchInRes) {
			super(name, strs, includeCondMatchInRes);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			int off = super.dstBuf.length();
			if(super.isComplete()) {
				super.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<String> matchingStrs = super.matchingStrs;
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
							super.anyComplete = true;
						}
					}
				}
				else {
					matchingStrs.remove(i);
				}
			}

			super.matchCount++;

			if(anyFound) {
				if(super.acceptedCount == 0) {
					super.coords.setStart(buf);
				}
				super.acceptedCount++;
				super.dstBuf.append(ch);
				if(super.anyComplete) {
					super.coords.setEnd(buf);
				}
			}
			else {
				super.reset();
			}
			return true;
		}


		@Override
		public End copy() {
			val copy = new End(super.name, super.originalStrs, super.includeMatchInRes);
			return copy;
		}

	}

}
