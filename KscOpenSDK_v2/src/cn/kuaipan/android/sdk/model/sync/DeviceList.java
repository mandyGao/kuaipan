
package cn.kuaipan.android.sdk.model.sync;

import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.model.AbsKscData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class DeviceList extends AbsKscData {
    private static final String SOURCES = "DeviceList";

    public static final Parser<DeviceList> PARSER = new Parser<DeviceList>() {

        @Override
        public DeviceList parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException, KscException {
            try {
                return new DeviceList(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private ArrayList<DeviceData> mList;

    @SuppressWarnings("unchecked")
    public DeviceList(Map<String, Object> map) {
        mList = new ArrayList<DeviceData>();

        List<Object> sources = (List<Object>) map.get(SOURCES);

        for (Object source : sources) {
            Map<String, Object> item = (Map<String, Object>) source;
            DeviceData device = DeviceData.parser(item);
            if (device != null) {
                mList.add(device);
            }
        }
    }

    public ArrayList<DeviceData> getList() {
        return mList;
    }
}
