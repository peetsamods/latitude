package com.example.globe.util;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.repository.ServerPacksSource;
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
 *
 * <p><b>Everything else here follows the same pattern:</b> the compressed-NBT reader gained a
 * mandatory accountant mid-range and its writer moved from a {@code File} to a {@code Path} at the
 * same point, the built-in datapack source gained a mandatory directory validator, and widgets
 * gained a public height setter. Each is resolved once, in its own holder
 * class so that resolution happens on first use rather than when this class loads — a dedicated
 * server touches only the saved-data pair, and the two client holders are stripped there.</p>
 */
public final class McCompat {

    /**
     * Intermediary names. These are the stable Fabric identifiers for the adapted members; they are
     * identical on every supported version, which is what lets a single lookup table cover the
     * range. The named forms are kept as a last resort so the helper also works in a development
     * runtime that never applied the remap.
     */
    private static final String DIMENSION_DATA_STORAGE_OWNER = "net.minecraft.class_26";
    private static final String ABSTRACT_WIDGET_OWNER = "net.minecraft.class_339";
    private static final String MINECRAFT_OWNER = "net.minecraft.class_310";
    private static final String NBT_IO_OWNER = "net.minecraft.class_2507";
    private static final String NBT_ACCOUNTER_OWNER = "net.minecraft.class_2505";
    private static final String NBT_ACCOUNTER_DESCRIPTOR = "Lnet/minecraft/class_2505;";
    private static final String COMPOUND_TAG_DESCRIPTOR = "Lnet/minecraft/class_2487;";
    private static final String DIRECTORY_VALIDATOR_DESCRIPTOR = "Lnet/minecraft/class_8580;";
    private static final String SET_HEIGHT_INTERMEDIARY = "method_53533";
    private static final String WIDGET_HEIGHT_FIELD_INTERMEDIARY = "field_22759";
    private static final String DIRECTORY_VALIDATOR_INTERMEDIARY = "method_52702";
    private static final String READ_COMPRESSED_INTERMEDIARY = "method_30613";
    private static final String WRITE_COMPRESSED_INTERMEDIARY = "method_30614";
    private static final String UNLIMITED_HEAP_INTERMEDIARY = "method_53898";
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
        Set<String> computeNames = methodNames(DIMENSION_DATA_STORAGE_OWNER,
                COMPUTE_IF_ABSENT_INTERMEDIARY, "computeIfAbsent",
                "(Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/lang/String;)"
                        + SAVED_DATA_DESCRIPTOR,
                "(" + SAVED_DATA_FACTORY_DESCRIPTOR + "Ljava/lang/String;)" + SAVED_DATA_DESCRIPTOR);
        Set<String> getNames = methodNames(DIMENSION_DATA_STORAGE_OWNER, GET_INTERMEDIARY, "get",
                "(Ljava/util/function/Function;Ljava/lang/String;)" + SAVED_DATA_DESCRIPTOR,
                "(" + SAVED_DATA_FACTORY_DESCRIPTOR + "Ljava/lang/String;)" + SAVED_DATA_DESCRIPTOR);

