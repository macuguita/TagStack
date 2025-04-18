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

package com.macuguita.tagstack.client;

import com.macuguita.tagstack.TagStack;
import com.macuguita.tagstack.client.payloads.IdentifierListPayload;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagStackClient implements ClientModInitializer {

    private static final Map<Item, Integer> VANILLA_STACK_SIZES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            Registries.ITEM.forEach(item -> VANILLA_STACK_SIZES.put(item, item.getMaxCount()));
        });

        ClientPlayNetworking.registerGlobalReceiver(IdentifierListPayload.ID, (payload, context) -> {
            Object2IntOpenHashMap<Identifier> identifierIntegerMap = payload.identifierIntegerMap();
            context.client().execute(() -> {
                TagStack.LOGGER.info(String.format("Received packet containing: %s", identifierIntegerMap.toString()));
                DefaultItemComponentEvents.MODIFY.register(modifyContext -> {
                    modifyContext.modify(
                            item -> item.getMaxCount() != VANILLA_STACK_SIZES.get(item),
                            (builder, item) -> {
                                builder.add(DataComponentTypes.MAX_STACK_SIZE, VANILLA_STACK_SIZES.get(item));
                            });
                    for (var entry: identifierIntegerMap.entrySet()) {
                        Item modifiedItem = Registries.ITEM.get(entry.getKey());
                        int newStackSize = entry.getValue();
                        modifyContext.modify(
                                item -> item.equals(modifiedItem) && item.getMaxCount() != newStackSize,
                                (builder, item) -> {
                                    builder.add(DataComponentTypes.MAX_STACK_SIZE, newStackSize);
                                });
                    }
                });
                identifierIntegerMap.clear();
            });
        });
    }

    public static Map<Item, Integer> getVanillaStackSizes() {
        return VANILLA_STACK_SIZES;
    }
}
