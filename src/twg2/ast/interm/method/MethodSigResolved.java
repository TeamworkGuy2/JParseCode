package twg2.ast.interm.method;

import java.util.List;

import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.Keyword;

@Immutable
public class MethodSigResolved extends MethodSig<ParameterSigResolved, TypeSig.TypeSigResolved> {

	public MethodSigResolved(String name, List<String> fullName, List<? extends ParameterSigResolved> paramSigs,
			TypeSig.TypeSigResolved returnType, List<? extends Keyword> accessModifiers, List<? extends AnnotationSig> annotations, List<String> comments) {
		super(name, fullName, paramSigs, returnType, accessModifiers, annotations, comments);
	}

}