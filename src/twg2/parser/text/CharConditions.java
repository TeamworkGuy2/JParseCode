package twg2.parser.text;

import java.util.Arrays;

import lombok.val;
import twg2.arrays.ArrayUtil;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.primitiveCollections.CharListReadOnly;
import twg2.functions.BiPredicates;
import twg2.functions.Predicates;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParserUtils.ReadIsMatching;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class CharConditions {


	/**
	 * @author TeamworkGuy2
	 * @since 2016-2-20
	 */
	public static abstract class BaseCharParser implements CharParser {
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
		String name;


		public BaseCharParser(String name, Predicates.Char charMatcher, Inclusion includeCondMatchInRes, Object toStringSrc) {
			this.charMatcher = charMatcher;
			this.anyComplete = false;
			this.includeMatchInRes = includeCondMatchInRes;
			this.toStringSrc = toStringSrc;
			this.name = name;
		}


		@Override
		public String name() {
			return name;
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
		void acceptedCompletedChar(char ch, TextParser buf) {
			if(this.matchCount == 0) {
				this.coords.setStart(buf);
			}
			this.dstBuf.append(ch);
			this.acceptedChar = ch;
			this.acceptedCount++;
			this.matchCount++;
		}


		void acceptedMatchOrCompleteChar(char ch, TextParser buf) {
			if(this.matchCount == 0) {
				this.coords.setStart(buf);
			}

			if(this.anyComplete) {
				this.acceptedCount++;
				this.acceptedChar = ch;
				this.coords.setEnd(buf);
			}

			this.dstBuf.append(ch);
			this.matchCount++;
		}


		void reset() {
			anyComplete = false;
			failed = false;
			acceptedChar = 0;
			acceptedCount = 0;
			matchCount = 0;
			lastCharNotMatch = false;
			coords = new TextFragmentRef.ImplMut();
			dstBuf.setLength(0);
		}


		@Override
		public String toString() {
			return "one " + (toStringSrc != null ? toStringSrc.toString() : charMatcher);
		}


		public static BaseCharParser copyTo(BaseCharParser src, BaseCharParser dst) {
			dst.includeMatchInRes = src.includeMatchInRes;
			dst.charMatcher = src.charMatcher;
			dst.toStringSrc = src.toStringSrc;
			dst.name = src.name;
			return dst;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static abstract class BaseCharParserMatchable extends BaseCharParser implements CharParserMatchable {
		char[] firstMatchChars;
		Predicates.Char origMatcher;
		BiPredicates.CharObject<TextParser> firstCharMatcher;


		public BaseCharParserMatchable(String name, CharList chars, Inclusion includeCondMatchInRes) {
			this(name, chars::contains, null, chars.toArray(), includeCondMatchInRes, null);
		}


		public BaseCharParserMatchable(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher, char[] firstMatchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, includeCondMatchInRes, toStringSrc);
			this.firstMatchChars = firstMatchChars;
			this.firstCharMatcher = (char ch, TextParser buf) -> {
				return ArrayUtil.indexOf(firstMatchChars, ch) > -1;
			};
			this.origMatcher = firstCharMatcher;
		}


		@Override
		public BiPredicates.CharObject<TextParser> getFirstCharMatcher() {
			return firstCharMatcher;
		}


		@Override
		public String toString() {
			return "one " + (toStringSrc != null ? toStringSrc.toString() : Arrays.toString(firstMatchChars));
		}


		public static BaseCharParserMatchable copyTo(BaseCharParserMatchable src, BaseCharParserMatchable dst) {
			dst.firstMatchChars = src.firstMatchChars;
			dst.includeMatchInRes = src.includeMatchInRes;
			dst.charMatcher = src.charMatcher;
			dst.firstCharMatcher = src.firstCharMatcher;
			dst.toStringSrc = src.toStringSrc;
			dst.name = src.name;
			return dst;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static class Start extends BaseCharParserMatchable {

		public Start(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public Start(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher,
				char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, firstCharMatcher, matchChars, includeCondMatchInRes, toStringSrc);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			if(super.anyComplete || super.failed) {
				super.failed = true;
				return false;
			}

			super.anyComplete = super.charMatcher.test(ch);

			if(super.anyComplete) {
				super.acceptedCompletedChar(ch, buf);
				super.coords.setEnd(buf);
				return true;
			}
			else {
				super.failed = true;
				return false;
			}
		}


		@Override
		public Start copy() {
			val copy = new Start(super.name, super.charMatcher, super.origMatcher, super.firstMatchChars, super.includeMatchInRes, super.toStringSrc);
			return copy;
		}

	}




	/**
	 */
	public static class Literal extends Start {

		public Literal(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public Literal(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher,
				char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, firstCharMatcher, matchChars, includeCondMatchInRes, toStringSrc);
		}


		@Override
		public Literal copy() {
			val copy = new Literal(super.name, super.charMatcher, super.origMatcher, super.firstMatchChars, super.includeMatchInRes, super.toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-13
	 */
	public static class ContainsFirstSpecial extends BaseCharParserMatchable {

		public ContainsFirstSpecial(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public ContainsFirstSpecial(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher,
				char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, firstCharMatcher, matchChars, includeCondMatchInRes, toStringSrc);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			// fail if the condition is already complete
			if(super.anyComplete) {
				super.failed = true;
				return false;
			}

			if(super.matchCount == 0 ? super.firstCharMatcher.test(ch, buf) : super.charMatcher.test(ch)) {
				super.acceptedCompletedChar(ch, buf);

				// this condition doesn't complete until the first non-matching character
				if(!ReadIsMatching.isNext(buf, super.charMatcher, 1)) {
					super.anyComplete = true;
					super.coords.setEnd(buf); // TODO somewhat inefficient, but we can't be sure that calls to this function are sequential parser positions, so we can't move this to the failure condition
				}
				return true;
			}
			else {
				return false;
			}
		}


		@Override
		public ContainsFirstSpecial copy() {
			val copy = new ContainsFirstSpecial(super.name, super.charMatcher, super.origMatcher, super.firstMatchChars, super.includeMatchInRes, super.toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-11-27
	 */
	public static class Contains extends ContainsFirstSpecial {

		public Contains(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public Contains(String name, Predicates.Char charMatcher,
				char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, charMatcher, matchChars, includeCondMatchInRes, toStringSrc);
		}


		@Override
		public Contains copy() {
			val copy = new Contains(super.name, super.charMatcher, super.firstMatchChars, super.includeMatchInRes, super.toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static class End extends BaseCharParserMatchable {

		public End(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public End(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher,
				char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, firstCharMatcher, matchChars, includeCondMatchInRes, toStringSrc);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			if(super.anyComplete || super.failed) {
				super.failed = true;
				return false;
			}
			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			super.anyComplete = super.charMatcher.test(ch);

			super.acceptedMatchOrCompleteChar(ch, buf);

			return true;
		}


		@Override
		public End copy() {
			val copy = new End(super.name, super.charMatcher, super.origMatcher, super.firstMatchChars, super.includeMatchInRes, super.toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class EndNotPrecededBy extends BaseCharParserMatchable {
		private final int minPreEndChars;


		public EndNotPrecededBy(String name, CharList chars, int minPreEndChars, Inclusion includeCondMatchInRes, CharListReadOnly notPrecededBy) {
			super(name, chars::contains, null, chars.toArray(), includeCondMatchInRes, null);
			super.notPreceding = notPrecededBy;
			this.minPreEndChars = minPreEndChars;
		}


		public EndNotPrecededBy(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher,
				char[] matchChars, int minPreEndChars, Inclusion includeCondMatchInRes, Object toStringSrc, CharListReadOnly notPrecededBy) {
			super(name, charMatcher, firstCharMatcher, matchChars, includeCondMatchInRes, toStringSrc);
			super.notPreceding = notPrecededBy;
			this.minPreEndChars = minPreEndChars;
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			if(super.anyComplete || super.failed) {
				super.failed = true;
				return false;
			}

			if(super.notPreceding.contains(ch)) {
				super.lastCharNotMatch = true;
				return true;
			}
			if(super.lastCharNotMatch) {
				super.reset();
				return true;
			}

			// reverse iterate through the bag so we don't have to adjust the loop counter when we remove elements
			super.anyComplete = super.charMatcher.test(ch) && (this.minPreEndChars == 0 || super.matchCount >= this.minPreEndChars);

			super.acceptedMatchOrCompleteChar(ch, buf);

			return true;
		}


		@Override
		public EndNotPrecededBy copy() {
			val copy = new EndNotPrecededBy(super.name, super.charMatcher, super.origMatcher, super.firstMatchChars, this.minPreEndChars, super.includeMatchInRes, super.toStringSrc, super.notPreceding);
			BaseCharParserMatchable.copyTo(this, copy);
			return copy;
		}

	}

}
