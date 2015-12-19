package twg2.parser.documentParser.block;

import java.util.List;

/**
 * @author TeamworkGuy2
 * @since 2014-12-7
 */
public interface TextBlock {


	public boolean hasSubBlocks();


	public List<TextBlock> getSubBlocks();


	public boolean hasParentBlock();


	public TextBlock getParentBlock();


	public String getText(String text);


	public String toString(String text);

}

