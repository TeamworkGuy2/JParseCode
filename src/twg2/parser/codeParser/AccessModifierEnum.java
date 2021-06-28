package twg2.parser.codeParser;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public enum AccessModifierEnum implements Keyword {
	PUBLIC,
	NAMESPACE_LOCAL,
	INHERITANCE_LOCAL,
	NAMESPACE_OR_INHERITANCE_LOCAL,
	PRIVATE;


	@Override
	public String toSrc() {
		return this.name();
	}

}
