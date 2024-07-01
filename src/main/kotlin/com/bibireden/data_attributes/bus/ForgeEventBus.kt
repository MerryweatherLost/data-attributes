package com.bibireden.data_attributes.bus

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.client.Keybindings
import com.bibireden.data_attributes.client.screens.DataAttributesScreen
import com.bibireden.data_attributes.config.Config
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus

@EventBusSubscriber(modid = DataAttributes.ID, bus = Bus.FORGE, value = [Dist.DEDICATED_SERVER])
class ForgeEventBus {
    @SubscribeEvent
    fun clientTick(event: TickEvent.ClientTickEvent) {
        if (Keybindings.CONFIG_SCREEN.isDown) {
            Keybindings.CONFIG_SCREEN.consumeClick()
//            Config.create().generateScreen(DataAttributesScreen())
        }
    }
}