package net.anno;

import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.model.Sqlresponse;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FtClient}.
 */
public class FtClientTest extends TestCase {

  @Mock Fusiontables fusiontables;
  @Mock Fusiontables.Query query;
  @Mock Fusiontables.Query.Sql sql;
  @Mock Fusiontables.Query.SqlGet sqlGet;
  FtClient ftClient;

  @Override
  protected void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(fusiontables.query()).thenReturn(query);
    when(query.sql(anyString())).thenReturn(sql);
    when(query.sqlGet(anyString())).thenReturn(sqlGet);
    ftClient = new FtClient(fusiontables);
  }

  public void testApp() throws Exception {
    Sqlresponse sqlresponse =
        new Sqlresponse().setColumns(asList("A", "B")).setRows(asList(asList((Object) "a", "b")));
    when(sqlGet.execute()).thenReturn(sqlresponse);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    ftClient.run(new PrintStream(out));

    assertThat(out.toString()).contains("[A, B]");
    assertThat(out.toString()).contains("[a, b]");
  }
}
