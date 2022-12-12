case class ErgoScriptContract(path: String) {
  def loadContract(): String = {
    val lines = scala.io.Source.fromFile(path).mkString
    lines
  }
}
