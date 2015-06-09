import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.commons.lang3.StringUtils;
import org.openjena.riot.WebContent;

import java.util.LinkedList;
import java.util.List;

public class SPARQLHelper {


    //TODO merge functions and find generalisation for query

    public boolean isDbpediaEntity(TableEntry entry) {
        List<String> properties = new LinkedList<>();
        String name= entry.getRDFTitle();
        String queryString;

        if (entry.isLiteral()) {
            queryString = isLiteralEntityQueryString(name);
        } else {
            queryString = isLiteralEntityQueryString(name);
        }

        Query query = QueryFactory.create(queryString);

        QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        qexec.setSelectContentType(WebContent.contentTypeResultsJSON);
        ResultSet results = qexec.execSelect();

        Boolean temp = results.hasNext();

        qexec.close();
        return temp;
    }

    public List<String> getPropertiesForEntity(String name) {
        String queryString = buildPredicateQuery(name);
        List<String> properties = new LinkedList<>();
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);

        try {
            ResultSet predicates = qexec.execSelect();
            //ResultSetFormatter.out(System.out, predicates, query);

            while (predicates.hasNext()) {
                QuerySolution solution = predicates.next();
                String solString = solution.toString();
                String property = (StringUtils.substringBetween(solString, "property/", ">"));
                if (property != null) {
                    properties.add(property);
                }
            }
        }
        finally {
            qexec.close();
        }
        return properties;
    }

    public List<String> getPredicatesBetweenEntities(String nameOfFirstEntity, String nameOfSecondEntity) {
        String queryString = buildPredicateBetweenEntitiesQuery(nameOfFirstEntity, nameOfSecondEntity);

        Query query = QueryFactory.create(queryString);

        QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        qexec.setSelectContentType(WebContent.contentTypeResultsJSON);

        List<String> matchedProperties = new LinkedList<>();

        try {
            ResultSet predicates = qexec.execSelect();
            ResultSetFormatter.out(System.out, predicates, query);

            while (predicates.hasNext()) {
                QuerySolution solution = predicates.next();
                matchedProperties.add(solution.toString());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            qexec.close();
        }
        return matchedProperties;
    }
    public List<String> getPredicatesBetweenEntityAndLiteral(String entityName, String literal) {
        String queryString = buildPredicateBetweenEntityAndLiteral(entityName, literal);

        Query query = QueryFactory.create(queryString);

        QueryEngineHTTP qexec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
        qexec.setSelectContentType(WebContent.contentTypeResultsJSON);

        List<String> matchedProperties = new LinkedList<>();

        try {
            ResultSet predicates = qexec.execSelect();
            ResultSetFormatter.out(System.out, predicates, query);

            while (predicates.hasNext()) {
                QuerySolution solution = predicates.next();
                matchedProperties.add(solution.toString());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        finally {
            qexec.close();
        }
        return matchedProperties;
    }

    private String buildPredicateBetweenEntitiesQuery(String name1, String name2) {

        String query =
        "SELECT ?predicate FROM <http://dbpedia.org> WHERE { " +
                "" +
                "<http://dbpedia.org/resource/" + name1 + "> ?predicate <http://dbpedia.org/resource/" + name2  + ">" + ". " +
                "}";
        return query;
    }

    private String buildPredicateBetweenEntityAndLiteral(String entityName, String literal) {
        if (!StringUtils.isNumeric(literal)) {
            literal = "\"" + literal + "\"";
        }
        String query =
                "SELECT ?predicate { " +
                        "<http://dbpedia.org/resource/" + entityName + "> ?predicate "  + literal +"." +
                        "}";
        return query;

    }
    private String getSimpleString() {
        return "SELECT ?s WHERE {?s ?o ?p.} LIMIT 5";
    }

    private String buildPredicateQuery(String name) {
        String query =
                "SELECT ?predicate { " +
                        "<http://dbpedia.org/resource/" + name + "> ?predicate ?object. " +
                        "}";
        return query;
    }

    private String isLiteralEntityQueryString(String name) {
        String query =
        "SELECT * {{" +
            "SELECT ?predicate {" + "<http://dbpedia.org/resource/" + name + "> ?predicate ?object. " + "}" +
        "}" +
         "UNION" + "{ "+ "SELECT ?subject " +
                "{" + "?subject ?predicate " + "<http://dbpedia.org/resource/" + name +">}"
       + "} }";

        return query;
    }

}
