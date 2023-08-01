package custom.Fio;

import page.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public abstract class FileUtil {

    public static JFrame F = MainFrame.F;
    private static File LastSelectedFile = new File("./");

    public static File getFile() {
        return getFile("./");
    }

    public static File getFile(String defaultDirectoryPath) {
        StdFileChooser chooser = new StdFileChooser(F, LastSelectedFile);
        File defaultDirectory;
        if (defaultDirectoryPath != null && (defaultDirectory = new File(defaultDirectoryPath)).exists()) {
            chooser.setDefaultDirectory(defaultDirectory);
        }
        return saveUserSelect(chooser.selectFile());
    }

    public static File getFile(String path, String description, String... extensions) {
        StdFileChooser chooser = new StdFileChooser(F, LastSelectedFile,
                                                    new FileNameExtensionFilter(description, extensions));

        chooser.setDefaultDirectory(new File(path));
        return saveUserSelect(chooser.selectFile());
    }

    private static File saveUserSelect(File file) {
        if (file != null) {
            LastSelectedFile = file;
        }
        return file;
    }

}
