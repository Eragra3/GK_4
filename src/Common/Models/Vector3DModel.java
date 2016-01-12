package Common.Models;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class Vector3DModel extends Basic3DPoint{

    public Vector3DModel(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3DModel() {

    }

    public final void normalize() {
        double length = Math.sqrt((x * x) + (y * y) + (z * z));
        x /= length;
        y /= length;
        z /= length;
    }
    public final double length() {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }
}
