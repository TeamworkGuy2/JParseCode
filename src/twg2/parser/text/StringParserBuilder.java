package twg2.parser.text;

import java.util.ArrayList;
import java.util.List;

import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.Inclusion;

/**
 * @author TeamworkGuy2
 * @since 2015-6-25
 */
public class StringParserBuilder {
	private List<CharParserCondition.WithMarks> stringFilters;
	private String name;


	public StringParserBuilder(String name) {
		this.stringFilters = new ArrayList<>();
		this.name = name;
	}


	public CharPrecondition build() {
		CharParserCondition.WithMarks[] conditions = new CharParserCondition.WithMarks[stringFilters.size()];
		for(int i = 0, size = stringFilters.size(); i < size; i++) {
			conditions[i] = stringFilters.get(i);
		}
		return new CharPreconditionImpl<CharParserCondition.WithMarks>(name, false, conditions);
	}


	public StringParserBuilder addStringLiteralMarker(String name, String str) {
		this.stringFilters.add(new StringConditions.StringLiteralFilter(name, new String[] { str }, Inclusion.INCLUDE));
		return this;
	}


	public StringParserBuilder addCharLiteralMarker(String name, char start) {
		this.stringFilters.add(new CharConditions.CharLiteralFilter(name, CharArrayList.of(start), Inclusion.INCLUDE));
		return this;
	}


	public StringParserBuilder addCharMatcher(String name, char[] chars) {
		this.stringFilters.add(new CharConditions.ContainsCharFilter(name, CharArrayList.of(chars), Inclusion.INCLUDE));
		return this;
	}


	public StringParserBuilder addConditionMatcher(CharParserCondition.WithMarks condition) {
		this.stringFilters.add(condition);
		return this;
	}

}
