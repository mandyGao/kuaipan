package cn.kuaipan.android.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.json.JSONException;
import org.sky.base.json.JsonReader;
import org.sky.base.json.JsonToken;

import android.test.AndroidTestCase;
import android.util.Log;

public class JsonUtils extends AndroidTestCase {

  public void testToken() throws Exception {
    Reader in = new StringReader("[null],[]");
    Object result = parser(in);
    Log.d("TEST", "Result:" + result);
  }

  public static Object parserArray(Reader in) throws IOException,
      JSONException {
    JsonReader reader = new JsonReader(in);
    Object result = parserArray(reader);
    if (reader.peek() != JsonToken.END_DOCUMENT) {
      throw new JSONException("Document not end of EOF");
    }
    return result;
  }

  public static Object parserObject(Reader in) throws IOException,
      JSONException {
    JsonReader reader = new JsonReader(in);
    Object result = parserObject(reader);
    if (reader.peek() != JsonToken.END_DOCUMENT) {
      throw new JSONException("Document not end of EOF");
    }
    return result;
  }

  public static Object parser(InputStream in) throws IOException,
      JSONException {
    JsonReader reader = new JsonReader(new InputStreamReader(in));
    Object result = parser(reader);
    if (reader.peek() != JsonToken.END_DOCUMENT) {
      throw new JSONException("Document not end of EOF");
    }
    return result;
  }

  public static Object parser(Reader in) throws IOException, JSONException {
    JsonReader reader = new JsonReader(in);
    Object result = parser(reader);
    if (reader.peek() != JsonToken.END_DOCUMENT) {
      throw new JSONException("Document not end of EOF");
    }
    return result;
  }

  private static Object parser(JsonReader reader) throws IOException,
      JSONException {
    Object result = null;
    JsonToken token = reader.peek();
    switch (token) {
      case BEGIN_ARRAY:
        result = parserArray(reader);
        break;
      case BEGIN_OBJECT:
        result = parserObject(reader);
        break;
      case BOOLEAN:
        result = reader.nextBoolean();
        break;
      case NUMBER:
        result = parserNumber(reader);
        break;
      case STRING:
        result = reader.nextString();
        break;
      case NULL:
        result = null;
        reader.nextNull();
        break;
      case NAME:
      case END_ARRAY:
      case END_DOCUMENT:
      case END_OBJECT:
        throw new JSONException("Meet EOF when json not end.");
    }
    return result;
  }

  private static Object parserNumber(JsonReader reader) throws IOException {
    Object result = null;
    try {
      result = reader.nextInt();
    } catch (NumberFormatException e) {
      try {
        result = reader.nextLong();
      } catch (NumberFormatException e1) {
        result = reader.nextDouble();
      }
    }
    return result;
  }

  private static Object parserObject(JsonReader reader) throws IOException,
      JSONException {
    reader.beginObject();
    ObtainabelHashMap<String, Object> result = ObtainabelHashMap.obtain();
    try {
      JsonToken token = reader.peek();
      while (token != JsonToken.END_OBJECT) {
        result.put(reader.nextName(), parser(reader));
        token = reader.peek();
      }
      reader.endObject();
    } catch (IOException e) {
      result.recycle();
      throw e;
    } catch (JSONException e) {
      result.recycle();
      throw e;
    }
    return result;
  }

  private static Object parserArray(JsonReader reader) throws IOException,
      JSONException {
    reader.beginArray();
    ObtainabelList<Object> result = ObtainabelList.obtain();
    try {
      JsonToken token = reader.peek();
      while (token != JsonToken.END_ARRAY) {
        result.add(parser(reader));
        token = reader.peek();
      }
      reader.endArray();
    } catch (IOException e) {
      result.recycle();
      throw e;
    } catch (JSONException e) {
      result.recycle();
      throw e;
    }
    return result;
  }
}
