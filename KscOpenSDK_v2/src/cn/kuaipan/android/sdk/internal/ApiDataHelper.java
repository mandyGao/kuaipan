
package cn.kuaipan.android.sdk.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.channels.ClosedByInterruptException;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.json.JSONException;
import org.sky.base.json.MalformedJsonException;

import android.util.Log;
import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.ErrorHelper;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.model.IKscData;
import cn.kuaipan.android.utils.JsonUtils;

public class ApiDataHelper {

    private static final String LOG_TAG = "ApiDataHelper";

    /**
     * Return a obtainable Map contain data if success parser.
     * 
     * @param response
     * @return A obtainable Map contain data if success parser
     * @throws KscException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> contentToMap(KscHttpResponse response)
            throws KscException, InterruptedException {
        InputStream in = null;
        try {
            in = response.getContent();
            if (in == null) {
                throw new KscException(ErrorCode.DATA_IS_NOT_JSON,
                        response.dump());
            }

            Map<String, Object> result = (Map<String, Object>) JsonUtils
                    .parser(in);
            if (result == null || result.isEmpty()) {
                throw new KscException(ErrorCode.DATA_TYPE_INVALID,
                        response.dump());
            }

            return result;
        } catch (MalformedJsonException e) {
            throw new KscException(ErrorCode.DATA_IS_NOT_JSON, response.dump(),
                    e);
        } catch (JSONException e) {
            throw new KscException(ErrorCode.DATA_IS_NOT_JSON, response.dump(),
                    e);
        } catch (ClosedByInterruptException e) {
            ErrorHelper.handleInterruptException(e);
            return null;
        } catch (IOException e) {
            throw KscException.newInstance(e, response.dump());
        } catch (ClassCastException e) {
            throw new KscException(ErrorCode.DATA_TYPE_INVALID,
                    response.dump(), e);
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public static <T extends IKscData> T parser(KscHttpResponse origResponse,
            Map<String, Object> map, Class<T> resultClass, String... requires)
            throws KscException {
        try {
            IKscData.Parser<T> parser = getParser(resultClass);
            return parser.parserMap(map, requires);
        } catch (DataFormatException e) {
            throw new KscException(ErrorCode.DATA_TYPE_INVALID,
                    origResponse.dump(), e);
        } catch (ClassCastException e) {
            throw new KscException(ErrorCode.DATA_TYPE_INVALID,
                    origResponse.dump(), e);
        } catch (IllegalArgumentException e) {
            throw new KscException(ErrorCode.DATA_TYPE_INVALID,
                    origResponse.dump(), e);
        } catch (KscRuntimeException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new KscRuntimeException(ErrorCode.UNKNOW_ERR,
                    origResponse.dump(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends IKscData> IKscData.Parser<T> getParser(
            Class<T> dataClass) {
        IKscData.Parser<T> parser = null;
        try {
            Field f = dataClass.getField(IKscData.PARSER_NAME);
            parser = (IKscData.Parser<T>) f.get(null);
        } catch (IllegalAccessException e) {
            Log.e(LOG_TAG, "Parser Class not found in " + dataClass, e);
            throw new KscRuntimeException(ErrorCode.DATA_MISS_PARSER,
                    "IllegalAccessException when parser: " + dataClass, e);
        } catch (ClassCastException e) {
            throw new KscRuntimeException(ErrorCode.BAD_DATA_PARSER,
                    "IKscData protocol requires a IKscData.Creator object called "
                            + " PARSER on class " + dataClass);
        } catch (NoSuchFieldException e) {
            throw new KscRuntimeException(ErrorCode.BAD_DATA_PARSER,
                    "IKscData protocol requires a IKscData.Creator object called "
                            + " PARSER on class " + dataClass);
        }
        if (parser == null) {
            throw new KscRuntimeException(ErrorCode.BAD_DATA_PARSER,
                    "IKscData protocol requires a IKscData.Creator object called "
                            + " PARSER on class " + dataClass);
        }
        return parser;
    }
}
