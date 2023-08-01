package utilpc.stage2csv;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;


public class FileSelectorImpl extends AbstractFileSelector {
    private final Component parent;
    private FileNameExtensionFilter extensionFilter;
    private File defaultFile;
    private File defaultDirectory;

    public FileSelectorImpl(JFrame f, File defaultFile, FileNameExtensionFilter filter) {
        this(f, defaultFile);
        extensionFilter = filter;
    }

    public FileSelectorImpl(Component parent) {
        this.parent = parent;
    }

    public FileSelectorImpl(Component parent, File defaultFile) {
        this(parent);
        this.defaultFile = defaultFile;
        this.defaultDirectory = defaultFile == null ? null : defaultFile.getParentFile();
    }

    @Override
    public String setTitle() {
        return "Choose File";
    }

    @Override
    public File getDefaultDirectory() {
        return defaultDirectory;
    }

    public void setDefaultDirectory(File defaultDirectory) {
        this.defaultDirectory = defaultDirectory;
    }

    @Override
    public FileNameExtensionFilter getFileNameExtensionFilter() {
        return extensionFilter;
    }

    @Override
    public Component getParentFrame() {
        return parent;
    }

    @Override
    public File getDefaultFile() {
        return defaultFile == null ? getTarget() : defaultFile;
    }
}
