
package cn.kuaipan.android.sdk.oauth;

import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.http.client.URIRedirector;
import cn.kuaipan.android.sdk.oauth.OAuthSession.SignType;
import cn.kuaipan.android.utils.UriUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class OAuthRedirector implements URIRedirector {

    private final OAuthSession mSession;

    public OAuthRedirector(OAuthSession session) {
        mSession = session;
    }

    @Override
    public boolean redirect(HttpContext context) {
        RequestWrapper wrapper = (RequestWrapper) context
                .getAttribute(ExecutionContext.HTTP_REQUEST);
        if (wrapper == null) {
            return true;
        }

        String methodStr = wrapper.getMethod();
        methodStr = methodStr == null ? null : methodStr.toUpperCase();
        HttpMethod method = null;
        try {
            method = HttpMethod.valueOf(methodStr);
        } catch (Exception e) {
            return false;
        }

        HttpRequest origReq = wrapper.getOriginal();
        Uri origUri;
        if (origReq instanceof HttpUriRequest) {
            origUri = Uri.parse(((HttpUriRequest) origReq).getURI().toString());
        } else {
            RequestLine requestLine = origReq.getRequestLine();
            origUri = Uri.parse(requestLine.getUri());
        }

        SignType type = mSession.testSignType(origUri, null);
        if (type == null || type == SignType.NONE) {
            return true;
        }

        List<NameValuePair> postParams = null;

        if (wrapper instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) wrapper)
                    .getEntity();
            if (entity != null) {
                try {
                    if (!entity.isRepeatable()) {
                        return false;
                    }

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    entity.writeTo(out);
                    String posts = new String(out.toByteArray());
                    Uri base = Uri.parse("http://hostname?" + posts);
                    postParams = UriUtils.getQuerys(base);
                } catch (Exception e) {
                    return false;
                }
            }
        }

        Uri uri = mSession.sign(null, method, origUri, postParams);
        URI destUri = URI.create(uri.toString());

        URI wrapperUri = wrapper.getURI();
        HttpHost host = null;
        try {
            host = new HttpHost(wrapperUri.getHost(), wrapperUri.getPort(),
                    wrapperUri.getScheme());
        } catch (Exception e) {
            // ignore
        }

        try {
            wrapper.setURI(URIUtils.rewriteURI(destUri, host));
            return true;
        } catch (URISyntaxException e) {
            return false;
        }

    }
}
