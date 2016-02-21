package twg2.parser.documentParser;

import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public class DocumentFragmentRef<T> implements DocumentFragment<TextFragmentRef, T> {
	// package-private
	final TextFragmentRef textFragment;
	final T fragmentType;


	public DocumentFragmentRef(T type, TextFragmentRef text) {
		this.fragmentType = type;
		this.textFragment = text;
	}


	@Override
	public TextFragmentRef getTextFragment() {
		return textFragment;
	}


	@Override
	public T getFragmentType() {
		return fragmentType;
	}


	@Override
	public String toString() {
		return "DocumentFragmentRef: { type: " + fragmentType + ", " + textFragment.toString() + " }";
	}

}
