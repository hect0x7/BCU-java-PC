package custom.Fio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;


public abstract class AbstractFileSelector {
    private File target;

    public File getTarget() {
        return target;
    }

    public File selectFile() {
        // target = null;
        JFileChooser chooser = new JFileChooser(getDefaultDirectory());
        chooser.setDialogTitle(setTitle());
        chooser.setSelectedFile(getDefaultFile());
        FileNameExtensionFilter filter = getFileNameExtensionFilter();
        chooser.addChoosableFileFilter(filter);

        while (chooser.showDialog(getParentFrame(), "select") != JFileChooser.CANCEL_OPTION) {
            target = chooser.getSelectedFile();

            if (filter == null) {
                return target;
            } else {
                if (filter.accept(target)) {
                    target = chooser.getSelectedFile();
                    return target;
                } else {
                    boolean isBreak = handleWhenUnFitted(chooser);
                    if (isBreak) {
                        break;
                    }
                }
            }
        }

        return null;
    }


    private boolean handleWhenUnFitted(JFileChooser chooser) {
        String msg = "Please select " + getFileNameExtensionFilter().getDescription();
        JOptionPane.showMessageDialog(getParentFrame(), msg, "confirm", JOptionPane.ERROR_MESSAGE);
        chooser.setSelectedFile(null);
        return false;
    }

    public File getDefaultDirectory() {
        return new File("./");
    }

    public String setTitle() {
        return this.getClass().getSimpleName();
    }

    public abstract FileNameExtensionFilter getFileNameExtensionFilter();

    public File getDefaultFile() {
        return null;
    }

    public abstract Component getParentFrame();

}
