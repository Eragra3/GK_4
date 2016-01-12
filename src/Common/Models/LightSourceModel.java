package Common.Models;

/**
 * Created by bider_000 on 10.01.2016.
 */
public class LightSourceModel extends Vector3DModel {
    public int r;
    public int g;
    public int b;

    public LightSourceModel() {
    }

    public LightSourceModel(double z, double x, double y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    public LightSourceModel(double x, double y, double z, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
