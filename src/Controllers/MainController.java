package Controllers;

import Common.Configuration;
import Common.Models.TriangleModel;
import Common.Models.Vertex3DModel;
import OBJReader.OBJReader;
import OBJReader.OBJResponse;
import Renderers.PerspectiveRenderer;
import Renderers.XOYRenderer;
import Renderers.XOZRenderer;
import Renderers.YOZRenderer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import jdk.nashorn.internal.runtime.regexp.joni.Config;

import javax.swing.*;
import java.net.URL;
import java.util.*;
import java.util.Timer;

public class MainController implements Initializable {

    @FXML
    Canvas cXOY;
    @FXML
    Canvas cXOZ;
    @FXML
    Canvas cYOZ;
    @FXML
    Canvas cCamera;

    //axises
    @FXML
    Canvas cXOYAxis;
    @FXML
    Canvas cXOZAxis;
    @FXML
    Canvas cYOZAxis;
    @FXML
    Canvas cCameraAxis;

    int[] axisPixelData = new int[Configuration.IMAGE_WIDTH * Configuration.IMAGE_HEIGHT];
    //

    //CONTROLS
    @FXML
    CheckBox chBShowAxis;
    @FXML
    Slider sFOVX;
    @FXML
    Slider sFOVY;
    @FXML
    TextField tFObserverX;
    @FXML
    TextField tFObserverY;
    @FXML
    TextField tFObserverZ;
    @FXML
    TextField tFLightX;
    @FXML
    TextField tFLightY;
    @FXML
    TextField tFLightZ;
    //
    //PREVIEW VALUES
    @FXML
    Label labelAngleX;
    @FXML
    Label labelAngleY;
    @FXML
    Label labelObserverX;
    @FXML
    Label labelObserverY;
    @FXML
    Label labelObserverZ;
    @FXML
    Label labelLightX;
    @FXML
    Label labelLightY;
    @FXML
    Label labelLightZ;
    //

    ArrayList<Vertex3DModel> vertices;

    ArrayList<TriangleModel> triangles;

    //Renderers
    XOYRenderer xoyRenderer;
    XOZRenderer xozRenderer;
    YOZRenderer yozRenderer;
    PerspectiveRenderer perspectiveRenderer;

    public MainController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Locale.setDefault(Locale.ENGLISH);

        //bindings
        labelAngleX.setText(String.valueOf(Configuration.observer.angleX));
        labelAngleY.setText(String.valueOf(Configuration.observer.angleY));
        sFOVX.setValue(Configuration.observer.angleX);
        sFOVY.setValue(Configuration.observer.angleY);
        labelObserverX.setText(String.valueOf(Configuration.observer.x));
        labelObserverY.setText(String.valueOf(Configuration.observer.y));
        labelObserverZ.setText(String.valueOf(Configuration.observer.z));
        tFObserverX.setText(String.valueOf(Configuration.observer.x));
        tFObserverY.setText(String.valueOf(Configuration.observer.y));
        tFObserverZ.setText(String.valueOf(Configuration.observer.z));
        labelLightX.setText(String.valueOf(Configuration.lightSource.x));
        labelLightY.setText(String.valueOf(Configuration.lightSource.y));
        labelLightZ.setText(String.valueOf(Configuration.lightSource.z));
        tFLightX.setText(String.valueOf(Configuration.lightSource.x));
        tFLightY.setText(String.valueOf(Configuration.lightSource.y));
        tFLightZ.setText(String.valueOf(Configuration.lightSource.z));
        bindListeners();
        //

        readFile();

        cXOY.setHeight(Configuration.IMAGE_HEIGHT);
        cXOY.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext xoyGC = cXOY.getGraphicsContext2D();
        PixelWriter xoyPW = xoyGC.getPixelWriter();
        xoyRenderer = new XOYRenderer(vertices, triangles, xoyPW);

        cXOZ.setHeight(Configuration.IMAGE_HEIGHT);
        cXOZ.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext xozGC = cXOZ.getGraphicsContext2D();
        PixelWriter xozPW = xozGC.getPixelWriter();
        xozRenderer = new XOZRenderer(vertices, triangles, xozPW);

        cYOZ.setHeight(Configuration.IMAGE_HEIGHT);
        cYOZ.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext yozGC = cYOZ.getGraphicsContext2D();
        PixelWriter yozPW = yozGC.getPixelWriter();
        yozRenderer = new YOZRenderer(vertices, triangles, yozPW);

        cCamera.setHeight(Configuration.IMAGE_HEIGHT);
        cCamera.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext cGC = cCamera.getGraphicsContext2D();
        PixelWriter cPW = cGC.getPixelWriter();
        perspectiveRenderer = new PerspectiveRenderer(vertices, triangles, cPW);
//        cCamera.setHeight(Configuration.IMAGE_HEIGHT);
//        cCamera.setWidth(Configuration.IMAGE_WIDTH);
//        GraphicsContext xoyGC = cXOY.getGraphicsContext2D();
//        PixelWriter xoyPW = xoyGC.getPixelWriter();
//        cameraRenderer = new XOYRenderer(vertices, triangles, xoyPW);


