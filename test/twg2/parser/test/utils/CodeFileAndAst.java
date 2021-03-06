package twg2.parser.test.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import twg2.ast.interm.classes.ClassAst;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.BlockType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguage;
import twg2.parser.main.ParseCodeFile;
import twg2.parser.output.WriteSettings;
import twg2.parser.workflow.CodeFileParsed;
import twg2.parser.workflow.CodeFileSrc;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-09-05
 */
public class CodeFileAndAst<T_BLOCK extends BlockType> {
	public final CodeLanguage lang;
	public final String fileName;
	public final String fullClassName;
	public final char[] srcCode;
	public final int srcOff;
	public final int srcLen;
	public final CodeFileSrc ast;
	public final List<CodeFileParsed.Simple<T_BLOCK>> parsedBlocks;


	private CodeFileAndAst(CodeLanguage lang, String fileName, String fullClassName, char[] srcCode, int srcOff, int srcLen,
			CodeFileSrc ast, List<CodeFileParsed.Simple<T_BLOCK>> parsedBlocks) {
		this.fileName = fileName;
		this.lang = lang;
		this.fullClassName = fullClassName;
		this.srcCode = srcCode;
		this.srcOff = srcOff;
		this.srcLen = srcLen;
		this.ast = ast;
		this.parsedBlocks = parsedBlocks;
	}



	public static <_T_BLOCK extends BlockType> CodeFileAndAst<_T_BLOCK> parse(CodeLanguage lang, String fileName, String fullClassName, boolean print, Iterable<String> srcCodeLines) {
		char[] srcCode = StringJoin.join(srcCodeLines, "\n").toCharArray();
		CodeFileSrc ast = ParseCodeFile.parseCode(fileName, lang, srcCode, 0, srcCode.length, null, null);

		if(print) {
			System.out.println(srcCode);
		}

		@SuppressWarnings("unchecked")
		List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<_T_BLOCK>>> blockDeclarations = ((AstExtractor<_T_BLOCK>)lang.getExtractor()).extractClassFieldsAndMethodSignatures(ast.astTree);

		var parsedBlocks = new ArrayList<CodeFileParsed.Simple<_T_BLOCK>>();

		for(var block : blockDeclarations) {
			var fileParsed = new CodeFileParsed.Simple<_T_BLOCK>(fileName, block.getValue(), block.getKey());
			parsedBlocks.add(fileParsed);

			try {
				WriteSettings ws = new WriteSettings(true, true, true, true);
				StringBuilder sb = new StringBuilder();
				fileParsed.parsedClass.toJson(sb, ws);

				if(print) {
					System.out.println(sb.toString());
				}
			} catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}

		return new CodeFileAndAst<_T_BLOCK>(lang, fileName, fullClassName, srcCode, 0, srcCode.length, ast, parsedBlocks);
	}

}
