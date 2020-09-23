package trw.dbsubsetter.datacopy

import trw.dbsubsetter.workflow.DataCopyTask

trait DataCopyWorkflow {

  def process(dataCopyTask: DataCopyTask): Unit

}
