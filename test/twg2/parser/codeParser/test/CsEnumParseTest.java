package twg2.parser.codeParser.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldSig;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.CodeFileAndAst;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class CsEnumParseTest {
	private static final CodeFileAndAst<CsBlock> simpleEnumCs = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.C_SHARP, "SimpleEnumCs.cs", "ParserExamples.Samples.SimpleEnumCs", true, Arrays.asList(
		"namespace ParserExamples.Samples {",
		"",
		"  /// <summary>",
		"  /// A simple enum to test parsing.",
		"  /// </summary>",
		"  public enum SimpleEnumCs : short {",
		"    /// <value>The fields enum</value>",
		"    Fields = 2,",
		"    Methods = 3,",
		"    /// <value>The classes enum</value>",
		"    Classes = 4",
	    "  }",
		"",
		"}"
	));


	@Test
	public void simpleEnumCsParseTest() {
		List<CodeFileParsed.Simple<String, CsBlock>> blocks = simpleEnumCs.parsedBlocks;
		String fullClassName = simpleEnumCs.fullClassName;
		Assert.assertEquals(1, blocks.size());
		ClassAst.SimpleImpl<CsBlock> clas = blocks.get(0).getParsedClass();
		List<FieldDef> enums = clas.getEnumMembers();
		Assert.assertEquals(3, enums.size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("enum", clas.getSignature().getDeclarationType());

		FieldSig f = enums.get(0);
		Assert.assertEquals(fullClassName + ".Fields", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("short", f.getFieldType().getTypeName());
		Assert.assertEquals(Arrays.asList(" <value>The fields enum</value>\n"), f.getComments());

		f = enums.get(1);
		Assert.assertEquals(fullClassName + ".Methods", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("short", f.getFieldType().getTypeName());

		f = enums.get(2);
		Assert.assertEquals(fullClassName + ".Classes", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals("short", f.getFieldType().getTypeName());
		Assert.assertEquals(Arrays.asList(" <value>The classes enum</value>\n"), f.getComments());
	}

}
