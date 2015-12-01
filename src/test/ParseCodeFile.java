package test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import lombok.val;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;
import codeParser.codeStats.ParseDirectoryCodeFiles;
import codeParser.csharp.CSharpDirtyInterfaceExtractor;
import codeRepresentation.method.MethodSig;
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

			List<MethodSig> intfMethods = CSharpDirtyInterfaceExtractor.extractInterfaceMethods(parsedFile);

			// recreate the source, excluding the parsed elements
			System.out.println("\n====\n" + DocumentParser.toSource(tree, parsedFile.getSrc(), false));
			System.out.println("\n====\n" + intfMethods);

			i++;
		}

	}

}
