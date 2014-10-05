package org.dartlang.service;

/**
 * Created by johnmccutchan on 10/4/14.
 */
public class Response {
    public boolean isError() { return this instanceof ServiceError; }
    public boolean isException() { return this instanceof ServiceException; }
    public boolean isObject() { return this instanceof ServiceObject; }
}
