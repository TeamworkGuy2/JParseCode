package twg2.ast.interm.classes;

import java.util.List;

import twg2.parser.codeParser.Keyword;
import twg2.parser.output.JsonWritableSig;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface ClassSig extends JsonWritableSig {

	public Keyword getAccessModifier();

	public List<String> getFullName();

	public String getDeclarationType();

	public String getSimpleName();

}
