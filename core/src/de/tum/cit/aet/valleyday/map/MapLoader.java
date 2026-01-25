package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;

import java.util.*;

/**
 * Loads and parses map files.
 * format: x,y=objectType
 * 
 * Object types:
 * 0 = Fence
 * 1 = Debris
 * 2 = Entrance
 * 3 = Wildlife visitor
 * 4 = Exit
 * 5 = Fertilizer
 * 6 = Watering Can
 * 7 = Shovel
 * 9 = Garden Bed
 */
public class MapLoader {
    
    /**
     * Map object.
     */
    public static class MapObject {
        public final int x;
        public final int y;
        public final int type;
        
        public MapObject(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
    
    /**
     * Result of loading a map file.
     */
    public static class MapData {
        public final List<MapObject> objects;
        public final int entranceX;
        public final int entranceY;
        public final int minX;
        public final int maxX;
        public final int minY;
        public final int maxY;
        
        public MapData(List<MapObject> objects, int entranceX, int entranceY, int minX, int maxX, int minY, int maxY) {
            this.objects = objects;
            this.entranceX = entranceX;
            this.entranceY = entranceY;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
    
    /**
     * interface for map loading results.
     */
    public interface MapLoadCallback {
        void onMapLoaded(MapData mapData);
        void onCancellation();
        void onError(Exception exception);
    }
    
    /**
     * Loads map file using the file chooser with a callback.
     * @param fileChooser file chooser to use for selecting the file.
     * @param callback callback to handle the result (map loaded, cancellation, or error).
     */
    public static void loadMap(NativeFileChooser fileChooser, MapLoadCallback callback) {
        NativeFileChooserConfiguration config = new NativeFileChooserConfiguration();
        config.title = "Select map file";
        config.nameFilter = (dir, name) -> name.endsWith(".properties");
        
        // sets default directory, try maps folder first, then user home
        FileHandle mapsFolder = Gdx.files.local("maps");
        if (mapsFolder.exists() && mapsFolder.isDirectory()) {
            config.directory = mapsFolder;
        } else {
            // user home directory
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                config.directory = Gdx.files.absolute(userHome);
            }
        }
        
        fileChooser.chooseFile(config, new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                try {
                    MapData mapData = parseMapFile(file);
                    callback.onMapLoaded(mapData); // actions after file chosen
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
            //cancel
            @Override
            public void onCancellation() {
                callback.onCancellation();
            }

            //error
            @Override
            public void onError(Exception exception) {
                callback.onError(exception);
            }
        });
    }
    
    /**
     * Loads a map file from a specific file.
     * @param fileHandle The file handle to load from.
     * @return The loaded map data.
     */
    public static MapData loadMap(FileHandle fileHandle) {
        return parseMapFile(fileHandle);
    }
    
    /**
     * Reads and converts a map file and returns the map data.
     * Handles duplicate entrances/exits by replacing them with fences.
     * @param fileHandle The file handle to parse.
     * @return The parsed map data.
     */
    private static MapData parseMapFile(FileHandle fileHandle) {
        String content = fileHandle.readString(); // reads lines
        String[] lines = content.split("\n"); // splits by line (new line-new object)
        
        List<MapObject> objects = new ArrayList<>();
        List<MapObject> entrances = new ArrayList<>();
        List<MapObject> exits = new ArrayList<>();
        
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        // collects all objects and determine map bounds
        for (String line : lines) {
            line = line.trim(); // deletes extra space
            
            // skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // Parse format: x,y=type
            int equalsIndex = line.indexOf('='); // checks if there is "=" in the line
            if (equalsIndex == -1) {
                continue;
            }
            //gets the coordinates
            String positionPart = line.substring(0, equalsIndex).trim(); //trims everything before =
            //gets object type
            String typePart = line.substring(equalsIndex + 1).trim(); //trims everything after =
            
            // checks if "," exists and gets it's index
            int commaIndex = positionPart.indexOf(',');
            if (commaIndex == -1) {
                continue;
            }
            
            try {
                // gets x,y type values
                int x = Integer.parseInt(positionPart.substring(0, commaIndex).trim());
                int y = Integer.parseInt(positionPart.substring(commaIndex + 1).trim());
                int type = Integer.parseInt(typePart);

                // validates the type
                if ((type < 0 || type > 7) && type != 9) {
                    continue;
                }
                //creates new object
                MapObject obj = new MapObject(x, y, type);
                
                // updates map bounds
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);

                //adds objects based on type
                if (type == 2) { // entrance
                    entrances.add(obj);
                } else if (type == 4) { // exit
                    exits.add(obj);
                } else {
                    objects.add(obj);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        
        // handles duplicate entrances/exits
        // keeps only the first entrance
        if (!entrances.isEmpty()) {
            MapObject firstEntrance = entrances.get(0);
            objects.add(firstEntrance);
            
            // replaces duplicate entrances with fences
            for (int i = 1; i < entrances.size(); i++) {
                MapObject duplicate = entrances.get(i);
                objects.add(new MapObject(duplicate.x, duplicate.y, 0)); // Replace with fence
            }
        }
        
        // keep only the first exit
        if (!exits.isEmpty()) {
            MapObject firstExit = exits.get(0);
            objects.add(firstExit);
            
            // replaces duplicate exits with fences
            for (int i = 1; i < exits.size(); i++) {
                MapObject duplicate = exits.get(i);
                objects.add(new MapObject(duplicate.x, duplicate.y, 0)); // Replace with fence
            }
        }
        
        // determines players coordinates at start of game
        int entranceX = 1;
        int entranceY = 3;
        if (!entrances.isEmpty()) {
            entranceX = entrances.get(0).x;
            entranceY = entrances.get(0).y;
        }
        
        // if no objects were found, set default bounds
        if (minX == Integer.MAX_VALUE) {
            minX = 0;
            maxX = 20;
            minY = 0;
            maxY = 20;
        }
        // returns the mapa data
        return new MapData(objects, entranceX, entranceY, minX, maxX, minY, maxY);
    }
}
