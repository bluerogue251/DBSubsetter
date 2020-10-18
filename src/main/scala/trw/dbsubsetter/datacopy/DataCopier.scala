package trw.dbsubsetter.datacopy

import trw.dbsubsetter.workflow.DataCopyTask

trait DataCopier {

  def runTask(task: DataCopyTask): Unit

}
