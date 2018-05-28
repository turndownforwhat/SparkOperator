import com.google.gson._

class FlowContextDealer {
  var context = new JsonObject

  def this(jsonObject: JsonObject){
    this()
    this.context = jsonObject
  }

  def readOperatorPres(operatorId:String):JsonArray = {
    context.getAsJsonObject("context")
      .getAsJsonObject("body")
      .getAsJsonObject("operator")
      .getAsJsonObject(operatorId)
      .getAsJsonArray("pre")
  }

  def getBody():JsonObject = {
    context.getAsJsonObject("context").getAsJsonObject("body")
  }

}
