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

package com.macuguita.tagstack.mixin.buckets;

import com.macuguita.tagstack.utils.EmptyBucketDispenserBehavior;
import com.macuguita.tagstack.utils.FilledBucketDispenserBehavior;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {

    @Inject(method = "registerBehavior", at = @At("HEAD"), cancellable = true)
    private static void daisy$dispensersWorksWithBigBucketStackSize(ItemConvertible provider, DispenserBehavior behavior, CallbackInfo ci) {
        Item item = provider.asItem();

        if (!(behavior instanceof FilledBucketDispenserBehavior) && !(behavior instanceof EmptyBucketDispenserBehavior)) {
            if (item == Items.BUCKET) {
                DispenserBlock.registerBehavior(provider, new EmptyBucketDispenserBehavior());
                ci.cancel();
            } else if (item == Items.WATER_BUCKET || item == Items.LAVA_BUCKET || item == Items.POWDER_SNOW_BUCKET) {
                DispenserBlock.registerBehavior(provider, new FilledBucketDispenserBehavior());
                ci.cancel();
            }
        }
    }
}

