package cn.zbx1425.resourcepackupdater.gui.forms;

import cn.zbx1425.resourcepackupdater.ResourcePackUpdater;
import cn.zbx1425.resourcepackupdater.gui.gl.GlHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;

public class ProgressForm implements GlScreenForm {

    private String primaryInfo = "";
    private String auxilaryInfo = "";
    private float primaryProgress;
    private String secondaryProgress;

    private static final float progressFormWidth = 600, progressFormHeight = 250;

    @Override
    public void render() {
        GlHelper.setMatCenterForm(progressFormWidth, progressFormHeight, 0.75f);
        GlHelper.begin(GlHelper.PRELOAD_FONT_TEXTURE);
        GlScreenForm.drawShadowRect(progressFormWidth, progressFormHeight, 0xffdee6ea);

        float barBegin = 0;
        float usableBarWidth = progressFormWidth - barBegin - 0;
        GlHelper.blit(barBegin, 0, usableBarWidth, 30, 0x4435aa8e);
        GlHelper.blit(barBegin, 0, usableBarWidth * primaryProgress, 30, 0xff35aa8e);
        GlHelper.drawString(barBegin + usableBarWidth * primaryProgress, 0 + 10, 80, LINE_HEIGHT, 16,
                String.format("%d%%", Math.round(primaryProgress * 100)), 0xff35aa8e, false, true);

        GlHelper.drawString(20, 45, progressFormWidth - 40, 50, 20,
                primaryInfo, 0xff222222, false, false);

        /*
        GlHelper.blit(barBegin, 110, usableBarWidth, 30, 0x4449baee);
        GlHelper.blit(barBegin, 110, usableBarWidth * secondaryProgress, 30, 0xff49baee);
        GlHelper.drawString(barBegin + usableBarWidth * secondaryProgress, 110 + 10, 80, LINE_HEIGHT, 16,
                String.format("%d%%", Math.round(secondaryProgress * 100)), 0xff49baee, false, true);
        */

        if (secondaryProgress.contains("\n")) {
            GlHelper.blit(0, 70, progressFormWidth, 185 - 70, 0x3399abab);
        }
        GlHelper.drawString(20, 80, progressFormWidth - 40, 180 - 80, 16,
                secondaryProgress, 0xff222222, false, true);

        boolean monospace = !auxilaryInfo.isEmpty() && auxilaryInfo.charAt(0)== ':';
        GlHelper.drawString(20, 195, progressFormWidth - 40, 30, 18,
                monospace ? auxilaryInfo.substring(1) : auxilaryInfo, 0xff222222, monospace, false);

        String escBtnHint = ResourcePackUpdater.CONFIG.sourceList.value.size() > 1 ? "Cancel / Use Another Source" : "Cancel";
        GlHelper.drawString(20, progressFormHeight - 30, progressFormWidth - 40, 16, 16, escBtnHint + ": Hold ESC", 0xff222222, false, true);

        GlHelper.end();
    }

    @Override
    public boolean shouldStopPausing() {
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_ESCAPE)) {
            throw new GlHelper.MinecraftStoppingException();
        }
        return true;
    }

    @Override
    public void reset() {
        primaryInfo = "";
        auxilaryInfo = "";
        primaryProgress = 0;
        secondaryProgress = "";
    }

    @Override
    public void printLog(String line) throws GlHelper.MinecraftStoppingException {
        primaryInfo = line;
    }

    @Override
    public void amendLastLog(String postfix) throws GlHelper.MinecraftStoppingException {

    }

    @Override
    public void setProgress(float primary, float secondary) throws GlHelper.MinecraftStoppingException {
        this.primaryProgress = primary;
    }

    @Override
    public void setInfo(String secondary, String textValue) throws GlHelper.MinecraftStoppingException {
        this.secondaryProgress = secondary;
        this.auxilaryInfo = textValue;
    }

    @Override
    public void setException(Exception exception) throws GlHelper.MinecraftStoppingException {

    }
}
