package twg2.parser.text;

import java.util.Arrays;

import lombok.val;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.primitiveCollections.CharListReadOnly;
import twg2.functions.Predicates;
import twg2.functions.Predicates.Char;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;
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
	 * @since 2015-2-21
	 */
	public static abstract class BaseCharParser implements CharParser.WithMarks {
		char[] originalChars;
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
		Predicates.Char firstCharMatcher;
		Object toStringSrc;
		String name;


		public BaseCharParser(String name, CharList chars, Inclusion includeCondMatchInRes) {
			this(name, chars::contains, null, chars.toArray(), includeCondMatchInRes, null);
		}


		public BaseCharParser(String name, Predicates.Char charMatcher, Predicates.Char firstCharMatcher, char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			this.originalChars = matchChars;
			this.charMatcher = charMatcher;
			this.firstCharMatcher = firstCharMatcher;
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
		public void getMatchFirstChars(CharList dst) {
			dst.addAll(originalChars);
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
			return "one " + (toStringSrc != null ? toStringSrc.toString() : Arrays.toString(this.originalChars));
		}


		public static BaseCharParser copyTo(BaseCharParser src, BaseCharParser dst) {
			dst.originalChars = src.originalChars;
			dst.includeMatchInRes = src.includeMatchInRes;
			dst.charMatcher = src.charMatcher;
			dst.firstCharMatcher = src.firstCharMatcher;
			dst.toStringSrc = src.toStringSrc;
			dst.name = src.name;
			return dst;
		}

	}




	/**
	 */
	public static class Literal extends Start {

		public Literal(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public Literal(String name, Char charMatcher, Char firstCharMatcher,
				char[] matchChars, Inclusion includeCondMatchInRes, Object toStringSrc) {
			super(name, charMatcher, firstCharMatcher, matchChars, includeCondMatchInRes, toStringSrc);
		}


		@Override
		public Literal copy() {
			val copy = new Literal(name, charMatcher, firstCharMatcher, originalChars, includeMatchInRes, toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static class Start extends BaseCharParser {

		public Start(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public Start(String name, Char charMatcher, Char firstCharMatcher,
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

			if(super.anyComplete) {
				if(super.acceptedCount == 0) {
					super.coords.setStart(buf);
				}
				super.dstBuf.append(ch);
				super.acceptedChar = ch;
				super.acceptedCount++;
				super.matchCount++;
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
			val copy = new Start(name, charMatcher, firstCharMatcher, originalChars, includeMatchInRes, toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-11-27
	 */
	public static class Contains extends BaseCharParser {

		public Contains(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public Contains(String name, Char charMatcher, Char firstCharMatcher,
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

			if(super.charMatcher.test(ch)) {
				if(super.matchCount == 0) {
					super.coords.setStart(buf);
				}
				super.dstBuf.append(ch);
				super.acceptedChar = ch;
				super.acceptedCount++;
				super.matchCount++;
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
		public Contains copy() {
			val copy = new Contains(name, charMatcher, firstCharMatcher, originalChars, includeMatchInRes, toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-13
	 */
	public static class ContainsFirstSpecial extends BaseCharParser {

		public ContainsFirstSpecial(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public ContainsFirstSpecial(String name, Char charMatcher, Char firstCharMatcher,
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

			if(super.matchCount == 0 ? super.firstCharMatcher.test(ch) : super.charMatcher.test(ch)) {
				if(super.matchCount == 0) {
					super.coords.setStart(buf);
				}
				super.dstBuf.append(ch);
				super.acceptedChar = ch;
				super.acceptedCount++;
				super.matchCount++;
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
			val copy = new ContainsFirstSpecial(name, charMatcher, firstCharMatcher, originalChars, includeMatchInRes, toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static class End extends BaseCharParser {

		public End(String name, CharList chars, Inclusion includeCondMatchInRes) {
			super(name, chars, includeCondMatchInRes);
		}


		public End(String name, Char charMatcher, Char firstCharMatcher,
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

			if(super.matchCount == 0) {
				super.coords.setStart(buf);
			}

			if(super.anyComplete) {
				super.acceptedCount++;
				super.acceptedChar = ch;
				super.coords.setEnd(buf);
			}

			super.dstBuf.append(ch);
			super.matchCount++;

			return true;
		}


		@Override
		public End copy() {
			val copy = new End(name, charMatcher, firstCharMatcher, originalChars, includeMatchInRes, toStringSrc);
			return copy;
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static class EndNotPrecededBy extends BaseCharParser {
		private final int minPreEndChars;


		public EndNotPrecededBy(String name, CharList chars, int minPreEndChars, Inclusion includeCondMatchInRes, CharListReadOnly notPrecededBy) {
			super(name, chars::contains, null, chars.toArray(), includeCondMatchInRes, null);
			super.notPreceding = notPrecededBy;
			this.minPreEndChars = minPreEndChars;
		}


		public EndNotPrecededBy(String name, Char charMatcher, Char firstCharMatcher,
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

			if(super.matchCount == 0) {
				super.coords.setStart(buf);
			}

			if(super.anyComplete) {
				super.acceptedChar = ch;
				super.acceptedCount++;
				super.coords.setEnd(buf);
			}

			super.dstBuf.append(ch);
			super.matchCount++;

			return true;
		}


		@Override
		public EndNotPrecededBy copy() {
			val copy = new EndNotPrecededBy(name, charMatcher, firstCharMatcher, super.originalChars, this.minPreEndChars, super.includeMatchInRes, super.toStringSrc, super.notPreceding);
			BaseCharParser.copyTo(this, copy);
			return copy;
		}

	}

}
