import java.io._
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._

import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity

class HttpUtil {
  /**
    * Returns the text (content) from a REST URL as a String.
    * Inspired by http://matthewkwong.blogspot.com/2009/09/scala-scalaiosource-fromurl-blockshangs.html
    * and http://alvinalexander.com/blog/post/java/how-open-url-read-contents-httpurl-connection-java
    *
    * The `connectTimeout` and `readTimeout` comes from the Java URLConnection
    * class Javadoc.
    * @param url The full URL to connect to.
    * @param connectTimeout Sets a specified timeout value, in milliseconds,
    *                       to be used when opening a communications link to the resource referenced
    *                       by this URLConnection. If the timeout expires before the connection can
    *                       be established, a java.net.SocketTimeoutException
    *                       is raised. A timeout of zero is interpreted as an infinite timeout.
    *                       Defaults to 5000 ms.
    * @param readTimeout If the timeout expires before there is data available
    *                    for read, a java.net.SocketTimeoutException is raised. A timeout of zero
    *                    is interpreted as an infinite timeout. Defaults to 5000 ms.
    * @param requestMethod Defaults to "GET". (Other methods have not been tested.)
    *
    *
    * @example get("http://www.example.com/getInfo")
    * @example get("http://www.example.com/getInfo", 5000)
    * @example get("http://www.example.com/getInfo", 5000, 5000)
    */
  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def get(url: String,
          connectTimeout: Int = 5000,
          readTimeout: Int = 5000,
          requestMethod: String = "POST"): String = {
    import java.net.{URL, HttpURLConnection}
    val connection = (new URL(url)).openConnection.asInstanceOf[HttpURLConnection]
    connection.setConnectTimeout(connectTimeout)
    connection.setReadTimeout(readTimeout)
    connection.setRequestMethod(requestMethod)
    connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded")
    connection.connect()

    var writer = new BufferedReader(new InputStreamReader(connection.getInputStream))
//    var line = new String
//    var response = new String
//    while((line = writer.readLine())!= null){
//      response += line
//    }
    //val content = scala.io.Source.fromInputStream(inputStream,"utf-8").mkString

    //content
    ""
  }

  def sendGet(url: String): String = {
    try {
      val content = get(url)
      return content

    } catch {
      case ioe: java.io.IOException => // handle this
      case ste: java.net.SocketTimeoutException => // handle this
    }
    ""
  }
}
