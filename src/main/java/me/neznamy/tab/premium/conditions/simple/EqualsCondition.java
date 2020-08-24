package me.neznamy.tab.premium.conditions.simple;

import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class EqualsCondition extends SimpleCondition {

	private String leftSide;
	private List<String> leftSidePlaceholders;
	private String rightSide;
	
	public EqualsCondition(String leftSide, String rightSide) {
		this.leftSide = leftSide;
		leftSidePlaceholders = Placeholders.detectAll(leftSide);
		this.rightSide = rightSide;
	}
	
	@Override
	public boolean isMet(ITabPlayer p) {
		String leftSide = this.leftSide;
		for (String identifier : leftSidePlaceholders) {
			Placeholder pl = Placeholders.getPlaceholder(identifier);
			if (pl != null) leftSide = pl.set(leftSide, p);
		}
		return leftSide.equals(rightSide);
	}
	
	public static EqualsCondition compile(String line) {
		if (line.contains("=")) {
			String[] arr = line.split("=");
			return new EqualsCondition(arr[0], arr[1]);
		} else {
			return null;
		}
	}
}