object scalaLearning {
  def main(args:Array[String]): Unit = {
    var contextdealer = new FlowContextDealer
    var content = contextdealer.retriveContext()

    print(content)


  }

}
