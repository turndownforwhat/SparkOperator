import com.google.gson._

object scalaLearning {
  def main(args:Array[String]): Unit = {

    val httpUtil = new HttpUtil
    var content = httpUtil.getRestContent("http://10.8.0.32:9090/contexts/test_SparkAdd_retrieve_and_update")

    var returnData = new JsonParser().parse(content).getAsJsonObject

    var dealer = new FlowContextDealer(returnData)

    val dbs = dealer.readOperatorPres("spark_add_201805282053")

    val tmp = dbs.get(0).getAsJsonObject.getAsJsonObject("params").getAsJsonPrimitive("dbname").toString

    println(tmp)

  }

}
