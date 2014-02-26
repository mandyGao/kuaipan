
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.sdk.exception.KscException;

import java.util.Map;
import java.util.zip.DataFormatException;

public interface IKscData {
    public static final String PARSER_NAME = "PARSER";

    public interface Parser<T extends IKscData> {
        T parserMap(Map<String, Object> map, String... requireds)
                throws DataFormatException, KscException;
    }
}
