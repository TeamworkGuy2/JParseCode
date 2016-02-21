package twg2.parser.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.val;
import twg2.collections.primitiveCollections.CharList;
import twg2.functions.BiPredicates;
import twg2.parser.condition.text.CharParser;
import twg2.parser.textParser.TextParser;

/** A collection of {@link CharParser}
 * Differs from {@link CharParserFactoryImpl} by allowing custom behavior instead of {@link twg2.parser.condition.text.CharParser.WithMarks#getMatchFirstChars(CharList)}
 * @author TeamworkGuy2
 * @since 2016-2-21
 */
public class CharParserPlainFactoryImpl<P extends CharParser> implements CharParserFactory {
	private String name;
	private List<P> conditions;
	private List<BiPredicates.CharObject<TextParser>> firstCharConds;
	private CharParser conditionSet;
	private @Getter boolean compound;


	@SafeVarargs
	public CharParserPlainFactoryImpl(String name, boolean compound, Entry<BiPredicates.CharObject<TextParser>, P>... parserConditions) {
		this.name = name;
		this.compound = compound;
		this.conditions = new ArrayList<>();
		this.firstCharConds = new ArrayList<>(parserConditions.length);

		// optimization for single condition sets
		if(parserConditions.length == 1) {
			this.conditions.add(parserConditions[0].getValue());
			this.conditionSet = parserConditions[0].getValue();
			this.firstCharConds.add(parserConditions[0].getKey());
		}
		else {
			@SuppressWarnings("unchecked")
			P[] conds = (P[])new Object[parserConditions.length];
			int i = 0;
			for(val entry : parserConditions) {
				conds[i] = entry.getValue();
				this.firstCharConds.add(entry.getKey());
				this.conditions.add(entry.getValue());
				i++;
			}
			this.conditionSet = new CharCompoundConditions.StartFilter(name, false, conds);
		}
	}


	public void add(BiPredicates.CharObject<TextParser> firstCharTest, P parserCondition) {
		this.conditions.add(parserCondition);
		this.firstCharConds.add(firstCharTest);
	}


	@Override
	public boolean isMatch(char ch, TextParser buf) {
		for(int i = 0, size = this.firstCharConds.size(); i < size; i++) {
			if(this.firstCharConds.get(i).test(ch, buf)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public CharParser createParser() {
		return conditionSet.copy();
	}


	@Override
	public String toString() {
		return (compound ? "compound " : "") + conditions;
	}

}
