package twg2.parser.test;

import java.util.List;
import java.util.function.Function;

import lombok.val;
import twg2.parser.fragment.CodeTokenType;
import twg2.parser.fragment.TextFragmentRefToken;
import twg2.text.stringSearch.StringIndex;
import twg2.text.stringUtils.StringJoin;


/**
 * @author TeamworkGuy2
 * @since 2016-2-28
 */
public class TemplateVar {
	public final String name;
	public final Function<String, List<String>> linesGetter;
	public final String startMark;
	public final String endMark;
	public final String startName;


	/**
	 * @param name
	 * @param linesGetter
	 */
	public TemplateVar(String startMark, String endMark, String name, Function<String, List<String>> linesGetter) {
		this.name = name;
		this.linesGetter = linesGetter;
		this.startMark = startMark;
		this.endMark = endMark;
		this.startName = startMark + name;
	}


	public boolean isMatch(String text) {
		return text.startsWith(startName);
	}


	public int insert(TextFragmentRefToken<? extends CodeTokenType> frag, boolean preserveIndentation, StringBuilder srcDst, int off, int len) {
		val name = inbetweenString(frag.getText(), startName + "(name=\"", "\")" + endMark);
		String replacementStr = null;
		val lines = linesGetter.apply(name);

		if(preserveIndentation) {
			int ldx = StringIndex.lastIndexOf(srcDst, 0, off, '\n');
			String indentation = ldx > -1 ? srcDst.substring(ldx + 1, off) : "";
			indentation = "\n" + indentation;
			replacementStr = StringJoin.join(lines, indentation);
		}
		else {
			replacementStr = StringJoin.join(lines, "\n");
		}

		srcDst.replace(off, off + len, replacementStr);
		return off + replacementStr.length();
	}


	public static String inbetweenString(String src, String start, String end) {
		int off = 0;
		int idx1 = src.indexOf(start, off);
		int idx2 = src.indexOf(end, idx1 + start.length());
		return idx1 > -1 && idx2 > -1 ? src.substring(idx1 + start.length(), idx2) : null;
	}

}
