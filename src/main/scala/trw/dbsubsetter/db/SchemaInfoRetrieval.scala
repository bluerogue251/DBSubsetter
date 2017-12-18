package trw.dbsubsetter.db

import java.util.NoSuchElementException

import trw.dbsubsetter.config.Config

object SchemaInfoRetrieval {
  def getSchemaInfo(config: Config): SchemaInfo = {
    val DbMetadataQueryResult(tables, columns, pks, fks, dbVendor) = DbMetadataQueries.queryDb(config)

    val tablesByName = tables.map { t =>
      val hasSqlServerAutoincrement = columns.exists(c => c.schema == t.schema && c.table == t.name && c.isSqlServerAutoincrement)
      val storePks = !config.skipPkStore.contains((t.schema, t.name))
      (t.schema, t.name) -> Table(t.schema, t.name, hasSqlServerAutoincrement, storePks)
    }.toMap

    val colsByTableAndName: Map[Table, Map[ColumnName, Column]] = {
      columns
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, partialColumns) =>
          table -> partialColumns.zipWithIndex.map { case (pc, i) => pc.name -> Column(table, pc.name, i, pc.jdbcType, pc.typeName) }.toMap
        }
    }

    val colByTableOrdered: Map[Table, Vector[Column]] = {
      colsByTableAndName.map { case (table, map) => table -> map.values.toVector.sortBy(_.ordinalPosition) }
    }

    val pkOrdinalsByTable: Map[Table, Vector[Int]] = {
      pks
        .groupBy(pk => tablesByName(pk.schema, pk.table))
        .map { case (table, partialPks) =>
          table -> partialPks.map(ppk => colsByTableAndName(table)(ppk.column)).map(_.ordinalPosition).sorted
        }
    }

    val foreignKeysOrdered: Array[ForeignKey] = {
      val fksUnordered = fks
        .groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
        .map { case ((fromSchemaName, fromTableName, toSchemaName, toTableName), partialForeignKeys) =>
          val fromTable = tablesByName(fromSchemaName, fromTableName)
          val fromCols = partialForeignKeys.map { pfk => colsByTableAndName(fromTable)(pfk.fromColumn) }
          val toTable = tablesByName(toSchemaName, toTableName)
          // MySQL schema introspection has a bug where they don't properly capitalize column names of
          // the `pointedTo` side of foreign keys.
          //
          // Doesn't seem to be remedied by &useInformationSchema=true in DB URL so that the `DatabaseMetaDataUsingInfoSchema` class is used
          //
          // This seems to be a bug at the MySQL layer, not at the JDBC Driver layer because the same
          // issue is present in the command line program using the `show create table my_table` command
          //
          // This is a hacky workaround and could cause problems in a schema where the same column name
          // is used twice in the same table with different capitalization. (This seems like it ought to be either impossible or rare though).
          //
          // A less hacky workaround would be to match by column ordinal rather than by name, but unfortunately that info
          // is not included in the result of the query we make to get foreign keys.
          lazy val mysqlWorkaround = colsByTableAndName.mapValues(nameToColMap => nameToColMap.map { case (k, v) => k.toLowerCase -> v })
          val toCols = partialForeignKeys.map { pfk =>
            try {
              colsByTableAndName(toTable)(pfk.toColumn)
            } catch {
              case _: NoSuchElementException if dbVendor == DbVendor.MySQL => mysqlWorkaround(toTable)(pfk.toColumn)
              case e: Throwable => throw e
            }
          }

          val pointsToPk = {
            val pkOpt = pkOrdinalsByTable.get(toTable)
            pkOpt.fold(false)(pkColOrdinals => pkColOrdinals == toCols.map(_.ordinalPosition))
          }

        ForeignKey(fromCols, toCols, pointsToPk, 0)
      }

      fksUnordered.toArray.zipWithIndex.map { case (fk, idx) => fk.copy(i = idx.toShort) }
    }

    val fksFromTable: Map[Table, Vector[ForeignKey]] = {
      foreignKeysOrdered.toVector.groupBy(_.fromTable).withDefaultValue(Vector.empty)
    }

    val fksToTable: Map[Table, Vector[ForeignKey]] = {
      foreignKeysOrdered.toVector.groupBy(_.toTable).withDefaultValue(Vector.empty)
    }

    SchemaInfo(
      tablesByName,
      colByTableOrdered,
      pkOrdinalsByTable,
      foreignKeysOrdered,
      fksFromTable,
      fksToTable,
      dbVendor
    )
  }
}