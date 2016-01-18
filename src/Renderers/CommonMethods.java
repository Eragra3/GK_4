package Renderers;

import Common.*;
import Common.Models.*;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by bider_000 on 11.01.2016.
 */
public class CommonMethods {


    public final static LightSourceModel projectLightSource(LightSourceModel lightSourceModel, RealMatrix projectionMatrix) {
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

    public final static Vector3DModel projectVector(Vector3DModel vector, RealMatrix projectionMatrix) {
        RealMatrix projectionResult;

        projectionResult = projectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {vector.x},
                        {vector.y},
                        {vector.z},
                        {1}
                }
        ));

        Vector3DModel resultVector;

        resultVector = new Vector3DModel(
                projectionResult.getEntry(0, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(1, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(2, 0)
        );
        return resultVector;
    }

    public final static Vector3DModel projectNormal(Vector3DModel vertex, RealMatrix normalsProjectionMatrix) {
        RealMatrix projectionResult;

        projectionResult = normalsProjectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {vertex.x},
                        {vertex.y},
                        {vertex.z},
                        {1}
                }
        ));

        Vector3DModel resultVector;

        resultVector = new Vector3DModel(
                projectionResult.getEntry(0, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(1, 0) / projectionResult.getEntry(3, 0),
                projectionResult.getEntry(2, 0)
        );

        return resultVector;
    }

    public final static Vertex3DModel projectVertex(Vertex3DModel vertex, RealMatrix projectionMatrix) {
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

    public final static TriangleModel projectTriangle(TriangleModel triangle, RealMatrix projectionMatrix) {
        TriangleModel projectedTriangle;

        projectedTriangle = new TriangleModel(
                projectVertex(triangle.a, projectionMatrix),
                projectVertex(triangle.b, projectionMatrix),
                projectVertex(triangle.c, projectionMatrix),
                triangle.aNorm,
                triangle.bNorm,
                triangle.cNorm
        );

        return projectedTriangle;
    }

    public final static Vertex3DModel unprojectVertex(Vertex3DModel vertex, RealMatrix invProjectionMatrix) {
        RealMatrix multResult;

        multResult = invProjectionMatrix.multiply(new Array2DRowRealMatrix(new double[][]
                {
                        {vertex.x},
                        {vertex.y},
                        {vertex.z},
                        {1}
                }
        ));

        Vertex3DModel resultVertex;

        resultVertex = new Vertex3DModel(
                multResult.getEntry(0, 0) / multResult.getEntry(3, 0),
                multResult.getEntry(1, 0) / multResult.getEntry(3, 0),
                multResult.getEntry(2, 0)
        );

        return resultVertex;
    }

    public final static Vertex3DModel getVertexFromBarycentric(double u, double v, Vertex3DModel a, Vertex3DModel b, Vertex3DModel c) {
        Vertex3DModel workingPoint = new Vertex3DModel(
                a.x * u + b.x * v + c.x * (1 - u - v),
                a.y * u + b.y * v + c.y * (1 - u - v),
                a.z * u + b.z * v + c.z * (1 - u - v)
        );
        return workingPoint;
    }

    public final static ColorModel calculateLighting(Vertex3DModel projectedVertex, Vector3DModel normal, LightSourceModel
            lightVector, Vector3DModel observerVector, Vector3DModel reflectionVector, ColorModel resultColor, RealMatrix
                                                             normalsProjectionMatrix, RealMatrix projectionMatrix) {
        double red, green, blue;
        final int n = 100;
        normal.x = projectedVertex.normX;
        normal.y = projectedVertex.normY;
        normal.z = projectedVertex.normZ;
        normal = projectNormal(normal, normalsProjectionMatrix);

        lightVector = projectLightSource(Configuration.lightSource, projectionMatrix);
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

    public final static int GouradShading(ColorModel aColor, ColorModel bColor, ColorModel cColor, double u, double v) {
        int alpha = 255;
        int red = (int) (aColor.red * u + bColor.red * v + cColor.red * (1 - u - v));
        int green = (int) (aColor.green * u + bColor.green * v + cColor.green * (1 - u - v));
        int blue = (int) (aColor.blue * u + bColor.blue * v + cColor.blue * (1 - u - v));
        return (alpha << 24) + (red << 16) + (green << 8) + blue;
    }

    public final static int PhongShading(ColorModel aColor, ColorModel bColor, ColorModel cColor, double u, double v) {


        int alpha = 255;
        int red = (int) (aColor.red * u + bColor.red * v + cColor.red * (1 - u - v));
        int green = (int) (aColor.green * u + bColor.green * v + cColor.green * (1 - u - v));
        int blue = (int) (aColor.blue * u + bColor.blue * v + cColor.blue * (1 - u - v));
        return (alpha << 24) + (red << 16) + (green << 8) + blue;
    }
}
