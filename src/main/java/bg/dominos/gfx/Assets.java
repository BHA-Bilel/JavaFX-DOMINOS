package bg.dominos.gfx;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Assets {

    public static BufferedImage[][] Vdominoes = new BufferedImage[7][7], Hdominoes = new BufferedImage[7][7];
    public static BufferedImage vback, hback;
    public static double height, width;

    public static void init_resolution() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        height = screenSize.getHeight();
        width = screenSize.getWidth();
    }

    public static void init_assets() {
        vback = ImageLoader.loadImage("/textures/vback.png");
        hback = ImageLoader.loadImage("/textures/hback.png");
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (i <= j) {
                    Vdominoes[i][j] = ImageLoader.loadImage("/textures/v" + i + "" + j + ".png");
                    Hdominoes[i][j] = ImageLoader.loadImage("/textures/h" + i + "" + j + ".png");
                } else {
                    Vdominoes[i][j] = Vdominoes[j][i];
                    Hdominoes[i][j] = Hdominoes[j][i];
                }
            }
        }
    }

    public static void drop_assets() {
        vback = null;
        hback = null;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Vdominoes[i][j] = null;
                Hdominoes[i][j] = null;
            }
        }
    }

    public static Image getDomino(int left, int right, boolean vertical) {
        return left == -1 ? SwingFXUtils.toFXImage(vertical ? vback : hback, null)
                : SwingFXUtils.toFXImage(vertical ? Vdominoes[left][right] : Hdominoes[left][right], null);
    }

    public static Image getVertical(int left, int right) {
        return SwingFXUtils.toFXImage(Vdominoes[left][right], null);
    }

    public static Image getHorizontal(int left, int right) {
        return SwingFXUtils.toFXImage(Hdominoes[left][right], null);
    }

}