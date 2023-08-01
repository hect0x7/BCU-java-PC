package custom.Fio;

import java.io.File;

public interface FileChooser<T> {
    File selectFile();

    File save(T t);

    T open();

}
