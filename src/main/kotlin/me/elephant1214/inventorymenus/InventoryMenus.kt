package me.elephant1214.inventorymenus

import me.elephant1214.inventorymenus.menu.InventoryMenu
import me.elephant1214.inventorymenus.menu.InventoryMenuImpl
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

@Suppress("unused")
object InventoryMenus : Listener {
    internal val menus = hashMapOf<UUID, InventoryMenu>()

    fun init(plugin: JavaPlugin) = Bukkit.getServer().pluginManager.registerEvents(this, plugin)

    fun Player.menu(
        plugin: JavaPlugin,
        owner: Player,
        title: Component,
        size: Int,
        allowClose: () -> Boolean,
        apply: InventoryMenu.() -> Unit = {}
    ): Inventory = InventoryMenuImpl(plugin, owner, title, size, allowClose).apply(apply).build()

    @EventHandler
    private fun onClick(event: InventoryClickEvent) {
        this.menus.forEach { (_, menu) ->
            if (event.view.title() != menu.title || event.inventory != menu.inventory) return@forEach
            if (event.view.player != menu.owner) {
                event.isCancelled = true
                event.view.player.closeInventory(InventoryCloseEvent.Reason.CANT_USE)
                return
            }

            if (event.currentItem != null && event.inventory == menu.inventory && event.clickedInventory != null) {
                if (event.clickedInventory != menu.inventory) {
                    event.isCancelled = true
                } else {
                    val slot = menu.slots[event.slot]!!
                    event.isCancelled = true
                    slot.handler.invoke(event)
                }
            }
        }
    }

    @EventHandler
    private fun onMoveItem(event: InventoryMoveItemEvent) = this.menus.forEach { (_, menu) ->
        if (event.source.holder?.inventory?.viewers?.contains(menu.owner)!! && event.source.holder is Container
            && (event.source.holder as Container).customName() == menu.owner
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onInvClose(event: InventoryCloseEvent) = this.menus.forEach { (id, menu) ->
        if (event.inventory != menu.inventory || event.view.title() != menu.title || event.view.player != menu.owner) return@forEach
        if (!menu.allowClose.invoke()) {
            menu.owner.openInventory(menu.inventory)
            return
        }

        menu.invokeCloseListeners(event)
        this.menus.remove(id, menu)
    }

    @EventHandler
    private fun onSwapHandItems(event: PlayerSwapHandItemsEvent) = this.menus.forEach { (_, menu) ->
        if (event.player.inventory == menu.inventory) event.isCancelled = true
    }
}