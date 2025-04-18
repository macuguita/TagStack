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

import com.macuguita.tagstack.mixin.accessor.ItemAccessor;
import com.macuguita.tagstack.utils.TagStackTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class TagStack implements ModInitializer {

	public static final String MOD_ID = "tag_stack";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier STACK_CHANGER_PACKET = id("stack_changer_packet");

	private static final Map<TagKey<Item>, Integer> STACK_SIZE_MAP = Map.of(
			TagStackTags.STACKABLE_TO_1, 1,
			TagStackTags.STACKABLE_TO_2, 2,
			TagStackTags.STACKABLE_TO_4, 4,
			TagStackTags.STACKABLE_TO_8, 8,
			TagStackTags.STACKABLE_TO_16, 16,
			TagStackTags.STACKABLE_TO_32, 32,
			TagStackTags.STACKABLE_TO_64, 64
	);

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for (Item item : Registries.ITEM) {
				for (Map.Entry<TagKey<Item>, Integer> entry : STACK_SIZE_MAP.entrySet()) {
					if (item.getRegistryEntry().isIn(entry.getKey())) {
						((ItemAccessor) item).tagStack$setMaxCount(entry.getValue());
						break;
					}
				}
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			PacketByteBuf buf = PacketByteBufs.create();
			Map<Identifier, Integer> stackSizes = new HashMap<>();

			for (Item item : Registries.ITEM) {
				for (Map.Entry<TagKey<Item>, Integer> entry : STACK_SIZE_MAP.entrySet()) {
					if (item.getRegistryEntry().isIn(entry.getKey())) {
						stackSizes.put(Registries.ITEM.getId(item), entry.getValue());
						break;
					}
				}
			}

			buf.writeVarInt(stackSizes.size());
			for (Map.Entry<Identifier, Integer> e : stackSizes.entrySet()) {
				buf.writeIdentifier(e.getKey());
				buf.writeVarInt(e.getValue());
			}

			ServerPlayNetworking.send(player, STACK_CHANGER_PACKET, buf);
		});
	}

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
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
