// Copied with modifications from Botania Tweaks, which is licensed under MPL
// Original code can be obtained here: https://github.com/quat1024/BotaniaTweaks

package vazkii.botania.api.recipe;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.common.core.helper.ItemNBTHelper;

public class RecipeTerrestrialAgglomeration {
    public final ImmutableList<ItemStack> recipeStacks;
    public final ImmutableList<String> recipeOreKeys;
    public final ItemStack recipeOutput;
    public final int manaCost;
    public final int color1;
    public final int color2;
    public final IBlockState multiblockCenter;
    public final IBlockState multiblockEdge;
    public final IBlockState multiblockCorner;
    @Nullable
    public final IBlockState multiblockCenterReplace;
    @Nullable
    public final IBlockState multiblockEdgeReplace;
    @Nullable
    public final IBlockState multiblockCornerReplace;

    final int totalInputs;

    private void verifyInputs(ImmutableList<Object> inputs) {
        if (inputs.isEmpty())
            throw new IllegalArgumentException("Can't make empty agglomeration recipe");

        for (Object o : inputs) {
            if (o instanceof ItemStack || o instanceof String)
                continue;
            throw new IllegalArgumentException("illegal recipe input " + o);
        }
    }

    public RecipeTerrestrialAgglomeration(ImmutableList<Object> recipeInputs, ItemStack recipeOutput, int manaCost,
            int color1, int color2, IBlockState multiblockCenter, IBlockState multiblockEdge,
            IBlockState multiblockCorner, @Nullable IBlockState multiblockCenterReplace,
            @Nullable IBlockState multiblockEdgeReplace, @Nullable IBlockState multiblockCornerReplace) {
        verifyInputs(recipeInputs);

        ImmutableList.Builder<ItemStack> stackInputBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<String> keyInputBuilder = new ImmutableList.Builder<>();

        for (Object o : recipeInputs) {
            if (o instanceof ItemStack)
                stackInputBuilder.add((ItemStack) o);
            else
                keyInputBuilder.add((String) o);
        }

        this.recipeStacks = stackInputBuilder.build();
        this.recipeOreKeys = keyInputBuilder.build();
        this.totalInputs = recipeStacks.size() + recipeOreKeys.size();

        this.recipeOutput = recipeOutput;
        this.manaCost = manaCost;
        this.color1 = color1;
        this.color2 = color2;

        this.multiblockCenter = multiblockCenter;
        this.multiblockEdge = multiblockEdge;
        this.multiblockCorner = multiblockCorner;

        this.multiblockCenterReplace = multiblockCenterReplace;
        this.multiblockEdgeReplace = multiblockEdgeReplace;
        this.multiblockCornerReplace = multiblockCornerReplace;
    }

    public boolean itemsMatch(List<ItemStack> userInputs) {
        // Early-exit if the input count is wrong anyways
        if (userInputs.size() == 0 || userInputs.size() != totalInputs)
            return false;

        int usedRecipeStackCount = 0;
        int usedOreKeyCount = 0;
        boolean[] usedUserInputs = new boolean[userInputs.size()];

        // ensure all recipe stacks are satisfied
        for (ItemStack recipeStack : recipeStacks) {
            for (int i = 0; i < userInputs.size(); i++) {
                if (usedUserInputs[i])
                    continue; // already matched against a recipe item, don't consume again

                ItemStack userInputStack = userInputs.get(i);
                if (compareStacks(recipeStack, userInputStack) && recipeStack.getCount() == userInputStack.getCount()) {
                    usedRecipeStackCount++;
                    usedUserInputs[i] = true;
                    break;
                }
            }
        }

        // user did not supply all of the required item stacks
        if (usedRecipeStackCount != recipeStacks.size())
            return false;

        // ensure all recipe ore dictionary keys are satisfied
        for (String key : recipeOreKeys) {
            List<ItemStack> matchingOres = OreDictionary.getOres(key);
            for (ItemStack oreStack : matchingOres) {
                for (int i = 0; i < userInputs.size(); i++) {
                    if (usedUserInputs[i])
                        continue;

                    ItemStack userInputStack = userInputs.get(i);
                    if (compareStacks(oreStack, userInputStack) && userInputStack.getCount() == 1) {
                        usedOreKeyCount++;
                        usedUserInputs[i] = true;
                        break;
                    }
                }
            }
        }

        return usedOreKeyCount == recipeOreKeys.size();
    }

    public ImmutableList<ItemStack> getRecipeStacks() {
        return recipeStacks;
    }

    public ImmutableList<String> getRecipeOreKeys() {
        return recipeOreKeys;
    }

    public int getManaCost() {
        return manaCost;
    }

    public ItemStack getRecipeOutputCopy() {
        return recipeOutput.copy();
    }

    private static boolean compareStacks(ItemStack recipe, ItemStack supplied) {
        return recipe.getItem() == supplied.getItem() && recipe.getItemDamage() == supplied.getItemDamage()
                && ItemNBTHelper.matchTag(recipe.getTagCompound(), supplied.getTagCompound());
    }
}
