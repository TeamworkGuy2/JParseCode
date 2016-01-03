package twg2.parser.main;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import lombok.val;
import twg2.io.files.FileVisitorUtil;
import twg2.parser.baseAst.csharp.CsBlock;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.codeParser.csharp.CsBlockExtractor;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.documentParser.DocumentParser;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class CsMain {


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

		List<IntermClass.SimpleImpl<CsBlock>> blockDeclarations = CsBlockExtractor.extractBlockFieldsAndInterfaceMethods(parsedFile.getDoc());

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


	public static void parseFileSet(List<Path> files, ProjectClassSet<? super IntermClass.SimpleImpl<CsBlock>> dstFileSet) throws IOException {
		val parsedFiles = ParseCodeFile.parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			List<IntermClass.SimpleImpl<CsBlock>> blockDeclarations = CsBlockExtractor.extractBlockFieldsAndInterfaceMethods(parsedFile.getDoc());

			for(val block : blockDeclarations) {
				dstFileSet.addCompilationUnit(block.getSignature().getFullyQualifyingName(), block);
			}
		}
	}

}
