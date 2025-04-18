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
import com.macuguita.tagstack.client.payloads.ItemListPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagStackClient implements ClientModInitializer {

    private static final Map<Item, Integer> VANILLA_STACK_SIZES = new HashMap<>();
    private static final List<Item> ITEM_LIST = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            Registries.ITEM.forEach(item -> VANILLA_STACK_SIZES.put(item, item.getMaxCount()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ItemListPayload.ID, ((payload, context) -> {
            context.client().execute(() -> {
                payload.items().forEach(identifier -> {
                    ITEM_LIST.add(Registries.ITEM.get(identifier));
                    TagStack.LOGGER.info(String.format("added %s to the list", identifier));
                });
            });
        }));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ITEM_LIST.forEach(listItem -> {
                DefaultItemComponentEvents.MODIFY.register(context -> {
                    context.modify(
                            item -> item.equals(listItem),
                            (builder, item) -> {
                                builder.add(DataComponentTypes.MAX_STACK_SIZE, VANILLA_STACK_SIZES.get(listItem));
                                TagStack.LOGGER.info(String.format("set %s to %d stack size", item, VANILLA_STACK_SIZES.get(item)));
                            });
                });
            });
            ITEM_LIST.clear();
        });
    }

    public static Map<Item, Integer> getVanillaStackSizes() {
        return VANILLA_STACK_SIZES;
    }
}
