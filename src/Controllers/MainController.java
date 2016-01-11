package Controllers;

import Common.Configuration;
import Common.Models.LightSourceModel;
import Common.Models.ObserverModel;
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
import javafx.scene.paint.Color;

import javax.swing.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    //

    //pixel writers
    PixelWriter pwAxisCamera;
    PixelWriter pwAxisXOY;
    PixelWriter pwAxisXOZ;
    PixelWriter pwAxisYOZ;
    //

    int[] axisPixelData = new int[Configuration.IMAGE_WIDTH * Configuration.IMAGE_HEIGHT];

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
        PixelWriter pwXOY = xoyGC.getPixelWriter();
        xoyRenderer = new XOYRenderer(vertices, triangles, pwXOY);

        cXOZ.setHeight(Configuration.IMAGE_HEIGHT);
        cXOZ.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext xozGC = cXOZ.getGraphicsContext2D();
        PixelWriter pwXOZ = xozGC.getPixelWriter();
        xozRenderer = new XOZRenderer(vertices, triangles, pwXOZ);

        cYOZ.setHeight(Configuration.IMAGE_HEIGHT);
        cYOZ.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext yozGC = cYOZ.getGraphicsContext2D();
        PixelWriter pwYOZ = yozGC.getPixelWriter();
        yozRenderer = new YOZRenderer(vertices, triangles, pwYOZ);

        cCamera.setHeight(Configuration.IMAGE_HEIGHT);
        cCamera.setWidth(Configuration.IMAGE_WIDTH);
        GraphicsContext cGC = cCamera.getGraphicsContext2D();
        PixelWriter pwC = cGC.getPixelWriter();
        perspectiveRenderer = new PerspectiveRenderer(vertices, triangles, pwC);
//        cCamera.setHeight(Configuration.IMAGE_HEIGHT);
//        cCamera.setWidth(Configuration.IMAGE_WIDTH);
//        GraphicsContext xoyGC = cXOY.getGraphicsContext2D();
//        PixelWriter xoyPW = xoyGC.getPixelWriter();
//        cameraRenderer = new XOYRenderer(vertices, triangles, xoyPW);

//        Thread perspectiveTask = new Thread(() -> {
//            while (!Thread.currentThread().isInterrupted())
//                perspectiveRenderer.render();
//        });
//        perspectiveTask.start();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
//            try {
                xoyRenderer.render();
                xozRenderer.render();
                yozRenderer.render();
                perspectiveRenderer.render();
//            } catch (Exception e){
//                e.printStackTrace();
//            }
        }, 1000, 100, TimeUnit.MILLISECONDS);
//        Timer timer = new Timer("Main Timer", true);
//        TimerTask renderersTask = new TimerTask() {
//            @Override
//            public void run() {
//                if (!Thread.currentThread().isInterrupted()) {
//                    xoyRenderer.render();
//                    xozRenderer.render();
//                    yozRenderer.render();
//                    perspectiveRenderer.render();
//                }
//            }
//        };
//        timer.scheduleAtFixedRate(renderersTask, 0L, 100L);

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

        pwAxisXOY = cXOYAxis.getGraphicsContext2D().getPixelWriter();

        pwAxisXOZ = cXOZAxis.getGraphicsContext2D().getPixelWriter();

        pwAxisYOZ = cYOZAxis.getGraphicsContext2D().getPixelWriter();

        pwAxisCamera = cCameraAxis.getGraphicsContext2D().getPixelWriter();
        cCameraAxis.getGraphicsContext2D().strokeText("Perspective", 10, 10);
