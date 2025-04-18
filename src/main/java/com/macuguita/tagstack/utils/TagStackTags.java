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

package com.macuguita.tagstack.utils;

import com.macuguita.tagstack.TagStack;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TagStackTags {

    public static final TagKey<Item> STACKABLE_TO_1 = registerTagKey("stackable_to_1");
    public static final TagKey<Item> STACKABLE_TO_2 = registerTagKey("stackable_to_2");
    public static final TagKey<Item> STACKABLE_TO_4 = registerTagKey("stackable_to_4");
    public static final TagKey<Item> STACKABLE_TO_8 = registerTagKey("stackable_to_8");
    public static final TagKey<Item> STACKABLE_TO_16 = registerTagKey("stackable_to_16");
    public static final TagKey<Item> STACKABLE_TO_32 = registerTagKey("stackable_to_32");
    public static final TagKey<Item> STACKABLE_TO_64 = registerTagKey("stackable_to_64");

    public static TagKey<Item> registerTagKey(String name) {
        return TagKey.of(RegistryKeys.ITEM, TagStack.id(name));
    }
}
