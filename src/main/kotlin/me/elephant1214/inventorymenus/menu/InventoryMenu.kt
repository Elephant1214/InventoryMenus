package me.elephant1214.inventorymenus.menu

import me.elephant1214.inventorymenus.InventoryMenus
import me.elephant1214.inventorymenus.slot.Element
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")
class InventoryMenu private constructor(
    val owner: Player?,
    val title: Component,
    val size: Int,
    val allowClose: () -> Boolean,
    private val onClose: (InventoryCloseEvent.() -> Unit)?,
    private val elements: MutableMap<Int, Element>,
) {
    val id: UUID = UUID.randomUUID()
    val inventory: Inventory = Bukkit.createInventory(owner, size, title).also { inv ->
        elements.forEach { (slot, stack) ->
            inv.setItem(slot, stack.stack)
        }
    }

    /**
     * Run when the class is constructed, adds this menu to the map that holds all the
     * existing menus.
     */
    init {
        InventoryMenus.menus[id] = this
    }

    /**
     * Gets the element at the [index]. Null there is no element in that slot.
     */
    fun slot(index: Int): Element? = elements[index]

    /**
     * Sets the slot at [index] to the new [element].
     */
    fun setSlot(index: Int, element: Element) {
        elements[index] = element
        inventory.setItem(index, element.stack)
    }

    /**
     * Clears the slot at [index].
     */
    fun clearSlot(index: Int) {
        elements.remove(index)
        inventory.setItem(index, null)
    }

    /**
     * Opens this menu for the provided [player]. Does nothing if the menu has a non-null
     * owner and the player is not the owner.
     */
    fun open(player: Player) {
        if (owner == null || owner == player) {
            player.openInventory(inventory)
        }
    }

    /**
     * Invokes the close handler using the supplied [InventoryCloseEvent] then closes
     * every active view of the inventory.
     */
    fun close(event: InventoryCloseEvent) {
        invokeCloseListener(event)
        forceClose()
    }

    /**
     * Invokes the closer listener for this menu.
     */
    fun invokeCloseListener(event: InventoryCloseEvent) {
        onClose?.invoke(event)
    }

    /**
     * Forces all active views to close.
     */
    fun forceClose() {
        inventory.close()
    }

    /**
     * Builder class for menus.
     */
    class Builder(
        private val size: Int,
    ) {
        var owner: Player? = null
        private var title: Component = Component.empty()
        private val slots = HashMap<Int, Element>(size)
        private var allowClose: () -> Boolean = { true }
        private var onClose: (InventoryCloseEvent.() -> Unit)? = null
        private var defaultClickHandler: (InventoryClickEvent.() -> Unit)? = null

        /**
         * Sets the title of this menu with the provided [TextComponent] builder.
         */
        fun title(builder: () -> Component) {
            title = builder()
        }

        /**
         * Puts the element created with the [builder] into the slot at [index]. The
         * default click handler is set before the [builder] is processed, so if you want
         * to use the default handler for this element, just don't set a handler in the
         * [builder].
         */
        fun slot(index: Int, builder: Element.Builder.() -> Unit) {
            slots[index] = Element.build {
                clickHandler = defaultClickHandler
                builder()
            }
        }

        /**
         * Fills every slot from [start] to [end], ignoring occupied slots depending on
         * [ignoreOccupied], with the element produced by [builder].
         */
        fun fill(start: Int, end: Int, ignoreOccupied: Boolean, builder: Element.Builder.() -> Unit) {
            val filler = Element.build(builder)

            for (index in start until end) {
                if (!ignoreOccupied || slots[index] == null) {
                    slots[index] = filler
                }
            }
        }

        /**
         * Fills all empty slots in the menu with a button that does nothing.
         */
        fun fillEmpty(item: ItemStack) {
            val dummyButton = Element.build {
                stack = item
                isButton = true
                clickHandler = null
            }

            for (index in 0 until size) {
                if (slots[index] == null) {
                    slots[index] = dummyButton
                }
            }
        }

        /**
         * Sets the function to determine whether to allow players to close the menu.
         * `true`/`false` will allow and deny players from closing the menu respectively,
         * defaults to `true`.
         */
        fun allowClose(function: () -> Boolean) {
            allowClose = function
        }

        /**
         * Sets the close handler. Defaults to null and does nothing.
         */
        fun onClose(handler: InventoryCloseEvent.() -> Unit) {
            onClose = handler
        }

        /**
         * Sets the default click handler. Defaults to null and does nothing.
         */
        fun defaultClickHandler(handler: InventoryClickEvent.() -> Unit) {
            defaultClickHandler = handler
        }

        /**
         * This constructs the InventoryMenu object, causing the actual inventory to be
         * built.
         */
        fun build() = InventoryMenu(owner, title, size, allowClose, onClose, slots)
    }

    companion object {
        inline fun build(size: Int, init: Builder.() -> Unit): InventoryMenu {
            return Builder(size).apply(init).build()
        }
    }
}
