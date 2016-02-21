package twg2.parser.documentParser;

import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class DocumentFragmentText<T> extends DocumentFragmentRef<T> implements DocumentFragment<TextFragmentRef, T> {
	private final String text;


	public DocumentFragmentText(T type, TextFragmentRef textFrag, String text) {
		super(type, textFrag);
		this.text = text;
	}


	public String getText() {
		return text;
	}


	@Override
	public String toString() {
		return "DocumentFragmentText: { type: " + fragmentType + ", " + textFragment.toString() + " }";
	}

}
