package twg2.parser.templates;

import java.util.List;

import twg2.template.codeTemplate.ClassTemplate;

/**
 * @author TeamworkGuy2
 * @since 2015-1-4
 */
public class ParseBlockInfo extends ClassTemplate {
	public List<ParserType> parseTypes;


	public static class ParserType extends ClassTemplate {
		public String startMarkerName;
		public String endMarkerName;
		public String startMarkerLength;
		public String endMarkerLength;
		public String parserType;
		public String markerType;
		public String calcTmpLen;
		public String calcInitStartIndex;
		public String calcInitEndIndex;
		public String calcStartIndex;
		public String calcEndIndex;
		public String calcLastTmpLength;
	}

}
