package trw.dbsubsetter.db

object Constants {

  // Must be in descending order
  // TODO: investigate the case of a primary key with so many columns that we may run into too many placeholders for a prepared statement.
  val dataCopyBatchSizes: Seq[Short] = Seq(1024, 512, 256, 16, 4, 2, 1)
}
