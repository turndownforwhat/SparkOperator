import com.google.gson._

class ParamEntity() {
  private var singleDbInfo = new JsonObject
  private var dbParams:Map[String,String] = Map()
  private var calcParams:Map[String,String] = Map()

  /*
                "database": {
                "type": "mysql",
                "params": {
                  "passwd": "root",
                  "ip": "120.76.226.75",
                  "port": "3306",
                  "user": "root",
                  "table": "t_m_day",
                  "where": {
                    "start": 1514736000000,
                    "entity_id": "0000100270001",
                    "end": 1575129600000
                  },
                  "dbname": "energy_test_data"
                }
              }
   */

  def this(databaseInfo: JsonObject){
    this()

    this.singleDbInfo = databaseInfo

    this.dbParams += ("url" -> assembleJdbcUrl())
    this.dbParams += ("dbtable" -> connParamsFromJson("table"))
    this.dbParams += ("user" -> connParamsFromJson("user"))
    this.dbParams += ("password" -> connParamsFromJson("passwd"))

    this.calcParams += ("start_at" -> calcParamsFromJson("start"))
    this.calcParams += ("end_at" -> calcParamsFromJson("end"))
    this.calcParams += ("entity_id" -> calcParamsFromJson("entity_id"))
  }

  def assembleJdbcUrl():String = {
    val dbtype = singleDbInfo.getAsJsonPrimitive("type").getAsString
    val ip = singleDbInfo.getAsJsonObject("params").getAsJsonPrimitive("ip").getAsString
    val port = singleDbInfo.getAsJsonObject("params").getAsJsonPrimitive("port").getAsString
    val dbname = singleDbInfo.getAsJsonObject("params").getAsJsonPrimitive("dbname").getAsString

    val JdbcUrl = s"jdbc:$dbtype://$ip:$port/$dbname"
    JdbcUrl
  }

  def connParamsFromJson(paramName:String):String = {
    singleDbInfo.getAsJsonObject("params")
      .getAsJsonPrimitive(paramName)
      .getAsString
  }

  def calcParamsFromJson(paramName:String):String ={
    singleDbInfo.getAsJsonObject("params")
      .getAsJsonObject("where")
      .getAsJsonPrimitive(paramName)
      .getAsString
  }
  
  def getDbParams():Map[String,String]={
    this.dbParams
  }

  def getCalcParams():Map[String,String]={
    this.calcParams
  }

}
