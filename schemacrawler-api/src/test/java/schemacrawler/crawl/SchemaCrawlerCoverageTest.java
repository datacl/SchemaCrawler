/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.crawl;


import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.loadHsqldbConfig;
import static schemacrawler.test.utility.ObjectPropertyTestUtility.checkBooleanProperties;
import static schemacrawler.test.utility.ObjectPropertyTestUtility.checkIntegerProperties;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexColumnSortSequence;
import schemacrawler.schema.JdbcDriverInfo;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schemacrawler.SchemaReference;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.Config;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.test.utility.TestContextParameterResolver;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@ExtendWith(TestContextParameterResolver.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchemaCrawlerCoverageTest
{

  private Catalog catalog;

  @BeforeAll
  public void loadCatalog(final Connection connection)
    throws Exception
  {
    final Config config = loadHsqldbConfig();

    final SchemaRetrievalOptions schemaRetrievalOptions =
      SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions(config);

    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder
      .builder()
      .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
      .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"))
      .includeAllSynonyms()
      .includeAllSequences()
      .includeAllRoutines()
      .loadRowCounts();
    final SchemaCrawlerOptions schemaCrawlerOptions = schemaCrawlerOptionsBuilder.toOptions();

    catalog = getCatalog(connection, schemaRetrievalOptions, schemaCrawlerOptions);
  }

  @Test
  public void coverPrimaryKey()
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final Index index = table
      .lookupIndex("IDX_B_AUTHORS")
      .get();
    final PrimaryKey primaryKey = new MutablePrimaryKey((MutableIndex) index);

    assertThat(index.getFullName(), is(primaryKey.getFullName()));
    assertThat(index.getColumns(), is(primaryKey.getColumns()));
    assertThat(index.getPages(), is(primaryKey.getPages()));
    assertThat(index.getCardinality(), is(primaryKey.getCardinality()));
    assertThat(index.getType(), is(primaryKey.getType()));

    assertThat(index.isUnique(), is(false));
    assertThat(primaryKey.isUnique(), is(true));
  }

  @Test
  public void coverIndexColumn()
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final Index index = table
      .lookupIndex("IDX_B_AUTHORS")
      .get();
    final IndexColumn indexColumn = index
      .getColumns()
      .get(0);
    final Column column = table
      .lookupColumn(indexColumn.getName())
      .get();

    compareColumnFields(indexColumn, column);

    assertThat(indexColumn.hasDefinition(), is(false));
    assertThat(indexColumn.getIndex(), is(index));
    assertThat(indexColumn.getIndexOrdinalPosition(), is(1));
    assertThat(indexColumn.getSortSequence(), is(IndexColumnSortSequence.ascending));

  }

  @Test
  public void coverTableConstraintColumn()
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final TableConstraint tableConstraint = new ArrayList<>(table.getTableConstraints()).get(0);
    final TableConstraintColumn tableConstraintColumn = tableConstraint
      .getColumns()
      .get(0);
    final Column column = table
      .lookupColumn(tableConstraintColumn.getName())
      .get();

    compareColumnFields(tableConstraintColumn, column);

    assertThat(tableConstraintColumn.getTableConstraint(), is(tableConstraint));
    assertThat(tableConstraintColumn.getTableConstraintOrdinalPosition(), is(0));

  }

  private void compareColumnFields(final Column wrappedColumn, final Column column)
  {
    assertThat(wrappedColumn.getFullName(), is(column.getFullName()));
    assertThat(wrappedColumn.getColumnDataType(), is(column.getColumnDataType()));
    assertThat(wrappedColumn.getDecimalDigits(), is(column.getDecimalDigits()));
    assertThat(wrappedColumn.getOrdinalPosition(), is(column.getOrdinalPosition()));
    assertThat(wrappedColumn.getSize(), is(column.getSize()));
    assertThat(wrappedColumn.getWidth(), is(column.getWidth()));
    assertThat(wrappedColumn.isNullable(), is(column.isNullable()));
    assertThat(wrappedColumn.getDefaultValue(), is(column.getDefaultValue()));
    assertThat(wrappedColumn.getPrivileges(), is(column.getPrivileges()));
    assertThat(wrappedColumn.isAutoIncremented(), is(column.isAutoIncremented()));
    assertThat(wrappedColumn.isGenerated(), is(column.isGenerated()));
    assertThat(wrappedColumn.isHidden(), is(column.isHidden()));
    assertThat(wrappedColumn.isPartOfForeignKey(), is(column.isPartOfForeignKey()));
    assertThat(wrappedColumn.isPartOfIndex(), is(column.isPartOfIndex()));
    assertThat(wrappedColumn.isPartOfPrimaryKey(), is(column.isPartOfPrimaryKey()));
    assertThat(wrappedColumn.isPartOfUniqueIndex(), is(column.isPartOfUniqueIndex()));
    assertThat(wrappedColumn.getType(), is(column.getType()));
  }

  @Test
  public void columnProperties()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final Column column = table
      .lookupColumn("FIRSTNAME")
      .get();

    checkBooleanProperties(column, "autoIncremented", "generated", "hidden");

  }

  @Test
  public void columnDataTypeProperties()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final Column column = table
      .lookupColumn("FIRSTNAME")
      .get();
    final ColumnDataType columnDataType = column.getColumnDataType();

    checkBooleanProperties(columnDataType,
                           "autoIncrementable",
                           "caseSensitive",
                           "fixedPrecisionScale",
                           "nullable",
                           "unsigned",
                           "userDefined");

  }

  @Test
  public void primaryKeyProperties()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final PrimaryKey primaryKey = table.getPrimaryKey();

    assertThat(primaryKey.isUnique(), is(true));
    assertThat(primaryKey.isDeferrable(), is(false));
    assertThat(primaryKey.isInitiallyDeferred(), is(false));

    final TableConstraint constraint = new MutableTableConstraint(table, primaryKey.getName());
    final Optional<TableConstraint> optionalTableConstraint = table.lookupTableConstraint(primaryKey.getName());
    assertThat(optionalTableConstraint, isPresentAndIs(constraint));

  }

  @Test
  public void indexProperties()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();
    final Index index = table
      .lookupIndex("IDX_B_AUTHORS")
      .get();

    checkIntegerProperties(index, "cardinality", "pages");
    checkBooleanProperties(index, "unique");

  }

  @Test
  public void jdbcDriverInfoProperties()
    throws Exception
  {
    final JdbcDriverInfo jdbcDriverInfo = catalog.getJdbcDriverInfo();

    checkBooleanProperties(jdbcDriverInfo, "jdbcCompliant");

  }

  @Test
  public void sequenceProperties()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Sequence sequence = catalog
      .lookupSequence(schema, "PUBLISHER_ID_SEQ")
      .get();

    checkBooleanProperties(sequence, "cycle");

  }

  @Test
  public void viewProperties()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final View view = (View) catalog
      .lookupTable(schema, "AUTHORSLIST")
      .get();

    checkBooleanProperties(view, "updatable");

  }

  @Test
  public void tableAttributes()
    throws Exception
  {
    final SchemaReference schema = new SchemaReference("PUBLIC", "BOOKS");
    final Table table = catalog
      .lookupTable(schema, "AUTHORS")
      .get();

    assertThat(table.hasAttribute("unknown"), is(false));
    assertThat(table.lookupAttribute("unknown"), isEmpty());
    assertThat(table.hasAttribute("schemacrawler.table.row_count"), is(true));
    assertThat(table.lookupAttribute("schemacrawler.table.row_count"), isPresentAndIs(20L));

    assertThat(table.getAttribute("unknown", "no value"), is("no value"));
    assertThat(table.getAttribute("unknown", 10.5f), is(10.5f));
    assertThat(table.getAttribute("schemacrawler.table.row_count", 10L), is(20L));
    assertThrows(ClassCastException.class,
                 () -> { final String string = table.getAttribute("schemacrawler.table.row_count", "no value"); });

    assertThat(table.hasAttribute("new_one"), is(false));
    table.setAttribute("new_one", "some_value");
    assertThat(table.getAttribute("new_one", ""), is("some_value"));
    table.setAttribute("new_one", null);
    assertThat(table.hasAttribute("new_one"), is(false));
    table.setAttribute("new_one", "some_value");
    assertThat(table.hasAttribute("new_one"), is(true));
    table.removeAttribute("new_one");
    assertThat(table.hasAttribute("new_one"), is(false));
  }

}
