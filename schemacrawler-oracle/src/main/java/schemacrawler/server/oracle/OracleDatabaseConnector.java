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
package schemacrawler.server.oracle;


import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.databaseconnector.DatabaseSystemConnector;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.options.DatabaseServerType;

public final class OracleDatabaseConnector
  extends DatabaseConnector
{

  private static final class OracleDatabaseSystemConnector
    extends DatabaseSystemConnector
  {
    private OracleDatabaseSystemConnector(String configResource,
                                          String informationSchemaViewsResourceFolder)
    {
      super(configResource, informationSchemaViewsResourceFolder);
    }

    @Override
    public Executable newPreExecutable()
      throws SchemaCrawlerException
    {
      return new OraclePreExecutable();
    }
  }

  public OracleDatabaseConnector()
  {
    super(new DatabaseServerType("oracle", "Oracle"),
          "/help/Connections.oracle.txt",
          new OracleDatabaseSystemConnector("/schemacrawler-oracle.config.properties",
                                            "/oracle.information_schema"));

    System.setProperty("oracle.jdbc.Trace", "true");
  }

}
