package me.elephant1214.inventorymenus.menu

import me.elephant1214.inventorymenus.util.Slot
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

interface InventoryMenu {
    val plugin: JavaPlugin
    
    val id: UUID
    
    val owner: Player
    
    val title: Component
    
    val inventory: Inventory
    
    val size: Int
    
    val slots: MutableMap<Int, out Slot>

    val allowClose: () -> Boolean

    fun slot(slot: Int, stack: ItemStack, handler: InventoryClickEvent.() -> Unit)
    
    fun slot(slot: Int, stack: ItemStack)
    
    fun fill(stack: ItemStack)

    fun build(): Inventory
    
    fun onClose(handler: InventoryCloseEvent.() -> Unit)
    
    fun close()
    
    fun invokeCloseListeners(event: InventoryCloseEvent)
    
    fun destroy()
}