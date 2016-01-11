package Common.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class Vertex3DModel {
    public double x;
    public double y;
    public double z;

    public int a = 255;
    public int r = 255;
    public int g = 0;
    public int b = 0;

    public double normX;
    public double normY;
    public double normZ;

    public double ks = 0.5;
    public double kd = 0.5;
    public double ka = 0.5;

    public Vertex3DModel(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex3DModel copy() {
        return new Vertex3DModel(x, y, z);
    }

    public double distanceTo(double x, double y, double z) {
        return Math.sqrt(
                (this.x - x) * (this.x - x) +
                        (this.y - y) * (this.y - y) +
                        (this.z - z) * (this.z - z)
        );
    }

    public final void normalize() {
        double length = Math.sqrt((normX * normX) + (normY * normY) + (normZ * normZ));
        normX /= length;
        normY /= length;
        normZ /= length;
    }
}
