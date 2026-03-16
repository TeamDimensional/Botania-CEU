package vazkii.botania.api.recipe;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeElvenTrade {

	private final ImmutableList<ItemStack> outputs;
	private final ImmutableList<Object> inputs;

	public RecipeElvenTrade(ItemStack[] outputs, Object... inputs) {
		this.outputs = ImmutableList.copyOf(outputs);

		ImmutableList.Builder<Object> inputsToSet = ImmutableList.builder();
		for(Object obj : inputs) {
			if(obj instanceof String || obj instanceof ItemStack)
				inputsToSet.add(obj);
			else throw new IllegalArgumentException("Invalid input");
		}

		this.inputs = inputsToSet.build();
	}

	public List<ItemStack> getMatches(List<ItemStack> stacks) {
		List<Object> inputsMissing = new ArrayList<>(inputs);
		List<ItemStack> stacksToRemove = new ArrayList<>();
		List<List<ItemStack>> validStacks = new ArrayList<>();

		for (Object input : inputs) {
			if (input instanceof String)
				validStacks.add(OreDictionary.getOres((String) input));
			else
				validStacks.add(Collections.emptyList());
		}


		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) {
				continue;
			}
			if (inputsMissing.isEmpty())
				break;

			int stackIndex = -1, oredictIndex = -1;

			for (int j = 0; j < inputsMissing.size(); j++) {
				Object input = inputsMissing.get(j);
				if (input instanceof String) {
					for (int o = 0; o < validStacks.get(j + (validStacks.size() - inputsMissing.size())).size(); o++) {
						ItemStack oreStack = validStacks.get(j + (validStacks.size() - inputsMissing.size())).get(o);
						if (OreDictionary.itemMatches(oreStack, stack, false)) {
							oredictIndex = handleOredictMatch(stack, stacksToRemove, j);
							break;
						}
					}

				} else if (input instanceof ItemStack && simpleAreStacksEqual((ItemStack) input, stack)) {
					stackIndex = handleSimpleMatch(stack, stacksToRemove, j);
				}
			}

			if (stackIndex != -1)
				inputsMissing.remove(stackIndex);
			else if (oredictIndex != -1)
				inputsMissing.remove(oredictIndex);
		}

		return stacksToRemove;
	}

	private int handleSimpleMatch(ItemStack stack, List<ItemStack> stacksToRemove, int j) {
		int stackIndex = -1;
		ItemStack singular = stack.copy();
		singular.setCount(1);
		boolean exists = false;
		for (ItemStack stackIn : stacksToRemove) {
			if (simpleAreStacksEqual(stackIn, stack)) {
				if (stack.getCount() >= stackIn.getCount() + 1) {
					stackIn.setCount(stackIn.getCount() + 1);
					exists = true;
					break;
				}
				else
					return stackIndex;
			}
		}
		if (!exists)
			stacksToRemove.add(singular.copy());
		stackIndex = j;
		return stackIndex;
	}

	private int handleOredictMatch(ItemStack stack, List<ItemStack> stacksToRemove, int j) {
		int oredictIndex = -1;
		ItemStack singular = stack.copy();
		singular.setCount(1);
		boolean exists = false;
		for (ItemStack stackIn : stacksToRemove) {
			if (simpleAreStacksEqual(stackIn, stack)) {
				if (stack.getCount() >= stackIn.getCount() + 1) {
					stackIn.setCount(stackIn.getCount() + 1);
					exists = true;
					break;
				}
				else
					return oredictIndex;
			}
		}
		if (!exists)
			stacksToRemove.add(singular.copy());
		oredictIndex = j;
		return oredictIndex;
	}

	private boolean simpleAreStacksEqual(ItemStack stack, ItemStack stack2) {
		return stack.getItem() == stack2.getItem() && stack.getItemDamage() == stack2.getItemDamage();
	}

	public List<Object> getInputs() {
		return inputs;
	}

	public List<ItemStack> getOutputs() {
		return outputs;
	}

}
