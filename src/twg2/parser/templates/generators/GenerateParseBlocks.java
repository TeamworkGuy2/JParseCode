package twg2.parser.templates.generators;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.stringtemplate.v4.ST;

import twg2.parser.templates.ParseBlockInfo;
import twg2.template.codeTemplate.render.StringTemplatesUtil;
import twg2.template.codeTemplate.render.TemplateFilesIo;
import twg2.template.codeTemplate.render.TemplateImports;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2015-1-4
 */
public class GenerateParseBlocks {
	static String tmplDir = "src/twg2/parser/templates/";


	public static final void generateParseBlocks() throws IOException {
		ParseBlockInfo.ParserType strStrParser = new ParseBlockInfo.ParserType();
		{
			ParseBlockInfo.ParserType info = strStrParser;
			info.startMarkerName = "startStr";
			info.startMarkerLength = "startStr.length()";
			info.endMarkerName = "endStr";
			info.endMarkerLength = "endStr.length()";
			info.parserType = "String";
			info.markerType = "String";
			info.calcTmpLen = "tmpLength";
			info.calcInitStartIndex = "input.indexOf(" + info.startMarkerName + ", 0)";
			info.calcInitEndIndex = "input.indexOf(" + info.endMarkerName + ", 0)";
			info.calcStartIndex = "input.indexOf(" + info.startMarkerName + ", startIndex + startStrLen)";
			info.calcEndIndex = "input.indexOf(" + info.endMarkerName + ", endIndex + endStrLen)";
			info.calcLastTmpLength = "input.length() - tmpStart";
		}

		ParseBlockInfo.ParserType strCharParser = new ParseBlockInfo.ParserType();
		{
			ParseBlockInfo.ParserType info = strCharParser;
			info.startMarkerName = "startChar";
			info.startMarkerLength = "1";
			info.endMarkerName = "endChar";
			info.endMarkerLength = "1";
			info.parserType = "String";
			info.markerType = "int";
			info.calcTmpLen = "startIndex - (startIndices.size() - 2) + startStrLen";
			info.calcInitStartIndex = "input.indexOf(" + info.startMarkerName + ", 0)";
			info.calcInitEndIndex = "input.indexOf(" + info.endMarkerName + ", 0)";
			info.calcStartIndex = "StringIndex.indexOf(input, startIndex + startStrLen, " + info.startMarkerName + ")";
			info.calcEndIndex = "StringIndex.indexOf(input, endIndex + endStrLen, " + info.endMarkerName + ")";
			info.calcLastTmpLength = "input.length()";
		}

		ParseBlockInfo.ParserType strBStrParser = new ParseBlockInfo.ParserType();
		{
			ParseBlockInfo.ParserType info = strBStrParser;
			info.startMarkerName = "startStr";
			info.startMarkerLength = "startStr.length()";
			info.endMarkerName = "endStr";
			info.endMarkerLength = "endStr.length()";
			info.parserType = "StringBuilder";
			info.markerType = "String";
			info.calcTmpLen = "tmpLength";
			info.calcInitStartIndex = "input.indexOf(" + info.startMarkerName + ")";
			info.calcInitEndIndex = "input.indexOf(" + info.endMarkerName + ")";
			info.calcStartIndex = "input.indexOf(" + info.startMarkerName + ", startIndex + startStrLen)";
			info.calcEndIndex = "input.indexOf(" + info.endMarkerName + ", endIndex + endStrLen)";
			info.calcLastTmpLength = "input.length() - tmpStart";
		}

		ParseBlockInfo.ParserType strBCharParser = new ParseBlockInfo.ParserType();
		{
			ParseBlockInfo.ParserType info = strBCharParser;
			info.startMarkerName = "startChar";
			info.startMarkerLength = "1";
			info.endMarkerName = "endChar";
			info.endMarkerLength = "1";
			info.parserType = "StringBuilder";
			info.markerType = "int";
			info.calcTmpLen = "startIndex - (startIndices.size() - 2) + startStrLen";
			info.calcInitStartIndex = "StringIndex.indexOf(input, 0, " + info.startMarkerName + ")";
			info.calcInitEndIndex = "StringIndex.indexOf(input, 0, " + info.endMarkerName + ")";
			info.calcStartIndex = "StringIndex.indexOf(input, startIndex + startStrLen, " + info.startMarkerName + ")";
			info.calcEndIndex = "StringIndex.indexOf(input, endIndex + endStrLen, " + info.endMarkerName + ")";
			info.calcLastTmpLength = "input.length()";
		}

		ParseBlockInfo info = new ParseBlockInfo();
		info.className = "ParseBlocks";
		info.packageName = "twg2.parser.documentParser.block";
		info.parseTypes = Arrays.asList(strStrParser, strCharParser, strBStrParser, strBCharParser);

		ST template = StringTemplatesUtil.fileTemplate(tmplDir + "TParseBlocks.stg", "TParseBlocks", TemplateImports.emptyInst());

		Writer out = new FileWriter(TemplateFilesIo.getDefaultInst().getSrcRelativePath(info).toFile());
		StringTemplatesUtil.writeArgs(template, out, Tuples.of("var", info));
		out.close();
	}


	public static void main(String[] args) throws IOException {
		generateParseBlocks();
	}

}
