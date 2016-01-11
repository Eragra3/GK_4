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
import sun.security.provider.certpath.Vertex;

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

        double x, x0, x1, y, y0, y1, u, v, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;
        int alpha, red, green, blue;
        TriangleModel projectedTriangle;
        Vertex3DModel workingPoint;

        //bounding box
        double tlX;
        double tlY;

        double brX;
        double brY;

        RealMatrix invProjectionResult;

        for (TriangleModel model : triangles) {
            projectedTriangle = projectTriangle(model);


            tlX = Helpers.min(projectedTriangle.a.x, projectedTriangle.b.x, projectedTriangle.c.x);
            tlY = Helpers.min(projectedTriangle.a.y, projectedTriangle.b.y, projectedTriangle.c.y);
            brX = Helpers.max(projectedTriangle.a.x, projectedTriangle.b.x, projectedTriangle.c.x);
            brY = Helpers.max(projectedTriangle.a.y, projectedTriangle.b.y, projectedTriangle.c.y);

            x0 = projectedTriangle.a.x - projectedTriangle.c.x;
            x1 = projectedTriangle.b.x - projectedTriangle.c.x;

            y0 = projectedTriangle.a.y - projectedTriangle.c.y;
            y1 = projectedTriangle.b.y - projectedTriangle.c.y;

            // Compute dot products
            dot00 = x0 * x0 + y0 * y0;
            dot01 = x0 * x1 + y0 * y1;
            dot11 = x1 * x1 + y1 * y1;

            // Compute barycentric coordinates
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);

            if (invDenom != Double.POSITIVE_INFINITY && invDenom != Double.NEGATIVE_INFINITY) {
                for (int i = (int) tlX; i < (int) brX + 1 && i > -200 && i < 200; i++) {
                    for (int j = (int) tlY; j < (int) brY + 1 && j > -200 && j < 200; j++) {
                        x = i - projectedTriangle.c.x;

                        y = j - projectedTriangle.c.y;

                        dot02 = x0 * x + y0 * y;
                        dot12 = x1 * x + y1 * y;

                        u = (dot11 * dot02 - dot01 * dot12) * invDenom;
                        v = (dot00 * dot12 - dot01 * dot02) * invDenom;


                        if (u <= 0 || v <= 0 || u + v > 1) {
                            continue;
                        } else {
//                            dist = Math.abs(Configuration.observer.distanceTo(
//                                    model.a.x * t0 + model.b.x * t1 + model.c.x * (1 - t0 - t1),
//                                    model.a.y * t0 + model.b.y * t1 + model.c.y * (1 - t0 - t1),
//                                    model.a.z * t0 + model.b.z * t1 + model.c.z * (1 - t0 - t1)
//                            ));

//                            dist = projectedTriangle.a.z * t0 + projectedTriangle.b.z * t1 + projectedTriangle.c.z * (1 - t0 - t1);
                            workingPoint = getVertexFromBarycentric(u, v, projectedTriangle.a, projectedTriangle.b, projectedTriangle.c);
                            dist = projectVertex(workingPoint).z;
//                            tempI = (int) (invProjectionResult.getEntry(0, 0) / invProjectionMatrix.getEntry(3, 0) +
//                                    Configuration.IMAGE_WIDTH_HALF);
//                            tempJ = -((int) (invProjectionResult.getEntry(1, 0) / invProjectionMatrix.getEntry(3, 0) +
//                                    Configuration.IMAGE_HEIGHT_HALF));
                            tempI = i + Configuration.IMAGE_WIDTH_HALF;
                            tempJ = -j + Configuration.IMAGE_HEIGHT_HALF;
                            if (dist < zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI]) {
                                zBuffer[tempJ * Configuration.IMAGE_WIDTH + tempI] = dist;

                                invProjectionResult = invProjectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                                        {
                                                {i},
                                                {j},
                                                {0},
                                                {1}
                                        }
                                ));

                                //lighting
//                                alpha = (int) (model.a.a * t0 + model.b.a * t1 + model.c.a * (1 - t0 - t1));
                                alpha = 255;
                                red = (int) (projectedTriangle.a.r * u + projectedTriangle.b.r * v + projectedTriangle.c.r * (1 - u - v));
                                green = (int) (projectedTriangle.a.g * u + projectedTriangle.b.g * v + projectedTriangle.c.g * (1 - u - v));
                                blue = (int) (projectedTriangle.a.b * u + projectedTriangle.b.b * v + projectedTriangle.c.b * (1 - u - v));
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
    }

    final private Vertex3DModel getVertexFromBarycentric(double u, double v, Vertex3DModel a, Vertex3DModel b, Vertex3DModel c) {
        Vertex3DModel workingPoint = new Vertex3DModel(
                a.x * u + b.x * v + c.x * (1 - u - v),
                a.y * u + b.y * v + c.y * (1 - u - v),
                a.z * u + b.z * v + c.z * (1 - u - v)
        );
        return workingPoint;
    }

    final private TriangleModel projectTriangle(TriangleModel triangle) {
        TriangleModel projectedTriangle;

        projectedTriangle = new TriangleModel(
                projectVertex(triangle.a),
                projectVertex(triangle.b),
                projectVertex(triangle.c),
                triangle.aNorm,
                triangle.bNorm,
                triangle.cNorm
        );

        return projectedTriangle;
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

        return resultVertex;
    }

    final private Vertex3DModel unprojectVertex(Vertex3DModel vertex) {
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

        return resultVertex;
    }

    final private void prepareProjectionMatrix() {
        double fovX = Math.atan(Math.toRadians(Configuration.observer.angleX / 2.0));
        double fovY = Math.atan(Math.toRadians(Configuration.observer.angleY / 2.0));

        projectionMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {Configuration.IMAGE_WIDTH_HALF * fovX, 0, 0, 0},
                        {0, Configuration.IMAGE_HEIGHT_HALF * fovY, 0, 0},
                        {0, 0, ((farZ + nearZ) / (farZ - nearZ)), ((2 * farZ * nearZ) / (farZ - nearZ))},
                        {0, 0, 1, 1}
                }
        );
        projectionMatrix = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {1, 0, 0, -Configuration.observer.x},
                        {0, 1, 0, -Configuration.observer.y},
                        {0, 0, 1, -Configuration.observer.z},
                        {0, 0, 0, 1}
                }
        ));
        invProjectionMatrix = MatrixUtils.inverse(projectionMatrix);
    }


}
