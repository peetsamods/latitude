package com.example.globe.world;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SpawnCellShapePolicyTest {
    private SpawnCellShapePolicyTest() {
    }

    public static void run() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        check(Blocks.AIR.defaultBlockState(), true, "air");
        check(Blocks.DANDELION.defaultBlockState(), true, "harmless flower");
        check(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1), true, "one snow layer");
        check(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2), false, "two snow layers have collision");
        check(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 8), false, "full snow block");
        check(Blocks.STONE.defaultBlockState(), false, "solid stone");
        check(Blocks.OAK_LEAVES.defaultBlockState(), false, "leaves obstruct the cell");
        check(Blocks.WATER.defaultBlockState(), false, "water");
        check(Blocks.SWEET_BERRY_BUSH.defaultBlockState(), false, "harmful non-collision plant");
        check(Blocks.POWDER_SNOW.defaultBlockState(), false, "powder snow");
        check(Blocks.FIRE.defaultBlockState(), false, "fire");
        require(!SpawnSafetyPolicy.canOccupySpawnCell(true, true, null), "unknown registry identity must fail closed");

        String source = Files.readString(Path.of("src/main/java/com/example/globe/GlobeMod.java"));
        String column = methodSection(source, "private static BlockPos placeSafeY(");
        require(column.contains("!isOpenSpawnCell(world, spawn)")
                        && column.contains("!isOpenSpawnCell(world, spawn.above())"),
                "production must validate both the feet and head cells");
        String cell = methodSection(source, "private static boolean isOpenSpawnCell(").replaceAll("\\s+", " ");
        require(cell.contains("SpawnSafetyPolicy.canOccupySpawnCell( state.getCollisionShape(world, pos).isEmpty(), state.getFluidState().isEmpty(), id == null ? null : id.toString())"),
                "production must pass the actual collision, fluid and block identity to the tested policy");
        System.out.println("SPAWN_CELL_SHAPE_POLICY_TEST_PASS");
    }

    private static void check(BlockState state, boolean expected, String label) {
        var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        boolean actual = SpawnSafetyPolicy.canOccupySpawnCell(
                state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty(),
                state.getFluidState().isEmpty(), id == null ? null : id.toString());
        require(actual == expected, label + ": expected " + expected + " but was " + actual);
    }

    private static String methodSection(String source, String signature) {
        int start = source.indexOf(signature);
        require(start >= 0, "missing method: " + signature);
        int open = source.indexOf('{', start);
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            if (source.charAt(i) == '{') depth++;
            if (source.charAt(i) == '}' && --depth == 0) return source.substring(open, i + 1);
        }
        throw new AssertionError("unclosed method: " + signature);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
