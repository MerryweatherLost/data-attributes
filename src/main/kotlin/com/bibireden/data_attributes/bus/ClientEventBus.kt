package com.bibireden.data_attributes.bus

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.client.Keybindings
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus

@EventBusSubscriber(modid = DataAttributes.ID, bus = Bus.MOD, value = [Dist.CLIENT])
class ClientEventBus {
    @SubscribeEvent
    fun registerKeys(event: RegisterKeyMappingsEvent) {
        event.register(Keybindings.CONFIG_SCREEN)
    }
}