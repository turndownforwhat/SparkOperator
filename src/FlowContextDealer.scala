import com.google.gson._

class FlowContextDealer {
  // 用于存放工作流上下文
  var context = new JsonObject

  // 通过传入的Json对象进行实例构造
  def this(jsonObject: JsonObject){
    this()
    this.context = jsonObject
  }

  // 根据操作节点id读取其前置节点
  def readOperatorPres(operatorId:String):JsonArray = {
    var dbInfoJsonArray = new JsonArray
    var preIds = this.readOperator(operatorId)
      .getAsJsonArray("pre")

    var iterPreIds = preIds.iterator()
    var StringPreId = ""
    var preNodeOutputs = new JsonArray

    while(iterPreIds.hasNext){
      StringPreId = iterPreIds.next().getAsString
      preNodeOutputs = this.readOperatorOutputs(StringPreId)
      var iterPreNodeOutputs = preNodeOutputs.iterator()

      while(iterPreNodeOutputs.hasNext){
        dbInfoJsonArray.add(iterPreNodeOutputs.next()
          .getAsJsonObject
          .get("database"))
      }
    }
    dbInfoJsonArray
  }

  // 根据操作节点id读取节点内信息
  def readOperator(operatorId:String):JsonObject = {
    context.getAsJsonObject("context")
      .getAsJsonObject("body")
      .getAsJsonObject("operator")
      .getAsJsonObject(operatorId)
  }

  // 根据操作节点id读取节点内输出内容
  def readOperatorOutputs(operatorId:String):JsonArray = {
    this.readOperator(operatorId)
      .getAsJsonArray("outputs")
  }

  // 根据操作节点id更新该节点的结果输出内容
  def updateOperatorOutput(operatorId:String,resultSize:Int):JsonArray = {

  }


}
