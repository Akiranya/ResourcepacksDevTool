package dev.lone.resourcepackdevtool;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;

public class ResourcepackDevTool implements ModInitializer {
    @Override
    public void onInitialize() {
        if (Configuration.inst().hasPackCached()) {
            MinecraftClient.getInstance()
                .getServerResourcePackProvider()
                .loadServerPack(Configuration.inst().getLastPack(), ResourcePackSource.SERVER)
                .thenRun(ResourcepackDevTool::showLoadServerPackFromCacheToast);
        }
    }

    public static void showLoadServerPackFromCacheToast() {
        CustomToast.show(MinecraftClient.getInstance().getToastManager(), Text.literal("快速资源包"), Text.literal("从缓存中提前载入服务器资源包."));
    }
}