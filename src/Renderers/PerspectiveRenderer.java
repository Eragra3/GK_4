package Renderers;

import Common.Configuration;
import Common.Helpers;
import Common.Models.TriangleModel;
import Common.Models.Vector3DModel;
import Common.Models.Vertex3DModel;
import javafx.scene.image.PixelWriter;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class PerspectiveRenderer implements IRenderer {

    private final double nearZ = 1;
    private final double farZ = 500;

    ArrayList<Vertex3DModel> vertices;

//    List<Vertex2DModel> projectedVertices;

    ArrayList<TriangleModel> triangles;

    PixelWriter pixelWriter;

    int[] pixelData = new int[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH];

    double[] zBuffer = new double[Configuration.IMAGE_WIDTH * Configuration.IMAGE_HEIGHT];

    RealMatrix projectionMatrix;
    RealMatrix invProjectionMatrix;

    // vectors to pass arguments to and from methods
    Vector3DModel normal = new Vector3DModel(),
            lightVector = new Vector3DModel(),
            observerVector = new Vector3DModel(),
            reflectionVector = new Vector3DModel();
    //


    public PerspectiveRenderer(ArrayList<Vertex3DModel> vertices, ArrayList<TriangleModel> triangles, PixelWriter pixelWriter) {
        this.vertices = vertices;
        this.triangles = triangles;
        this.pixelWriter = pixelWriter;

//        this.projectedVertices = new ArrayList<>(vertices.size());

        reloadData();
    }

    public void render() {
        Helpers.resetZBuffer(zBuffer);

        Helpers.resetPixelDataToWhite(pixelData);


        double x, x0, x1, y, y0, y1, t0, t1, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;
        int alpha, red, green, blue;

        //bounding box
        double tlX;
        double tlY;

        double brX;
        double brY;

        RealMatrix invProjectionResult;

        for (TriangleModel triangle : triangles) {
            tlX = Helpers.min(triangle.a.projX, triangle.b.projX, triangle.c.projX);
            tlY = Helpers.min(triangle.a.projY, triangle.b.projY, triangle.c.projY);
            brX = Helpers.max(triangle.a.projX, triangle.b.projX, triangle.c.projX);
            brY = Helpers.max(triangle.a.projY, triangle.b.projY, triangle.c.projY);

            x0 = triangle.a.projX - triangle.c.projX;
            x1 = triangle.b.projX - triangle.c.projX;

            y0 = triangle.a.projY - triangle.c.projY;
            y1 = triangle.b.projY - triangle.c.projY;

            // Compute dot products
            dot00 = x0 * x0 + y0 * y0;
            dot01 = x0 * x1 + y0 * y1;
            dot11 = x1 * x1 + y1 * y1;

            // Compute barycentric coordinates
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);

            if (invDenom != Double.POSITIVE_INFINITY && invDenom != Double.NEGATIVE_INFINITY) {
                for (int i = (int) tlX; i < (int) brX + 1 && i > -201 && i < 200; i++) {
                    for (int j = (int) tlY; j < (int) brY + 1 && j > -201 && j < 200;
                         j++) {
                        x = i - triangle.c.projX;

                        y = j - triangle.c.projY;

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
                            dist = triangle.a.projDistance * t0 + triangle.b.projDistance * t1 + triangle.c.projDistance * (1 - t0 - t1);

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
                            tempJ = -j + Configuration.IMAGE_HEIGHT_HALF;
                            if (dist < zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI]) {
                                zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI] = dist;

                                //lighting
//                                alpha = (int) (model.a.a * t0 + model.b.a * t1 + model.c.a * (1 - t0 - t1));
                                alpha = 255;
                                red = (int) (triangle.a.r * t0 + triangle.b.r * t1 + triangle.c.r * (1 - t0 - t1));
                                green = (int) (triangle.a.g * t0 + triangle.b.g * t1 + triangle.c.g * (1 - t0 - t1));
                                blue = (int) (triangle.a.b * t0 + triangle.b.b * t1 + triangle.c.b * (1 - t0 - t1));
                                //

                                pixelData[tempJ * Configuration.IMAGE_WIDTH + tempI] = (alpha << 24) + (red << 16) + (green << 8) + blue;
                            }
                        }
                    }
                }
            }
        }

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, Configuration.pixelARGBFormat,
                pixelData, 0, Configuration.IMAGE_WIDTH);
    }

    public void reloadData() {
        prepareProjectionMatrix();

        projectVerticesAndCalculateLighting();
    }

    final private void projectVerticesAndCalculateLighting() {
        RealMatrix resultVector;

        for (Vertex3DModel vertex : vertices) {
            resultVector = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                    {
                            {vertex.x},
                            {vertex.y},
                            {vertex.z},
                            {1}
                    }
            ));
            vertex.projX = (resultVector.getEntry(0, 0) / resultVector.getEntry(3, 0)) * Configuration.IMAGE_WIDTH;
            vertex.projY = (resultVector.getEntry(1, 0) / resultVector.getEntry(3, 0)) * Configuration.IMAGE_HEIGHT;
            vertex.projDistance = resultVector.getEntry(2, 0);

            CommonMethods.calculateLighting(vertex, normal, lightVector, observerVector, reflectionVector);
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


}
