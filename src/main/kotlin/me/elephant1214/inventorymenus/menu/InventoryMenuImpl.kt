package me.elephant1214.inventorymenus.menu

import me.elephant1214.inventorymenus.InventoryMenus
import me.elephant1214.inventorymenus.util.Slot
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class InventoryMenuImpl(
    override val plugin: JavaPlugin,
    override val owner: Player,
    override val title: Component,
    override val size: Int,
    override val allowClose: () -> Boolean,
) : InventoryMenu {
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
        for (i in 0 until this.size) {
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
        this.owner.openInventory(this.inventory)
        return this.inventory
    }

    override fun onClose(handler: InventoryCloseEvent.() -> Unit) {
        this.closeListeners.add(handler)
    }

    override fun close() {
        if (this::inventory.isInitialized) this.inventory.close()
    }

    override fun invokeCloseListeners(event: InventoryCloseEvent) = this.closeListeners.forEach { it(event) }

    override fun destroy() {
        if (this.owner.openInventory == this.inventory) this.owner.closeInventory()
    }
}