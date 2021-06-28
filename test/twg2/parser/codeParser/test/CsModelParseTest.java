package twg2.parser.codeParser.test;

import static twg2.parser.test.utils.AnnotationAssert.assertAnnotation;
import static twg2.parser.test.utils.MethodAssert.assertParameter;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.method.ParameterSig;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.csharp.CsKeyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;
import static twg2.parser.test.utils.FieldAssert.assertField;
import static twg2.parser.test.utils.TypeAssert.*;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsModelParseTest {
	private static List<String> srcLines = ls(
		"namespace ParserExamples.Samples {",
		"",
		"  /// <summary>",
		"  /// A simple class to test parsing.",
		"  /// </summary>",
		"  protected class Model1Cs {",
		"",
		"    /// <value>The modification count.</value>",
		"    [MultiLineAnnotation(Alpha = \"alpha-1\", ",
		"        Beta = Double.TYPE,",
		"        Charlie = 3)",
		"    ]",
		"    private int mod;",
		"",
		"    private int? initialMod;",
		"",
		"    /// <value>The name.</value>",
		"    private string _name { get; } = \"initial-name\";",
		"",
		"    /// <value>The properties.</value>",
		"    public IDictionary<int, string> Props { get; private set; }",
		"",
		"    /// <summary>Set properties</summary>",
		"    /// <param name=\"props\">the properties</param>",
		"    /// <returns>the properties</returns>",
		"    [SetterAnnotation(Prop = \"Props\", UriTemplate = \"/SetProps?props={props}\",",
		"        ResponseFormat = WebMessageFormat.Json)]",
		"    public static Result<IList<int?>> SetProps(this SimpleCs inst, ref Constraints constraints, params List<string>[] props) {",
		"        content of SetNames;",
		"    }",
		"",
		"    IList<string> hiddenField;",
		"",
		"    IList<Tuple<string, int>>[] _fields;",
		"  }",
		"",
		"}"
	);

	@Parameter
	private CodeFileAndAst<CsBlock> simpleCs = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "Model1Cs.cs", "ParserExamples.Samples.Model1Cs", true, srcLines);


	public CsModelParseTest() throws IOException {
	}


	@Test
	public void model1CsParseTest() {
		List<CodeFileParsed.Simple<CsBlock>> blocks = simpleCs.parsedBlocks;
		String fullClassName = simpleCs.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).parsedClass;
		List<FieldDef> fs = clas.getFields();
		Assert.assertEquals(6, fs.size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.INHERITANCE_LOCAL, clas.getSignature().getAccessModifier());
		Assert.assertEquals("class", clas.getSignature().getDeclarationType());

		assertField(fs, 0, fullClassName + ".mod", "int");
		Assert.assertEquals(ls(" <value>The modification count.</value>\n"), fs.get(0).getComments());
		// annotations: [MultiLineAnnotation(Alpha = "alpha-1", Beta = Double.TYPE, Charlie = 3)]
		assertAnnotation(fs.get(0).getAnnotations(), 0, "MultiLineAnnotation", new String[] { "Alpha", "Beta", "Charlie" }, "alpha-1", "Double.TYPE", "3");

		assertField(fs, 1, fullClassName + ".initialMod", "int?");
		assertField(fs, 2, fullClassName + "._name", "string");
		assertField(fs, 3, fullClassName + ".Props", ary("IDictionary", ary("int", "string")));
		assertField(fs, 4, fullClassName + ".hiddenField", ary("IList", ary("string")));
		assertField(fs, 5, fullClassName + "._fields", ary("IList[]", ary("Tuple", ary("string", "int"))));

		// methods:
		Assert.assertEquals(1, clas.getMethods().size());

		// SetProps()
		MethodSigSimple m = clas.getMethods().get(0);
		Assert.assertEquals(fullClassName + ".SetProps", NameUtil.joinFqName(m.fullName));
		Assert.assertEquals(ls(" <summary>Set properties</summary>\n",
				" <param name=\"props\">the properties</param>\n",
				" <returns>the properties</returns>\n"), m.comments);
		List<ParameterSig> ps = m.paramSigs;
		assertParameter(ps, 0, "inst", "SimpleCs", false, null, ls(CsKeyword.THIS), null);
		assertParameter(ps, 1, "constraints", "Constraints", false, null, ls(CsKeyword.REF), null);
		assertParameter(ps, 2, "props", "List<string>[]", false, null, ls(CsKeyword.PARAMS), null);
		// annotations:
		// [SetterAnnotation(Prop = "Props", UriTemplate = "/SetProps?props={props}", ResponseFormat = WebMessageFormat.Json)]
		assertAnnotation(m.annotations, 0, "SetterAnnotation", new String[] { "Prop", "UriTemplate", "ResponseFormat" }, new String[] { "Props", "/SetProps?props={props}", "WebMessageFormat.Json" });

		//returnType: {"typeName": "Result", "genericParameters": [ {"typeName": "IList", "genericParameters": [ {"typeName": "int", "nullable": true, "primitive": true}]}]}
		assertType(ary("Result", ary("IList", ary("int?"))), m.returnType);
	}

}
