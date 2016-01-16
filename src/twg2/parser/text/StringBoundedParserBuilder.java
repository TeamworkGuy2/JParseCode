package twg2.parser.text;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParser;

/**
 * @author TeamworkGuy2
 * @since 2015-2-13
 */
public class StringBoundedParserBuilder {
	private List<CharParser> filters;
	private boolean compound;
	private String name;


	public StringBoundedParserBuilder(String name) {
		this.filters = new ArrayList<>();
		this.name = name;
	}


	public CharParserFactory build() {
		return new CharParserFactoryImpl<CharParser>(name, compound, filters.toArray(new CharParser[filters.size()]));
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, String start, String end, Inclusion includeEnd) {
		val startFilter = new StringConditions.Start(name + "-start", new String[] { start }, Inclusion.INCLUDE);
		val endFilter = new StringConditions.End(name + "-end", new String[] { end }, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(name, startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, String start, char end, Inclusion includeEnd) {
		val startFilter = new StringConditions.Start(name + "-start", new String[] { start }, Inclusion.INCLUDE);
		val endFilter = new CharConditions.End(name + "-end", CharArrayList.of(end), includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(name, startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, char start, String end, Inclusion includeEnd) {
		val startFilter = new CharConditions.Start(name + "-start", CharArrayList.of(start), Inclusion.INCLUDE);
		val endFilter = new StringConditions.End(name + "-end", new String[] { end }, includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(name, startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndMarkers(String name, char start, char end, Inclusion includeEnd) {
		val startFilter = new CharConditions.Start(name + "-start", CharArrayList.of(start), Inclusion.INCLUDE);
		val endFilter = new CharConditions.End(name + "-end", CharArrayList.of(end), includeEnd);
		this.filters.add(new CharConditionPipe.AllRequired<>(name, startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStartEndNotPrecededByMarkers(String name, char start, char notPreced, char end, Inclusion includeEnd) {
		val startFilter = new CharConditions.Start(name + "-start", CharArrayList.of(start), Inclusion.INCLUDE);
		val endFilter = new CharConditions.EndNotPrecededBy(name + "-end", CharArrayList.of(end), includeEnd, CharArrayList.of(notPreced));
		this.filters.add(new CharConditionPipe.AllRequired<>(name, startFilter, endFilter));
		return this;
	}


	public StringBoundedParserBuilder addStringLiteralMarker(String name, String str) {
		this.filters.add(new StringConditions.Literal(name, new String[] { str }, Inclusion.INCLUDE));
		return this;
	}


	public StringBoundedParserBuilder addCharLiteralMarker(String name, char ch) {
		this.filters.add(new CharConditions.Literal(name, CharArrayList.of(ch), Inclusion.INCLUDE));
		return this;
	}


	public StringBoundedParserBuilder addConditionMarker(CharParser condition) {
		this.filters.add(condition);
		return this;
	}


	public StringBoundedParserBuilder isCompound(boolean compound) {
		this.compound = compound;
		return this;
	}

}
