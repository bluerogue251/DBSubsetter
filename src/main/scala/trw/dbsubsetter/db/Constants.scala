package trw.dbsubsetter.db

object Constants {

  // Must be in descending order
  val dataCopyBatchSizes: Seq[Int] = Seq(2048, 1024, 256, 16, 4, 2, 1)
}
