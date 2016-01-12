package Renderers;

import Common.Configuration;
import Common.Helpers;
import Common.Models.*;
import javafx.scene.image.PixelWriter;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class PerspectiveRenderer implements IRenderer {

    ArrayList<Vertex3DModel> vertices;

//    List<Vertex2DModel> projectedVertices;

    ArrayList<TriangleModel> triangles;

    PixelWriter pixelWriter;

    int[] pixelData = new int[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH];

    double[] zBuffer = new double[Configuration.IMAGE_WIDTH * Configuration.IMAGE_HEIGHT];

    RealMatrix projectionMatrix;
    RealMatrix normalsProjectionMatrix;
    RealMatrix invProjectionMatrix;

    // vectors to pass arguments to and from methods
    Vector3DModel normal = new Vector3DModel(),
            observerVector = new Vector3DModel(),
            reflectionVector = new Vector3DModel();
    LightSourceModel lightVector = new LightSourceModel();
    //


    public PerspectiveRenderer(ArrayList<Vertex3DModel> vertices, ArrayList<TriangleModel> triangles, PixelWriter pixelWriter) {
        this.vertices = vertices;
        this.triangles = triangles;
        this.pixelWriter = pixelWriter;

//        this.projectedVertices = new ArrayList<>(vertices.size());

        reloadData();
    }

    @Override
    public void setPixelWriter(PixelWriter pixelWriter) {
        this.pixelWriter = pixelWriter;
    }


    public void render() {
        Helpers.resetZBuffer(zBuffer);

        Helpers.resetPixelData(pixelData);

        double x, x0, x1, y, y0, y1, u, v, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;
        int alpha, red, green, blue;
        ColorModel aColor = new ColorModel(), bColor = new ColorModel(), cColor = new ColorModel();
        TriangleModel projectedTriangle;
        Vertex3DModel workingPoint;

        //bounding box
        double tlX;
        double tlY;

        double brX;
        double brY;

        RealMatrix invProjectionResult;

        for (TriangleModel model : triangles) {
            projectedTriangle = CommonMethods.projectTriangle(model, projectionMatrix);

            CommonMethods.calculateLighting(CommonMethods.projectVertex(model.a, projectionMatrix), normal, lightVector, observerVector,
                    reflectionVector, aColor, normalsProjectionMatrix, projectionMatrix);
            CommonMethods.calculateLighting(CommonMethods.projectVertex(model.b, projectionMatrix), normal, lightVector, observerVector,
                    reflectionVector, bColor, normalsProjectionMatrix, projectionMatrix);
            CommonMethods.calculateLighting(CommonMethods.projectVertex(model.c, projectionMatrix), normal, lightVector, observerVector,
                    reflectionVector, cColor, normalsProjectionMatrix, projectionMatrix);

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
                            workingPoint = CommonMethods.getVertexFromBarycentric(u, v, projectedTriangle.a, projectedTriangle.b, projectedTriangle.c);
                            dist = CommonMethods.projectVertex(workingPoint, projectionMatrix).z;
//                            tempI = (int) (invProjectionResult.getEntry(0, 0) / invProjectionMatrix.getEntry(3, 0) +
//                                    Configuration.IMAGE_WIDTH_HALF);
//                            tempJ = -((int) (invProjectionResult.getEntry(1, 0) / invProjectionMatrix.getEntry(3, 0) +
//                                    Configuration.IMAGE_HEIGHT_HALF));
                            tempI = i + Configuration.IMAGE_WIDTH_HALF;
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

    final private void prepareProjectionMatrix() {
        double fovX = Math.atan(Math.toRadians(Configuration.observer.fovX / 2.0));
        double fovY = Math.atan(Math.toRadians(Configuration.observer.fovY / 2.0));

        ObserverModel o = Configuration.observer;
        LookAtModel lA = Configuration.lookAtPoint;

        ObserverModel transformedObserverModel = new ObserverModel(
                o.x,
                o.y,
                o.z,
                o.fovX,
                o.fovY,
                o.xAngle,
                o.yAngle,
                o.zAngle
        );

        projectionMatrix = new Array2DRowRealMatrix(new double[][]
                {
                        {Configuration.IMAGE_WIDTH * fovX, 0, 0, 0},
                        {0, Configuration.IMAGE_HEIGHT * fovY, 0, 0},
                        {
                                0,
                                0,
                                -((Configuration.FAR_Z + Configuration.NEAR_Z) / (Configuration.FAR_Z - Configuration.NEAR_Z)),
                                -((2 * Configuration.FAR_Z * Configuration.NEAR_Z) / (Configuration.FAR_Z - Configuration.NEAR_Z))
                        },
                        {0, 0, -1, 1}
                }
        );
//        projectionMatrix = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
//                {
//                        {1, 0, 0, 0},
//                        {0, Math.cos(Configuration.observer.yAngle), -Math.sin(Configuration.observer.yAngle), 0},
//                        {0, Math.sin(Configuration.observer.yAngle), Math.cos(Configuration.observer.yAngle), 0},
//                        {0, 0, 0, 1}
//                }
//        ));

        //translation
        RealMatrix translationM = new Array2DRowRealMatrix(new double[][]
                {
                        {1, 0, 0, lA.x},
                        {0, 1, 0, lA.y},
                        {0, 0, 1, lA.z},
                        {0, 0, 0, 1}
                }
        );
        projectionMatrix = projectionMatrix.multiply(translationM);

        RealMatrix transformedObserverVector = Helpers.getColumnVector(
                transformedObserverModel.x,
                transformedObserverModel.y,
                transformedObserverModel.z
        );

        transformedObserverVector = translationM.multiply(transformedObserverVector);
        transformedObserverModel.x = transformedObserverVector.getEntry(0, 0) / transformedObserverVector.getEntry(3, 0);
        transformedObserverModel.y = transformedObserverVector.getEntry(1, 0) / transformedObserverVector.getEntry(3, 0);
        transformedObserverModel.z = transformedObserverVector.getEntry(2, 0) / transformedObserverVector.getEntry(3, 0);

        //OY rotation
        Configuration.lookAtPoint.yAngle = Math.PI - Math.atan2(transformedObserverModel.x, transformedObserverModel.z);

        RealMatrix yRotationM = new Array2DRowRealMatrix(new double[][]
                {
                        {Math.cos(Configuration.lookAtPoint.yAngle), 0, Math.sin(Configuration.lookAtPoint.yAngle), 0},
                        {0, 1, 0, 0},
                        {-Math.sin(Configuration.lookAtPoint.yAngle), 0, Math.cos(Configuration.lookAtPoint.yAngle), 0},
                        {0, 0, 0, 1}
                }
        );

        projectionMatrix = projectionMatrix.multiply(yRotationM);
        //OX rotation
        transformedObserverVector = yRotationM.multiply(transformedObserverVector);
        transformedObserverModel.x = transformedObserverVector.getEntry(0, 0) / transformedObserverVector.getEntry(3, 0);
        transformedObserverModel.y = transformedObserverVector.getEntry(1, 0) / transformedObserverVector.getEntry(3, 0);
        transformedObserverModel.z = transformedObserverVector.getEntry(2, 0) / transformedObserverVector.getEntry(3, 0);

        Configuration.lookAtPoint.xAngle = -Math.PI / 2 - Math.atan2(transformedObserverModel.z, transformedObserverModel.y);

        RealMatrix xRotationM = new Array2DRowRealMatrix(new double[][]
                {
                        {1, 0, 0, 0},
                        {0, Math.cos(Configuration.lookAtPoint.xAngle), -Math.sin(Configuration.lookAtPoint.xAngle), 0},
                        {0, Math.sin(Configuration.lookAtPoint.xAngle), Math.cos(Configuration.lookAtPoint.xAngle), 0},
                        {0, 0, 0, 1}
                }
        );

        projectionMatrix = projectionMatrix.multiply(xRotationM);

//        Configuration.lookAtPoint.xAngle = Math.PI / 2 - Math.atan2(transformedObserverModel.y, transformedObserverModel.x);
//        RealMatrix zRotationM = new Array2DRowRealMatrix(new double[][]
//                {
//                        {Math.cos(Configuration.lookAtPoint.zAngle), -Math.sin(Configuration.lookAtPoint.zAngle), 0, 0},
//                        {Math.sin(Configuration.lookAtPoint.zAngle), Math.cos(Configuration.lookAtPoint.zAngle), 0, 0},
//                        {0, 0, 1, 0},
//                        {0, 0, 0, 1}
//                }
//        );
//
//        projectionMatrix = projectionMatrix.multiply(zRotationM);

//        projectionMatrix = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
//                {
//                        {1, 0, 0, -lA.x},
//                        {0, 1, 0, -lA.y},
//                        {0, 0, 1, -lA.z},
//                        {0, 0, 0, 1}
//                }
//        ));
//
//        projectionMatrix = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
//                {
//                        {1, 0, 0, -Configuration.observer.x},
//                        {0, 1, 0, -Configuration.observer.y},
//                        {0, 0, 1, -Configuration.observer.z},
//                        {0, 0, 0, 1}
//                }
//        ));
        invProjectionMatrix = MatrixUtils.inverse(projectionMatrix);
        normalsProjectionMatrix = invProjectionMatrix.transpose();
    }

}
