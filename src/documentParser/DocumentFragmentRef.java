package documentParser;

import parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public class DocumentFragmentRef<T> implements DocumentFragment<TextFragmentRef, T> {
	private final TextFragmentRef textFragment;
	private final T type;


	public DocumentFragmentRef(T type, TextFragmentRef text) {
		this.type = type;
		this.textFragment = text;
	}


	@Override
	public TextFragmentRef getTextFragment() {
		return textFragment;
	}


	@Override
	public T getFragmentType() {
		return type;
	}


	@Override
	public String toString() {
		return "DocumentFragmentRef: { type: " + type + ", " + textFragment.toString() + " }";
	}

}
