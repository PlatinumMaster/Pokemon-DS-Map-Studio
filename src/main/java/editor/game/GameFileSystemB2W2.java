package editor.game;

public class GameFileSystemB2W2 extends GameFileSystem {
    public final String exteriorBuildingPath, interiorBuildingPath, exteriorBuildingTilesets, interiorBuildingTilesets, areaDataPath;

    public GameFileSystemB2W2() {
        exteriorBuildingPath = getPath(new String[]{"data", "a", "2", "2", "5"});
        interiorBuildingPath = getPath(new String[]{"data", "a", "2", "2", "6"});
        exteriorBuildingTilesets = getPath(new String[]{"data", "a", "1", "7", "4"});
        interiorBuildingTilesets = getPath(new String[]{"data", "a", "1", "7", "5"});
        areaDataPath = getPath(new String[]{"data", "a", "0", "1", "3"});
    }
}
