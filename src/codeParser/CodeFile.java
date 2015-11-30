package codeParser;

import java.util.List;

import lombok.Getter;
import twg2.treeLike.simpleTree.SimpleTree;
import documentParser.DocumentFragment;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class CodeFile {
	@Getter SimpleTree<DocumentFragment<CodeFragmentType>> doc;
	@Getter String src;
	@Getter List<String> lines;

	public CodeFile(SimpleTree<DocumentFragment<CodeFragmentType>> doc, String src, List<String> lines) {
		this.doc = doc;
		this.src = src;
		this.lines = lines;
	}

}
