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
        CustomToast.show(MinecraftClient.getInstance().getToastManager(),
            Text.literal("MewCraft 上古时代服务器 ❤"),
            Text.literal("已从您的游戏内存中快速读取资源包"),
            2000);
    }
}