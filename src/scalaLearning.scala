import com.google.gson._

object scalaLearning {
  def main(args:Array[String]): Unit = {
    val httpUtil = new HttpUtil
    var content = httpUtil.getRestContent("http://10.8.0.32:9090/contexts/")

    var returnData = new JsonParser().parse(content).getAsJsonObject

    println(returnData.get("flow_id"))



  }

}
