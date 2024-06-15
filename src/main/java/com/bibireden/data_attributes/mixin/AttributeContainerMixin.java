package com.bibireden.data_attributes.mixin;

import java.util.*;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents;
import com.bibireden.data_attributes.mutable.MutableAttributeContainer;
import com.bibireden.data_attributes.mutable.MutableAttributeInstance;
import com.google.common.collect.Multimap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

@Mixin(AttributeContainer.class)
abstract class AttributeContainerMixin implements MutableAttributeContainer {

	@Unique
	private final Map<Identifier, EntityAttributeInstance> data_custom = new HashMap<>();

	@Unique
	private final Map<Identifier, EntityAttributeInstance> data_tracked = new HashMap<>();

	@Unique
	private LivingEntity data_livingEntity;

	@Final
	@Shadow
	private DefaultAttributeContainer fallback;

	@Shadow
	private void updateTrackedStatus(EntityAttributeInstance instance) {}

	@Inject(method = "updateTrackedStatus", at = @At("HEAD"), cancellable = true)
	private void data_attributes$updateTrackedStatus(EntityAttributeInstance instance, CallbackInfo ci) {
		Identifier identifier = ((MutableAttributeInstance) instance).getId();
		if (identifier != null) {
			this.data_tracked.put(identifier, instance);
		}
		ci.cancel();
	}

	@ModifyReturnValue(method = "getTracked", at = @At("RETURN"))
	private Set<EntityAttributeInstance> data_attributes$getTracked(Set<EntityAttributeInstance> original) {
		return new HashSet<>(this.data_tracked.values());
	}

	@ModifyReceiver(method = "getAttributesToSend", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Map<?, ?> data_attributes$getAttributesToSend(Map<?, ?> instance) { return this.data_custom; }

	@ModifyReturnValue(method = "getCustomInstance(Lnet/minecraft/entity/attribute/EntityAttribute;)Lnet/minecraft/entity/attribute/EntityAttributeInstance;", at = @At("RETURN"))
	private EntityAttributeInstance data_attributes$getCustomInstance(EntityAttributeInstance instance) {
		Identifier identifier = Registries.ATTRIBUTE.getId(instance.getAttribute());

		if (identifier != null) {
			EntityAttributeInstance entityAttributeInstance = this.data_custom
					.computeIfAbsent(identifier, id -> this.fallback.createOverride(this::updateTrackedStatus, instance.getAttribute()));

			if (entityAttributeInstance != null) {
				MutableAttributeInstance mutable = (MutableAttributeInstance) entityAttributeInstance;
				mutable.setContainerCallback((AttributeContainer) (Object) this);

				if (mutable.getId() == null) {
					mutable.updateId(identifier);
				}
			}

			return entityAttributeInstance;
		}

		return null;
	}

	@ModifyExpressionValue(
		method = "hasAttribute(Lnet/minecraft/entity/attribute/EntityAttribute;)Z",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/DefaultAttributeContainer;has(Lnet/minecraft/entity/attribute/EntityAttribute;)Z")
	)
	private boolean data_attributes$hasAttribute(boolean original, EntityAttribute attribute) {
		return this.data_custom.get(Registries.ATTRIBUTE.getId(attribute)) != null || original;
	}

	@ModifyExpressionValue(method = "hasModifierForAttribute(Lnet/minecraft/entity/attribute/EntityAttribute;Ljava/util/UUID;)Z", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_hasModifierForAttribute(Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getValue(Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getBaseValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getBaseValue(Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getModifierValue(Lnet/minecraft/entity/attribute/EntityAttribute;Ljava/util/UUID;)D", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_getModifierValue(Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	// Injection to remove custom modifiers
	@Inject(method = "removeModifiers", at = @At("HEAD"), cancellable = true)
	private void data_removeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers,
			CallbackInfo ci) {
		attributeModifiers.asMap().forEach((attribute, collection) -> {
			Identifier identifier = Registries.ATTRIBUTE.getId(attribute);
			EntityAttributeInstance entityAttributeInstance = this.data_custom.get(identifier);

			if (entityAttributeInstance != null) {
				collection.forEach(entityAttributeInstance::removeModifier);
			}
		});

		ci.cancel();
	}

	// Injection to set custom attributes from another container
	@SuppressWarnings("all") // todo: temp until intellij update
	@Inject(method = "setFrom", at = @At("HEAD"), cancellable = true)
	private void data_setFrom(AttributeContainer other, CallbackInfo ci) {
		AttributeContainer container = (AttributeContainer) (Object) this;

		((MutableAttributeContainer) other).data_attributes$custom().values().forEach(attributeInstance -> {
			EntityAttribute entityAttribute = attributeInstance.getAttribute();
			EntityAttributeInstance entityAttributeInstance = container.getCustomInstance(entityAttribute);

			if (entityAttributeInstance != null) {
				entityAttributeInstance.setFrom(attributeInstance);
				final double value = entityAttributeInstance.getValue();
				EntityAttributeModifiedEvents.MODIFIED.invoker().onModified(entityAttribute, this.data_livingEntity, null, value, false);
			}
		});

		ci.cancel();
	}

	// Redirecting to use custom attributes for serialization
	@ModifyExpressionValue(method = "toNbt", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_toNbt(Collection<?> original) {
		return this.data_custom.values();
	}

	@Override
	public Map<Identifier, EntityAttributeInstance> data_attributes$custom() {
		return this.data_custom;
	}

	@Override
	public LivingEntity data_attributes$getLivingEntity() {
		return this.data_livingEntity;
	}

	@Override
	public void data_attributes$setLivingEntity(final LivingEntity livingEntity) {
		this.data_livingEntity = livingEntity;
	}

	@Override
	public void data_attributes$refresh() {
		for (EntityAttributeInstance instance : this.data_custom.values()) {
			((MutableAttributeInstance) instance).refresh();
		}
	}

	@Override
	public void data_attributes$clearTracked() {
		this.data_tracked.clear();
	}
}
