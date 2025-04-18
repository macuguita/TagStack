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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class EmptyBucketDispenserBehavior extends ItemDispenserBehavior {

    private final ItemDispenserBehavior fallback = new ItemDispenserBehavior();

    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        BlockPos targetPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        BlockState state = world.getBlockState(targetPos);
        Block block = state.getBlock();
        ItemStack filled = ItemStack.EMPTY;

        if (block instanceof FluidDrainable drainable) {
            filled = drainable.tryDrainFluid(world, targetPos, state);
        } else if (state.isOf(Blocks.LAVA_CAULDRON)) {
            filled = new ItemStack(Items.LAVA_BUCKET);
            world.setBlockState(targetPos, Blocks.CAULDRON.getDefaultState());
            world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else if (state.isOf(Blocks.POWDER_SNOW_CAULDRON) && state.get(PowderSnowCauldronBlock.LEVEL) == 3) {
            filled = new ItemStack(Items.POWDER_SNOW_BUCKET);
            world.setBlockState(targetPos, Blocks.CAULDRON.getDefaultState());
            world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else if (state.isOf(Blocks.WATER_CAULDRON) && state.get(LeveledCauldronBlock.LEVEL) == 3) {
            filled = new ItemStack(Items.WATER_BUCKET);
            world.setBlockState(targetPos, Blocks.CAULDRON.getDefaultState());
            world.playSound(null, targetPos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        if (!filled.isEmpty()) {
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, targetPos);
            stack.decrement(1);

            if (stack.isEmpty()) return filled;
            if (!insertIntoInventory(pointer, filled)) {
                fallback.dispense(pointer, filled);
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