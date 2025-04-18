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
import com.macuguita.tagstack.client.payloads.ItemListPayload;
import com.macuguita.tagstack.utils.TagStackTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class TagStack implements ModInitializer {

	public static final String MOD_ID = "tag_stack";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier ITEM_LIST_PAYLOAD_ID = id("item_list");

	public static final Map<TagKey<Item>, Integer> STACK_SIZE_MAP = Map.of(
			TagStackTags.STACKABLE_TO_1, 1,
			TagStackTags.STACKABLE_TO_2, 2,
			TagStackTags.STACKABLE_TO_4, 4,
			TagStackTags.STACKABLE_TO_8, 8,
			TagStackTags.STACKABLE_TO_16, 16,
			TagStackTags.STACKABLE_TO_32, 32,
			TagStackTags.STACKABLE_TO_64, 64
	);

	private static final List<Identifier> MODIFIED_ITEMS = new ArrayList<>();

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(ItemListPayload.ID, ItemListPayload.CODEC);

		DefaultItemComponentEvents.MODIFY.register(context -> {
			CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
				for (var entry : STACK_SIZE_MAP.entrySet()) {
					TagKey<Item> tag = entry.getKey();
					int newSize = entry.getValue();
					context.modify(
							item -> registries.get(RegistryKeys.ITEM).getEntry(item).isIn(tag),
							(builder, item) -> {
								builder.add(DataComponentTypes.MAX_STACK_SIZE, newSize);
								MODIFIED_ITEMS.add(registries.get(RegistryKeys.ITEM).getId(item));
							}
					);
				}
			});
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
					for (Map.Entry<Item, Integer> entry : TagStackClient.getVanillaStackSizes().entrySet()) {
						Item item = entry.getKey();
						int originalSize = entry.getValue();
						context.modify(
								itemToModify -> itemToModify == item,
								(builder, itemToModify) -> builder.add(DataComponentTypes.MAX_STACK_SIZE, originalSize)
						);
					}
				});
			}
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
				ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
					ServerPlayNetworking.send(handler.getPlayer(), new ItemListPayload(MODIFIED_ITEMS));
					LOGGER.info(String.format("sent packet to %s", handler.getPlayer()));
				});
			}
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
