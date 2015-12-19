package twg2.parser.documentParser.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author TeamworkGuy2
 * @since 2014-12-7
 */
public class IntermediateBlock implements TextBlock {
	private IntermediateBlock parent;
	List<TextBlock> subBlocks;


	{
		this.subBlocks = new ArrayList<>();
	}


	public IntermediateBlock() {
	}


	public IntermediateBlock(IntermediateBlock parent) {
		this.parent = parent;
	}


	public IntermediateBlock(IntermediateBlock parent, String str) {
		this.parent = parent;
		if(str != null) {
			this.subBlocks.add(new StringBlock(parent, str));
		}
	}


	public IntermediateBlock(IntermediateBlock parent, TextBlock... subBlocks) {
		this.parent = parent;
		if(subBlocks != null) {
			for(TextBlock subBlock : subBlocks) {
				this.subBlocks.add(subBlock);
			}
		}
	}


	@Override
	public List<TextBlock> getSubBlocks() {
		return subBlocks;
	}


	public void addSubBlock(TextBlock block) {
		this.subBlocks.add(block);
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
	public String getText(String text) {
		throw new IllegalStateException("cannot get text from an intermediate text block");
	}


	@Override
	public boolean hasSubBlocks() {
		return subBlocks.size() > 0;
	}


	/** 
	 * @param action the function to call for each leaf block in this text block
	 */
	public void forEach(BiConsumer<TextBlock, Integer> action) {
		forEach(action, 0, false);
	}


	public void forEachLeaf(BiConsumer<TextBlock, Integer> action) {
		forEach(action, 0, true);
	}


	private void forEach(BiConsumer<TextBlock, Integer> action, int depth, boolean onlyLeaves) {
		for(TextBlock block : subBlocks) {
			if(block instanceof IntermediateBlock) {
				if(!onlyLeaves) {
					action.accept(block, depth);
				}
				((IntermediateBlock)block).forEach(action, depth + 1, onlyLeaves);
			}
			else {
				action.accept(block, depth);
			}
		}
	}


	private String toString(String text, int depth, StringBuilder indentation, StringBuilder strB) {
		for(TextBlock block : subBlocks) {
			if(block instanceof IntermediateBlock) {
				strB.append(indentation.toString());
				strB.append("\nblock [\n");
				indentation.append('\t');
				((IntermediateBlock)block).toString(text, depth, indentation, strB);
				indentation.deleteCharAt(indentation.length() - 1);
				strB.append(indentation.toString());
				strB.append("\n]\n");
			}
			else {
				strB.append(indentation.toString());
				strB.append(block.toString(text).trim());
			}
		}
		return strB.toString();
	}


	@Override
	public String toString(String text) {
		StringBuilder strB = new StringBuilder(256);
		StringBuilder indentation = new StringBuilder();
		toString(text, 0, indentation, strB);
		return strB.toString();
	}


	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder(256);
		StringBuilder indentation = new StringBuilder();
		toString(null, 0, indentation, strB);
		return strB.toString();
	}

}
