
package cn.kuaipan.android.http.client;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class KscRedirectHandler extends DefaultRedirectHandler {

    @Override
    public URI getLocationURI(HttpResponse response, HttpContext context)
            throws ProtocolException {
        URI result = super.getLocationURI(response, context);

        @SuppressWarnings("unchecked")
        List<HttpMessage> messages = (List<HttpMessage>) context
                .getAttribute(KscHttpClient.KSC_MESSAGE_LIST);
        if (messages == null) {
            messages = new LinkedList<HttpMessage>();
            context.setAttribute(KscHttpClient.KSC_MESSAGE_LIST, messages);
        }
        messages.add(response);
        return result;
    }

}
