package test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import parser.textFragment.TextFragmentRef;
import lombok.val;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.TreeTraverse;
import twg2.treeLike.parameters.IndexedTreeTraverseParameters;
import twg2.treeLike.simpleTree.SimpleTreeUtil;
import codeParser.CodeFragmentType;
import codeParser.codeStats.ParseDirectoryCodeFiles;
import documentParser.DocumentFragment;
import documentParser.DocumentParser;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class ParseCodeFile {


	public static void main(String[] args) throws IOException {
		Path file = Paths.get("./res/ITrackSearchService.cs");
		val files = Arrays.asList(file);
		val parsedFiles = ParseDirectoryCodeFiles.parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			System.out.println(parsedFile);
			val tree = parsedFile.getDoc();

			System.out.println("\nFile: " + files.get(i));
			SimpleTreeUtil.traverseLeafNodes(tree, TreeTraversalOrder.PRE_ORDER, (token, idx, parent) -> {
				System.out.println(token.getFragmentType() + ": " + token.getTextFragment().getText(parsedFile.getSrc()));
			});

			TreeTraverse.Indexed.traverse(IndexedTreeTraverseParameters.allNodes(tree, TreeTraversalOrder.PRE_ORDER, (node) -> node.getChildren().size() > 0, (node) -> node.getChildren())
				.setConsumerIndexed((tokenNode, idx, size, depth, parent) -> {
					DocumentFragment<CodeFragmentType> token = tokenNode.getData();
					TextFragmentRef textFrag = token.getTextFragment();
					String text = textFrag.getText(parsedFile.getSrc()).toString();
					if(token.getFragmentType().isCompound() && text.indexOf('(') == 0) {
						System.out.println();
					}
				})
			);

			// recreate the source, excluding the parsed elements
			System.out.println("\n====\n" + DocumentParser.toSource(tree, parsedFile.getSrc(), false));

			i++;
		}

	}

}
