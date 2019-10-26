package trw.dbsubsetter.db

object Constants {

  // Must be in descending order
  // TODO: investigate the case of a primary key with 3 columns where we may run into 3 * 2000 = too many placeholders for a prepared statement.
  val dataCopyBatchSizes: Seq[Short] = Seq(2000, 1000, 256, 16, 4, 2, 1)
}
