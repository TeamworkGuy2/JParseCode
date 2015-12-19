package twg2.parser.codeParser.codeStats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author TeamworkGuy2
 * @since 2015-9-20
 */
@AllArgsConstructor
@NoArgsConstructor
public final class ParsedCategoryStats {
	@Getter String srcId;
	@Getter int charCount;
	@Getter int fileCount;
	@Getter int whitespaceLineCount;
	@Getter int commentLineCount;
	@Getter int totalLineCount;

}