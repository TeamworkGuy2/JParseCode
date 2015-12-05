package codeParser;

import java.util.function.Function;

import lombok.Getter;
import twg2.collections.util.arrayUtils.ArrayUtil;
import codeParser.csharp.CSharpClassParser;
import documentParser.DocumentFragmentText;


/**
 * @author TeamworkGuy2
 * @since 2015-9-19
 */
public enum CodeLanguage {
	JAVA(null, "java"),
	JAVASCRIPT(null, "js", "ts"),
	C_SHARP(CSharpClassParser::parse, "cs"),
	CSS(null, "css"),
	XML(null, "html", "xml");


	@Getter final Function<ParseInput, CodeFile<DocumentFragmentText<CodeFragmentType>>> parser;
	final String[] fileExtensions;


	private CodeLanguage(Function<ParseInput, CodeFile<DocumentFragmentText<CodeFragmentType>>> parser, String... fileExtensions) {
		this.parser = parser;
		this.fileExtensions = fileExtensions;
	}


	public static CodeLanguage fromFileExtension(String fileExtension) throws IllegalArgumentException {
		CodeLanguage lang = tryFromFileExtension(fileExtension);
		if(lang == null) {
			throw new IllegalArgumentException("unsupported file extension '" + fileExtension + "' for parsing");
		}
		return lang;
	}


	public static CodeLanguage tryFromFileExtension(String fileExtension) {
		if(fileExtension.charAt(0) == '.') {
			fileExtension = fileExtension.substring(1);
		}

		for(CodeLanguage lang : CodeLanguage.values()) {
			if(ArrayUtil.indexOf(lang.fileExtensions, fileExtension) > -1) {
				return lang;
			}
		}
		return null;
	}

}
