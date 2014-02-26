package org.sky.base.utils;

import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class XmlUtils {
  public static String buildXml(XMLObject root) {
    if (root == null) {
      return null;
    }

    String result = null;

    StringWriter writer = new StringWriter();
    XmlSerializer serializer = Xml.newSerializer();
    try {
      serializer.setOutput(writer);

      serializer.startDocument(HTTP.UTF_8, null);
      append(serializer, root);
      serializer.endDocument();

      result = writer.toString();
    } catch (Exception e) {
      Log.w("TAG", "Failed build xml.");
    }

    return result;
  }

  private static void append(XmlSerializer serializer, XMLObject root)
      throws IllegalArgumentException, IllegalStateException, IOException {
    if (root == null) {
      return;
    }

    String tag = root.getTag();
    serializer.startTag(null, tag);
    Map<String, String> attrs = root.getAttributes();
    if (attrs != null && !attrs.isEmpty()) {
      Set<Entry<String, String>> entries = attrs.entrySet();
      for (Entry<String, String> entry : entries) {
        serializer.attribute(null, entry.getKey(), entry.getValue());
      }
    }

    List<? extends Object> values = root.getValues();
    if (values != null && !values.isEmpty()) {
      StringBuilder builder = new StringBuilder();

      for (Object object : values) {
        if (object instanceof XMLObject) {
          append(serializer, (XMLObject) object);
        } else {
          builder.append(object);
        }
      }

      if (builder.length() > 0) {
        serializer.text(builder.toString());
      }
    }

    serializer.endTag(null, tag);
  }

  public static interface XMLObject {
    String getTag();

    List<? extends Object> getValues();

    Map<String, String> getAttributes();
  }

  public static class SimpleXMLObject implements XMLObject {
    private final String tag;
    private final String text;

    public SimpleXMLObject(String tag, String text) {
      super();
      this.tag = tag;
      this.text = text;
    }

    @Override
    public String getTag() {
      return tag;
    }

    @Override
    public List<String> getValues() {
      List<String> result = new ArrayList<String>(1);
      result.add(text);
      return result;
    }

    @Override
    public Map<String, String> getAttributes() {
      return null;
    }

  }

  public static class XLiveXMLObject implements XMLObject {
    private static final String ROOT_TAG = "xLive";

    private List<SimpleXMLObject> values;

    public XLiveXMLObject() {
      values = new ArrayList<SimpleXMLObject>();
    }

    public void addParam(String key, String vaule) {
      values.add(new SimpleXMLObject(key, vaule));
    }

    @Override
    public String getTag() {
      return ROOT_TAG;
    }

    @Override
    public List<SimpleXMLObject> getValues() {
      return values;
    }

    @Override
    public Map<String, String> getAttributes() {
      return null;
    }

  }

}
