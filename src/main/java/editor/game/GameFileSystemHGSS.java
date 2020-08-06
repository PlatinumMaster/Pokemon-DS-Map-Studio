/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package editor.game;

import java.io.File;

/**
 *
 * @author Trifindo
 */
public class GameFileSystemHGSS extends GameFileSystem {

    private final String buildModelPath;//
    private final String buildModelMatshpPath;//
    private final String buildModelAnimeListPath;//
    private final String buildModelAnimePath;
    private final String areaDataPath;
    private final String areaBuildTilesetPath;
    private final String areaBuildModelPath;//
    
    private final String buildModelRoomPath;
    private final String buildModelRoomMatshpPath;//
    private final String buildModelRoomAnimeListPath;//

    public GameFileSystemHGSS() {
        buildModelPath = getPath(new String[]{"data", "a", "0", "4", "0"});
        buildModelMatshpPath = getPath(new String[]{"data", "fielddata", "build_model", "bm_field_matshp.dat"});
        buildModelAnimeListPath = getPath(new String[]{"data", "a", "1", "0", "7"});
        buildModelAnimePath = getPath(new String[]{"data", "a", "1", "0", "6"});
        areaDataPath = getPath(new String[]{"data", "a", "0", "4", "2"});
        areaBuildTilesetPath = getPath(new String[]{"data", "a", "0", "7", "0"});
        areaBuildModelPath = getPath(new String[]{"data", "a", "0", "4", "3"});
        
        buildModelRoomPath = getPath(new String[]{"data", "a", "1", "4", "8"});
        buildModelRoomMatshpPath = getPath(new String[]{"data", "fielddata", "build_model", "bm_room_matshp.dat"});
        buildModelRoomAnimeListPath = getPath(new String[]{"data", "a", "1", "0", "8"});
    }

    public String getAreaDataPath() {
        return areaDataPath;
    }

    public String getAreaBuildModelPath() {
        return areaBuildModelPath;
    }

    public String getAreaBuildTilesetPath() {
        return areaBuildTilesetPath;
    }

    public String getBuildModelPath() {
        return buildModelPath;
    }

    public String getBuildModelMatshpPath() {
        return buildModelMatshpPath;
    }

    public String getBuildModelAnimePath() {
        return buildModelAnimePath;
    }

    public String getBuildModelAnimeListPath() {
        return buildModelAnimeListPath;
    }

    public String getBuildModelRoomAnimeListPath() {
        return buildModelRoomAnimeListPath;
    }

    public String getBuildModelRoomMatshpPath() {
        return buildModelRoomMatshpPath;
    }

    public String getBuildModelRoomPath() {
        return buildModelRoomPath;
    }
    
    

}