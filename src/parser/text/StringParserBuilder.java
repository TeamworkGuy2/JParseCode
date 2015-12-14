package parser.text;

import java.util.ArrayList;
import java.util.List;

/**
 * @author TeamworkGuy2
 * @since 2015-6-25
 */
public class StringParserBuilder {
	private List<CharParserCondition.WithMarks> stringFilters;


	public StringParserBuilder() {
		this.stringFilters = new ArrayList<>();
	}


	public CharPrecondition build() {
		CharParserCondition.WithMarks[] conditions = new CharParserCondition.WithMarks[stringFilters.size()];
		for(int i = 0, size = stringFilters.size(); i < size; i++) {
			conditions[i] = stringFilters.get(i);
		}
		return new CharPreconditionImpl<CharParserCondition.WithMarks>(false, conditions);
	}


	public StringParserBuilder addStringLiteralMarker(String str) {
		this.stringFilters.add(StringConditions.stringLiteralFactory().create(str));
		return this;
	}


	public StringParserBuilder addCharLiteralMarker(char start) {
		this.stringFilters.add(CharConditions.charLiteralFactory().create(start));
		return this;
	}


	public StringParserBuilder addCharMatcher(char[] chars) {
		this.stringFilters.add(CharConditions.containsCharFactory().create(chars));
		return this;
	}


	public StringParserBuilder addConditionMatcher(CharParserCondition.WithMarks condition) {
		this.stringFilters.add(condition);
		return this;
	}

}
