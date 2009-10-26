/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2009, Sualeh Fatehi.
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
package schemacrawler.main;


import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.Version;
import schemacrawler.main.dbconnector.BundledDriverDatabaseConnector;
import schemacrawler.main.dbconnector.DatabaseConnector;
import schemacrawler.main.dbconnector.DatabaseConnectorException;
import schemacrawler.main.dbconnector.PropertiesDataSourceDatabaseConnector;
import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.Commands;
import schemacrawler.tools.OutputOptions;

/**
 * Utility for parsing the SchemaCrawler command line.
 * 
 * @author Sualeh Fatehi
 */
public class SchemaCrawlerCommandLine
{

  private static final Logger LOGGER = Logger
    .getLogger(SchemaCrawlerCommandLine.class.getName());

  private final Commands commands;
  private final Config config;
  private final SchemaCrawlerOptions schemaCrawlerOptions;
  private final OutputOptions outputOptions;
  private final DatabaseConnector databaseConnector;

  public SchemaCrawlerCommandLine(final Commands commands,
                                  final Config config,
                                  final DatabaseConnector databaseConnector,
                                  final OutputOptions outputOptions)
  {
    this.commands = commands;
    this.config = config;
    this.databaseConnector = databaseConnector;
    this.outputOptions = outputOptions;
    schemaCrawlerOptions = new SchemaCrawlerOptions();
  }

  /**
   * Loads objects from command line options.
   * 
   * @param args
   *        Command line arguments.
   * @throws SchemaCrawlerException
   *         On an exception
   */
  public SchemaCrawlerCommandLine(final String[] args,
                                  final HelpOptions helpOptions)
    throws SchemaCrawlerException
  {
    this(args, helpOptions, null);
  }

  /**
   * Loads objects from command line options. Optionally loads the
   * config from the classpath.
   * 
   * @param args
   *        Command line arguments.
   * @param configResource
   *        Config resource.
   * @throws SchemaCrawlerException
   *         On an exception
   */
  public SchemaCrawlerCommandLine(final String[] args,
                                  final HelpOptions helpOptions,
                                  final String configResource)
    throws SchemaCrawlerException
  {
    final ApplicationOptions applicationOptions;
    if (args != null && args.length > 0)
    {
      applicationOptions = new ApplicationOptionsParser(args).getOptions();
      commands = new CommandParser(args).getOptions();
      outputOptions = new OutputOptionsParser(args).getOptions();
    }
    else
    {
      applicationOptions = new ApplicationOptions();
      commands = new Commands();
      outputOptions = new OutputOptions();
    }

    if (applicationOptions.isShowHelp())
    {
      helpOptions.showHelp();
      System.exit(0);
    }

    applicationOptions.applyApplicationLogLevel();
    LOGGER.log(Level.INFO, Version.about());
    LOGGER.log(Level.CONFIG, "Command line: " + Arrays.toString(args));
    try
    {
      if (!schemacrawler.utility.Utility.isBlank(configResource))
      {
        config = Config.load(SchemaCrawlerCommandLine.class
          .getResourceAsStream(configResource));
        databaseConnector = new BundledDriverDatabaseConnector(args, config);
      }
      else
      {
        if (args != null && args.length > 0)
        {
          config = new ConfigParser(args).getOptions();
        }
        else
        {
          config = new Config();
        }
        databaseConnector = new PropertiesDataSourceDatabaseConnector(args,
                                                                      config);
      }
    }
    catch (final DatabaseConnectorException e)
    {
      throw new SchemaCrawlerException("Cannot create a database connector - re-run with -help for help",
                                       e);
    }

    schemaCrawlerOptions = new SchemaCrawlerOptionsParser(args,
                                                          config)
      .getOptions();
  }

  /**
   * Creates the connection.
   * 
   * @return Database connection
   * @throws DatabaseConnectorException
   *         On an exception
   */
  public Connection createConnection()
    throws SchemaCrawlerException
  {
    try
    {
      return databaseConnector.createConnection();
    }
    catch (final DatabaseConnectorException e)
    {
      throw new SchemaCrawlerException(e);
    }
  }

  /**
   * Gets the commands.
   * 
   * @return Commands.
   */
  public Commands getCommands()
  {
    return commands;
  }

  /**
   * Gets the config.
   * 
   * @return Config.
   */
  public Config getConfig()
  {
    return new Config(config);
  }

  /**
   * Gets the output options.
   * 
   * @return Output options.
   */
  public OutputOptions getOutputOptions()
  {
    return outputOptions.duplicate();
  }

  public SchemaCrawlerOptions getSchemaCrawlerOptions()
  {
    return schemaCrawlerOptions;
  }

}
