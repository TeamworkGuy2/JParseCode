package intermAst.project;

import intermAst.classes.IntermClassWithFieldsMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;
import baseAst.CompoundBlock;
import baseAst.util.NameUtil;

/** A group of classes/interfaces
 * @author TeamworkGuy2
 * @since 2015-12-8
 */
public class ProjectClassSet<T_BLOCK extends CompoundBlock> {
	private Map<String, IntermClassWithFieldsMethods<T_BLOCK>> compilationUnitsByFullyQualifyingName = new HashMap<>();


	public void addCompilationUnit(List<String> fullyQualifyingName, IntermClassWithFieldsMethods<T_BLOCK> classUnit) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		compilationUnitsByFullyQualifyingName.put(fullName, classUnit);
	}


	public IntermClassWithFieldsMethods<T_BLOCK> getCompilationUnit(List<String> fullyQualifyingName) {
		String fullName = NameUtil.joinFqName(fullyQualifyingName);
		return compilationUnitsByFullyQualifyingName.get(fullName);
	}


	public List<IntermClassWithFieldsMethods<T_BLOCK>> getCompilationUnitsStartWith(List<String> startOfFullyQualifyingName) {
		String startName = NameUtil.joinFqName(startOfFullyQualifyingName);
		List<IntermClassWithFieldsMethods<T_BLOCK>> resBlocks = new ArrayList<>();

		for(val entry : compilationUnitsByFullyQualifyingName.entrySet()) {
			if(entry.getKey().startsWith(startName)) {
				resBlocks.add(entry.getValue());
			}
		}
		return resBlocks;
	}

}
