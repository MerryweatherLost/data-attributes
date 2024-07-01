package com.bibireden.data_attributes

import com.bibireden.data_attributes.bus.ClientEventBus
import com.bibireden.data_attributes.bus.ForgeEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(DataAttributes.ID)
object DataAttributes {
    const val ID = "data_attributes"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
//        ForgeRegistries.ATTRIBUTES.forEach { attribute ->
//            if (attribute is RangedAttribute) {}
//        }
//        MOD_BUS.register(ClientEventBus())
        FORGE_BUS.register(ForgeEventBus())
        runForDist(clientTarget = { MOD_BUS.addListener(::onClientSetup) }, serverTarget = { MOD_BUS.addListener(::onServerSetup) })
        LOGGER.info("Data Attributes has initialized!")
    }

    /**
     * This is used for initializing client specific
     * things such as renderers and keymaps
     * Fired on the mod specific event bus.
     */
    private fun onClientSetup(event: FMLClientSetupEvent) {
    }

    /**
     * Fired on the global Forge bus.
     */
    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
    }
}