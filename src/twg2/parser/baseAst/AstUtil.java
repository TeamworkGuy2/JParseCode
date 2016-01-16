package twg2.parser.baseAst;


/**
 * @author TeamworkGuy2
 * @since 2015-12-6
 */
public interface AstUtil<T_BLOCK extends CompoundBlock, T_KEYWORD> {

	public AccessModifierParser<AccessModifierEnum, T_BLOCK> getAccessModifierParser();

	public AstTypeChecker<T_KEYWORD> getChecker();
}
