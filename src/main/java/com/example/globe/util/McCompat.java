package com.example.globe.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Adapters for the handful of vanilla entry points whose shape changes inside the supported
 * Minecraft range, so one jar can serve the whole range.
 *
 * <p>This is release surface. It never launches a process, never reads the filesystem, and holds no
 * development-only behaviour; it resolves each adapted entry point once per JVM and then behaves
 * exactly like a direct call.</p>
 *
 * <p><b>Saved data.</b> {@code DimensionDataStorage} takes a loose
 * {@code (deserializer, constructor, id)} triple on the oldest supported line and a single
 * {@code SavedData.Factory} record on every later one. The mod compiles against the oldest line, so
 * the factory type does not exist at compile time and has to be built reflectively. Both shapes are
 * resolved through {@link MappingResolver} so the lookup survives the remap that ships with the jar
 * (maintainer ruling, 2026-09-07: one jar, method-handle adapters, fail loudly).</p>
 */
public final class McCompat {

    /**
     * Intermediary names. These are the stable Fabric identifiers for the adapted members; they are
     * identical on every supported version, which is what lets a single lookup table cover the
     * range. The named forms are kept as a last resort so the helper also works in a development
     * runtime that never applied the remap.
     */
    private static final String DIMENSION_DATA_STORAGE_OWNER = "net.minecraft.class_26";
    private static final String SAVED_DATA_DESCRIPTOR = "Lnet/minecraft/class_18;";
    private static final String SAVED_DATA_FACTORY_DESCRIPTOR = "Lnet/minecraft/class_18$class_8645;";
    private static final String COMPUTE_IF_ABSENT_INTERMEDIARY = "method_17924";
    private static final String GET_INTERMEDIARY = "method_20786";
    private static final String DATA_FIX_TYPES_OWNER = "net.minecraft.class_4284";
    private static final String DATA_FIX_TYPES_DESCRIPTOR = "Lnet/minecraft/class_4284;";
    private static final String SAVED_DATA_COMMAND_STORAGE_INTERMEDIARY = "field_45077";

    /** True when the storage takes the loose triple rather than a factory record. */
    private static final boolean LOOSE_SAVED_DATA_SHAPE;

    private static final MethodHandle COMPUTE_IF_ABSENT;
    private static final MethodHandle GET;

    /** Factory record constructor and its data-fix type; both null on the loose shape. */
    private static final MethodHandle SAVED_DATA_FACTORY;
    private static final Object SAVED_DATA_FIX_TYPE;

    static {
        Set<String> computeNames = methodNames(COMPUTE_IF_ABSENT_INTERMEDIARY, "computeIfAbsent",
                "(Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/lang/String;)"
                        + SAVED_DATA_DESCRIPTOR,
                "(" + SAVED_DATA_FACTORY_DESCRIPTOR + "Ljava/lang/String;)" + SAVED_DATA_DESCRIPTOR);
        Set<String> getNames = methodNames(GET_INTERMEDIARY, "get",
                "(Ljava/util/function/Function;Ljava/lang/String;)" + SAVED_DATA_DESCRIPTOR,
                "(" + SAVED_DATA_FACTORY_DESCRIPTOR + "Ljava/lang/String;)" + SAVED_DATA_DESCRIPTOR);

        Method looseCompute = findMethod(computeNames, parameters -> parameters.length == 3
                && parameters[0] == Function.class
                && parameters[1] == Supplier.class
                && parameters[2] == String.class);
        Method looseGet = findMethod(getNames, parameters -> parameters.length == 2
                && parameters[0] == Function.class
                && parameters[1] == String.class);
        Method factoryCompute = findMethod(computeNames, parameters -> parameters.length == 2
                && parameters[0] != Function.class
                && parameters[1] == String.class);
        Method factoryGet = findMethod(getNames, parameters -> parameters.length == 2
                && parameters[0] != Function.class
                && parameters[1] == String.class);

        if (looseCompute == null && factoryCompute == null
                || looseGet == null && factoryGet == null) {
            throw new IllegalStateException(
                    "This Minecraft version exposes neither supported saved-data storage shape on "
                            + DimensionDataStorage.class.getName()
                            + "; Latitude cannot load or create its world state.");
        }

        boolean loose = looseCompute != null && looseGet != null;
        Method compute = loose ? looseCompute : factoryCompute;
        Method read = loose ? looseGet : factoryGet;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle factoryConstructor = null;
        Object fixType = null;
        MethodHandle computeHandle;
        MethodHandle readHandle;
        try {
            computeHandle = lookup.unreflect(compute);
            readHandle = lookup.unreflect(read);
            if (!loose) {
                Class<?> factoryType = compute.getParameterTypes()[0];
                factoryConstructor = lookup.unreflectConstructor(factoryType.getConstructor(
                        Supplier.class, Function.class, DataFixTypes.class));
                fixType = savedDataFixType();
            }
        } catch (IllegalStateException failure) {
            throw failure;
        } catch (Throwable failure) {
            throw new IllegalStateException(
                    "Latitude could not adapt the saved-data storage of this Minecraft version.",
                    failure);
        }

        LOOSE_SAVED_DATA_SHAPE = loose;
        COMPUTE_IF_ABSENT = computeHandle;
        GET = readHandle;
        SAVED_DATA_FACTORY = factoryConstructor;
        SAVED_DATA_FIX_TYPE = fixType;
    }

