package trw.dbsubsetter.workflow


object SkipPkStore {
  def process(res: OriginDbResult): PksAdded = {
    val children = if (res.fetchChildren) res.rows else Vector.empty
    PksAdded(res.table, res.rows, children, res.viaTableOpt)
  }
}
