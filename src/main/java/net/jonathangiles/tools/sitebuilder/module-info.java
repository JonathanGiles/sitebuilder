module net.jonathangiles.tools.sitebuilder {
    requires org.commonmark;
    requires org.commonmark.ext.front.matter;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.xml;

    exports net.jonathangiles.tools.sitebuilder;
    exports net.jonathangiles.tools.sitebuilder.models;
}