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
	public final CodeFileSrc<CodeLanguage> ast;
	public final List<CodeFileParsed.Simple<String, T_BLOCK>> parsedBlocks;


	private CodeFileAndAst(CodeLanguage lang, String fileName, String fullClassName, char[] srcCode, int srcOff, int srcLen,
			CodeFileSrc<CodeLanguage> ast, List<CodeFileParsed.Simple<String, T_BLOCK>> parsedBlocks) {
		super();
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
		CodeFileSrc<CodeLanguage> ast = ParseCodeFile.parseCode(fileName, lang, srcCode, 0, srcCode.length, null, null);
		List<CodeFileParsed.Simple<String, _T_BLOCK>> parsedBlocks = new ArrayList<CodeFileParsed.Simple<String, _T_BLOCK>>();

		if(print) {
			System.out.println(srcCode);
		}

		@SuppressWarnings("unchecked")
		List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<_T_BLOCK>>> blockDeclarations = ((AstExtractor<_T_BLOCK>)lang.getExtractor()).extractClassFieldsAndMethodSignatures(ast.getDoc());
		for(Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<_T_BLOCK>> block : blockDeclarations) {
			//CodeFileParsed.Simple<CodeFileSrc<DocumentFragmentText<CodeFragmentType>, CodeLanguage>, CompoundBlock> fileParsed = new CodeFileParsed.Simple<>(parsedFile, block.getValue(), block.getKey());
			CodeFileParsed.Simple<String, _T_BLOCK> fileParsed = new CodeFileParsed.Simple<>(fileName, block.getValue(), block.getKey());
			parsedBlocks.add(fileParsed);

			try {
				WriteSettings ws = new WriteSettings(true, true, true, true);
				StringBuilder sb = new StringBuilder();
				fileParsed.getParsedClass().toJson(sb, ws);

				if(print) {
					System.out.println(sb.toString());
				}
			} catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
		}

		CodeFileAndAst<_T_BLOCK> inst = new CodeFileAndAst<_T_BLOCK>(lang, fileName, fullClassName, srcCode, 0, srcCode.length, ast, parsedBlocks);

		return inst;
	}

}
