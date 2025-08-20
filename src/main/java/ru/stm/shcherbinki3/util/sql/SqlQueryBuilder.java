package ru.stm.shcherbinki3.util.sql;

import lombok.Getter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class SqlQueryBuilder {
    private final StringBuilder sql;
    @Getter
    private final MapSqlParameterSource params;

    public SqlQueryBuilder(String baseQuery) {
        this.sql = new StringBuilder(baseQuery);
        this.params = new MapSqlParameterSource();
    }

    public static MapSqlParameterSource[] toBatchParams(List<?> entities,
                                                        BiConsumer<Object, MapSqlParameterSource> paramMapper) {
        return entities.stream()
                .map(entity -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    paramMapper.accept(entity, params);
                    return params;
                })
                .toArray(MapSqlParameterSource[]::new);
    }

    public SqlQueryBuilder addValue(String paramName, Object value) {
        params.addValue(paramName, value);
        return this;
    }

    public SqlQueryBuilder addFilterWhere(String condition, String paramName, Object paramValue) {
        sql.append(" WHERE 1=1 ");
        return addFilter(condition, paramName, paramValue);
    }

    public SqlQueryBuilder addFilter(String condition, String paramName, Object paramValue) {
        if (paramValue != null) {
            if (paramValue instanceof String str && str.isBlank()) {
                return this;
            }
            sql.append(" AND ")
                    .append(condition);
            params.addValue(paramName, paramValue);
        }
        return this;
    }

    public SqlQueryBuilder addFilter(String condition) {
        sql.append(" AND ")
                .append(condition);

        return this;
    }

    public SqlQueryBuilder addOneDayDateFilter(LocalDate date, String dateField) {
        if (date != null) {
            sql.append(" AND ")
                    .append(dateField)
                    .append(" >= :startDate AND ")
                    .append(dateField)
                    .append(" < :endDate");
            params.addValue("startDate", date.atStartOfDay());
            params.addValue("endDate", date.plusDays(1)
                    .atStartOfDay());
        } else {
            sql.append(" AND ")
                    .append(dateField)
                    .append(" >= :currentTime");
            params.addValue("currentTime", LocalDateTime.now());
        }
        return this;
    }

    public SqlQueryBuilder addDateRangeFilter(LocalDate after, LocalDate before, String dateField) {
        if (after != null) {
            sql.append(" AND ")
                    .append(dateField)
                    .append(" >= :after");
            params.addValue("after", after.atStartOfDay());
        }
        if (before != null) {
            sql.append(" AND ")
                    .append(dateField)
                    .append(" < :before");
            params.addValue("before", before.plusDays(1)
                    .atStartOfDay());
        }
        return this;
    }

    public SqlQueryBuilder addPagination(Pageable pageable, String tableAlias, Set<String> allowedSortColumns) {
        String sortBy = allowedSortColumns.contains(pageable.sortBy()) ? pageable.sortBy() : "id";
        sql.append(" ORDER BY ")
                .append(tableAlias)
                .append(".")
                .append(sortBy)
                .append(" ")
                .append(pageable.direction()
                                .name())
                .append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", pageable.size());
        params.addValue("offset", pageable.offset());
        return this;
    }

    public SqlQueryBuilder addUpdateFields(Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }
        List<String> setClauses = fields.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getKey() + " = :" + entry.getKey())
                .toList();
        if (setClauses.isEmpty()) {
            throw new IllegalArgumentException("At least one non-null field must be provided for update");
        }
        sql.append(" SET ")
                .append(String.join(", ", setClauses));
        params.addValues(fields);
        return this;
    }

    public String getSql() {
        return sql.toString();
    }

    @Override
    public String toString() {
        return sql.toString();
    }
}