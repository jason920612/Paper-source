package net.minecraft.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
// CraftBukkit end

public class ShapelessRecipes implements RecipeCrafting {

    private final MinecraftKey key;
    private final String group;
    private final ItemStack result;
    private final NonNullList<RecipeItemStack> ingredients;

    public ShapelessRecipes(MinecraftKey minecraftkey, String s, ItemStack itemstack, NonNullList<RecipeItemStack> nonnulllist) {
        this.key = minecraftkey;
        this.group = s;
        this.result = itemstack;
        this.ingredients = nonnulllist;
    }

    // CraftBukkit start
    @SuppressWarnings("unchecked")
    public org.bukkit.inventory.ShapelessRecipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapelessRecipe recipe = new CraftShapelessRecipe(result, this);
        recipe.setGroup(this.group);

        for (RecipeItemStack list : this.ingredients) {
            recipe.addIngredient(CraftRecipe.toBukkit(list));
        }
        return recipe;
    }
    // CraftBukkit end

    @Override
    public MinecraftKey getKey() {
        return this.key;
    }

    @Override
    public RecipeSerializer<?> getRecipeSerializer() {
        return RecipeSerializer.b;
    }

    @Override
    public ItemStack getResult() {
        return this.result;
    }

    @Override
    public NonNullList<RecipeItemStack> a() {
        return this.ingredients;
    }

    public boolean a(InventoryCrafting inventorycrafting, World world) {
        AutoRecipeStackManager autorecipestackmanager = new AutoRecipeStackManager();
        int i = 0;

        // Paper start
        java.util.List<ItemStack> providedItems = new java.util.ArrayList<>();
        co.aikar.util.Counter<ItemStack> matchedProvided = new co.aikar.util.Counter<>();
        co.aikar.util.Counter<RecipeItemStack> matchedIngredients = new co.aikar.util.Counter<>();
        // Paper end
        for (int j = 0; j < inventorycrafting.getSize(); ++j) {
            ItemStack itemstack = inventorycrafting.getItem(j);

            if (!itemstack.isEmpty()) {
                // Paper start
                itemstack = itemstack.cloneItemStack();
                providedItems.add(itemstack);
                for (RecipeItemStack ingredient : ingredients) {
                    if (ingredient.test(itemstack)) {
                        matchedProvided.increment(itemstack);
                        matchedIngredients.increment(ingredient);
                    }
                }
                // Paper end
            }
        }

        // Paper start
        java.util.List<RecipeItemStack> ingredients = new java.util.ArrayList<>(this.ingredients);
        providedItems.sort(java.util.Comparator.comparingInt((ItemStack c) -> (int) matchedProvided.getCount(c)).reversed());
        ingredients.sort(java.util.Comparator.comparingInt((RecipeItemStack c) -> (int) matchedIngredients.getCount(c)));

        PROVIDED:
        for (ItemStack provided : providedItems) {
            for (Iterator<RecipeItemStack> itIngredient = ingredients.iterator(); itIngredient.hasNext(); ) {
                RecipeItemStack ingredient = itIngredient.next();
                if (ingredient.test(provided)) {
                    itIngredient.remove();
                    continue PROVIDED;
                }
            }
            return false;
        }
        return ingredients.isEmpty();
        // Paper end
    }

    public ItemStack a(InventoryCrafting inventorycrafting) {
        return this.result.cloneItemStack();
    }

    public static class a implements RecipeSerializer<ShapelessRecipes> {

        public a() {}

        @Override
        public ShapelessRecipes a(MinecraftKey minecraftkey, JsonObject jsonobject) {
            String s = ChatDeserializer.a(jsonobject, "group", "");
            NonNullList<RecipeItemStack> nonnulllist = a(ChatDeserializer.u(jsonobject, "ingredients"));

            if (nonnulllist.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (nonnulllist.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            } else {
                ItemStack itemstack = ShapedRecipes.a(ChatDeserializer.t(jsonobject, "result"));

                return new ShapelessRecipes(minecraftkey, s, itemstack, nonnulllist);
            }
        }

        private static NonNullList<RecipeItemStack> a(JsonArray jsonarray) {
            NonNullList<RecipeItemStack> nonnulllist = NonNullList.a();

            for (int i = 0; i < jsonarray.size(); ++i) {
                RecipeItemStack recipeitemstack = RecipeItemStack.a(jsonarray.get(i));

                if (!recipeitemstack.d()) {
                    nonnulllist.add(recipeitemstack);
                }
            }

            return nonnulllist;
        }

        @Override
        public ShapelessRecipes a(MinecraftKey minecraftkey, PacketDataSerializer packetdataserializer) {
            String s = packetdataserializer.e(32767);
            int i = packetdataserializer.i();
            NonNullList<RecipeItemStack> nonnulllist = NonNullList.a(i, RecipeItemStack.a);

            for (int j = 0; j < nonnulllist.size(); ++j) {
                nonnulllist.set(j, RecipeItemStack.b(packetdataserializer));
            }

            ItemStack itemstack = packetdataserializer.m();

            return new ShapelessRecipes(minecraftkey, s, itemstack, nonnulllist);
        }

        public void a(PacketDataSerializer packetdataserializer, ShapelessRecipes shapelessrecipes) {
            packetdataserializer.a(shapelessrecipes.group);
            packetdataserializer.d(shapelessrecipes.ingredients.size());
            Iterator iterator = shapelessrecipes.ingredients.iterator();

            while (iterator.hasNext()) {
                RecipeItemStack recipeitemstack = (RecipeItemStack) iterator.next();

                recipeitemstack.a(packetdataserializer);
            }

            packetdataserializer.a(shapelessrecipes.result);
        }
    }
}
