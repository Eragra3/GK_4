package Renderers;

import Common.Configuration;
import Common.Helpers;
import Common.Models.*;
import javafx.geometry.Point2D;
import javafx.scene.image.PixelWriter;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class XOZRenderer implements IRenderer {

    ArrayList<Vertex3DModel> vertices;

    ArrayList<TriangleModel> triangles;

    PixelWriter pixelWriter;

    RealMatrix projectionMatrix;
    RealMatrix normalsProjectionMatrix;
    RealMatrix invProjectionMatrix;

    int[] pixelData = new int[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH];

    double[] zBuffer = new double[Configuration.IMAGE_WIDTH * Configuration.IMAGE_HEIGHT];

    public XOZRenderer(ArrayList<Vertex3DModel> vertices, ArrayList<TriangleModel> triangles, PixelWriter pixelWriter) {
        this.vertices = vertices;
        this.triangles = triangles;
        this.pixelWriter = pixelWriter;

        prepareProjectionMatrix();
    }

    @Override
    public void setPixelWriter(PixelWriter pixelWriter) {
        this.pixelWriter = pixelWriter;
    }

    public void render() {
        Helpers.resetZBuffer(zBuffer);

        Helpers.resetPixelData(pixelData);

        double x, x0, x1, z, z0, z1, u, v, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;
        int alpha, red, green, blue, color;
        ColorModel aColor = new ColorModel(), bColor = new ColorModel(), cColor = new ColorModel();

        //bounding box
        Point2D tl;
        Point2D br;

        // vectors to pass arguments to and from methods
        Vector3DModel normal = new Vector3DModel(),
                observerVector = new Vector3DModel(),
                reflectionVector = new Vector3DModel();
        LightSourceModel lightVector = new LightSourceModel();
        Vertex3DModel workingPointViewSpace, workingPoint;
        //

        for (TriangleModel model : triangles) {

            if (!CommonMethods.usePhong) {
                CommonMethods.calculateLighting(CommonMethods.projectVertex(model.a, projectionMatrix), normal, lightVector, observerVector,
                        reflectionVector, aColor, normalsProjectionMatrix, projectionMatrix, Configuration.observerXOZ);
                CommonMethods.calculateLighting(CommonMethods.projectVertex(model.b, projectionMatrix), normal, lightVector, observerVector,
                        reflectionVector, bColor, normalsProjectionMatrix, projectionMatrix, Configuration.observerXOZ);
                CommonMethods.calculateLighting(CommonMethods.projectVertex(model.c, projectionMatrix), normal, lightVector, observerVector,
                        reflectionVector, cColor, normalsProjectionMatrix, projectionMatrix, Configuration.observerXOZ);
            }

            tl = new Point2D(Helpers.min(model.a.x, model.b.x, model.c.x), Helpers.min(model.a.z, model.b.z, model.c.z));
            br = new Point2D(Helpers.max(model.a.x, model.b.x, model.c.x), Helpers.max(model.a.z, model.b.z, model.c.z));

            x0 = model.a.x - model.c.x;
            x1 = model.b.x - model.c.x;

            z0 = model.a.z - model.c.z;
            z1 = model.b.z - model.c.z;

            // Compute dot products
            dot00 = x0 * x0 + z0 * z0;
            dot01 = x0 * x1 + z0 * z1;
            dot11 = x1 * x1 + z1 * z1;

            // Compute barycentric coordinates
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);

            if (invDenom != Double.POSITIVE_INFINITY && invDenom != Double.NEGATIVE_INFINITY) {
                for (int i = (int) tl.getX(); i < (int) br.getX() + 1; i++) {
                    for (int j = (int) tl.getY(); j < (int) br.getY() + 1; j++) {
                        x = i - model.c.x;

                        z = j - model.c.z;

                        dot02 = x0 * x + z0 * z;
                        dot12 = x1 * x + z1 * z;

                        u = (dot11 * dot02 - dot01 * dot12) * invDenom;
                        v = (dot00 * dot12 - dot01 * dot02) * invDenom;


                        if (u <= 0 || v <= 0 || u + v > 1) {
                            continue;
                        } else {
                            dist = Math.abs(Configuration.observer.y - (model.a.y * u + model.b.y * v + model.c.y * (1 - u -
                                    v)));

                            workingPoint = CommonMethods.getVertexFromBarycentric(u, v, model.a, model.b, model.c);
                            workingPointViewSpace = CommonMethods.projectVertexWithNormals(workingPoint, projectionMatrix, normalsProjectionMatrix);
//                            dist = workingPointViewSpace.z;
                            tempI = i + Configuration.IMAGE_WIDTH_HALF;
                            //JAVAFX y coordinate grows downwards, hence minus sign
                            tempJ = -j + Configuration.IMAGE_HEIGHT_HALF;
                            if (dist < zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI]) {
                                zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI] = dist;

                                //shading
                                if (CommonMethods.usePhong) {
//                                    color = CommonMethods.PhongShading(aColor, bColor, cColor, u, v);
                                    CommonMethods.calculateLighting(workingPointViewSpace, normal, lightVector, observerVector,
                                            reflectionVector, aColor, normalsProjectionMatrix, projectionMatrix, Configuration.observerXOZ);
                                    color = aColor.getRGB();
                                } else {
                                    color = CommonMethods.GouradShading(aColor, bColor, cColor, u, v);
                                }
                                //

                                pixelData[tempJ * Configuration.IMAGE_WIDTH + tempI] = color;
                            }
                        }
                    }
                }
            }
        }

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, Configuration.pixelARGBFormat,
                pixelData, 0, Configuration.IMAGE_WIDTH);
    }

    final private void prepareProjectionMatrix() {
        projectionMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {Configuration.IMAGE_WIDTH, 0, 0, 0},
                        {0, Configuration.IMAGE_HEIGHT, 0, 0},
                        {0, 0,
                                -((Configuration.FAR_Z + Configuration.NEAR_Z) / (Configuration.FAR_Z - Configuration.NEAR_Z)),
                                -((2 * Configuration.FAR_Z * Configuration.NEAR_Z) / (Configuration.FAR_Z - Configuration.NEAR_Z))},
                        {0, 0, -1, 1}
                }
        );
        invProjectionMatrix = MatrixUtils.inverse(projectionMatrix);
        normalsProjectionMatrix = invProjectionMatrix.transpose();
    }
}
