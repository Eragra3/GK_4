package Renderers;

import Common.*;
import Common.Models.*;

/**
 * Created by bider_000 on 11.01.2016.
 */
public class CommonMethods {

    public final static void calculateLighting(Vertex3DModel vertex, Vector3DModel normal, Vector3DModel
            lightVector, Vector3DModel observerVector, Vector3DModel reflectionVector) {
        double Ir, Ig, Ib;
        final int n = 50;
        normal.x = vertex.normX;
        normal.y = vertex.normY;
        normal.z = vertex.normZ;

        lightVector.x = Configuration.lightSource.x - vertex.x;
        lightVector.y = Configuration.lightSource.y - vertex.y;
        lightVector.z = Configuration.lightSource.z - vertex.z;

        reflectionVector.x = lightVector.x - 2 * Helpers.dotProduct(normal, lightVector) * normal.x;
        reflectionVector.y = lightVector.y - 2 * Helpers.dotProduct(normal, lightVector) * normal.y;
        reflectionVector.z = lightVector.z - 2 * Helpers.dotProduct(normal, lightVector) * normal.z;

        //observer is hardcoded at 0 0 0
        observerVector.x = vertex.x - 0;
        observerVector.y = vertex.y - 0;
        observerVector.z = vertex.z - 0;

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
