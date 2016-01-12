package Common.Models;

/**
 * Created by bider_000 on 11.01.2016.
 */
public class ColorModel {
    public int red;
    public int green;
    public int blue;
    public int alpha = 0xff;

    public ColorModel(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public ColorModel(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public ColorModel() {

    }
}
