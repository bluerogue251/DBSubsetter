package trw.dbsubsetter.datacopy

import trw.dbsubsetter.workflow.DataCopyTask

trait DataCopier {

  def copy(task: DataCopyTask): Unit

}
