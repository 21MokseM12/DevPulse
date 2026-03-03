package backend.academy.scrapper.db.query;

import backend.academy.scrapper.utils.FileResourceUtils;

public interface ScrapperQueryHolder {
    String query();

    default String readQuery(String path) {return FileResourceUtils.readToString(path);}
}
