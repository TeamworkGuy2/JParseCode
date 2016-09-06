package twg2.parser.codeParser;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface BlockType {

	public boolean canContainFields();

	public boolean canContainMethods();

	public boolean isEnum();

	public boolean isInterface();
}
