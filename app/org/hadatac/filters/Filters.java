package org.hadatac.filters;

import play.http.DefaultHttpFilters;

import javax.inject.Inject;

public class Filters extends DefaultHttpFilters {

    @Inject
    public Filters(HadatacLoggingFilter loggingFilter) {
        super(loggingFilter);
    }
}

/*
to make sure you are not missing/overriding existing filters, see here:
https://www.playframework.com/documentation/2.8.x/Filters
 */