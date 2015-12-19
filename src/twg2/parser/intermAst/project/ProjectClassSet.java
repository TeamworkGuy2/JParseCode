package twg2.parser.intermAst.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.util.NameUtil;
import twg2.parser.intermAst.classes.IntermClass;
import lombok.val;

/** A group of classes/interfaces
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ProjectClassSet<T_BLOCK extends CompoundBlock> {
	private Map<String, IntermClass<T_BLOCK>> compilationUnitsByFullyQualifyingName = new HashMap<>();


	public void addCompilationUnit(List<String> fullyQualifyingName, IntermClass<T_BLOCK> classUnit) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		compilationUnitsByFullyQualifyingName.put(fullName, classUnit);
	}


	public IntermClass<T_BLOCK> getCompilationUnit(List<String> fullyQualifyingName) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		return compilationUnitsByFullyQualifyingName.get(fullName);
	}


	public List<IntermClass<T_BLOCK>> getCompilationUnitsStartWith(List<String> startOfFullyQualifyingName) {
		String startName = NameUtil.joinFqName(startOfFullyQualifyingName);
		List<IntermClass<T_BLOCK>> resBlocks = new ArrayList<>();

		for(val entry : compilationUnitsByFullyQualifyingName.entrySet()) {
			if(entry.getKey().startsWith(startName)) {
				resBlocks.add(entry.getValue());
			}
		}
		return resBlocks;
	}

}
