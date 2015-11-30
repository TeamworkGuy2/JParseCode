package codeParser;

import java.util.EnumSet;

/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public enum CommentStyle {
	MULTILINE_C_STYLE,
	END_OF_LINE,
	XML_COMMENT;


	static final EnumSet<CommentStyle> multiAndEndOfLine = EnumSet.of(CommentStyle.MULTILINE_C_STYLE, CommentStyle.END_OF_LINE);


	public static EnumSet<CommentStyle> multiAndSingleLine() {
		return multiAndEndOfLine;
	}


	public static EnumSet<CommentStyle> fromFileExtension(String fileExtension) throws IllegalArgumentException {
		EnumSet<CommentStyle> commentStyle = tryFromFileExtension(fileExtension);
		if(commentStyle == null) {
			throw new IllegalArgumentException("unsupported file extension '" + fileExtension + "' for parsing");
		}
		return commentStyle;
	}


	public static EnumSet<CommentStyle> tryFromFileExtension(String fileExtension) {
		if(fileExtension.charAt(0) == '.') {
			fileExtension = fileExtension.substring(1);
		}

		// TODO add C# string literal parsing support
		switch(fileExtension) {
		case "cs":
			return CommentStyle.multiAndSingleLine();
		case "java":
			return CommentStyle.multiAndSingleLine();
		case "js":
			return CommentStyle.multiAndSingleLine();
		case "ts":
			return CommentStyle.multiAndSingleLine();
		case "html":
			return EnumSet.of(CommentStyle.XML_COMMENT);
		case "css":
			return EnumSet.of(CommentStyle.MULTILINE_C_STYLE);
		}
		return null;
	}

}
