package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.platforms.krypton.features.unlimitedtags.KryptonNameTagX
import me.neznamy.tab.shared.Platform
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl
import me.neznamy.tab.shared.features.nametags.NameTag
import me.neznamy.tab.shared.features.sorting.Sorting
import me.neznamy.tab.shared.permission.LuckPerms
import me.neznamy.tab.shared.permission.None
import me.neznamy.tab.shared.permission.PermissionPlugin
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class KryptonPlatform(private val plugin: Main) : Platform(KryptonPacketBuilder) {

    private val server = plugin.server

    override fun detectPermissionPlugin(): PermissionPlugin {
        if (server.pluginManager.isLoaded("luckperms")) return LuckPerms(getPluginVersion("luckperms"))
        return None()
    }

    override fun loadFeatures() {
        val tab = TAB.getInstance()
        if (tab.configuration.isPipelineInjection) {
            tab.featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, KryptonPipelineInjector())
        }

        // Placeholders
        KryptonPlaceholderRegistry(plugin).registerPlaceholders(tab.placeholderManager)
        UniversalPlaceholderRegistry().registerPlaceholders(tab.placeholderManager)

        if (tab.configuration.config.getBoolean("scoreboard-teams.enabled", true)) {
            tab.featureManager.registerFeature(TabConstants.Feature.SORTING, Sorting())
            if (tab.config.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
                tab.featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, KryptonNameTagX(plugin))
            } else {
                tab.featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, NameTag())
            }
        }

        // Load features
        tab.loadUniversalFeatures()
        if (TAB.getInstance().config.getBoolean("bossbar.enabled", false)) {
            TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, BossBarManagerImpl())
        }
        server.players.forEach { TAB.getInstance().addPlayer(KryptonTabPlayer(it, plugin.getProtocolVersion(it))) }
    }

    override fun sendConsoleMessage(message: String, translateColors: Boolean) {
        val component = if (translateColors) LegacyComponentSerializer.legacyAmpersand().deserialize(message) else Component.text(message)
        server.console.sendMessage(component)
    }

    override fun registerUnknownPlaceholder(identifier: String) {
        val manager = TAB.getInstance().placeholderManager
        if (identifier.startsWith("%rel_")) {
            // One day, when PlaceholderAPI v3 is a thing, this will work. One day...
            manager.registerRelationalPlaceholder(identifier, manager.getRelationalRefresh(identifier)) { _, _ -> "" }
            return
        }
        val serverIntervals = manager.serverPlaceholderRefreshIntervals
        val playerIntervals = manager.playerPlaceholderRefreshIntervals
        if (serverIntervals.containsKey(identifier)) {
            manager.registerServerPlaceholder(identifier, serverIntervals.get(identifier)!!) { identifier }
            return
        }
        if (playerIntervals.containsKey(identifier)) {
            manager.registerPlayerPlaceholder(identifier, playerIntervals.get(identifier)!!) { identifier }
            return
        }
        manager.registerPlayerPlaceholder(identifier, manager.defaultRefresh) { identifier }
    }

    override fun getPluginVersion(plugin: String): String? = server.pluginManager.getPlugin(plugin)?.description?.version
}
