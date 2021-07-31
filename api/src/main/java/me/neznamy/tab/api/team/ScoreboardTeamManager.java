package me.neznamy.tab.api.team;

import me.neznamy.tab.api.TabPlayer;

public interface ScoreboardTeamManager {

	/**
	 * Makes player's nametag globally invisible
	 * @see showNametag 
	 * @see hasHiddenNametag
	 */
	public void hideNametag(TabPlayer player);

	/**
	 * Hides player's nametag for specified player until it's shown again
	 * @param viewer - player to hide nametag for
	 */
	public void hideNametag(TabPlayer player, TabPlayer viewer);

	/**
	 * Makes player's nametag visible again
	 * @see hideNametag
	 * @see hasHiddenNametag
	 */
	public void showNametag(TabPlayer player);

	/**
	 * Shows player's nametag for specified viewer if it was hidden before
	 * @param viewer - player to show nametag back for
	 */
	public void showNametag(TabPlayer player, TabPlayer viewer);

	/**
	 * Return whether player has hidden nametag or not
	 * @return Whether player has hidden nametag or not
	 * @see hideNametag
	 * @see showNametag
	 */
	public boolean hasHiddenNametag(TabPlayer player);

	/**
	 * Returns true if nametag is hidden for specified viewer, false if not
	 * @param viewer - player to check visibility status for
	 * @return true if hidden, false if not
	 */
	public boolean hasHiddenNametag(TabPlayer player, TabPlayer viewer);
	
	/**
	 * Unregisters player's team and no longer handles it, as well as disables anti-override for teams.
	 * This can be resumed using resumeTeamHandling(). If team handling was paused already, nothing happens.
	 */
	public void pauseTeamHandling(TabPlayer player);

	/**
	 * Resumes team handling if it was before paused using pauseTeamHandling(), if not, nothing happens
	 */
	public void resumeTeamHandling(TabPlayer player);

	/**
	 * Returns true if team handling is paused for this player using pauseTeamHandling(), false if not or 
	 * it was resumed already using resumeTeamHandling
	 * @return true if paused, false if not
	 */
	public boolean hasTeamHandlingPaused(TabPlayer player);
	
	/**
	 * Forces new team name for the player until this method is called again with null argument and 
	 * performs all actions to change player's team name
	 * @param teamName - forced team name
	 */
	public void forceTeamName(TabPlayer player, String teamName);

	/**
	 * Returns forced team name of player or null if not forced
	 * @return forced team name of player or null if not forced
	 */
	public String getForcedTeamName(TabPlayer player);
	

	/**
	 * Forces collision rule for the player. Setting it to null will remove forced value
	 * @param collision - forced collision rule
	 */
	public void setCollisionRule(TabPlayer player, Boolean collision);

	/**
	 * Returns forced collision rule or null if collision is not forced using setCollisionRule
	 * @return forced value or null if not forced
	 */
	public Boolean getCollisionRule(TabPlayer player);
	
	/**
	 * Sends update team properties packet of player's team to everyone
	 * @param p - player to update team data of
	 */
	public void updateTeamData(TabPlayer p);
}
