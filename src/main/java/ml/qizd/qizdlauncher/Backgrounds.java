package ml.qizd.qizdlauncher;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Random;

public class Backgrounds {
    public static class Background {
        public Image image;
        public Color color;

        public Background(Image image, Color color) {
            this.image = image;
            this.color = color;
        }
    }

    public static Background[] backgrounds = new Background[] {
            new Background(new Image(Backgrounds.class.getResourceAsStream("images/backgrounds/sunset.jpg")), Color.valueOf("#8c6ed2")),
            new Background(new Image(Backgrounds.class.getResourceAsStream("images/backgrounds/mountains.jpg")), Color.valueOf("#b17D70")),
            new Background(new Image(Backgrounds.class.getResourceAsStream("images/backgrounds/bliss.png")), Color.valueOf("#598fe9"))
    };

    public static Background getRandom() {
        return backgrounds[new Random().nextInt(backgrounds.length)];
    }
}
