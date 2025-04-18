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

import com.macuguita.tagstack.client.payloads.IdentifierListPayload;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TagStackServer implements DedicatedServerModInitializer {

    private static final Map<Item, Integer> VANILLA_STACK_SIZES = new HashMap<>();
    private static MinecraftServer SERVER = null;

    static {
        Registries.ITEM.forEach(item -> VANILLA_STACK_SIZES.put(item, item.getMaxCount()));
    }

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((server -> {
            SERVER = server;
        }));

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            Object2IntOpenHashMap<Identifier> identifierIntegerMap = new Object2IntOpenHashMap<>();
            for (Item item: Registries.ITEM) {
                if (item.getMaxCount() != VANILLA_STACK_SIZES.get(item)) {
                    identifierIntegerMap.put(Registries.ITEM.getId(item), item.getMaxCount());
                }
            }
            if (!identifierIntegerMap.isEmpty()) {
                for (ServerPlayerEntity player: PlayerLookup.all(SERVER)) {
                    ServerPlayNetworking.send(player, new IdentifierListPayload(identifierIntegerMap));
                    TagStack.LOGGER.info(String.format("Sent packet containing: %s", identifierIntegerMap.toString()));
                }
                identifierIntegerMap.clear();
            }
        });
    }

    public static Map<Item, Integer> getVanillaStackSizes() {
        return VANILLA_STACK_SIZES;
    }
}