        Method looseCompute = findMethod(DimensionDataStorage.class, computeNames,
                parameters -> parameters.length == 3
                && parameters[0] == Function.class
                && parameters[1] == Supplier.class
                && parameters[2] == String.class);
        Method looseGet = findMethod(DimensionDataStorage.class, getNames,
                parameters -> parameters.length == 2
                && parameters[0] == Function.class
                && parameters[1] == String.class);
        Method factoryCompute = findMethod(DimensionDataStorage.class, computeNames,
                parameters -> parameters.length == 2
                && parameters[0] != Function.class
                && parameters[1] == String.class);
        Method factoryGet = findMethod(DimensionDataStorage.class, getNames,
                parameters -> parameters.length == 2
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
     * Reads a gzipped NBT file. The oldest two supported lines take a {@code File} and account for
     * nothing; from the third on the same entry point takes a {@code Path} plus an
     * {@code NbtAccounter}, and the unlimited-heap accountant that reproduces the older behaviour
     * only exists there. Both shapes read the same bytes into the same tag.
     */
    public static CompoundTag readCompressedNbt(Path path) throws IOException {
        try {
            if (CompressedNbt.ACCOUNTED) {
                return (CompoundTag) CompressedNbt.READ.invoke(path, CompressedNbt.unlimitedHeap());
            }
            return (CompoundTag) CompressedNbt.READ.invoke(path.toFile());
        } catch (IOException | RuntimeException | Error direct) {
            throw direct;
        } catch (Throwable failure) {
            throw new IllegalStateException("Latitude could not read the compressed NBT at " + path,
                    failure);
        }
    }

    /**
     * Writes a gzipped NBT file. The mirror of {@link #readCompressedNbt(Path)}: the oldest two
     * supported lines take a {@code File} and the newer two take a {@code Path}, under one
     * identifier. The bytes written are the same either way -- both shapes only open the file and
     * hand the stream to the same writer -- so this preserves each line's own file handling rather
     * than opening the stream here.
     */
    public static void writeCompressedNbt(CompoundTag tag, Path path) throws IOException {
        try {
            if (CompressedNbt.PATH_WRITER) {
                CompressedNbt.WRITE.invoke(tag, path);
            } else {
                CompressedNbt.WRITE.invoke(tag, path.toFile());
            }
        } catch (IOException | RuntimeException | Error direct) {
            throw direct;
        } catch (Throwable failure) {
            throw new IllegalStateException("Latitude could not write the compressed NBT at " + path,
                    failure);
        }
    }

    /**
     * Sets a widget's height. {@code AbstractWidget} publishes a height setter only from the second
     * supported line on; the protected field it assigns is declared, unchanged, on every one of
     * them, so the older line is served by writing that field directly — which is the whole body of
     * the newer setter.
     */
    @Environment(EnvType.CLIENT)
    public static void setWidgetHeight(AbstractWidget widget, int height) {
        if (widget == null) {
            return;
        }
        try {
            WidgetHeight.SETTER.invoke(widget, height);
        } catch (RuntimeException | Error direct) {
            throw direct;
        } catch (Throwable failure) {
            throw new IllegalStateException("Latitude could not resize a widget on this Minecraft "
                    + "version.", failure);
        }
    }

    /**
     * Moves a widget to an exact rectangle. {@code AbstractWidget.setRectangle} arrived on the
     * second-newest supported line; the four assignments it performs are available on all of them.
     */
    @Environment(EnvType.CLIENT)
    public static void setWidgetRectangle(AbstractWidget widget, int width, int height, int x, int y) {
        if (widget == null) {
            return;
        }
        widget.setWidth(width);
        setWidgetHeight(widget, height);
        widget.setX(x);
        widget.setY(y);
    }

    /**
     * Builds the vanilla built-in datapack source for a client-side world creation. The oldest
     * supported line takes no arguments; every later one takes the client's symlink-directory
     * validator, which the oldest line does not have at all.
     */
    @Environment(EnvType.CLIENT)
    public static ServerPacksSource newServerPacksSource(Minecraft client) {
        try {
            if (BuiltInPacks.VALIDATED) {
                return (ServerPacksSource) BuiltInPacks.CONSTRUCTOR
                        .invoke(BuiltInPacks.DIRECTORY_VALIDATOR.invoke(client));
            }
            return (ServerPacksSource) BuiltInPacks.CONSTRUCTOR.invoke();
        } catch (RuntimeException | Error direct) {
            throw direct;
        } catch (Throwable failure) {
            throw new IllegalStateException("Latitude could not open the built-in datapack source "
                    + "on this Minecraft version.", failure);
        }
    }

    /** Resolved on first use so a dedicated server never touches the client-only entry points. */
    @Environment(EnvType.CLIENT)
    private static final class WidgetHeight {
        private static final MethodHandle SETTER;

        static {
            Method setter = findMethod(AbstractWidget.class,
                    methodNames(ABSTRACT_WIDGET_OWNER, SET_HEIGHT_INTERMEDIARY, "setHeight", "(I)V"),
                    parameters -> parameters.length == 1 && parameters[0] == int.class);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                if (setter != null) {
                    SETTER = lookup.unreflect(setter);
                } else {
                    SETTER = lookup.unreflectSetter(heightField());
                }
            } catch (IllegalStateException failure) {
                throw failure;
            } catch (Throwable failure) {
                throw new IllegalStateException(
                        "Latitude could not adapt widget resizing on this Minecraft version.",
                        failure);
            }
        }

        private static Field heightField() throws NoSuchFieldException {
            Set<String> names = new LinkedHashSet<>();
            MappingResolver resolver = mappingResolver();
            if (resolver != null) {
                names.add(resolver.mapFieldName("intermediary", ABSTRACT_WIDGET_OWNER,
                        WIDGET_HEIGHT_FIELD_INTERMEDIARY, "I"));
            }
            names.add(WIDGET_HEIGHT_FIELD_INTERMEDIARY);
            names.add("height");
            for (String name : names) {
                try {
                    Field field = AbstractWidget.class.getDeclaredField(name);
                    if (field.getType() == int.class) {
                        field.setAccessible(true);
                        return field;
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try the next spelling.
                }
            }
            throw new IllegalStateException("This Minecraft version publishes neither a widget "
                    + "height setter nor the field it assigns; Latitude cannot lay out its screens.");
        }

        private WidgetHeight() {
        }
    }

    /** Resolved on first use, for the same reason as {@link WidgetHeight}. */
    @Environment(EnvType.CLIENT)
    private static final class BuiltInPacks {
        private static final boolean VALIDATED;
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle DIRECTORY_VALIDATOR;

        static {
            Constructor<?> plain = null;
            Constructor<?> validated = null;
            for (Constructor<?> candidate : ServerPacksSource.class.getConstructors()) {
                if (candidate.getParameterCount() == 0) {
                    plain = candidate;
                } else if (candidate.getParameterCount() == 1) {
                    validated = candidate;
                }
            }
            if (plain == null && validated == null) {
                throw new IllegalStateException("This Minecraft version publishes no usable "
                        + "built-in datapack source; Latitude cannot open its create-world screen.");
            }
            boolean needsValidator = plain == null;
            Method validator = needsValidator
                    ? findMethod(Minecraft.class, methodNames(MINECRAFT_OWNER,
                            DIRECTORY_VALIDATOR_INTERMEDIARY, "directoryValidator",
                            "()" + DIRECTORY_VALIDATOR_DESCRIPTOR),
                            parameters -> parameters.length == 0)
                    : null;
            if (needsValidator && validator == null) {
                throw new IllegalStateException("This Minecraft version requires a directory "
                        + "validator for the built-in datapack source but publishes none; Latitude "
                        + "cannot open its create-world screen.");
            }
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                VALIDATED = needsValidator;
                CONSTRUCTOR = lookup.unreflectConstructor(needsValidator ? validated : plain);
                DIRECTORY_VALIDATOR = needsValidator ? lookup.unreflect(validator) : null;
            } catch (Throwable failure) {
                throw new IllegalStateException("Latitude could not adapt the built-in datapack "
                        + "source of this Minecraft version.", failure);
            }
        }

        private BuiltInPacks() {
        }
    }

