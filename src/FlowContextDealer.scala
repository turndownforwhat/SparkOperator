import org.apache.spark.rdd.RDD
import java.net.HttpURLConnection

class FlowContextDealer {
  val apiBase = "https://private-eb3791-job2.apiary-mock.com/context/2ffd275c-031b-3a43-864b-4c75ecb7f0d2"
  val flow_id = "2ffd275c-031b-3a43-864b-4c75ecb7f0d2"
  val joesUrl = "203.195.152.12:4000/users?username=Jom&orgName=Org1"

  def retriveContext(): Unit = {

    var httputil = new HttpUtil
    var content = httputil.sendGet(joesUrl)
    print(content)
  }
}
