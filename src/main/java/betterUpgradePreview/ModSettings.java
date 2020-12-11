package betterUpgradePreview;

import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.io.IOException;

public class ModSettings {
    public static SpireConfig config;
    public static String removeColor = "FF6563";
    public static String addColor = "7FFF00";

    public static boolean colorMatches(Color color, String savedColor) {
        String asStr = color.toString().substring(0, 6);
        return asStr.equals(savedColor);
    }

    public static void saveSettings() {
        try {
            // And based on that boolean, set the settings and save them
            config = new SpireConfig("BetterUpgradePreview", "betterUpgradePreviewSettings");
            config.load();
            config.setString("removeColor", removeColor);
            config.setString("addColor", addColor);
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSettings() {
        try {
            config = new SpireConfig("BetterUpgradePreview", "betterUpgradePreviewSettings");
            config.load();
            removeColor = config.getString("removeColor");
            addColor = config.getString("addColor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
