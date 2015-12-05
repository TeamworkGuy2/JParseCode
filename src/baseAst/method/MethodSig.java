package baseAst.method;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
@AllArgsConstructor
public class MethodSig {
	private @Getter String returnType;
	private @Getter String name;
	private @Getter String paramsSig;


	@Override
	public String toString() {
		return returnType + " " + name + paramsSig;
	}

}
