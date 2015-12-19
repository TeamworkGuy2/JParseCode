package twg2.parser.codeParser;

import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
@AllArgsConstructor
public class ParseInput {
	@Getter private final String src;
	@Getter private final Consumer<Exception> errorHandler;
	@Getter private final String fileName; // optional

	@Override
	public String toString() {
		return "parseInput: { file: " + fileName + " }";
	}

}
