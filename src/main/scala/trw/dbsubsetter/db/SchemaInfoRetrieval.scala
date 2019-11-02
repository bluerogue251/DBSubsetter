package trw.dbsubsetter.db

import java.util.NoSuchElementException

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.ColumnTypes.ColumnType

// scalastyle:off
object SchemaInfoRetrieval {
  def getSchemaInfo(config: Config): SchemaInfo = {
    val DbMetadataQueryResult(
      tableMetadataRows,
      columnMetadataRows,
      primaryKeyMetadataRows,
      foreignKeyMetadataRows,
      dbVendor
    ) = DbMetadataQueries.queryDb(config)

    val tablesByName = tableMetadataRows.map { t =>
      val hasSqlServerAutoincrement = columnMetadataRows.exists(c => c.schema == t.schema && c.table == t.name && c.isSqlServerAutoincrement)
      (t.schema, t.name) -> new Table(t.schema, t.name, hasSqlServerAutoincrement)
    }.toMap

    val colsByTableAndName: Map[Table, Map[ColumnName, Column]] = {
      columnMetadataRows
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, cols) =>
          table -> cols.zipWithIndex.map { case (c, i) =>
            val columnType: ColumnType = ColumnTypes.fromRawInfo(c.jdbcType, c.typeName, dbVendor)
            val column: Column = new Column(
              table = table,
              name = c.name,
              keyOrdinalPosition = -1, // Placeholder value that will be mutated (corrected) later
              dataOrdinalPosition = i,
              dataType = columnType
            )
            c.name -> column
          }.toMap
        }
    }

    val allColumnsByTableOrdered: Map[Table, Vector[Column]] = {
      colsByTableAndName.map { case (table, map) => table -> map.values.toVector.sortBy(_.dataOrdinalPosition) }
    }

    val pksByTable: Map[Table, PrimaryKey] = {
      primaryKeyMetadataRows
        .groupBy(pk => tablesByName(pk.schema, pk.table))
        .map { case (table, singleTablePrimaryKeyMetadataRows) =>
          val columnNames = singleTablePrimaryKeyMetadataRows.map(_.column).toSet
          val orderedColumns = allColumnsByTableOrdered(table).filter(c => columnNames.contains(c.name))
          table -> new PrimaryKey(orderedColumns)
        }
    }

    val foreignKeysOrdered: Array[ForeignKey] = {
      val fksUnordered = foreignKeyMetadataRows
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

          val pointsToPk = pksByTable.get(toTable).fold(false)(pk => pk.columns == toCols)

        new ForeignKey(fromCols, toCols, pointsToPk, 0)
      }

      fksUnordered.toArray.zipWithIndex.map { case (fk, idx) =>
        fk.setIndex(idx.toShort)
        fk
      }
    }

    val fksFromTable: Map[Table, Vector[ForeignKey]] = {
      foreignKeysOrdered.toVector.groupBy(_.fromTable).withDefaultValue(Vector.empty)
    }

    val fksToTable: Map[Table, Vector[ForeignKey]] = {
      foreignKeysOrdered.toVector.groupBy(_.toTable).withDefaultValue(Vector.empty)
    }

    val keyColumnsByTableOrdered: Map[Table, Vector[Column]] = {
      allColumnsByTableOrdered.map { case (table, allColumns) =>
        val primaryKey: PrimaryKey = pksByTable(table)
        val foreignKeysFromTable: Seq[ForeignKey] = fksFromTable(table)
        val foreignKeysToTable: Seq[ForeignKey] = fksToTable(table)

        def isKeyColumn(col: Column): Boolean = {
          primaryKey.columns.contains(col) ||
            foreignKeysFromTable.exists(_.fromCols.contains(col)) ||
            foreignKeysToTable.exists(_.toCols.contains(col))
        }

        val keyColumns: Vector[Column] = allColumns.filter(isKeyColumn)

        keyColumns.zipWithIndex.foreach { case (keyColumn, i) =>
          keyColumn.keyOrdinalPosition = i
        }

        table -> keyColumns
      }
    }

    new SchemaInfo(
      tablesByName = tablesByName,
      keyColumnsByTableOrdered = keyColumnsByTableOrdered,
      dataColumnsByTableOrdered = allColumnsByTableOrdered,
      pksByTable = pksByTable,
      fksOrdered = foreignKeysOrdered,
      fksFromTable = fksFromTable,
      fksToTable = fksToTable
    )
  }
}
