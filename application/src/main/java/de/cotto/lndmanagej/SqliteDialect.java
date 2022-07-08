package de.cotto.lndmanagej;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StringType;

import java.sql.Types;
 
@SuppressWarnings({"unused", "PMD.TooManyMethods"})
public class SqliteDialect extends Dialect {

    private static final String INTEGER = "integer";
    private static final String TINYINT = "tinyint";
    private static final String SMALLINT = "smallint";
    private static final String BIGINT = "bigint";
    private static final String FLOAT = "float";
    private static final String REAL = "real";
    private static final String DOUBLE = "double";
    private static final String NUMERIC = "numeric";
    private static final String DECIMAL = "decimal";
    private static final String CHAR = "char";
    private static final String VARCHAR = "varchar";
    private static final String LONGVARCHAR = "longvarchar";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String TIMESTAMP = "timestamp";
    private static final String BLOB = "blob";

    public SqliteDialect() {
        super();
        registerColumnType(Types.BIT, INTEGER);
        registerColumnType(Types.TINYINT, TINYINT);
        registerColumnType(Types.SMALLINT, SMALLINT);
        registerColumnType(Types.INTEGER, INTEGER);
        registerColumnType(Types.BIGINT, BIGINT);
        registerColumnType(Types.FLOAT, FLOAT);
        registerColumnType(Types.REAL, REAL);
        registerColumnType(Types.DOUBLE, DOUBLE);
        registerColumnType(Types.NUMERIC, NUMERIC);
        registerColumnType(Types.DECIMAL, DECIMAL);
        registerColumnType(Types.CHAR, CHAR);
        registerColumnType(Types.VARCHAR, VARCHAR);
        registerColumnType(Types.LONGVARCHAR, LONGVARCHAR);
        registerColumnType(Types.DATE, DATE);
        registerColumnType(Types.TIME, TIME);
        registerColumnType(Types.TIMESTAMP, TIMESTAMP);
        registerColumnType(Types.BINARY, BLOB);
        registerColumnType(Types.VARBINARY, BLOB);
        registerColumnType(Types.LONGVARBINARY, BLOB);
        registerColumnType(Types.BLOB, BLOB);
        registerColumnType(Types.CLOB, "clob");
        registerColumnType(Types.BOOLEAN, INTEGER);
 
        registerFunction("concat", new VarArgsSQLFunction(StringType.INSTANCE, "", "||", ""));
        registerFunction("mod", new SQLFunctionTemplate(StringType.INSTANCE, "?1 % ?2"));
        registerFunction("substr", new StandardSQLFunction("substr", StringType.INSTANCE));
        registerFunction("substring", new StandardSQLFunction("substr", StringType.INSTANCE));
    }
 
    public boolean supportsIdentityColumns() {
        return true;
    }
 
    public boolean hasDataTypeInIdentityColumn() {
        return false; // As specify in NHibernate dialect
    }
 
    public String getIdentityColumnString() {
        return INTEGER;
    }
 
    public String getIdentitySelectString() {
        return "select last_insert_rowid()";
    }
 
    public boolean supportsTemporaryTables() {
        return true;
    }
 
    public String getCreateTemporaryTableString() {
        return "create temporary table if not exists";
    }
 
    public boolean dropTemporaryTableAfterUse() {
        return false;
    }

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    @Override
    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }

    @Override
    public boolean supportsUnionAll() {
        return true;
    }

    @Override
    public boolean hasAlterTable() {
        return false; // As specify in NHibernate dialect
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public String getForUpdateString() {
        return "";
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    @Override
    public String getDropForeignKeyString() {
        throw new UnsupportedOperationException("No drop foreign key syntax supported by SQLiteDialect");
    }

    @Override
    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable,
            String[] primaryKey, boolean referencesPrimaryKey) {
        throw new UnsupportedOperationException("No add foreign key syntax supported by SQLiteDialect");
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        throw new UnsupportedOperationException("No add primary key syntax supported by SQLiteDialect");
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }
}