    private McCompat() {
    }

    /**
     * Returns the stored saved data for {@code id}, creating and caching it when the world has none
     * yet. Mirrors the vanilla call of the same name on every supported version.
     */
    @SuppressWarnings("unchecked")
    public static <T extends SavedData> T computeIfAbsent(DimensionDataStorage storage,
                                                          Supplier<T> constructor,
                                                          Function<CompoundTag, T> deserializer,
                                                          String id) {
        try {
            if (LOOSE_SAVED_DATA_SHAPE) {
                return (T) (SavedData) COMPUTE_IF_ABSENT.invoke(storage, deserializer, constructor, id);
            }
            return (T) (SavedData) COMPUTE_IF_ABSENT.invoke(storage, factory(constructor, deserializer), id);
        } catch (RuntimeException | Error direct) {
            throw direct;
        } catch (Throwable failure) {
            throw new IllegalStateException("Latitude could not read or create saved data " + id, failure);
        }
    }

    /**
     * Returns the stored saved data for {@code id}, or {@code null} when the world has none. Never
     * creates one, so a world Latitude did not generate is left untouched.
     */
    @SuppressWarnings("unchecked")
    public static <T extends SavedData> T get(DimensionDataStorage storage,
                                              Supplier<T> constructor,
                                              Function<CompoundTag, T> deserializer,
                                              String id) {
        try {
            if (LOOSE_SAVED_DATA_SHAPE) {
                return (T) (SavedData) GET.invoke(storage, deserializer, id);
            }
            return (T) (SavedData) GET.invoke(storage, factory(constructor, deserializer), id);
        } catch (RuntimeException | Error direct) {
            throw direct;
        } catch (Throwable failure) {
            throw new IllegalStateException("Latitude could not read saved data " + id, failure);
        }
    }

    private static Object factory(Supplier<? extends SavedData> constructor,
                                  Function<CompoundTag, ? extends SavedData> deserializer) throws Throwable {
        return SAVED_DATA_FACTORY.invoke(constructor, deserializer, SAVED_DATA_FIX_TYPE);
    }

    /**
     * The data-fix type the factory shape requires. Picked by identifier rather than by field
     * reference because the oldest supported line does not declare this constant at all, so the
     * name cannot be written into the source that compiles against it.
     */
    private static DataFixTypes savedDataFixType() {
        Set<String> names = new LinkedHashSet<>();
        MappingResolver resolver = mappingResolver();
        if (resolver != null) {
            names.add(resolver.mapFieldName("intermediary", DATA_FIX_TYPES_OWNER,
                    SAVED_DATA_COMMAND_STORAGE_INTERMEDIARY, DATA_FIX_TYPES_DESCRIPTOR));
        }
        names.add(SAVED_DATA_COMMAND_STORAGE_INTERMEDIARY);
        names.add("SAVED_DATA_COMMAND_STORAGE");
        for (DataFixTypes type : DataFixTypes.values()) {
            if (names.contains(type.name())) {
                return type;
            }
        }
        throw new IllegalStateException(
                "This Minecraft version declares the factory saved-data shape but no matching "
                        + "data-fix type; Latitude cannot build the saved-data factory.");
    }

    /**
     * Every name the given member can carry at runtime: what the loader's mappings resolve each
     * candidate descriptor to, the intermediary identifier itself (already correct in a remapped
     * runtime), and the readable name (correct in an unremapped development runtime).
     */
    private static Set<String> methodNames(String intermediaryName, String readableName,
                                           String... intermediaryDescriptors) {
        Set<String> names = new LinkedHashSet<>();
        MappingResolver resolver = mappingResolver();
        if (resolver != null) {
            for (String descriptor : intermediaryDescriptors) {
                names.add(resolver.mapMethodName("intermediary", DIMENSION_DATA_STORAGE_OWNER,
                        intermediaryName, descriptor));
            }
        }
        names.add(intermediaryName);
        names.add(readableName);
        return names;
    }

    private static MappingResolver mappingResolver() {
        try {
            return FabricLoader.getInstance().getMappingResolver();
        } catch (Throwable unavailable) {
            // Reachable when the helper is exercised outside a loader runtime; the literal
            // identifiers alone are enough there.
            return null;
        }
    }

    private static Method findMethod(Set<String> names, Predicate<Class<?>[]> shape) {
        for (Method candidate : DimensionDataStorage.class.getMethods()) {
            if (names.contains(candidate.getName()) && shape.test(candidate.getParameterTypes())) {
                return candidate;
            }
        }
        return null;
    }
}
