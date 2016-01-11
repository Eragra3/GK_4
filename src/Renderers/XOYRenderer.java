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
public class XOYRenderer implements IRenderer {

    ArrayList<Vertex3DModel> vertices;

    ArrayList<TriangleModel> triangles;

    PixelWriter pixelWriter;


    byte[] pixelData = new byte[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH * 3];

    double[][] zBuffer = new double[Configuration.IMAGE_WIDTH][Configuration.IMAGE_HEIGHT];

    public XOYRenderer(ArrayList<Vertex3DModel> vertices, ArrayList<TriangleModel> triangles, PixelWriter pixelWriter) {
        this.vertices = vertices;
        this.triangles = triangles;
        this.pixelWriter = pixelWriter;
    }

    public void render() {
        //todo for, not clone
        for (int i = 0; i < Helpers.zBufferInitial.length; i++)
            zBuffer[i] = Helpers.zBufferInitial[i].clone();

        System.arraycopy(Helpers.whitePixelsData, 0, pixelData, 0, Helpers.whitePixelsData.length);

        double x, x0, x1, y, y0, y1, t0, t1, dist;
        double dot00, dot01, dot02, dot11, dot12, invDenom;
        int tempI, tempJ;

        //bounding box
        Point2D tl;
        Point2D br;


        for (TriangleModel model : triangles) {
            tl = new Point2D(Helpers.min(model.a.x, model.b.x, model.c.x), Helpers.min(model.a.y, model.b.y, model.c.y));
            br = new Point2D(Helpers.max(model.a.x, model.b.x, model.c.x), Helpers.max(model.a.y, model.b.y, model.c.y));

            x0 = model.a.x - model.c.x;
            x1 = model.b.x - model.c.x;

            y0 = model.a.y - model.c.y;
            y1 = model.b.y - model.c.y;

            // Compute dot products
            dot00 = x0 * x0 + y0 * y0;
            dot01 = x0 * x1 + y0 * y1;
            dot11 = x1 * x1 + y1 * y1;

            // Compute barycentric coordinates
            invDenom = 1 / (dot00 * dot11 - dot01 * dot01);

            if (invDenom != Double.POSITIVE_INFINITY && invDenom != Double.NEGATIVE_INFINITY) {
                for (int i = (int) tl.getX(); i < (int) br.getX() + 1; i++) {
                    for (int j = (int) tl.getY(); j < (int) br.getY() + 1; j++) {
                        x = i - model.c.x;

                        y = j - model.c.y;

                        dot02 = x0 * x + y0 * y;
                        dot12 = x1 * x + y1 * y;

                        t0 = (dot11 * dot02 - dot01 * dot12) * invDenom;
                        t1 = (dot00 * dot12 - dot01 * dot02) * invDenom;


                        if (t0 <= 0 || t1 <= 0 || t0 + t1 > 1) {
                            continue;
                        } else {
                            dist = Math.abs(Configuration.observer.z - (model.a.z * t0 + model.b.z * t1 + model.c.z * (1 - t0 -
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

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, Configuration.pixelRGBFormat, pixelData, 0,
                Configuration.IMAGE_WIDTH * 3);
    }

}
