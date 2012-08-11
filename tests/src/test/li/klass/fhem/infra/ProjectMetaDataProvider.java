package li.klass.fhem.infra;

import java.io.File;

public class ProjectMetaDataProvider {
    public static String getProjectRoot() {
        String userDirectory = System.getProperty("user.dir");
        File userDirectorySrc = new File(userDirectory + File.separator + "src");
        if (userDirectorySrc.exists()) {
            return userDirectory + "/..";
        }
        return userDirectory;
    }
}
