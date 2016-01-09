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
public class XOYRenderer {

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
        for (int i = 0; i < Helpers.zBufferInitial.length; i++)
            zBuffer[i] = Helpers.zBufferInitial[i].clone();

        System.arraycopy(Helpers.whitePixelsData, 0, pixelData, 0, Helpers.whitePixelsData.length);

        double x, x0, x1, y, y0, y1, det;
        int tempI, tempJ;

        //bounding box
        Point2D tl;
//        Point2D tr;
//        Point2D bl;
        Point2D br;


        for (TriangleModel model : triangles) {
            tl = new Point2D(Helpers.min(model.a.x, model.b.x, model.c.x), Helpers.min(model.a.y, model.b.y, model.c
                    .y));
//            tr = new Point2D(Helpers.max(model.a.x, model.b.x, model.c.x), Helpers.max(model.a.y, model.b.y, model.c
//                    .y));
//            bl = new Point2D(Helpers.min(model.a.x, model.b.x, model.c.x), Helpers.min(model.a.y, model.b.y, model.c
//                    .y));
            br = new Point2D(Helpers.max(model.a.x, model.b.x, model.c.x), Helpers.max(model.a.y, model.b.y, model.c
                    .y));

            x0 = model.a.x - model.c.x;
            x1 = model.b.x - model.c.x;

            y0 = model.a.y - model.c.y;
            y1 = model.b.y - model.c.y;

            det = x0 * y1 - y0 * x1;

            for (int i = (int) tl.getX(); i < (int) br.getX(); i++) {
                for (int j = (int) tl.getY(); j < (int) br.getY(); j++) {
                    x = i - model.c.x;

                    y = j - model.c.y;

                    double t0 = 0;
                    double t1 = 0;

                    // < 0.01
                    if ((Math.abs(det) != 0)) {
                        t0 = (x * y1 - y * x1) / det;
                        t1 = (x0 * y - y0 * x) / det;
                    } else {
                        continue;
                    }


                    if (t0 < 0 || t1 < 0 || t0 + t1 > 1) {
                        continue;
                    } else {
                        //x is distance to observer
                        x = Math.abs(Configuration.OBSERVER_Z - (model.a.z * t0 + model.b.z * t1 + model.c.z * (1 - t0 -
                                t1)));
                        tempI = i + Configuration.IMAGE_WIDTH_HALF;
                        tempJ = j + Configuration.IMAGE_HEIGHT_HALF;
                        if (x < zBuffer[tempI][tempJ]) {
                            zBuffer[tempI][tempJ] = x;
                            pixelData[(tempI * Configuration.IMAGE_WIDTH + tempJ) * 3] = model.rC;
                            pixelData[(tempI * Configuration.IMAGE_WIDTH + tempJ) * 3 + 1] = model.gC;
                            pixelData[(tempI * Configuration.IMAGE_WIDTH + tempJ) * 3 + 2] = model.bC;
                        }
                    }
                }
            }
        }

        pixelWriter.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, PixelFormat
                .getByteRgbInstance(), pixelData, 0, Configuration.IMAGE_WIDTH * 3);
    }
//        int c = 0;
//        for (int i = 0; i < Configuration.IMAGE_WIDTH; i++) {
//            for (int j = 0; j < Configuration.IMAGE_HEIGHT; j++) {
//                pixelWriter.setColor(i, j, Color.rgb(Byte.toUnsignedInt(pixelData[c]),
//                        Byte.toUnsignedInt(pixelData[c + 1]), Byte.toUnsignedInt(pixelData[c + 2])));
//                c += 3;
//            }
//        }

}
