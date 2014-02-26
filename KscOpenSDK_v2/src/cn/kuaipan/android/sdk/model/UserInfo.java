
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class UserInfo {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_CTIME = "ctime";
    public static final String KEY_MOBILE = "mobile";
    public static final String KEY_USERID = "userId";
    public static final String KEY_ETIME = "etime";
    public static final String KEY_REGTIME = "userregtime";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_NICKNAME = "nickName";
    public static final String KEY_EMAIL = "email";

    private Bundle values = new Bundle();

    public boolean isValid() {
        return values.containsKey(KEY_USERNAME)
                && values.containsKey(KEY_USERID)
                && values.containsKey(KEY_TOKEN);
    }

    public String getVaule(String key) {
        return values.getString(key);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public static UserInfo parser(InputStream in) throws SAXException,
            IOException, ParserConfigurationException, KscException {
        if (null == in) {
            return null;
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        ParserHandler parserHandle = new ParserHandler();
        parser.parse(in, parserHandle);
        UserInfo result = parserHandle.getResult();

        if (result == null || !result.isValid()) {
            throw new KscException(ErrorCode.DATA_UNSCHEDULE,
                    result == null ? null : result.values.toString());
        }

        return result;
    }

    private static class ParserHandler extends DefaultHandler {
        private final UserInfo result = new UserInfo();

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

            result.values.putString(currentLabel, value);
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

        public UserInfo getResult() {
            return result;
        }

    }
}
