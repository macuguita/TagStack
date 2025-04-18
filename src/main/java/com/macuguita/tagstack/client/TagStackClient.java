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
import com.macuguita.tagstack.mixin.accessor.ItemAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TagStackClient implements ClientModInitializer {

    private static final Map<Item, Integer> VANILLA_STACK_SIZES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            Registries.ITEM.forEach(item -> VANILLA_STACK_SIZES.put(item, item.getMaxCount()));
        });

        ClientPlayNetworking.registerGlobalReceiver(
                TagStack.STACK_CHANGER_PACKET,
                (client, handler, buf, responseSender) -> {
                    int count = buf.readVarInt();
                    Map<Identifier, Integer> stackSizes = new HashMap<>(count);
                    for (int i = 0; i < count; i++) {
                        stackSizes.put(buf.readIdentifier(), buf.readVarInt());
                    }

                    client.execute(() -> {
                        for (Map.Entry<Item, Integer> e : VANILLA_STACK_SIZES.entrySet()) {
                            ((ItemAccessor) e.getKey()).tagStack$setMaxCount(e.getValue());
                        }

                        for (Map.Entry<Identifier, Integer> entry : stackSizes.entrySet()) {
                            Item item = Registries.ITEM.get(entry.getKey());
                            if (item != null) {
                                ((ItemAccessor) item).tagStack$setMaxCount(entry.getValue());
                            }
                        }
                    });
                }
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            for (Map.Entry<Item, Integer> e : VANILLA_STACK_SIZES.entrySet()) {
                ((ItemAccessor) e.getKey()).tagStack$setMaxCount(e.getValue());
            }
        });
    }
}
