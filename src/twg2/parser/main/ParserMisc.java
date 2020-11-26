package twg2.parser.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import twg2.ast.interm.classes.ClassAst;
import twg2.dataUtil.dataUtils.ParallelWork;
import twg2.dataUtil.dataUtils.ParallelWork.WorkBlockPolicy;
import twg2.io.files.FileFormatException;
import twg2.io.files.FileReadUtil;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.analytics.PerformanceTrackers;
import twg2.parser.fragment.CodeToken;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.tokenizers.CodeTreeToSource;
import twg2.parser.workflow.CodeFileParsed;
import twg2.parser.workflow.CodeFileSrc;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.TreeTraversalOrder;
import twg2.treeLike.simpleTree.SimpleTree;
import twg2.treeLike.simpleTree.SimpleTreeUtil;

/**
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ParserMisc {


	public static void printParseFileInfo(String fileName, CodeFileSrc parsedFile, boolean printParsedTokens, boolean printUnparsedSrcCode,
			boolean printBlockSignatures, boolean printFieldSignatures, boolean printMethodSignatures) throws FileFormatException {
		var tree = parsedFile.astTree;

		System.out.println("\nFile: " + fileName);
		if(printParsedTokens) {
			SimpleTreeUtil.traverseLeafNodes(tree, TreeTraversalOrder.PRE_ORDER, (token, idx, parent) -> {
				System.out.println(token.getTokenType() + ": " + token.getText());
			});
		}

		if(printUnparsedSrcCode) {
			// recreate the source, excluding the parsed elements
			System.out.println("\n====\n" + CodeTreeToSource.toSource(tree, parsedFile.src, parsedFile.srcOff, parsedFile.srcLen, false));
		}

		try {
			@SuppressWarnings("unchecked")
			List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<BlockType>>> blockDeclarations = ((AstExtractor<BlockType>)parsedFile.language.getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.astTree);

			if(printBlockSignatures) {
				System.out.println("\n==== Blocks: \n" + StringJoin.join(blockDeclarations, "\n"));
			}

			for(var blockInfo : blockDeclarations) {
				var block = blockInfo.getValue();
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
			throw new FileFormatException(parsedFile.srcName, null, e);
		}

	}


	public static <T_BLOCK extends BlockType> void parseFileSet(List<Path> paths, ProjectClassSet.Intermediate<T_BLOCK> dstFileSet,
			ExecutorService executor, ThreadLocal<FileReadUtil> fileReader, PerformanceTrackers perfTracking) throws IOException, FileFormatException {
		@SuppressWarnings("unchecked")
		var dstFiles = (ProjectClassSet.Intermediate<BlockType>)dstFileSet;

		if(executor != null) {
			var dst = new ArrayList<CodeFileParsed.Intermediate<BlockType>>();
			var processedFiles = new HashSet<Path>();

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
					var perfTracker = perfTracking != null ? perfTracking.getOrCreateParseTimes(file.toString()) : null;

					CodeFileSrc parsedFile = ParseCodeFile.parseFile(file, fileReader.get(), perfTracking);

					long start = (perfTracking != null ? System.nanoTime() : 0);

					@SuppressWarnings("unchecked")
					List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<BlockType>>> blockDeclarations = ((AstExtractor<BlockType>)parsedFile.language.getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.astTree);

					for(var block : blockDeclarations) {
						var fileParsed = new CodeFileParsed.Intermediate<>(parsedFile, block.getValue(), block.getKey());
						synchronized(dst) {
							dst.add(fileParsed);
						}
					}

					if(perfTracker != null) {
						perfTracker.setTimeExtractAst(System.nanoTime() - start);
					}

					return parsedFile;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

			synchronized(dst) {
				for(int i = 0, size = dst.size(); i < size; i++) {
					var res = dst.get(i);
					dstFiles.addCompilationUnit(res.parsedClass.getSignature().getFullName(), res);
				}
			}
		}
		else {
			List<CodeFileSrc> parsedFiles = ParseCodeFile.parseFiles(paths, fileReader.get(), perfTracking);

			for(int i = 0, sizeI = paths.size(); i < sizeI; i++) {
				var parsedFile = parsedFiles.get(i);
				var perfTracker = perfTracking != null ? perfTracking.getOrCreateParseTimes(parsedFile.srcName) : null;

				try {
					long start = (perfTracking != null ? System.nanoTime() : 0);

					@SuppressWarnings("unchecked")
					List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<BlockType>>> blockDeclarations = ((AstExtractor<BlockType>)parsedFile.language.getExtractor()).extractClassFieldsAndMethodSignatures(parsedFile.astTree);

					for(var block : blockDeclarations) {
						var fileParsed = new CodeFileParsed.Intermediate<>(parsedFile, block.getValue(), block.getKey());
						dstFiles.addCompilationUnit(block.getValue().getSignature().getFullName(), fileParsed);
					}

					if(perfTracker != null) {
						perfTracker.setTimeExtractAst(System.nanoTime() - start);
					}
				} catch(Exception e) {
					throw new FileFormatException(parsedFile.srcName, null, e);
				}
			}
		}
	}

}
