package Common.Models;


/**
 * Created by bider_000 on 08.01.2016.
 */
public class TriangleModel {

    public Vertex3DModel a;
    public Vertex3DModel b;
    public Vertex3DModel c;

    public Vector3DModel aNorm;
    public Vector3DModel bNorm;
    public Vector3DModel cNorm;

    public double ks = 1;
    public double kd = 1;
    public double ka = 1;

    public byte rC = 0;
    public byte gC = 0;
    public byte bC = 0;

    public TriangleModel() {

    }

    public TriangleModel(Vertex3DModel a, Vertex3DModel b, Vertex3DModel c, Vector3DModel aNorm, Vector3DModel bNorm,
                         Vector3DModel cNorm) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.aNorm = aNorm;
        this.bNorm = bNorm;
        this.cNorm = cNorm;
    }
}
