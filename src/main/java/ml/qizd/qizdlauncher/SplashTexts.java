package ml.qizd.qizdlauncher;

import java.util.Random;

public class SplashTexts {
    public static String[] texts = new String[] {
            "Now with less cannibalism!",
            "Built by QiZD",
            "pasha.png is now downloading...",
            "Save dolphins from alopecia. Donate now!"
    };

    public static String getRandom() {
        return texts[new Random().nextInt(texts.length)];
    }
}
