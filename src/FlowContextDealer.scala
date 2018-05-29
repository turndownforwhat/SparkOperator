import com.google.gson._

class FlowContextDealer {
  var context = new JsonObject

  def this(jsonObject: JsonObject){
    this()
    this.context = jsonObject
  }

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

  def readOperator(operatorId:String):JsonObject = {
    context.getAsJsonObject("context")
      .getAsJsonObject("body")
      .getAsJsonObject("operator")
      .getAsJsonObject(operatorId)
  }

  def readOperatorOutputs(operatorId:String):JsonArray = {
    this.readOperator(operatorId)
      .getAsJsonArray("outputs")
  }


}
