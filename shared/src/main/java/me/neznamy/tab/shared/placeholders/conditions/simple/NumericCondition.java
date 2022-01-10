package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public abstract class NumericCondition extends SimpleCondition {

	private boolean leftSideStatic;
	private float leftSideValue;
	private boolean rightSideStatic;
	private float rightSideValue;
	
	@Override
	protected void setSides(String leftSide, String rightSide) {
		super.setSides(leftSide, rightSide);
		try {
			leftSideValue = Float.parseFloat(leftSide);
			leftSideStatic = true;
		} catch (NumberFormatException e) {
			//not a valid number
		}
		try {
			rightSideValue = Float.parseFloat(rightSide);
			rightSideStatic = true;
		} catch (NumberFormatException e) {
			//not a valid number
		}
	}
	
	public double getLeftSide(TabPlayer p) {
		if (leftSideStatic) return leftSideValue;
		String value = parseLeftSide(p);
		if (value.contains(",")) value = value.replace(",", "");
		return TAB.getInstance().getErrorManager().parseDouble(value, 0);
	}
	
	public double getRightSide(TabPlayer p) {
		if (rightSideStatic) return rightSideValue;
		String value = parseRightSide(p);
		if (value.contains(",")) value = value.replace(",", "");
		return TAB.getInstance().getErrorManager().parseDouble(value, 0);
	}
}