package de.platin42.tameablespider.client;

import de.platin42.tameablespider.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.SpiderRenderer;

public class TameableSpiderClient implements ClientModInitializer {
	@Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntityTypes.TAMEABLE_SPIDER, (context) ->
                new SpiderRenderer<>(context, ModelLayers.SPIDER)
        );
    }
}