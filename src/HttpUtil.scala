import java.io._

import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet

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
}
