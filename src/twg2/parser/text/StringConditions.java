package twg2.parser.text;

import java.util.Arrays;
import java.util.Collection;

import lombok.val;
import twg2.arrays.ArrayUtil;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.util.dataStructures.Bag;
import twg2.parser.Inclusion;
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
	public static abstract class BaseStringFilter implements CharParserCondition.WithMarks {
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
		String name;


		// package-private
		BaseStringFilter(String name, Collection<String> strs, Inclusion includeCondMatchInRes) {
			this(name, strs.toArray(ArrayUtil.EMPTY_STRING_ARRAY), includeCondMatchInRes);
		}


		// package-private
		BaseStringFilter(String name, String[] strs, Inclusion includeCondMatchInRes) {
			this.originalStrs = strs;
			this.matchingStrs = new Bag<String>(this.originalStrs, 0, this.originalStrs.length);
			this.anyComplete = false;
			this.includeMatchInRes = includeCondMatchInRes;
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


		public static BaseStringFilter copyTo(BaseStringFilter src, BaseStringFilter dst) {
			dst.originalStrs = src.originalStrs;
			dst.includeMatchInRes = src.includeMatchInRes;
			return dst;
		}

	}




	/**
	 */
	public static class StringLiteralFilter extends StartStringFilter {

		public StringLiteralFilter(String name, String[] strs, Inclusion includeCondMatchInRes) {
			super(name, strs, includeCondMatchInRes);
		}


		@Override
		public StringLiteralFilter copy() {
			val copy = new StringLiteralFilter(name, originalStrs, includeMatchInRes);
			return copy;
		}

	}




	/**
	 */
	public static class StartStringFilter extends BaseStringFilter {

		public StartStringFilter(String name, String[] strs, Inclusion includeCondMatchInRes) {
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
		public CharParserCondition copy() {
			val copy = new StartStringFilter(super.name, super.originalStrs, super.includeMatchInRes);
			return copy;
		}

	}




	/**
	 */
	public static class EndStringFilter extends BaseStringFilter {

		public EndStringFilter(String name, String[] strs, Inclusion includeCondMatchInRes) {
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
		public EndStringFilter copy() {
			val copy = new EndStringFilter(super.name, super.originalStrs, super.includeMatchInRes);
			return copy;
		}

	}

}
