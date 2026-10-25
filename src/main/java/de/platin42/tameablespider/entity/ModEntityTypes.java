package de.platin42.tameablespider.entity;

import de.platin42.tameablespider.TameableSpiderMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.NotNull;

public class ModEntityTypes {
    public static final EntityType<@NotNull TameableSpiderEntity> TAMEABLE_SPIDER = register(
            EntityType.Builder.<TameableSpiderEntity>of(TameableSpiderEntity::new, MobCategory.MONSTER)
                    .sized(0.75f, 1.75f)
    );

    private static <T extends LivingEntity> EntityType<@NotNull T> register(EntityType.Builder<@NotNull T> builder) {
        ResourceKey<@NotNull EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, 
                Identifier.fromNamespaceAndPath(TameableSpiderMod.MOD_ID, "tameable_spider"));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void registerModEntityTypes() {
        TameableSpiderMod.LOGGER.info("Registering EntityTypes for " + TameableSpiderMod.MOD_ID);
    }

    public static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(TAMEABLE_SPIDER, TameableSpiderEntity.createAttributes());
    }
    
}
