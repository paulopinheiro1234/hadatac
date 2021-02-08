package org.hadatac.console.activities;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.UserDownloadActivity;
import org.hadatac.console.models.UserSearchActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class SearchActivityAnnotationAction extends Action<SearchActivityAnnotation> {

    private static final Logger log = LoggerFactory.getLogger(SearchActivityAnnotationAction.class);

    public CompletionStage<Result> call(Http.Context context) {

        // get the request URI
        String[] requestURI = new String[1];
        context.request().getHeaders().get("Raw-Request-URI").ifPresent( uri -> requestURI[0] = uri );

        // if for some reason there is no request URI (should never happen), then do nothing
        if ( requestURI[0] == null ) return delegate.call(context);

        // if this is a search, then do search-related annotation
        if ( !requestURI[0].toLowerCase().contains("download") ) {
            processSearchActivity(context);
        } else {  // this is a download
            processDownloadActivity(context);
        }

        return delegate.call(context);

    }

    private void processSearchActivity(Http.Context context) {

        String jsonPayload = "";
        String sessionEmail = "";
        String timeStr = ZonedDateTime.now(ZoneId.systemDefault()).toString();

        Map<String, String[]> parameters = context.request().body().asFormUrlEncoded();
        if ( parameters != null ) {
            String[] payload = (String[])(parameters.get("facets"));
            if ( payload != null && payload.length > 0  ) {
                jsonPayload = payload[0];
                log.info("=======> here is the search details: " + jsonPayload);
            }
        }

        // get the user
        SysUser user = AuthApplication.getLocalUser(context.session());
        if (user != null) {
            sessionEmail = user.getEmail();
            log.info("the current users is: " + sessionEmail);
        }

        // write to solr
        UserSearchActivity userSearchActivity = new UserSearchActivity();
        userSearchActivity.setUser_email(sessionEmail);
        userSearchActivity.setSubmission_time(timeStr);
        userSearchActivity.setJson_query(jsonPayload);
        userSearchActivity.save();

        // write the id to the session
        String id_s = userSearchActivity.getId_s();
        context.session().put("user_search_id", id_s);

    }

    private void processDownloadActivity(Http.Context context) {

        String sessionEmail = "";
        String timeStr = ZonedDateTime.now(ZoneId.systemDefault()).toString();

        // get the reference id from session, this is the id representing the last search of the current user
        String userSearchIdRef = null;
        if ( context != null && context.session() != null ) {
            userSearchIdRef = context.session().get("user_search_id");
        }

        // if there is no id in the session, ignore this one
        if ( userSearchIdRef == null || userSearchIdRef.length() == 0 ) return;

        // get the user
        SysUser user = AuthApplication.getLocalUser(context.session());
        if (user != null) {
            sessionEmail = user.getEmail();
            log.info("the current users is: " + sessionEmail);
        }

        // write to solr
        UserDownloadActivity userDownloadActivity = new UserDownloadActivity();
        userDownloadActivity.setUser_email(sessionEmail);
        userDownloadActivity.setUser_search_ref(userSearchIdRef);
        userDownloadActivity.setSubmission_time(timeStr);
        userDownloadActivity.save();
    }
}
