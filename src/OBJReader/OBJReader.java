package OBJReader;

import Common.Models.TriangleModel;
import Common.Models.Vector3DModel;
import Common.Models.Vertex3DModel;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class OBJReader implements IOBJReader {

    private double scale;

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public OBJResponse readFile(String fileName) {
        OBJResponse response = new OBJResponse();

        List<Vertex3DModel> vertices = response.vertices;
        List<TriangleModel> triangles = response.triangles;

        ArrayList<Vector3DModel> normalVectors = new ArrayList<>(20000);

        try {
            Scanner sc = new Scanner(new BufferedReader(new FileReader("src/Resources/" + fileName))).useLocale(Locale
                    .US);

            sc.useDelimiter(" |\r\n|,\r\n|//");

            while (sc.hasNext()) {
                String s = sc.next();

                if (s.equals("v")) {
                    vertices.add(
                            new Vertex3DModel(
                                    sc.nextDouble() * scale,
                                    sc.nextDouble() * scale,
                                    sc.nextDouble() * scale
                            )
                    );
                } else if (s.equals("vn")) {
                    normalVectors.add(new Vector3DModel(sc.nextDouble(), sc.nextDouble(), sc.nextDouble()));
                } else if (s.equals("f")) {
                    int vertexIndex1 = sc.nextInt();
                    int normalVectorIndex1 = (sc.nextInt() - 1) / 3;

                    int vertexIndex2 = sc.nextInt();
                    int normalVectorIndex2 = (sc.nextInt() - 1) / 3;

                    int vertexIndex3 = sc.nextInt();
                    int normalVectorIndex3 = (sc.nextInt() - 1) / 3;

                    triangles.add(new TriangleModel(
                            vertices.get(vertexIndex1 - 1),
                            vertices.get(vertexIndex2 - 1),
                            vertices.get(vertexIndex3 - 1),
                            normalVectors.get(normalVectorIndex1),
                            normalVectors.get(normalVectorIndex2),
                            normalVectors.get(normalVectorIndex3)
                    ));

                } else {
//                    throw new Exception("Not supported line" + sc.next());
                }
            }

            sc.close();

            //compute normal averages
            ArrayList<Vector3DModel> normals = new ArrayList<>(100);
            for (Vertex3DModel vertex : vertices) {
                for (TriangleModel triangle : triangles) {
                    if (triangle.a == vertex) {
                        normals.add(triangle.aNorm);
                    } else if (triangle.b == vertex) {
                        normals.add(triangle.bNorm);
                    } else if (triangle.c == vertex) {
                        normals.add(triangle.cNorm);
                    }
                }
                vertex.normX = normals.stream().mapToDouble(value -> value.x).average().getAsDouble();
                vertex.normY = normals.stream().mapToDouble(value -> value.y).average().getAsDouble();
                vertex.normZ = normals.stream().mapToDouble(value -> value.z).average().getAsDouble();

                vertex.normalize();
                normals.clear();
            }

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return response;
    }
}

