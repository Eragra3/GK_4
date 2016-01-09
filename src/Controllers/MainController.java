package Controllers;

import Common.Configuration;
import Common.Models.TriangleModel;
import Common.Models.Vertex3DModel;
import OBJReader.OBJReader;
import OBJReader.OBJResponse;
import Renderers.XOYRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class MainController implements Initializable {

    @FXML
    Canvas cXOY;
    @FXML
    ImageView ivXOZ;
    @FXML
    ImageView ivYOZ;
    @FXML
    ImageView ivCamera;

    ArrayList<Vertex3DModel> vertices;

    ArrayList<TriangleModel> triangles;

    //Renderers
    XOYRenderer xoyRenderer;

    public MainController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cXOY.setHeight(Configuration.IMAGE_HEIGHT);
        cXOY.setWidth(Configuration.IMAGE_WIDTH);

        //test
        BufferedImage bf1 = new BufferedImage(Configuration.IMAGE_WIDTH, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bf1.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, bf1.getWidth(), bf1.getHeight());
        g.dispose();

        BufferedImage bf2 = new BufferedImage(Configuration.IMAGE_WIDTH, 400, BufferedImage.TYPE_INT_RGB);
        g = bf2.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, bf1.getWidth(), bf1.getHeight());
        g.dispose();

        BufferedImage bf3 = new BufferedImage(Configuration.IMAGE_WIDTH, 400, BufferedImage.TYPE_INT_RGB);
        g = bf3.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, bf1.getWidth(), bf1.getHeight());
        g.dispose();

        BufferedImage bf4 = new BufferedImage(Configuration.IMAGE_WIDTH, 400, BufferedImage.TYPE_INT_RGB);
        g = bf4.createGraphics();
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, bf1.getWidth(), bf1.getHeight());
        g.dispose();

//        cXOY.setImage(SwingFXUtils.toFXImage(bf1, null));
        ivXOZ.setImage(SwingFXUtils.toFXImage(bf2, null));
        ivYOZ.setImage(SwingFXUtils.toFXImage(bf3, null));
        ivCamera.setImage(SwingFXUtils.toFXImage(bf4, null));

        readFile();

        GraphicsContext xoyGC = cXOY.getGraphicsContext2D();
        PixelWriter xoyPW = xoyGC.getPixelWriter();

        xoyRenderer = new XOYRenderer(vertices, triangles, xoyPW);

        Timer timer = new Timer("Main Timer");

        TimerTask xoyTask = new TimerTask() {
            @Override
            public void run() {
                xoyRenderer.render();
            }
        };

        timer.scheduleAtFixedRate(xoyTask, 0L, 1000L * 2L);

//        byte[] pixelsData = new byte[Configuration.IMAGE_WIDTH * Configuration.IMAGE_HEIGHT * 3];
//        for (int i = 0; i < pixelsData.length; i += 3) {
//            if (i % 2 == 0) {
//                pixelsData[i] = (byte) 0;
//                pixelsData[i + 1] = (byte) 0;
//                pixelsData[i + 2] = (byte) 0;
//            } else {
//                pixelsData[i] = (byte) 255;
//                pixelsData[i + 1] = (byte) 255;
//                pixelsData[i + 2] = (byte) 255;
//            }
//        }
//
//        xoyPW.setPixels(0, 0, Configuration.IMAGE_HEIGHT, Configuration.IMAGE_WIDTH, PixelFormat.getByteRgbInstance(),
//                Helpers.whitePixelsData, 0, Configuration.IMAGE_WIDTH * 3);
    }

    @FXML
    public void readFile() {
        OBJReader reader = new OBJReader();
        OBJResponse response = reader.readFile("hexagonal_prism.obj");
        vertices = response.vertices;
        triangles = response.triangles;
    }

}
