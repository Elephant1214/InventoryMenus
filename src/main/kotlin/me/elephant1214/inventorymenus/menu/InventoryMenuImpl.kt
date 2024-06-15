package me.elephant1214.inventorymenus.menu

import me.elephant1214.inventorymenus.InventoryMenus
import me.elephant1214.inventorymenus.util.Slot
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
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class InventoryMenuImpl(
    override val plugin: JavaPlugin,
    override val owner: Player,
    override val title: Component,
    override val size: Int,
    private val allowClose: () -> Boolean,
) : InventoryMenu, Listener {
    override val id: UUID = UUID.randomUUID()
    override lateinit var inventory: Inventory
    override val slots = HashMap<Int, Slot>(this.size)
    private val closeListeners = mutableListOf<InventoryCloseEvent.() -> Unit>()

    init {
        InventoryMenus.menus[this.id] = this
    }

    override fun slot(slot: Int, stack: ItemStack, handler: InventoryClickEvent.() -> Unit) {
        this.slots[slot] = Slot(stack, handler)
    }

    override fun slot(slot: Int, stack: ItemStack) {
        this.slot(slot, stack) {}
    }

    override fun fill(stack: ItemStack) {
        for (i in 0 until this.size - 1) {
            val value: Slot? = this.slots[i]
            if (value == null) {
                this.slot(i, stack)
            }
        }
    }

    override fun build(): Inventory {
        this.inventory = Bukkit.createInventory(this.owner, this.size, this.title)
        this.slots.forEach { (slot, item) ->
            this.inventory.setItem(slot, item.stack)
        }
        Bukkit.getServer().pluginManager.registerEvents(this, this.plugin)
        this.owner.openInventory(this.inventory)
        return this.inventory
    }

    override fun onClose(handler: InventoryCloseEvent.() -> Unit) {
        this.closeListeners.add(handler)
    }

    override fun close() {
        if (this::inventory.isInitialized) this.inventory.close()
    }

    override fun destroy() {
        if (this.owner.openInventory == this.inventory) this.owner.closeInventory()
    }

    @EventHandler
    private fun onClick(event: InventoryClickEvent) {
        if (event.view.title() != this.title) return
        if (InventoryMenus.menus.contains(this.id)) {
            if (event.view.player != this.owner) {
                event.isCancelled = true
                return
            }

            if (event.currentItem != null && event.inventory == this.inventory && event.clickedInventory != null) {
                if (event.clickedInventory != this.inventory) {
                    event.isCancelled = true
                } else {
                    val slot = this.slots[event.slot]!!
                    event.isCancelled = true
                    slot.handler.invoke(event)
                }
            }
        }
    }

    @EventHandler
    private fun onMoveItem(event: InventoryMoveItemEvent) {
        if (InventoryMenus.menus.contains(this.id) && event.source.holder?.inventory?.viewers?.contains(this.owner)!!
            && event.source.holder is Container && (event.source.holder as Container).customName() == this.title
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler
    private fun onInvClose(event: InventoryCloseEvent) {
        if (!this.allowClose.invoke()) {
            this.owner.openInventory(this.inventory)
            return
        }
        
        this.closeListeners.forEach { it(event) }
        if (event.view.player == this.owner && InventoryMenus.menus.contains(this.id)) InventoryMenus.menus.remove(this.id)
    }
    
    @EventHandler
    private fun onSwapHandItems(event: PlayerSwapHandItemsEvent) {
        if (event.player.inventory == this.inventory) event.isCancelled = true
    }
}