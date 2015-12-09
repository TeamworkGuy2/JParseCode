package codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.util.AstUtil;
import codeParser.CodeFragmentType;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class CSharpUsingStatementExtractor {
	List<List<String>> usingStatements = new ArrayList<>();
	SimpleTree<DocumentFragmentText<CodeFragmentType>> tree;


	public CSharpUsingStatementExtractor(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree) {
		this.usingStatements = new ArrayList<>();
		this.tree = tree;
	}


	public void extractUsingStatementsSubTreeConsumer(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		if(siblingCount > 1 && AstUtil.isKeyword(token, CSharpKeyword.USING)) {
			List<String> usingNameSpace = new ArrayList<>();
			for(int i = idx + 1; i < siblingCount; i++) {
				val node = siblings.get(i);
				if(!AstUtil.isType(node.getData(), CodeFragmentType.IDENTIFIER)) {
					break;
				}
				usingNameSpace.add(node.getData().getText());
			}
			if(usingNameSpace.size() > 0) {
				usingStatements.add(usingNameSpace);
			}
		}
	}


	public static List<List<String>> extractUsingStatements(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree) {
		CSharpUsingStatementExtractor extractor = new CSharpUsingStatementExtractor(tokenTree);
		AstUtil.forEach(extractor.tree, extractor::extractUsingStatementsSubTreeConsumer);
		return extractor.usingStatements;
	}

}
