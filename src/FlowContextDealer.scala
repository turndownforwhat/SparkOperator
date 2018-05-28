import com.google.gson._

class FlowContextDealer {
  var context = new JsonObject

  def this(jsonObject: JsonObject){
    this()
    this.context = jsonObject
  }

  def readOperatorPres(operatorId:String):JsonObject = {
    context.getAsJsonObject("body")
      .getAsJsonObject("operator")
      .getAsJsonObject(operatorId)
      .getAsJsonObject("pre")
  }

}
