package com.bibireden.data_attributes.client

import com.bibireden.data_attributes.DataAttributes
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object Keybindings {
    @JvmField
    val CONFIG_SCREEN = KeyMapping("key.${DataAttributes.ID}.config_screen", GLFW.GLFW_KEY_KP_0, "category.data_attributes")
}