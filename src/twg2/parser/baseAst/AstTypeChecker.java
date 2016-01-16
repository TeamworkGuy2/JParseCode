package twg2.parser.baseAst;

import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
public interface AstTypeChecker<T_KEYWORD> {

	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, T_KEYWORD keyword1);

	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, T_KEYWORD keyword1, T_KEYWORD keyword2);

	public boolean isKeyword(DocumentFragmentText<CodeFragmentType> node, T_KEYWORD keyword1, T_KEYWORD keyword2, T_KEYWORD keyword3);

}
