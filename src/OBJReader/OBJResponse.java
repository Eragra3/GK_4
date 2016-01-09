package OBJReader;

import Common.Models.TriangleModel;
import Common.Models.Vertex3DModel;

import java.util.ArrayList;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class OBJResponse {
    public ArrayList<Vertex3DModel> vertices;

    public ArrayList<TriangleModel> triangles;

    public OBJResponse() {
        vertices = new ArrayList<>(15000);
        triangles = new ArrayList<>(20000);
    }
}