    /** Resolved on first use; common to both sides, unlike the two holders above. */
    private static final class CompressedNbt {
        private static final boolean ACCOUNTED;
        private static final MethodHandle READ;
        private static final MethodHandle UNLIMITED_HEAP;
        private static final boolean PATH_WRITER;
        private static final MethodHandle WRITE;

        static {
            Set<String> names = methodNames(NBT_IO_OWNER, READ_COMPRESSED_INTERMEDIARY,
                    "readCompressed",
                    "(Ljava/io/File;)" + COMPOUND_TAG_DESCRIPTOR,
                    "(Ljava/nio/file/Path;" + NBT_ACCOUNTER_DESCRIPTOR + ")" + COMPOUND_TAG_DESCRIPTOR);
            Method fromFile = findMethod(NbtIo.class, names,
                    parameters -> parameters.length == 1 && parameters[0] == java.io.File.class);
            Method fromPath = findMethod(NbtIo.class, names,
                    parameters -> parameters.length == 2 && parameters[0] == Path.class);
            if (fromFile == null && fromPath == null) {
                throw new IllegalStateException("This Minecraft version exposes neither supported "
                        + "compressed-NBT reader; Latitude cannot read a save's state file.");
            }
            boolean accounted = fromFile == null;
            Method accounter = accounted
                    ? findMethod(NbtAccounter.class, methodNames(NBT_ACCOUNTER_OWNER,
                            UNLIMITED_HEAP_INTERMEDIARY, "unlimitedHeap",
                            "()" + NBT_ACCOUNTER_DESCRIPTOR),
                            parameters -> parameters.length == 0)
                    : null;
            if (accounted && accounter == null) {
                throw new IllegalStateException("This Minecraft version accounts compressed-NBT "
                        + "reads but publishes no unlimited-heap accountant; Latitude cannot read a "
                        + "save's state file.");
            }
            Set<String> writeNames = methodNames(NBT_IO_OWNER, WRITE_COMPRESSED_INTERMEDIARY,
                    "writeCompressed",
                    "(" + COMPOUND_TAG_DESCRIPTOR + "Ljava/io/File;)V",
                    "(" + COMPOUND_TAG_DESCRIPTOR + "Ljava/nio/file/Path;)V");
            Method toFile = findMethod(NbtIo.class, writeNames,
                    parameters -> parameters.length == 2 && parameters[1] == java.io.File.class);
            Method toPath = findMethod(NbtIo.class, writeNames,
                    parameters -> parameters.length == 2 && parameters[1] == Path.class);
            if (toFile == null && toPath == null) {
                throw new IllegalStateException("This Minecraft version exposes neither supported "
                        + "compressed-NBT writer; Latitude cannot write a save's state file.");
            }

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            try {
                ACCOUNTED = accounted;
                READ = lookup.unreflect(accounted ? fromPath : fromFile);
                UNLIMITED_HEAP = accounted ? lookup.unreflect(accounter) : null;
                PATH_WRITER = toFile == null;
                WRITE = lookup.unreflect(toFile == null ? toPath : toFile);
            } catch (Throwable failure) {
                throw new IllegalStateException("Latitude could not adapt the compressed-NBT reader "
                        + "of this Minecraft version.", failure);
            }
        }

        private static Object unlimitedHeap() throws Throwable {
            return UNLIMITED_HEAP.invoke();
        }

        private CompressedNbt() {
        }
    }

    /**
     * Every name the given member can carry at runtime: what the loader's mappings resolve each
     * candidate descriptor to, the intermediary identifier itself (already correct in a remapped
     * runtime), and the readable name (correct in an unremapped development runtime).
     */
    private static Set<String> methodNames(String owner, String intermediaryName,
                                           String readableName, String... intermediaryDescriptors) {
        Set<String> names = new LinkedHashSet<>();
        MappingResolver resolver = mappingResolver();
        if (resolver != null) {
            for (String descriptor : intermediaryDescriptors) {
                names.add(resolver.mapMethodName("intermediary", owner, intermediaryName, descriptor));
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

    private static Method findMethod(Class<?> owner, Set<String> names, Predicate<Class<?>[]> shape) {
        for (Method candidate : owner.getMethods()) {
            if (names.contains(candidate.getName()) && shape.test(candidate.getParameterTypes())) {
                return candidate;
            }
        }
        return null;
    }
}
