package twg2.ast.interm.block;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.classes.ClassSig;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.documentParser.CodeFragment;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
@Immutable
public class BlockAst<T_BLOCK extends CompoundBlock> {
	private final @Getter ClassSig.SimpleImpl declaration;
	private final @Getter SimpleTree<CodeFragment> blockTree;
	private final @Getter T_BLOCK blockType;


	public BlockAst(ClassSig.SimpleImpl declaration, SimpleTree<CodeFragment> blockTree, T_BLOCK blockType) {
		this.declaration = declaration;
		this.blockTree = blockTree;
		this.blockType = blockType;
	}


	@Override
	public String toString() {
		return "unparsed block: " + declaration.toString() + ": " + blockTree;
	}

}
