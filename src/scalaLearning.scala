import com.google.gson._

object scalaLearning {
  def main(args:Array[String]): Unit = {

    val httpUtil = new HttpUtil
    var content = httpUtil.getRestContent("http://10.8.0.32:9090/contexts/cf98c897-db7b-3341-9bc1-a788f01279b2")

    var returnData = new JsonParser().parse(content).getAsJsonObject

    var dealer = new FlowContextDealer(returnData)

    val dbs = dealer.readOperatorPres("minus_91534cdee6d93da79d4fef7616911203")

    println(dbs)

  }

}
