package me.neznamy.tab.platforms.krypton.event

import me.neznamy.tab.api.TabPlayer

/**
 * Krypton event that is called when player is successfully loaded after
 * joining. This also includes plugin reloading.
 */
// Thanks kapt...
//@JvmRecord
data class TabPlayerLoadEvent(val player: TabPlayer)
