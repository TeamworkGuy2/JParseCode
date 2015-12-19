package twg2.parser.output;

import java.io.IOException;

/**
 * @author TeamworkGuy2
 * @since 2015-12-10
 */
public interface JsonWritableSig {

	public void toJson(Appendable dst, WriteSettings st) throws IOException;

}
