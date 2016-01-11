package Common.Models;

/**
 * Created by bider_000 on 10.01.2016.
 */
public class ObserverModel  {
    public double x = 100;
    public double y = 100;
    public double z = 0;

    public double angleX = 45.0;
    public double angleY = 45.0;

    public ObserverModel(double x, double y, double z, double angleX, double angleY) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angleX = angleX;
        this.angleY = angleY;
    }

    public double distanceTo(double x, double y, double z) {
        return Math.sqrt(
                (this.x - x) * (this.x - x) +
                        (this.y - y) * (this.y - y) +
                        (this.z - z) * (this.z - z)
        );
    }
}
