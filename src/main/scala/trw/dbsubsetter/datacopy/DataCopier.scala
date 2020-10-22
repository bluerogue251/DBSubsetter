package trw.dbsubsetter.datacopy

trait DataCopier {

  def runTask(task: DataCopyTask): Unit

}
