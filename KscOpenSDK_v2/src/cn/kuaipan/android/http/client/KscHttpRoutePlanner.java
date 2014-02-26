
package cn.kuaipan.android.http.client;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.params.AbstractHttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.sky.base.utils.NetworkHelpers;

public class KscHttpRoutePlanner extends DefaultHttpRoutePlanner {

    public KscHttpRoutePlanner(SchemeRegistry schreg) {
        super(schreg);
    }

    @Override
    public HttpRoute determineRoute(HttpHost target, HttpRequest request,
            HttpContext context) throws HttpException {

        HttpHost apnProxy = NetworkHelpers.getCurrentProxy();
        if (apnProxy != null) {
            KscHttpParams params = new KscHttpParams(request.getParams());
            ConnRouteParams.setDefaultProxy(params, apnProxy);
            request.setParams(params);
        }

        return super.determineRoute(target, request, context);
    }

    private class KscHttpParams extends AbstractHttpParams {
        private final HttpParams mOrgParams;
        private final HttpParams mExtParams;

        public KscHttpParams(HttpParams orgParams) {
            mOrgParams = orgParams;
            mExtParams = new BasicHttpParams();
        }

        private KscHttpParams(HttpParams orgParams, HttpParams extParams) {
            mOrgParams = orgParams;
            mExtParams = extParams;
        }

        @Override
        public Object getParameter(String name) {
            Object obj = mExtParams.getParameter(name);
            if (obj == null) {
                obj = mOrgParams.getParameter(name);
            }

            return obj;
        }

        @Override
        public HttpParams setParameter(String name, Object value) {
            mExtParams.setParameter(name, value);
            return this;
        }

        @Override
        public HttpParams copy() {
            KscHttpParams copyed = new KscHttpParams(mOrgParams,
                    mExtParams.copy());
            return copyed;
        }

        @Override
        public boolean removeParameter(String name) {
            boolean result = mExtParams.removeParameter(name);
            if (!result) {
                try {
                    result = mOrgParams.removeParameter(name);
                } catch (Exception e) {
                    // ignore
                }
            }
            return result;
        }
    }
}
