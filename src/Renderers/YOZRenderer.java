package Renderers;

import Common.Configuration;
import Common.Helpers;
import Common.Models.TriangleModel;
import Common.Models.Vertex3DModel;
import javafx.geometry.Point2D;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

import java.util.ArrayList;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class YOZRenderer implements IRenderer {

    ArrayList<Vertex3DModel> vertices;

    ArrayList<TriangleModel> triangles;

    PixelWriter pixelWriter;


    byte[] pixelData = new byte[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH * 3];

    double[][] zBuffer = new double[Configuration.IMAGE_WIDTH][Configuration.IMAGE_HEIGHT];

    public YOZRenderer(ArrayList<Vertex3DModel> vertices, ArrayList<TriangleModel> triangles, PixelWriter pixelWriter) {
        this.vertices = vertices;
        this.triangles = triangles;
        this.pixelWriter = pixelWriter;
    }


    public void render() {
        for (int i = 0; i < Helpers.zBufferInitial.length; i++)
            zBuffer[i] = Helpers.zBufferInitial[i].clone();

        System.arraycopy(Helpers.whitePixelsData, 0, pixelData, 0, Helpers.whitePixelsData.length);

        double y, y0, y1, z, z0, z1, t0, t1, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;

        //bounding box
        Point2D tl;
        Point2D br;


        for (TriangleModel model : triangles) {
            tl = new Point2D(Helpers.min(model.a.y, model.b.y, model.c.y), Helpers.min(model.a.z, model.b.z, model.c.z));
            br = new Point2D(Helpers.max(model.a.y, model.b.y, model.c.y), Helpers.max(model.a.z, model.b.z, model.c.z));

            y0 = model.a.y - model.c.y;
            y1 = model.b.y - model.c.y;

            z0 = model.a.z - model.c.z;
            z1 = model.b.z - model.c.z;

            // Compute dot products
            dot00 = y0 * y0 + z0 * z0;
            dot01 = y0 * y1 + z0 * z1;
            dot11 = y1 * y1 + z1 * z1;

            // Compute barycentric coordinates
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);

            if (invDenom != Double.POSITIVE_INFINITY && invDenom != Double.NEGATIVE_INFINITY) {
                for (int i = (int) tl.getX(); i < (int) br.getX() + 1; i++) {
                    for (int j = (int) tl.getY(); j < (int) br.getY() + 1; j++) {
                        y = i - model.c.y;

                        z = j - model.c.z;

                        dot02 = y0 * y + z0 * z;
                        dot12 = y1 * y + z1 * z;

                        t0 = (dot11 * dot02 - dot01 * dot12) * invDenom;
                        t1 = (dot00 * dot12 - dot01 * dot02) * invDenom;


                        if (t0 <= 0 || t1 <= 0 || t0 + t1 > 1) {
                            continue;
                        } else {
                            dist = Math.abs(Configuration.observer.x - (model.a.x * t0 + model.b.x * t1 + model.c.x * (1 - t0 -
                                    t1)));
                            tempI = i + Configuration.IMAGE_WIDTH_HALF;
                            tempJ = j + Configuration.IMAGE_HEIGHT_HALF;
                            if (dist < zBuffer[tempI][tempJ]) {
                                zBuffer[tempI][tempJ] = dist;
                                pixelData[(tempI * Configuration.IMAGE_WIDTH + tempJ) * 3] = model.rC;
                                pixelData[(tempI * Configuration.IMAGE_WIDTH + tempJ) * 3 + 1] = model.gC;
                                pixelData[(tempI * Configuration.IMAGE_WIDTH + tempJ) * 3 + 2] = model.bC;
                            }
                        }
                    }
                }
            }
        }

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, Configuration.pixelRGBFormat,
                pixelData, 0, Configuration.IMAGE_WIDTH * 3);
    }

}
