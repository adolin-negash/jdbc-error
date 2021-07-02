package adolin.testjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.util.Assert;

/**
 * Application entry point
 *
 * @author Adolin Negash 02.07.2021
 */
public class AppTest {

    private static final String LIST_RESULT_NAME = "list";

    static class Dto {

        private int field1;

        private String field2;
    }

    public static void main(String[] args) {
        final DataSource dataSource = createDataSource();

        assertResult(testCaseOne(dataSource), "CASE #1");
        assertResult(testCaseTwo(dataSource), "CASE #2");
    }

    private static void assertResult(SimpleJdbcCall jdbcCall, String caseName) {
        try {
            final Map<String, Object> result = jdbcCall.execute(Collections.emptyMap());
            final Object obj = result.get(LIST_RESULT_NAME);
            Assert.isInstanceOf(List.class, obj);
            final List list = (List)obj;
            Assert.isTrue(list.size() == 2, "invalid size");
            Assert.isInstanceOf(Dto.class, list.get(0));
            Assert.isInstanceOf(Dto.class, list.get(1));
            System.out.printf("OK ON %s%n", caseName);
        } catch (Exception e) {
            System.out.printf("FAIL ON CASE %s - %s: %s%n",
                caseName, e.getClass(), e.getMessage());
        }
    }

    private static void assertDto(Object obj, int expectedField1, String expectedField2) {
        Assert.isInstanceOf(Dto.class, obj);
        final Dto dto = (Dto)obj;
        Assert.isTrue(expectedField1 == dto.field1, "field1 differs");
        Assert.isTrue(Objects.equals(expectedField2, dto.field2), "field2 differs");
    }

    private static PGSimpleDataSource createDataSource() {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setDatabaseName("test");
        dataSource.setUser("postgres");
        dataSource.setPassword("pA**w0rd");
        dataSource.setPortNumbers(new int[]{5432});
        return dataSource;
    }

    private static SimpleJdbcCall testCaseOne(DataSource dataSource) {
        return new SimpleJdbcCall(dataSource)
            .withSchemaName("public")
            .withFunctionName("fn_test")
            .returningResultSet("list", AppTest::map) // NOT WORKING
            ;
    }

    private static SimpleJdbcCall testCaseTwo(DataSource dataSource) {
        final SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
            .withSchemaName("public")
            .withProcedureName("fn_test")
            .withoutProcedureColumnMetaDataAccess() // WITHOUT IT FAILS TO CONVERT ResultSet TO List
            ;

        jdbcCall.addDeclaredParameter(new SqlReturnResultSet(LIST_RESULT_NAME, AppTest::map));

        return jdbcCall;
    }

    private static Dto map(ResultSet resultSet, int rowId) throws SQLException {
        final Dto dto = new Dto();
        dto.field1 = resultSet.getInt("field1");
        dto.field2 = resultSet.getString("field2");
        return dto;
    }
}
