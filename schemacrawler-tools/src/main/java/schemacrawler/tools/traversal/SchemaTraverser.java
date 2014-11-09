/*
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2014, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */
package schemacrawler.tools.traversal;


import java.util.Collection;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;

public class SchemaTraverser
{

  private Catalog catalog;
  private SchemaTraversalHandler handler;

  public Catalog getCatalog()
  {
    return catalog;
  }

  public SchemaTraversalHandler getHandler()
  {
    return handler;
  }

  public void setCatalog(final Catalog catalog)
  {
    if (catalog == null)
    {
      throw new IllegalArgumentException("No catalog provided");
    }
    this.catalog = catalog;
  }

  public void setHandler(final SchemaTraversalHandler handler)
  {
    if (handler == null)
    {
      throw new IllegalArgumentException("No handler provided");
    }
    this.handler = handler;
  }

  public final void traverse()
    throws SchemaCrawlerException
  {
    if (catalog == null || handler == null)
    {
      throw new SchemaCrawlerException("Cannot traverse database");
    }

    handler.begin();

    handler.handleInfoStart();
    handler.handle(catalog.getSchemaCrawlerInfo());
    handler.handle(catalog.getDatabaseInfo());
    handler.handle(catalog.getJdbcDriverInfo());
    handler.handleInfoEnd();

    final Collection<ColumnDataType> columnDataTypes = catalog
      .getColumnDataTypes();
    final Collection<Table> tables = catalog.getTables();
    final Collection<Routine> routines = catalog.getRoutines();
    final Collection<Synonym> synonyms = catalog.getSynonyms();
    final Collection<Sequence> sequences = catalog.getSequences();

    if (!columnDataTypes.isEmpty())
    {
      handler.handleColumnDataTypesStart();
      for (final ColumnDataType columnDataType: columnDataTypes)
      {
        handler.handle(columnDataType);
      }
      handler.handleColumnDataTypesEnd();
    }

    if (!tables.isEmpty())
    {
      handler.handleTablesStart();
      for (final Table table: tables)
      {
        handler.handle(table);
      }
      handler.handleTablesEnd();
    }

    if (!routines.isEmpty())
    {
      handler.handleRoutinesStart();
      for (final Routine routine: routines)
      {
        handler.handle(routine);
      }
      handler.handleRoutinesEnd();
    }

    if (!sequences.isEmpty())
    {
      handler.handleSequencesStart();
      for (final Sequence sequence: sequences)
      {
        handler.handle(sequence);
      }
      handler.handleSequencesEnd();
    }

    if (!synonyms.isEmpty())
    {
      handler.handleSynonymsStart();
      for (final Synonym synonym: synonyms)
      {
        handler.handle(synonym);
      }
      handler.handleSynonymsEnd();
    }

    handler.end();
  }

}
