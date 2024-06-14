package me.elephant1214.inventorymenus.util

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

data class Slot(val stack: ItemStack, val handler: InventoryClickEvent.() -> Unit)
