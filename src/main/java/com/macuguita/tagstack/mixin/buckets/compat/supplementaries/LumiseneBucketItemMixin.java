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

package com.macuguita.tagstack.mixin.buckets.compat.supplementaries;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.macuguita.tagstack.TagStack;
import net.mehvahdjukaar.supplementaries.common.items.fabric.LumiseneBucketItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(LumiseneBucketItem.class)
public class LumiseneBucketItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mehvahdjukaar/supplementaries/common/items/fabric/LumiseneBucketItem;getEmptiedStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack daisy$LumiseneBucketsWorksWithBigStackSize(ItemStack stack, PlayerEntity player, Operation<ItemStack> original) {
        return TagStack.handleStackableBucket(stack, player, new ItemStack(Items.BUCKET));
    }
}
