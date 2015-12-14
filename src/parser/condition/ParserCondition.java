package parser.condition;

import java.util.Collection;

import parser.text.CharMultiConditionParser;
import codeParser.ParserBuilder;

/** A token parser, commonly used by {@link ParserBuilder} and {@link CharMultiConditionParser}, to determine whether
 * a series of input token match a requirement.<br>
 * An instance of this interface must keep track of previous tokens passed to an 'accept' method implemented by sub-classes
 * and return false once the set of tokens forms an invalid sequence.
 * @author TeamworkGuy2
 * @since 2015-12-12
 * @see Precondition
 */
public interface ParserCondition {

	/**
	 * @return true if this precondition filter has been successfully completed/matched, false if not
	 * @see #isFailed()
	 */
	public boolean isComplete();


	/**
	 * @return true if this precondition cannot create accept any further input to 'accept'
	 * That is, 'accept' will return false for any input
	 */
	public boolean isFailed();


	public ParserCondition copy();


	public boolean canRecycle();


	public default ParserCondition recycle() {
		throw new UnsupportedOperationException("ParserCondition recycling not supported");
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

}
