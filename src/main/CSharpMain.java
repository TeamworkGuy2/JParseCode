package main;

import intermAst.classes.IntermClassWithFieldsMethods;
import intermAst.project.ProjectClassSet;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import lombok.val;
import twg2.io.files.FileVisitorUtil;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;
import baseAst.csharp.CSharpBlock;
import codeParser.CodeFileSrc;
import codeParser.CodeFragmentType;
import codeParser.CodeLanguage;
import codeParser.codeStats.ParseDirectoryCodeFiles;
import codeParser.csharp.CSharpBlockExtractor;
import documentParser.DocumentFragmentText;
import documentParser.DocumentParser;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class CSharpMain {


	public static void printParseFileInfo(String fileName, CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage> parsedFile, boolean printParsedTokens, boolean printUnparsedSrcCode,
			boolean printBlockSignatures, boolean printFieldSignatures, boolean printMethodSignatures) {
		val tree = parsedFile.getDoc();

		System.out.println("\nFile: " + fileName);
		if(printParsedTokens) {
			SimpleTreeUtil.traverseLeafNodes(tree, TreeTraversalOrder.PRE_ORDER, (token, idx, parent) -> {
				System.out.println(token.getFragmentType() + ": " + token.getText());
			});
		}

		if(printUnparsedSrcCode) {
			// recreate the source, excluding the parsed elements
			System.out.println("\n====\n" + DocumentParser.toSource(tree, parsedFile.getSrc(), false));
		}

		List<IntermClassWithFieldsMethods<CSharpBlock>> blockDeclarations = CSharpBlockExtractor.extractBlockFieldsAndInterfaceMethods(parsedFile.getDoc(), true, true, true);

		if(printBlockSignatures) {
			System.out.println("\n==== Blocks: \n" + StringJoin.Objects.join(blockDeclarations, "\n"));
		}

		for(val block : blockDeclarations) {
			System.out.println("\n==== Block: \n" + block.getSignature());
			if(printFieldSignatures) {
				if(block.getBlockType().canContainFields()) {
					System.out.println("\n\t==== Fields: \n\t" + StringJoin.Objects.join(block.getFields(), "\n\t"));
				}
			}
			if(printMethodSignatures) {
				if(block.getBlockType().canContainMethods()) {
					System.out.println("\n\t==== Methods: \n\t" + StringJoin.Objects.join(block.getMethods(), "\n\t"));
				}
			}
			System.out.println("====\n");
		}

	}


	public static List<Path> getFilesByExtension(Path fileOrDir, int depth, String... extensions) throws IOException {
		val fileFilterBldr = new FileVisitorUtil.Builder();
		fileFilterBldr.getVisitFileFilter().addFileExtensionFilters(true, extensions);
		fileFilterBldr.getVisitFileFilter().setTrackMatches(true);
		val fileFilterCache = fileFilterBldr.build();
		Files.walkFileTree(fileOrDir, EnumSet.noneOf(FileVisitOption.class), depth, fileFilterCache.getFileVisitor());
		val files = fileFilterCache.getVisitFileFilterCache().getMatches();
		return files;
	}


	public static void parseFileSet(List<Path> files, ProjectClassSet<CSharpBlock> dstFileSet) throws IOException {
		val parsedFiles = ParseDirectoryCodeFiles.parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			List<IntermClassWithFieldsMethods<CSharpBlock>> blockDeclarations = CSharpBlockExtractor.extractBlockFieldsAndInterfaceMethods(parsedFile.getDoc(), true, true, true);

			for(val block : blockDeclarations) {
				dstFileSet.addCompilationUnit(block.getSignature().getFullyQualifyingName(), block);
			}
		}
	}

}
