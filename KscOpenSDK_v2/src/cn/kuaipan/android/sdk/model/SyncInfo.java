
package cn.kuaipan.android.sdk.model;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SyncInfo {

    public static enum CursorType {
        file, share;

        public static CursorType parser(String value) {
            if ("opVer".equalsIgnoreCase(value)) {
                return file;
            } else if ("shareVer".equalsIgnoreCase(value)) {
                return share;
            } else {
                try {
                    return CursorType.valueOf(value);
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }

    private static class ParserHandler extends DefaultHandler {
        private final SyncInfo result = new SyncInfo();

        private int depth = 0;
        private String currentLabel;

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
            if (TextUtils.isEmpty(currentLabel) || depth != 2) {
                return;
            }

            String value = new String(ch, start, length);
            if ("deviceId".equalsIgnoreCase(currentLabel)) {
                result.deviceId = value;
            } else {
                CursorType type = CursorType.parser(currentLabel);
                if (type != null) {
                    result.addCursor(type, value);
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            currentLabel = localName;
            depth++;
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            depth--;
        }

        public SyncInfo getResult() {
            return result;
        }

    }

    public static SyncInfo parser(InputStream in) throws SAXException,
            IOException, ParserConfigurationException {
        if (null == in) {
            return null;
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        ParserHandler parserHandle = new ParserHandler();
        parser.parse(in, parserHandle);

        return parserHandle.getResult();
    }

    private String deviceId;
    private final HashMap<CursorType, String> mCursors;

    private SyncInfo() {
        super();
        mCursors = new HashMap<CursorType, String>();
    }

    void addCursor(CursorType type, String cursor) {
        mCursors.put(type, cursor);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public CursorType[] getUpdatedCursor() {
        return mCursors.keySet().toArray(new CursorType[mCursors.size()]);
    }

    public String getCursor(CursorType type) {
        return mCursors.get(type);
    }
}
