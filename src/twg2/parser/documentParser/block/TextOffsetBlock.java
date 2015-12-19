package twg2.parser.documentParser.block;

import java.util.List;

/**
 * @author TeamworkGuy2
 * @since 2014-12-13
 */
public class TextOffsetBlock implements TextBlock {
	private IntermediateBlock parent;
	private int off;
	private int len;


	public TextOffsetBlock(IntermediateBlock parent, int offset, int length) {
		this.parent = parent;
		this.off = offset;
		this.len = length;
	}


	@Override
	public boolean hasParentBlock() {
		return parent != null;
	}


	@Override
	public IntermediateBlock getParentBlock() {
		if(parent == null) {
			throw new IllegalStateException("this text block does not have a parent block");
		}
		return parent;
	}


	@Override
	public List<TextBlock> getSubBlocks() {
		throw new IllegalStateException("cannot get sub twg2.parser.documentParser.block from a text block");
	}


	@Override
	public String getText(String text) {
		return text.substring(off, off + len);
	}


	@Override
	public boolean hasSubBlocks() {
		return false;
	}


	public int getOffset() {
		return off;
	}


	public int getLength() {
		return len;
	}


	@Override
	public String toString(String text) {
		return text != null ? text.substring(off, off + len) : toString();
	}


	@Override
	public String toString() {
		return "[" + off + " to " + (off + len) + "]";
	}

}
