package me.botsko.prism.database;

import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.measurement.TimeTaken;

/**
 * A Select Query returns a query that selects a data set.
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/04/2019.
 */
public interface SelectQuery {
    String getQuery(PrismParameters parameters, boolean shouldGroup);

    void setParameters(PrismParameters parameters);

    void setShouldGroup(boolean shouldGroup);

    QueryResult executeSelect(TimeTaken eventTimer);
    /*    These methods should exist in a selectQuery and it should extend QueryBuilder
    String select();
    String where() ;
    void worldCondition() ;
    void actionCondition();
    void playerCondition();
    void radiusCondition();
    void blockCondition() ;
    void entityCondition() ;
    void timeCondition() ;
    void keywordCondition() ;
    void coordinateCondition();DERBY_LOCAL
    String buildWhereConditions();
    String group();
    String order() ;
    String limit();
    String buildMultipleConditions(HashMap<String, MatchRule> origValues, String field_name, String format);
    String buildGroupConditions(String fieldname, String[] arg_values, String matchFormat, String matchType,
                                                   String dataFormat) ;
    void buildRadiusCondition(Vector minLoc, Vector maxLoc) ;
    String buildTimeCondition(Long dateFrom, String equation) ;*/
}

