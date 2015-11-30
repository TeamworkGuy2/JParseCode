package documentParser.block;

import java.util.List;

/**
 * @author TeamworkGuy2
 * @since 2014-12-7
 */
public class StringBlock implements TextBlock {
	private IntermediateBlock parent;
	private String text;


	public StringBlock(IntermediateBlock parent, String str) {
		this.parent = parent;
		this.text = str;
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
		throw new IllegalStateException("cannot get sub documentParser.block from a text block");
	}


	@Override
	public String getText(String str) {
		return text;
	}


	@Override
	public boolean hasSubBlocks() {
		return false;
	}


	@Override
	public String toString(String text) {
		return text;
	}


	@Override
	public String toString() {
		return text;
	}

}
