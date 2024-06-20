package cn.zbx1425.resourcepackupdater.mixin;

import cn.zbx1425.resourcepackupdater.ResourcePackUpdater;
import cn.zbx1425.resourcepackupdater.drm.AssetEncryption;
import cn.zbx1425.resourcepackupdater.drm.ServerLockRegistry;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(PathPackResources.class)
public abstract class FolderPackResourcesMixin extends AbstractPackResources {

    @Unique
    private Path canonicalRoot;

    @Unique
    private Path getCanonicalRoot() {
        if (canonicalRoot == null) {
            try {
                canonicalRoot = root.toRealPath();
            } catch (IOException e) {
                canonicalRoot = root;
            }
        }
        return canonicalRoot;
    }

    private FolderPackResourcesMixin(String string, boolean bl) { super(string, bl); }

    @Shadow @Final private Path root;

    @Inject(method = "getResource(Lnet/minecraft/server/packs/PackType;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/server/packs/resources/IoSupplier;", at = @At("HEAD"), cancellable = true)
    void getResource(PackType packType, ResourceLocation location, CallbackInfoReturnable<IoSupplier<InputStream>> cir) throws IOException {
        if (getCanonicalRoot().toFile().equals(ResourcePackUpdater.CONFIG.packBaseDirFile.value)) {
            Path path = this.root.resolve(packType.getDirectory()).resolve(location.getNamespace());
            var decomposeResult = FileUtil.decomposePath(location.getPath()).get();
            if (decomposeResult.left().isEmpty()) {
                cir.setReturnValue(null);
                cir.cancel();
                return;
            }
            Path path2 = FileUtil.resolvePath(path, decomposeResult.left().get());
            if (ServerLockRegistry.shouldRefuseProvidingFile(path2.toString())) {
                cir.setReturnValue(null);
                cir.cancel();
                return;
            }
            if (Files.exists(path2)) {
                cir.setReturnValue(() -> AssetEncryption.wrapInputStream(new FileInputStream(path2.toFile())));
                cir.cancel();
            } else {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
#if MC_VERSION >= "11900"
    void getResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput, CallbackInfo ci) {
#else
    void getResources(PackType type, String namespace, String path, int maxDepth, Predicate<ResourceLocation> filter, CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
#endif
        if (getCanonicalRoot().equals(ResourcePackUpdater.CONFIG.packBaseDirFile.value)) {
            if (ServerLockRegistry.shouldRefuseProvidingFile(null)) {
                ci.cancel();
            }
        }
    }
    @Inject(method = "getNamespaces", at = @At("HEAD"), cancellable = true)
    void getNamespaces(PackType type, CallbackInfoReturnable<Set<String>> cir) {
        if (getCanonicalRoot().equals(ResourcePackUpdater.CONFIG.packBaseDirFile.value)) {
            if (ServerLockRegistry.shouldRefuseProvidingFile(null)) {
                cir.setReturnValue(Collections.emptySet()); cir.cancel();
            }
        }
    }
}