//        cCameraAxis.getGraphicsContext2D().strokeText("X", 205, 15);
//        cCameraAxis.getGraphicsContext2D().strokeText("Y", 390, 215);
        renderOverlays();
    }

    @FXML
    public void readFile() {
        OBJReader reader = new OBJReader();
        reader.setScale(Configuration.objectScale);
        OBJResponse response = reader.readFile("sphere.obj");
        vertices = response.vertices;
        triangles = response.triangles;
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
        tFObserverX.setOnScroll(event -> {
                    if (event.getDeltaY() > 0)
                        tFObserverX.setText(String.valueOf(Double.parseDouble(tFObserverX.getText()) + 1));
                    else
                        tFObserverX.setText(String.valueOf(Double.parseDouble(tFObserverX.getText()) - 1));
                }
        );
        tFObserverX.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.observer.x = value;
                    labelObserverX.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                    renderOverlays();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFObserverY.setOnScroll(event -> {
                    if (event.getDeltaY() > 0)
                        tFObserverY.setText(String.valueOf(Double.parseDouble(tFObserverY.getText()) + 1));
                    else
                        tFObserverY.setText(String.valueOf(Double.parseDouble(tFObserverY.getText()) - 1));
                }
        );
        tFObserverY.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.observer.y = value;
                    labelObserverY.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                    renderOverlays();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
        tFObserverZ.setOnScroll(event -> {
                    if (event.getDeltaY() > 0)
                        tFObserverZ.setText(String.valueOf(Double.parseDouble(tFObserverZ.getText()) + 1));
                    else
                        tFObserverZ.setText(String.valueOf(Double.parseDouble(tFObserverZ.getText()) - 1));
                }
        );
        tFObserverZ.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0 && !newValue.equalsIgnoreCase("-"))
                try {
                    double value = Double.parseDouble(newValue.replace(",", "."));
                    Configuration.observer.z = value;
                    labelObserverZ.setText(String.valueOf(value));
                    perspectiveRenderer.reloadData();
                    renderOverlays();
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
                    renderOverlays();
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
                    renderOverlays();
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
                    renderOverlays();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Input is incorrect");
                }
        });
    }

    private final void renderOverlays() {
        ObserverModel o = Configuration.observer;
        LightSourceModel l = Configuration.lightSource;


        pwAxisXOY.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(), axisPixelData, 0,
                Configuration.IMAGE_WIDTH);
        GraphicsContext gcXOY = cXOYAxis.getGraphicsContext2D();
        gcXOY.setFill(Color.GREEN);
        gcXOY.fillRect(o.x + Configuration.IMAGE_WIDTH_HALF - 4, -o.y + Configuration.IMAGE_HEIGHT_HALF - 5, 9, 9);
        gcXOY.setFill(Color.VIOLET);
        gcXOY.fillRect(l.x + Configuration.IMAGE_WIDTH_HALF - 4, -l.y + Configuration.IMAGE_HEIGHT_HALF - 5, 9, 9);
        gcXOY.strokeText("XOY", 10, 10);
        gcXOY.strokeText("X", 390, 215);
        gcXOY.strokeText("Y", 205, 15);


        pwAxisXOZ.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(), axisPixelData, 0,
                Configuration.IMAGE_WIDTH);
        GraphicsContext gcXOZ = cXOZAxis.getGraphicsContext2D();
        gcXOZ.setFill(Color.GREEN);
        gcXOZ.fillRect(o.x + Configuration.IMAGE_WIDTH_HALF - 4, -o.z + Configuration.IMAGE_HEIGHT_HALF - 5, 9, 9);
        gcXOZ.setFill(Color.VIOLET);
        gcXOZ.fillRect(l.x + Configuration.IMAGE_WIDTH_HALF - 4, -l.z + Configuration.IMAGE_HEIGHT_HALF - 5, 9, 9);
        gcXOZ.strokeText("XOZ", 10, 10);
        gcXOZ.strokeText("Z", 205, 15);
        gcXOZ.strokeText("X", 390, 215);


        pwAxisYOZ.setPixels(0, 0, Configuration.IMAGE_WIDTH, Configuration.IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(), axisPixelData, 0,
                Configuration.IMAGE_WIDTH);
        GraphicsContext gcYOZ = cYOZAxis.getGraphicsContext2D();
        gcYOZ.setFill(Color.GREEN);
        gcYOZ.fillRect(o.y + Configuration.IMAGE_WIDTH_HALF - 4, -o.z + Configuration.IMAGE_HEIGHT_HALF - 5, 9, 9);
        gcYOZ.setFill(Color.VIOLET);
        gcYOZ.fillRect(l.y + Configuration.IMAGE_WIDTH_HALF - 4, -l.z + Configuration.IMAGE_HEIGHT_HALF - 5, 9, 9);
        gcYOZ.strokeText("YOZ", 10, 10);
        gcYOZ.strokeText("Z", 205, 15);
        gcYOZ.strokeText("Y", 390, 215);

    }
}
