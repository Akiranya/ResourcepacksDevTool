package dev.lone.resourcepackdevtool.mixin;

import dev.lone.resourcepackdevtool.Configuration;
import dev.lone.resourcepackdevtool.ResourcepackDevTool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.ServerResourcePackProvider;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(ServerResourcePackProvider.class) public abstract class MixinDownloadingPackFinder {
    @Shadow @Final private ReentrantLock lock;

    @Shadow @Nullable private CompletableFuture<?> downloadTask;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Nullable private ResourcePackProfile serverContainer;

    @Shadow @Final private static Text SERVER_NAME_TEXT;

    /**
     * @author LoneDev
     * @reason Do not remove server resource pack from memory on leave for faster re-join
     */
    @Overwrite public CompletableFuture<?> clear() {
        this.lock.lock();

        try {
            if (this.downloadTask != null) this.downloadTask.cancel(true);

            this.downloadTask = null;
        } finally {
            this.lock.unlock();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * @author LoneDev
     * @reason Do not remove server resource pack from memory on leave for faster re-join
     */
    @Overwrite public CompletableFuture<Void> loadServerPack(File packZip, ResourcePackSource packSource) {
        ResourcePackProfile.PackFactory packFactory = (name) -> new ZipResourcePack(name, packZip, false);
        ResourcePackProfile.Metadata metadata = ResourcePackProfile.loadMetadata("server", packFactory);
        if (metadata == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid pack metadata at " + packZip));
        } else {
            LOGGER.info("Applying server pack {}", packZip);

            if (metadata.description().getString().contains("mewcraft") ||
                metadata.description().getString().contains("上古时代")
            ) {
                // We are playing MewCraft!

                ResourcePackProfile newServerPack = ResourcePackProfile.of(
                    "server",
                    Text.translatable("resourcePack.server.name"),
                    true,
                    name -> new ZipResourcePack(name, packZip, false),
                    metadata,
                    ResourceType.CLIENT_RESOURCES,
                    ResourcePackProfile.InsertionPosition.TOP,
                    false,
                    packSource
                );
                if (this.serverContainer == null || !isPackCached(packZip)) {
                    this.serverContainer = newServerPack;
                    Configuration.inst().cacheServerPack(packZip);
                    return MinecraftClient.getInstance().reloadResourcesConcurrently();
                } else {
                    ResourcepackDevTool.showLoadServerPackFromCacheToast();
                    return CompletableFuture.completedFuture(null);
                }
            } else {
                // We are playing a random server :(

                this.serverContainer = ResourcePackProfile.of("server", SERVER_NAME_TEXT, true, packFactory, metadata, ResourceType.CLIENT_RESOURCES, ResourcePackProfile.InsertionPosition.TOP, true, packSource);
                return MinecraftClient.getInstance().reloadResourcesConcurrently();
            }
        }
    }

    private static boolean isPackCached(File packZip) {
        File lastPack = Configuration.inst().getLastPack();
        if (lastPack == null) return false;
        return packZip.getName().equals(lastPack.getName());
    }
}