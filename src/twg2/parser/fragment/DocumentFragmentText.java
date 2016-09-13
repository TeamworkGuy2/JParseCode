package twg2.parser.fragment;

import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class DocumentFragmentText<T> implements DocumentFragment<TextFragmentRef, T> {
	// package-private
	final TextFragmentRef textFragment;
	final T fragmentType;
	final String text;


	public DocumentFragmentText(T type, TextFragmentRef textFrag, String text) {
		this.fragmentType = type;
		this.textFragment = textFrag;
		this.text = text;
	}


	public String getText() {
		return text;
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
		return "DocumentFragmentText: { type: " + fragmentType + ", " + textFragment.toString() + " }";
	}

}
