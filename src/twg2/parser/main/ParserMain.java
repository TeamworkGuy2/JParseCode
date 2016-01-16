package twg2.parser.main;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

import lombok.val;
import twg2.io.files.FileVisitorUtil;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.CodeFileParsed;
import twg2.parser.codeParser.CodeFileSrc;
import twg2.parser.codeParser.CodeLanguage;
import twg2.parser.documentParser.DocumentParser;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ParserMain {


	public static void printParseFileInfo(String fileName, CodeFileSrc<CodeLanguage> parsedFile, boolean printParsedTokens, boolean printUnparsedSrcCode,
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

		@SuppressWarnings("unchecked")
		val blockDeclarations = ((AstExtractor<CompoundBlock>)parsedFile.getLanguage().getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.getDoc());

		if(printBlockSignatures) {
			System.out.println("\n==== Blocks: \n" + StringJoin.Objects.join(blockDeclarations, "\n"));
		}

		for(val blockInfo : blockDeclarations) {
			val block = blockInfo.getValue();
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
		if(extensions.length > 0) {
			fileFilterBldr.getVisitFileFilter().addFileExtensionFilters(true, extensions);
		}
		fileFilterBldr.getVisitFileFilter().setTrackMatches(true);
		val fileFilterCache = fileFilterBldr.build();
		Files.walkFileTree(fileOrDir, EnumSet.noneOf(FileVisitOption.class), depth, fileFilterCache.getFileVisitor());
		val files = fileFilterCache.getVisitFileFilterCache().getMatches();
		return files;
	}


	public static <T_BLOCK extends CompoundBlock> void parseFileSet(List<Path> files, ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, T_BLOCK> dstFileSet) throws IOException {
		@SuppressWarnings("unchecked")
		ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CompoundBlock> dstFileSetCast = (ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, CompoundBlock>)dstFileSet;

		val parsedFiles = ParseCodeFile.parseFiles(files);

		for(int i = 0, sizeI = files.size(); i < sizeI; i++) {
			val parsedFile = parsedFiles.get(i);
			@SuppressWarnings("unchecked")
			val blockDeclarations = ((AstExtractor<CompoundBlock>)parsedFile.getLanguage().getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.getDoc());

			for(val block : blockDeclarations) {
				val fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
				dstFileSetCast.addCompilationUnit(block.getValue().getSignature().getFullName(), fileParsed);
			}
		}
	}

}
