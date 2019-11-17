package schemacrawler.tools.lint.executable;


import static java.util.Objects.requireNonNull;
import static sf.util.Utility.isBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.options.OutputOptions;

public class LintReportJsonBuilder
  extends BaseLintReportJacksonBuilder
{

  LintReportJsonBuilder(final OutputOptions outputOptions)
    throws SchemaCrawlerException
  {
    super(outputOptions);
  }

  @Override
  protected ObjectMapper newObjectMapper()
  {
    return new ObjectMapper();
  }

  @Override
  public boolean canBuildReport(final LintOptions options,
                                final OutputOptions outputOptions)
  {
    final boolean canBuildReport;
    final String outputFormatValue = outputOptions.getOutputFormatValue();
    canBuildReport =
      !isBlank(outputFormatValue) && outputFormatValue.equalsIgnoreCase("json");
    return canBuildReport;
  }

}
