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
package schemacrawler.integration.test;


import static java.nio.file.Files.lines;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.utility.SchemaCrawlerUtility.getCatalog;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.test.utility.BaseAdditionalDatabaseTest;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.HeavyDatabaseBuildCondition;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.integration.diagram.DiagramOutputFormat;
import schemacrawler.tools.integration.serialize.SerializationFormat;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.options.TextOutputFormat;
import sf.util.DatabaseUtility;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(HeavyDatabaseBuildCondition.class)
@DisplayName("Test for issue #284 - support enum values")
public class PostgreSQLEnumColumnTest
  extends BaseAdditionalDatabaseTest
{

  @Container
  private JdbcDatabaseContainer dbContainer =
    new HeavyDatabaseBuildCondition().getJdbcDatabaseContainer(() -> new PostgreSQLContainer<>());

  @BeforeEach
  public void createDatabase()
    throws Exception
  {
    createDataSource(dbContainer.getJdbcUrl(),
                     dbContainer.getUsername(),
                     dbContainer.getPassword());

    try (
      final Connection connection = getConnection();
      final Statement stmt = connection.createStatement();
    )
    {
      stmt.execute("CREATE TYPE mood AS ENUM ('sad', 'ok', 'happy')");
      stmt.execute("CREATE TABLE person (name text, current_mood mood)");
      connection.commit();
    }
  }

  @Test
  public void columnWithEnumOutputFormats()
    throws Exception
  {

    final SchemaCrawlerOptions schemaCrawlerOptions =
      DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;;

    final SchemaCrawlerExecutable executable;
    executable = new SchemaCrawlerExecutable("details");
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);

    for (final OutputFormat outputFormat : new OutputFormat[] {
      DiagramOutputFormat.scdot, TextOutputFormat.text, TextOutputFormat.html
    })
    {
      assertThat(outputOf(executableExecution(getConnection(),
                                              executable,
                                              outputFormat)),
                 hasSameContentAs(classpathResource(
                   "testColumnWithEnum." + outputFormat.getFormat())));
    }
  }

  @Test
  public void columnWithEnumSerialization()
    throws Exception
  {

    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder =
      SchemaCrawlerOptionsBuilder
        .builder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
    final SchemaCrawlerOptions schemaCrawlerOptions =
      schemaCrawlerOptionsBuilder.toOptions();

    SchemaCrawlerExecutable executable;
    executable = new SchemaCrawlerExecutable("serialize");
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);

    for (final OutputFormat outputFormat : new OutputFormat[] {
      SerializationFormat.json, SerializationFormat.yaml
    })
    {
      final Path outputFile =
        executableExecution(getConnection(), executable, outputFormat);
      final List<String> enumOutput = lines(outputFile)
        .filter(line -> line.contains("happy"))
        .collect(Collectors.toList());
      assertThat(enumOutput, is(not(empty())));
      assertThat(enumOutput.get(0), matchesPattern(".*happy.*"));
    }
  }

  @Test
  public void columnWithEnum()
    throws Exception
  {

    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder =
      SchemaCrawlerOptionsBuilder
        .builder()
        .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
    final SchemaCrawlerOptions schemaCrawlerOptions =
      schemaCrawlerOptionsBuilder.toOptions();

    final Catalog catalog = getCatalog(getConnection(), schemaCrawlerOptions);
    final Schema schema = catalog
      .lookupSchema("public")
      .orElse(null);
    assertThat(schema, notNullValue());
    final Table table = catalog
      .lookupTable(schema, "person")
      .orElse(null);
    assertThat(table, notNullValue());

    final Column nameColumn = table
      .lookupColumn("name")
      .orElse(null);
    assertThat(nameColumn, notNullValue());
    assertThat(nameColumn
                 .getColumnDataType()
                 .isEnumerated(), is(false));
    assertThat(nameColumn
                 .getColumnDataType()
                 .getEnumValues(), emptyCollectionOf(String.class));

    final Column currentMoodColumn = table
      .lookupColumn("current_mood")
      .orElse(null);
    assertThat(currentMoodColumn, notNullValue());
    assertThat(currentMoodColumn
                 .getColumnDataType()
                 .isEnumerated(), is(true));
    assertThat(currentMoodColumn
                 .getColumnDataType()
                 .getEnumValues(), containsInAnyOrder("sad", "ok", "happy"));
  }

}
