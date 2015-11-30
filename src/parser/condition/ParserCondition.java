package parser.condition;

import java.util.Collection;

import codeParser.ParserBuilder;
import parser.textFragment.TextFragmentRef;
import twg2.parser.textParser.TextParser;

/** A precondition, commonly used by {@link ParserBuilder} and {@link MultiConditionParser}, to determine whether the beginning
 * of an input token matches a set of requirements.<br>
 * A {@code ParserCondition} is stateful and must keep track of previous characters passed to {@link #acceptNext(char, TextParser)}
 * and return false once the set of characters forms an invalid sequence.
 * @author TeamworkGuy2
 * @since 2015-2-10
 * @see Precondition
 */
public interface ParserCondition {

	/**
	 * @param ch the character to accept/add to this filter
	 * @return true if the char was accepted, false if not.
	 * If false is returned, this {@code ParserCondition} enters a failed state and
	 * will not return true for any further inputs
	 */
	public boolean acceptNext(char ch, TextParser pos);


	/**
	 * @return true if this precondition filter has been successfully completed/matched, false if not
	 * @see #isFailed()
	 */
	public boolean isComplete();


	public TextFragmentRef getCompleteMatchedTextCoords();


	public StringBuilder getParserDestination();


	public void setParserDestination(StringBuilder parserDestination);


	/**
	 * @return true if this precondition cannot create accept any further input to {@link #acceptNext(char, TextParser)}.
	 * That is, {@link #acceptNext(char, TextParser)} will return false for any input
	 */
	public boolean isFailed();


	public ParserCondition copy();


	public boolean canRecycle();


	public default ParserCondition recycle() {
		throw new UnsupportedOperationException("ParserCondition recycling not supported");
	}


	public default boolean readConditional(TextParser buf) {
		return readConditional(buf, null);
	}


	public default boolean readConditional(TextParser buf, StringBuilder dst) {
		int off = dst != null ? dst.length() : 0;
		int count = 0;
		while(!this.isComplete()) {
			if(!buf.hasNext()) {
				return false;
			}
			count++;
			char ch = buf.nextChar();
			if(!this.acceptNext(ch, buf)) {
				buf.unread(count);
				if(dst != null) {
					dst.setLength(off);
				}
				return false;
			}
			if(dst != null) {
				dst.append(ch);
			}
		}
		return true;
	}


	public default ParserCondition copyOrReuse() {
		ParserCondition filter = null;
		if(this.canRecycle()) {
			filter = this.recycle();
		}
		else {
			filter = this.copy();
		}
		return filter;
	}


	public static boolean canRecycleAll(Collection<? extends ParserCondition> coll) {
		boolean canReuse = true;
		for(ParserCondition cond : coll) {
			canReuse &= cond.canRecycle();
		}
		return canReuse;
	}


	public static boolean canRecycleAll(ParserCondition[] coll) {
		boolean canReuse = true;
		for(ParserCondition cond : coll) {
			canReuse &= cond.canRecycle();
		}
		return canReuse;
	}


	/**
	 * @author TeamworkGuy2
	 * @since 2015-2-14
	 */
	public static interface WithMarks extends ParserCondition, ParserStartMark {
	}


}
