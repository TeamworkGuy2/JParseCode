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
		return "TextToken: { type: " + fragmentType + ", " + textFragment.toString() + " }";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fragmentType == null ? 0 : fragmentType.hashCode());
		result = prime * result + (text == null ? 0 : text.hashCode());
		result = prime * result + (textFragment == null ? 0 : textFragment.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		var other = (TextFragmentRefToken<?>) obj;

		if (fragmentType == null) {
			if (other.fragmentType != null)
				return false;
		}
		else if (!fragmentType.equals(other.fragmentType)) {
			return false;
		}

		if (text == null) {
			if (other.text != null)
				return false;
		}
		else if (!text.equals(other.text)) {
			return false;
		}

		if (textFragment == null) {
			if (other.textFragment != null)
				return false;
		}
		else if (!textFragment.equals(other.textFragment)) {
			return false;
		}

		return true;
	}

}
