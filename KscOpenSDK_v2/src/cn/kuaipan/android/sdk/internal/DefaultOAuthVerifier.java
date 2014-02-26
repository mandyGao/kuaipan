
package cn.kuaipan.android.sdk.internal;

import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.model.ResultMsg;
import cn.kuaipan.android.utils.IObtainable;

import org.apache.http.HttpStatus;

import java.util.Map;

public class DefaultOAuthVerifier extends ResponseVerifier {

    @Override
    public void verify(KscHttpResponse response, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException {
        int statusCode = response.getStatusCode();

        if (statusCode != HttpStatus.SC_OK
                && !(appendMode && statusCode == HttpStatus.SC_PARTIAL_CONTENT)) {
            String msg = null;
            Map<String, Object> dataMap = null;
            try {
                dataMap = ApiDataHelper.contentToMap(response);
                ResultMsg data = ApiDataHelper.parser(response, dataMap,
                        ResultMsg.class);
                msg = data == null ? null : data.msg;
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                // ignore
            } finally {
                if (dataMap != null && dataMap instanceof IObtainable) {
                    ((IObtainable) dataMap).recycle();
                }
            }
            throwServerError(statusCode, msg, response);
        }
    }

}
