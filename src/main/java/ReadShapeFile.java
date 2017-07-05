import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by User on 2017/7/5.
 */
public class ReadShapeFile {

    private static HashMap<String, ArrayList<Road>> roadmap = new HashMap<String, ArrayList<Road>>();

    public static HashMap<String, ArrayList<Road>> readSHP(String path) {
        File file = new File(path);
        ShapefileDataStore sh_store = null;
        try {
            sh_store = new ShapefileDataStore(file.toURL());
            sh_store.setStringCharset(Charset.forName("GBK"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //to read Feature access;

        String typeName = sh_store.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> feature_source = null;

        try {
            feature_source = (FeatureSource<SimpleFeatureType, SimpleFeature>) sh_store.getFeatureSource(typeName);
        } catch (IOException e) {
            e.printStackTrace();
        }


        FeatureCollection<SimpleFeatureType, SimpleFeature> result = null;
        try {
            result = feature_source.getFeatures();
        } catch (IOException e) {
            e.printStackTrace();
        }


        FeatureIterator<SimpleFeature> itertor = result.features();
        try {
            while (itertor.hasNext()) {
                // Data Reader
                SimpleFeature feature = itertor.next();
                MultiLineString geom = (MultiLineString) feature.getDefaultGeometry();

                double width = Double.parseDouble(feature.getAttribute("Width").toString());
                double length = Double.parseDouble(feature.getAttribute("Length").toString());
                double negative_max_speed = Double.parseDouble(feature.getAttribute("NegMaxSped").toString());
                double positive_max_speed = Double.parseDouble(feature.getAttribute("PosMaxSped").toString());

                String roadID = feature.getAttribute("ID").toString();
                String roadName = feature.getAttribute("Name").toString();

                LineString geometry = (LineString) geom.getGeometryN(0);

                Road road = new Road(roadID, roadName, width, negative_max_speed, positive_max_speed, geometry);
                HashSet<Integer> lngs = new HashSet<Integer>();
                HashSet<Integer> lats = new HashSet<Integer>();
                for (int i = 0; i < geometry.getNumPoints(); i++) {
                    Point p = geometry.getPointN(i);
                    lngs.add(new Integer((int) (p.getX() * 100)));
                    lats.add(new Integer((int) (p.getY() * 100)));
                }

                String mapID;
                for (Integer x : lngs) {
                    for (Integer y : lats) {
                        mapID = x + "_" + y;
                        if (!roadmap.containsKey(mapID)) {
                            ArrayList<Road> roadList = new ArrayList<Road>();
                            roadList.add(road);
                            roadmap.put(mapID, roadList);
                        } else {
                            ArrayList<Road> roadList = roadmap.get(mapID);
                            roadList.add(road);           //  加入roadmap.put(mapID, roadList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            itertor.close();
            sh_store.dispose();
        }


        return roadmap;
    }

    public static void main(String[] args) {


        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = null;
        try {
            store = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();


            // Create a map content and add our shapefile to it
            MapContent map = new MapContent();
            map.setTitle("Quickstart");

            Style style = SLD.createSimpleStyle(featureSource.getSchema());
            org.geotools.map.Layer layer = new FeatureLayer(featureSource, style);
            map.addLayer(layer);

            // Now display the map
            JMapFrame.showMap(map);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int count=0;
        int count2=0;
        HashMap<String, ArrayList<Road>> roadmap12 = readSHP("E:\\北斗中心相关\\吴弘伟工作相关\\guandong_shp\\guangdong2014shp_new\\guangdong_polyline.shp");
        Iterator iter = roadmap12.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString();
            System.out.println("mapId: "+key+"---------------------------------------");
            ArrayList<Road> val =(ArrayList<Road>)entry.getValue();
            count2++;
            for(int i=0;i<val.size();i++){
                System.out.println(val.get(i).toString());
                count++;

            }
        }
        System.out.println("total:"+count);
        System.out.println("total:"+count2);

    }
}
