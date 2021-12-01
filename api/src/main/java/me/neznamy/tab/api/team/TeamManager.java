package me.neznamy.tab.api.team;

import me.neznamy.tab.api.TabPlayer;

public interface TeamManager {

	/**
	 * Makes player's NameTag globally invisible
	 * @see #showNametag(TabPlayer)
	 * @see #hasHiddenNametag(TabPlayer)
	 */
	void hideNametag(TabPlayer player);

	/**
	 * Hides player's NameTag for specified player until it's shown again
	 * @param viewer - player to hide NameTag for
	 */
	void hideNametag(TabPlayer player, TabPlayer viewer);

	/**
	 * Makes player's NameTag visible again
	 * @see #hideNametag(TabPlayer)
	 * @see #hasHiddenNametag(TabPlayer)
	 */
	void showNametag(TabPlayer player);

	/**
	 * Shows player's NameTag for specified viewer if it was hidden before
	 * @param viewer - player to show NameTag back for
	 */
	void showNametag(TabPlayer player, TabPlayer viewer);

	/**
	 * Return whether player has hidden NameTag or not
	 * @return Whether player has hidden NameTag or not
	 * @see #hideNametag(TabPlayer)
	 * @see #showNametag(TabPlayer)
	 */
	boolean hasHiddenNametag(TabPlayer player);

	/**
	 * Returns true if NameTag is hidden for specified viewer, false if not
	 * @param viewer - player to check visibility status for
	 * @return true if hidden, false if not
	 */
	boolean hasHiddenNametag(TabPlayer player, TabPlayer viewer);
	
	/**
	 * Unregisters player's team and no longer handles it, as well as disables anti-override for teams.
	 * This can be resumed using resumeTeamHandling(). If team handling was paused already, nothing happens.
	 */
	void pauseTeamHandling(TabPlayer player);

	/**
	 * Resumes team handling if it was before paused using pauseTeamHandling(), if not, nothing happens
	 */
	void resumeTeamHandling(TabPlayer player);

	/**
	 * Returns true if team handling is paused for this player using pauseTeamHandling(), false if not, or
	 * it was resumed already using resumeTeamHandling
	 * @return true if paused, false if not
	 */
	boolean hasTeamHandlingPaused(TabPlayer player);
	
	/**
	 * Forces new team name for the player until this method is called again with null argument and 
	 * performs all actions to change player's team name
	 * @param teamName - forced team name
	 */
	void forceTeamName(TabPlayer player, String teamName);

	/**
	 * Returns forced team name of player or null if not forced
	 * @return forced team name of player or null if not forced
	 */
	String getForcedTeamName(TabPlayer player);
	

	/**
	 * Forces collision rule for the player. Setting it to null will remove forced value
	 * @param collision - forced collision rule
	 */
	void setCollisionRule(TabPlayer player, Boolean collision);

	/**
	 * Returns forced collision rule or null if collision is not forced using setCollisionRule
	 * @return forced value or null if not forced
	 */
	Boolean getCollisionRule(TabPlayer player);
	
	/**
	 * Sends update team's properties packet of player's team to everyone
	 * @param player - player to update team data of
	 */
	void updateTeamData(TabPlayer player);
	
	void setPrefix(TabPlayer player, String prefix);
	
	void setSuffix(TabPlayer player, String suffix);
	
	void resetPrefix(TabPlayer player);
	
	void resetSuffix(TabPlayer player);
	
	String getCustomPrefix(TabPlayer player);
	
	String getCustomSuffix(TabPlayer player);
	
	String getOriginalPrefix(TabPlayer player);
	
	String getOriginalSuffix(TabPlayer player);
}
