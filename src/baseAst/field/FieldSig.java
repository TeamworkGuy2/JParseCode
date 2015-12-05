package baseAst.field;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@AllArgsConstructor
public class FieldSig {
	private @Getter String returnType;
	private @Getter String name;


	@Override
	public String toString() {
		return returnType + " " + name;
	}

}
