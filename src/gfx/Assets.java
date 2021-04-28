package gfx;

import java.awt.*;
import java.awt.image.BufferedImage;

import javafx.scene.transform.Scale;
import model.Domino;
import model.Position;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Assets {


    public static BufferedImage[][] Vdominoes = new BufferedImage[7][7], Hdominoes = new BufferedImage[7][7];
    public static BufferedImage vback, hback;

    private static double width, height;
    public static double scale_width, scale_height, unscale_width, unscale_height;
    public static Scale scale;

    public static double mainApp_width = 0, mainApp_height = 0;
    public static double joinApp_width = 0, joinApp_height = 0;
    public static double roomApp_width = 0, roomApp_height = 0;
    public static double gameApp_width = 0, gameApp_height = 0;

    public static void init_scale() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = screenSize.getWidth();
        height = screenSize.getHeight();
        scale_width = scale_width(1);
        scale_height = scale_height(1);
        unscale_width = unscale_width(1);
        unscale_height = unscale_height(1);
        scale = new Scale(scale_width, scale_height, 0, 0);
    }

    public static double scale_width(double old_width) {
        return old_width * (width / 1920.0);
    }

    public static double scale_height(double old_height) {
        return old_height * (height / 1080.0);
    }

    public static double unscale_width(double old_width) {
        return old_width * (1920.0 / width);
    }

    public static double unscale_height(double old_height) {
        return old_height * (1080.0 / height);
    }


    public static void init() {
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

    public static ImageView getDomino(Domino domino) {
        Image image;
        boolean vertical = domino.getPosition() == Position.TOP || domino.getPosition() == Position.BOTTOM
                || (domino.getPosition() == Position.CENTER && domino.isDouble());
        if (domino.getLeftValue() == -1 || domino.getRightValue() == -1 || domino.getLeftValue() == 7
                || domino.getRightValue() == 7)
            image = SwingFXUtils.toFXImage(vertical ? vback : hback, null);
        else
            image = SwingFXUtils.toFXImage(vertical ? Vdominoes[domino.getLeftValue()][domino.getRightValue()]
                    : Hdominoes[domino.getLeftValue()][domino.getRightValue()], null);
        ImageView iv = new ImageView();
        iv.setImage(image);
        if (domino.getLeftValue() < domino.getRightValue())
            iv.setRotate(180);
        iv.setPreserveRatio(true);
        if (vertical)
            iv.setFitHeight(image.getHeight());
        else
            iv.setFitWidth(image.getWidth());
        return iv;
    }

    public static ImageView getVertical(int left, int right, boolean rotate) {
        Image image;
        image = SwingFXUtils.toFXImage(Vdominoes[left][right], null);
        ImageView iv = new ImageView();
        iv.setImage(image);
        if (left < right || rotate)
            iv.setRotate(180);
        iv.setPreserveRatio(true);
        iv.setFitHeight(image.getHeight());
        return iv;
    }

    public static ImageView getHorizontal(int left, int right, boolean corner) {
        Image image;
        image = SwingFXUtils.toFXImage(Hdominoes[left][right], null);
        ImageView iv = new ImageView();
        iv.setImage(image);
        if (!corner && left < right || corner && left > right)
            iv.setRotate(180);
        iv.setPreserveRatio(true);
        iv.setFitWidth(image.getWidth());
        return iv;
    }

    public static ImageView getHorizontal(int leftValue, int rightValue) {
        Image image;
        image = SwingFXUtils.toFXImage(Hdominoes[leftValue][rightValue], null);
        ImageView iv = new ImageView();
        iv.setImage(image);
        iv.setPreserveRatio(true);
        iv.setFitWidth(image.getWidth());
        return iv;
    }

}