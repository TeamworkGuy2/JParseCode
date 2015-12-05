package codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.AstUtil;
import baseAst.field.FieldSig;
import codeParser.CodeFile;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public class CSharpDataModelExtractor {
	List<FieldSig> fields = new ArrayList<>();


	public void extractDataModelFieldsIndexedSubTreeConsumer(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		val prevNode = AstUtil.getSibling(siblings, idx, -1);
		val nextNode = AstUtil.getSibling(siblings, idx, 1);
		if(parent != null && prevNode != null && nextNode != null && parent.getFragmentType() == CodeFragmentType.BLOCK && parent.getText().startsWith("{")) {
			if(token.getFragmentType() == CodeFragmentType.IDENTIFIER && AstUtil.isType(prevNode.getData(), CodeFragmentType.IDENTIFIER, CodeFragmentType.KEYWORD) &&
					(nextNode.getData().getFragmentType() != CodeFragmentType.BLOCK || AstUtil.blockContainsOnly(nextNode, (node, type) -> type == CodeFragmentType.IDENTIFIER && ("get".equals(node.getText()) || "set".equals(node.getText())), true))) {
				fields.add(new FieldSig(prevNode.getData().getText(), token.getText()));
			}
		}
	}


	public static List<FieldSig> extractDataModelFieldsMethods(CodeFile<DocumentFragmentText<CodeFragmentType>> parsedFile) {
		CSharpDataModelExtractor extractor = new CSharpDataModelExtractor();
		AstUtil.forEach(parsedFile.getDoc(), extractor::extractDataModelFieldsIndexedSubTreeConsumer);
		return extractor.fields;
	}

}
