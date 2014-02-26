
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.utils.OAuthTimeUtils;

import java.util.Date;
import java.util.Map;

public abstract class AbsKscData implements IKscData {

    public static String asStringOrThrow(Map<String, Object> dataMap, String key) {

        if (dataMap == null) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse.");
        }
        Object obj = dataMap.get(key);
        if (obj == null) {
            throw new IllegalArgumentException("Miss required data: " + key);
        }
        return obj.toString();
    }

    public static String asString(Map<String, Object> dataMap, String key) {

        if (dataMap == null) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse.");
        }
        Object obj = dataMap.get(key);

        return obj == null ? null : obj.toString();
    }

    public static Number asNumber(Object obj, Number defaultValue) {
        Number result = defaultValue;
        if (obj == null) {
            return result;
        }

        if (obj instanceof Number) {
            result = (Number) obj;
        } else {
            String value = obj.toString();
            try {
                result = Long.parseLong(value);
            } catch (NumberFormatException e) {
                result = Double.parseDouble(value);
            }
        }

        return result;
    }

    public static boolean asBoolean(Object obj, boolean defaultValue) {
        boolean result = defaultValue;
        if (obj == null) {
            return result;
        }

        if (obj instanceof Boolean) {
            result = (Boolean) obj;
        } else if (obj instanceof Number) {
            result = ((Number) obj).intValue() != 0;
        } else {
            String value = obj.toString();
            result = Boolean.parseBoolean(value);
        }

        return result;
    }

    public static synchronized Date asDate(Object obj, Date defaultValue) {
        if (obj == null) {
            return defaultValue;
        }

        return OAuthTimeUtils.parser(obj.toString(), defaultValue);
    }

}
