package twg2.parser.documentParser;

import twg2.parser.fragment.CodeFragmentType;
import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2016-4-12
 */
public class CodeFragment extends DocumentFragmentText<CodeFragmentType> {

	public CodeFragment(CodeFragmentType type, TextFragmentRef textFrag, String text) {
		super(type, textFrag, text);
	}

}
