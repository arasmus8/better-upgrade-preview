package betterUpgradePreview;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.io.IOException;

public class ModSettings {
    public static SpireConfig config;
    public static Color removeColor = new Color(0xFF6563FF);
    public static Color addColor = new Color(0x7FFF00FF);

    public static boolean colorMatches(Color color, Color savedColor) {
        return savedColor.equals(color);
    }

    public static void saveSettings() {
        try {
            // And based on that boolean, set the settings and save them
            config = new SpireConfig("BetterUpgradePreview", "betterUpgradePreviewSettings");
            config.load();
            config.setString("removeColor", removeColor.toString());
            config.setString("addColor", addColor.toString());
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSettings() {
        try {
            config = new SpireConfig("BetterUpgradePreview", "betterUpgradePreviewSettings");
            config.load();
            String removeColorStr = config.getString("removeColor");
            if (removeColorStr != null && removeColorStr.length() > 0) {
                removeColor = Color.valueOf(removeColorStr);
            }
            String addColorStr = config.getString("addColor");
            if (addColorStr != null && addColorStr.length() > 0) {
                addColor = Color.valueOf(addColorStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
