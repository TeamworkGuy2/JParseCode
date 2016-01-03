package twg2.parser.baseAst;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface CompoundBlock {

	public boolean canContainFields();

	public boolean canContainMethods();

	public boolean isInterface();

}
