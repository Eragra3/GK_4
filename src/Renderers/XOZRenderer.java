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
        int alpha, red, green, blue;
        ColorModel aColor = new ColorModel(), bColor = new ColorModel(), cColor = new ColorModel();

        //bounding box
        Point2D tl;
        Point2D br;

        // vectors to pass arguments to and from methods
        Vector3DModel normal = new Vector3DModel(),
                observerVector = new Vector3DModel(),
                reflectionVector = new Vector3DModel();
        LightSourceModel lightVector = new LightSourceModel();
        //

        for (TriangleModel model : triangles) {
            calculateLighting(projectVertex(model.a), normal, lightVector, observerVector, reflectionVector, aColor);
            calculateLighting(projectVertex(model.b), normal, lightVector, observerVector, reflectionVector, bColor);
            calculateLighting(projectVertex(model.c), normal, lightVector, observerVector, reflectionVector, cColor);

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
                            tempI = i + Configuration.IMAGE_WIDTH_HALF;
                            //JAVAFX y coordinate grows downwards, hence minus sign
                            tempJ = -j + Configuration.IMAGE_HEIGHT_HALF;
                            if (dist < zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI]) {
                                zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI] = dist;

                                //lighting
//                                alpha = (int) (model.a.a * t0 + model.b.a * t1 + model.c.a * (1 - t0 - t1));
                                alpha = 255;
                                red = (int) (aColor.red * u + bColor.red * v + cColor.red * (1 - u - v));
                                green = (int) (aColor.green * u + bColor.green * v + cColor.green * (1 - u - v));
                                blue = (int) (aColor.blue * u + bColor.blue * v + cColor.blue * (1 - u - v));
                                //

                                pixelData[tempJ * Configuration.IMAGE_WIDTH - tempI] = (alpha << 24) + (red << 16) + (green << 8) + blue;
                            }
                        }
                    }
                }
            }
        }

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, Configuration.pixelARGBFormat,
                pixelData, 0, Configuration.IMAGE_WIDTH);
    }

    private final ColorModel calculateLighting(Vertex3DModel projectedVertex, Vector3DModel normal, LightSourceModel
            lightVector, Vector3DModel observerVector, Vector3DModel reflectionVector, ColorModel resultColor) {
        double red, green, blue;
        final int n = 50;
        normal.x = projectedVertex.normX;
        normal.y = projectedVertex.normY;
        normal.z = projectedVertex.normZ;

        lightVector = projectLightSource(Configuration.lightSource);
        lightVector.x = lightVector.x - projectedVertex.x;
        lightVector.y = lightVector.y - projectedVertex.y;
        lightVector.z = lightVector.z - projectedVertex.z;


        //observer is hardcoded at 0 0 0
        observerVector.x = projectedVertex.x - Configuration.observer.x;
        observerVector.y = projectedVertex.y - Configuration.observer.y;
        observerVector.z = projectedVertex.z - Configuration.observer.z;

        //normalize
        lightVector.normalize();
        observerVector.normalize();
        normal.normalize();
        //

        //reflection vector
        reflectionVector.x = 2 * Helpers.dotProduct(normal, lightVector) * normal.x - lightVector.x;
        reflectionVector.y = 2 * Helpers.dotProduct(normal, lightVector) * normal.y - lightVector.y;
        reflectionVector.z = 2 * Helpers.dotProduct(normal, lightVector) * normal.z - lightVector.z;
        reflectionVector.normalize();
        //

        double normalLightVectorDotProduct = Helpers.dotProduct(normal, lightVector);
        if (normalLightVectorDotProduct < 0)
            normalLightVectorDotProduct = 0;

        double reflectionObserverVectorDotProduct = Helpers.dotProduct(reflectionVector, observerVector);
        if (reflectionObserverVectorDotProduct < 0)
            reflectionObserverVectorDotProduct = 0;
        else
            reflectionObserverVectorDotProduct = Math.pow(reflectionObserverVectorDotProduct, n);

        red = projectedVertex.ka * Configuration.ambientLightR + Configuration.lightSource.r * (projectedVertex.kd * normalLightVectorDotProduct +
                projectedVertex.ks * reflectionObserverVectorDotProduct);
        green = projectedVertex.ka * Configuration.ambientLightG + Configuration.lightSource.g * (projectedVertex.kd * normalLightVectorDotProduct +
                projectedVertex.ks * reflectionObserverVectorDotProduct);
        blue = projectedVertex.ka * Configuration.ambientLightB + Configuration.lightSource.b * (projectedVertex.kd * normalLightVectorDotProduct +
                projectedVertex.ks * reflectionObserverVectorDotProduct);

        resultColor.red = (int) Helpers.clamp(red, 0, 255);
        resultColor.green = (int) Helpers.clamp(green, 0, 255);
        resultColor.blue = (int) Helpers.clamp(blue, 0, 255);
        resultColor.alpha = 255;

        return resultColor;
    }

    final private LightSourceModel projectLightSource(LightSourceModel lightSourceModel) {
        RealMatrix projectionResult;

        projectionResult = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {lightSourceModel.x},
                        {lightSourceModel.y},
                        {lightSourceModel.z},
                        {1}
                }
        ));

        LightSourceModel resultLightSourceModel;

        resultLightSourceModel = new LightSourceModel(
                projectionResult.getEntry(0, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(1, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(2, 0),
                lightSourceModel.r,
                lightSourceModel.g,
                lightSourceModel.b
        );
        return resultLightSourceModel;
    }

    final private Vertex3DModel projectVertex(Vertex3DModel vertex) {
        RealMatrix projectionResult;

        projectionResult = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {vertex.x},
                        {vertex.y},
                        {vertex.z},
                        {1}
                }
        ));

        Vertex3DModel resultVertex;

        resultVertex = new Vertex3DModel(
                projectionResult.getEntry(0, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(1, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(2, 0)
        );
        resultVertex.normX = vertex.normX;
        resultVertex.normY = vertex.normY;
        resultVertex.normZ = vertex.normZ;
        return resultVertex;
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
