package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.documentParser.DocumentParser;
import twg2.parser.text.CharParserFactory;
import twg2.parser.textFragment.TextFragmentRef;
import twg2.parser.textFragment.TextTransformer;
import twg2.parser.textParser.TextParser;
import twg2.parser.textParser.TextParserImpl;
import twg2.parser.textStream.CharsLineSupplier;
import twg2.streams.EnhancedIterator;
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


	public ParserBuilder addParser(CharParserFactory parser, TextTransformer<CodeFragmentType> transformer) {
		this.fileParser.addFragmentParser(parser, transformer);
		return this;
	}


	public ParserBuilder addConstParser(CharParserFactory parser, CodeFragmentType type) {
		this.fileParser.addFragmentParser(parser, (text, off, len) -> type);
		return this;
	}


	/** Parse a source string using the parsers added via ({@link #addParser(CharParserFactory, TextTransformer)} and {@link #addConstParser(CharParserFactory, CodeFragmentType)})
	 * @param src the source string
	 * @param language optional
	 * @param srcName optional
	 * @return a parsed {@link CodeFileSrc} containing {@link DocumentFragmentText} nodes represented the tokens parsed from {@code src}
	 */
	public <L extends CodeLanguage> CodeFileSrc<L> buildAndParse(String src, L language, String srcName) {
		List<char[]> lines = new ArrayList<>();

		// intercept each line request and add the line to our list of lines
		CharsLineSupplier srcLineReader = new CharsLineSupplier(src, 0, src.length(), true, true, true, true);
		EnhancedIterator<char[]> lineReader = new EnhancedIterator<>(() -> {
			char[] chs = srcLineReader.get();
			if(chs != null) {
				lines.add(chs);
			}
			return chs;
		});

		TextParser input = TextParserImpl.fromCharArrays(lineReader);

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
