package Common.Models;

/**
 * Created by bider_000 on 10.01.2016.
 */
public class LightSourceModel {
    public double x = 100;
    public double y = 100;
    public double z = 0;

    public int r = 127;
    public int g = 127;
    public int b = 127;

    public LightSourceModel(double x, double y, double z, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
