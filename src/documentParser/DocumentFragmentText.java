package documentParser;

import lombok.Getter;
import parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class DocumentFragmentText<T> implements DocumentFragment<TextFragmentRef, T> {
	private @Getter final TextFragmentRef textFragment;
	private @Getter final T fragmentType;
	private @Getter final String text;


	public DocumentFragmentText(T type, TextFragmentRef textFrag, String text) {
		this.fragmentType = type;
		this.textFragment = textFrag;
		this.text = text;
	}


	@Override
	public String toString() {
		return "DocumentFragmentRef: { type: " + fragmentType + ", " + textFragment.toString() + " }";
	}

}
