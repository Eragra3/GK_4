package Common.Models;

/**
 * Created by bider_000 on 10.01.2016.
 */
public class ObserverModel {
    public double x = 100;
    public double y = 100;
    public double z = 0;

    public double fovX = 45.0;
    public double fovY = 45.0;

    public double xAngle;
    public double yAngle;
    public double zAngle;

    public ObserverModel(double x, double y, double z, double fovX, double fovY, double xAngle, double yAngle, double zAngle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.fovX = fovX;
        this.fovY = fovY;
        this.xAngle = xAngle;
        this.yAngle = yAngle;
        this.zAngle = zAngle;
    }

    public double distanceTo(double x, double y, double z) {
        return Math.sqrt(
                (this.x - x) * (this.x - x) +
                        (this.y - y) * (this.y - y) +
                        (this.z - z) * (this.z - z)
        );
    }
}
