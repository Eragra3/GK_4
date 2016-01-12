package Common.Models;

/**
 * Created by bider_000 on 12.01.2016.
 */
public class LookAtModel {
    public double x;
    public double y;
    public double z;

    public double xAngle;
    public double yAngle;
    public double zAngle;

    public LookAtModel(double x, double y, double z, double xAngle, double yAngle, double zAngle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xAngle = xAngle;
        this.yAngle = yAngle;
        this.zAngle = zAngle;
    }

    public LookAtModel(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
