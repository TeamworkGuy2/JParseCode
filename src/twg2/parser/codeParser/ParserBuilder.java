package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.documentParser.DocumentParser;
import twg2.parser.text.CharPrecondition;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textFragment.TextTransformer;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.streams.EnhancedIterator;
import twg2.streams.StringLineSupplier;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class ParserBuilder {
	private DocumentParser<CodeFragmentType> fileParser;


	{
		this.fileParser = new DocumentParser<>();
	}


	public ParserBuilder addParser(CharPrecondition parser, TextTransformer<CodeFragmentType> transformer) {
		this.fileParser.addFragmentParser(parser, transformer);
		return this;
	}


	public ParserBuilder addConstParser(CharPrecondition parser, CodeFragmentType type) {
		this.fileParser.addFragmentParser(parser, (text, off, len) -> type);
		return this;
	}


	/** Parse a source string using the parsers added via ({@link #addParser(CharPrecondition, TextTransformer)} and {@link #addConstParser(CharPrecondition, CodeFragmentType)})
	 * @param src the source string
	 * @param language optional
	 * @param srcName optional
	 * @return a parsed {@link CodeFileSrc} containing {@link DocumentFragmentText} nodes represented the tokens parsed from {@code src}
	 */
	public <L extends CodeLanguage> CodeFileSrc<L> buildAndParse(String src, L language, String srcName) {
		List<String> lines = new ArrayList<>();

		// intercept each line request and add the line to our list of lines
		StringLineSupplier srcLineReader = new StringLineSupplier(src, 0, src.length(), true, true);
		EnhancedIterator<String> lineReader = new EnhancedIterator<>(() -> {
			String str = srcLineReader.get();
			if(str != null) {
				lines.add(str);
			}
			return str;
		});

		TextParser input = new TextParserImpl(lineReader, true);

		val docTextFragment = new TextFragmentRef.ImplMut(0, src.length(), 0, 0, -1, -1);
		val docRoot = new DocumentFragmentText<>(CodeFragmentType.DOCUMENT, docTextFragment, docTextFragment.getText(src).toString());

		SimpleTree<DocumentFragmentText<CodeFragmentType>> docTree = fileParser.parseDocument(input, docRoot,
				(type, frag) -> new DocumentFragmentText<>(type, frag, frag.getText(src).toString()),
				(docFrag) -> docFrag.getFragmentType().isCompound(),
				(parent, child) -> parent != child && parent.getTextFragment().contains(child.getTextFragment()));

		docTextFragment.setLineEnd(input.getLineNumber() - 1);
		docTextFragment.setColumnEnd(input.getColumnNumber() - 1);

		return new CodeFileSrc<>(docTree, srcName, src, lines, language);
	}

}
