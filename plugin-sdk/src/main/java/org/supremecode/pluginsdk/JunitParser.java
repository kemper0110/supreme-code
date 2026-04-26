package org.supremecode.pluginsdk;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.supremecode.pluginsdk.junit.Testsuite;
import org.supremecode.pluginsdk.junit.Testsuites;

import java.io.IOException;

public class JunitParser {
    protected JAXBContext jaxbContext;

    public JunitParser() throws JAXBException {
        jaxbContext = JAXBContext.newInstance("org.supremecode.pluginsdk.junit");
    }

    public Testsuite parseTestsuite(String xmlReport) throws JAXBException, IOException {
        final var unmarshaller = jaxbContext.createUnmarshaller();
        final var object = unmarshaller.unmarshal(IOUtils.toInputStream(xmlReport, "UTF-8"));
        return (Testsuite) object;
    }

    public Testsuites parseTestsuites(String xmlReport) throws JAXBException, IOException {
        final var unmarshaller = jaxbContext.createUnmarshaller();
        final var object = unmarshaller.unmarshal(IOUtils.toInputStream(xmlReport, "UTF-8"));
        return (Testsuites) object;
    }
}
