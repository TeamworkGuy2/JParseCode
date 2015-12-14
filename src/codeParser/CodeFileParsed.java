package codeParser;

import lombok.Getter;
import baseAst.block.CodeFileBlocks;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-3
 */
public class CodeFileParsed<L extends CodeLanguage> {
	private @Getter CodeFileSrc<DocumentFragmentText<CodeFragmentType>, L> codeFile;
	private @Getter CodeFileBlocks parsedAst;

}
