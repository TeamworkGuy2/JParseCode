package parser.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.collections.primitiveCollections.CharList;
import twg2.collections.primitiveCollections.CharMapSorted;

/** A collection of {@link CharParserCondition}
 * @author TeamworkGuy2
 * @since 2015-2-14
 */
public class CharPreconditionImpl<P extends CharParserCondition> implements CharPrecondition {
	private List<P> conditions;
	private CharMapSorted<P> firstChars;
	private CharList tmpChars = new CharArrayList();
	private CharParserCondition conditionSet;
	private @Getter boolean compound;


	@SafeVarargs
	public CharPreconditionImpl(boolean compound, P... parserConditions) {
		this.compound = compound;
		this.conditions = new ArrayList<>();
		Collections.addAll(this.conditions, parserConditions);
		this.firstChars = new CharMapSorted<>(parserConditions.length);

		// optimization for single condition sets
		if(parserConditions.length == 1) {
			this.conditionSet = parserConditions[0];
			addFirstChars(parserConditions[0], tmpChars, firstChars);
		}
		else {
			this.conditionSet = CharCompoundConditions.startFilterFactory().create(parserConditions);

			for(int i = 0, size = parserConditions.length; i < size; i++) {
				addFirstChars(parserConditions[i], tmpChars, firstChars);
			}
		}
	}


	public void add(P parserCondition) {
		this.conditions.add(parserCondition);
		addFirstChars(parserCondition, tmpChars, firstChars);
	}


	@Override
	public boolean isMatch(char ch) {
		return firstChars.contains(ch);
	}


	@Override
	public CharParserCondition createParser() {
		return conditionSet.copy();
	}


	private static final <P extends CharParserCondition> void addFirstChars(P cond, CharList tmpChars, CharMapSorted<P> dstChars) {
		if(cond instanceof ParserStartChars) {
			((ParserStartChars)cond).getMatchFirstChars(tmpChars);
		}
		for(int ii = 0, sizeI = tmpChars.size(); ii < sizeI; ii++) {
			dstChars.put(tmpChars.get(ii), cond);
		}
		tmpChars.clear();
	}


	@Override
	public String toString() {
		return (compound ? "compound " : "") + conditions;
	}

}
