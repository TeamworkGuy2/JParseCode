package twg2.parser.codeParser.test;

import java.util.Arrays;

import lombok.val;

import org.junit.Assert;
import org.junit.Test;

import twg2.ast.interm.field.FieldSig;
import twg2.parser.codeParser.AccessModifierEnum;
import twg2.parser.codeParser.csharp.CsBlock;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.test.utils.CodeFileAndAst;

/**
 * @author TeamworkGuy2
 * @since 2016-1-1
 */
public class JavasEnumParseTest {
	private static final CodeFileAndAst<CsBlock> simpleEnumJava = CodeFileAndAst.<CsBlock>parse(CodeLanguageOptions.JAVA, "SimpleEnumJava.java", "ParserExamples.Samples.SimpleEnumJava", true, Arrays.asList(
		"package ParserExamples.Samples;",
		"",
		"/** A simple enum to test parsing.",
		" */",
		"public enum SimpleEnumJava {",
		"  // The fields enum",
		"  Fields(2),",
		"",
		"  Methods,",
		"",
		"  /** The classes enum */",
		"  Classes(4) {",
		"    @Override",
		"	public String toString() { return \"classic\"; }",
		"  };",
        "",
		"  int secret;",
        "",
		"  SimpleEnumJava() {",
		"    this.secret = -1;",
		"  }",
        "",
		"  SimpleEnumJava(int secret) {",
		"    this.secret = secret;",
		"  }",
        "",
		"}"
	));


	@Test
	public void simpleEnumCsParseTest() {
		val blocks = simpleEnumJava.parsedBlocks;
		val fullClassName = simpleEnumJava.fullClassName;
		Assert.assertEquals(1, blocks.size());
		val clas = blocks.get(0).getParsedClass();
		val enums = clas.getEnumMembers();
		Assert.assertEquals(3, enums.size());

		Assert.assertEquals(fullClassName, NameUtil.joinFqName(clas.getSignature().getFullName()));
		Assert.assertEquals(AccessModifierEnum.PUBLIC, clas.getSignature().getAccessModifier());
		Assert.assertEquals("enum", clas.getSignature().getDeclarationType());

		FieldSig f = enums.get(0);
		Assert.assertEquals(fullClassName + ".Fields", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals(Arrays.asList(" The fields enum\n"), f.getComments());

		f = enums.get(1);
		Assert.assertEquals(fullClassName + ".Methods", NameUtil.joinFqName(f.getFullName()));

		f = enums.get(2);
		Assert.assertEquals(fullClassName + ".Classes", NameUtil.joinFqName(f.getFullName()));
		Assert.assertEquals(Arrays.asList(" The classes enum "), f.getComments());
	}

}
