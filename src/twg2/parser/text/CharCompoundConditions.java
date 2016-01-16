package twg2.parser.text;

import java.util.Collection;

import lombok.val;
import twg2.collections.util.dataStructures.Bag;
import twg2.parser.condition.text.CharParser;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-3-07
 */
public class CharCompoundConditions {


	/** A collection of {@link CharParser ParserConditions}
	 * @author TeamworkGuy2
	 * @since 2015-2-21
	 */
	public static abstract class BaseFilter implements CharParser {
		CharParser[] originalConds;
		Bag<CharParser> matchingConds;
		boolean anyComplete = false;
		boolean failed = false;
		int acceptedCount;
		StringBuilder dstBuf = new StringBuilder();
		TextFragmentRef.ImplMut coords = new TextFragmentRef.ImplMut();
		Runnable resetFunc;
		String name;


		public BaseFilter(String name, boolean doCopyConds, Collection<CharParser> conds) {
			this(name, doCopyConds, conds.toArray(new CharParser[conds.size()]));
		}


		@SafeVarargs
		public BaseFilter(String name, boolean doCopyConds, CharParser... conds) {
			this.originalConds = conds;

			CharParser[] copyConds = conds;
			if(doCopyConds) {
				copyConds = new CharParser[conds.length];
				for(int i = 0, size = conds.length; i < size; i++) {
					copyConds[i] = conds[i].copy();
				}
			}

			this.matchingConds = new Bag<>(copyConds, 0, copyConds.length);
			this.anyComplete = false;
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
	public static class Filter extends StartFilter {

		public Filter(String name, boolean doCopyConds, CharParser[] conds) {
			super(name, doCopyConds, conds);
		}


		@Override
		public Filter copy() {
			val copy = new Filter(name, true, originalConds);
			return copy;
		}

	}


	/** Accept input that matches this parse condition
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static class StartFilter extends BaseFilter {

		public StartFilter(String name, boolean doCopyConds, CharParser[] conds) {
			super(name, doCopyConds, conds);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			int off = super.dstBuf.length();
			if(super.acceptedCount > off) {
				super.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<CharParser> matchingConds = super.matchingConds;
			CharParser condI = null;
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
							super.anyComplete = true;
						}
					}
				}
				else {
					matchingConds.remove(i);
				}
			}

			if(anyFound) {
				if(super.acceptedCount == 0) {
					super.coords.setStart(buf);
				}
				super.acceptedCount++;
				super.dstBuf.append(ch);
				if(super.anyComplete) {
					super.coords.setEnd(buf);
				}

				// TODO debugging - ensure that this compound condition ends up with the same parsed text fragment as the sub-conditions
				//if(cond.anyComplete && !condI.getCompleteMatchedTextCoords().equals(cond.coords)) {
				//	throw new RuntimeException("CharCompoundConditions " + condI.getCompleteMatchedTextCoords().toString() + " not equal to sub " + cond + " " + cond.coords.toString());
				//}

				return true;
			}
			else {
				super.failed = true;
				super.anyComplete = false;
				return false;
			}
		}


		@Override
		public StartFilter copy() {
			val copy = new StartFilter(super.name, true, super.originalConds);
			return copy;
		}

	}




	/** Accept input until a full match for this parse condition is encountered
	 * @author TeamworkGuy2
	 * @since 2015-2-10
	 */
	public static class EndFilter extends BaseFilter {

		public EndFilter(String name, boolean doCopyConds, CharParser[] conds) {
			super(name, doCopyConds, conds);
		}


		@Override
		public boolean acceptNext(char ch, TextParser buf) {
			if(super.isComplete()) {
				super.failed = true;
				return false;
			}
			boolean anyFound = false;
			Bag<CharParser> matchingConds = super.matchingConds;
			// reverse iterate through the bag so we don't have to adjust the loop variable when we remove elements
			for(int i = matchingConds.size() - 1; i > -1; i--) {
				CharParser condI = matchingConds.get(i);
				if(!condI.isFailed()) {
					if(!condI.acceptNext(ch, buf)) {
						matchingConds.remove(i);
					}
					else {
						anyFound = true;
						if(condI.isComplete()) {
							super.anyComplete = true;
						}
					}
				}
				else {
					matchingConds.remove(i);
				}
			}

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
		public EndFilter copy() {
			val copy = new EndFilter(this.name, true, this.originalConds);
			return copy;
		}

	}

}
