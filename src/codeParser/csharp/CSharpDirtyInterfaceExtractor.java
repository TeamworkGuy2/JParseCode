package codeParser.csharp;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import codeParser.CodeFile;
import codeParser.ParseInput;
import codeRepresentation.method.MethodSig;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CSharpDirtyInterfaceExtractor {

	public static List<MethodSig> parse(ParseInput params) {
		val parsedFile = CSharpClassParser.parse(params);
		val methodDefs = extractInterfaceMethods(parsedFile);
		
		return methodDefs;
	}


	static List<MethodSig> extractInterfaceMethods(CodeFile parsedFile) {
		List<MethodSig> methods = new ArrayList<>();
		// TODO implement
		return methods;
	}

}
