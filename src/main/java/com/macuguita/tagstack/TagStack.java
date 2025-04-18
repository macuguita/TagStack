/*
 * Copyright (c) 2025 macuguita.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.macuguita.tagstack;

import com.macuguita.tagstack.client.TagStackClient;
import com.macuguita.tagstack.client.payloads.IdentifierListPayload;
import com.macuguita.tagstack.utils.TagStackTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TagStack implements ModInitializer {

	public static final String MOD_ID = "tag_stack";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier IDENTIFIER_LIST_PAYLOAD_ID = id("identifier_list");

	public static final Map<TagKey<Item>, Integer> STACK_SIZE_MAP = Map.of(
			TagStackTags.STACKABLE_TO_1, 1,
			TagStackTags.STACKABLE_TO_2, 2,
			TagStackTags.STACKABLE_TO_4, 4,
			TagStackTags.STACKABLE_TO_8, 8,
			TagStackTags.STACKABLE_TO_16, 16,
			TagStackTags.STACKABLE_TO_32, 32,
			TagStackTags.STACKABLE_TO_64, 64
	);

	//TODO: idk if it might be posible but, when server reloads tags, it sends a packet telling the client to update the stack size of the items, might have to rescue the custom item list payload in previous commits...
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(IdentifierListPayload.ID, IdentifierListPayload.CODEC);

		DefaultItemComponentEvents.MODIFY.register(context -> {
			CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
				if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
					context.modify(
							item -> item.getMaxCount() != TagStackClient.getVanillaStackSizes().get(item),
							(builder, item) -> {
								builder.add(DataComponentTypes.MAX_STACK_SIZE, TagStackClient.getVanillaStackSizes().get(item));
							});
				}
				if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
					ServerLifecycleEvents.SERVER_STARTING.register((server -> {
						context.modify(
								item -> item.getMaxCount() != TagStackServer.getVanillaStackSizes().get(item),
								(builder, item) -> {
									builder.add(DataComponentTypes.MAX_STACK_SIZE, TagStackServer.getVanillaStackSizes().get(item));
								});
					}));
				}
				for (var entry : STACK_SIZE_MAP.entrySet()) {
					TagKey<Item> tag = entry.getKey();
					int newSize = entry.getValue();
					context.modify(
							item -> registries.get(RegistryKeys.ITEM).getEntry(item).isIn(tag),
							(builder, item) -> {
								builder.add(DataComponentTypes.MAX_STACK_SIZE, newSize);
							}
					);
				}
			});
		});
	}


	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	public static ItemStack handleStackableBucket(ItemStack stack, PlayerEntity player, ItemStack emptyContainer) {
		if (player == null || player.getAbilities().creativeMode) {
			return stack;
		}
		if (stack.getCount() == 1) {
			stack.decrement(1);
			return emptyContainer;
		}
		stack.decrement(1);
		if (!player.getInventory().insertStack(emptyContainer)) {
			player.dropItem(emptyContainer, false);
		}
		return stack;
	}
}
