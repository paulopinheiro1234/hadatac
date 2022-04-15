package org.hadatac.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public class GSPClient {
    private final URI endpoint;

    public GSPClient(URI endpoint) {
        this.endpoint = endpoint;
    }

    public GSPClient(String endpointUri) throws URISyntaxException {
        this(new URI(endpointUri));
    }

    public void postInputStream(Supplier<? extends InputStream> streamSupplier, String mimeType, String graph) {
        try {
            URIBuilder requestUriBuilder = new URIBuilder(endpoint);
            if (StringUtils.isNotBlank(graph)) {
                requestUriBuilder.addParameter("graph", graph);
            }
            URI requestUri = requestUriBuilder.build();
            System.out.println("REQUEST URI: " + requestUri);
            HttpRequest request = HttpRequest.newBuilder(requestUri)
                    .POST(HttpRequest.BodyPublishers.ofInputStream(streamSupplier))
                    .header("Content-Type", mimeType)
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Resp: " + response.statusCode() + ". '" + response.body() + "'");
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void postFile(File file, String mimeType, String graph) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        postInputStream(() -> fileInputStream, mimeType, graph);
    }

    public void postModel(Model model) {
        final RDFFormat defaultFormat = RDFFormat.TURTLE;
        for (Resource context : model.contexts()) {
            Model subGraph = model.filter(null, null, null, context);
            postInputStream(
                    () -> new SerializedModelInputStream(subGraph, defaultFormat),
                    defaultFormat.getDefaultMIMEType(),
                    Optional.ofNullable(context).map(Resource::toString).orElse(null)
            );
        }
    }

    private static class SerializedModelInputStream extends InputStream {
        protected byte[] buf;
        protected int pos;
        protected int count;
        private final ByteArrayOutputStream outputStream;
        private final RDFWriter writer;
        private final Iterator<Statement> rows;

        public SerializedModelInputStream(Model model, RDFFormat format) {
            this.outputStream = new ByteArrayOutputStream();
            this.writer = Rio.createWriter(format, outputStream);
            writer.startRDF();
            this.rows = model.iterator();
            this.count = -1;
        }

        private void reloadBuffer() {
            while (rows.hasNext() && outputStream.size() == 0) {
                writer.handleStatement(rows.next());
                if (!rows.hasNext()) {
                    writer.endRDF();
                }
            }
            buf = outputStream.toByteArray();
            outputStream.reset();
            pos = 0;
            count = buf.length;
        }

        public int read() {
            if (pos >= count) {
                reloadBuffer();
            }
            if (pos < count) {
                return (buf[pos++] & 0xff);
            } else {
                return -1;
            }
        }

    }

}
