package codeParser.csharp;

import intermAst.block.IntermBlock;
import intermAst.field.IntermFieldSig;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.CompoundBlock;
import baseAst.util.AstUtil;
import baseAst.util.NameUtil;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class CSharpDataModelFieldExtractor {
	IntermBlock<? extends CompoundBlock> parentBlock;
	List<IntermFieldSig> fields = new ArrayList<>();


	public CSharpDataModelFieldExtractor(IntermBlock<? extends CompoundBlock> parentBlock) {
		this.parentBlock = parentBlock;
	}


	public void extractDataModelFieldsIndexedTreeConsumer(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		val prevNode = AstUtil.getSibling(siblings, idx, -1);
		val nextNode = AstUtil.getSibling(siblings, idx, 1);
		if(prevNode != null && token.getFragmentType() == CodeFragmentType.IDENTIFIER && AstUtil.isType(prevNode.getData(), CodeFragmentType.IDENTIFIER)) {
			if((nextNode == null || nextNode.getData().getFragmentType() != CodeFragmentType.BLOCK ||
					AstUtil.blockContainsOnly(nextNode, (node, type) -> type == CodeFragmentType.IDENTIFIER && ("get".equals(node.getText()) || "set".equals(node.getText())), true))) {
				String fieldType = prevNode.getData().getText();
				String fieldName = token.getText();

				fields.add(new IntermFieldSig(fieldName, NameUtil.newFqName(parentBlock.getDeclaration().getFullyQualifyingName(), fieldName), fieldType));
			}
		}
	}


	public static List<IntermFieldSig> extractDataModelFields(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree, IntermBlock<? extends CompoundBlock> parentBlock) {
		CSharpDataModelFieldExtractor extractor = new CSharpDataModelFieldExtractor(parentBlock);
		AstUtil.forChildrenOnly(0, tokenTree, extractor::extractDataModelFieldsIndexedTreeConsumer);
		return extractor.fields;
	}

}
