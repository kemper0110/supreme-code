package net.danil;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import net.danil.generated.Testsuite;

public class JunitParser {
    public static void main(String... args) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("net.danil.generated");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final var xmlReport = JunitParser.class.getClassLoader().getResourceAsStream("TEST-net.danil.TwoSumTest.xml");
        var testsuite = (Testsuite) unmarshaller.unmarshal(xmlReport);
        System.out.println(testsuite);
    }
}
