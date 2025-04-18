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

import net.minecraft.block.*;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FilledBucketDispenserBehavior extends ItemDispenserBehavior {

    private final ItemDispenserBehavior fallback = new ItemDispenserBehavior();

    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof FluidModificationItem fluidItem) {
            BlockPos targetPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
            World world = pointer.getWorld();

            boolean pumped = false;

            if (fluidItem.placeFluid(null, world, targetPos, null)) {
                fluidItem.onEmptied(null, world, stack, targetPos);
                pumped = true;
            } else {
                BlockState state = world.getBlockState(targetPos);
                if (state.isOf(Blocks.CAULDRON)) {
                    if (item == Items.WATER_BUCKET) {
                        world.setBlockState(targetPos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
                        world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        pumped = true;
                    } else if (item == Items.LAVA_BUCKET) {
                        world.setBlockState(targetPos, Blocks.LAVA_CAULDRON.getDefaultState());
                        world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        pumped = true;
                    } else if (item == Items.POWDER_SNOW_BUCKET) {
                        world.setBlockState(targetPos, Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(PowderSnowCauldronBlock.LEVEL, 3));
                        world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        pumped = true;
                    }
                }
            }

            if (pumped) {
                stack.decrement(1);
                ItemStack bucket = new ItemStack(Items.BUCKET);
                if (stack.isEmpty()) return bucket;

                if (!insertIntoInventory(pointer, bucket)) {
                    fallback.dispense(pointer, bucket);
                }
            }

            return stack;
        }

        return fallback.dispense(pointer, stack);
    }

    private boolean insertIntoInventory(BlockPointer pointer, ItemStack stack) {
        BlockEntity entity = pointer.getBlockEntity();
        if (entity instanceof Inventory inventory && addToFirstFreeSlot(inventory, stack) >= 0) return true;

        entity = pointer.getWorld().getBlockEntity(pointer.getPos().down());
        return entity instanceof Inventory inventory && addToFirstFreeSlot(inventory, stack) >= 0;
    }

    private int addToFirstFreeSlot(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);
            if (current == stack && current.getCount() + stack.getCount() <= current.getMaxCount()) {
                current.increment(stack.getCount());
                return i;
            }
            if (current.isEmpty()) {
                inventory.setStack(i, stack);
                return i;
            }
        }
        return -1;
    }
}