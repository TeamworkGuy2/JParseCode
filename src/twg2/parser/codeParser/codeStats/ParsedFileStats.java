package twg2.parser.codeParser.codeStats;

import lombok.Getter;
import twg2.collections.primitiveCollections.IntArrayList;
import twg2.collections.primitiveCollections.IntList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public final class ParsedFileStats {
	@Getter String srcId;
	@Getter int charCount;
	@JsonIgnore
	@Getter IntList whitespaceLineNumbers;
	@Getter int whitespaceLineCount;
	@JsonIgnore
	@Getter IntList commentLineNumbers;
	@Getter int commentLineCount;
	@Getter int totalLineCount;


	public ParsedFileStats() {
	}


	public ParsedFileStats(String srcId, int charCount, int whitespaceLineCount, int commentLineCount, int totalLineCount) {
		this.srcId = srcId;
		this.charCount = charCount;
		this.whitespaceLineCount = whitespaceLineCount;
		this.commentLineCount = commentLineCount;
		this.totalLineCount = totalLineCount;
	}


	public ParsedFileStats(String srcId, int charCount, IntList whitespaceLineNumbers, IntList commentLineNumbers, int totalLineCount) {
		this.srcId = srcId;
		this.charCount = charCount;
		this.whitespaceLineNumbers = whitespaceLineNumbers;
		this.whitespaceLineCount = whitespaceLineNumbers.size();
		this.commentLineNumbers = commentLineNumbers;
		this.commentLineCount = commentLineNumbers.size();
		this.totalLineCount = totalLineCount;
	}


	@JsonProperty("whitespaceLineNumbers")
	public int[] _getWhitespaceLineNumbers() {
		return whitespaceLineNumbers != null ? whitespaceLineNumbers.toArray() : null;
	}


	@JsonProperty("whitespaceLineNumbers")
	public void setWhitespaceLineNumbers(int[] whitespaceLineNumbers) {
		this.whitespaceLineNumbers = whitespaceLineNumbers != null ? IntArrayList.of(whitespaceLineNumbers) : null;
	}


	@JsonProperty("commentLineNumbers")
	public int[] _getCommentLineNumbers() {
		return commentLineNumbers != null ? commentLineNumbers.toArray() : null;
	}


	@JsonProperty("commentLineNumbers")
	public void setCommentLineNumbers(int[] commentLineNumbers) {
		this.commentLineNumbers = commentLineNumbers != null ? IntArrayList.of(commentLineNumbers) : null;
	}


}