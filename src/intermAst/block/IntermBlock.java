package intermAst.block;

import intermAst.classes.IntermClassSig;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.CompoundBlock;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
@Immutable
public class IntermBlock<T_BLOCK extends CompoundBlock> {
	private final @Getter IntermClassSig declaration;
	private final @Getter SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree;
	private final @Getter T_BLOCK blockType;


	public IntermBlock(IntermClassSig declaration, SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree, T_BLOCK blockType) {
		this.declaration = declaration;
		this.blockTree = blockTree;
		this.blockType = blockType;
	}


	@Override
	public String toString() {
		return "unparsed block: " + declaration.toString() + ": " + blockTree;
	}

}
