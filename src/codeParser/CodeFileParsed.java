package codeParser;

import documentParser.DocumentFragmentText;
import lombok.Getter;
import baseAst.block.CodeFileAst;

/**
 * @author TeamworkGuy2
 * @since 2015-12-3
 */
public class CodeFileParsed {
	private @Getter CodeFile<DocumentFragmentText<CodeFragmentType>> codeFile;
	private @Getter CodeFileAst parsedAst;

}
