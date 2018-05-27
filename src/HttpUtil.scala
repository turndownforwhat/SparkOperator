import java.io._
import java.util

import com.google.gson._
import org.apache.commons._
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.message.BasicNameValuePair

class HttpUtil {

  def getRestContent(url:String):String={
    val httpClient = new DefaultHttpClient()
    val httpResponse = httpClient.execute(new HttpGet(url))
    val entity = httpResponse.getEntity
    var content = ""
    if (entity != null){
      val inputStream = entity.getContent
      content = scala.io.Source.fromInputStream(inputStream).getLines().mkString("")
      inputStream.close
    }

    httpClient.getConnectionManager.shutdown()
    return content
  }

  def postRestContent(url:String,jsonToPost:JsonObject):String={
    val httpClient = new DefaultHttpClient()
    val StringJson = jsonToPost.toString

    val nameValuePairs = new util.ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("JSON", StringJson))

    val httpPost = new HttpPost(url)
    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    val httpResponse = httpClient.execute(httpPost)
    val entity = httpResponse.getEntity
    var content = ""
    if (entity != null){
      val inputStream = entity.getContent
      content = scala.io.Source.fromInputStream(inputStream).getLines().mkString("")
      inputStream.close
    }

    //httpPost.setHeader("Content-Type","application/json")


    httpClient.getConnectionManager.shutdown()
    content

  }
}
