package me.elephant1214.inventorymenus.slot

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

/**
 * An element that fills a slot inside a menu.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class Element private constructor(
    val stack: ItemStack,
    val isButton: Boolean,
    val clickHandler: (InventoryClickEvent.() -> Unit)?,
) {
    /**
     * Builder class for elements.
     */
    class Builder {
        var stack: ItemStack = ItemStack.empty()
        var isButton: Boolean = true
        var clickHandler: (InventoryClickEvent.() -> Unit)? = null

        inline fun stack(builder: () -> ItemStack) {
            stack = builder.invoke()
        }

        fun build() = Element(stack, isButton, clickHandler)
    }

    companion object {
        inline fun build(init: Builder.() -> Unit): Element {
            return Builder().apply(init).build()
        }
    }
}
