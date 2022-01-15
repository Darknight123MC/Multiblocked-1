package io.github.cleanroommc.multiblocked;

import io.github.cleanroommc.multiblocked.api.block.BlockComponent;
import io.github.cleanroommc.multiblocked.api.block.ItemComponent;
import io.github.cleanroommc.multiblocked.api.capability.IO;
import io.github.cleanroommc.multiblocked.api.definition.ControllerDefinition;
import io.github.cleanroommc.multiblocked.api.pattern.BlockPattern;
import io.github.cleanroommc.multiblocked.api.pattern.FactoryBlockPattern;
import io.github.cleanroommc.multiblocked.api.recipe.ItemsIngredient;
import io.github.cleanroommc.multiblocked.api.recipe.RecipeMap;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockCapabilities;
import io.github.cleanroommc.multiblocked.api.registry.MultiblockComponents;
import io.github.cleanroommc.multiblocked.client.renderer.impl.IModelRenderer;
import io.github.cleanroommc.multiblocked.client.renderer.impl.OBJRenderer;
import io.github.cleanroommc.multiblocked.events.Listeners;
import io.github.cleanroommc.multiblocked.network.MultiblockedNetworking;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Function;

import static io.github.cleanroommc.multiblocked.api.pattern.Predicates.air;
import static io.github.cleanroommc.multiblocked.api.pattern.Predicates.blocks;

@Mod.EventBusSubscriber(modid = Multiblocked.MODID)
public class CommonProxy {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(Listeners.class);
        MultiblockedNetworking.initializeC2S();
        MultiblockedNetworking.initializeS2C();
        MultiblockCapabilities.registerCapabilities();
        ControllerDefinition definition = new ControllerDefinition(new ResourceLocation("multiblocked:test_block"), new RecipeMap("test_recipe_map"));
        definition.recipeMap.start()
                .inputItems(new ItemsIngredient(2, new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.IRON_INGOT)))
                .outputItems(new ItemStack(Items.APPLE, 10))
                .duration(60)
                .buildAndRegister();
        definition.basePattern = FactoryBlockPattern.start()
                .aisle("XXX")
                .aisle("A#A")
                .aisle("XYX")
                .where('X', blocks(Blocks.STONE))
                .where('#', air())
                .where('A', blocks(Blocks.CHEST))
                .where('Y', definition.selfPredicate())
                .build();

//        definition.formedRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"block/emitter"));
        definition.formedRenderer = new OBJRenderer(new ResourceLocation(Multiblocked.MODID,"models/obj/energy_core_model.obj"));
        definition.baseRenderer = new IModelRenderer(new ResourceLocation(Multiblocked.MODID,"test_model"));
        definition.isOpaqueCube = false;
        MultiblockComponents.registerComponent(definition);
    }

    public void init() {

    }

    public void postInit() {

    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        MultiblockComponents.COMPONENT_BLOCKS_REGISTRY.values().forEach(registry::register);
        MultiblockComponents.registerTileEntity();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        for (BlockComponent block : MultiblockComponents.COMPONENT_BLOCKS_REGISTRY
                .values()) {
            registry.register(createItemBlock(block, ItemComponent::new));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(block.getRegistryName());
        return itemBlock;
    }

}

