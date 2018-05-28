import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Column, DataFrame, Dataset, SQLContext}

//case class energy_data(id:Int,entity_id:String,value:String,timestamp:Long,timestamp_str:String)
//case class onlyValue(value:String)

object SparkExec {

  def main(args:Array[String]) : Unit = {

//    var DatasToOperate: List[Dataset[onlyValue]] = List()

    //for saving the info of database
    var dbParams: Map[String,String] = Map()
    var calcParams1: Map[String,String] = Map()
    var calcParams2: Map[String,String] = Map()
    var calcParams3: Map[String,String] = Map()

    dbParams += ("url" -> "jdbc:mysql://120.76.226.75:3306/energy_test_data")
    dbParams += ("dbtable" -> "t_m_hour")
    dbParams += ("user" -> "root")
    dbParams += ("password" -> "root")

    calcParams1 += ("start_at" -> "1451705100000")
    calcParams1 += ("end_at" -> "1451877900000")
    calcParams1 += ("entity_id" -> "0000100270001")

    calcParams2 += ("start_at" -> "1451705100000")
    calcParams2 += ("end_at" -> "1451877900000")
    calcParams2 += ("entity_id" -> "0000100270002")

    calcParams3 += ("start_at" -> "1451705100000")
    calcParams3 += ("end_at" -> "1451877900000")
    calcParams3 += ("entity_id" -> "0000100270003")

    val sparkConf = new SparkConf()
      .setAppName("SparkAdd")
      .setMaster("local")
    val sc = new SparkContext(sparkConf)
    val sqc = new SQLContext(sc)

    /* for loop to read pre-nodes in context
    *
    * getContext(flow_id)
    * getOperatorContext(operator_id)
    * getPreNodes(operatorContext["pre"]: Array[JSON]
    * getPreNodesOutput(PreNodes["output"]
    * getAllParams:{
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
    * assembleDbUrl(ip,port,dbname)
    * createDBParams(url,user,password,dbtable):Map()
    * createCalcParams(start_at,end_at,entity_id):Map()
    * union2Params(DBParams:Map[String,String],calcParams:Map[String,String]):Tuple2()
    *
    * add tuple into tuples
    *
    * */

    import sqc.implicits._

    val paramsTuple1 = (dbParams,calcParams1)
    val paramsTuple2 = (dbParams,calcParams2)
    val paramsTuple3 = (dbParams,calcParams3)

    val timestampColumn = getOriginalColumn(sqc,paramsTuple1,"timestamp")
    val timestampStrColumn = getOriginalColumn(sqc,paramsTuple1,"timestamp_str")

    val paramsTuples = List(paramsTuple1,paramsTuple2,paramsTuple3)
    var onlyValueRDD:List[RDD[(Long, String)]] = List()

    val timestampDs = getOriginalColumn(sqc,paramsTuples.head,"timestamp")

    for( x <- paramsTuples.indices){
      var valueDs = getDsValue(sqc,paramsTuples(x),"value")
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
                 paramTuple:(Map[String,String],Map[String,String]),
                 columnName:String) : Dataset[String] = {
    import sqc.implicits._
    val ds = sqc.read
      .format("jdbc")
      .options(paramTuple._1)
      .load()
      .filter(genFilterSql(paramTuple._2))
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

