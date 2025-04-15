package me.elephant1214.inventorymenus

import me.elephant1214.inventorymenus.menu.InventoryMenu
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
object InventoryMenus : Listener {
    private lateinit var initPlugin: JavaPlugin

    /**
     * Initializes InventoryMenus, registering all events using your plugin.
     * This is REQUIRED for the library to function.
     */
    fun init(plugin: JavaPlugin) {
        initPlugin = plugin
        Bukkit.getServer().pluginManager.registerEvents(this, plugin)
    }

    /**
     * Contains every menu currently being handled by the library.
     */
    internal val menus = HashMap<UUID, InventoryMenu>()

    /**
     * Handles click events for each menu.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onInvClick(event: InventoryClickEvent) {
        if (event.whoClicked !is Player) return

        val menu = getMenuByViewer(event.whoClicked as Player) ?: return
        if (menu.owner != null && event.whoClicked != menu.owner) {
            event.isCancelled = true
            event.whoClicked.closeInventory(InventoryCloseEvent.Reason.CANT_USE)
            return
        }

        if (event.currentItem != null && event.inventory == menu.inventory && event.clickedInventory != null) {
            if (event.clickedInventory != menu.inventory) {
                event.isCancelled = true
            } else {
                val slot = menu.slot(event.slot)!!
                if (slot.isButton) event.isCancelled = true
                if (slot.clickHandler != null) slot.clickHandler.invoke(event)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onItemDrag(event: InventoryDragEvent) {
        if (event.whoClicked !is Player) return

        getMenuByViewer(event.whoClicked as Player) ?: return
        event.isCancelled = true
    }

    /**
     * Stops players from closing a menu if they aren't allow to close it. Otherwise,
     * invokes the close listeners and removes it from the map of tracked menus.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onInvClose(event: InventoryCloseEvent) {
        if (event.player !is Player) return

        val menu = menus.entries.firstOrNull { it.value.inventory.viewers.contains(event.player) } ?: return
        if (!menu.value.allowClose.invoke() && event.reason != InventoryCloseEvent.Reason.CANT_USE) {
            scheduleMenuReopen(menu.value, event.player as Player)
            return
        }

        menu.value.close(event)
        menus.remove(menu.key, menu.value)
    }

    /**
     * Stops potential duplication problems by disabling hand swapping in menus.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onSwapHandItems(event: PlayerSwapHandItemsEvent) {
        getMenuByViewer(event.player) ?: return
        event.isCancelled = true
    }

    private fun getMenuByViewer(player: Player) = menus.values.firstOrNull { it.inventory.viewers.contains(player) }

    private fun scheduleMenuReopen(menu: InventoryMenu, player: Player) {
        Bukkit.getScheduler().runTask(initPlugin, Runnable {
            menu.open(player)
        })
    }
}
