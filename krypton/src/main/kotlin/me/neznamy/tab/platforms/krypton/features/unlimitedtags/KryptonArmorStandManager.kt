package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.ArmorStand
import me.neznamy.tab.api.ArmorStandManager
import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX

class KryptonArmorStandManager(nameTagX: NameTagX, owner: TabPlayer) : ArmorStandManager {

    private val armorStands = LinkedHashMap<String, ArmorStand>()
    private var armorStandArray = emptyArray<ArmorStand>()

    private val nearbyPlayers = ArrayList<TabPlayer>()
    private var nearbyPlayerArray = emptyArray<TabPlayer>()

    init {
        nameTagX as KryptonNameTagX
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).currentRawValue +
            owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).currentRawValue +
            owner.getProperty(TabConstants.Property.TAGSUFFIX).currentRawValue)

        var height = 0.0
        nameTagX.dynamicLines.forEach { line ->
            addArmorStand(line, KryptonArmorStand(this, nameTagX, owner, line, height, false))
            height += 0.26
        }
        nameTagX.staticLines.forEach { line ->
            addArmorStand(line.key, KryptonArmorStand(this, nameTagX, owner, line.key, line.value.toString().toDouble(), true))
        }
        fixArmorStandHeights()
    }

    fun teleport(viewer: TabPlayer) {
        armorStandArray.forEach { it.teleport(viewer) }
    }

    fun teleport() {
        armorStandArray.forEach { it.teleport() }
    }

    fun nearbyPlayers(): Array<TabPlayer> = nearbyPlayerArray

    fun isNearby(viewer: TabPlayer): Boolean = nearbyPlayers.contains(viewer)

    fun hasArmorStandWithId(entityId: Int): Boolean = armorStandArray.any { it.entityId == entityId }

    fun sneak(sneaking: Boolean) {
        armorStandArray.forEach { it.sneak(sneaking) }
    }

    fun respawn() {
        armorStandArray.forEach { stand ->
            nearbyPlayerArray.forEach { player -> stand.respawn(player) }
        }
    }

    fun spawn(viewer: TabPlayer) {
        nearbyPlayers.add(viewer)
        nearbyPlayerArray = nearbyPlayers.toTypedArray()
        armorStandArray.forEach { it.spawn(viewer) }
    }

    private fun fixArmorStandHeights() {
        var currentY = -0.26
        armorStandArray.forEach { stand ->
            if (stand.hasStaticOffset()) return@forEach
            if (stand.property.get().isNotEmpty()) {
                currentY += 0.26
                stand.offset = currentY
            }
        }
    }

    fun addArmorStand(name: String, stand: ArmorStand) {
        armorStands.put(name, stand)
        armorStandArray = armorStands.values.toTypedArray()
        nearbyPlayerArray.forEach { stand.spawn(it) }
    }

    fun unregisterPlayer(viewer: TabPlayer) {
        if (nearbyPlayers.remove(viewer)) nearbyPlayerArray = nearbyPlayers.toTypedArray()
    }

    fun updateVisibility(force: Boolean) {
        armorStandArray.forEach { it.updateVisibility(force) }
    }

    fun destroy(viewer: TabPlayer) {
        armorStandArray.forEach { it.destroy(viewer) }
        unregisterPlayer(viewer)
    }

    override fun destroy() {
        armorStandArray.forEach { it.destroy() }
        nearbyPlayers.clear()
        nearbyPlayerArray = emptyArray()
    }

    override fun refresh(force: Boolean) {
        var fix = false
        armorStandArray.forEach { stand ->
            if (stand.property.update() || force) {
                stand.refresh()
                fix = true
            }
        }
        if (fix) fixArmorStandHeights()
    }
}