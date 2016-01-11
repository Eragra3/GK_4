package Renderers;

import Common.Configuration;
import Common.Helpers;
import Common.Models.TriangleModel;
import Common.Models.Vector3DModel;
import Common.Models.Vertex2DModel;
import Common.Models.Vertex3DModel;
import javafx.geometry.Point2D;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class PerspectiveRenderer implements IRenderer {

    private final double nearZ = 1;
    private final double farZ = 500;

    ArrayList<Vertex3DModel> vertices;

    List<Vertex2DModel> projectedVertices;

    ArrayList<TriangleModel> triangles;

    PixelWriter pixelWriter;

    int[] pixelData = new int[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH];

    double[][] zBuffer = new double[Configuration.IMAGE_WIDTH][Configuration.IMAGE_HEIGHT];

    RealMatrix projectionMatrix;
    RealMatrix invProjectionMatrix;

    public PerspectiveRenderer(ArrayList<Vertex3DModel> vertices, ArrayList<TriangleModel> triangles, PixelWriter pixelWriter) {
        this.vertices = vertices;
        this.triangles = triangles;
        this.pixelWriter = pixelWriter;

        this.projectedVertices = new ArrayList<>(vertices.size());

        reloadData();
    }

    public void render() {
        for (int i = 0; i < Helpers.zBufferInitial.length; i++)
            zBuffer[i] = Helpers.zBufferInitial[i].clone();

        for (int i = 0; i < pixelData.length; i++)
            pixelData[i] = 0xffffffff;

        double x, x0, x1, y, y0, y1, t0, t1, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;
        int alpha, red, green, blue;

        double Ia, Ir, Ig, Ib;
        //bounding box
        Point2D tl;
        Point2D br;

        RealMatrix invProjectionResult;

        for (TriangleModel model : triangles) {

            tl = new Point2D(Helpers.min(model.a.projX, model.b.projX, model.c.projX),
                    Helpers.min(model.a.projY, model.b.projY, model.c.projY));
            br = new Point2D(Helpers.max(model.a.projX, model.b.projX, model.c.projX),
                    Helpers.max(model.a.projY, model.b.projY, model.c.projY));

            x0 = model.a.projX - model.c.projX;
            x1 = model.b.projX - model.c.projX;

            y0 = model.a.projY - model.c.projY;
            y1 = model.b.projY - model.c.projY;

            // Compute dot products
            dot00 = x0 * x0 + y0 * y0;
            dot01 = x0 * x1 + y0 * y1;
            dot11 = x1 * x1 + y1 * y1;

            // Compute barycentric coordinates
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);

            if (invDenom != Double.POSITIVE_INFINITY && invDenom != Double.NEGATIVE_INFINITY) {
                for (int i = (int) tl.getX(); i < (int) br.getX() + 1 && i > -201 && i < 200; i++) {
                    for (int j = (int) tl.getY(); j < (int) br.getY() + 1 && j > -201 && j < 200; j++) {
                        x = i - model.c.projX;

                        y = j - model.c.projY;

                        dot02 = x0 * x + y0 * y;
                        dot12 = x1 * x + y1 * y;

                        t0 = (dot11 * dot02 - dot01 * dot12) * invDenom;
                        t1 = (dot00 * dot12 - dot01 * dot02) * invDenom;


                        if (t0 <= 0 || t1 <= 0 || t0 + t1 > 1) {
                            continue;
                        } else {
//                            dist = Math.abs(Configuration.observer.distanceTo(
//                                    model.a.x * t0 + model.b.x * t1 + model.c.x * (1 - t0 - t1),
//                                    model.a.y * t0 + model.b.y * t1 + model.c.y * (1 - t0 - t1),
//                                    model.a.z * t0 + model.b.z * t1 + model.c.z * (1 - t0 - t1)
//                            ));
                            dist = model.a.projDistance * t0 + model.b.projDistance * t1 + model.c.projDistance * (1 - t0 - t1);

//                            invProjectionResult = invProjectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
//                                    {
//                                            {i},
//                                            {j},
//                                            {(i * t0 + j * t1 - dist) / (1 - t0 - t1)},
//                                            {1}
//                                    }
//                            ));

//                            tempI = (int)(invProjectionResult.getEntry(0, 0) / invProjectionMatrix.getEntry(3, 0) +
//                                    Configuration.IMAGE_WIDTH_HALF);
//                            tempJ = (int)(invProjectionResult.getEntry(1, 0) / invProjectionMatrix.getEntry(3, 0) +
//                                    Configuration.IMAGE_HEIGHT_HALF);
                            tempI = i + Configuration.IMAGE_WIDTH_HALF;
                            tempJ = j + Configuration.IMAGE_HEIGHT_HALF;
                            if (dist < zBuffer[tempI][tempJ]) {
                                zBuffer[tempI][tempJ] = dist;

                                //lighting
                                alpha = (int) (model.a.a * t0 + model.b.a * t1 + model.c.a * (1 - t0 - t1));
                                red = (int) (model.a.r * t0 + model.b.r * t1 + model.c.r * (1 - t0 - t1));
                                green = (int) (model.a.g * t0 + model.b.g * t1 + model.c.g * (1 - t0 - t1));
                                blue = (int) (model.a.b * t0 + model.b.b * t1 + model.c.b * (1 - t0 - t1));
                                //

                                pixelData[tempI * Configuration.IMAGE_WIDTH + tempJ] = (alpha << 24) + (red << 16) + (green << 8) + blue;
                            }
                        }
                    }
                }
            }
        }

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(),
                pixelData, 0, Configuration.IMAGE_WIDTH);
        pixelWriter.setColor(180, 285, Color.RED);

    }

    public void reloadData() {
        prepareProjectionMatrix();

        projectVertices();
    }

    final private void projectVertices() {
        RealMatrix resultVector;

        Vector3DModel normal = new Vector3DModel(),
                lightVector = new Vector3DModel(),
                observerVector = new Vector3DModel(),
                reflectionVector = new Vector3DModel();

        for (Vertex3DModel vertex : vertices) {
            resultVector = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                    {
                            {vertex.x},
                            {vertex.y},
                            {vertex.z},
                            {1}
                    }));
            vertex.projX = (resultVector.getEntry(0, 0) / resultVector.getEntry(3, 0)) * Configuration.IMAGE_WIDTH;
            vertex.projY = (resultVector.getEntry(1, 0) / resultVector.getEntry(3, 0)) * Configuration.IMAGE_HEIGHT;
            vertex.projDistance = resultVector.getEntry(2, 0);

            calculateLighting(vertex, normal, lightVector, observerVector, reflectionVector);
        }
    }

    final private void prepareProjectionMatrix() {
//        double fovX = 1.0 / Math.tan(Math.toRadians(Configuration.observer.angleX) / 2.0);
//        double fovY = 1.0 / Math.tan(Math.toRadians(Configuration.observer.angleY) / 2.0);
        double fovX = Math.atan(Math.toRadians(Configuration.observer.angleX / 2.0));
        double fovY = Math.atan(Math.toRadians(Configuration.observer.angleY / 2.0));

        projectionMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {fovX, 0, 0, 0},
                        {0, fovY, 0, 0},
                        {0, 0, -((farZ + nearZ) / (farZ - nearZ)), -((2 * farZ * nearZ) / (farZ - nearZ))},
                        {0, 0, -1, 1}
                }
        );
        projectionMatrix = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {1, 0, 0, Configuration.observer.x},
                        {0, 1, 0, Configuration.observer.y},
                        {0, 0, 1, Configuration.observer.z},
                        {0, 0, 0, 1}
                }
        ));
        invProjectionMatrix = MatrixUtils.inverse(projectionMatrix);
    }

    private final static void calculateLighting(Vertex3DModel vertex, Vector3DModel normal, Vector3DModel
            lightVector, Vector3DModel observerVector, Vector3DModel reflectionVector) {
        double Ir, Ig, Ib;
        final int n = 50;
        normal.x = vertex.normX;
        normal.y = vertex.normY;
        normal.z = vertex.normZ;

        lightVector.x = Configuration.lightSource.x - vertex.x;
        lightVector.y = Configuration.lightSource.y - vertex.y;
        lightVector.z = Configuration.lightSource.z - vertex.z;

        reflectionVector.x =  lightVector.x - 2 * Helpers.dotProduct(normal, lightVector) * normal.x ;
        reflectionVector.y =  lightVector.y - 2 * Helpers.dotProduct(normal, lightVector) * normal.y ;
        reflectionVector.z =  lightVector.z - 2 * Helpers.dotProduct(normal, lightVector) * normal.z ;

        //observer is hardcoded
        observerVector.x = vertex.x;
        observerVector.y = vertex.y;
        observerVector.z = vertex.z;

        //normalize
        lightVector.normalize();
        observerVector.normalize();
        reflectionVector.normalize();
        normal.normalize();
        //
        double normalLightVectorDotProduct = Helpers.dotProduct(normal, lightVector);
//        if (normalLightVectorDotProduct < 0)
//            normalLightVectorDotProduct = 0;

        double reflectionObserverVectorDotProduct = Helpers.dotProduct(normal, lightVector);
//        if (reflectionObserverVectorDotProduct < 0)
//            reflectionObserverVectorDotProduct = 0;
//        else
            reflectionObserverVectorDotProduct = Math.pow(reflectionObserverVectorDotProduct, n);

        Ir = vertex.ka * Configuration.ambientLightR + Configuration.lightSource.r * (vertex.kd * normalLightVectorDotProduct +
                vertex.ks * reflectionObserverVectorDotProduct);
        Ig = vertex.ka * Configuration.ambientLightG + Configuration.lightSource.g * (vertex.kd * normalLightVectorDotProduct +
                vertex.ks * reflectionObserverVectorDotProduct);
        Ib = vertex.ka * Configuration.ambientLightB + Configuration.lightSource.b * (vertex.kd * normalLightVectorDotProduct +
                vertex.ks * reflectionObserverVectorDotProduct);

        vertex.a = 255;
        vertex.r = (int) Ir;
        vertex.g = (int) Ig;
        vertex.b = (int) Ib;
    }

}
