package org.hadatac.filters;

import akka.stream.Materializer;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.mvc.Session;
import play.api.mvc.request.RequestAttrKey;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class HadatacLoggingFilter extends Filter {

    private static final Logger log = LoggerFactory.getLogger("access");

    @Inject
    public HadatacLoggingFilter(Materializer materializer) {
        super(materializer);
    }

    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> nextFilter,
            Http.RequestHeader requestHeader) {

        long startTime = System.currentTimeMillis();

        Session session = requestHeader.asScala().session();
        String sessionEmail = "unknown";
        if ( session != null && requestHeader.uri().contains("/hadatac/downloader/checkcompletion") == false &&
                requestHeader.uri().contains("hadatac/downloader") == false && requestHeader.uri().contains("hadatac/assets/") == false ) {
            // System.out.println("------------------------------------->>>>> retrieving users: " + requestHeader.uri().toString());
            SysUser user = AuthApplication.getLocalUser(session.asJava());
            if (user != null) {
                sessionEmail = user.getEmail();
            }
        }

        final String userEmail = sessionEmail;
        return nextFilter.apply(requestHeader)
                .thenApply(
                        result -> {
                            long requstTime = System.currentTimeMillis() - startTime;
                            String msg = String.format("user %s requests %s %s took %sms returned %s",
                                    userEmail,
                                    requestHeader.method(),
                                    requestHeader.uri(),
                                    requstTime,
                                    result.status());
                            if ( !msg.contains("/assets/") && requestHeader.uri() != null
                                    && requestHeader.uri().contains("/hadatac/downloader/checkcompletion") == false
                                    && requestHeader.uri().contains("/hadatac/downloader") == false ) {
                                log.info(msg);
                            }
                            return result.withHeader("Request-Time", "" + requstTime);
                        });
    }

}