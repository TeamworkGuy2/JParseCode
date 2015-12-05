package test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import lombok.val;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;
import baseAst.field.FieldSig;
import baseAst.method.MethodSig;
import codeParser.CodeFile;
import codeParser.CodeFragmentType;
import codeParser.codeStats.ParseDirectoryCodeFiles;
import codeParser.csharp.CSharpDataModelExtractor;
import codeParser.csharp.CSharpInterfaceExtractor;
import documentParser.DocumentFragmentText;
import documentParser.DocumentParser;

/**
 * @author TeamworkGuy2
 * @since 2015-11-22
 */
public class ParseCodeFile {


	public static void printParseFileInfo(String fileName, CodeFile<DocumentFragmentText<CodeFragmentType>> parsedFile, boolean printParsedTokens, boolean printUnparsedSrcCode,
			boolean printFieldSignatures, boolean printMethodSignatures) {
		val tree = parsedFile.getDoc();

		System.out.println("\nFile: " + fileName);
		if(printParsedTokens) {
			SimpleTreeUtil.traverseLeafNodes(tree, TreeTraversalOrder.PRE_ORDER, (token, idx, parent) -> {
				System.out.println(token.getFragmentType() + ": " + token.getTextFragment().getText(parsedFile.getSrc()));
			});
		}

		if(printUnparsedSrcCode) {
			// recreate the source, excluding the parsed elements
			System.out.println("\n====\n" + DocumentParser.toSource(tree, parsedFile.getSrc(), false));
		}

		if(printFieldSignatures) {
			List<FieldSig> intfMethods = CSharpDataModelExtractor.extractDataModelFieldsMethods(parsedFile);
			System.out.println("\n====\n" + StringJoin.Objects.join(intfMethods, "\n"));
		}

		if(printMethodSignatures) {
			List<MethodSig> intfMethods = CSharpInterfaceExtractor.extractInterfaceMethods(parsedFile);
			System.out.println("\n====\n" + StringJoin.Objects.join(intfMethods, "\n"));
		}

	}


	public static void main(String[] args) throws IOException {
		Path file = Paths.get("./rsc/ITrackSearchService.cs");
		//Path file = Paths.get("./rsc/TrackInfo.cs");
		val files = Arrays.asList(file);
		val parsedFiles = ParseDirectoryCodeFiles.parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			printParseFileInfo(files.get(i).toString(), parsedFile, true, true, true, true);
		}

	}

}
