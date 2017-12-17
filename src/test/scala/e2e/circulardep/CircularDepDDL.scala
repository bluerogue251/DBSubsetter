package e2e.circulardep

trait CircularDepDDL {
  val profile: slick.jdbc.JdbcProfile

  import profile.api._

  lazy val schema: profile.SchemaDescription = Grandparents.schema ++ Parents.schema ++ Children.schema

  /**
    * grandparents
    */
  case class GrandparentsRow(id: Int, favoriteParentId: Option[Int] = None)

  class Grandparents(_tableTag: Tag) extends profile.api.Table[GrandparentsRow](_tableTag, "grandparents") {
    def * = (id, favoriteParentId) <> (GrandparentsRow.tupled, GrandparentsRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val favoriteParentId: Rep[Option[Int]] = column[Option[Int]]("favorite_parent_id", O.Default(None))
    lazy val parentsFk = foreignKey("grandparents_favorite_parent_id_fkey", favoriteParentId, Parents)(r => Rep.Some(r.id))
    val index1 = index("grandparents_points_to_parents_idx", favoriteParentId)
  }

  lazy val Grandparents = new TableQuery(tag => new Grandparents(tag))

  /**
    * parents
    */
  case class ParentsRow(id: Int, grandparentId: Int)

  class Parents(_tableTag: Tag) extends profile.api.Table[ParentsRow](_tableTag, "parents") {
    def * = (id, grandparentId) <> (ParentsRow.tupled, ParentsRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val grandparentId: Rep[Int] = column[Int]("grandparent_id")
    lazy val grandparentsFk = foreignKey("parents_grandparent_id_fkey", grandparentId, Grandparents)(r => r.id)
    val index1 = index("parents_points_to_grandparents_idx", grandparentId)
  }

  lazy val Parents = new TableQuery(tag => new Parents(tag))

  /**
    * children
    */
  case class ChildrenRow(id: Int, parentId: Int)

  class Children(_tableTag: Tag) extends profile.api.Table[ChildrenRow](_tableTag, "children") {
    def * = (id, parentId) <> (ChildrenRow.tupled, ChildrenRow.unapply)

    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    val parentId: Rep[Int] = column[Int]("parent_id")
    lazy val parentsFk = foreignKey("children_parent_id_fkey", parentId, Parents)(r => r.id)
    val index1 = index("children_points_to_parents_idx", parentId)
  }

  lazy val Children = new TableQuery(tag => new Children(tag))
}
