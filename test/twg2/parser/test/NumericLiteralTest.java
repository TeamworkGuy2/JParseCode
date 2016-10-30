package twg2.parser.test;

import static twg2.text.tokenizer.test.ParserTestUtils.parseTest;

import org.junit.Test;

import twg2.parser.primitive.NumericParser;

/**
 * @author TeamworkGuy2
 * @since 2015-12-12
 */
public class NumericLiteralTest {


	@Test
	public void parseIntTest() {
		String name = "readIntTest";
		NumericParser cond = new NumericParser();

		long[] vals = new long[] {		 132_32, 	 0_312___4,			 0x2_F_3A1L,	 0X3aacab3F3L,		 0xCAB33BABEL };

		parseTest(true, false, name, cond, "132_32");
		parseTest(true, false, name, cond, "0_312___4");
		parseTest(true, false, name, cond, "0x2_F_3A1L");
		parseTest(true, false, name, cond, "0X3aacab3F3L");
		parseTest(true, false, name, cond, "0xCAB33BABEL");
	}


	@Test
	public void parseFloatTest() {
		String name = "readFloatTest";
		NumericParser cond = new NumericParser();

		double[] vals = new double[] { 15_37.4_6e8_2,	 -3_4.98e-54D,		 0x2_F_3.39b1p1D,	 0x3A5.86p143D,	 234.433444D };

		parseTest(true, false, name, cond, "15_37.4_6e8_2");
		parseTest(true, false, name, cond, "3_4.98e-54D");
		parseTest(true, false, name, cond, "0x2_F_3.39b1p1D");
		parseTest(false, true, name, cond, "0x3A5.86p143D__ ");
		parseTest(true, false, name, cond, "234.433444D");
	}


	public static void parseIntHexLiterals() {
		
	}


	public static void parseIntDecimalLiterals() {
		String[] input1 = { "0", "2", "0372", "0xDada_Cafe", "1996", "0x00_FF__00_FF" };
		String[] input2 = { "0l", "0777L", "0x100000000L", "2_147_483_648L", "0xC0B0L" };
		int[] expect1 = { 0, 2, 0372, 0xDada_Cafe, 1996, 0x00_FF__00_FF };
		long[] expect2 = { 0l, 0777L, 0x100000000L, 2_147_483_648L, 0xC0B0L };
	}


	public static void parseIntOctalLiterals() {
		
	}


	public static void parseIntBinaryLiterals() {
		
	}


	public static void parseFloatHexLiterals() {
		
	}


	public static void parseFloatDecimalLiterals() {
		String[] input1 = { "1e1f", "2.f", ".3f", "0f", "3.14f", "6.022137e+23f" };
		String[] input2 = { "1e1", "2.", ".3", "0.0", "3.14", "1e-9d", "1e137" };
		float[] expect1 = { 1e1f, 2.f, .3f, 0f, 3.14f, 6.022137e+23f };
		double[] expect2 = { 1e1, 2., .3, 0.0, 3.14, 1e-9d, 1e137 };

	}

}
