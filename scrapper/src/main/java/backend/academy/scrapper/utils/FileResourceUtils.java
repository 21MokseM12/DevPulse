package backend.academy.scrapper.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class FileResourceUtils {

    public String readToString(String path) {
        try {
            return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка чтения файла из ресурса: " + path, e);
        }
    }
}
