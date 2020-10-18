package trw.dbsubsetter.datacopy

trait DataCopierFactory {

  def build(): DataCopier

}
