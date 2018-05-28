import com.google.gson._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Column, DataFrame, Dataset, SQLContext}

//case class energy_data(id:Int,entity_id:String,value:String,timestamp:Long,timestamp_str:String)
//case class onlyValue(value:String)

object SparkExec {

  def main(args:Array[String]) : Unit = {

    val sparkConf = new SparkConf()
      .setAppName("SparkAdd")
      .setMaster("local")
    val sc = new SparkContext(sparkConf)
    val sqc = new SQLContext(sc)

    val httpUtil = new HttpUtil
    var content = httpUtil.getRestContent("http://10.8.0.32:9090/contexts/test1_SparkAdd_retrieve_and_update")

    var returnData = new JsonParser().parse(content).getAsJsonObject

    var dealer = new FlowContextDealer(returnData)

    val dbs = dealer.readOperatorPres("spark_add_201805282053")
    val dbsIterator = dbs.iterator()

    var paramsList:List[ParamEntity] = List()

    while(dbsIterator.hasNext){
      var dbInfoJsonObject:JsonObject = dbsIterator.next().getAsJsonObject
      var eachParam = new ParamEntity(dbInfoJsonObject)
      paramsList = eachParam :: paramsList
    }

    /* for loop to read pre-nodes in context
    *   "params": {
                  "passwd": "root",
                  "ip": "120.76.226.75",
                  "port": "3306",
                  "user": "root",
                  "table": "t_m_day",
                  "where": {
                    "start": 1451705100000,
                    "entity_id": "0000100270001",
                    "end": 1451877900000
                  },
                  "dbname": "energy_test_data"
                  }
        }
    * */

    import sqc.implicits._

    var onlyValueRDD:List[RDD[(Long, String)]] = List()

    for( x <- paramsList.indices){
      var valueDs = getDsValue(sqc,paramsList(x),"value")
      var dsWithindex = valueDs.rdd
        .zipWithIndex()
        .map(a=>(a._2.toLong+1,a._1))

      onlyValueRDD = dsWithindex :: onlyValueRDD
      //dsWithindex.toDS().show(50)
    }

    var unionRDD = onlyValueRDD.head
    for(x <- 1 until onlyValueRDD.length){
      var middle_RDD = unionRDD
        .join(onlyValueRDD(x))
        .sortByKey()

      unionRDD = middle_RDD
        .map(a => (a._2._1.toDouble + a._2._2.toDouble).toString)
        .zipWithIndex()
        .map(a=>(a._2.toLong+1,a._1))
    }


    var resultDf = unionRDD.toDF()
    //  .toDF()

    resultDf.show(50)
  }


  def showDb(df:DataFrame,limit:Int): Unit = {
    df.show(limit)
  }

  def getDsValue(sqc:SQLContext,
                 params:ParamEntity,
                 columnName:String) : Dataset[String] = {
    import sqc.implicits._
    val ds = sqc.read
      .format("jdbc")
      .options(params.getDbParams())
      .load()
      .filter(genFilterSql(params.getCalcParams()))
      .select(columnName)
      .as[String]

    ds
  }

  def updateDb(resultDF:DataFrame,dbParams:Map[String,String]): Unit = {
    resultDF.write
      .format("jdbc")
      .options(dbParams)
      .save()
  }

  /* */
  def genFilterSql(calparams:Map[String,String]): String = {
    val start_at = new String(calparams.apply("start_at"))
    val end_at = new String(calparams.apply("end_at"))
    val entity_id = new String(calparams.apply("entity_id"))

    val filterSql = s"entity_id = $entity_id " +
      s"and timestamp >= $start_at " +
      s"and timestamp < $end_at"

    filterSql
  }

  def getOriginalColumn[T](sqc:SQLContext,
                           paramTuple:(Map[String,String],Map[String,String]),
                           colomnName:String): Column = {
    import sqc.implicits._
    val colToRet = sqc.read
      .format("jdbc")
      .options(paramTuple._1)
      .load()
      .filter(genFilterSql(paramTuple._2))
      .col(colomnName)
      .as[String]

    colToRet
  }

}

