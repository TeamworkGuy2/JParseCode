package twg2.parser.fragment;

import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class TextFragmentRefToken<T> implements TextToken<TextFragmentRef, T> {
	// package-private
	final TextFragmentRef textFragment;
	final T fragmentType;
	final String text;


	public TextFragmentRefToken(T type, TextFragmentRef textFrag, String text) {
		this.fragmentType = type;
		this.textFragment = textFrag;
		this.text = text;
	}


	public String getText() {
		return text;
	}


	@Override
	public TextFragmentRef getToken() {
		return textFragment;
	}


	@Override
	public T getTokenType() {
		return fragmentType;
	}


	@Override
	public String toString() {
		return "DocumentFragmentText: { type: " + fragmentType + ", " + textFragment.toString() + " }";
	}

}
