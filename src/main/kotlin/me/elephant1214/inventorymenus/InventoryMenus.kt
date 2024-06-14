package me.elephant1214.inventorymenus

import me.elephant1214.inventorymenus.menu.InventoryMenu
import me.elephant1214.inventorymenus.menu.InventoryMenuImpl
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

object InventoryMenus {
    internal val menus = hashMapOf<UUID, InventoryMenu>()

    fun Player.menu(
        plugin: JavaPlugin,
        owner: Player,
        title: Component,
        size: Int,
        allowClose: () -> Boolean,
        apply: InventoryMenu.() -> Unit = {}
    ) =
        InventoryMenuImpl(plugin, owner, title, size, allowClose).apply(apply).build()
}