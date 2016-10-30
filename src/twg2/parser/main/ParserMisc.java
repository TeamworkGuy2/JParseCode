package twg2.parser.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

import lombok.val;
import twg2.dataUtil.dataUtils.ParallelWork;
import twg2.dataUtil.dataUtils.ParallelWork.WorkBlockPolicy;
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.codeParser.analytics.ParseTimes.TrackerAction;
import twg2.parser.language.CodeLanguage;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.tokenizers.CodeTreeToSource;
import twg2.parser.workflow.CodeFileParsed;
import twg2.parser.workflow.CodeFileSrc;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTreeUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ParserMisc {


	public static void printParseFileInfo(String fileName, CodeFileSrc<CodeLanguage> parsedFile, boolean printParsedTokens, boolean printUnparsedSrcCode,
			boolean printBlockSignatures, boolean printFieldSignatures, boolean printMethodSignatures) throws FileFormatException {
		val tree = parsedFile.getDoc();

		System.out.println("\nFile: " + fileName);
		if(printParsedTokens) {
			SimpleTreeUtil.traverseLeafNodes(tree, TreeTraversalOrder.PRE_ORDER, (token, idx, parent) -> {
				System.out.println(token.getTokenType() + ": " + token.getText());
			});
		}

		if(printUnparsedSrcCode) {
			// recreate the source, excluding the parsed elements
			System.out.println("\n====\n" + CodeTreeToSource.toSource(tree, parsedFile.getSrc(), parsedFile.getSrcOff(), parsedFile.getSrcLen(), false));
		}

		try {
			@SuppressWarnings("unchecked")
			val blockDeclarations = ((AstExtractor<BlockType>)parsedFile.getLanguage().getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.getDoc());

			if(printBlockSignatures) {
				System.out.println("\n==== Blocks: \n" + StringJoin.join(blockDeclarations, "\n"));
			}

			for(val blockInfo : blockDeclarations) {
				val block = blockInfo.getValue();
				System.out.println("\n==== Block: \n" + block.getSignature());
				if(printFieldSignatures) {
					if(block.getBlockType().canContainFields()) {
						System.out.println("\n\t==== Fields: \n\t" + StringJoin.join(block.getFields(), "\n\t"));
					}
				}
				if(printMethodSignatures) {
					if(block.getBlockType().canContainMethods()) {
						System.out.println("\n\t==== Methods: \n\t" + StringJoin.join(block.getMethods(), "\n\t"));
					}
				}
				System.out.println("====\n");
			}
		} catch(Exception e) {
			throw new FileFormatException(parsedFile.getSrcName(), null, e);
		}

	}


	public static <T_BLOCK extends BlockType> void parseFileSet(List<Path> paths, ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, T_BLOCK> dstFileSet,
			ExecutorService executor, FileReadUtil fileReader, PerformanceTrackers perfTracking) throws IOException, FileFormatException {
		@SuppressWarnings("unchecked")
		val dstFiles = (ProjectClassSet.Simple<CodeFileSrc<CodeLanguage>, BlockType>)dstFileSet;

		if(executor != null) {
			val dst = Collections.synchronizedList(new ArrayList<CodeFileParsed.Simple<CodeFileSrc<CodeLanguage>, BlockType>>());
			val processedFiles = new HashSet<Path>();

			// TODO should add a consumeBlocks or similar function, since we don't have a result to return
			ParallelWork.transformBlocks(WorkBlockPolicy.newFixedBlockSize(40, executor), paths, (path) -> {
				synchronized(processedFiles) {
					if(processedFiles.contains(path)) {
						System.err.println("already parsed '" + path + "'");
					}
					processedFiles.add(path);
				}
				try {
					File file = path.toFile();
					val perfTracker = perfTracking != null ? perfTracking.getOrCreateParseTimes(file.toString()) : null;

					CodeFileSrc<CodeLanguage> parsedFile = ParseCodeFile.parseFile(file, fileReader, perfTracking);

					long start = 0;
					if(perfTracking != null) { start = System.nanoTime(); }

					val blockDeclarations = castParser(parsedFile.getLanguage().getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.getDoc());

					for(val block : blockDeclarations) {
						val fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
						dst.add(fileParsed);
					}

					if(perfTracker != null) {
						perfTracker.log(TrackerAction.PARSE, System.nanoTime() - start);
					}

					return parsedFile;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

			for(int i = 0, size = dst.size(); i < size; i++) {
				val res = dst.get(i);
				dstFiles.addCompilationUnit(res.getParsedClass().getSignature().getFullName(), res);
			}
		}
		else {
			val parsedFiles = ParseCodeFile.parseFiles(paths, fileReader, perfTracking);

			for(int i = 0, sizeI = paths.size(); i < sizeI; i++) {
				val parsedFile = parsedFiles.get(i);
				val perfTracker = perfTracking != null ? perfTracking.getOrCreateParseTimes(parsedFile.getSrcName()) : null;

				try {
					long start = 0;
					if(perfTracking != null) { start = System.nanoTime(); }

					@SuppressWarnings("unchecked")
					val blockDeclarations = ((AstExtractor<BlockType>)parsedFile.getLanguage().getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.getDoc());

					for(val block : blockDeclarations) {
						val fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
						dstFiles.addCompilationUnit(block.getValue().getSignature().getFullName(), fileParsed);
					}

					if(perfTracker != null) {
						perfTracker.log(TrackerAction.PARSE, System.nanoTime() - start);
					}
				} catch(Exception e) {
					throw new FileFormatException(parsedFile.getSrcName(), null, e);
				}
			}
		}
	}


	@SuppressWarnings("unchecked")
	private static AstExtractor<BlockType> castParser(AstExtractor<? extends BlockType> extractor) {
		return (AstExtractor<BlockType>)extractor;
	}

}