        Timer timer = new Timer("Main Timer");

        TimerTask renderersTask = new TimerTask() {
            @Override
            public void run() {

                xoyRenderer.render();
                xozRenderer.render();
                yozRenderer.render();
                perspectiveRenderer.render();
            }
        };

        timer.scheduleAtFixedRate(renderersTask, 0L, 100L);

        for (int i = 0; i < Configuration.IMAGE_WIDTH; i += 2) {
            axisPixelData[i + 200 * Configuration.IMAGE_WIDTH] = 0xff0000ff;
            axisPixelData[200 + i * Configuration.IMAGE_WIDTH] = 0xff0000ff;
            axisPixelData[190 + i * Configuration.IMAGE_WIDTH] = 0xff00ff00;
            axisPixelData[210 + i * Configuration.IMAGE_WIDTH] = 0xff00ff00;
            axisPixelData[i + 190 * Configuration.IMAGE_WIDTH] = 0xff00ff00;
            axisPixelData[i + 210 * Configuration.IMAGE_WIDTH] = 0xff00ff00;
        }

//        AXISES
        cXOZAxis.setHeight(Configuration.IMAGE_HEIGHT);
        cXOZAxis.setWidth(Configuration.IMAGE_WIDTH);

        cXOYAxis.setHeight(Configuration.IMAGE_HEIGHT);
        cXOYAxis.setWidth(Configuration.IMAGE_WIDTH);

        cYOZAxis.setHeight(Configuration.IMAGE_HEIGHT);
        cYOZAxis.setWidth(Configuration.IMAGE_WIDTH);

        cCameraAxis.setHeight(Configuration.IMAGE_HEIGHT);
        cCameraAxis.setWidth(Configuration.IMAGE_WIDTH);

        paintAxis(cXOYAxis);
        cXOYAxis.getGraphicsContext2D().strokeText("XOY", 10, 10);
        cXOYAxis.getGraphicsContext2D().strokeText("X", 205, 15);
        cXOYAxis.getGraphicsContext2D().strokeText("Y", 390, 215);
        paintAxis(cXOZAxis);
        cXOZAxis.getGraphicsContext2D().strokeText("XOZ", 10, 10);
        cXOZAxis.getGraphicsContext2D().strokeText("X", 205, 15);
        cXOZAxis.getGraphicsContext2D().strokeText("Z", 390, 215);
        paintAxis(cYOZAxis);
        cYOZAxis.getGraphicsContext2D().strokeText("YOZ", 10, 10);
        cYOZAxis.getGraphicsContext2D().strokeText("Y", 205, 15);
        cYOZAxis.getGraphicsContext2D().strokeText("Z", 390, 215);
        paintAxis(cCameraAxis);
        cCameraAxis.getGraphicsContext2D().strokeText("Perspective", 10, 10);
//        cCameraAxis.getGraphicsContext2D().strokeText("X", 205, 15);
//        cCameraAxis.getGraphicsContext2D().strokeText("Y", 390, 215);
    }

    @FXML
    public void readFile() {
        OBJReader reader = new OBJReader();
        reader.setScale(Configuration.objectScale);
        OBJResponse response = reader.readFile("toruses.obj");
        vertices = response.vertices;
        triangles = response.triangles;
    }

    private void paintAxis(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();
        pw.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(), axisPixelData, 0,
                Configuration.IMAGE_WIDTH);
    }

    private void bindListeners() {
        chBShowAxis.selectedProperty().addListener((observable, oldValue, newValue) -> {
            cXOYAxis.setVisible(newValue);
            cXOZAxis.setVisible(newValue);
            cYOZAxis.setVisible(newValue);
            cCameraAxis.setVisible(newValue);
        });
        sFOVX.valueProperty().addListener((observable, oldValue, newValue) -> {
            Configuration.observer.angleX = newValue.doubleValue();
            perspectiveRenderer.reloadData();
            if (newValue.toString().length() < 4)
                labelAngleX.setText(newValue.toString());
            else
                labelAngleX.setText(newValue.toString().substring(0, 4));
        });
        sFOVY.valueProperty().addListener((observable, oldValue, newValue) -> {
            Configuration.observer.angleY = newValue.doubleValue();
            perspectiveRenderer.reloadData();
            if (newValue.toString().length() < 4)
                labelAngleY.setText(newValue.toString());
            else
                labelAngleY.setText(newValue.toString().substring(0, 4));
        });
        tFObserverX.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.observer.x = value;
                    labelObserverX.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFObserverY.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.observer.y = value;
                    labelObserverY.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFObserverZ.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.observer.z = value;
                    labelObserverZ.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFLightX.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.lightSource.x = value;
                    labelLightX.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFLightY.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.lightSource.y = value;
                    labelLightY.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFLightZ.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.lightSource.z = value;
                    labelLightZ.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
    }
}
