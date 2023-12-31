package com.github.clevernucleus.dataattributes_dc.mixin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.dataattributes_dc.mutable.MutableSimpleRegistry;
import com.mojang.serialization.Lifecycle;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

@Mixin(SimpleRegistry.class)
abstract class SimpleRegistryMixin<T> implements MutableSimpleRegistry<T> {

    // Unique annotation is used for fields that are not present in the original
    // class
    @Unique
    private Collection<Identifier> data_idCache;

    // Final annotation indicates that the field is not meant to be changed
    @Final
    @Shadow
    private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;

    @Final
    @Shadow
    private Object2IntMap<T> entryToRawId;

    @Final
    @Shadow
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Final
    @Shadow
    private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;

    @Final
    @Shadow
    private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Final
    @Shadow
    private Map<T, Lifecycle> entryToLifecycle;

    @Shadow
    private Lifecycle lifecycle;

    @Shadow
    private List<RegistryEntry.Reference<T>> cachedEntries;

    @Shadow
    private int nextId;

    // Inject annotation is used for injecting code into existing methods
    @Inject(method = "<init>", at = @At("TAIL"))
    private void data_init(CallbackInfo ci) {
        this.data_idCache = new HashSet<Identifier>();
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "assertNotFrozen", at = @At("HEAD"), cancellable = true)
    private void data_assertNotFrozen(CallbackInfo ci) {
        // Cancel annotation is used to cancel the execution of the method
        if ((SimpleRegistry<T>) (Object) this == Registries.ATTRIBUTE) {
            ci.cancel();
        }
    }

    @SuppressWarnings("deprecation")
    private <V extends T> void remove(RegistryKey<T> key, Lifecycle lifecycle) {
        Validate.notNull(key);
        RegistryEntry.Reference<T> reference = this.keyToEntry.get(key);
        T value = reference.value();
        final int rawId = this.entryToRawId.getInt(value);

        this.rawIdToEntry.remove(rawId);
        this.valueToEntry.remove(value);
        this.idToEntry.remove(key.getValue());
        this.keyToEntry.remove(key);
        this.nextId--;
        this.lifecycle = this.lifecycle.add(lifecycle);
        this.entryToLifecycle.remove(value);
        this.cachedEntries = null;
        this.entryToRawId.remove(value);

        for (T t : this.entryToRawId.keySet()) {
            int i = this.entryToRawId.get(t);

            if (i > rawId) {
                this.entryToRawId.replace(t, i - 1);
            }
        }
    }

    @Override
    public void removeCachedIds(Registry<T> registry) {
        if (this.data_idCache == null)
            return;
        for (Iterator<Identifier> iterator = this.data_idCache.iterator(); iterator.hasNext();) {
            Identifier id = iterator.next();

            this.remove(RegistryKey.of(registry.getKey(), id), Lifecycle.stable());
            iterator.remove();
        }
    }

    @Override
    public void cacheId(Identifier id) {
        this.data_idCache.add(id);
    }
}