package documentParser;

import parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2015-5-28
 */
public class DocumentFragment<T> {
	private TextFragmentRef textFragment;
	private T type;


	public DocumentFragment() {
	}


	public DocumentFragment(T type, TextFragmentRef text) {
		setFragmentType(type);
		setTextFragment(text);
	}


	public TextFragmentRef getTextFragment() {
		return textFragment;
	}


	public void setTextFragment(TextFragmentRef text) {
		this.textFragment = text;
	}


	public T getFragmentType() {
		return type;
	}


	public void setFragmentType(T type) {
		this.type = type;
	}


	@Override
	public String toString() {
		return "DocumentFragment: { type: " + type + ", " + textFragment.toString() + " }";
	}

}
