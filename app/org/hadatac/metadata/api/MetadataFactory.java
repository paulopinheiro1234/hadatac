package org.hadatac.metadata.api;

import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.hadatac.metadata.loader.URIUtils;


public class MetadataFactory {
    
    public static Model createModel(List<Map<String, Object>> rows, String namedGraphUri) {        
        ModelFactory modelFactory = new LinkedHashModelFactory();
        Model model = modelFactory.createEmptyModel();

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI namedGraph = null;
        if (!namedGraphUri.isEmpty()) {
            namedGraph = factory.createIRI(namedGraphUri);
        }

        for (Map<String, Object> row : rows) {
            IRI sub = factory.createIRI(URIUtils.replacePrefixEx((String)row.get("hasURI")));
            for (String key : row.keySet()) {
                if (!"hasURI".equals(key)) {
                    IRI pred = null;
                    if ("a".equals(key)) {
                        pred = factory.createIRI(URIUtils.replacePrefixEx("rdf:type"));
                    } else {
                        pred = factory.createIRI(URIUtils.replacePrefixEx(key));
                    }

                    String cellValue = (String)row.get(key);
                    if (URIUtils.isValidURI(cellValue)) {
                        IRI obj = factory.createIRI(URIUtils.replacePrefixEx(cellValue));

                        if (namedGraph == null) {
                            model.add(sub, pred, obj);
                        } else {
                            model.add(sub, pred, obj, (Resource)namedGraph);
                        }
                    } else {
                        if (cellValue == null) {
                            cellValue = "NULL";
                        }
                        
                        Literal obj = factory.createLiteral(
                                cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));

                        if (namedGraph == null) {
                            model.add(sub, pred, obj);
                        } else {
                            model.add(sub, pred, obj, (Resource)namedGraph);
                        }
                    }
                }
            }
        }

        return model;
    }
    
    public static int commitModelToTripleStore(Model model, String endpointUrl) {
        Repository repo = new SPARQLRepository(endpointUrl);
        repo.init();
        
        RepositoryConnection con = null;
        try {
            con = repo.getConnection();
            con.add(model);
        } catch (RepositoryException e) {
            e.printStackTrace();
            return -1;
        } finally {
            con.close();
        }
        
        return model.size();
    }
}
