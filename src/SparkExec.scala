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

    // SQLcontext 隐式转换，用于dataframe格式转换
    import sqc.implicits._
    // 初始化中间结果集合
    var onlyValueRDD:List[RDD[(Long, String)]] = List()
    // for循环遍历数据库参数列表，获取各组输入数据
    for( x <- paramsList.indices){
      //获取数据
      var valueDs = getDsValue(sqc,paramsList(x),"value")
      //为每一组数据在右侧添加索引，并通过映射函数交换索引与数值的位置
      var dsWithindex = valueDs.rdd
        .zipWithIndex()
        .map(a=>(a._2.toLong+1,a._1))
      //将附加索引的数值数据添加到中间结果集合
      onlyValueRDD = dsWithindex :: onlyValueRDD

    }

    //取出中间结果集合的头元素作为最终输出的RDD，进行循环迭代
    var unionRDD = onlyValueRDD.head
    //从集合第二位元素开始循环操作
    for(x <- 1 until onlyValueRDD.length){
      //数据通过索引进行连接，连接后按索引顺序进行排序，得到RDD迭代中间状态
      var middle_RDD = unionRDD
        .join(onlyValueRDD(x))
        .sortByKey()

      //对RDD中间态进行映射
      //将连接好的双数值列进行类型转换并执行加/减操作
      //加上索引并交换索引列和数值列的位置
      //更新输出RDD，开始下一次循环
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

