package twg2.parser.miscellaneous;

import java.util.Enumeration;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.tree.TreeNode;

import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since-12-14 
 */
public class TextBlockElement extends AbstractDocument.AbstractElement {
	private static final long serialVersionUID = -4426562862225510702L;

	private int off;
	private int len;


	public TextBlockElement(AbstractDocument abstractDocument, Element parent, TextFragmentRef textBlock) {
		abstractDocument.super(parent, null);
		this.off = textBlock.getOffsetStart();
		this.len = textBlock.getOffsetEnd() - textBlock.getOffsetStart();
	}


	@Override
	public int getStartOffset() {
		return off;
	}


	@Override
	public int getEndOffset() {
		return off + len;
	}


	@Override
	public Element getElement(int index) {
		return null;
	}


	@Override
	public int getElementCount() {
		return 0;
	}


	@Override
	public int getElementIndex(int offset) {
		return 0;
	}


	@Override
	public boolean isLeaf() {
		return true;
	}


	@Override
	public boolean getAllowsChildren() {
		return false;
	}


	@Override
	public Enumeration<TreeNode> children() {
		return null;
	}

}
