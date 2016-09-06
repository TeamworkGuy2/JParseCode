package twg2.parser.codeParser;

import twg2.parser.fragment.AstTypeChecker;


/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public interface AstUtil<T_BLOCK extends BlockType, T_KEYWORD> {

	public AccessModifierParser<AccessModifierEnum, T_BLOCK> getAccessModifierParser();

	public AstTypeChecker<T_KEYWORD> getChecker();
}
